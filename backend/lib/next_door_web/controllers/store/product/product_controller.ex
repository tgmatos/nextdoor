defmodule NextDoorWeb.ProductController do
  use NextDoorWeb, :controller
  alias NextDoor.Products
  alias NextDoor.Cache
  @cache :nd_cache

  action_fallback(NextDoorWeb.FallbackController)

  def create(conn, params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    product = params["product"] || %{}

    with {:ok, image} <- decode_base64_image(product["image"]),
         {:ok, product} <-
           Products.create(owner_id, %{
             name: product["name"],
             description: product["description"],
             price: product["price"],
             image: image,
             inventory: %{quantity: product["quantity"]}
           }) do
      Cache.clear_view_cache("view_cache:/api/stores")
      Cache.clear_view_cache("view_cache:owner:#{owner_id}.")
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

      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value,
        expire: 60
      )

      json(conn, result)
    end
  end

  def update(conn, %{"id" => id, "product" => product}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, product} <- prepare_product_params(product),
         {:ok, product} <- Products.update(owner_id, id, product) do
      Cache.clear_view_cache("view_cache:/api/stores")
      Cache.clear_view_cache("view_cache:owner:#{owner_id}.")
      render(conn, :create, %{product: product})
    end
  end

  def update(_conn, _params), do: {:error, :missing_params}

  def delete(conn, %{"id" => product_id}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, _} <- Products.delete(product_id, owner_id) do
      Cache.clear_view_cache("view_cache:/api/stores")
      Cache.clear_view_cache("view_cache:owner:#{owner_id}.")

      conn
      |> send_resp(:no_content, "")
    end
  end

  defp prepare_product_params(product) do
    product =
      case Map.has_key?(product, "quantity") and not is_nil(product["quantity"]) do
        true -> Map.put(product, "inventory", %{"quantity" => product["quantity"]})
        false -> product
      end

    if Map.has_key?(product, "image") and not is_nil(product["image"]) do
      with {:ok, image} <- decode_base64_image(product["image"]) do
        {:ok, Map.put(product, "image", image)}
      end
    else
      {:ok, Map.delete(product, "image")}
    end
  end

  defp decode_base64_image(nil), do: {:error, :invalid_base64}

  defp decode_base64_image(base64_string) do
    cleaned =
      base64_string
      |> String.replace(~r/^data:image\/[a-z]+;base64,/, "")

    case Base.decode64(cleaned) do
      {:ok, binary} -> {:ok, binary}
      :error -> {:error, :invalid_base64}
    end
  end
end
