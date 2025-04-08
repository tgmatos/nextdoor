defmodule NextDoor.Store do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.Account

  @primary_key{:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:owner, :owner_id, :__meta__]}
  schema "store" do
	field :name, :string
	field :description, :string
	field :address, :string
	field :telephone, :string
	field :category, :string
	timestamps()
	belongs_to :owner, Account, foreign_key: :owner_id
  end

  def new_store_changeset(store, params \\ %{}) do
	store
	|> cast(params, [:name, :description, :address, :telephone, :category, :owner_id])
	|> validate_required([:name, :description, :address, :telephone, :category, :owner_id])
	|> foreign_key_constraint(:owner_id)
  end

  def update_store_changeset(store, params \\ %{}) do
	store
	|> cast(params, [:name, :description, :address, :telephone, :category])
	|> validate_required([:owner_id])
	|> foreign_key_constraint(:owner_id)
  end
end

