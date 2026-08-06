defmodule NextDoor.Product do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Store, Inventory}

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:store, :store_id, :active, :__meta__]}
  schema "product" do
    field(:name, :string)
    field(:price, :decimal)
    field(:description, :string)
    field(:image, :binary)
    field(:active, :boolean, default: true)
    belongs_to(:store, Store, foreign_key: :store_id)

    has_one(:inventory, Inventory,
      foreign_key: :product_id,
      on_delete: :delete_all,
      on_replace: :update
    )

    timestamps()
  end

  def new_product_changeset(product, params \\ %{}) do
    product
    |> cast(params, [:name, :description, :price, :image, :store_id])
    |> cast_assoc(:inventory, required: true)
    |> validate_required([:name, :description, :price, :image, :store_id])
    |> foreign_key_constraint(:store_id)
    |> foreign_key_constraint(:product_id)
  end

  # TODO: Validate if the quantity is < 0
  def update_product_changeset(product, params \\ %{}) do
    product
    |> cast(params, [:name, :price, :description, :image])
    |> cast_assoc(:inventory)
    |> foreign_key_constraint(:store_id)
    |> foreign_key_constraint(:product_id)
  end

  def deactivate_changeset(product, params \\ %{}) do
    product
    |> cast(params, [:active])
  end
end
