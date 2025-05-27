defmodule NextDoorWeb.AccountJSONTest do
  use NextDoorWeb.ConnCase, async: true

  @email "test@gmail.com"
  @username "test"
  @password "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"

  test "Register User", %{conn: conn} do
    conn =
      post(conn, ~p"/api/account/register", %{
        email: @email,
        username: @username,
        password: @password
      })

    assert json_response(conn, 200)
  end

  test "Login User", %{conn: conn} do
    NextDoor.Accounts.new_account(%{
      email: @email,
      username: @username,
      plain_password: @password
    })

    conn =
      post(conn, ~p"/api/account/login", %{
        email: @email,
        password: @password
      })

    assert json_response(conn, 200)
  end
end
