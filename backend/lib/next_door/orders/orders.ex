defmodule NextDoor.Orders do
  alias NextDoor.{Order, Store, Repo}
  import Ecto.Query

  @valid_transitions %{
     "ESPERANDO" => ["ACEITO", "RECUSADO"],
     "ACEITO" => ["PREPARACAO", "CANCELADO"],
     "PREPARACAO" => ["ROTA", "CANCELADO"],
     "ROTA" => ["CONCLUIDO", "CANCELADO"],
     "CONCLUIDO" => [],
     "CANCELADO" => []
   }

  def create(attr) when not attr == %{} do
    %Order{}
  end

  def get_orders_by_store(%{owner_id: owner_id}) do
    result = from(s in Store,
                  join: o in Order,
                  on: s.id == o.store_id,
                  where: s.owner_id == ^owner_id,
                  select:
                    %{id: o.id,
                      total: o.total,
                      payment_method: o.payment_method,
                      status: o.status_order})
    |> Repo.all

    {:ok, result}
  end

  def get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
    result = from(o in Order,
		  join: s in Store,
		  on: s.id == o.store_id,
		  where: s.owner_id == ^owner_id and o.id == ^order_id,
		  select: o)
    |> Repo.one
    |> Repo.preload([order_product: [:product]])

    {:ok, result}
  end

  def get_order_by_customer(%{order_id: id, customer_id: customer_id}) do
    result = from(o in Order,
                  where:
                    o.id == ^id
                  and o.account_id == ^customer_id,
                  select: o)
    |> Repo.one()
    |> Repo.preload([:address, :products])

    {:ok, result}
  end

  def get_orders_by_customer(%{customer_id: id}) do
    result = Repo.all_by(Order, account_id: id) |> Repo.preload([:address, :products])
    {:ok, result}
  end

  def update_status_order(order_id, owner_id, %{before: status_before, after: status_after}) do
    query = from(s in Store,
                 join: o in Order,
                 on: s.id == o.store_id,
                 where: s.owner_id == ^owner_id and o.id == ^order_id,
                 select: o)

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
  end

  defp valid_transition?(status_before, status_after) do
    status_after in Map.get(@valid_transitions, status_before, [])
  end
end
