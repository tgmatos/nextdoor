defmodule NextDoor.Addresses do
  alias NextDoor.{Address, Account, Repo}
  alias NextDoor.Validators
  import Ecto.Query

  def new_address(params) do
    acc = Repo.get(Account, params.account_id)

    %Address{}
    |> Address.changeset(Map.put(params, :account, acc))
    |> Repo.insert()
  end

  def get_address(%{account_id: account_id, address_id: address_id}) do
    with {:ok, ac_uuid} <- Validators.parse_uuid(account_id),
         {:ok, ad_uuid} <- Validators.parse_uuid(address_id) do
      from(a in Address,
        join: aa in "account_address",
        on: aa.address_id == a.id,
        where: aa.account_id == ^ac_uuid and aa.address_id == ^ad_uuid,
        select: a
      )
      |> Repo.one()
      |> case do
        nil -> {:error, :record_not_found}
        address -> {:ok, address}
      end
    end
  end

  def list_addresses(%{account_id: account_id}) do
    with {:ok, uuid} <- Validators.parse_uuid(account_id) do
      from(a in Address,
        join: aa in "account_address",
        on: aa.address_id == a.id,
        where: aa.account_id == ^uuid,
        select: a
      )
      |> Repo.all()
      |> then(&{:ok, &1})
    end
  end

  def update_address(%{account_id: account_id, address_id: address_id, address: address}) do
    with {:ok, ac_uuid} <- Validators.parse_uuid(account_id),
         {:ok, ad_uuid} <- Validators.parse_uuid(address_id),
         %Address{} = record <-
           from(a in Address,
             join: aa in "account_address",
             on: aa.address_id == a.id,
             where: aa.account_id == ^ac_uuid and aa.address_id == ^ad_uuid,
             select: a
           )
           |> Repo.one() do
      record
      |> Address.update_changeset(address)
      |> Repo.update()
    else
      nil -> {:error, :record_not_found}
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end
end
