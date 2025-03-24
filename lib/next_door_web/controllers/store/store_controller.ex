defmodule NextDoorWeb.StoreController do
  use NextDoorWeb, :controller
  alias NextDoor.Stores

  action_fallback NextDoorWeb.FallbackController

  def create(conn, %{"name" => name, "description" => description, "address" => address, "telephone" => telephone, "category" => category}) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, store} <- Stores.create(%{name: name, description: description, address: address, telephone: telephone, category: category, owner_id: owner_id}) do
	  render(conn, :create, %{store: store})
	end
  end

  def index(conn, _params) do
	with {:ok, stores} <- Stores.index do
	  render(conn, :index, %{stores: stores})
	end
  end

  def show(conn, %{"id" => id}) do
	with {:ok, id} <- Stores.show(id) do
	  render(conn, :show, %{store: id})
	end
  end

  def show(conn, _params) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, store} <- Stores.show(%{owner_id: owner_id}) do
	  render(conn, :show, %{store: store})
	end
  end

  def update(conn, %{"store" => store}) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, _} <- Stores.update(store, owner_id) do
	  conn
	  |> put_status(:ok)
	  |> send_resp(:ok, "")
	end
  end

  def delete(conn, _params) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, _} <- Stores.delete(owner_id) do
	  conn
	  |> put_status(:ok)
	  |> send_resp(:ok, "")
	end
  end
end

# def update_user(user, params) do
#   import Ecto.Changeset

#   user
#   |> change_user(params)
#   |> validate_required([:username])
#   |> Repo.update()
# end

# def change_user(user, params \\ %{}) do
#   import Ecto.Changeset
#   user
#   |> cast(params, [
#     :email,
#     :name,
#     :username,
#     :theme_name,
#     :theme_prefer_original,
#     :terminal_font_family,
#     :default_recording_visibility,
#     :default_stream_visibility,
#     :stream_recording_enabled
#   ])
#   |> validate_required([:email])
#   |> update_change(:email, &String.downcase/1)
#   |> validate_format(:email, @valid_email_re)
#   |> validate_username()
#   |> validate_inclusion(:theme_name, Themes.terminal_themes())
#   |> validate_inclusion(:terminal_font_family, Fonts.terminal_font_families())
#   |> add_contraints()
# end
