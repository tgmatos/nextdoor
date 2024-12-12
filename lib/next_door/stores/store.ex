defmodule NextDoor.Store do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.Account

  @primary_key{:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  
  schema "store" do
	field :name, :string
	field :description, :string
	belongs_to :owner, Account, foreign_key: :owner_id
  end

  def new_store_changeset(store, params \\ %{}) do
	store
	|> cast(params, [:name, :description, :owner_id])
	|> validate_required([:name, :description, :owner_id])
	|> foreign_key_constraint(:owner_id)
  end
end
