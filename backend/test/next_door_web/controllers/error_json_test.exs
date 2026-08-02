defmodule NextDoorWeb.ErrorJSONTest do
  use ExUnit.Case, async: true

  test "renders 401" do
    assert NextDoorWeb.ErrorJSON.render("401.json", %{}) == %{errors: %{detail: "Unauthorized"}}
  end

  test "renders 404" do
    assert NextDoorWeb.ErrorJSON.render("404.json", %{}) == %{errors: %{detail: "Not Found"}}
  end

  test "renders 500" do
    assert NextDoorWeb.ErrorJSON.render("500.json", %{}) ==
             %{errors: %{detail: "Internal Server Error"}}
  end

  test "renders an unknown status with its status message" do
    assert NextDoorWeb.ErrorJSON.render("418.json", %{}) == %{errors: %{detail: "I'm a teapot"}}
  end
end
