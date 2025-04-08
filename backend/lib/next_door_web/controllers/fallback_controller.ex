defmodule NextDoorWeb.FallbackController do
  use NextDoorWeb, :controller
  import Plug.Conn

  def call(conn, {:error, :unauthorized}) do
	conn
	|> put_status(401)
	|> put_view(json: NextDoorWeb.ErrorJSON)
	|> render(:"401")
  end

  def call(conn, {:error, :store_not_found}) do
	conn
	|> put_status(404)
	|> put_view(json: NextDoorWeb.ErrorJSON)
	|> render(:"404")
  end

  def call(conn, {:error, :record_not_found}) do
	conn
	|> put_status(404)
	|> put_view(json: NextDoorWeb.ErrorJSON)
	|> render(:"404")
  end
  
  def call(conn, reason) do
	IO.inspect(reason)
	conn
	|> put_status(500)
	|> put_view(json: NextDoorWeb.ErrorJSON)
	|> render(:"500")
  end
end
