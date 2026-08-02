defmodule NextDoor.CacheTest do
  use NextDoor.DataCase, async: false

  alias NextDoor.{Repo, Store, Cache, Account}

  setup do
    Cache.flush()
    :ok
  end

  defp account_fixture do
    {:ok, account} =
      Repo.insert(%Account{
        email: "cache_#{System.unique_integer([:positive])}@example.com",
        password: Argon2.hash_pwd_salt("Password1!"),
        username: "user_#{System.unique_integer([:positive])}"
      })

    account
  end

  test "get_by returns nil from cache and falls back to the repo" do
    account = account_fixture()

    assert Cache.get_by(Account, email: account.email) == {:ok, account}
    assert Cache.get_by(Account, email: account.email) == {:ok, account}
  end

  test "get_by returns record_not_found when the record does not exist" do
    assert Cache.get_by(Account, email: "missing@example.com") == {:error, :record_not_found}
  end

  test "get_all returns records and caches them" do
    account_fixture()
    assert {:ok, accounts} = Cache.get_all(Account)
    assert is_list(accounts)
    assert length(accounts) >= 1
  end

  test "flush clears every cached entry" do
    account = account_fixture()
    Cache.get_by(Account, email: account.email)

    assert match?({:ok, _}, Cache.flush())
    assert Cache.get_by(Account, email: account.email) == {:ok, account}
  end

  test "clear_view_cache removes keys whose prefix matches" do
    Cachex.put(:nd_cache, "view_cache:/api/stores", {200, "[]"})
    Cachex.put(:nd_cache, "view_cache:/api/stores/1/product", {200, "[]"})
    Cachex.put(:nd_cache, "some_other_key", "value")

    assert Cache.clear_view_cache("view_cache:/api/stores") == :ok
    assert Cachex.get(:nd_cache, "view_cache:/api/stores") == {:ok, nil}
    assert Cachex.get(:nd_cache, "view_cache:/api/stores/1/product") == {:ok, nil}
    assert Cachex.get(:nd_cache, "some_other_key") == {:ok, "value"}
  end

  test "clear_view_cache preserves non-string keys" do
    Cachex.put(:nd_cache, {Store, %{owner_id: "x"}}, %Store{})
    Cache.clear_view_cache("view_cache:")
    assert {:ok, %Store{}} = Cachex.get(:nd_cache, {Store, %{owner_id: "x"}})
  end

  test "get_by falls back to the repo on a cache miss" do
    account = account_fixture()
    assert Cache.get_by(Account, email: account.email) == {:ok, account}

    Cachex.del(:nd_cache, {Account, [email: account.email]})
    assert Cache.get_by(Account, email: account.email) == {:ok, account}
  end

  test "get_by caches and returns the struct on subsequent calls" do
    account = account_fixture()
    assert Cache.get_by(Account, email: account.email) == {:ok, account}

    {:ok, cached} = Cachex.get(:nd_cache, {Account, [email: account.email]})
    assert cached.id == account.id
  end
end
