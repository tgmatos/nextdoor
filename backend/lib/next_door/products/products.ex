defmodule NextDoor.Products do
  alias NextDoor.{Store, Repo, Cache, Product}
  alias NextDoor.Validators
  import Ecto.Query

  def create(owner_id, attr \\ %{}) do
    case Cache.get_by(NextDoor.Store, %{owner_id: owner_id}) do
      {:ok, store} ->
        %Product{}
        |> Product.new_product_changeset(Map.put(attr, :store_id, store.id))
        |> Repo.insert()
        |> case do
          {:ok, product} -> {:ok, product}
          {:error, changeset} -> {:error, changeset}
        end

      {:error, :record_not_found} ->
        {:error, :record_not_found}
    end
  end

  def list_products(store_id) do
    with {:ok, _} <- Validators.parse_uuid(store_id) do
      result =
        from(p in Product,
          join: s in Store,
          on: s.id == p.store_id,
          where: s.id == ^store_id,
          select: p
        )
        |> Repo.all()
        |> Enum.map(&Repo.preload(&1, :inventory))

      {:ok, result}
    else
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def index(owner_id) do
    result =
      from(p in Product,
        join: s in Store,
        on: p.store_id == s.id,
        where: s.owner_id == ^owner_id,
        select: p
      )
      |> Repo.all()
      |> Enum.map(&Repo.preload(&1, :inventory))

    {:ok, result}
  end

  def update(owner_id, product_id, product) do
    with {:ok, _} <- Validators.parse_uuid(product_id),
         %Product{} = product_record <-
           from(p in Product,
             join: s in Store,
             on: p.store_id == s.id,
             where: s.owner_id == ^owner_id and p.id == ^product_id,
             select: p
           )
           |> Repo.one() do
      product_record
      |> Repo.preload(:inventory)
      |> Product.update_product_changeset(product)
      |> Repo.update()
    else
      nil -> {:error, :record_not_found}
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  def delete(product_id, owner_id) do
    with {:ok, _} <- Validators.parse_uuid(product_id),
         %Product{} = product <-
           from(p in Product,
             join: s in Store,
             on: p.store_id == s.id,
             where: s.owner_id == ^owner_id and p.id == ^product_id,
             select: p
           )
           |> Repo.one() do
      Repo.delete(product)
    else
      nil -> {:error, :record_not_found}
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end
end
