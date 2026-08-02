defmodule NextDoorWeb.AddressControllerTest do
  use NextDoorWeb.ConnCase

  alias NextDoor.Repo

  setup %{conn: conn} do
    flush_cache()
    {:ok, conn: conn}
  end

  defp addresses_of(account), do: account |> Repo.preload(:address) |> Map.get(:address)

  describe "list addresses" do
    test "returns the authenticated account addresses", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      [address] = addresses_of(account)

      conn = get(conn, ~p"/api/account/address")

      assert [%{"id" => id, "address_number" => "123"}] = json_response(conn, 200)
      assert id == address.id
    end

    test "returns 401 without a token", %{conn: conn} do
      conn = get(conn, ~p"/api/account/address")
      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end
  end

  describe "get address" do
    test "returns an address owned by the account", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      [address] = addresses_of(account)

      conn = get(conn, ~p"/api/account/address/#{address.id}")

      assert json_response(conn, 200) == %{
               "id" => address.id,
               "address_number" => address.address_number,
               "street" => address.street,
               "neighborhood" => address.neighborhood,
               "cep" => address.cep
             }
    end

    test "returns 404 for an address not owned by the account", %{conn: conn} do
      account = account_fixture()
      other = account_fixture()
      [other_address] = addresses_of(other)
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/account/address/#{other_address.id}")

      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 404 for a missing address id", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/account/address/#{Ecto.UUID.generate()}")

      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 400 for a malformed uuid", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)

      conn = get(conn, ~p"/api/account/address/#{invalid_uuid()}")

      assert json_response(conn, 400)["errors"]["detail"] == "Bad Request"
    end
  end

  describe "update address" do
    test "updates an address owned by the account", %{conn: conn} do
      account = account_fixture()
      [address] = addresses_of(account)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/account/address/#{address.id}", %{
          address: %{street: "Updated Street", cep: "99999999"}
        })

      assert %{"id" => id, "street" => "Updated Street", "cep" => "99999999"} =
               json_response(conn, 200)

      assert id == address.id
    end

    test "returns 404 when updating another account's address", %{conn: conn} do
      account = account_fixture()
      other = account_fixture()
      [other_address] = addresses_of(other)
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/account/address/#{other_address.id}", %{
          address: %{street: "Nope"}
        })

      assert json_response(conn, 404)["errors"]["detail"] == "Not Found"
    end

    test "returns 400 for a malformed uuid", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/account/address/#{invalid_uuid()}", %{
          address: %{street: "Nope"}
        })

      assert json_response(conn, 400)["errors"]["detail"] == "Bad Request"
    end
  end
end
