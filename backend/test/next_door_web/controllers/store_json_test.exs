defmodule NextDoorWeb.StoreJSONTest do
  use ExUnit.Case, async: true

  alias NextDoor.Store

  defp store do
    %Store{
      id: "store-id",
      name: "Store",
      description: "description",
      telephone: "11223344556",
      category: "VESTUARIO",
      image: "fake image binary"
    }
  end

  defp formatted_store do
    %{
      id: "store-id",
      name: "Store",
      description: "description",
      telephone: "11223344556",
      category: "VESTUARIO",
      image: Base.encode64("fake image binary")
    }
  end

  test "create returns the store id" do
    assert NextDoorWeb.StoreJSON.create(%{store: store()}) == %{id: "store-id"}
  end

  test "show formats the store with the image encoded" do
    assert NextDoorWeb.StoreJSON.show(%{store: store()}) == formatted_store()
  end

  test "update formats the store with the image encoded" do
    assert NextDoorWeb.StoreJSON.update(%{store: store()}) == formatted_store()
  end

  test "index maps every store" do
    assert NextDoorWeb.StoreJSON.index(%{stores: [store(), store()]}) ==
             %{stores: [formatted_store(), formatted_store()]}
  end

  test "index handles an empty list" do
    assert NextDoorWeb.StoreJSON.index(%{stores: []}) == %{stores: []}
  end

  test "show encodes a nil image as an empty string" do
    store = %Store{store() | image: nil}
    assert NextDoorWeb.StoreJSON.show(%{store: store}).image == ""
  end
end
