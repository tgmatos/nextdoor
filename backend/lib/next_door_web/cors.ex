defmodule NextDoorWeb.CORS do
  use Corsica.Router,
    origins: ["http://localhost:5173"],
    allow_credentials: true,
    allow_methods: :all,
    allow_headers: :all,
    max_age: 7200

  resource("/api/account/*",
           origins: ["http://localhost:5173"],
           allow_methods: :all,
           allow_headers: :all)
  
  resource("/api/store/*",
    origins: ["http://localhost:5173"],
    allow_methods: :all,
    allow_headers: :all
  )
end
