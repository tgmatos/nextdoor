defmodule NextDoorWeb.AccountController do
  use NextDoorWeb, :controller
  alias NextDoor.Accounts

  action_fallback(NextDoorWeb.FallbackController)

  def register(conn, %{"email" => email, "username" => username, "password" => plain_password}) do
    with {:ok, token, _claims} <-
           Accounts.new_account(%{
             email: email,
             username: username,
             plain_password: plain_password
           }) do
      render(conn, :register_account, %{token: token})
    end
  end

  def login(conn, %{"email" => email, "password" => plain_password}) do
    with {:ok, token} <- Accounts.login(%{email: email, plain_password: plain_password}) do
      render(conn, :login, %{token: token})
    end
  end
end
