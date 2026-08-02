defmodule NextDoorWeb.AccountJSON do
  def register_account(%{token: token}), do: %{token: token}
  def login(%{token: token}), do: %{token: token}

  def show(%{account: account}) do
    %{id: id, email: email, username: username} = account
    %{account: %{id: id, email: email, username: username}}
  end
end
