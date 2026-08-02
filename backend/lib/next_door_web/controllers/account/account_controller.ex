defmodule NextDoorWeb.AccountController do
  use NextDoorWeb, :controller
  alias NextDoor.Accounts

  action_fallback(NextDoorWeb.FallbackController)

  def register(conn, params) do
    account_params = %{
      email: params["email"],
      username: params["username"],
      plain_password: params["password"],
      address: normalize_address(params["address"])
    }

    with {:ok, token, _claims} <- Accounts.new_account(account_params) do
      render(conn, :register_account, %{token: token})
    end
  end

  def login(conn, params) do
    with {:ok, token} <-
           Accounts.login(%{email: params["email"], plain_password: params["password"]}) do
      render(conn, :login, %{token: token})
    end
  end

  def show(conn, _params) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, account} <- Accounts.get(account_id) do
      render(conn, :show, %{account: account})
    end
  end

  def update(conn, %{"account" => account}) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, acc} <- Accounts.update(%{account_id: account_id, account: account}) do
      %{email: email, username: username} = acc
      json(conn, %{email: email, username: username})
    end
  end

  def update(_conn, _params), do: {:error, :missing_params}

  def delete(conn, _params) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, _} <- Accounts.delete(account_id) do
      conn
      |> send_resp(:no_content, "")
    end
  end

  def logout(conn, _params) do
    conn
    |> send_resp(:no_content, "")
  end

  defp normalize_address(nil), do: nil

  defp normalize_address(%{
         "number" => number,
         "street" => street,
         "neighborhood" => neighborhood,
         "cep" => cep
       }) do
    [%{address_number: number, street: street, neighborhood: neighborhood, cep: cep}]
  end

  defp normalize_address(_), do: nil
end
