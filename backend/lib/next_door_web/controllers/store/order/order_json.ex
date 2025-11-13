defmodule NextDoorWeb.OrderJson do
  def show(%{order: order}) do
    # %{order_product: products} = orders
    # products
    # |> Enum.map(fn p -> format_product(p) end)
    Map.update(order, :order_product, [], fn products ->
      Enum.map(products, &format_product(&1.product))
    end)
  end

  defp format_product(product) do
    IO.inspect(Map.delete(product, :image))
    %{id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: price,
      image: image,
    } = product

    %{id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: Decimal.to_float(price),
      image: Base.encode64(image || "")
    }
  end
end
