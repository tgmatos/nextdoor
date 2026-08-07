defmodule NextDoor.ContextsTest do
  use NextDoor.DataCase

  alias NextDoor.{Accounts, Stores, Products, Addresses, Orders, Account, Store, Cache}

  setup do
    NextDoor.Cache.flush()

    owner = owner_fixture()
    store = store_fixture(owner)
    product = product_fixture(store)
    customer = customer_fixture()

    {:ok, owner: owner, store: store, product: product, customer: customer}
  end

  defp owner_fixture do
    {:ok, account} =
      NextDoor.Repo.insert(%Account{
        email: "ctx_owner_#{System.unique_integer([:positive])}@example.com",
        password: Argon2.hash_pwd_salt("Password1!"),
        username: "ctx_owner_#{System.unique_integer([:positive])}"
      })

    account
  end

  defp customer_fixture do
    {:ok, account} =
      NextDoor.Repo.insert(%Account{
        email: "ctx_customer_#{System.unique_integer([:positive])}@example.com",
        password: Argon2.hash_pwd_salt("Password1!"),
        username: "ctx_customer_#{System.unique_integer([:positive])}"
      })

    account
  end

  defp store_fixture(account) do
    {:ok, store} =
      Stores.create(%{
        name: "Ctx Store #{System.unique_integer([:positive])}",
        description: "desc",
        telephone: "99#{System.unique_integer([:positive])}",
        category: "VESTUARIO",
        image: "img",
        owner_id: account.id
      })

    store
  end

  defp product_fixture(store) do
    {:ok, product} =
      Products.create(store.owner_id, %{
        name: "Ctx Product #{System.unique_integer([:positive])}",
        description: "desc",
        price: Decimal.new("10.00"),
        image: "img",
        inventory: %{quantity: 5}
      })

    product
  end

  describe "Accounts" do
    test "get returns record_not_found for a missing account" do
      assert Accounts.get(Ecto.UUID.generate()) == {:error, :record_not_found}
    end

    test "update returns record_not_found for a missing account" do
      assert Accounts.update(%{account_id: Ecto.UUID.generate(), account: %{email: "x@y.com"}}) ==
               {:error, :record_not_found}
    end

    test "delete returns record_not_found for a missing account" do
      assert Accounts.delete(Ecto.UUID.generate()) == {:error, :record_not_found}
    end
  end

  describe "Stores" do
    test "create returns a changeset error for missing fields", %{owner: owner} do
      assert {:error, changeset} = Stores.create(%{owner_id: owner.id})
      refute changeset.valid?
    end

    test "show by owner returns record_not_found for a missing store" do
      other = owner_fixture()
      assert Stores.show(%{owner_id: other.id}) == {:error, :store_not_found}
    end

    test "update returns store_not_found when the owner has no store" do
      other = owner_fixture()
      assert Stores.update(%{name: "X"}, other.id) == {:error, :store_not_found}
    end

    test "delete returns store_not_found when the owner has no store" do
      other = owner_fixture()
      assert Stores.delete(other.id) == {:error, :store_not_found}
    end

    test "get_by_id returns invalid_uuid for a malformed id" do
      assert Stores.get_by_id("nope") == {:error, :invalid_uuid}
    end
  end

  describe "Products" do
    test "create returns record_not_found when the owner has no store" do
      other = owner_fixture()
      assert Products.create(other.id, %{}) == {:error, :record_not_found}
    end

    test "list_products returns invalid_uuid for a malformed store id" do
      assert Products.list_products("nope") == {:error, :invalid_uuid}
    end

    test "update returns record_not_found for a missing product", %{owner: owner} do
      assert Products.update(owner.id, Ecto.UUID.generate(), %{name: "X"}) ==
               {:error, :record_not_found}
    end

    test "delete returns record_not_found for a missing product", %{owner: owner} do
      assert Products.delete(Ecto.UUID.generate(), owner.id) == {:error, :record_not_found}
    end
  end

  describe "Addresses" do
    test "get_address returns record_not_found for a missing address", %{owner: owner} do
      assert Addresses.get_address(%{account_id: owner.id, address_id: Ecto.UUID.generate()}) ==
               {:error, :record_not_found}
    end

    test "get_address returns invalid_uuid for a malformed id", %{owner: owner} do
      assert Addresses.get_address(%{account_id: owner.id, address_id: "nope"}) ==
               {:error, :invalid_uuid}
    end
  end

  describe "Orders" do
    test "get_order_by_store returns record_not_found for a missing order", %{owner: owner} do
      assert Orders.get_order_by_store(%{owner_id: owner.id, order_id: Ecto.UUID.generate()}) ==
               {:error, :record_not_found}
    end

    test "get_order_by_store returns invalid_uuid for a malformed id", %{owner: owner} do
      assert Orders.get_order_by_store(%{owner_id: owner.id, order_id: "nope"}) ==
               {:error, :invalid_uuid}
    end

    test "update_status_order returns invalid_uuid for a malformed id", %{owner: owner} do
      assert Orders.update_status_order("nope", owner.id, %{before: "ESPERANDO", after: "ACEITO"}) ==
               {:error, :invalid_uuid}
    end

    test "update_status_order returns not_found for a missing order", %{owner: owner} do
      assert Orders.update_status_order(Ecto.UUID.generate(), owner.id, %{
               before: "ESPERANDO",
               after: "ACEITO"
             }) ==
               {:error, :not_found}
    end

    test "update_status_order rejects an invalid transition", %{
      owner: owner,
      customer: customer,
      store: store
    } do
      order = order_fixture(customer, store)

      assert Orders.update_status_order(order.id, owner.id, %{before: "ESPERANDO", after: "ROTA"}) ==
               {:error, :invalid_transition}
    end
  end

  describe "Soft delete" do
    test "store delete marks the store inactive and hides it from read paths", %{
      owner: owner,
      store: store
    } do
      assert {:ok, deleted} = Stores.delete(owner.id)
      refute deleted.active

      assert Stores.show(store.id) == {:error, :store_not_found}
      assert Stores.get_by_id(store.id) == {:error, :store_not_found}
      assert Stores.show(%{owner_id: owner.id}) == {:error, :store_not_found}

      {:ok, %{entries: stores}} = Stores.index()
      refute Enum.any?(stores, &(&1.id == store.id))
    end

    test "Products.create returns record_not_found for an inactive store", %{
      owner: owner
    } do
      assert {:ok, _} = Stores.delete(owner.id)
      assert Products.create(owner.id, %{}) == {:error, :record_not_found}
    end

    test "Orders.create_order returns store_not_found for an inactive store", %{
      owner: owner,
      store: store,
      customer: customer,
      product: product
    } do
      ensure_address(customer)
      assert {:ok, _} = Stores.delete(owner.id)

      assert Orders.create_order(%{
               store_id: store.id,
               customer_id: customer.id,
               products: [%{"product" => product.id, "quantity" => 1}],
               payment_method: "PIX"
             }) == {:error, :store_not_found}
    end

    test "product delete marks the product inactive and hides it from listings", %{
      owner: owner,
      store: store,
      product: product,
      customer: customer
    } do
      ensure_address(customer)
      assert {:ok, deleted} = Products.delete(product.id, owner.id)
      refute deleted.active

      {:ok, %{entries: products}} = Products.list_products(store.id)
      refute Enum.any?(products, &(&1.id == product.id))

      {:ok, %{entries: products}} = Products.index(owner.id)
      refute Enum.any?(products, &(&1.id == product.id))

      assert Products.update(owner.id, product.id, %{name: "Edited"}) ==
               {:error, :record_not_found}
    end

    test "ordering a deleted product returns product_not_found", %{
      owner: owner,
      store: store,
      product: product,
      customer: customer
    } do
      ensure_address(customer)
      assert {:ok, _} = Products.delete(product.id, owner.id)

      assert Orders.create_order(%{
               store_id: store.id,
               customer_id: customer.id,
               products: [%{"product" => product.id, "quantity" => 1}],
               payment_method: "PIX"
             }) == {:error, :product_not_found}
    end

    test "deleted products still appear in order details", %{
      owner: owner,
      store: store,
      product: product,
      customer: customer
    } do
      ensure_address(customer)

      {:ok, order} =
        Orders.create_order(%{
          store_id: store.id,
          customer_id: customer.id,
          products: [%{"product" => product.id, "quantity" => 1}],
          payment_method: "PIX"
        })

      assert {:ok, _} = Products.delete(product.id, owner.id)

      assert {:ok, by_customer} =
               Orders.get_order_by_customer(%{order_id: order.id, customer_id: customer.id})

      assert Enum.any?(by_customer.products, &(&1.id == product.id))

      assert {:ok, by_store} =
               Orders.get_order_by_store(%{owner_id: owner.id, order_id: order.id})

      assert by_store.order_product
             |> Enum.map(& &1.product.id)
             |> Enum.member?(product.id)
    end
  end

  describe "Multi transactions" do
    test "create_order rolls back everything when stock is insufficient", %{
      store: store,
      product: product,
      customer: customer
    } do
      ensure_address(customer)

      orders_before = NextDoor.Repo.aggregate(NextDoor.Order, :count)
      order_products_before = NextDoor.Repo.aggregate(NextDoor.OrderProduct, :count)

      assert {:error, :insufficient_stock} =
               Orders.create_order(%{
                 store_id: store.id,
                 customer_id: customer.id,
                 products: [%{"product" => product.id, "quantity" => 1_000}],
                 payment_method: "PIX"
               })

      assert NextDoor.Repo.aggregate(NextDoor.Order, :count) == orders_before
      assert NextDoor.Repo.aggregate(NextDoor.OrderProduct, :count) == order_products_before
    end

    test "new_address links the address to the account", %{customer: customer} do
      {:ok, address} =
        Addresses.new_address(%{
          account_id: customer.id,
          address_number: "10",
          street: "Test St",
          neighborhood: "Center",
          cep: "12345678"
        })

      assert {:ok, addresses} = Addresses.list_addresses(%{account_id: customer.id})
      assert Enum.any?(addresses, &(&1.id == address.id))
    end
  end

  describe "Cache invalidation" do
    test "store update evicts the cached store entry", %{owner: owner} do
      assert {:ok, _} = Cache.get_by(Store, %{owner_id: owner.id})
      assert {:ok, _} = Stores.update(%{name: "Renamed Store"}, owner.id)
      assert Cachex.get(:nd_cache, {Store, %{owner_id: owner.id}}) == {:ok, nil}
    end

    test "store delete evicts the cached store entry" do
      owner = owner_fixture()
      _store = store_fixture(owner)

      assert {:ok, _} = Cache.get_by(Store, %{owner_id: owner.id})
      assert {:ok, _} = Stores.delete(owner.id)
      assert Cachex.get(:nd_cache, {Store, %{owner_id: owner.id}}) == {:ok, nil}
    end

    test "create_order evicts customer, owner, and store listing view caches", %{
      owner: owner,
      customer: customer,
      store: store,
      product: product
    } do
      ensure_address(customer)

      Cachex.put(:nd_cache, "view_cache:customer:#{customer.id}./api/orders", {200, "[]"})
      Cachex.put(:nd_cache, "view_cache:owner:#{owner.id}./api/orders", {200, "[]"})
      Cachex.put(:nd_cache, "view_cache:/api/stores/#{store.id}/product", {200, "[]"})

      {:ok, _order} =
        Orders.create_order(%{
          store_id: store.id,
          customer_id: customer.id,
          products: [%{"product" => product.id, "quantity" => 1}],
          payment_method: "PIX"
        })

      assert Cachex.get(:nd_cache, "view_cache:customer:#{customer.id}./api/orders") == {:ok, nil}
      assert Cachex.get(:nd_cache, "view_cache:owner:#{owner.id}./api/orders") == {:ok, nil}
      assert Cachex.get(:nd_cache, "view_cache:/api/stores/#{store.id}/product") == {:ok, nil}
    end

    test "update_status_order evicts owner and customer view caches", %{
      owner: owner,
      customer: customer,
      store: store
    } do
      order = order_fixture(customer, store)

      Cachex.put(:nd_cache, "view_cache:owner:#{owner.id}./api/orders", {200, "[]"})
      Cachex.put(:nd_cache, "view_cache:customer:#{customer.id}./api/orders", {200, "[]"})

      {:ok, _order} =
        Orders.update_status_order(order.id, owner.id, %{before: "ESPERANDO", after: "ACEITO"})

      assert Cachex.get(:nd_cache, "view_cache:owner:#{owner.id}./api/orders") == {:ok, nil}
      assert Cachex.get(:nd_cache, "view_cache:customer:#{customer.id}./api/orders") == {:ok, nil}
    end
  end

  defp ensure_address(customer) do
    case customer |> NextDoor.Repo.preload(:address) |> Map.get(:address) do
      [address | _] ->
        address

      [] ->
        address =
          NextDoor.Repo.insert!(%NextDoor.Address{
            address_number: "1",
            street: "S",
            neighborhood: "N",
            cep: "12345678"
          })

        {:ok, account_bin} = Ecto.UUID.dump(customer.id)
        {:ok, address_bin} = Ecto.UUID.dump(address.id)

        NextDoor.Repo.insert_all("account_address", [
          %{account_id: account_bin, address_id: address_bin}
        ])

        address
    end
  end

  defp order_fixture(customer, store) do
    address = ensure_address(customer)

    NextDoor.Repo.insert!(%NextDoor.Order{
      total: Decimal.new("30.00"),
      status_order: "ESPERANDO",
      payment_method: "PIX",
      account_id: customer.id,
      store_id: store.id,
      address_id: address.id
    })
  end
end
