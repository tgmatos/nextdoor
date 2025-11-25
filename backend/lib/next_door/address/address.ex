defmodule NextDoor.Address do
  alias NextDoor.{Account}
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:account, :__meta__]}
  schema "address" do
    field(:address_number, :string)
    field(:street, :string)
    field(:neighborhood, :string)
    field(:cep, :string)
    many_to_many(:account, Account, join_through: "account_address")
  end

  # def changeset(address, params \\ %{}) do
  #   address
  #   |> cast(params, [:address_number, :street, :neighborhood, :cep])
  #   |> put_assoc(:account, [params.account])
  #   |> validate_required([:address_number, :street, :neighborhood, :cep, :account])
  # end

  def changeset(address, params \\ %{}) do
  address
  |> cast(params, [:address_number, :street, :neighborhood, :cep])
  |> validate_required([:address_number, :street, :neighborhood, :cep])
  end


  
  def update_changeset(address, params \\ %{}) do
    address
    |> cast(params, [:address_number, :street, :neighborhood, :cep])
  end
end
