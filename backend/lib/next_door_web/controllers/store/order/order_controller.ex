defmodule NextDoorWeb.OrderController do
  use NextDoorWeb, :controller
  alias NextDoor.Orders
  @cache :nd_cache

  def list_orders_by_store(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, orders} <- Orders.get_orders_by_store(%{owner_id: owner_id}) do
      result = %{orders: orders}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def get_order_by_store(conn, %{"id" => order_id}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, order} <- Orders.get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
      result = %{order: order}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def get_order_by_customer(conn, %{"id" => order_id}) do
    %{"sub" => customer} = Guardian.Plug.current_claims(conn)
    with {:ok, order} <- Orders.get_order_by_customer(%{order_id: order_id, customer_id: customer}) do
      result = %{order: order}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:customer:#{customer}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def get_orders_by_customer(conn, _params) do
    %{"sub" => customer} = Guardian.Plug.current_claims(conn)
    with {:ok, orders} <- Orders.get_orders_by_customer(%{customer_id: customer}) do
      result = %{orders: orders}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:customer:#{customer}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end
end
