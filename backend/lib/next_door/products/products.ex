defmodule NextDoor.Products do
  alias NextDoor.{Product, Store, Repo, Cache}
  import Ecto.Query

  def create(owner_id, attr \\ %{}) do
    case Cache.get_by(NextDoor.Store, %{owner_id: owner_id}) do
      {:ok, store} ->
        %Product{}
        |> Product.new_product_changeset(Map.put(attr, :store_id, store.id))
        |> Repo.insert()
        |> case do
          {:ok, product} -> {:ok, product}
          {:error, result_errors} -> result_errors.errors
        end

      {:error, :record_not_found} ->
        {:error, :record_not_found}
    end
  end

  def index(owner_id) do
    from(p in Product,
         join: s in Store,
         on: p.store_id == s.id,
         where: s.owner_id == ^owner_id,
         select: p)
    |> Repo.all
  end

  def update(owner_id, product) do
    case Repo.get_by(NextDoor.Store, %{owner_id: owner_id}) do
      nil -> {:error, :store_not_found}
      store ->
        Product.update_product_changeset(%{product | store_id: store.id})
        |> Repo.update()
    end
  end

  def delete(product_id, owner_id) do
    case Repo.get_by(NextDoor.Store, %{owner_id: owner_id}) do
      nil -> {:error, :store_not_found}
      {:ok, store} ->
        Repo.get_by(Product, product_id: product_id, store_id: store.id)
        |> Repo.delete()
    end
  end
end
