defmodule NextDoorWeb.OrderControllerTest do
  use NextDoorWeb.ConnCase

  setup %{conn: conn} do
    flush_cache()

    owner = account_fixture()
    store = store_fixture(owner)
    product = product_fixture(store)
    customer = account_fixture()
    order = order_fixture(customer, store, product)

    conn = auth_conn(conn, owner)

    {:ok,
     conn: conn, owner: owner, store: store, product: product, customer: customer, order: order}
  end

  describe "list orders by store" do
    test "lists the store owner's orders", %{conn: conn, order: order} do
      conn = get(conn, ~p"/api/store/order")

      assert %{"orders" => orders} = json_response(conn, 200)
      assert Enum.any?(orders, &(&1["id"] == order.id))
    end

    test "returns an empty list when the owner has no orders", %{conn: conn} do
      other = account_fixture()
      store_fixture(other)
      conn = auth_conn(conn, other)

      conn = get(conn, ~p"/api/store/order")

      assert json_response(conn, 200)["orders"] == []
    end
  end

  describe "get order by store" do
    test "returns the store owner's order and products", %{
      conn: conn,
      order: order,
      product: product
    } do
      conn = get(conn, ~p"/api/store/order/#{order.id}")

      assert %{"order_product" => order_product} = json_response(conn, 200)
      assert Enum.any?(order_product, &(&1["id"] == product.id))
    end

    test "returns 404 for another owner's order", %{conn: conn, order: order} do
      other = account_fixture()
      store_fixture(other)
      conn = auth_conn(conn, other)

      conn = get(conn, ~p"/api/store/order/#{order.id}")

      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 404 for a missing order", %{conn: conn} do
      conn = get(conn, ~p"/api/store/order/#{Ecto.UUID.generate()}")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end

  describe "customer orders" do
    test "lists orders for the customer", %{conn: conn, customer: customer, order: order} do
      conn = auth_conn(conn, customer)
      conn = get(conn, ~p"/api/account/order")

      assert %{"orders" => orders} = json_response(conn, 200)
      assert Enum.any?(orders, &(&1["id"] == order.id))
    end

    test "returns the customer's order", %{conn: conn, customer: customer, order: order} do
      conn = auth_conn(conn, customer)
      conn = get(conn, ~p"/api/account/order/#{order.id}")

      assert %{"order" => result} = json_response(conn, 200)
      assert result["id"] == order.id
    end

    test "returns 404 when fetching another customer's order", %{conn: conn, order: order} do
      other = account_fixture()
      conn = auth_conn(conn, other)
      conn = get(conn, ~p"/api/account/order/#{order.id}")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end

  describe "update status order" do
    test "performs a valid status transition", %{conn: conn, order: order} do
      conn =
        patch(conn, ~p"/api/store/order/#{order.id}", %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert %{"id" => id, "status_order" => "ACEITO"} = json_response(conn, 200)
      assert id == order.id
    end

    test "returns 422 for an invalid transition", %{conn: conn, order: order} do
      conn =
        patch(conn, ~p"/api/store/order/#{order.id}", %{
          before: "ESPERANDO",
          after: "ROTA"
        })

      assert json_response(conn, 422)["error"] == "Invalid status transition"
    end

    test "returns 422 when the current status does not match", %{conn: conn, order: order} do
      conn =
        patch(conn, ~p"/api/store/order/#{order.id}", %{
          before: "PREPARACAO",
          after: "ROTA"
        })

      assert json_response(conn, 422)["error"] == "Invalid status transition"
    end

    test "returns 404 for a missing order", %{conn: conn} do
      conn =
        patch(conn, ~p"/api/store/order/#{Ecto.UUID.generate()}", %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert json_response(conn, 404)["error"] == "Order not found"
    end

    test "returns 400 for a malformed order id", %{conn: conn} do
      conn =
        patch(conn, ~p"/api/store/order/#{invalid_uuid()}", %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert json_response(conn, 400)["error"] == "Invalid order id"
    end
  end
end
