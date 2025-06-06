defmodule NextDoorWeb.ProductController do
  use NextDoorWeb, :controller
  alias NextDoor.Products
  @cache :nd_cache

  def create(conn, %{
        "product" => %{
          "name" => name,
          "description" => description,
          "price" => price,
          "stock" => quantity
        }
      }) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, product} <-
           Products.create(owner_id, %{
             name: name,
             description: description,
             price: price,
             quantity: quantity
           }) do
      render(conn, :create, %{product: product})
    end
  end

  def index(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, products} <- Products.index(owner_id) do
      result = %{products: products}
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 60)
      IO.puts("view_cache:owner:#{owner_id}.#{conn.request_path}")
      json(conn, result)
    end
  end
  
  def update(conn, %{"product" => product}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, _} <- Products.update(owner_id, product) do
      conn
      |> put_status(:ok)
      |> send_resp(:ok, "")
    end
  end

  def delete(conn, %{"id" => product_id}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, _} <- Products.delete(product_id, owner_id) do
      conn
      |> put_status(:ok)
      |> send_resp(:ok, "")
    end
    
    Cachex.del(@cache, "view_cache:owner:#{owner_id}./api/store/product")
  end
end
