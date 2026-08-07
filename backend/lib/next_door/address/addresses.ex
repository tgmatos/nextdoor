defmodule NextDoor.Addresses do
  alias NextDoor.{Address, Repo}
  alias NextDoor.Validators
  alias Ecto.Multi
  import Ecto.Query
  import NextDoor.RepoHelper

  def new_address(params) do
    Multi.new()
    |> Multi.insert(:address, %Address{} |> Address.changeset(params))
    |> Multi.run(:link, fn repo, %{address: address} -> link_address(repo, address, params) end)
    |> transact(:address)
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
      |> then(&Multi.update(Multi.new(), :address, &1))
      |> transact(:address)
    else
      nil -> {:error, :record_not_found}
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  defp link_address(repo, address, params) do
    address_id_binary = Ecto.UUID.dump!(address.id)
    account_id_binary = Ecto.UUID.dump!(params.account_id)

    repo.insert_all("account_address", [
      %{account_id: account_id_binary, address_id: address_id_binary}
    ])

    {:ok, address}
  end
end
