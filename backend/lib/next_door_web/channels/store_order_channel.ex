defmodule NextDoorWeb.StoreOrderChannel do
  use NextDoorWeb, :channel

  alias NextDoor.{Repo, Store}
  alias NextDoorWeb.OrderJSON

  def join("store:order:" <> owner_id, _payload, socket) do
    account_id = socket.assigns.account_id

    if account_id == owner_id and Repo.get_by(Store, owner_id: account_id) do
      {:ok, socket}
    else
      {:error, %{reason: "unauthorized"}}
    end
  end

  def join(_topic, _payload, _socket), do: {:error, %{reason: "unauthorized"}}

  def handle_info({:new_order, order}, socket) do
    push(socket, "new_order", OrderJSON.show(%{order: order}))
    {:noreply, socket}
  end

  def handle_info({:order_updated, payload}, socket) do
    push(socket, "order_updated", payload)
    {:noreply, socket}
  end
end
