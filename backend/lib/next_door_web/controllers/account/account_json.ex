defmodule NextDoorWeb.AccountJSON do
  def register_account(%{token: token}), do: %{token: token}

  def login(%{token: token}), do: %{token: token}
end
