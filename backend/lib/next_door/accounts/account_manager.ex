defmodule NextDoor.AccountManager do
  use Guardian, otp_app: :next_door,
				permissions: %{
				  default: [:user]
				}
    
  def subject_for_token(%{id: id}, _claims) do
	{:ok, to_string(id)}
  end

  def subject_for_token(_, _) do
	{:error, :reason}
  end
  
  def resource_from_claims(_claims) do
    {:error, :reason_for_error}
  end
end
