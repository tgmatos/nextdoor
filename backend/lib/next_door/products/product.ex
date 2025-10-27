defmodule NextDoor.Product do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Store, Inventory}

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:store, :store_id, :__meta__]}
  schema "product" do
    field(:name, :string)
    field(:price, :decimal)
    field(:description, :string)
    belongs_to(:store, Store, foreign_key: :store_id)
    has_one(:inventory, Inventory, foreign_key: :product_id, on_delete: :delete_all, on_replace: :update)
    timestamps()
  end

  def new_product_changeset(product, params \\ %{}) do
    product
    |> cast(params, [:name, :description, :price, :store_id])
    |> cast_assoc(:inventory, required: true)
    |> validate_required([:name, :description, :price, :store_id])
    |> foreign_key_constraint(:store_id)
    |> foreign_key_constraint(:product_id)
  end

  # TODO: Validate if the quantity is < 0
  def update_product_changeset(product, params \\ %{}) do
    product
    |> cast(params, [:name, :price, :description])
    |> cast_assoc(:inventory)
    |> foreign_key_constraint(:store_id)
    |> foreign_key_constraint(:product_id)
  end
end
