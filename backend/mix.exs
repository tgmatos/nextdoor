defmodule NextDoor.MixProject do
  use Mix.Project

  def project do
    [
      app: :next_door,
      version: "0.1.0",
      elixir: "~> 1.19",
      elixirc_paths: elixirc_paths(Mix.env()),
      start_permanent: Mix.env() == :prod,
      aliases: aliases(),
      test_coverage: [summary: [threshold: 85]],
      deps: deps()
      # compilers: [:phoenix_swagger]
      # compilers: [:phoenix, :gettext] ++ Mix.compilers ++ [:phoenix_swagger],
    ]
  end

  # Configuration for the OTP application.
  #
  # Type `mix help compile.app` for more information.
  def application do
    [
      mod: {NextDoor.Application, []},
      extra_applications: [:logger, :runtime_tools]
    ]
  end

  # Specifies which paths to compile per environment.
  defp elixirc_paths(:test), do: ["lib", "test/support"]
  defp elixirc_paths(_), do: ["lib"]

  # Specifies your project dependencies.
  #
  # Type `mix help deps` for examples and options.
  defp deps do
    [
      {:phoenix, "~> 1.8"},
      {:phoenix_ecto, "~> 4.7"},
      {:ecto_sql, "~> 3.14"},
      {:postgrex, "~> 0.22"},
      {:swoosh, "~> 1.27"},
      {:finch, "~> 0.23"},
      {:telemetry_metrics, "~> 1.1"},
      {:telemetry_poller, "~> 1.3"},
      {:jason, "~> 1.4"},
      {:dns_cluster, "~> 0.2"},
      {:bandit, "~> 1.12"},
      {:guardian, "~> 2.4"},
      {:argon2_elixir, "~> 4.1"},
      {:cachex, "~> 4.1"},
      {:phoenix_live_dashboard, "~> 0.8"},
      {:corsica, "~> 2.1"},
      {:ecto_erd, "~> 0.7"}
      # {:phoenix_swagger, "~> 0.8.5"},
      # {:open_api_spex, "~> 3.22"},
      # {:poison, "~> 6.0"}
    ]
  end

  # Aliases are shortcuts or tasks specific to the current project.
  # For example, to install project dependencies and perform other setup tasks, run:
  #
  #     $ mix setup
  #
  # See the documentation for `Mix` for more info on aliases.
  defp aliases do
    [
      setup: ["deps.get"]
    ]
  end
end
