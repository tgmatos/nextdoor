defmodule NextDoorWeb.AccountOrderChannelTest do
  use NextDoorWeb.ChannelCase

  alias NextDoor.Orders
  alias NextDoorWeb.UserSocket

  describe "join" do
    test "customer can join its own account order topic" do
      customer = account_fixture()

      {:ok, token, _claims} = AccountManager.encode_and_sign(customer)
      {:ok, socket} = connect(UserSocket, %{token: token})

      assert {:ok, _reply, joined} =
               subscribe_and_join(socket, "account:order:#{customer.id}", %{})

      assert joined.assigns.account_id == customer.id
    end

    test "cannot join another account topic" do
      customer = account_fixture()
      intruder = account_fixture()

      {:ok, token, _claims} = AccountManager.encode_and_sign(intruder)
      {:ok, socket} = connect(UserSocket, %{token: token})

      assert {:error, %{reason: "unauthorized"}} =
               subscribe_and_join(socket, "account:order:#{customer.id}", %{})
    end
  end

  describe "order_updated pushes" do
    test "broadcasts status update to the customer" do
      owner = account_fixture()
      customer = account_fixture()
      store = store_fixture(owner)
      product = product_fixture(store)
      order = order_fixture(customer, store, product)

      {:ok, token, _claims} = AccountManager.encode_and_sign(customer)
      {:ok, socket} = connect(UserSocket, %{token: token})
      {:ok, _, _} = subscribe_and_join(socket, "account:order:#{customer.id}", %{})

      {:ok, _updated} =
        Orders.update_status_order(order.id, owner.id, %{before: "ESPERANDO", after: "ACEITO"})

      assert_push("order_updated", payload)
      assert payload.id == order.id
      assert payload.status_order == "ACEITO"
      assert payload.payment_method == "PIX"
    end
  end
end
