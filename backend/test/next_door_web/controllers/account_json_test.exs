defmodule NextDoorWeb.AccountJSONTest do
  use ExUnit.Case, async: true

  test "register_account returns the token" do
    assert NextDoorWeb.AccountJSON.register_account(%{token: "token-123"}) == %{
             token: "token-123"
           }
  end

  test "login returns the token" do
    assert NextDoorWeb.AccountJSON.login(%{token: "token-123"}) == %{token: "token-123"}
  end

  test "show returns the account id, email and username" do
    account = %{id: "account-id", email: "user@example.com", username: "user"}

    assert NextDoorWeb.AccountJSON.show(%{account: account}) == %{
             account: %{id: "account-id", email: "user@example.com", username: "user"}
           }
  end
end
