defmodule NextDoorWeb.Endpoint do
  use Phoenix.Endpoint, otp_app: :next_door
  
  # The session will be stored in the cookie and signed,
  # this means its contents can be read but not tampered with.
  # Set :encryption_salt if you would also like to encrypt it.
  @session_options [
    store: :cookie,
    key: "_next_door_key",
    signing_salt: "HTFYkH/B",
    same_site: "Lax"
  ]

  socket "/live", Phoenix.LiveView.Socket
  plug Phoenix.LiveDashboard.RequestLogger,
       param_key: "request_logger",
       cookie_key: "request_logger"
  #   websocket: [connect_info: [session: @session_options]],
  #   longpoll: [connect_info: [session: @session_options]]

  # Serve at "/" the static files from "priv/static" directory.
  #
  # You should set gzip to true if you are running phx.digest
  # when deploying your static files in production.
  plug Plug.Static,
    at: "/",
    from: :next_door,
    gzip: false,
    only: NextDoorWeb.static_paths()

  # Code reloading can be explicitly enabled under the
  # :code_reloader configuration of your endpoint.
  if code_reloading? do
    plug Phoenix.CodeReloader
    plug Phoenix.Ecto.CheckRepoStatus, otp_app: :next_door
  end

  plug Plug.RequestId
  plug Plug.Telemetry, event_prefix: [:phoenix, :endpoint]

  plug Plug.Parsers,
    parsers: [:urlencoded, :multipart, :json],
    pass: ["*/*"],
    json_decoder: Phoenix.json_library()

  plug Plug.MethodOverride
  plug Plug.Head
  plug Plug.Session, @session_options
  #plug Corsica, origins: "http://localhost:5173"
  plug NextDoorWeb.CORS
  plug NextDoorWeb.Router
end
