defmodule NextDoorWeb.CachePlug do
  import Plug.Conn
  @cache :nd_cache

  def init(opts), do: opts

  def call(conn, _opts) do
    # %{req_headers: headers} = conn
    # [{_, bearer}] = headers
    # |> Enum.filter(fn x ->
    #   {a, b} = x
    #   case a == "authorization" do
    #     true -> b
    #     false -> nil
    #   end
    # end)

    # [_, token] = String.split(bearer, "Bearer ")
    # cache_key = case NextDoor.AccountManager.decode_and_verify(token) do
    #   {:ok, %{"sub" => owner_id}} ->
    #     "view_cache:owner:#{owner_id}.#{conn.request_path}"
    #     {:error, _} -> "view_cache:#{conn.request_path}"
    # end

    ### Fazer um switch baseado nas possíveis chaves.
    # case conn.request_path do

    # end
    IO.inspect(conn.request_path)
    cache_key = "asdf"

    case Cachex.get(@cache, cache_key) do
      {:ok, nil} ->
        conn
      {:ok, {status, response}} ->
        conn
        |> put_resp_content_type("application/json")
        |> send_resp(status, response)
        |> halt()
    end
  end
end
