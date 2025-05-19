defmodule NextDoor.Cache do
  alias NextDoor.Repo
  @cache :nd_cache
  
  def get_by_id(table, id) do
	case Cachex.get(@cache, id) do
	  {:ok, nil} -> get_from_repo_by_id(table, id)
	  {:ok, record} -> {:ok, record}
	end
  end

  def get_by(table, clauses) do
	case Cachex.get(@cache, {table, clauses}) do
	  {:ok, nil} -> get_from_repo_by(table, clauses)
	  {:ok, record} -> {:ok, record}
	end
  end

  def get_from_query(query) do
    case Cachex.get(@cache, query) do
      {:ok, nil} -> get_all_from_repo_by(query)
        
      {:ok, record} -> {:ok, record}
    end
  end
  
  def get_all(table) do
	case Cachex.get(@cache, table) do
	  {:ok, nil} -> get_from_repo(table)
	  {:ok, records} -> {:ok, records}
	end
  end

  defp get_all_from_repo_by(query) do
    case Repo.all(query) do
      [] -> {:ok, []}
      list ->
        Cachex.put(@cache, query, list)
        {:ok, list}
    end
  end

  defp get_from_repo_by_id(table, id) do
    case Repo.get_by(table, id: id) do
      nil -> {:error, :record_not_found}
      record ->
	Cachex.put(@cache, id, record)
	{:ok, record}
    end
  end

  defp get_from_repo_by(table, clauses) do
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
