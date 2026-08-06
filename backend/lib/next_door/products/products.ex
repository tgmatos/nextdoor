defmodule NextDoor.Products do
  alias NextDoor.{Store, Repo, Cache, Product}
  alias NextDoor.Validators
  alias Ecto.Multi
  import Ecto.Query

  def create(owner_id, attr \\ %{}) do
    case Cache.get_by(NextDoor.Store, %{owner_id: owner_id}) do
      {:ok, %{active: false}} ->
        {:error, :record_not_found}

      {:ok, store} ->
        store_with_id = Map.put(attr, :store_id, store.id)

        Multi.new()
        |> Multi.insert(:product, Product.new_product_changeset(%Product{}, store_with_id))
        |> transact(:product)

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
          where: s.id == ^store_id and p.active and s.active,
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
        where: s.owner_id == ^owner_id and p.active and s.active,
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
             where: s.owner_id == ^owner_id and p.id == ^product_id and p.active,
             select: p
           )
           |> Repo.one() do
      product_record
      |> Repo.preload(:inventory)
      |> then(&Multi.update(Multi.new(), :product, Product.update_product_changeset(&1, product)))
      |> transact(:product)
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
      Multi.new()
      |> Multi.update(:product, Product.deactivate_changeset(product, %{active: false}))
      |> transact(:product)
    else
      nil -> {:error, :record_not_found}
      {:error, :invalid_uuid} -> {:error, :invalid_uuid}
    end
  end

  defp transact(multi, step) do
    case Repo.transaction(multi) do
      {:ok, changes} -> {:ok, Map.fetch!(changes, step)}
      {:error, _step, reason, _changes} -> {:error, reason}
    end
  end
end
