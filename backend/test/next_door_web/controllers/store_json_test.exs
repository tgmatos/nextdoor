defmodule NextDoorWeb.StoreJSONTest do
  use NextDoorWeb.ConnCase, async: true

  @email "test@gmail.com"
  @username "test"
  @password "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"

  @store "Store Test"
  @descr "Store Test Description"
  @address "1234 Elm Street"
  @phone "11223344556"
  @category "VESTUARIO"

  setup %{conn: conn} do
    {:ok, token, _} =
      NextDoor.Accounts.new_account(%{
        email: @email,
        username: @username,
        plain_password: @password
      })

    conn = put_req_header(conn, "authorization", "Bearer #{token}")
    {:ok, conn: conn}
  end

  test "New Store", %{conn: conn} do
    conn =
      post(conn, ~p"/api/store", %{
        name: @store,
        description: @descr,
        address: @address,
        telephone: @phone,
        category: @category
      })

    assert json_response(conn, 200)
  end

  test "List Stores", %{conn: conn} do
    post(conn, ~p"/api/store", %{
      name: @store,
      description: @descr,
      address: @address,
      telephone: @phone,
      category: @category
    })

    conn = get(conn, ~p"/api/stores")

    assert json_response(conn, 200)
    assert is_list(json_response(conn, 200)["stores"])
  end

  test "Update Store", %{conn: conn} do
    post(conn, ~p"/api/store", %{
      name: @store,
      description: @descr,
      address: @address,
      telephone: @phone,
      category: @category
    })

    put(conn, ~p"/api/store", %{
      store: %{
        name: "updated store",
        description: @descr,
        address: @address,
        telephone: @phone,
        category: @category
      }
    })

    result = case NextDoor.Repo.get_by(NextDoor.Store, name: "updated store") do
      nil -> {:error, :updated_failed}
      store -> {:ok, store}
    end

    assert  {:ok, _} = result
  end
end
