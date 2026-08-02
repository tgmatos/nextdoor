defmodule NextDoor.SchemasTest do
  use ExUnit.Case, async: true

  import Ecto.Changeset
  alias NextDoor.{Account, Store, Product, Address, Order, Inventory}

  defp error_keys(changeset) do
    changeset.errors |> Enum.map(&elem(&1, 0)) |> Enum.uniq()
  end

  describe "Account" do
    test "requires email, username and password" do
      changeset = Account.new_account_changeset(%Account{}, %{})
      refute changeset.valid?

      keys = error_keys(changeset)
      assert :email in keys
      assert :username in keys
      assert :plain_password in keys
      assert :address in keys
    end

    test "is inspectable" do
      assert inspect(%Account{id: Ecto.UUID.generate(), email: "a@b.com"}) =~ "account"
    end

    test "rejects a short password" do
      changeset =
        Account.new_account_changeset(%Account{}, %{
          email: "u@example.com",
          username: "user",
          plain_password: "short"
        })

      refute changeset.valid?
    end

    test "updates email and username" do
      changeset = Account.update_changeset(%Account{}, %{email: "a@b.com", username: "new"})
      assert changeset.valid?
      assert get_change(changeset, :email) == "a@b.com"
    end
  end

  describe "Address" do
    test "requires all address fields" do
      changeset = Address.changeset(%Address{}, %{})
      refute changeset.valid?
      assert Enum.sort(error_keys(changeset)) == [:address_number, :cep, :neighborhood, :street]
    end

    test "accepts a valid address" do
      changeset =
        Address.changeset(%Address{}, %{
          address_number: "123",
          street: "Main",
          neighborhood: "Downtown",
          cep: "12345678"
        })

      assert changeset.valid?
    end

    test "update changeset casts fields without requiring them" do
      changeset = Address.update_changeset(%Address{}, %{street: "New"})
      assert changeset.valid?
      assert get_change(changeset, :street) == "New"
    end
  end

  describe "Store" do
    test "requires name, description, telephone, category and owner_id" do
      changeset = Store.new_store_changeset(%Store{}, %{})
      refute changeset.valid?

      assert Enum.sort(error_keys(changeset)) == [
               :category,
               :description,
               :name,
               :owner_id,
               :telephone
             ]
    end

    test "accepts a valid store" do
      changeset =
        Store.new_store_changeset(%Store{}, %{
          name: "Store",
          description: "desc",
          telephone: "11999",
          category: "VESTUARIO",
          owner_id: Ecto.UUID.generate()
        })

      assert changeset.valid?
    end
  end

  describe "Product" do
    test "requires name, description, price, image, store_id and inventory" do
      changeset = Product.new_product_changeset(%Product{}, %{})
      refute changeset.valid?

      keys = error_keys(changeset)
      assert :name in keys
      assert :description in keys
      assert :price in keys
      assert :image in keys
      assert :store_id in keys
      assert :inventory in keys
    end

    test "update changeset casts product fields" do
      changeset =
        Product.update_product_changeset(%Product{}, %{name: "New", price: Decimal.new("5.00")})

      assert changeset.valid?
      assert get_change(changeset, :name) == "New"
    end
  end

  describe "Order" do
    test "requires total, status_order, payment_method, account, store and address" do
      changeset = Order.changeset(%Order{}, %{})
      refute changeset.valid?

      assert Enum.sort(error_keys(changeset)) == [
               :account,
               :address,
               :payment_method,
               :status_order,
               :store,
               :total
             ]
    end

    test "update changeset requires a status order" do
      changeset = Order.update_changeset(%Order{}, %{})
      refute changeset.valid?
      assert error_keys(changeset) == [:status_order]
    end

    test "update changeset casts a new status order" do
      changeset = Order.update_changeset(%Order{}, %{status_order: "ACEITO"})
      assert changeset.valid?
    end
  end

  describe "Inventory" do
    test "requires a quantity" do
      changeset = Inventory.changeset(%Inventory{}, %{})
      refute changeset.valid?
      assert error_keys(changeset) == [:quantity]
    end

    test "accepts a valid quantity" do
      changeset = Inventory.changeset(%Inventory{}, %{quantity: 5})
      assert changeset.valid?
    end
  end
end
