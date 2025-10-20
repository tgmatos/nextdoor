defmodule NextDoor.OrderProduct do
  use Ecto.Schema
  # import Ecto.Changeset
  alias NextDoor.{Product, Order}

  @primary_key false
  @foreign_key_type :binary_id

  @derive {Jason.Encoder, except: [:order, :product_id, :order_id, :__meta__]}
  schema "order_product" do
    field(:quantity, :integer)
    belongs_to(:order, Order, primary_key: true)
    belongs_to(:product, Product, primary_key: true)
  end
end
