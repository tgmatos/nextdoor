defmodule NextDoorWeb.StoreController do
  use NextDoorWeb, :controller
  alias NextDoor.Stores
  @cache :nd_cache
    
  action_fallback(NextDoorWeb.FallbackController)

  def create(conn, %{
        "name" => name,
        "description" => description,
        "telephone" => telephone,
        "category" => category
      }) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)

    with {:ok, store} <-
           Stores.create(%{
             name: name,
             description: description,
             telephone: telephone,
             category: category,
             owner_id: owner_id
           }) do
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
      #json_response = Jason.encode!(result)
      #cache_value = {200, json_response}
      # Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def show(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, store} <- Stores.show(%{owner_id: owner_id}) do
      result = NextDoorWeb.StoreJSON.show(%{store: store})
      json_response = Jason.encode!(result)
      cache_value = {200, json_response}
      Cachex.put(@cache, "view_cache:owner:#{owner_id}.#{conn.request_path}", cache_value, expire: 1000)
      json(conn, result)
    end
  end

  def update(conn, %{"store" => store}) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, s} <- Stores.update(store, owner_id) do
      render(conn, :show, %{store: s})
    end
  end

  def delete(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, _} <- Stores.delete(owner_id) do
      conn
      |> put_status(:ok)
      |> send_resp(:ok, "")
    end
  end
end
