defmodule NextDoorWeb.ProductJSON do
  def create(%{product: product}), do: %{product: format_product(product)}
  def show(%{products: products}), do: %{products: Enum.map(products, &format_product/1)}

  defp format_product(product) do
    %{
      id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: price,
      image: image,
      inventory: inventory
    } = product

    quantity = if inventory, do: inventory.quantity, else: nil

    %{
      id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: Decimal.to_float(price),
      quantity: quantity,
      image: Base.encode64(image || "")
    }
  end
end
