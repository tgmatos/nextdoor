defmodule NextDoorWeb.AddressController do
  use NextDoorWeb, :controller
  alias NextDoor.Addresses

  action_fallback(NextDoorWeb.FallbackController)

  def update_address(conn, %{"id" => id, "address" => address}) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, address} <-
           Addresses.update_address(%{account_id: account_id, address_id: id, address: address}) do
      json(conn, address)
    end
  end

  def update_address(_conn, _params), do: {:error, :missing_params}

  def list_addresses(conn, _params) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, addresses} <- Addresses.list_addresses(%{account_id: account_id}) do
      json(conn, addresses)
    end
  end

  def get_address(conn, %{"id" => id}) do
    %{"sub" => account_id} = Guardian.Plug.current_claims(conn)

    with {:ok, address} <- Addresses.get_address(%{account_id: account_id, address_id: id}) do
      json(conn, address)
    end
  end
end
