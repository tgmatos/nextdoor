defmodule NextDoor.ValidatorsTest do
  use ExUnit.Case, async: true

  alias NextDoor.Validators

  test "parse_uuid accepts a valid uuid" do
    assert {:ok, binary} = Validators.parse_uuid(Ecto.UUID.generate())
    assert byte_size(binary) == 16
  end

  test "parse_uuid rejects a malformed uuid" do
    assert Validators.parse_uuid("not-a-uuid") == {:error, :invalid_uuid}
  end

  test "parse_uuid rejects non-string values" do
    assert Validators.parse_uuid(nil) == {:error, :invalid_uuid}
    assert Validators.parse_uuid(123) == {:error, :invalid_uuid}
  end
end
