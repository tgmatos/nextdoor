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
  scope("/api", NextDoorWeb) do
    pipe_through(:api)

    scope("/account") do
      post("/register", AccountController, :register)
      post("/login", AccountController, :login)
      get("/logout", AccountController, :logout)
    end

    scope("/stores") do
      get("/", StoreController, :index)
      get("/:id", StoreController, :get_by_id)
      scope("/:id/product") do
        get("/", ProductController, :list)
      end
    end
  end

  # Authenticated routes
  scope("/api", NextDoorWeb) do
    pipe_through([:api, :auth])
    
    resources("/account", AccountController,
              only: [:show, :delete],
              singleton: true) do
      patch("/", AccountController, :update)
      scope("/order") do
        get("/", OrderController, :get_orders_by_customer)
        get("/:id", OrderController, :get_order_by_customer)
      end

      scope("/address") do
        get("/", AddressController, :list_addresses)
        get("/:id", AddressController, :get_address)
        patch("/:id", AddressController, :update_address)
      end
    end

    resources("/store", StoreController,
              only: [:create, :update, :delete, :show],
              singleton: true) do
      scope("/order") do
        get("/", OrderController, :list_orders_by_store)
        get("/:id", OrderController, :get_order_by_store)
        patch("/:id", OrderController, :update_status_order)
      end
      resources("/product", ProductController, only: [:create, :update, :delete, :index])
    end
  end

  # Enable Swoosh mailbox preview in development
  if Application.compile_env(:next_door, :dev_routes) do
    scope("/") do
      # pipe_through :browser
      live_dashboard("/dashboard", metrics: NextDoorWeb.Telemetry)
    end

    scope("/dev")do
      pipe_through([:fetch_session, :protect_from_forgery])

      forward("/mailbox", Plug.Swoosh.MailboxPreview)
    end
  end
end
