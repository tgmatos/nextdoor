defmodule NextDoorWeb.Router do
  use NextDoorWeb, :router
  import Phoenix.LiveDashboard.Router

  pipeline :api do
    plug(:accepts, ["json"])
  end

  pipeline :auth do
    plug(NextDoorWeb.AuthenticationPipeline)
  end

  # Unauthenticated routes
  scope "/api", NextDoorWeb do
    pipe_through(:api)

    scope "/account" do
      post("/register", AccountController, :register)
      post("/login", AccountController, :login)
    end

    resources("/stores", StoreController, only: [:index, :show])
  end

  # Authenticated routes
  scope "/api", NextDoorWeb do
    pipe_through([:api, :auth])
    resources("/account", AccountController, only: [:show, :update, :delete], singleton: true)

    resources "/store", StoreController,
      only: [:create, :update, :delete, :show],
      singleton: true do
      resources("/product", ProductController, only: [:create, :update, :delete, :index])
    end
  end

  # Enable Swoosh mailbox preview in development
  if Application.compile_env(:next_door, :dev_routes) do
    scope "/" do
      # pipe_through :browser
      live_dashboard("/dashboard", metrics: NextDoorWeb.Telemetry)
    end

    scope "/dev" do
      pipe_through([:fetch_session, :protect_from_forgery])

      forward("/mailbox", Plug.Swoosh.MailboxPreview)
    end
  end
end
