defmodule NextDoorOrders do
  alias NextDoor.Order

  def create(attr) when not attr == %{} do
    %Order{}
  end
end
