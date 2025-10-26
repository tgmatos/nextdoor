defmodule NextDoorWeb.StoreJSON do
  def create(%{store: %{id: id}}), do: %{id: id}
  def index(%{stores: stores}), do: %{stores: Enum.map(stores, &format_store/1)}
  def show(%{store: store}), do: format_store(store)
  def update(%{store: store}), do: %{store: store}
  def get_orders(%{orders: orders}), do: %{orders: orders}

   defp format_store(store) do
    %{id: id,
      name: name,
      description: description,
      telephone: telephone,
      category: category
    } = store

    %{id: id,
      name: name,
      description: description,
      telephone: telephone,
      category: category
    }
  end
end
