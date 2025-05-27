defmodule NextDoor.Accounts do
  alias NextDoor.Account
  alias NextDoor.Repo

  def new_account(attr \\ %{}) do
    %Account{}
    |> Account.new_account_changeset(attr)
    |> Repo.insert()
    |> case do
      {:ok, account} -> NextDoor.AccountManager.encode_and_sign(account)
      {:error, result_errors} -> result_errors.errors |> Enum.map(fn {_, {error, _}} -> error end)
    end
  end

  def login(%{email: email, plain_password: plain_password}) do
    with account when not is_nil(account) <- Repo.get_by(Account, email: email),
         true <- Argon2.verify_pass(plain_password, account.password) do
      {_, token, _} = NextDoor.AccountManager.encode_and_sign(account)
      {:ok, token}
    else
      error when error in [nil, false] -> {:error, :unauthorized}
    end
  end
end
