defmodule NextDoorWeb.ProductControllerTest do
  use NextDoorWeb.ConnCase

  alias NextDoor.Repo
  alias NextDoor.Product

  setup %{conn: conn} do
    flush_cache()
    {:ok, conn: conn}
  end

  @base64 "data:image/png;base64," <> Base.encode64("fake image binary data")

  defp product_params(attrs \\ %{}) do
    %{
      product:
        Map.merge(
          %{
            name: "Product #{System.unique_integer([:positive])}",
            description: "description",
            price: "20.00",
            quantity: 300,
            image: @base64
          },
          attrs
        )
    }
  end

  describe "create" do
    test "creates a product with quantity and image data", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn = post(conn, ~p"/api/store/product", product_params())

      assert %{"product" => product} = json_response(conn, 200)
      assert product["quantity"] == 300
      assert product["price"] == 20.0
    end

    test "returns 401 without a token", %{conn: conn} do
      conn = post(conn, ~p"/api/store/product", product_params())
      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 404 when the owner has no store", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = post(conn, ~p"/api/store/product", product_params())
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 422 when required fields are missing", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn = post(conn, ~p"/api/store/product", %{product: %{image: @base64}})

      assert %{"errors" => errors} = json_response(conn, 422)
      assert errors["name"]
      assert errors["price"]
    end

    test "returns 400 for an invalid base64 image", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn = post(conn, ~p"/api/store/product", product_params(%{image: invalid_base64()}))

      assert json_response(conn, 400)["errors"]["image"] == ["invalid base64 image"]
    end
  end

  describe "index (authenticated owner)" do
    test "lists the owner's products", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      product = product_fixture(store)
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/store/product")

      assert %{"products" => products} = json_response(conn, 200)
      assert Enum.any?(products, &(&1["id"] == product.id))
    end

    test "returns an empty list when the owner has no products", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/store/product")

      assert json_response(conn, 200)["products"] == []
    end
  end

  describe "list (public by store)" do
    test "lists products for a store", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      product = product_fixture(store)

      conn = get(conn, ~p"/api/stores/#{store.id}/product")

      assert %{"products" => products} = json_response(conn, 200)
      assert Enum.any?(products, &(&1["id"] == product.id))
    end

    test "returns an empty list when the store has no products", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)

      conn = get(conn, ~p"/api/stores/#{store.id}/product")

      assert json_response(conn, 200)["products"] == []
    end

    test "returns 400 for a malformed store id", %{conn: conn} do
      conn = get(conn, ~p"/api/stores/#{invalid_uuid()}/product")
      assert json_response(conn, 400)["errors"]["detail"] == "Bad Request"
    end
  end

  describe "update" do
    test "updates product details, quantity and image", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      product = product_fixture(store)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/store/product/#{product.id}", %{
          product: %{name: "Updated", price: "15.00", quantity: 5, image: @base64}
        })

      assert %{"product" => updated} = json_response(conn, 200)
      assert updated["name"] == "Updated"
      assert updated["price"] == 15.0
      assert updated["quantity"] == 5
    end

    test "returns 404 when updating a product from another owner", %{conn: conn} do
      owner = account_fixture()
      store = store_fixture(owner)
      product = product_fixture(store)

      other = account_fixture()
      conn = auth_conn(conn, other)

      conn = patch(conn, ~p"/api/store/product/#{product.id}", %{product: %{name: "Nope"}})
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 400 for an invalid base64 image", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      product = product_fixture(store)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/store/product/#{product.id}", %{
          product: %{name: "Updated", image: invalid_base64()}
        })

      assert json_response(conn, 400)["errors"]["image"] == ["invalid base64 image"]
    end
  end

  describe "delete" do
    test "soft-deletes the owner's product", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      product = product_fixture(store)
      conn = auth_conn(conn, account)

      conn = delete(conn, ~p"/api/store/product/#{product.id}")

      assert response(conn, 204) == ""
      assert Repo.get(Product, product.id).active == false
    end

    test "returns 404 when deleting another owner's product", %{conn: conn} do
      owner = account_fixture()
      store = store_fixture(owner)
      product = product_fixture(store)

      other = account_fixture()
      conn = auth_conn(conn, other)

      conn = delete(conn, ~p"/api/store/product/#{product.id}")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end
end
