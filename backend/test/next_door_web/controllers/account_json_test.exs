defmodule NextDoorWeb.AccountJSONTest do
  use NextDoorWeb.ConnCase, async: true

  test "Register User", %{conn: conn} do	
	conn =
      post(conn, ~p"/api/account/register", %{
        email: "teste@gmail.com",
        username: "teste",
        password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"
      })
	 assert json_response(conn, 200)
  end

  test "Login User", %{conn: conn} do
	NextDoor.Accounts.new_account(
		   %{email: "teste@gmail.com",
			 username: "teste",
			 password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"}
		 )

	conn =
	  post(conn, ~p"/api/account/login", %{
		email: "teste@gmail.com",
		password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"
	  })
	assert json_response(conn, 401)
  end
  
end
