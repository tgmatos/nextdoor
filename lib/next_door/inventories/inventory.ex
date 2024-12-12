defmodule NextDoor.Inventory do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Store, Product}

  @primary_key{:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "inventory" do
	field :name, :string
	belongs_to :store, Store, foreign_key: :store_id
	has_many :product, Product
  end

  def new_inventory_changeset(inventory, params \\ %{}) do
	inventory
	|> cast(params, [:name, :store_id])
	|> validate_required([:name, :store_id])
	|> foreign_key_constraint(:store_id)
  end
end
