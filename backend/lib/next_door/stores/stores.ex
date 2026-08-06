defmodule NextDoor.Stores do
  alias NextDoor.{Store, Repo, Cache}
  alias NextDoor.Validators
  alias Ecto.Multi
  import Ecto.Query

  def create(attr \\ %{}) do
    Multi.new()
    |> Multi.insert(:store, Store.new_store_changeset(%Store{}, attr))
    |> transact(:store)
  end

  def index do
    case Repo.all(from(s in Store, where: s.active)) do
      nil -> {:ok, nil}
      stores -> {:ok, stores}
    end
  end

  def get_by_id(id) do
    with {:ok, _} <- Validators.parse_uuid(id) do
      case Repo.get_by(Store, id: id, active: true) do
        nil -> {:error, :store_not_found}
        store -> {:ok, store}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def show(%{owner_id: owner_id}) do
    case Repo.get_by(Store, %{owner_id: owner_id, active: true}) do
      nil -> {:error, :store_not_found}
      store -> {:ok, store}
    end
  end

  def show(id) do
    with {:ok, _} <- Validators.parse_uuid(id) do
      case Repo.get_by(Store, id: id, active: true) do
        nil -> {:error, :store_not_found}
        store -> {:ok, store}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def update(record, owner_id) do
    case Repo.get_by(Store, %{owner_id: owner_id}) do
      nil ->
        {:error, :store_not_found}

      store ->
        with {:ok, store} <-
               Multi.new()
               |> Multi.update(:store, Store.update_store_changeset(store, record))
               |> transact(:store) do
          Cache.delete({Store, %{owner_id: owner_id}})
          {:ok, store}
        end
    end
  end

  def delete(owner_id) do
    case Repo.get_by(Store, owner_id: owner_id) do
      nil ->
        {:error, :store_not_found}

      store ->
        with {:ok, store} <-
               Multi.new()
               |> Multi.update(:store, Store.deactivate_changeset(store, %{active: false}))
               |> transact(:store) do
          Cache.delete({Store, %{owner_id: owner_id}})
          {:ok, store}
        end
    end
  end

  defp transact(multi, step) do
    case Repo.transaction(multi) do
      {:ok, changes} -> {:ok, Map.fetch!(changes, step)}
      {:error, _step, reason, _changes} -> {:error, reason}
    end
  end
end
