defmodule NextDoorWeb.AccountOrderChannel do
  use NextDoorWeb, :channel

  def join("account:order:" <> account_id, _payload, socket) do
    if account_id == socket.assigns.account_id do
      {:ok, socket}
    else
      {:error, %{reason: "unauthorized"}}
    end
  end

  def join(_topic, _payload, _socket), do: {:error, %{reason: "unauthorized"}}

  def handle_info({:order_updated, order}, socket) do
    push(socket, "order_updated", %{
      id: order.id,
      total: order.total,
      status_order: order.status_order,
      payment_method: order.payment_method
    })

    {:noreply, socket}
  end
end
