defmodule NextDoorWeb.CORS do
  use Corsica.Router,
    origins: {NextDoorWeb.CORS, :allowed_origin?, []},
    allow_credentials: true,
    allow_methods: :all,
    allow_headers: :all,
    max_age: 7200

  def allowed_origin?(_conn, origin) when is_binary(origin) do
    origin in Application.get_env(:next_door, :cors_origins, [
      "http://localhost:3000",
      "http://127.0.0.1:3000"
    ])
  end

  def allowed_origin?(_conn, _origin), do: false

  resource("/*")
end
