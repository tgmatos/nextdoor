defmodule NextDoorWeb.ChannelCase do
  @moduledoc """
  This module defines the test case to be used by
  tests that require connecting to a socket or joining channels.

  Such tests rely on `Phoenix.ChannelTest` and also
  import other functionality to make it easier
  to build common data structures and query the data layer.

  It sets up the Ecto sandbox so database changes are
  rolled back at the end of every test.
  """

  use ExUnit.CaseTemplate

  using do
    quote do
      @endpoint NextDoorWeb.Endpoint

      use NextDoorWeb, :verified_routes

      # Import conveniences for testing channels
      import Phoenix.ChannelTest
      import NextDoorWeb.ChannelCase
      import NextDoor.Fixtures

      alias NextDoor.AccountManager
    end
  end

  setup tags do
    NextDoor.DataCase.setup_sandbox(tags)
    :ok
  end
end
