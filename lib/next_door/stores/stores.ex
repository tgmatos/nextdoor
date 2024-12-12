defmodule NextDoor.Stores do
  alias NextDoor.{Store, Repo}
  def new_store(attr \\ %{}) do
	%Store{}
	|> Store.new_store_changeset(attr)
	|> Repo.insert
	|> case do
	  {:ok, store} -> {:ok, store}
	  {:error, result_errors} -> result_errors.errors |> Enum.map(fn {_, {error, _}} -> error end)
	end
  end
end
