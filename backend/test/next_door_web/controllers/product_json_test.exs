defmodule NextDoorWeb.ProductJSONTest do
  use ExUnit.Case, async: true

  alias NextDoor.{Product, Inventory}

  @ts ~U[2024-01-01 00:00:00Z]

  defp product do
    %Product{
      id: "product-id",
      name: "Product",
      description: "description",
      price: Decimal.new("20.00"),
      image: "fake image binary",
      inserted_at: @ts,
      updated_at: @ts,
      inventory: %Inventory{quantity: 300}
    }
  end

  defp formatted_product do
    %{
      id: "product-id",
      name: "Product",
      description: "description",
      inserted_at: @ts,
      updated_at: @ts,
      price: 20.0,
      quantity: 300,
      image: Base.encode64("fake image binary")
    }
  end

  test "create formats a product" do
    assert NextDoorWeb.ProductJSON.create(%{product: product()}) == %{
             product: formatted_product()
           }
  end

  test "show maps every product" do
    assert NextDoorWeb.ProductJSON.show(%{products: [product(), product()]}) ==
             %{products: [formatted_product(), formatted_product()]}
  end

  test "show handles an empty list" do
    assert NextDoorWeb.ProductJSON.show(%{products: []}) == %{products: []}
  end

  test "price is converted from decimal to float" do
    result = NextDoorWeb.ProductJSON.create(%{product: product()})
    assert is_float(result.product.price)
    assert result.product.price == 20.0
  end

  test "image is base64 encoded" do
    result = NextDoorWeb.ProductJSON.create(%{product: product()})
    assert result.product.image == Base.encode64("fake image binary")
  end
end
