defmodule NextDoorWeb.StoreController do
  use NextDoorWeb, :controller
  alias NextDoor.Stores
  alias NextDoor.Cache
  @cache :nd_cache

  action_fallback(NextDoorWeb.FallbackController)

  def create(conn, params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, image} <- decode_base64_image(params["image"]),
         {:ok, store} <-
           Stores.create(%{
             name: params["name"],
             description: params["description"],
             telephone: params["telephone"],
             category: params["category"],
             image: image,
             owner_id: owner_id
           }) do
      Cache.clear_view_cache("view_cache:/api/stores")
      render(conn, :create, %{store: store})
    end
  end

  def index(conn, _params) do
    with {:ok, stores} <- Stores.index() do
      result = NextDoorWeb.StoreJSON.index(%{stores: stores})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def get_by_id(conn, %{"id" => id}) do
    with {:ok, store} <- Stores.show(id) do
      result = NextDoorWeb.StoreJSON.show(%{store: store})
      json(conn, result)
    end
  end

  def show(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, store} <- Stores.show(%{owner_id: owner_id}) do
      result = NextDoorWeb.StoreJSON.show(%{store: store})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}

      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value,
        expire: 1000
      )

      json(conn, result)
    end
  end

  def update(conn, %{"store" => store_params}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, store} <- prepare_store_params(store_params),
         {:ok, store} <- Stores.update(store, owner_id) do
      Cache.clear_view_cache("view_cache:/api/stores")
      Cache.clear_view_cache("view_cache:owner:#{owner_id}.")
      render(conn, :show, %{store: store})
    end
  end

  def update(_conn, _params), do: {:error, :missing_params}

  def delete(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, _} <- Stores.delete(owner_id) do
      Cache.clear_view_cache("view_cache:/api/stores")
      Cache.clear_view_cache("view_cache:owner:#{owner_id}.")

      conn
      |> send_resp(:no_content, "")
    end
  end

  defp prepare_store_params(store) do
    if Map.has_key?(store, "image") and not is_nil(store["image"]) do
      with {:ok, image} <- decode_base64_image(store["image"]) do
        {:ok, Map.put(store, "image", image)}
      end
    else
      {:ok, Map.delete(store, "image")}
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
