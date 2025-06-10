defmodule NextDoor.Store do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Account, Address}

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:owner, :owner_id, :__meta__]}
  schema "store" do
    field(:name, :string)
    field(:description, :string)
    field(:telephone, :string)
    field(:category, :string)
    timestamps()
    belongs_to(:address, Address, references: :id)
    belongs_to(:owner, Account, foreign_key: :owner_id)
  end

  def new_store_changeset(store, params \\ %{}) do
    store
    |> cast(params, [:name, :description, :telephone, :category, :owner_id, :address_id])
    |> validate_required([:name, :description, :telephone, :category, :owner_id, :address_id])
    |> foreign_key_constraint(:owner_id)
    |> foreign_key_constraint(:address_id)
  end

  def update_store_changeset(store, params \\ %{}) do
    store
    |> cast(params, [:name, :description, :address, :telephone, :category])
    |> validate_required([:owner_id])
    |> foreign_key_constraint(:owner_id)
  end
end
