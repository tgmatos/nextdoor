defmodule NextDoor.Addresses do
  alias NextDoor.{Address, Account, Repo}
  import Ecto.Query
  
  def new_address(params) do
    acc = Repo.get(Account, params.account_id)
    
    %Address{}
    |> Address.changeset(Map.put(params, :account, acc))
    |> Repo.insert
  end

  def get_address(%{account_id: account_id, address_id: address_id}) do
    {:ok, ac_uuid} = Ecto.UUID.dump(account_id)
    {:ok, ad_uuid} = Ecto.UUID.dump(address_id)
    from(a in Address,
         join: aa in "account_address",
         on: aa.address_id == a.id,
         where: aa.account_id == ^ac_uuid and aa.address_id == ^ad_uuid,
         select: a)
    |> Repo.one
    |> then(&{:ok, &1})
  end
  
  def list_addresses(%{account_id: account_id}) do
    {:ok, uuid} = Ecto.UUID.dump(account_id)
    from(a in Address,
         join: aa in "account_address",
         on: aa.address_id == a.id,
         where: aa.account_id == ^uuid,
         select: a)
    |> Repo.all
    |> then(&{:ok, &1})
  end
  
  def update_address(%{account_id: account_id, address_id: address_id, address: address}) do
    {:ok, ac_uuid} = Ecto.UUID.dump(account_id)
    {:ok, ad_uuid} = Ecto.UUID.dump(address_id)
    from(a in Address,
         join: aa in "account_address",
         on: aa.address_id == a.id,
         where: aa.account_id == ^ac_uuid and aa.address_id == ^ad_uuid,
         select: a)
    |> Repo.one
    |> Address.update_changeset(address)
    |> Repo.update
  end

  
end
