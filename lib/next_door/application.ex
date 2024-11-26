defmodule NextDoor.Application do
  # See https://hexdocs.pm/elixir/Application.html
  # for more information on OTP Applications
  @moduledoc false

  use Application

  @impl true
  def start(_type, _args) do
    children = [
      NextDoorWeb.Telemetry,
      NextDoor.Repo,
      {DNSCluster, query: Application.get_env(:next_door, :dns_cluster_query) || :ignore},
      {Phoenix.PubSub, name: NextDoor.PubSub},
      # Start the Finch HTTP client for sending emails
      {Finch, name: NextDoor.Finch},
      # Start a worker by calling: NextDoor.Worker.start_link(arg)
      # {NextDoor.Worker, arg},
      # Start to serve requests, typically the last entry
      NextDoorWeb.Endpoint
    ]

    # See https://hexdocs.pm/elixir/Supervisor.html
    # for other strategies and supported options
    opts = [strategy: :one_for_one, name: NextDoor.Supervisor]
    Supervisor.start_link(children, opts)
  end

  # Tell Phoenix to update the endpoint configuration
  # whenever the application is updated.
  @impl true
  def config_change(changed, _new, removed) do
    NextDoorWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
