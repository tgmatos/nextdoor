defmodule NextDoorWeb.PoductJSONTest do
  use NextDoorWeb.ConnCase, async: true

  @email "test@gmail.com"
  @username "test"
  @password "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"

  @store "Store Test"
  @descr "Store Test Description"
  @address "1234 Elm Street"
  @phone "11223344556"
  @category "VESTUARIO"

  @product "Product Test"
  @pdescr "Product Test Description"
  @price 20.0
  @stock 300

  setup %{conn: conn} do
    {:ok, token, _} =
      NextDoor.Accounts.new_account(%{
        email: @email,
        username: @username,
        plain_password: @password
      })

    conn = put_req_header(conn, "authorization", "Bearer #{token}")

    post(conn, ~p"/api/store", %{
      name: @store,
      description: @descr,
      address: @address,
      telephone: @phone,
      category: @category
    })
    |> response(200)

    {:ok, conn: conn}
  end

  test "Add Product", %{conn: conn} do
    conn =
      post(conn, ~p"/api/store/product/", %{
        product: %{
          name: @product,
          description: @pdescr,
          price: @price,
          stock: @stock
        }
      })

    assert json_response(conn, 200)
  end
end
