defmodule NextDoorWeb.OrderJSON do
  def create(%{order: order}) do
    order_product =
      case order.order_product do
        %Ecto.Association.NotLoaded{} -> []
        products -> Enum.map(products, &format_product(&1.product))
      end

    %{
      id: order.id,
      total: order.total,
      status_order: order.status_order,
      payment_method: order.payment_method,
      order_product: order_product
    }
  end

  def show(%{order: order}) do
    order_product =
      case order.order_product do
        %Ecto.Association.NotLoaded{} ->
          []

        products ->
          Enum.map(products, fn
            %{product: %Ecto.Association.NotLoaded{}} ->
              nil

            %{product: product, quantity: quantity} ->
              product
              |> format_product()
              |> Map.put(:quantity, quantity)
          end)
      end

    %{
      id: order.id,
      total: order.total,
      inserted_at: order.inserted_at,
      updated_at: order.updated_at,
      payment_method: order.payment_method,
      status_order: order.status_order,
      client: format_client(order.account),
      address: format_address(order.address),
      order_product: Enum.reject(order_product, &is_nil/1)
    }
  end

  defp format_client(%Ecto.Association.NotLoaded{}), do: nil
  defp format_client(nil), do: nil

  defp format_client(account) do
    %{id: id, username: username, email: email} = account
    %{id: id, username: username, email: email}
  end

  defp format_address(%Ecto.Association.NotLoaded{}), do: nil
  defp format_address(nil), do: nil

  defp format_address(address) do
    %{address_number: address_number, street: street, neighborhood: neighborhood, cep: cep} =
      address

    %{
      address_number: address_number,
      street: street,
      neighborhood: neighborhood,
      cep: cep
    }
  end

  defp format_product(product) do
    %{
      id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: price,
      image: image
    } = product

    %{
      id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: Decimal.to_float(price),
      image: Base.encode64(image || "")
    }
  end
end
