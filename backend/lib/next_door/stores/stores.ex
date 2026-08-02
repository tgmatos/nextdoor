defmodule NextDoor.Stores do
  alias NextDoor.{Store, Repo}
  alias NextDoor.Validators

  def create(attr \\ %{}) do
    %Store{}
    |> Store.new_store_changeset(attr)
    |> Repo.insert()
    |> case do
      {:ok, store} -> {:ok, store}
      {:error, changeset} -> {:error, changeset}
    end
  end

  def index do
    case Repo.all(Store) do
      nil -> {:ok, nil}
      stores -> {:ok, stores}
    end
  end

  def get_by_id(id) do
    with {:ok, _} <- Validators.parse_uuid(id) do
      case Repo.get(Store, id) do
        nil -> {:error, :store_not_found}
        store -> {:ok, store}
      end
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def show(%{owner_id: owner_id}) do
    case Repo.get_by(Store, %{owner_id: owner_id}) do
      nil -> {:error, :store_not_found}
      store -> {:ok, store}
    end
  end

  def show(id) do
    with {:ok, _} <- Validators.parse_uuid(id) do
      case Repo.get(Store, id) do
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
        Store.update_store_changeset(store, record)
        |> Repo.update()
    end
  end

  def delete(owner_id) do
    case Repo.get_by(Store, owner_id: owner_id) do
      nil -> {:error, :store_not_found}
      store -> Repo.delete(store)
    end
  end
end
