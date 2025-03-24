defmodule NextDoor.Products do
  alias NextDoor.{Product, Repo, Cache}

  def create(owner_id, attr \\ %{}) do
	case Cache.get(NextDoor.Store, %{owner_id: owner_id}) do
	  {:ok, store} ->
		%Product{}
		|> Product.new_product_changeset(%{attr | store_id: store.id})
		|> Repo.insert()
		|> case do
		  {:ok, product} -> {:ok, product}
		  {:error, result_errors} -> result_errors.errors
		end
	  {:error, :record_not_found} -> {:error, :record_not_found}
	end
  end

  def index(store_id) do
	Cache.get_all_by(Product, %{store_id: store_id})
  end

  def update(product_id, owner_id, product) do
	case Cache.get(NextDoor.Store, %{owner_id: owner_id}) do
	  {:ok, store} ->
		Product.update_product_changeset(%{product | store_id: store.id})
		|> Repo.update()
	  {:error, :record_not_found} -> {:error, :record_not_found}
	end
  end

  def delete(product_id, owner_id) do
	case Cache.get(NextDoor.Store, %{owner_id: owner_id}) do
	  {:ok, store} ->
		Repo.get_by(Product, product_id: product_id, store_id: store.id)
		|> Repo.delete()
	  {:error, :record_not_found} -> {:error, :record_not_found}
	end
  end
end
