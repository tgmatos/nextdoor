defmodule NextDoorWeb.AuthErrorHandler do
  import Plug.Conn

  @behaviour Guardian.Plug.ErrorHandler

  @impl Guardian.Plug.ErrorHandler
  def auth_error(conn, {type, _reason}, _opts) do
    {_, body} = Jason.encode(%{error: type})

    conn
    |> put_resp_content_type("application/json")
    |> send_resp(401, body)
  end
end
