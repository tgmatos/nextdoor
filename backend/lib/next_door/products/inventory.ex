defmodule NextDoor.Inventory do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.Product

  @primary_key {:product_id, :binary_id, autogenerate: false}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:product_id, :product, :__meta__]}
  schema "inventory" do
    field(:quantity, :integer)
    belongs_to(:product, Product, define_field: false, type: :binary_id)
  end

  def changeset(inventory, params \\ %{}) do
    inventory
    |> cast(params, [:quantity])
    |> validate_required([:quantity])
    |>foreign_key_constraint(:product_id)
  end
end
