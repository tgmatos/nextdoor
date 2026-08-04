# NextDoor Backend — API Endpoints

Base URL (dev): `http://localhost:4000` — all endpoints under `/api`.

## Conventions

- Request and response bodies are JSON (`Content-Type: application/json`).
- `POST /api/account/register` and `POST /api/account/login` return a JWT `token`.
- Authenticated endpoints require the header `Authorization: Bearer <token>` (the JWT returned by login/register). Without it you get `401`:

```json
{"errors":{"detail":"Unauthorized"}}
```

- Error responses follow one of two shapes:
  - Changeset validation errors: `{"errors":{"<field>":["message", ...]}}`
  - API errors: `{"errors":{"detail":"message"}}`

---

## 1. Accounts & Auth

### POST /api/account/register
Creates an account **and** its first address. Returns a JWT token.

```bash
curl -X POST http://localhost:4000/api/account/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@nextdoor.com",
    "username": "demo_user",
    "password": "DemoPass1!",
    "address": {
      "number": "42",
      "street": "Rua Demo",
      "neighborhood": "Demo",
      "cep": "00000000"
    }
  }'
```

Response `200` (token truncated for readability):

```json
{"token":"eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJOZXh0RG9vciIs... (JWT)"}
```

`422` — invalid password (must be 6+ chars, lowercase, uppercase, and a digit/punctuation):

```json
{"errors":{"plain_password":["at least one digit or punctuation character","at least one upper case character","should be at least 6 character(s)"]}}
```

### POST /api/account/login
Authenticates and returns a JWT token.

```bash
curl -X POST http://localhost:4000/api/account/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@nextdoor.com","password":"DemoPass1!"}'
```

Response `200`:

```json
{"token":"eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJOZXh0RG9vciIs... (JWT)"}
```

`401` — wrong credentials:

```json
{"errors":{"detail":"Unauthorized"}}
```

### GET /api/account/logout
Terminates the session (no-op response).

```bash
curl -X GET http://localhost:4000/api/account/logout
```

Response `204 No Content`.

---

## 2. Stores — public

### GET /api/stores
Lists all stores.

```bash
curl http://localhost:4000/api/stores
```

Response `200`:

```json
{
  "stores": [
    {
      "id": "06f3a633-7037-4a1a-b602-a4d03e348c0c",
      "name": "Loja do Bairro",
      "description": "Roupas e acessórios do bairro",
      "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
      "category": "VESTUARIO",
      "telephone": "1133334444"
    }
  ]
}
```

### GET /api/stores/:id
Returns a single store.

```bash
curl http://localhost:4000/api/stores/06f3a633-7037-4a1a-b602-a4d03e348c0c
```

Response `200`:

```json
{
  "id": "06f3a633-7037-4a1a-b602-a4d03e348c0c",
  "name": "Loja do Bairro",
  "description": "Roupas e acessórios do bairro",
  "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
  "category": "VESTUARIO",
  "telephone": "1133334444"
}
```

`400` — invalid id:

```json
{"errors":{"detail":"Bad Request"}}
```

### GET /api/stores/:id/product
Lists the products of a store.

```bash
curl http://localhost:4000/api/stores/06f3a633-7037-4a1a-b602-a4d03e348c0c/product
```

Response `200`:

```json
{
  "products": [
    {
      "id": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8",
      "name": "Camiseta Básica",
      "description": "Camiseta 100% algodão",
      "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
      "quantity": 95,
      "price": 49.9,
      "inserted_at": "2026-08-03T20:33:36",
      "updated_at": "2026-08-03T20:33:36"
    }
  ]
}
```

---

## 3. Account (customer, authenticated)

All endpoints in this section use the customer token, e.g.:

```bash
TOKEN="<customer jwt>"
curl http://localhost:4000/api/account \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/account
Returns the authenticated account.

```bash
curl http://localhost:4000/api/account -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "account": {
    "id": "0ab6c0c3-2eb2-4336-a5f0-ce6f7f114602",
    "username": "customer_one",
    "email": "customer1@nextdoor.com"
  }
}
```

### PATCH /api/account
Updates the account email/username.

```bash
curl -X PATCH http://localhost:4000/api/account \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"account":{"email":"customer1@nextdoor.com","username":"customer_one"}}'
```

Response `200`:

```json
{"username":"customer_one","email":"customer1@nextdoor.com"}
```

### DELETE /api/account
Deletes the authenticated account.

```bash
curl -X DELETE http://localhost:4000/api/account -H "Authorization: Bearer $TOKEN"
```

Response `204 No Content`.

### GET /api/account/order
Lists the customer's orders (with the ordered products).

```bash
curl http://localhost:4000/api/account/order -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "orders": [
    {
      "id": "8609ad7d-3401-43bc-a7da-a2a60f3a7cbf",
      "total": "99.80",
      "inserted_at": "2026-08-03T20:33:51",
      "updated_at": "2026-08-03T20:33:51",
      "payment_method": "PIX",
      "status_order": "ESPERANDO",
      "order_product": [
        {
          "id": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8",
          "name": "Camiseta Básica",
          "description": "Camiseta 100% algodão",
          "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
          "price": 49.9,
          "inserted_at": "2026-08-03T20:33:36",
          "updated_at": "2026-08-03T20:33:36"
        }
      ]
    }
  ]
}
```

### GET /api/account/order/:id
Returns one of the customer's orders.

```bash
curl http://localhost:4000/api/account/order/8609ad7d-3401-43bc-a7da-a2a60f3a7cbf \
  -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "order": {
    "id": "8609ad7d-3401-43bc-a7da-a2a60f3a7cbf",
    "total": "99.80",
    "inserted_at": "2026-08-03T20:33:51",
    "updated_at": "2026-08-03T20:33:51",
    "payment_method": "PIX",
    "status_order": "ESPERANDO",
    "order_product": [
      {
        "id": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8",
        "name": "Camiseta Básica",
        "description": "Camiseta 100% algodão",
        "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
        "price": 49.9,
        "inserted_at": "2026-08-03T20:33:36",
        "updated_at": "2026-08-03T20:33:36"
      }
    ]
  }
}
```

### GET /api/account/address
Lists the customer's addresses.

```bash
curl http://localhost:4000/api/account/address -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
[
  {
    "id": "a589e659-a8be-4236-b79e-d0966918f057",
    "address_number": "10",
    "street": "Rua das Flores",
    "neighborhood": "Vila Nova",
    "cep": "04567010"
  }
]
```

### GET /api/account/address/:id
Returns one of the customer's addresses.

```bash
curl http://localhost:4000/api/account/address/a589e659-a8be-4236-b79e-d0966918f057 \
  -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "id": "a589e659-a8be-4236-b79e-d0966918f057",
  "address_number": "10",
  "street": "Rua das Flores",
  "neighborhood": "Vila Nova",
  "cep": "04567010"
}
```

### PATCH /api/account/address/:id
Updates one of the customer's addresses.

```bash
curl -X PATCH http://localhost:4000/api/account/address/a589e659-a8be-4236-b79e-d0966918f057 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"address":{"address_number":"11","street":"Rua das Flores","neighborhood":"Vila Nova","cep":"04567010"}}'
```

Response `200`:

```json
{
  "id": "a589e659-a8be-4236-b79e-d0966918f057",
  "address_number": "11",
  "street": "Rua das Flores",
  "neighborhood": "Vila Nova",
  "cep": "04567010"
}
```

---

## 4. Store owner (authenticated)

All endpoints in this section use the **store owner** token. The owner is resolved from the token, so the store is not passed in the body (except where `:id` is explicit).

### POST /api/store
Creates the owner's store. `image` is a base64 data URL and is required.

```bash
curl -X POST http://localhost:4000/api/store \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Demo Store",
    "description": "Descrição da loja demo",
    "telephone": "2199998888",
    "category": "PETS",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
  }'
```

`category` enum: `VESTUARIO`, `ELETRONICOS`, `COSMETICOS`, `PETS`, `LIVRARIA`.

Response `200`:

```json
{"id":"85459f27-e2cb-4937-96f4-9079d7287f6d"}
```

### GET /api/store
Returns the owner's store.

```bash
curl http://localhost:4000/api/store -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "id": "85459f27-e2cb-4937-96f4-9079d7287f6d",
  "name": "Demo Store",
  "description": "Descrição da loja demo",
  "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
  "category": "PETS",
  "telephone": "2199998888"
}
```

### PATCH /api/store
Updates the owner's store. `image` is optional (omit it to keep the current one).

```bash
curl -X PATCH http://localhost:4000/api/store \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"store":{"name":"Demo Store Renomeada","description":"Descrição atualizada","telephone":"2199998888","category":"PETS"}}'
```

Response `200`:

```json
{
  "id": "85459f27-e2cb-4937-96f4-9079d7287f6d",
  "name": "Demo Store Renomeada",
  "description": "Descrição atualizada",
  "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
  "category": "PETS",
  "telephone": "2199998888"
}
```

### DELETE /api/store
Deletes the owner's store.

```bash
curl -X DELETE http://localhost:4000/api/store -H "Authorization: Bearer $TOKEN"
```

Response `204 No Content`.

### POST /api/store/product
Creates a product for the owner's store (and its inventory). `image` is required; `quantity` sets the initial inventory.

```bash
curl -X POST http://localhost:4000/api/store/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "product": {
      "name": "Ração Premium",
      "description": "Ração 10kg",
      "price": "120.00",
      "quantity": 50,
      "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
    }
  }'
```

Response `200`:

```json
{
  "product": {
    "id": "9ffdf29e-b183-4cf2-8bf8-d49d1c1703df",
    "name": "Ração Premium",
    "description": "Ração 10kg",
    "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
    "quantity": 50,
    "price": 120.0,
    "inserted_at": "2026-08-03T21:25:39",
    "updated_at": "2026-08-03T21:25:39"
  }
}
```

### GET /api/store/product
Lists the owner's products.

```bash
curl http://localhost:4000/api/store/product -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "products": [
    {
      "id": "9ffdf29e-b183-4cf2-8bf8-d49d1c1703df",
      "name": "Ração Premium",
      "description": "Ração 10kg",
      "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
      "quantity": 50,
      "price": 120.0,
      "inserted_at": "2026-08-03T21:25:39",
      "updated_at": "2026-08-03T21:25:39"
    }
  ]
}
```

### PATCH /api/store/product/:id
Updates a product. `quantity` (inventory) and `image` are optional.

```bash
curl -X PATCH http://localhost:4000/api/store/product/9ffdf29e-b183-4cf2-8bf8-d49d1c1703df \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"product":{"name":"Ração Premium 10kg","description":"Ração premium 10kg","price":"125.50","quantity":40}}'
```

Response `200`:

```json
{
  "product": {
    "id": "9ffdf29e-b183-4cf2-8bf8-d49d1c1703df",
    "name": "Ração Premium 10kg",
    "description": "Ração premium 10kg",
    "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
    "quantity": 40,
    "price": 125.5,
    "inserted_at": "2026-08-03T21:25:39",
    "updated_at": "2026-08-03T21:25:43"
  }
}
```

### DELETE /api/store/product/:id
Deletes a product.

```bash
curl -X DELETE http://localhost:4000/api/store/product/9ffdf29e-b183-4cf2-8bf8-d49d1c1703df \
  -H "Authorization: Bearer $TOKEN"
```

Response `204 No Content`.

### GET /api/store/order
Lists the orders placed at the owner's store.

```bash
curl http://localhost:4000/api/store/order -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "orders": [
    {
      "id": "8609ad7d-3401-43bc-a7da-a2a60f3a7cbf",
      "status": "ESPERANDO",
      "total": "99.80",
      "payment_method": "PIX"
    },
    {
      "id": "50699f76-7e27-4803-804d-54469dd4bcb7",
      "status": "ESPERANDO",
      "total": "49.90",
      "payment_method": "CC"
    }
  ]
}
```

### GET /api/store/order/:id
Returns one order (with its products) placed at the owner's store.

```bash
curl http://localhost:4000/api/store/order/50699f76-7e27-4803-804d-54469dd4bcb7 \
  -H "Authorization: Bearer $TOKEN"
```

Response `200`:

```json
{
  "id": "50699f76-7e27-4803-804d-54469dd4bcb7",
  "total": "49.90",
  "inserted_at": "2026-08-03T20:33:51",
  "updated_at": "2026-08-03T20:33:51",
  "payment_method": "CC",
  "status_order": "ESPERANDO",
  "order_product": [
    {
      "id": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8",
      "name": "Camiseta Básica",
      "description": "Camiseta 100% algodão",
      "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
      "price": 49.9,
      "inserted_at": "2026-08-03T20:33:36",
      "updated_at": "2026-08-03T20:33:36"
    }
  ]
}
```

### POST /api/store/order/:id
Creates an order at the store `:id` for the authenticated customer. Body is a list of products (by UUID) with quantities, plus the payment method. Order total is computed server-side from the stored prices, and the inventory is decremented.

`payment_method` enum: `CC`, `CD`, `PIX`, `DINHEIRO`.

```bash
curl -X POST http://localhost:4000/api/store/order/06f3a633-7037-4a1a-b602-a4d03e348c0c \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "products": [
      {"product": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8", "quantity": 1}
    ],
    "payment_method": "DINHEIRO"
  }'
```

Response `200`:

```json
{
  "id": "ee835869-3043-436c-a44f-7f8c71e6e60e",
  "total": "49.90",
  "payment_method": "DINHEIRO",
  "status_order": "ESPERANDO",
  "order_product": [
    {
      "id": "3cba5f69-5fb5-46ab-b06e-a471d42e8ed8",
      "name": "Camiseta Básica",
      "description": "Camiseta 100% algodão",
      "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
      "price": 49.9,
      "inserted_at": "2026-08-03T20:33:36",
      "updated_at": "2026-08-03T20:33:36"
    }
  ]
}
```

Errors:
- `404` — store not found: `{"errors":{"detail":"Not Found"}}`
- `422` — unknown product: `{"errors":{"detail":"one or more products were not found"}}`
- `422` — insufficient stock: `{"errors":{"detail":"insufficient stock"}}`
- `422` — missing/invalid `payment_method` or malformed payload: `{"errors":{"detail":"invalid payload"}}`

### PATCH /api/store/order/:id
Updates an order status, validating the transition (`ESPERANDO -> ACEITO/RECUSADO`, `ACEITO -> PREPARACAO/CANCELADO`, `PREPARACAO -> ROTA/CANCELADO`, `ROTA -> CONCLUIDO/CANCELADO`).

```bash
curl -X PATCH http://localhost:4000/api/store/order/1b8e8134-85eb-4ecb-8412-68159b265b86 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"before":"ESPERANDO","after":"ACEITO"}'
```

Response `200`:

```json
{"id":"1b8e8134-85eb-4ecb-8412-68159b265b86","total":"249.50","payment_method":"PIX","status_order":"ACEITO"}
```

`422` — invalid transition:

```json
{"error":"Invalid status transition"}
```
