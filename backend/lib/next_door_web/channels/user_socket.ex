defmodule NextDoorWeb.UserSocket do
  use Phoenix.Socket

  alias NextDoor.AccountManager

  channel("store:order:*", NextDoorWeb.StoreOrderChannel)
  channel("account:order:*", NextDoorWeb.AccountOrderChannel)

  def connect(params, socket, _connect_info) do
    case AccountManager.decode_and_verify(params["token"]) do
      {:ok, claims} ->
        {:ok, assign(socket, :account_id, claims["sub"])}

      {:error, _} ->
        {:error, :unauthorized}
    end
  end

  def id(socket), do: "user_socket:#{socket.assigns.account_id}"
end
