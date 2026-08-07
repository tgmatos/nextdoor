defmodule NextDoorWeb.OrderControllerTest do
  use NextDoorWeb.ConnCase

  alias NextDoor.{Inventory, Order, Repo}

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
    test "lists the store owner's orders with client info and dates", %{
      conn: conn,
      order: order,
      customer: customer
    } do
      customer_id = customer.id
      customer_username = customer.username
      customer_email = customer.email

      conn = get(conn, ~p"/api/store/order")

      assert %{"entries" => entries} = json_response(conn, 200)
      assert [listed] = Enum.filter(entries, &(&1["id"] == order.id))
      assert listed["client"]["id"] == customer_id
      assert listed["client"]["username"] == customer_username
      assert listed["client"]["email"] == customer_email
      assert listed["inserted_at"] != nil
      assert listed["updated_at"] != nil
    end

    test "returns an empty list when the owner has no orders", %{conn: conn} do
      other = account_fixture()
      store_fixture(other)
      conn = auth_conn(conn, other)

      conn = get(conn, ~p"/api/store/order")

      assert json_response(conn, 200)["entries"] == []
    end
  end

  describe "get order by store" do
    test "returns the store owner's order with client info, address and quantities", %{
      conn: conn,
      order: order,
      product: product,
      customer: customer
    } do
      customer_id = customer.id
      customer_username = customer.username
      customer_email = customer.email
      conn = get(conn, ~p"/api/store/order/#{order.id}")

      assert %{
               "order_product" => [order_product],
               "client" => %{
                 "id" => ^customer_id,
                 "username" => ^customer_username,
                 "email" => ^customer_email
               },
               "address" => %{
                 "address_number" => "123",
                 "street" => "Main St",
                 "neighborhood" => "Downtown",
                 "cep" => "12345678"
               },
               "status_order" => "ESPERANDO"
             } = json_response(conn, 200)

      assert order_product["id"] == product.id
      assert order_product["quantity"] == 2
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

      assert %{"entries" => entries} = json_response(conn, 200)
      assert Enum.any?(entries, &(&1["id"] == order.id))
    end

    test "returns the customer's order", %{
      conn: conn,
      customer: customer,
      order: order,
      product: product
    } do
      conn = auth_conn(conn, customer)
      conn = get(conn, ~p"/api/account/order/#{order.id}")

      assert %{"order" => result} = json_response(conn, 200)
      assert result["id"] == order.id
      assert result["client"]["id"] == customer.id
      assert result["address"]["address_number"] == "123"
      assert [order_product] = result["order_product"]
      assert order_product["id"] == product.id
      assert order_product["quantity"] == 2
    end

    test "returns 404 when fetching another customer's order", %{conn: conn, order: order} do
      other = account_fixture()
      conn = auth_conn(conn, other)
      conn = get(conn, ~p"/api/account/order/#{order.id}")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end

  describe "create order" do
    test "creates an order, order products and decrements inventory", %{
      conn: conn,
      store: store,
      product: product,
      customer: customer
    } do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: product.id, quantity: 2}],
          payment_method: "PIX"
        })

      assert %{
               "id" => id,
               "total" => "40.00",
               "status_order" => "ESPERANDO",
               "payment_method" => "PIX",
               "order_product" => [order_product]
             } = json_response(conn, 200)

      assert order_product["id"] == product.id

      order = Repo.get!(Order, id)
      assert order.account_id == customer.id
      assert order.store_id == store.id
      assert length(Repo.preload(order, :order_product).order_product) == 1
      assert Repo.get!(Inventory, product.id).quantity == 298
    end

    test "returns 404 when the store does not exist", %{conn: conn, customer: customer} do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{Ecto.UUID.generate()}", %{
          products: [%{product: Ecto.UUID.generate(), quantity: 1}],
          payment_method: "PIX"
        })

      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 422 when a product does not belong to the store", %{
      conn: conn,
      store: store,
      customer: customer
    } do
      other = account_fixture()
      other_store = store_fixture(other)
      other_product = product_fixture(other_store)
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: other_product.id, quantity: 1}],
          payment_method: "PIX"
        })

      assert json_response(conn, 422)["errors"]["detail"] == "one or more products were not found"
    end

    test "returns 422 for insufficient stock", %{
      conn: conn,
      store: store,
      product: product,
      customer: customer
    } do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: product.id, quantity: 9999}],
          payment_method: "PIX"
        })

      assert json_response(conn, 422)["errors"]["detail"] == "insufficient stock"
      assert Repo.get!(Inventory, product.id).quantity == 300
    end

    test "returns 422 when payment_method is missing", %{
      conn: conn,
      store: store,
      product: product,
      customer: customer
    } do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: product.id, quantity: 1}]
        })

      assert json_response(conn, 422)["errors"]["detail"] == "invalid payload"
    end

    test "returns 422 for a malformed payload", %{
      conn: conn,
      store: store,
      product: product,
      customer: customer
    } do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: product.id, quantity: 0}],
          payment_method: "PIX"
        })

      assert json_response(conn, 422)["errors"]["detail"] == "invalid payload"
    end

    test "returns 400 for an invalid store id", %{conn: conn, customer: customer} do
      conn = auth_conn(conn, customer)

      conn =
        post(conn, ~p"/api/store/order/#{invalid_uuid()}", %{
          products: [%{product: Ecto.UUID.generate(), quantity: 1}],
          payment_method: "PIX"
        })

      assert json_response(conn, 400)["errors"]["detail"] == "Bad Request"
    end

    test "returns 401 without authentication", %{store: store} do
      conn = build_conn()

      conn =
        post(conn, ~p"/api/store/order/#{store.id}", %{
          products: [%{product: Ecto.UUID.generate(), quantity: 1}],
          payment_method: "PIX"
        })

      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
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

      assert json_response(conn, 422)["errors"]["detail"] == "Invalid status transition"
    end

    test "returns 422 when the current status does not match", %{conn: conn, order: order} do
      conn =
        patch(conn, ~p"/api/store/order/#{order.id}", %{
          before: "PREPARACAO",
          after: "ROTA"
        })

      assert json_response(conn, 422)["errors"]["detail"] == "Invalid status transition"
    end

    test "returns 404 for a missing order", %{conn: conn} do
      conn =
        patch(conn, ~p"/api/store/order/#{Ecto.UUID.generate()}", %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert json_response(conn, 404)["errors"]["detail"] == "Order not found"
    end

    test "returns 400 for a malformed order id", %{conn: conn} do
      conn =
        patch(conn, ~p"/api/store/order/#{invalid_uuid()}", %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert json_response(conn, 400)["errors"]["detail"] == "Invalid order id"
    end
  end
end
