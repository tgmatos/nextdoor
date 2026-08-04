defmodule NextDoorWeb.CORS do
  use Corsica.Router,
    origins: ["http://localhost:3000", "http://127.0.0.1:3000"],
    allow_credentials: true,
    allow_methods: :all,
    allow_headers: :all,
    max_age: 7200

  resource("/*")
end
