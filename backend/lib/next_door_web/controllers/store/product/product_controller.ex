defmodule NextDoorWeb.ProductController do
  use NextDoorWeb, :controller
  alias NextDoor.Products
  
  def create(conn, %{"product" => product}) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, product} <- Products.create(owner_id, product) do
	  render(conn, :create, %{product: product})
	end
  end

  def update(conn, %{"id" => product_id, "product" => product}) do
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, _} <- Products.update(product_id, owner_id, product) do
	  conn
	  |> put_status(:ok)
	  |> send_resp(:ok, "")
	end
  end

  def index(conn, _params) do
    %{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
    with {:ok, products} <- Products.index(owner_id) do
      render(conn, :show, %{products: products})
    end
  end

  def delete(conn, %{"id" => product_id}) do	
	%{"sub" => owner_id} = Guardian.Plug.current_claims(conn)
	with {:ok, _} <- Products.delete(product_id, owner_id) do
	  conn
	  |> put_status(:ok)
	  |> send_resp(:ok, "")
	end
  end
end
