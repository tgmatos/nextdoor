import React from 'react';
import { ActiveTab, Store } from '../types';
import { LogOut, Store as StoreIcon, ShoppingBag, Package } from 'lucide-react';

interface HeaderProps {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
  store: Store | null;
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  activeTab,
  setActiveTab,
  store,
  onLogout
}) => {
  const getTabTitle = () => {
    switch (activeTab) {
      case 'orders':
        return 'Painel de Pedidos';
      case 'inventory':
        return 'Gestão de Estoque';
      case 'store_profile':
        return 'Perfil da Loja & Configurações';
      default:
        return 'Painel de Controle';
    }
  };

  return (
    <header className="bg-white border-b border-[#e5e5df] sticky top-0 z-30 text-[#3d3d33] shadow-xs px-4 sm:px-8 py-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        
        {/* Left: Breadcrumb / Title */}
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#f0f0eb] border border-[#e5e5df] flex items-center justify-center text-[#5A5A40] shrink-0">
            {activeTab === 'orders' && <ShoppingBag className="w-5 h-5" />}
            {activeTab === 'inventory' && <Package className="w-5 h-5" />}
            {activeTab === 'store_profile' && <StoreIcon className="w-5 h-5" />}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-[#8a8a78] uppercase tracking-wider">
                {store?.name || 'Sua Loja'}
              </span>
              <span className="text-[#8a8a78] text-xs">•</span>
              <span className="text-xs font-semibold text-[#5A5A40]">
                {store?.category || 'Comércio'}
              </span>
            </div>
            <h1 className="font-serif font-bold text-lg text-[#3d3d33] leading-tight">
              {getTabTitle()}
            </h1>
          </div>
        </div>

        {/* Right Actions: Logout Button */}
        <div className="flex items-center justify-end gap-3">
          
          {/* Logout Button */}
          <button
            id="btn-topbar-logout"
            type="button"
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onLogout?.();
            }}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full bg-[#f5f5f0] hover:bg-rose-50 text-[#5A5A40] hover:text-rose-700 font-bold text-xs border border-[#e5e5df] hover:border-rose-200 transition-all duration-150 active:scale-95 cursor-pointer"
            title="Encerrar sessão"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sair</span>
          </button>

        </div>

      </div>
    </header>
  );
};
