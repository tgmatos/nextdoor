defmodule NextDoor.Product do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.Inventory

  @primary_key{:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "product" do
	field :name, :string
	field :quantity, :integer
	field :description, :string
	belongs_to :inventory, Inventory, foreign_key: :inventory_id
  end

  def new_product_changeset(product, params \\ %{}) do
	product
	|> cast(params, [:name, :quantity, :description])
	|> validate_required([:name, :quantity, :description, :inventory_id])
	|> foreign_key_constraint(:inventory_id)
  end
end
