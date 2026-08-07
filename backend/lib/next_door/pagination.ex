defmodule NextDoor.Pagination do
  import Ecto.Query

  def paginate(query, repo, opts \\ %{}) do
    page = get_opt(opts, :page, 1) |> max(1)
    per_page = get_opt(opts, :per_page, 20) |> max(1) |> min(100)

    total = repo.aggregate(query, :count) || 0

    entries =
      query
      |> limit(^per_page)
      |> offset(^(per_page * (page - 1)))
      |> repo.all()

    %{
      entries: entries,
      page: page,
      per_page: per_page,
      total: total,
      total_pages: if(total == 0, do: 0, else: ceil(total / per_page))
    }
  end

  defp get_opt(opts, key, default) do
    cond do
      opts[key] != nil ->
        to_int(opts[key], default)

      is_map(opts) and opts[to_string(key)] != nil ->
        to_int(opts[to_string(key)], default)

      true ->
        default
    end
  end

  defp to_int(int, _default) when is_integer(int), do: int

  defp to_int(str, default) when is_binary(str) do
    case Integer.parse(str) do
      {int, _rest} -> int
      :error -> default
    end
  end

  defp to_int(_other, default), do: default
end
