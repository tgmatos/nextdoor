defmodule NextDoor.Product do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.Store

  @primary_key{:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:store, :store_id, :__meta__]}
  schema "product" do
    field :name, :string
    field :quantity, :integer
    field :price, :decimal
    field :description, :string
    belongs_to :store, Store, foreign_key: :store_id
    timestamps()
  end

  def new_product_changeset(product, params \\ %{}) do
	product
	|> cast(params, [:name, :quantity, :description, :price, :store_id])
	|> validate_required([:name, :quantity, :description, :price, :store_id])
	|> foreign_key_constraint(:store_id)
  end

  # TODO: Validate if the quantity is < 0
  def update_product_changeset(product, params \\ %{}) do
	product
	|> cast(params, [:name, :quantity, :description])
	|> foreign_key_constraint(:store_id)
  end
end
