# This file is responsible for configuring your application
# and its dependencies with the aid of the Config module.
#
# This configuration file is loaded before any dependency and
# is restricted to this project.

# General application configuration
import Config

config :next_door,
  ecto_repos: [NextDoor.Repo],
  generators: [timestamp_type: :utc_datetime]

# Configures the endpoint
config :next_door, NextDoorWeb.Endpoint,
  url: [host: "0.0.0.0"],
  adapter: Bandit.PhoenixAdapter,
  render_errors: [
    formats: [json: NextDoorWeb.ErrorJSON],
    layout: false
  ],
  pubsub_server: NextDoor.PubSub,
  live_view: [signing_salt: "3auNqBwi"]

# Configures the mailer
#
# By default it uses the "Local" adapter which stores the emails
# locally. You can see the emails in your browser, at "/dev/mailbox".
#
# For production it's recommended to configure a different adapter
# at the `config/runtime.exs`.
config :next_door, NextDoor.Mailer, adapter: Swoosh.Adapters.Local

# Configures Elixir's Logger
config :logger, :console,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

# Use Jason for JSON parsing in Phoenix
config :phoenix, :json_library, Jason

config :next_door, NextDoor.AccountManager,
  issuer: "NextDoor",
  secret_key: "804dphpqK1nN0wNAnBx+V/iQcLeabgBlxNlyMz8MXIplGzYn3OUj7Gd8mw4vdLkS"

# Import environment specific config. This must remain at the bottom
# of this file so it overrides the configuration defined above.
import_config "#{config_env()}.exs"
