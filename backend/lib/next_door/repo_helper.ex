defmodule NextDoor.RepoHelper do
  alias NextDoor.Repo

  def transact(multi, step) do
    case Repo.transaction(multi) do
      {:ok, changes} -> {:ok, Map.fetch!(changes, step)}
      {:error, _step, reason, _changes} -> {:error, reason}
    end
  end
end
