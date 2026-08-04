import React, { useState, useMemo } from 'react';
import { Product } from '../types';
import { formatDate } from '../utils';
import { 
  Package, 
  Search, 
  Plus, 
  AlertTriangle, 
  CheckCircle2, 
  XCircle, 
  RefreshCw,
  ChevronRight
} from 'lucide-react';

interface InventoryPageProps {
  products: Product[];
  isLoading: boolean;
  onRefresh: () => void;
  onSelectProduct: (product: Product) => void;
  selectedProductId?: string;
  onAddProduct: () => void;
}

export const InventoryPage: React.FC<InventoryPageProps> = ({
  products,
  isLoading,
  onRefresh,
  onSelectProduct,
  selectedProductId,
  onAddProduct
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [stockFilter, setStockFilter] = useState<'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK'>('ALL');

  // Filtered products list
  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      // Search term
      if (searchTerm.trim()) {
        const term = searchTerm.toLowerCase().trim();
        const nameMatch = p.name.toLowerCase().includes(term);
        const descMatch = (p.description || '').toLowerCase().includes(term);
        const idMatch = p.id.toLowerCase().includes(term);
        if (!nameMatch && !descMatch && !idMatch) return false;
      }

      // Stock status filter
      if (stockFilter === 'IN_STOCK') return p.quantity > 5;
      if (stockFilter === 'LOW_STOCK') return p.quantity > 0 && p.quantity <= 5;
      if (stockFilter === 'OUT_OF_STOCK') return p.quantity === 0;

      return true;
    });
  }, [products, searchTerm, stockFilter]);

  // Inventory stats
  const totalProducts = products.length;
  const totalStockItems = products.reduce((sum, p) => sum + (p.quantity || 0), 0);
  const lowStockCount = products.filter(p => p.quantity > 0 && p.quantity <= 5).length;
  const outOfStockCount = products.filter(p => p.quantity === 0).length;

  return (
    <div className="space-y-6">
      
      {/* Top Banner & Stats Overview */}
      <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-serif font-bold text-[#3d3d33] tracking-tight flex items-center gap-2">
              <Package className="w-6 h-6 text-[#5A5A40]" />
              Gestão de Estoque e Inventário
            </h2>
            <p className="text-xs text-[#8a8a78] mt-1">
              Controle a disponibilidade de produtos, preços e níveis de estoque da sua loja.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={onRefresh}
              disabled={isLoading}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-[#f5f5f0] text-[#5A5A40] hover:bg-[#ebebe5] text-xs font-bold transition-all border border-[#e5e5df] active:scale-95 disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-[#5A5A40]' : 'text-[#8a8a78]'}`} />
              <span>Atualizar</span>
            </button>

            <button
              id="btn-add-product"
              onClick={onAddProduct}
              className="inline-flex items-center gap-1.5 px-5 py-2 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white text-xs font-bold shadow-xs transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>+ Novo Produto</span>
            </button>
          </div>
        </div>

        {/* Quick Metrics Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 pt-3 border-t border-[#f0f0eb]">
          <div className="bg-[#f9f9f7] p-4 rounded-2xl border border-[#e5e5df]">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] block">Total de Produtos</span>
            <span className="text-xl font-serif font-extrabold text-[#3d3d33] mt-1 block">{totalProducts}</span>
          </div>

          <div className="bg-[#f9f9f7] p-4 rounded-2xl border border-[#e5e5df]">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] block">Itens em Estoque</span>
            <span className="text-xl font-serif font-extrabold text-[#5A5A40] mt-1 block">{totalStockItems} un.</span>
          </div>

          <div className="bg-amber-50/60 p-4 rounded-2xl border border-amber-200/80">
            <span className="text-[10px] font-bold uppercase tracking-wider text-amber-800 block">Estoque Baixo (≤ 5)</span>
            <span className="text-xl font-serif font-extrabold text-amber-900 mt-1 block">{lowStockCount} produtos</span>
          </div>

          <div className="bg-rose-50/60 p-4 rounded-2xl border border-rose-200/80">
            <span className="text-[10px] font-bold uppercase tracking-wider text-rose-800 block">Sem Estoque (0)</span>
            <span className="text-xl font-serif font-extrabold text-rose-900 mt-1 block">{outOfStockCount} produtos</span>
          </div>
        </div>

        {/* Search & Stock Filter bar */}
        <div className="flex flex-col md:flex-row items-center gap-3 pt-3 border-t border-[#f0f0eb]">
          <div className="relative w-full md:flex-1">
            <Search className="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
            <input
              id="product-search-input"
              type="text"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              placeholder="Buscar produto por nome ou descrição..."
              className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm text-[#3d3d33] placeholder:text-[#8a8a78] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40] transition-all"
            />
          </div>

          <div className="flex items-center gap-1.5 w-full md:w-auto overflow-x-auto pb-1 md:pb-0">
            <button
              onClick={() => setStockFilter('ALL')}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                stockFilter === 'ALL'
                  ? 'bg-[#5A5A40] text-white shadow-xs'
                  : 'bg-[#f5f5f0] text-[#5A5A40] hover:bg-[#ebebe5]'
              }`}
            >
              Todos ({totalProducts})
            </button>

            <button
              onClick={() => setStockFilter('IN_STOCK')}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                stockFilter === 'IN_STOCK'
                  ? 'bg-emerald-700 text-white shadow-xs'
                  : 'bg-emerald-100/70 text-emerald-900 hover:bg-emerald-100'
              }`}
            >
              Em Estoque
            </button>

            <button
              onClick={() => setStockFilter('LOW_STOCK')}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                stockFilter === 'LOW_STOCK'
                  ? 'bg-amber-700 text-white shadow-xs'
                  : 'bg-amber-100/70 text-amber-900 hover:bg-amber-100'
              }`}
            >
              Estoque Baixo ({lowStockCount})
            </button>

            <button
              onClick={() => setStockFilter('OUT_OF_STOCK')}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                stockFilter === 'OUT_OF_STOCK'
                  ? 'bg-rose-700 text-white shadow-xs'
                  : 'bg-rose-100/70 text-rose-900 hover:bg-rose-100'
              }`}
            >
              Esgotados ({outOfStockCount})
            </button>
          </div>
        </div>
      </div>

      {/* Products Grid / Table */}
      <div className="bg-white rounded-3xl border border-[#e5e5df] shadow-xs overflow-hidden">
        
        {/* Requirement 1: List all products with name and quantity */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#f9f9f7] text-[#5A5A40] text-[10px] uppercase font-bold tracking-wider border-b border-[#e5e5df]">
                <th className="py-3.5 px-5">Produto</th>
                <th className="py-3.5 px-5 text-center">Quantidade em Estoque</th>
                <th className="py-3.5 px-5 text-right">Preço Unitário</th>
                <th className="py-3.5 px-5">Última Atualização</th>
                <th className="py-3.5 px-3 text-center w-10"></th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#f0f0eb] text-sm">
              {filteredProducts.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#8a8a78]">
                    <div className="max-w-xs mx-auto space-y-3">
                      <div className="w-12 h-12 rounded-full bg-[#f5f5f0] flex items-center justify-center mx-auto text-[#8a8a78]">
                        <Package className="w-6 h-6" />
                      </div>
                      <p className="font-serif text-[#3d3d33] text-lg font-bold">Nenhum produto encontrado</p>
                      <p className="text-xs text-[#8a8a78]">Tente buscar por outro termo ou cadastre um novo produto.</p>
                      <button
                        onClick={onAddProduct}
                        className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-[#5A5A40] text-white text-xs font-bold shadow-xs"
                      >
                        + Cadastrar Produto
                      </button>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredProducts.map((product) => {
                  const isSelected = selectedProductId === product.id;
                  
                  // Stock status badge
                  let stockBadge = (
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-100/80 text-emerald-900 border border-emerald-200">
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                      {product.quantity} unidades
                    </span>
                  );

                  if (product.quantity === 0) {
                    stockBadge = (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-rose-100/80 text-rose-900 border border-rose-200">
                        <XCircle className="w-3.5 h-3.5 text-rose-600" />
                        Sem estoque
                      </span>
                    );
                  } else if (product.quantity <= 5) {
                    stockBadge = (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-100/80 text-amber-900 border border-amber-200">
                        <AlertTriangle className="w-3.5 h-3.5 text-amber-600" />
                        Apenas {product.quantity} un.
                      </span>
                    );
                  }

                  return (
                    // Requirement 2: When clicking a product, opens right side panel with all details
                    <tr
                      key={product.id}
                      tabIndex={0}
                      role="button"
                      aria-label={`Ver detalhes do produto ${product.name}`}
                      onClick={() => onSelectProduct(product)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          onSelectProduct(product);
                        }
                      }}
                      className={`cursor-pointer transition-colors duration-150 group focus:outline-none focus:ring-2 focus:ring-inset focus:ring-[#5A5A40] ${
                        isSelected 
                          ? 'bg-[#f5f5f0] border-l-4 border-l-[#5A5A40]' 
                          : 'hover:bg-[#fdfdfb]'
                      }`}
                    >
                      {/* Product Thumbnail & Name */}
                      <td className="py-3.5 px-5">
                        <div className="flex items-center gap-3">
                          <div className="w-11 h-11 rounded-2xl bg-[#f5f5f0] border border-[#e5e5df] shrink-0 overflow-hidden flex items-center justify-center">
                            {product.image ? (
                              <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                            ) : (
                              <Package className="w-5 h-5 text-[#8a8a78]" />
                            )}
                          </div>
                          <div>
                            <p className="font-bold text-[#3d3d33] group-hover:text-[#5A5A40] transition-colors text-xs sm:text-sm">
                              {product.name}
                            </p>
                            <p className="text-xs text-[#8a8a78] line-clamp-1 max-w-xs">
                              {product.description || 'Sem descrição'}
                            </p>
                          </div>
                        </div>
                      </td>

                      {/* Quantity / Stock status */}
                      <td className="py-3.5 px-5 text-center">
                        {stockBadge}
                      </td>

                      {/* Unit Price */}
                      <td className="py-3.5 px-5 text-right font-serif font-bold text-[#5A5A40] text-base">
                        R$ {parseFloat(String(product.price)).toFixed(2)}
                      </td>

                      {/* Last Updated */}
                      <td className="py-3.5 px-5 text-xs text-[#8a8a78]">
                        {formatDate(product.updated_at || product.inserted_at)}
                      </td>

                      {/* Right Chevron */}
                      <td className="py-3.5 px-3 text-center">
                        <ChevronRight className="w-4 h-4 text-[#8a8a78] group-hover:text-[#5A5A40] group-hover:translate-x-0.5 transition-all" />
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

      </div>

    </div>
  );
};
