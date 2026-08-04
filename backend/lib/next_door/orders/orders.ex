defmodule NextDoor.Orders do
  alias NextDoor.{Account, Order, OrderProduct, Inventory, Product, Store, Repo}
  alias NextDoor.Validators
  import Ecto.Query

  @valid_transitions %{
    "ESPERANDO" => ["ACEITO", "RECUSADO"],
    "ACEITO" => ["PREPARACAO", "CANCELADO"],
    "PREPARACAO" => ["ROTA", "CANCELADO"],
    "ROTA" => ["CONCLUIDO", "CANCELADO"],
    "CONCLUIDO" => [],
    "CANCELADO" => []
  }

  def get_orders_by_store(%{owner_id: owner_id}) do
    result =
      from(s in Store,
        join: o in Order,
        on: s.id == o.store_id,
        where: s.owner_id == ^owner_id,
        select: %{
          id: o.id,
          total: o.total,
          payment_method: o.payment_method,
          status: o.status_order
        }
      )
      |> Repo.all()

    {:ok, result}
  end

  def get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
    with {:ok, _} <- Validators.parse_uuid(order_id) do
      from(o in Order,
        join: s in Store,
        on: s.id == o.store_id,
        where: s.owner_id == ^owner_id and o.id == ^order_id,
        select: o
      )
      |> Repo.one()
      |> Repo.preload(order_product: [:product], account: [], address: [])
      |> case do
        nil -> {:error, :record_not_found}
        order -> {:ok, order}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def get_order_by_customer(%{order_id: order_id, customer_id: customer_id}) do
    with {:ok, _} <- Validators.parse_uuid(order_id) do
      from(o in Order,
        where: o.id == ^order_id and o.account_id == ^customer_id,
        select: o
      )
      |> Repo.one()
      |> Repo.preload([:address, :products, :account])
      |> case do
        nil -> {:error, :record_not_found}
        order -> {:ok, order}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def get_orders_by_customer(%{customer_id: customer_id}) do
    result =
      Repo.all_by(Order, account_id: customer_id)
      |> Repo.preload([:address, :products, :account])

    {:ok, result}
  end

  def update_status_order(order_id, owner_id, %{before: status_before, after: status_after}) do
    with {:ok, _} <- Validators.parse_uuid(order_id) do
      query =
        from(s in Store,
          join: o in Order,
          on: s.id == o.store_id,
          where: s.owner_id == ^owner_id and o.id == ^order_id,
          select: o
        )

      with %Order{} = order <- Repo.one(query),
           true <- order.status_order == status_before,
           true <- valid_transition?(status_before, status_after) do
        order
        |> Order.update_changeset(%{status_order: status_after})
        |> Repo.update()
      else
        false -> {:error, :invalid_transition}
        nil -> {:error, :not_found}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def create_order(%{
        store_id: store_id,
        customer_id: customer_id,
        products: products,
        payment_method: payment_method
      }) do
    with {:ok, _} <- Validators.parse_uuid(store_id),
         {:ok, store} <- get_store(store_id),
         {:ok, items} <- validate_items(products),
         {:ok, payment_method} <- validate_payment_method(payment_method),
         {:ok, products} <- fetch_products(store.id, items),
         {:ok, address_id} <- get_customer_address(customer_id) do
      total = compute_total(products, items)

      Repo.transaction(fn ->
        with {:ok, order} <- insert_order(store, customer_id, address_id, total, payment_method),
             {:ok, order} <- insert_order_products(order, items),
             :ok <- decrement_inventory(items) do
          order
          |> Repo.preload(order_product: [:product])
        else
          {:error, reason} -> Repo.rollback(reason)
        end
      end)
    end
  end

  defp get_store(store_id) do
    case Repo.get(Store, store_id) do
      nil -> {:error, :store_not_found}
      store -> {:ok, store}
    end
  end

  defp validate_items(products) when is_list(products) and products != [] do
    with {:ok, items} <- build_items(products),
         {:ok, items} <- reject_duplicate_products(items) do
      {:ok, items}
    end
  end

  defp validate_items(_products), do: {:error, :invalid_payload}

  @payment_methods ~w(CC CD PIX DINHEIRO)

  defp validate_payment_method(payment_method) when payment_method in @payment_methods,
    do: {:ok, payment_method}

  defp validate_payment_method(_payment_method), do: {:error, :invalid_payload}

  defp build_items(products) do
    Enum.reduce_while(products, [], fn
      %{"product" => product_id, "quantity" => quantity}, acc
      when is_integer(quantity) and quantity > 0 ->
        case Validators.parse_uuid(product_id) do
          {:ok, _} -> {:cont, [%{product_id: product_id, quantity: quantity} | acc]}
          {:error, :invalid_uuid} -> {:halt, {:error, :invalid_payload}}
        end

      _, _acc ->
        {:halt, {:error, :invalid_payload}}
    end)
    |> case do
      {:error, _} = error -> error
      items when is_list(items) -> {:ok, Enum.reverse(items)}
    end
  end

  defp reject_duplicate_products(items) do
    ids = Enum.map(items, & &1.product_id)

    if length(ids) == length(Enum.uniq(ids)) do
      {:ok, items}
    else
      {:error, :invalid_payload}
    end
  end

  defp fetch_products(store_id, items) do
    ids = Enum.map(items, & &1.product_id)

    products =
      from(p in Product, where: p.store_id == ^store_id and p.id in ^ids)
      |> Repo.all()

    if length(products) == length(Enum.uniq(ids)) do
      {:ok, products}
    else
      {:error, :product_not_found}
    end
  end

  defp get_customer_address(customer_id) do
    case Repo.get(Account, customer_id) |> Repo.preload(:address) do
      %Account{address: [%{id: address_id} | _]} -> {:ok, address_id}
      _ -> {:error, :address_not_found}
    end
  end

  defp compute_total(products, items) do
    Enum.reduce(items, Decimal.new("0"), fn item, acc ->
      %{price: price} = Enum.find(products, &(&1.id == item.product_id))
      Decimal.add(acc, Decimal.mult(price, Decimal.new(item.quantity)))
    end)
  end

  defp insert_order(store, customer_id, address_id, total, payment_method) do
    %Order{
      total: total,
      status_order: "ESPERANDO",
      payment_method: payment_method,
      account_id: customer_id,
      store_id: store.id,
      address_id: address_id
    }
    |> Repo.insert()
  end

  defp insert_order_products(order, items) do
    Enum.reduce_while(items, {:ok, order}, fn item, {:ok, _order} ->
      case Repo.insert(%OrderProduct{
             order_id: order.id,
             product_id: item.product_id,
             quantity: item.quantity
           }) do
        {:ok, _order_product} -> {:cont, {:ok, order}}
        {:error, changeset} -> {:halt, {:error, changeset}}
      end
    end)
  end

  defp decrement_inventory(items) do
    Enum.reduce_while(items, :ok, fn item, :ok ->
      case decrement_stock(item.product_id, item.quantity) do
        :ok -> {:cont, :ok}
        {:error, _reason} = error -> {:halt, error}
      end
    end)
  end

  defp decrement_stock(product_id, quantity) do
    from(i in Inventory,
      where: i.product_id == ^product_id and i.quantity >= ^quantity,
      update: [inc: [quantity: ^(-quantity)]]
    )
    |> Repo.update_all([])
    |> case do
      {1, _} -> :ok
      {0, _} -> {:error, :insufficient_stock}
    end
  end

  defp valid_transition?(status_before, status_after) do
    status_after in Map.get(@valid_transitions, status_before, [])
  end
end
