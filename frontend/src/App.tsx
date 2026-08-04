import React, { useState, useEffect, useCallback } from 'react';
import { ActiveTab, Order, Product, Store, Account, Address, OrderStatusType } from './types';
import { Header } from './components/Header';
import { Sidebar } from './components/Sidebar';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import { OrdersDashboard } from './components/OrdersDashboard';
import { OrderDetailsPanel } from './components/OrderDetailsPanel';
import { InventoryPage } from './components/InventoryPage';
import { ProductDetailsPanel } from './components/ProductDetailsPanel';
import { AddProductModal } from './components/AddProductModal';
import { StoreProfilePage } from './components/StoreProfilePage';
import { Toast } from './components/Toast';
import { 
  fetchStoreOrders, 
  fetchOrderDetails, 
  updateOrderStatus, 
  fetchStoreProducts, 
  createProduct, 
  updateProduct, 
  deleteProduct,
  fetchStoreProfile, 
  updateStoreProfile, 
  deleteStoreProfile,
  fetchAccount, 
  updateAccount, 
  fetchAddresses, 
  updateAddress,
  logoutAccount,
  getAuthToken,
  clearAuthToken,
  onUnauthorized
} from './api/client';

export default function App() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('orders');
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => Boolean(getAuthToken()));
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');

  // Data states
  const [orders, setOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [store, setStore] = useState<Store | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);

  // Selection states for side panels
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  // Modals & Loaders
  const [isAddProductOpen, setIsAddProductOpen] = useState(false);
  const [isLoadingOrders, setIsLoadingOrders] = useState(false);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [isLoadingProfile, setIsLoadingProfile] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [isUpdatingProduct, setIsUpdatingProduct] = useState(false);
  const [isSubmittingProduct, setIsSubmittingProduct] = useState(false);

  // Toast notifications
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  // Loaders
  const loadOrders = useCallback(async () => {
    setIsLoadingOrders(true);
    try {
      const data = await fetchStoreOrders();
      setOrders(data);
    } catch (err: any) {
      console.error('Error fetching orders:', err);
      if (err?.status !== 401) {
        showToast(err?.message || 'Erro ao carregar pedidos', 'error');
      }
    } finally {
      setIsLoadingOrders(false);
    }
  }, []);

  const loadProducts = useCallback(async () => {
    setIsLoadingProducts(true);
    try {
      const data = await fetchStoreProducts();
      setProducts(data);
    } catch (err: any) {
      console.error('Error fetching products:', err);
      if (err?.status !== 401) {
        showToast(err?.message || 'Erro ao carregar produtos', 'error');
      }
    } finally {
      setIsLoadingProducts(false);
    }
  }, []);

  const loadProfile = useCallback(async () => {
    setIsLoadingProfile(true);
    try {
      const [s, a, addr] = await Promise.all([
        fetchStoreProfile().catch((e: any) => e?.status === 401 ? Promise.reject(e) : null),
        fetchAccount().catch((e: any) => e?.status === 401 ? Promise.reject(e) : null),
        fetchAddresses().catch((e: any) => e?.status === 401 ? Promise.reject(e) : [])
      ]);
      if (s) setStore(s);
      if (a) setAccount(a);
      if (addr) setAddresses(addr);
    } catch (err: any) {
      console.error('Error loading store profile:', err);
      if (err?.status !== 401) {
        showToast(err?.message || 'Erro ao carregar perfil da loja', 'error');
      }
    } finally {
      setIsLoadingProfile(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      loadOrders();
      loadProducts();
      loadProfile();
    }
  }, [isAuthenticated, loadOrders, loadProducts, loadProfile]);

  // Auto-refresh orders every 15s while on the orders tab
  useEffect(() => {
    if (!isAuthenticated || activeTab !== 'orders') return;
    const interval = setInterval(() => {
      loadOrders();
    }, 15000);
    return () => clearInterval(interval);
  }, [isAuthenticated, activeTab, loadOrders]);

  // Force logout when the session expires (401)
  useEffect(() => {
    onUnauthorized(() => {
      setIsAuthenticated(false);
      setAuthMode('login');
      setStore(null);
      setAccount(null);
      setOrders([]);
      setProducts([]);
      setSelectedOrder(null);
      setSelectedProduct(null);
      showToast('Sessão expirada. Faça login novamente.', 'error');
    });
  }, []);

  // Handle Order Selection & Details
  const handleSelectOrder = async (order: Order) => {
    try {
      const fullDetails = await fetchOrderDetails(order.id);
      setSelectedOrder(fullDetails);
    } catch {
      setSelectedOrder(order);
    }
  };

  // Handle Status Update
  const handleUpdateOrderStatus = async (
    orderId: string, 
    before: OrderStatusType, 
    after: OrderStatusType
  ) => {
    setIsUpdatingStatus(true);
    try {
      const res = await updateOrderStatus(orderId, before, after);
      
      // Update local state
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status_order: res.status_order, updated_at: new Date().toISOString() } : o));
      if (selectedOrder && selectedOrder.id === orderId) {
        setSelectedOrder(prev => prev ? { ...prev, status_order: res.status_order, updated_at: new Date().toISOString() } : null);
      }
      showToast(`Status do pedido alterado para ${res.status_order}`);
    } catch (err: any) {
      showToast(err.message || 'Erro ao atualizar status do pedido', 'error');
      throw err;
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  // Product Handlers
  const handleAddProduct = async (productData: Partial<Product>) => {
    setIsSubmittingProduct(true);
    try {
      const newProd = await createProduct(productData);
      setProducts(prev => [newProd, ...prev]);
      showToast(`Produto "${newProd.name}" cadastrado com sucesso!`);
    } catch (err: any) {
      showToast(err.message || 'Erro ao criar produto', 'error');
      throw err;
    } finally {
      setIsSubmittingProduct(false);
    }
  };

  const handleUpdateProduct = async (id: string, updatedData: Partial<Product>) => {
    setIsUpdatingProduct(true);
    try {
      const updated = await updateProduct(id, updatedData);
      setProducts(prev => prev.map(p => p.id === id ? updated : p));
      if (selectedProduct && selectedProduct.id === id) {
        setSelectedProduct(updated);
      }
      showToast(`Produto "${updated.name}" atualizado!`);
    } catch (err: any) {
      showToast(err.message || 'Erro ao atualizar produto', 'error');
      throw err;
    } finally {
      setIsUpdatingProduct(false);
    }
  };

  const handleDeleteProduct = async (id: string) => {
    try {
      await deleteProduct(id);
      setProducts(prev => prev.filter(p => p.id !== id));
      if (selectedProduct && selectedProduct.id === id) {
        setSelectedProduct(null);
      }
      showToast('Produto excluído com sucesso');
    } catch (err: any) {
      showToast(err.message || 'Erro ao excluir produto', 'error');
      throw err;
    }
  };

  // Profile Handlers
  const handleUpdateStore = async (storeData: Partial<Store>) => {
    try {
      const updated = await updateStoreProfile(storeData);
      setStore(updated);
      showToast('Perfil da loja atualizado!');
    } catch (err: any) {
      showToast(err.message || 'Erro ao atualizar loja', 'error');
      throw err;
    }
  };

  const handleUpdateAccount = async (accountData: Partial<Account>) => {
    try {
      const updated = await updateAccount(accountData);
      setAccount(prev => prev ? { ...prev, ...updated } : null);
      showToast('Dados da conta atualizados!');
    } catch (err: any) {
      showToast(err.message || 'Erro ao atualizar conta', 'error');
      throw err;
    }
  };

  const handleUpdateAddress = async (id: string, addrData: Partial<Address>) => {
    try {
      const updated = await updateAddress(id, addrData);
      setAddresses(prev => prev.map(a => a.id === id ? updated : a));
      showToast('Endereço atualizado!');
    } catch (err: any) {
      showToast(err.message || 'Erro ao atualizar endereço', 'error');
      throw err;
    }
  };

  const handleDeleteStore = async () => {
    try {
      await deleteStoreProfile();
      setStore(null);
      showToast('Loja removida');
    } catch (err: any) {
      showToast(err.message || 'Erro ao remover loja', 'error');
    }
  };

  const handleLogin = (userEmail: string) => {
    setIsAuthenticated(true);
    loadProfile();
    loadOrders();
    loadProducts();
    showToast(`Bem-vindo! Login efetuado com sucesso.`);
  };

  const handleRegisterComplete = () => {
    setIsAuthenticated(true);
    setAuthMode('login');
    loadProfile();
    loadOrders();
    loadProducts();
    showToast('Conta e Loja criadas com sucesso! Bem-vindo ao seu painel.');
  };

  const handleLogout = () => {
    clearAuthToken();
    setIsAuthenticated(false);
    setAuthMode('login');
    setStore(null);
    setAccount(null);
    setOrders([]);
    setProducts([]);
    setSelectedOrder(null);
    setSelectedProduct(null);
    showToast('Sessão encerrada com sucesso!');

    logoutAccount().catch((e) => {
      console.warn('Logout endpoint notification error:', e);
    });
  };

  if (!isAuthenticated) {
    return (
      <>
        <Toast toast={toast} />

        {authMode === 'register' ? (
          <RegisterPage
            onComplete={handleRegisterComplete}
            onGoToLogin={() => setAuthMode('login')}
          />
        ) : (
          <LoginPage 
            store={store} 
            onLogin={handleLogin} 
            onGoToRegister={() => setAuthMode('register')}
          />
        )}
      </>
    );
  }

  return (
    <div className="min-h-screen bg-[#fdfdfb] text-[#3d3d33] flex flex-col md:flex-row font-sans">
      
      {/* Toast Floating Notification */}
      <Toast toast={toast} />

      {/* Requirement: Left Sidebar Navigation with Top Store Image Button */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        store={store}
        ordersCount={orders.length}
        productsCount={products.length}
        onLogout={handleLogout}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        
        {/* Requirement: Topbar with Logout button on right */}
        <Header
          activeTab={activeTab}
          setActiveTab={setActiveTab}
          store={store}
          onLogout={handleLogout}
        />

        {/* Main Container Content */}
        <main className="flex-1 p-4 sm:p-8 max-w-7xl w-full mx-auto">
          
          {/* Tab 1: Default Page - Orders Dashboard */}
          {activeTab === 'orders' && (
            <OrdersDashboard
              orders={orders}
              isLoading={isLoadingOrders}
              onRefresh={loadOrders}
              onSelectOrder={handleSelectOrder}
              selectedOrderId={selectedOrder?.id}
            />
          )}

          {/* Tab 2: Inventory Page */}
          {activeTab === 'inventory' && (
            <InventoryPage
              products={products}
              isLoading={isLoadingProducts}
              onRefresh={loadProducts}
              onSelectProduct={setSelectedProduct}
              selectedProductId={selectedProduct?.id}
              onAddProduct={() => setIsAddProductOpen(true)}
            />
          )}

          {/* Tab 3: Store Profile & Account Page */}
          {activeTab === 'store_profile' && (
            <StoreProfilePage
              store={store}
              account={account}
              addresses={addresses}
              onUpdateStore={handleUpdateStore}
              onUpdateAccount={handleUpdateAccount}
              onUpdateAddress={handleUpdateAddress}
              onDeleteStore={handleDeleteStore}
              isLoading={isLoadingProfile}
            />
          )}

        </main>

        {/* Footer copyright */}
        <footer className="border-t border-[#e5e5df] bg-[#f9f9f7] py-4 text-center text-xs text-[#8a8a78] mt-auto">
          <p>© NextDoor Portal Lojista</p>
        </footer>

      </div>

      {/* Order Details Slide-over Panel */}
      {selectedOrder && (
        <OrderDetailsPanel
          order={selectedOrder}
          onClose={() => setSelectedOrder(null)}
          onUpdateStatus={handleUpdateOrderStatus}
          isUpdating={isUpdatingStatus}
        />
      )}

      {/* Product Details Slide-over Panel */}
      {selectedProduct && (
        <ProductDetailsPanel
          product={selectedProduct}
          onClose={() => setSelectedProduct(null)}
          onUpdateProduct={handleUpdateProduct}
          onDeleteProduct={handleDeleteProduct}
          isUpdating={isUpdatingProduct}
        />
      )}

      {/* Add Product Modal */}
      <AddProductModal
        isOpen={isAddProductOpen}
        onClose={() => setIsAddProductOpen(false)}
        onAddProduct={handleAddProduct}
        isSubmitting={isSubmittingProduct}
      />

    </div>
  );
}
