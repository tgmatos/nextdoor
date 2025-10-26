defmodule NextDoor.Products do
  alias NextDoor.{Store, Repo, Cache, Product, Inventory}
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

  def list_products(store_id) do
    result = from(p in Product,
                  join: s in Store,
                  where: s.id == ^store_id,
                  select: p)
    |> Repo.all
    |>Enum.map(fn products ->
      products |> Repo.preload(:inventory)
    end)

    {:ok, result}
  end
  
  def index(owner_id) do
    result = from(p in Product,
         join: s in Store,
         on: p.store_id == s.id,
         where: s.owner_id == ^owner_id,
         select: p)
    |> Repo.all
    |> Enum.map(fn products ->
      products |> Repo.preload(:inventory)
    end)

    {:ok, result}
  end

  def update(owner_id, product_id, product) do
    from(p in Product,
                  join: s in Store,
                  on: p.store_id == s.id,
                  where: s.owner_id == ^owner_id and p.id == ^product_id,
                  select: p)
    |> Repo.one()
    |> Repo.preload(:inventory)
    |> Product.update_product_changeset(product)
    |> Repo.update()
  end

  def delete(product_id, owner_id) do
    from(p in Product,
         join: s in Store,
         on: p.store_id == s.id,
         where: s.owner_id == ^owner_id and p.id == ^product_id)
    |> Repo.one
    |> Repo.delete
  end
end
