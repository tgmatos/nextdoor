defmodule NextDoor.Orders do
  alias NextDoor.{Order, Store, Repo}
  import Ecto.Query

  def create(attr) when not attr == %{} do
    %Order{}
  end

  def update_status() do
  end

  def get_orders_by_store(%{owner_id: owner_id}) do
    result = from(s in Store,
                  join: o in Order,
                  on: s.id == o.store_id,
                  where: s.owner_id == ^owner_id,
                  select:
                    %{id: o.id,
                      total: o.total,
                      payment_method: o.payment_method,
                      status: o.status_order})
    |> Repo.all

    {:ok, result}
  end

  def get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
    result = from(s in Store,
                  join: o in Order,
                  on: s.id == o.store_id,
                  where: s.owner_id == ^owner_id and o.id == ^order_id,
                  select: o)
    |> Repo.all
    |> Repo.preload([order_product: [:product]])

    {:ok, result}
  end

  def get_order_by_customer(%{order_id: id, customer_id: customer_id}) do
    result = from(o in Order,
                  where:
                    o.id == ^id
                  and o.account_id == ^customer_id,
                  select: o)
    |> Repo.one()
    |> Repo.preload([:address, :products])

    {:ok, result}
  end

  def get_orders_by_customer(%{customer_id: id}) do
    result = Repo.all_by(Order, account_id: id) |> Repo.preload([:address, :products])
    {:ok, result}
  end
end
