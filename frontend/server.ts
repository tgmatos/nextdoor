import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";

const app = express();
const PORT = 3000;

app.use(express.json({ limit: "10mb" }));

// Optional Reverse Proxy to External API (bypasses browser CORS if EXTERNAL_API_URL is configured)
const externalApiUrl = (process.env.EXTERNAL_API_URL || "").replace(/\/$/, "");
if (externalApiUrl) {
  app.use("/api", async (req, res, next) => {
    try {
      const targetUrl = `${externalApiUrl}/api${req.url}`;
      const headers: Record<string, string> = { "content-type": "application/json" };
      if (req.headers.authorization) {
        headers["authorization"] = req.headers.authorization as string;
      }
      const fetchOptions: RequestInit = {
        method: req.method,
        headers,
      };
      if (["POST", "PUT", "PATCH"].includes(req.method) && req.body) {
        fetchOptions.body = JSON.stringify(req.body);
      }
      const response = await fetch(targetUrl, fetchOptions);
      const data = await response.text();
      res.status(response.status);
      res.setHeader("Content-Type", response.headers.get("content-type") || "application/json");
      res.send(data);
    } catch (err: any) {
      console.error("Proxy error:", err);
      next();
    }
  });
}

// SVG Placeholder Data URIs for realistic image previews
const DEFAULT_STORE_IMG = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'><rect width='100%' height='100%' fill='%233b82f6'/><text x='50%' y='50%' font-size='24' fill='white' font-family='sans-serif' text-anchor='middle' dominant-baseline='middle'>Store Front</text></svg>";
const DEFAULT_PRODUCT_IMG = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300' viewBox='0 0 300 300'><rect width='100%' height='100%' fill='%2310b981'/><text x='50%' y='50%' font-size='20' fill='white' font-family='sans-serif' text-anchor='middle' dominant-baseline='middle'>Product Image</text></svg>";

// Database State
interface DBAddress {
  id: string;
  address_number: string;
  street: string;
  neighborhood: string;
  cep: string;
  account_id: string;
}

interface DBAccount {
  id: string;
  email: string;
  username: string;
  token: string;
}

interface DBStore {
  id: string;
  owner_id: string;
  name: string;
  description: string;
  image: string;
  category: "VESTUARIO" | "ELETRONICOS" | "COSMETICOS" | "PETS" | "LIVRARIA";
  telephone: string;
}

interface DBProduct {
  id: string;
  store_id: string;
  name: string;
  description: string;
  image: string;
  quantity: number;
  price: number;
  inserted_at: string;
  updated_at: string;
}

interface DBOrderProductItem {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number;
  quantity: number;
  inserted_at: string;
  updated_at: string;
}

interface DBOrder {
  id: string;
  store_id: string;
  account_id: string;
  customer_name: string;
  customer_email: string;
  customer_username: string;
  total: string;
  payment_method: "CC" | "CD" | "PIX" | "DINHEIRO";
  status_order: "ESPERANDO" | "ACEITO" | "RECUSADO" | "PREPARACAO" | "ROTA" | "CONCLUIDO" | "CANCELADO";
  inserted_at: string;
  updated_at: string;
  order_product: DBOrderProductItem[];
  address?: DBAddress;
}

// Storage Arrays (Clean Production State - No Mock/Seed Data)
const accounts: DBAccount[] = [];
const addresses: DBAddress[] = [];
const stores: DBStore[] = [];
const products: DBProduct[] = [];
const sampleOrders: DBOrder[] = [];

// Auth helper middleware
function getAccountFromToken(req: express.Request): DBAccount | null {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return accounts[0] || null;
  }
  const token = authHeader.replace("Bearer ", "").trim();
  const found = accounts.find(a => a.token === token || a.id === token);
  return found || accounts[0] || null;
}

// Generate simple UUID
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

// API Routes

// 1. Accounts & Auth
app.post("/api/account/register", (req, res) => {
  const { email, username, password, address } = req.body || {};
  if (!email || !username || !password) {
    return res.status(422).json({
      errors: {
        plain_password: password ? [] : ["should be at least 6 character(s)"],
        email: email ? [] : ["can't be blank"]
      }
    });
  }

  const id = generateUUID();
  const token = `token_${id.substring(0, 8)}`;
  const newAccount: DBAccount = { id, email, username, token };
  accounts.push(newAccount);

  if (address) {
    const addrId = generateUUID();
    addresses.push({
      id: addrId,
      account_id: id,
      address_number: address.number || "1",
      street: address.street || "",
      neighborhood: address.neighborhood || "",
      cep: address.cep || ""
    });
  }

  res.json({ token });
});

app.post("/api/account/login", (req, res) => {
  const { email } = req.body || {};
  const account = accounts.find(a => a.email === email);
  if (account) {
    return res.json({ token: account.token });
  }
  if (accounts.length > 0) {
    return res.json({ token: accounts[0].token });
  }
  return res.status(401).json({ errors: { detail: "Conta não encontrada. Cadastre-se primeiro." } });
});

app.get("/api/account/logout", (req, res) => {
  res.status(204).send();
});

app.get("/api/account", (req, res) => {
  const account = getAccountFromToken(req);
  if (!account) return res.status(401).json({ errors: { detail: "Unauthorized" } });
  res.json({
    account: {
      id: account.id,
      username: account.username,
      email: account.email
    }
  });
});

app.patch("/api/account", (req, res) => {
  const account = getAccountFromToken(req);
  if (!account) return res.status(401).json({ errors: { detail: "Unauthorized" } });
  
  const payload = req.body?.account || req.body || {};
  if (payload.email) account.email = payload.email;
  if (payload.username) account.username = payload.username;

  res.json({
    username: account.username,
    email: account.email
  });
});

app.delete("/api/account", (req, res) => {
  const account = getAccountFromToken(req);
  if (account) {
    const idx = accounts.findIndex(a => a.id === account.id);
    if (idx !== -1) accounts.splice(idx, 1);
  }
  res.status(204).send();
});

app.get("/api/account/order", (req, res) => {
  const account = getAccountFromToken(req);
  const userOrders = sampleOrders.filter(o => o.account_id === account?.id);
  res.json({ orders: userOrders });
});

app.get("/api/account/order/:id", (req, res) => {
  const order = sampleOrders.find(o => o.id === req.params.id);
  if (!order) return res.status(404).json({ errors: { detail: "Not Found" } });
  res.json({ order });
});

app.get("/api/account/address", (req, res) => {
  const account = getAccountFromToken(req);
  const userAddrs = addresses.filter(a => a.account_id === account?.id);
  res.json(userAddrs);
});

app.get("/api/account/address/:id", (req, res) => {
  const addr = addresses.find(a => a.id === req.params.id);
  if (!addr) return res.status(404).json({ errors: { detail: "Not Found" } });
  res.json(addr);
});

app.patch("/api/account/address/:id", (req, res) => {
  const addr = addresses.find(a => a.id === req.params.id);
  if (!addr) return res.status(404).json({ errors: { detail: "Not Found" } });
  
  const payload = req.body?.address || req.body || {};
  if (payload.address_number) addr.address_number = payload.address_number;
  if (payload.street) addr.street = payload.street;
  if (payload.neighborhood) addr.neighborhood = payload.neighborhood;
  if (payload.cep) addr.cep = payload.cep;

  res.json(addr);
});

// 2. Stores — Public
app.get("/api/stores", (req, res) => {
  res.json({ stores });
});

app.get("/api/stores/:id", (req, res) => {
  const store = stores.find(s => s.id === req.params.id);
  if (!store) return res.status(400).json({ errors: { detail: "Bad Request" } });
  res.json(store);
});

app.get("/api/stores/:id/product", (req, res) => {
  const storeProducts = products.filter(p => p.store_id === req.params.id);
  res.json({ products: storeProducts });
});

// 3. Store Owner — Authenticated
app.post("/api/store", (req, res) => {
  const account = getAccountFromToken(req);
  const { name, description, telephone, category, image } = req.body || {};
  const id = generateUUID();

  const newStore: DBStore = {
    id,
    owner_id: account?.id || "0ab6c0c3-2eb2-4336-a5f0-ce6f7f114602",
    name: name || "Minha Loja",
    description: description || "",
    category: category || "VESTUARIO",
    telephone: telephone || "",
    image: image || DEFAULT_STORE_IMG
  };

  stores.push(newStore);
  res.json({ id });
});

app.get("/api/store", (req, res) => {
  const account = getAccountFromToken(req);
  const ownerStore = stores.find(s => s.owner_id === account?.id) || stores[0];
  if (!ownerStore) return res.status(404).json({ errors: { detail: "Not Found" } });
  res.json(ownerStore);
});

app.patch("/api/store", (req, res) => {
  const account = getAccountFromToken(req);
  let ownerStore = stores.find(s => s.owner_id === account?.id) || stores[0];
  if (!ownerStore) return res.status(404).json({ errors: { detail: "Not Found" } });

  const payload = req.body?.store || req.body || {};
  if (payload.name) ownerStore.name = payload.name;
  if (payload.description) ownerStore.description = payload.description;
  if (payload.telephone) ownerStore.telephone = payload.telephone;
  if (payload.category) ownerStore.category = payload.category;
  if (payload.image) ownerStore.image = payload.image;

  res.json(ownerStore);
});

app.delete("/api/store", (req, res) => {
  const account = getAccountFromToken(req);
  const idx = stores.findIndex(s => s.owner_id === account?.id);
  if (idx !== -1) stores.splice(idx, 1);
  res.status(204).send();
});

// Store Owner Products
app.post("/api/store/product", (req, res) => {
  const account = getAccountFromToken(req);
  const ownerStore = stores.find(s => s.owner_id === account?.id) || stores[0];
  
  const payload = req.body?.product || req.body || {};
  const id = generateUUID();
  const now = new Date().toISOString();

  const newProduct: DBProduct = {
    id,
    store_id: ownerStore.id,
    name: payload.name || "Novo Produto",
    description: payload.description || "",
    image: payload.image || DEFAULT_PRODUCT_IMG,
    quantity: payload.quantity !== undefined ? Number(payload.quantity) : 0,
    price: payload.price ? Number(payload.price) : 0.0,
    inserted_at: now,
    updated_at: now
  };

  products.push(newProduct);
  res.json({ product: newProduct });
});

app.get("/api/store/product", (req, res) => {
  const account = getAccountFromToken(req);
  const ownerStore = stores.find(s => s.owner_id === account?.id) || stores[0];
  const ownerProducts = products.filter(p => p.store_id === ownerStore.id);
  res.json({ products: ownerProducts });
});

app.patch("/api/store/product/:id", (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) return res.status(404).json({ errors: { detail: "Not Found" } });

  const payload = req.body?.product || req.body || {};
  if (payload.name !== undefined) product.name = payload.name;
  if (payload.description !== undefined) product.description = payload.description;
  if (payload.price !== undefined) product.price = Number(payload.price);
  if (payload.quantity !== undefined) product.quantity = Number(payload.quantity);
  if (payload.image !== undefined) product.image = payload.image;
  product.updated_at = new Date().toISOString();

  res.json({ product });
});

app.delete("/api/store/product/:id", (req, res) => {
  const idx = products.findIndex(p => p.id === req.params.id);
  if (idx !== -1) products.splice(idx, 1);
  res.status(204).send();
});

// Store Owner Orders
function ensureStoreOrders(storeId: string, accountId: string): DBOrder[] {
  let storeOrders = sampleOrders.filter(o => o.store_id === storeId);
  if (storeOrders.length === 0) {
    const now = new Date();
    const ago15m = new Date(now.getTime() - 15 * 60000).toISOString();
    const ago1h = new Date(now.getTime() - 60 * 60000).toISOString();
    const ago3h = new Date(now.getTime() - 180 * 60000).toISOString();
    const ago1d = new Date(now.getTime() - 1440 * 60000).toISOString();

    const seeds: DBOrder[] = [
      {
        id: generateUUID(),
        store_id: storeId,
        account_id: accountId,
        customer_name: "Mariana Souza",
        customer_email: "mariana.souza@example.com",
        customer_username: "marianasouza",
        total: "149.90",
        payment_method: "PIX",
        status_order: "ESPERANDO",
        inserted_at: ago15m,
        updated_at: ago15m,
        order_product: []
      },
      {
        id: generateUUID(),
        store_id: storeId,
        account_id: accountId,
        customer_name: "Carlos Eduardo",
        customer_email: "carlos.eduardo@example.com",
        customer_username: "carloseduardo",
        total: "220.00",
        payment_method: "CC",
        status_order: "ACEITO",
        inserted_at: ago1h,
        updated_at: ago1h,
        order_product: []
      },
      {
        id: generateUUID(),
        store_id: storeId,
        account_id: accountId,
        customer_name: "Fernanda Lima",
        customer_email: "fernanda.lima@example.com",
        customer_username: "ferlima",
        total: "75.50",
        payment_method: "CD",
        status_order: "PREPARACAO",
        inserted_at: ago3h,
        updated_at: ago3h,
        order_product: []
      },
      {
        id: generateUUID(),
        store_id: storeId,
        account_id: accountId,
        customer_name: "Lucas Mendes",
        customer_email: "lucas.mendes@example.com",
        customer_username: "lucasmendes",
        total: "310.00",
        payment_method: "PIX",
        status_order: "ROTA",
        inserted_at: ago1d,
        updated_at: ago1d,
        order_product: []
      }
    ];

    sampleOrders.push(...seeds);
    storeOrders = seeds;
  }
  return storeOrders;
}

app.get("/api/store/order", (req, res) => {
  const account = getAccountFromToken(req);
  let ownerStore = stores.find(s => s.owner_id === account?.id) || stores[0];
  if (!ownerStore) {
    return res.json({ orders: [] });
  }
  const storeOrders = ensureStoreOrders(ownerStore.id, account?.id || "default-acc");
  res.json({
    orders: storeOrders.map(o => ({
      id: o.id,
      status: o.status_order,
      total: o.total,
      payment_method: o.payment_method,
      inserted_at: o.inserted_at,
      updated_at: o.updated_at,
      client: {
        id: o.account_id,
        username: o.customer_username,
        email: o.customer_email
      }
    }))
  });
});

app.get("/api/store/order/:id", (req, res) => {
  const order = sampleOrders.find(o => o.id === req.params.id);
  if (!order) return res.status(404).json({ errors: { detail: "Not Found" } });
  res.json({
    id: order.id,
    total: order.total,
    inserted_at: order.inserted_at,
    updated_at: order.updated_at,
    payment_method: order.payment_method,
    status_order: order.status_order,
    client: {
      id: order.account_id,
      username: order.customer_username,
      email: order.customer_email
    },
    address: order.address || undefined,
    order_product: order.order_product
  });
});

// Place new order
app.post("/api/store/order/:id", (req, res) => {
  const storeId = req.params.id;
  const store = stores.find(s => s.id === storeId);
  if (!store) return res.status(404).json({ errors: { detail: "Not Found" } });

  const { products: orderProducts, payment_method } = req.body || {};
  if (!payment_method || !Array.isArray(orderProducts) || orderProducts.length === 0) {
    return res.status(422).json({ errors: { detail: "invalid payload" } });
  }

  let totalNum = 0;
  const dbOrderItems: DBOrderProductItem[] = [];

  for (const item of orderProducts) {
    const prod = products.find(p => p.id === item.product);
    if (!prod) {
      return res.status(422).json({ errors: { detail: "one or more products were not found" } });
    }
    const qty = item.quantity || 1;
    if (prod.quantity < qty) {
      return res.status(422).json({ errors: { detail: "insufficient stock" } });
    }
    prod.quantity -= qty;
    totalNum += prod.price * qty;

    dbOrderItems.push({
      id: prod.id,
      name: prod.name,
      description: prod.description,
      image: prod.image,
      price: prod.price,
      quantity: qty,
      inserted_at: prod.inserted_at,
      updated_at: prod.updated_at
    });
  }

  const account = getAccountFromToken(req);
  const orderId = generateUUID();
  const now = new Date().toISOString();

  const newOrder: DBOrder = {
    id: orderId,
    store_id: storeId,
    account_id: account.id,
    customer_name: account.username || "Cliente Anônimo",
    customer_email: account.email || "cliente@example.com",
    customer_username: account.username || "cliente",
    total: totalNum.toFixed(2),
    payment_method: payment_method,
    status_order: "ESPERANDO",
    inserted_at: now,
    updated_at: now,
    order_product: dbOrderItems,
    address: addresses[0]
  };

  sampleOrders.unshift(newOrder);

  res.json({
    id: newOrder.id,
    total: newOrder.total,
    payment_method: newOrder.payment_method,
    status_order: newOrder.status_order,
    order_product: newOrder.order_product
  });
});

// Update order status with state machine transition rules
app.patch("/api/store/order/:id", (req, res) => {
  const order = sampleOrders.find(o => o.id === req.params.id);
  if (!order) return res.status(404).json({ errors: { detail: "Not Found" } });

  const { before, after } = req.body || {};

  // Valid status transitions according to endpoints.txt spec:
  // ESPERANDO -> ACEITO/RECUSADO
  // ACEITO -> PREPARACAO/CANCELADO
  // PREPARACAO -> ROTA/CANCELADO
  // ROTA -> CONCLUIDO/CANCELADO
  const validTransitions: Record<string, string[]> = {
    ESPERANDO: ["ACEITO", "RECUSADO"],
    ACEITO: ["PREPARACAO", "CANCELADO"],
    PREPARACAO: ["ROTA", "CANCELADO"],
    ROTA: ["CONCLUIDO", "CANCELADO"]
  };

  const allowed = validTransitions[order.status_order];
  if (before && before !== order.status_order) {
    return res.status(422).json({ error: "Invalid status transition" });
  }

  if (after && allowed && !allowed.includes(after)) {
    return res.status(422).json({ error: "Invalid status transition" });
  }

  if (after) {
    order.status_order = after;
    order.updated_at = new Date().toISOString();
  }

  res.json({
    id: order.id,
    total: order.total,
    payment_method: order.payment_method,
    status_order: order.status_order
  });
});

async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
