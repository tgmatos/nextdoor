defmodule NextDoor.Stores do
  alias NextDoor.{Store, Repo, Cache}

  def create(attr \\ %{}) do
	%Store{}
	|> Store.new_store_changeset(attr)
	|> Repo.insert
	|> case do
	  {:ok, store} -> {:ok, store}
	  {:error, result_errors} -> result_errors.errors
	end
  end

  # TODO: A way to select stores by type
  # TODO: Add Cachex
  def index do
	Cache.get_all(Store)
  end

  def show(%{owner_id: owner_id}) do
	case Cache.get_by(Store, %{owner_id: owner_id}) do
	  {:ok, record} -> {:ok, record}
	  {:error, :record_not_found} -> {:error, :store_not_found}
	end
  end

  def show(id) do
	case Cache.get_by_id(Store, id) do
	  {:ok, record} -> {:ok, record}
	  {:error, :record_not_found} -> {:error, :store_not_found}
	end
  end
    
  def update(record, owner_id) do
	case Cache.get_by(Store, %{owner_id: owner_id}) do
	  {:ok, store} ->
		Store.update_store_changeset(store, record)
		|> Repo.update()
	  
	  {:error, :record_not_found} -> {:error, :record_not_found}
	end
  end

  def delete(owner_id) do
	Repo.get_by(Store, owner_id: owner_id)
	|> Repo.delete()
  end
end
