defmodule NextDoorWeb.StoreJSON do
  def create(%{store: %{id: id}}), do: %{id: id}
  def index(%{stores: stores}), do: %{stores: stores}
  def show(%{store: id}), do: %{store: id}
  def update(%{store: store}), do: %{store: store}
end
