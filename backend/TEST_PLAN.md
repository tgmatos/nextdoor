# Controller Test Implementation Plan

## Scope

Improve the controller test suite and fix the application defects exposed by meaningful coverage.

The tests use the existing PostgreSQL test database. They do not create, migrate, drop, truncate, or recreate the database.

## Test Environment

Load the existing environment before running tests:

```bash
source env.sh
mix test
```

The database schema is managed externally through SQL. No Ecto migrations will be added or used.

Remove database lifecycle commands from Mix aliases so the test command does not run `ecto.create`, `ecto.migrate`, or destructive `ecto.reset` operations.

Use PostgreSQL SQL Sandbox transactions for test isolation. Test data must use unique account emails and other unique values so tests remain safe against the shared test database.

## Existing Test Repairs

Update the tests under `test/next_door_web/controllers`:

- Add the required nested address to account registration and account fixtures.
- Add required Base64 image data to store requests.
- Replace the product `stock` field with `quantity` and include the required image.
- Rename the misspelled `PoductJSONTest` module.
- Capture every response returned by `post`, `put`, `patch`, `get`, and `delete`.
- Assert exact status codes, response bodies, JSON shapes, and relevant database state.
- Replace assertions that only verify a truthy response body with explicit status and body assertions.
- Avoid cache leakage by clearing cache state or running cache-sensitive tests synchronously.

## Test Support

Add reusable helpers under `test/support` for:

- Creating unique accounts with valid addresses.
- Creating authenticated connections with valid Guardian tokens.
- Creating stores, products, inventory records, and orders.
- Generating valid and invalid Base64 image payloads.
- Creating records owned by separate accounts for isolation tests.
- Clearing and inspecting the `:nd_cache` Cachex instance.

## Account and Authentication Coverage

Add controller tests for:

- Successful registration and token response shape.
- Missing or invalid registration fields.
- Invalid password formats and duplicate accounts.
- Successful login.
- Invalid email and password combinations.
- Unauthorized requests without a token.
- Invalid and expired tokens.
- Account show, update, and delete behavior.
- Logout behavior, or removal of the route if logout is not supported.

## Address Coverage

Add tests for:

- Listing addresses for the authenticated account.
- Fetching an address owned by the account.
- Updating an owned address.
- Empty address lists.
- Missing addresses.
- Malformed UUIDs.
- Attempts to read or update another account's address.
- Validation failures.

## Store Coverage

Expand tests for:

- Creating a store with valid image data.
- Missing fields and invalid Base64 images.
- Listing stores, including an empty result.
- Fetching a store by ID.
- Fetching a store that does not exist.
- Fetching and updating the authenticated owner's store.
- Updating without an image.
- Deleting the authenticated owner's store.
- Missing stores and owner isolation.
- Exact image encoding and response fields.

## Product Coverage

Add tests for:

- Creating a product with quantity and image data.
- Required-field and invalid-image failures.
- Listing products by store.
- Listing products owned by the authenticated store owner.
- Empty product lists.
- Updating product details, quantity, and image.
- Invalid Base64 image updates.
- Deleting products.
- Missing products and owner isolation.
- Decimal price, quantity, timestamps, and Base64 image serialization.

## Order Coverage

Add fixture-based tests for the existing order endpoints:

- Listing orders for a store owner.
- Fetching a store owner's order and its products.
- Listing orders for a customer.
- Fetching a customer's order.
- Valid order status transitions.
- Invalid transitions.
- Incorrect current status values.
- Missing orders.
- Malformed order IDs.
- Store-owner and customer ownership isolation.

There is currently no order creation endpoint. Order fixtures should be inserted directly through test helpers until an order creation API is implemented separately.

## JSON and Error Coverage

Add focused serializer tests for:

- `AccountJSON` registration and login responses.
- `StoreJSON` create, list, show, and update responses.
- `ProductJSON` product and inventory formatting.
- `OrderJSON` order and product formatting.
- Decimal conversion and image encoding.
- `ErrorJSON` responses for 401, 404, 500, and unknown status templates.

## Production Fixes Required by Tests

Fix defects revealed by the new tests:

- Implement or remove undefined account and logout routes.
- Add consistent fallback handling to address, product, and order controllers.
- Return proper 4xx responses for missing records instead of `200` responses containing `nil`.
- Handle malformed UUIDs without crashing the request process.
- Return validation and invalid Base64 errors through the standard JSON error path.
- Remove debug `IO.inspect` calls from request paths and serializers.
- Replace the hardcoded cache key in `CachePlug` with request and authentication-aware keying, or remove the unused plug.
- Add complete cache invalidation for store, product, and order mutations.

## Verification

Run the following after implementation:

```bash
source env.sh
mix format
mix test
mix test --cover
```

Verify that tests use only the existing test database and do not execute database creation, migration, drop, truncate, or reset operations.
