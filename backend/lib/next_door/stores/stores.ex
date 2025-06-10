defmodule NextDoor.Stores do
  alias NextDoor.{Store, Repo}
  import Ecto.Query

  def create(attr \\ %{}) do
    address_id =
      from(acd in "account_address",
           where: acd.account_id == ^Ecto.UUID.dump!(attr.owner_id),
           select: acd.address_id)
      |> Repo.one
      |> Ecto.UUID.cast!
    
    %Store{}
    |> Store.new_store_changeset(Map.put(attr, :address_id, address_id))
    |> Repo.insert()
    |> case do
      {:ok, store} -> {:ok, store}
      {:error, result_errors} -> result_errors.errors
    end
  end

  # TODO: A way to select stores by type
  # TODO: Add Cachex
  def index do
    case Repo.all(Store) do
      nil -> {:ok, nil}
      stores -> {:ok, stores}
    end
  end

  def show(%{owner_id: owner_id}) do
    case Repo.get_by(Store, %{owner_id: owner_id}) do
      nil -> {:error, :store_not_found}
      store -> {:ok, store}
    end
  end

  def show(id) do
    case Repo.get(Store, id) do
      nil -> {:error, :store_not_found}
      store -> {:ok, store}
    end
  end

  def update(record, owner_id) do
    case Repo.get_by(Store, %{owner_id: owner_id}) do
      nil -> {:error, :store_not_found}
      store ->
        Store.update_store_changeset(store, record)
        |> Repo.update()
    end
  end

  def delete(owner_id) do
    Repo.get_by(Store, owner_id: owner_id)
     |> Repo.delete()
  end
end
