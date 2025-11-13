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
    IO.inspect(owner_id)
    with {:ok, order} <- Orders.get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
      
      result = NextDoorWeb.OrderJson.show(%{order: order})
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

  def update_status_order(conn, %{"id" => order_id, "before" => status_before, "after" => status_after}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    case Orders.update_status_order(order_id, owner_id, %{before: status_before, after: status_after}) do
      {:ok, order} -> json(conn, %{id: order.id, total: order.total, status_order: order.status_order, payment_method: order.payment_method})
      {:error, :invalid_transition} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid status transition"})

      {:error, :not_found} ->
        conn
        |> put_status(:not_found)
        |> json(%{error: "Order not found"})
    end
  end

  defp format_product(product) do
    %{id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: price,
      image: image,
      inventory: %{
        quantity: quantity
      }
    } = product

    %{id: id,
      name: name,
      description: description,
      inserted_at: inserted_at,
      updated_at: updated_at,
      price: Decimal.to_float(price),
      quantity: quantity,
      image: Base.encode64(image || "")
    }
  end
end
