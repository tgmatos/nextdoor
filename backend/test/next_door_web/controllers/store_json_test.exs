defmodule NextDoorWeb.StoreJSONTest do
  use NextDoorWeb.ConnCase, async: true

  test "New Store", %{conn: conn} do
	{:ok, token, _} =
	  NextDoor.Accounts.new_account(
		%{email: "teste@gmail.com",
		  username: "teste",
		  plain_password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"}
	  )
	
	conn =
	  conn
	  |> put_req_header("authorization", "Bearer #{token}")
	  |> post(~p"/api/store", %{name: "Loja Teste", description: "Loja de Teste", address: "Bairro A, Avenida B", telephone: "11223344556", category: "VESTUARIO"})

	assert json_response(conn, 200)
  end

  
  test "List Stores", %{conn: conn} do
	{:ok, token, _} = NextDoor.Accounts.new_account(
		%{email: "teste@gmail.com",
		  username: "teste",
		  plain_password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"})

	conn
	|> put_req_header("authorization", "Bearer #{token}")
	|> post(~p"/api/store", %{name: "Loja Teste", description: "Loja de Teste", address: "Bairro A, Avenida B", telephone: "11223344556", category: "VESTUARIO"})

	conn = conn
	|> get(~p"/api/store")

	assert json_response(conn, 200)
  end



  test "Update Store", %{conn: conn} do	
	{:ok, token, _} = NextDoor.Accounts.new_account(
		%{email: "teste@gmail.com",
		  username: "teste",
		  plain_password: "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"})

	%{assigns: %{store: %{id: id}}} =
	  conn
	  |> put_req_header("authorization", "Bearer #{token}")
	  |> post(~p"/api/store", %{name: "Loja Teste", description: "Loja de Teste", address: "Bairro A, Avenida B", telephone: "11223344556", category: "VESTUARIO"})

	conn
	|> put_req_header("authorization", "Bearer #{token}")
	|> put(~p"/api/store",
		   %{
			 store: %{
			   name: "Updated Loja Teste",
			   description: "Updated Loja de Teste",
			   address: "Updated Bairro A, Avenida B",
			   telephone: "22334455667",
			   category: "ELETRONICOS"
			 }})
	|> response(200)

	
  end
  
end
