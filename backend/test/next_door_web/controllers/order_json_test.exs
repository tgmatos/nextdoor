defmodule NextDoorWeb.OrderJsonTest do
  use ExUnit.Case, async: true

  alias NextDoor.{Order, OrderProduct, Product}

  @ts ~U[2024-01-01 00:00:00Z]

  defp product do
    %Product{
      id: "product-id",
      name: "Product",
      description: "description",
      price: Decimal.new("10.00"),
      image: "fake image binary",
      inserted_at: @ts,
      updated_at: @ts
    }
  end

  test "show formats the order products" do
    order = %Order{
      id: "order-id",
      total: Decimal.new("10.00"),
      status_order: "ESPERANDO",
      payment_method: "PIX",
      order_product: [%OrderProduct{product: product()}]
    }

    result = NextDoorWeb.OrderJson.show(%{order: order})

    assert result.order_product == [
             %{
               id: "product-id",
               name: "Product",
               description: "description",
               inserted_at: @ts,
               updated_at: @ts,
               price: 10.0,
               image: Base.encode64("fake image binary")
             }
           ]
  end

  test "show returns an empty product list when there are no order products" do
    order = %Order{
      id: "order-id",
      total: Decimal.new("10.00"),
      status_order: "ESPERANDO",
      payment_method: "PIX",
      order_product: []
    }

    result = NextDoorWeb.OrderJson.show(%{order: order})
    assert result.order_product == []
  end
end
