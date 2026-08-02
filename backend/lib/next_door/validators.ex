defmodule NextDoor.Validators do
  def parse_uuid(uuid) when is_binary(uuid) do
    case Ecto.UUID.dump(uuid) do
      {:ok, binary} -> {:ok, binary}
      :error -> {:error, :invalid_uuid}
    end
  end

  def parse_uuid(_uuid), do: {:error, :invalid_uuid}
end
