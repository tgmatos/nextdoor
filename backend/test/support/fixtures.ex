defmodule NextDoor.Fixtures do
  alias NextDoor.{Accounts, Account, Repo, Stores, Products, Order, OrderProduct}

  @password "KPc5@GrnA@2W@WSdoTKD9i%Up5G!wT!uKMvM9!*"

  def unique_email, do: "user_#{System.unique_integer([:positive])}@example.com"

  def valid_account_attrs do
    %{
      email: unique_email(),
      username: "user_#{System.unique_integer([:positive])}",
      password: @password,
      address: %{number: "123", street: "Main St", neighborhood: "Downtown", cep: "12345678"}
    }
  end

  def account_fixture do
    attrs = valid_account_attrs()

    {:ok, _token, _claims} =
      Accounts.new_account(%{
        email: attrs.email,
        username: attrs.username,
        plain_password: attrs.password,
        address: [
          %{
            address_number: attrs.address.number,
            street: attrs.address.street,
            neighborhood: attrs.address.neighborhood,
            cep: attrs.address.cep
          }
        ]
      })

    Repo.get_by!(Account, email: attrs.email)
  end

  def auth_conn(conn, account) do
    {:ok, token, _claims} = NextDoor.AccountManager.encode_and_sign(account)
    Plug.Conn.put_req_header(conn, "authorization", "Bearer #{token}")
  end

  def store_attrs do
    id = System.unique_integer([:positive])

    %{
      name: "Store #{id}",
      description: "Store Test Description",
      telephone: "11#{id}",
      category: "VESTUARIO"
    }
  end

  def store_fixture(account) do
    attrs = store_attrs()

    {:ok, store} =
      Stores.create(%{
        name: attrs.name,
        description: attrs.description,
        telephone: attrs.telephone,
        category: attrs.category,
        image: "fake image binary",
        owner_id: account.id
      })

    store
  end

  def product_attrs do
    %{
      name: "Product #{System.unique_integer([:positive])}",
      description: "Product Test Description",
      price: "20.00",
      quantity: 300
    }
  end

  def product_fixture(store, attrs \\ %{}) do
    defaults = product_attrs()
    attrs = Map.merge(defaults, attrs)

    {:ok, product} =
      Products.create(store.owner_id, %{
        name: attrs.name,
        description: attrs.description,
        price: attrs.price,
        image: "fake product image",
        inventory: %{quantity: attrs.quantity}
      })

    product
  end

  def order_fixture(customer, store, product, attrs \\ %{}) do
    address = customer |> Repo.preload(:address) |> Map.get(:address) |> List.first()

    order =
      Repo.insert!(%Order{
        total: Map.get(attrs, :total, Decimal.new("30.00")),
        status_order: Map.get(attrs, :status_order, "ESPERANDO"),
        payment_method: Map.get(attrs, :payment_method, "PIX"),
        account_id: customer.id,
        store_id: store.id,
        address_id: address.id
      })

    Repo.insert!(%OrderProduct{order_id: order.id, product_id: product.id, quantity: 2})
    order
  end

  def image_base64 do
    "data:image/png;base64," <> Base.encode64("fake image binary data")
  end

  def invalid_base64, do: "not!!valid??base64"

  def invalid_uuid, do: "not-a-valid-uuid"

  def flush_cache, do: NextDoor.Cache.flush()
end
