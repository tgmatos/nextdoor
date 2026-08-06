defmodule NextDoorWeb.StoreControllerTest do
  use NextDoorWeb.ConnCase

  alias NextDoor.Repo
  alias NextDoor.Store

  setup %{conn: conn} do
    flush_cache()
    {:ok, conn: conn}
  end

  @base64 "data:image/png;base64," <> Base.encode64("fake image binary data")

  describe "create" do
    test "creates a store with valid image data", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      attrs = store_attrs()

      conn = post(conn, ~p"/api/store", Map.put(attrs, :image, @base64))

      assert %{"id" => id} = json_response(conn, 200)
      assert Repo.get(Store, id) != nil
    end

    test "returns 401 without a token", %{conn: conn} do
      attrs = store_attrs()

      conn = post(conn, ~p"/api/store", Map.put(attrs, :image, @base64))

      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 422 when required fields are missing", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)

      conn = post(conn, ~p"/api/store", %{name: "Only name", image: @base64})

      assert %{"errors" => errors} = json_response(conn, 422)
      assert errors["telephone"]
      assert errors["category"]
    end

    test "returns 400 for an invalid base64 image", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      attrs = store_attrs()

      conn = post(conn, ~p"/api/store", Map.put(attrs, :image, invalid_base64()))

      assert json_response(conn, 400)["errors"]["image"] == ["invalid base64 image"]
    end
  end

  describe "index" do
    test "returns an empty list when there are no stores", %{conn: conn} do
      conn = get(conn, ~p"/api/stores")
      assert json_response(conn, 200)["stores"] == []
    end

    test "lists all stores", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)

      conn = get(conn, ~p"/api/stores")

      assert %{"stores" => stores} = json_response(conn, 200)
      assert is_list(stores)
      assert length(stores) == 1
    end
  end

  describe "get by id" do
    test "returns a store by id", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)

      conn = get(conn, ~p"/api/stores/#{store.id}")

      assert %{"id" => id, "name" => name, "category" => category} = json_response(conn, 200)
      assert id == store.id
      assert name == store.name
      assert category == store.category
    end

    test "returns 404 for a missing store", %{conn: conn} do
      conn = get(conn, ~p"/api/stores/#{Ecto.UUID.generate()}")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 400 for a malformed uuid", %{conn: conn} do
      conn = get(conn, ~p"/api/stores/#{invalid_uuid()}")
      assert json_response(conn, 400)["errors"]["detail"] == "Bad Request"
    end
  end

  describe "show (authenticated owner)" do
    test "returns the authenticated owner's store", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/store")

      assert %{"id" => id, "name" => name} = json_response(conn, 200)
      assert id == store.id
      assert name == store.name
    end

    test "returns 404 when the owner has no store", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = get(conn, ~p"/api/store")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end

  describe "update" do
    test "updates the owner's store", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/store", %{
          store: %{
            name: "Updated Store #{System.unique_integer([:positive])}",
            description: "Updated description",
            telephone: "99887766554",
            image: @base64
          }
        })

      assert %{"name" => "Updated Store " <> _, "description" => "Updated description"} =
               json_response(conn, 200)
    end

    test "updates the owner's store without an image", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/store", %{
          store: %{
            name: "Renamed #{System.unique_integer([:positive])}",
            description: store.description,
            telephone: store.telephone
          }
        })

      assert %{"name" => "Renamed " <> _} = json_response(conn, 200)
    end

    test "returns 400 for an invalid base64 image", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/store", %{
          store: %{name: "Nope", description: "d", telephone: "1", image: invalid_base64()}
        })

      assert json_response(conn, 400)["errors"]["image"] == ["invalid base64 image"]
    end

    test "returns 422 when the store param is missing", %{conn: conn} do
      account = account_fixture()
      store_fixture(account)
      conn = auth_conn(conn, account)

      conn = patch(conn, ~p"/api/store", %{})

      assert json_response(conn, 422)["errors"]["detail"] == "missing required parameters"
    end
  end

  describe "delete" do
    test "soft-deletes the owner's store", %{conn: conn} do
      account = account_fixture()
      store = store_fixture(account)
      conn = auth_conn(conn, account)

      conn = delete(conn, ~p"/api/store")

      assert response(conn, 204) == ""
      assert Repo.get(Store, store.id).active == false
    end

    test "returns 404 when the owner has no store", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = delete(conn, ~p"/api/store")
      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end
  end
end
