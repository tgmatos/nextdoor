defmodule NextDoorWeb.OrderJson do
  def show(%{order: order}) do
    order_product =
      case order.order_product do
        %Ecto.Association.NotLoaded{} -> []
        products -> Enum.map(products, &format_product(&1.product))
      end

    Map.put(order, :order_product, order_product)
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
