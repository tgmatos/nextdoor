defmodule NextDoor.Accounts do
  alias NextDoor.Account
  alias NextDoor.Repo

  def new_account(attr \\ %{}) do
    %Account{}
    |> Account.new_account_changeset(attr)
    |> Repo.insert()
    |> case do
      {:ok, account} -> NextDoor.AccountManager.encode_and_sign(account)
      {:error, changeset} -> {:error, changeset}
    end
  end

  def login(%{email: email, plain_password: plain_password}) do
    with account when not is_nil(account) <- Repo.get_by(Account, email: email),
         true <- Argon2.verify_pass(plain_password, account.password) do
      {_, token, _} = NextDoor.AccountManager.encode_and_sign(account)
      {:ok, token}
    else
      _error -> {:error, :unauthorized}
    end
  end

  def get(account_id) do
    case Repo.get(Account, account_id) do
      nil -> {:error, :record_not_found}
      account -> {:ok, account}
    end
  end

  def update(%{account_id: account_id, account: account}) do
    case Repo.get(Account, account_id) do
      nil -> {:error, :record_not_found}
      acc -> acc |> Account.update_changeset(account) |> Repo.update()
    end
  end

  def delete(account_id) do
    case Repo.get(Account, account_id) do
      nil -> {:error, :record_not_found}
      account -> Repo.delete(account)
    end
  end
end
