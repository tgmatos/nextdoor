defmodule NextDoorWeb.AccountControllerTest do
  use NextDoorWeb.ConnCase

  alias NextDoor.Repo
  alias NextDoor.Account

  setup %{conn: conn} do
    flush_cache()
    {:ok, conn: conn}
  end

  describe "register" do
    test "registers an account and returns a token", %{conn: conn} do
      attrs = valid_account_attrs()

      conn = post(conn, ~p"/api/account/register", attrs)

      assert %{"token" => token} = json_response(conn, 200)
      assert is_binary(token)

      account = Repo.get_by!(Account, email: attrs.email)
      assert account.username == attrs.username
      assert length(Repo.preload(account, :address).address) == 1
    end

    test "returns 422 when required fields are missing", %{conn: conn} do
      conn = post(conn, ~p"/api/account/register", %{email: unique_email()})

      assert %{"errors" => errors} = json_response(conn, 422)
      assert errors["username"]
      assert errors["password"] || errors["plain_password"]
      assert errors["address"]
    end

    test "returns 422 when the password is invalid", %{conn: conn} do
      attrs = %{valid_account_attrs() | password: "short"}

      conn = post(conn, ~p"/api/account/register", attrs)

      assert %{"errors" => %{"plain_password" => errors}} = json_response(conn, 422)
      assert is_list(errors)
    end

    test "returns 422 when the email is already registered", %{conn: conn} do
      attrs = valid_account_attrs()

      post(conn, ~p"/api/account/register", attrs)
      conn = post(conn, ~p"/api/account/register", attrs)

      assert %{"errors" => %{"email" => errors}} = json_response(conn, 422)
      assert is_list(errors)
    end
  end

  describe "login" do
    test "logs in with valid credentials and returns a token", %{conn: conn} do
      attrs = valid_account_attrs()
      post(conn, ~p"/api/account/register", attrs)

      conn =
        post(conn, ~p"/api/account/login", %{
          email: attrs.email,
          password: attrs.password
        })

      assert %{"token" => token} = json_response(conn, 200)
      assert is_binary(token)
    end

    test "returns 401 with an invalid password", %{conn: conn} do
      attrs = valid_account_attrs()
      post(conn, ~p"/api/account/register", attrs)

      conn =
        post(conn, ~p"/api/account/login", %{
          email: attrs.email,
          password: "WrongPassword1!"
        })

      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 401 when the email does not exist", %{conn: conn} do
      conn =
        post(conn, ~p"/api/account/login", %{
          email: unique_email(),
          password: "SomePassword1!"
        })

      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 401 when the password is missing", %{conn: conn} do
      conn = post(conn, ~p"/api/account/login", %{email: unique_email()})

      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end
  end

  describe "logout" do
    test "returns 204 no content", %{conn: conn} do
      conn = get(conn, ~p"/api/account/logout")
      assert response(conn, 204) == ""
    end
  end

  describe "show" do
    test "returns 401 without a token", %{conn: conn} do
      conn = get(conn, ~p"/api/account")
      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 401 with an invalid token", %{conn: conn} do
      conn = put_req_header(conn, "authorization", "Bearer invalid-token")
      conn = get(conn, ~p"/api/account")
      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns 401 with an expired token", %{conn: conn} do
      account = account_fixture()

      {:ok, token, _claims} =
        NextDoor.AccountManager.encode_and_sign(account, %{}, ttl: {-5, :seconds})

      conn = put_req_header(conn, "authorization", "Bearer #{token}")
      conn = get(conn, ~p"/api/account")
      assert json_response(conn, 401)["errors"]["detail"] == "Unauthorized"
    end

    test "returns the authenticated account", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = get(conn, ~p"/api/account")

      assert json_response(conn, 200) == %{
               "account" => %{
                 "id" => account.id,
                 "email" => account.email,
                 "username" => account.username
               }
             }
    end
  end

  describe "update" do
    test "updates the account email and username", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)

      conn =
        patch(conn, ~p"/api/account", %{
          account: %{email: "new_email@example.com", username: "new_username"}
        })

      assert json_response(conn, 200) == %{
               "email" => "new_email@example.com",
               "username" => "new_username"
             }

      assert Repo.get!(Account, account.id).email == "new_email@example.com"
    end

    test "returns 422 when the account param is missing", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = patch(conn, ~p"/api/account", %{})

      assert json_response(conn, 422)["errors"]["detail"] == "missing required parameters"
    end
  end

  describe "delete" do
    test "deletes the authenticated account", %{conn: conn} do
      account = account_fixture()
      conn = auth_conn(conn, account)
      conn = delete(conn, ~p"/api/account")

      assert response(conn, 204) == ""
      assert Repo.get(Account, account.id) == nil
    end
  end
end
