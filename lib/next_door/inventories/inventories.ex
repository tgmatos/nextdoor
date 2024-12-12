defmodule NextDoor.Inventories do
  alias NextDoor.Inventory
  def create_inventory(attr \\ %{}) do
	%Inventory{}
	|> Inventory.new_inventory_changeset(attr)
	|> case do
	  {:ok, inventory} -> {:ok, inventory}
	  {:error, result_errors} -> result_errors.errors |> Enum.map(fn {_, {error, _}} -> error end)
	end
  end
end
