defmodule NextDoorWeb.ProductJSON do
  def create(%{product: product}), do: %{product: product}
  def show(%{products: products}), do: %{products: products}
end
