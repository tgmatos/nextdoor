defmodule NextDoorWeb.StoreOrderChannelTest do
  use NextDoorWeb.ChannelCase

  alias NextDoor.Orders
  alias NextDoorWeb.UserSocket

  describe "join" do
    test "store owner can join its own store order topic" do
      owner = account_fixture()
      store = store_fixture(owner)

      {:ok, token, _claims} = AccountManager.encode_and_sign(owner)
      {:ok, socket} = connect(UserSocket, %{token: token})

      assert {:ok, _reply, joined} =
               subscribe_and_join(socket, "store:order:#{store.owner_id}", %{})

      assert joined.assigns.account_id == owner.id
    end

    test "account without a store cannot join" do
      owner = account_fixture()
      store = store_fixture(owner)
      intruder = account_fixture()

      {:ok, token, _claims} = AccountManager.encode_and_sign(intruder)
      {:ok, socket} = connect(UserSocket, %{token: token})

      assert {:error, %{reason: "unauthorized"}} =
               subscribe_and_join(socket, "store:order:#{store.owner_id}", %{})
    end

    test "invalid token is rejected at connect" do
      assert :error = connect(UserSocket, %{token: "invalid-token"})
    end
  end

  describe "new_order pushes" do
    test "broadcasts a new order to the store owner" do
      owner = account_fixture()
      customer = account_fixture()
      store = store_fixture(owner)
      product = product_fixture(store)

      {:ok, token, _claims} = AccountManager.encode_and_sign(owner)
      {:ok, socket} = connect(UserSocket, %{token: token})

      {:ok, _, _} =
        subscribe_and_join(socket, "store:order:#{store.owner_id}", %{})

      {:ok, order} =
        Orders.create_order(%{
          store_id: store.id,
          customer_id: customer.id,
          products: [%{"product" => product.id, "quantity" => 1}],
          payment_method: "PIX"
        })

      assert_push("new_order", payload)
      assert payload.id == order.id
      assert payload.status_order == "ESPERANDO"
      assert payload.payment_method == "PIX"
      assert payload.client.id == customer.id
    end
  end

  describe "order_updated pushes" do
    test "broadcasts a status change to the store owner" do
      owner = account_fixture()
      customer = account_fixture()
      store = store_fixture(owner)
      product = product_fixture(store)

      {:ok, token, _claims} = AccountManager.encode_and_sign(owner)
      {:ok, socket} = connect(UserSocket, %{token: token})

      {:ok, _, _} =
        subscribe_and_join(socket, "store:order:#{store.owner_id}", %{})

      {:ok, order} =
        Orders.create_order(%{
          store_id: store.id,
          customer_id: customer.id,
          products: [%{"product" => product.id, "quantity" => 1}],
          payment_method: "PIX"
        })

      {:ok, updated} =
        Orders.update_status_order(order.id, owner.id, %{
          before: "ESPERANDO",
          after: "ACEITO"
        })

      assert_push("order_updated", payload)
      assert payload.id == updated.id
      assert payload.status_order == "ACEITO"
      assert payload.payment_method == "PIX"
      assert payload.total == updated.total
    end
  end
end
