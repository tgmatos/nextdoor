import React from 'react';
import { ActiveTab, Store } from '../types';
import { ShoppingBag, Package, Store as StoreIcon, ChevronRight, LogOut } from 'lucide-react';

interface SidebarProps {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
  store: Store | null;
  ordersCount: number;
  productsCount: number;
  onLogout?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  setActiveTab,
  store,
  ordersCount,
  productsCount,
  onLogout
}) => {
  return (
    <aside className="w-full md:w-64 bg-white border-b md:border-b-0 md:border-r border-[#e5e5df] flex flex-col justify-between shrink-0 shadow-xs z-20">
      <div className="p-4 space-y-6">
        
        {/* Requirement: Top Button with Store's Image (Click opens 'perfil da loja') */}
        <button
          id="sidebar-store-profile-btn"
          onClick={() => setActiveTab('store_profile')}
          className={`w-full text-left p-3 rounded-2xl border transition-all group flex items-center gap-3 ${
            activeTab === 'store_profile'
              ? 'bg-[#f0f0eb] border-[#5A5A40] shadow-xs ring-1 ring-[#5A5A40]/20'
              : 'bg-[#f9f9f7] border-[#e5e5df] hover:border-[#5A5A40]/50 hover:bg-[#f0f0eb]/60'
          }`}
          title="Clique para abrir o Perfil da Loja"
        >
          <div className="relative w-12 h-12 rounded-xl bg-[#5A5A40] overflow-hidden border border-[#e5e5df] shrink-0 flex items-center justify-center text-white shadow-xs">
            {store?.image ? (
              <img 
                src={store.image} 
                alt={store.name || 'Logo da Loja'} 
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200" 
              />
            ) : (
              <StoreIcon className="w-6 h-6 text-[#f5f5f0]" />
            )}
          </div>

          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <h2 className="font-serif italic font-bold text-sm text-[#3d3d33] truncate group-hover:text-[#5A5A40]">
                {store?.name || 'Sua Loja'}
              </h2>
              <ChevronRight className="w-3.5 h-3.5 text-[#8a8a78] group-hover:translate-x-0.5 transition-transform shrink-0" />
            </div>
            <p className="text-[11px] font-semibold text-[#8a8a78] truncate mt-0.5">
              {store?.category || 'Comércio'}
            </p>
            <span className="inline-block mt-1 text-[9px] font-bold text-[#5A5A40] uppercase tracking-wider bg-[#e5e5df]/60 px-1.5 py-0.5 rounded">
              Ver Perfil
            </span>
          </div>
        </button>

        {/* Navigation Section */}
        <div className="space-y-1">
          <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] px-3 block mb-2">
            Navegação Principal
          </span>

          {/* Pedidos (Orders) */}
          <button
            id="sidebar-nav-orders"
            onClick={() => setActiveTab('orders')}
            className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-bold transition-all duration-150 ${
              activeTab === 'orders'
                ? 'bg-[#5A5A40] text-white shadow-xs'
                : 'text-[#5A5A40] hover:text-[#3d3d33] hover:bg-[#f0f0eb]'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <ShoppingBag className="w-4 h-4 shrink-0" />
              <span>Pedidos</span>
            </div>
            {ordersCount > 0 && (
              <span className={`text-[11px] px-2 py-0.5 rounded-full font-extrabold ${
                activeTab === 'orders' ? 'bg-[#4a4a34] text-white' : 'bg-[#e5e5df] text-[#5A5A40]'
              }`}>
                {ordersCount}
              </span>
            )}
          </button>

          {/* Estoque (Inventory) */}
          <button
            id="sidebar-nav-inventory"
            onClick={() => setActiveTab('inventory')}
            className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-bold transition-all duration-150 ${
              activeTab === 'inventory'
                ? 'bg-[#5A5A40] text-white shadow-xs'
                : 'text-[#5A5A40] hover:text-[#3d3d33] hover:bg-[#f0f0eb]'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <Package className="w-4 h-4 shrink-0" />
              <span>Estoque</span>
            </div>
            {productsCount > 0 && (
              <span className={`text-[11px] px-2 py-0.5 rounded-full font-extrabold ${
                activeTab === 'inventory' ? 'bg-[#4a4a34] text-white' : 'bg-[#e5e5df] text-[#5A5A40]'
              }`}>
                {productsCount}
              </span>
            )}
          </button>
        </div>

      </div>

      {/* Sidebar Footer / Status & Logout */}
      <div className="p-4 border-t border-[#f0f0eb] bg-[#f9f9f7] space-y-3">
        <div className="flex items-center justify-between text-xs text-[#5A5A40]">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-600 animate-pulse"></span>
            <span className="font-bold text-emerald-900 text-[11px]">Loja Aberta & Online</span>
          </div>
        </div>

        {onLogout && (
          <button
            id="sidebar-logout-btn"
            type="button"
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onLogout();
            }}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl bg-white hover:bg-rose-50 text-rose-700 hover:text-rose-800 font-bold text-xs border border-[#e5e5df] hover:border-rose-200 transition-all shadow-2xs active:scale-95 cursor-pointer"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sair da Conta</span>
          </button>
        )}
      </div>
    </aside>
  );
};
