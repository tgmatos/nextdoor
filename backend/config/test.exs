import Config

# Configure your database
#
# The MIX_TEST_PARTITION environment variable can be used
# to provide built-in test partitioning in CI environment.
# Run `mix help test` for more information.
config :next_door, NextDoor.Repo,
  database: System.get_env("TEST_DB"),
  username: System.get_env("DB_USERNAME"),
  password: System.get_env("DB_PASSWORD"),
  hostname: System.get_env("DB_HOST"),
  stacktrace: true,
  pool: Ecto.Adapters.SQL.Sandbox,
  pool_size: System.schedulers_online() * 2,
  show_sensitive_data_on_connection_error: true

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :next_door, NextDoorWeb.Endpoint,
  http: [ip: {127, 0, 0, 1}, port: 4002],
  secret_key_base: "YplAcKAwIVDsWimugR0qGv/oPt9UWy3YxKiu6wxLWdSmaHMGgMt4ySleMkKrAqYr",
  server: false

# In test we don't send emails
config :next_door, NextDoor.Mailer, adapter: Swoosh.Adapters.Test

# Disable swoosh api client as it is only required for production adapters
config :swoosh, :api_client, false

# Print only warnings and errors during test
config :logger, level: :warning

# Initialize plugs at runtime for faster test compilation
config :phoenix, :plug_init_mode, :runtime
