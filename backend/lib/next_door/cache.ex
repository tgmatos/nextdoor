defmodule NextDoor.Cache do
  alias NextDoor.Repo
  @cache :nd_cache
  
  def get(table, id) when is_binary(id) do
	case Cachex.get(@cache, id) do
	  {:ok, nil} -> get_from_repo(table, id)
	  {:ok, record} -> {:ok, record}
	end
  end

  def get(table, clauses) when is_map(clauses) do
	case Cachex.get(@cache, {table, clauses}) do
	  {:ok, nil} -> get_from_repo(table, clauses)
	  {:ok, record} -> {:ok, record}
	end
  end
  
  def get_all(table) do
	case Cachex.get(@cache, table) do
	  {:ok, nil} -> get_from_repo(table)
	  {:ok, records} -> {:ok, records}
	end
  end

  def get_all_by(table, clause) do
	case Cachex.get(@cache, {table, clause}) do
	  {:ok, nil} -> get_from_repo(table, clause)
	  {:ok, records} -> {:ok, records}
	end
  end
  
  defp get_from_repo(table, id) when is_binary(id) do
	case Repo.get_by(table, id: id) do
	  nil -> {:error, :record_not_found}
	  record ->
		Cachex.put(@cache, id, record)
		{:ok, record}
	end
  end

  defp get_from_repo(table, clauses) when is_atom(clauses) or is_map(clauses) do
	case Repo.get_by(table, clauses) do
	  nil -> {:error, :record_not_found}
	  record ->
		Cachex.put(@cache, {table, clauses}, record)
		{:ok, record}
	end
  end

  defp get_from_repo(table) do
	case Repo.all(table) do
	  [] -> {:ok, []}
	  records ->
		Cachex.put(@cache, table, records)
		{:ok, records}
	end
  end
  
end
