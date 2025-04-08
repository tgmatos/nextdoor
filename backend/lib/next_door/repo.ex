defmodule NextDoor.Repo do
  use Ecto.Repo,
    otp_app: :next_door,
    adapter: Ecto.Adapters.Postgres
end
