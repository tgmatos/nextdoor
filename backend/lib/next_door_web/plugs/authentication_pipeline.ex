defmodule NextDoorWeb.AuthenticationPipeline do
  use Guardian.Plug.Pipeline,
    otp_app: :next_door,
    module: NextDoor.AccountManager,
    error_handler: NextDoorWeb.AuthErrorHandler

  plug(Guardian.Plug.VerifySession, claims: %{"typ" => "access"})
  plug(Guardian.Plug.VerifyHeader, claims: %{"typ" => "access"})
  plug(Guardian.Plug.EnsureAuthenticated)
  plug(Guardian.Plug.LoadResource, allow_blank: true)
end
