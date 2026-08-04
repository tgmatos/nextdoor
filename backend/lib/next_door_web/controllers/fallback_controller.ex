defmodule NextDoorWeb.FallbackController do
  use NextDoorWeb, :controller
  import Ecto.Changeset
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

  def call(conn, {:error, :not_found}) do
    conn
    |> put_status(404)
    |> put_view(json: NextDoorWeb.ErrorJSON)
    |> render(:"404")
  end

  def call(conn, {:error, :invalid_uuid}) do
    conn
    |> put_status(400)
    |> put_view(json: NextDoorWeb.ErrorJSON)
    |> render(:"400")
  end

  def call(conn, {:error, :invalid_base64}) do
    conn
    |> put_status(400)
    |> json(%{errors: %{image: ["invalid base64 image"]}})
  end

  def call(conn, {:error, :missing_params}) do
    conn
    |> put_status(422)
    |> json(%{errors: %{detail: "missing required parameters"}})
  end

  def call(conn, {:error, :invalid_payload}) do
    conn
    |> put_status(422)
    |> json(%{errors: %{detail: "invalid payload"}})
  end

  def call(conn, {:error, :address_not_found}) do
    conn
    |> put_status(422)
    |> json(%{errors: %{detail: "customer has no address"}})
  end

  def call(conn, {:error, :product_not_found}) do
    conn
    |> put_status(422)
    |> json(%{errors: %{detail: "one or more products were not found"}})
  end

  def call(conn, {:error, :insufficient_stock}) do
    conn
    |> put_status(422)
    |> json(%{errors: %{detail: "insufficient stock"}})
  end

  def call(conn, {:error, %Ecto.Changeset{} = changeset}) do
    conn
    |> put_status(422)
    |> json(%{errors: traverse_errors(changeset, &translate_error/1)})
  end

  def call(conn, _reason) do
    conn
    |> put_status(500)
    |> put_view(json: NextDoorWeb.ErrorJSON)
    |> render(:"500")
  end

  defp translate_error({message, opts}) do
    Regex.replace(~r"%{(\w+)}", message, fn _, key ->
      opts |> Keyword.get(String.to_existing_atom(key), key) |> to_string()
    end)
  end
end
