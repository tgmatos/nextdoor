defmodule NextDoorWeb.AccountController do
  use NextDoorWeb, :controller
  alias NextDoor.Accounts
  
  def register(conn, %{"email" => email, "username" => username, "password" => plain_password}) do
	with {:ok, account} <- Accounts.new_account(%{email: email, username: username, plain_password: plain_password}) do
	  render(conn, :register_account, %{account: account})
	end
  end
end
