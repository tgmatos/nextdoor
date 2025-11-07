defmodule NextDoor.Order do
  use Ecto.Schema
  import Ecto.Changeset
  alias NextDoor.{Store, Account, Address, OrderProduct}

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:account, :account_id, :store, :store_id, :address, :address_id, :products,:__meta__]}
  schema "orders" do
    field :total, :decimal
    field :status_order, :string
    field :payment_method, :string
    belongs_to(:account, Account, foreign_key: :account_id)
    belongs_to(:store, Store, foreign_key: :store_id)
    belongs_to(:address, Address, foreign_key: :address_id)
    has_many(:order_product, OrderProduct)
    has_many(:products, through: [:order_product, :product])
    timestamps()
  end

  def changeset(orders, params \\ %{}) do
    orders
    |> cast(params, [:total, :status_order, :payment_method, :account, :store, :address, :produc])
    |> validate_required([:total, :status_order, :payment_method, :account, :store, :address])
  end

  def update_changeset(order, params \\ %{}) do
    order
    |> cast(params, [:status_order])
    |> validate_required([:status_order])
  end
end
