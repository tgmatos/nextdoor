defmodule NextDoorWeb.AuthErrorHandler do
  import Plug.Conn
  import Phoenix.Controller

  @behaviour Guardian.Plug.ErrorHandler

  @impl Guardian.Plug.ErrorHandler
  def auth_error(conn, {_type, _reason}, _opts) do
    conn
    |> put_status(401)
    |> put_view(json: NextDoorWeb.ErrorJSON)
    |> render(:"401")
  end
end
