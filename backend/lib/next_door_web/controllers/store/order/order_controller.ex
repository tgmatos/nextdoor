defmodule NextDoorWeb.OrderController do
  use NextDoorWeb, :controller
  alias NextDoor.Orders
  @cache :nd_cache

  action_fallback(NextDoorWeb.FallbackController)

  def create_order(conn, %{"id" => store_id} = params) do
    %{"sub" => customer_id} = Guardian.Plug.current_claims(conn)

    with {:ok, order} <-
           Orders.create_order(%{
             store_id: store_id,
             customer_id: customer_id,
             products: params["products"],
             payment_method: params["payment_method"]
           }) do
      json(conn, NextDoorWeb.OrderJSON.create(%{order: order}))
    end
  end

  def list_orders_by_store(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, orders} <- Orders.get_orders_by_store(%{owner_id: owner_id}) do
      result = %{orders: orders}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}

      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value,
        expire: 1000
      )

      json(conn, result)
    end
  end

  def get_order_by_store(conn, %{"id" => order_id}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, order} <- Orders.get_order_by_store(%{owner_id: owner_id, order_id: order_id}) do
      result = NextDoorWeb.OrderJSON.show(%{order: order})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}

      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value,
        expire: 1000
      )

      json(conn, result)
    end
  end

  def get_order_by_customer(conn, %{"id" => order_id}) do
    %{"sub" => customer} = Guardian.Plug.current_claims(conn)

    with {:ok, order} <-
           Orders.get_order_by_customer(%{order_id: order_id, customer_id: customer}) do
      result = %{order: NextDoorWeb.OrderJSON.show(%{order: order})}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}

      Cachex.put(@cache, "view_cache:customer:#{customer}.#{conn.request_path}", cache_value,
        expire: 1000
      )

      json(conn, result)
    end
  end

  def get_orders_by_customer(conn, _params) do
    %{"sub" => customer} = Guardian.Plug.current_claims(conn)

    with {:ok, orders} <- Orders.get_orders_by_customer(%{customer_id: customer}) do
      result = %{orders: Enum.map(orders, &NextDoorWeb.OrderJSON.show(%{order: &1}))}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}

      Cachex.put(@cache, "view_cache:customer:#{customer}.#{conn.request_path}", cache_value,
        expire: 1000
      )

      json(conn, result)
    end
  end

  def update_status_order(conn, %{
        "id" => order_id,
        "before" => status_before,
        "after" => status_after
      }) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    case Orders.update_status_order(order_id, owner_id, %{
           before: status_before,
           after: status_after
         }) do
      {:ok, order} ->
        json(conn, %{
          id: order.id,
          total: order.total,
          status_order: order.status_order,
          payment_method: order.payment_method
        })

      {:error, :invalid_transition} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid status transition"})

      {:error, :not_found} ->
        conn
        |> put_status(:not_found)
        |> json(%{error: "Order not found"})

      {:error, :invalid_uuid} ->
        conn
        |> put_status(:bad_request)
        |> json(%{error: "Invalid order id"})
    end
  end

  def update_status_order(_conn, _params), do: {:error, :missing_params}
end
