defmodule NextDoor.Account do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Store, Address}

  @primary_key {:id, :binary_id, autogenerate: true}

  schema "account" do
    field(:email, :string)
    field(:username, :string)
    field(:password, :string, redact: true)
    field(:plain_password, :string, virtual: true, redact: true)
    has_one(:store, Store, foreign_key: :owner_id)
    many_to_many(:address, Address, join_through: "account_address", join_keys: [account_id: :id, address_id: :id])
  end

  def new_account_changeset(user, params \\ %{}) do
    user
    |> cast(params, [:email, :plain_password, :username])
    |> validate_required([:email, :plain_password, :username])
    |> register_validate_password()
    |> cast_assoc(:address, required: true)
  end

  def register_validate_password(changeset) do
    changeset
    |> validate_length(:plain_password, min: 6, max: 256)
    |> validate_format(:plain_password, ~r/[a-z]/, message: "at least one lower case character")
    |> validate_format(:plain_password, ~r/[A-Z]/, message: "at least one upper case character")
    |> validate_format(:plain_password, ~r/[!?@#$%^&*_0-9]/,
      message: "at least one digit or punctuation character"
    )
    |> hash_password
  end

  def hash_password(changeset) do
    case Map.get(changeset, :errors) do
      [] ->
        %{changes: %{plain_password: plain_password}} = changeset

        changes =
          changeset
          |> Map.get(:changes)
          |> Map.put(:password, Argon2.hash_pwd_salt(plain_password))

        Map.put(changeset, :changes, changes)

      _ ->
        changeset
    end
  end
end
