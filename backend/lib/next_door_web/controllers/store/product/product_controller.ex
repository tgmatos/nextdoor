defmodule NextDoorWeb.ProductController do
  use NextDoorWeb, :controller
  alias NextDoor.Products
  @cache :nd_cache

  def create(conn, %{
        "product" => %{
          "name" => name,
          "description" => description,
          "price" => price,
          "quantity" => quantity
        }
      }) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, product} <-
           Products.create(owner_id, %{
             name: name,
             description: description,
             price: price,
             inventory: %{quantity: quantity}
           }) do
      render(conn, :create, %{product: product})
    end
  end

  def list(conn, %{"id" => store_id}) do
    with {:ok, products} <- Products.list_products(store_id) do
      result = NextDoorWeb.ProductJSON.show(%{products: products})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:#{conn.request_path}", cache_value, expire: 60)
      json(conn, result)
    end
  end

  def index(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, products} <- Products.index(owner_id) do
      result = NextDoorWeb.ProductJSON.show(%{products: products})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 60)
      json(conn, result)
    end
  end
  
  def update(conn, %{"id" => id, "product" => product}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    product =
      case Map.has_key?(product, "quantity") do
        true -> Map.put(product, "inventory", %{"quantity" => Map.get(product, "quantity")})
        false -> product
      end

    with {:ok, p} <- Products.update(owner_id, id, product) do
      render(conn, :create, %{product: p})
    end
  end

  def delete(conn, %{"id" => product_id}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, _} <- Products.delete(product_id, owner_id) do
      Cachex.del(@cache, "view_cache:owner:#{owner_id}./api/store/product")
      conn
      |> put_status(:ok)
      |> send_resp(:ok, "")
    end
  end
end
