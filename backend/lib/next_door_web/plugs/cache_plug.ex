defmodule NextDoorWeb.CachePlug do
  import Plug.Conn
  @cache :nd_cache

  def init(opts), do: opts

  def call(conn, _opts) do
    cache_key = case Guardian.Plug.current_claims(conn) do
      nil -> "view_cache:#{conn.request_path}"
      claims ->
        %{"sub" => owner_id} = claims
        "view_cache:owner:#{owner_id}.#{conn.request_path}"
    end
    
    case Cachex.get(@cache, cache_key) do
      {:ok, nil} -> conn
      {:ok, {status, response}} ->
        conn
        |> put_resp_content_type("application/json")
        |> send_resp(status, response)
        |> halt()
    end
  end
end
