import React, { useState, useMemo } from 'react';
import { Order, SortField, SortDirection } from '../types';
import { 
  Search, 
  ArrowUpDown, 
  ArrowUp, 
  ArrowDown, 
  ChevronRight, 
  Clock, 
  User, 
  CreditCard, 
  Filter, 
  RefreshCw,
  ShoppingBag,
  ExternalLink,
  DollarSign,
  TrendingUp,
  Receipt
} from 'lucide-react';

interface OrdersDashboardProps {
  orders: Order[];
  isLoading: boolean;
  onRefresh: () => void;
  onSelectOrder: (order: Order) => void;
  selectedOrderId?: string;
}

export const OrdersDashboard: React.FC<OrdersDashboardProps> = ({
  orders,
  isLoading,
  onRefresh,
  onSelectOrder,
  selectedOrderId
}) => {
  // Requirement 3: Top search field to search for customer name or UUID
  const [searchTerm, setSearchTerm] = useState('');
  
  // Filter by status if needed
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Requirement 2: Column sorting state
  const [sortField, setSortField] = useState<SortField>('inserted_at');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  // Requirement 1: Limit options (Default 10, with option to show 25, 50, All)
  const [pageSize, setPageSize] = useState<number>(10);

  // Summary Widget Metrics
  const totalRevenue = useMemo(() => {
    return orders.reduce((acc, o) => {
      const st = o.status_order || o.status || 'ESPERANDO';
      if (st !== 'CANCELADO' && st !== 'RECUSADO') {
        return acc + (parseFloat(o.total) || 0);
      }
      return acc;
    }, 0);
  }, [orders]);

  const activeOrdersCount = useMemo(() => {
    return orders.filter(o => ['ESPERANDO', 'ACEITO', 'PREPARACAO', 'ROTA'].includes(o.status_order || o.status || 'ESPERANDO')).length;
  }, [orders]);

  const averageOrderValue = useMemo(() => {
    const validOrders = orders.filter(o => {
      const st = o.status_order || o.status || 'ESPERANDO';
      return st !== 'CANCELADO' && st !== 'RECUSADO';
    });
    if (validOrders.length === 0) return 0;
    return totalRevenue / validOrders.length;
  }, [orders, totalRevenue]);

  // Handle column header click for requirement 2
  const handleSort = (field: SortField) => {
    if (sortField === field) {
      // Toggle between desc and asc (crescent)
      setSortDirection(prev => (prev === 'desc' ? 'asc' : 'desc'));
    } else {
      setSortField(field);
      setSortDirection('desc'); // Default to desc when changing field
    }
  };

  // Filter & Sort Pipeline
  const filteredAndSortedOrders = useMemo(() => {
    let result = [...orders];

    // Search filter
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase().trim();
      result = result.filter(order => {
        const uuidMatch = order.id.toLowerCase().includes(term);
        const nameMatch = (order.customer_name || '').toLowerCase().includes(term);
        const emailMatch = (order.customer_email || '').toLowerCase().includes(term);
        const usernameMatch = (order.customer_username || '').toLowerCase().includes(term);
        return uuidMatch || nameMatch || emailMatch || usernameMatch;
      });
    }

    // Status filter
    if (statusFilter !== 'ALL') {
      result = result.filter(o => (o.status_order || o.status || 'ESPERANDO') === statusFilter);
    }

    // Sort logic
    result.sort((a, b) => {
      let valA: any = '';
      let valB: any = '';

      switch (sortField) {
        case 'id':
          valA = a.id;
          valB = b.id;
          break;
        case 'inserted_at':
          valA = new Date(a.inserted_at).getTime() || 0;
          valB = new Date(b.inserted_at).getTime() || 0;
          break;
        case 'updated_at':
          valA = new Date(a.updated_at).getTime() || 0;
          valB = new Date(b.updated_at).getTime() || 0;
          break;
        case 'customer_name':
          valA = (a.customer_name || 'Anônimo').toLowerCase();
          valB = (b.customer_name || 'Anônimo').toLowerCase();
          break;
        case 'total':
          valA = parseFloat(a.total) || 0;
          valB = parseFloat(b.total) || 0;
          break;
        case 'status_order':
          valA = a.status_order || a.status || '';
          valB = b.status_order || b.status || '';
          break;
        default:
          valA = a.inserted_at;
          valB = b.inserted_at;
      }

      if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  }, [orders, searchTerm, statusFilter, sortField, sortDirection]);

  // Apply page size limit
  const displayedOrders = useMemo(() => {
    if (pageSize === -1) return filteredAndSortedOrders;
    return filteredAndSortedOrders.slice(0, pageSize);
  }, [filteredAndSortedOrders, pageSize]);

  // Format date helper
  const formatDate = (dateStr: string) => {
    if (!dateStr) return 'N/A';
    try {
      const date = new Date(dateStr);
      return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
    } catch {
      return dateStr;
    }
  };

  // Status Badge styling helper
  const getStatusBadge = (status?: string) => {
    const s = String(status || 'ESPERANDO').trim().toUpperCase();
    switch (s) {
      case 'ESPERANDO':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-100/80 text-amber-900 border border-amber-200"><span className="w-1.5 h-1.5 rounded-full bg-amber-600 animate-pulse"></span>Esperando</span>;
      case 'ACEITO':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-blue-100/80 text-blue-900 border border-blue-200"><span className="w-1.5 h-1.5 rounded-full bg-blue-600"></span>Aceito</span>;
      case 'PREPARACAO':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#f0f0eb] text-[#5A5A40] border border-[#e5e5df]"><span className="w-1.5 h-1.5 rounded-full bg-[#5A5A40]"></span>Em Preparação</span>;
      case 'ROTA':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-purple-100/80 text-purple-900 border border-purple-200"><span className="w-1.5 h-1.5 rounded-full bg-purple-600"></span>Em Rota</span>;
      case 'CONCLUIDO':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-100/80 text-emerald-900 border border-emerald-200"><span className="w-1.5 h-1.5 rounded-full bg-emerald-600"></span>Concluído</span>;
      case 'RECUSADO':
      case 'CANCELADO':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-100/80 text-rose-900 border border-rose-200"><span className="w-1.5 h-1.5 rounded-full bg-rose-600"></span>{s === 'RECUSADO' ? 'Recusado' : 'Cancelado'}</span>;
      default:
        return <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#f0f0eb] text-[#3d3d33]">{s || 'Esperando'}</span>;
    }
  };

  // Sort Icon Renderer
  const renderSortIcon = (field: SortField) => {
    if (sortField !== field) {
      return <ArrowUpDown className="w-3.5 h-3.5 text-[#8a8a78] opacity-0 group-hover:opacity-100 transition-opacity" />;
    }
    return sortDirection === 'desc' ? (
      <ArrowDown className="w-3.5 h-3.5 text-[#5A5A40] font-bold" />
    ) : (
      <ArrowUp className="w-3.5 h-3.5 text-[#5A5A40] font-bold" />
    );
  };

  return (
    <div className="space-y-6">
      
      {/* Top Summary Widget */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Total Revenue Card */}
        <div className="bg-white p-5 rounded-3xl border border-[#e5e5df] shadow-xs flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] block">Receita Total</span>
            <p className="text-2xl font-serif font-extrabold text-[#3d3d33]">
              R$ {totalRevenue.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
            <p className="text-[11px] text-[#8a8a78]">Soma de vendas ativas e concluídas</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-[#f0f0eb] border border-[#e5e5df] flex items-center justify-center text-[#5A5A40] shrink-0">
            <TrendingUp className="w-6 h-6" />
          </div>
        </div>

        {/* Active Orders Count Card */}
        <div className="bg-white p-5 rounded-3xl border border-[#e5e5df] shadow-xs flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] block">Pedidos Ativos</span>
            <p className="text-2xl font-serif font-extrabold text-[#5A5A40]">
              {activeOrdersCount} <span className="text-sm font-sans font-normal text-[#8a8a78]">pedidos</span>
            </p>
            <p className="text-[11px] text-[#8a8a78]">Aguardando ou em andamento</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-amber-100/70 border border-amber-200 flex items-center justify-center text-amber-900 shrink-0">
            <Clock className="w-6 h-6 text-amber-800" />
          </div>
        </div>

        {/* Average Order Value Card */}
        <div className="bg-white p-5 rounded-3xl border border-[#e5e5df] shadow-xs flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#8a8a78] block">Ticket Médio</span>
            <p className="text-2xl font-serif font-extrabold text-[#3d3d33]">
              R$ {averageOrderValue.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
            <p className="text-[11px] text-[#8a8a78]">Média por pedido efetuado</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-[#f0f0eb] border border-[#e5e5df] flex items-center justify-center text-[#5A5A40] shrink-0">
            <Receipt className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Top Banner & Control Bar */}
      <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-serif font-bold text-[#3d3d33] tracking-tight flex items-center gap-2">
              <ShoppingBag className="w-6 h-6 text-[#5A5A40]" />
              Painel de Pedidos
            </h2>
            <p className="text-xs text-[#8a8a78] mt-1">
              Acompanhe vendas em tempo real, verifique detalhes e gerencie o fluxo de atendimento.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={onRefresh}
              disabled={isLoading}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-[#f5f5f0] text-[#5A5A40] hover:bg-[#ebebe5] text-xs font-bold transition-all border border-[#e5e5df] active:scale-95 disabled:opacity-50"
              title="Atualizar lista de pedidos"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-[#5A5A40]' : 'text-[#8a8a78]'}`} />
              <span>Atualizar</span>
            </button>
          </div>
        </div>

        {/* Filter Controls Row */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3 pt-3 border-t border-[#f0f0eb]">
          
          {/* Requirement 3: Top Search Field */}
          <div className="md:col-span-6 relative">
            <Search className="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
            <input
              id="order-search-input"
              type="text"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              placeholder="Buscar por nome do cliente ou UUID do pedido..."
              className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm text-[#3d3d33] placeholder:text-[#8a8a78] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40] transition-all"
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-[#8a8a78] hover:text-[#3d3d33] bg-[#e5e5df]/60 rounded-full px-2 py-0.5"
              >
                Limpar
              </button>
            )}
          </div>

          {/* Status Filter */}
          <div className="md:col-span-3 flex items-center gap-2">
            <Filter className="w-4 h-4 text-[#8a8a78] hidden sm:block" />
            <select
              id="status-filter-select"
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="w-full py-2.5 px-4 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#5A5A40] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
            >
              <option value="ALL">Todos os Status</option>
              <option value="ESPERANDO">Esperando</option>
              <option value="ACEITO">Aceito</option>
              <option value="PREPARACAO">Em Preparação</option>
              <option value="ROTA">Em Rota</option>
              <option value="CONCLUIDO">Concluído</option>
              <option value="CANCELADO">Cancelado / Recusado</option>
            </select>
          </div>

          {/* Requirement 1: Limit selector (Show 10 / 25 / 50 / All) */}
          <div className="md:col-span-3 flex items-center justify-end gap-2">
            <span className="text-xs font-semibold text-[#8a8a78] whitespace-nowrap">Mostrar:</span>
            <select
              id="page-size-select"
              value={pageSize}
              onChange={e => setPageSize(Number(e.target.value))}
              className="py-2.5 px-4 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-bold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
            >
              <option value={10}>10 pedidos</option>
              <option value={25}>25 pedidos</option>
              <option value={50}>50 pedidos</option>
              <option value={-1}>Todos os pedidos</option>
            </select>
          </div>

        </div>
      </div>

      {/* Orders Table Card */}
      <div className="bg-white rounded-3xl border border-[#e5e5df] shadow-xs overflow-hidden">
        
        {/* Table Header / Summary */}
        <div className="px-6 py-3.5 bg-[#f9f9f7] border-b border-[#e5e5df] flex items-center justify-between text-xs text-[#8a8a78] font-medium">
          <div className="flex items-center gap-2">
            <span>Exibindo <strong>{displayedOrders.length}</strong> de <strong>{filteredAndSortedOrders.length}</strong> pedidos encontrados</span>
            {filteredAndSortedOrders.length < orders.length && (
              <span className="text-[#5A5A40] font-bold">(filtrados de {orders.length} no total)</span>
            )}
          </div>
          <div className="flex items-center gap-2">
            <span className="text-[#8a8a78]">Ordenado por:</span>
            <span className="font-bold text-[#5A5A40] capitalize">
              {sortField === 'inserted_at' && 'Data de Criação'}
              {sortField === 'updated_at' && 'Data de Atualização'}
              {sortField === 'customer_name' && 'Nome do Cliente'}
              {sortField === 'id' && 'UUID'}
              {sortField === 'total' && 'Valor Total'}
              {sortField === 'status_order' && 'Status'}
              {' '}({sortDirection === 'desc' ? 'Decrescente' : 'Crescente'})
            </span>
          </div>
        </div>

        {/* Table Content */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#f9f9f7] text-[#5A5A40] text-[10px] font-bold uppercase tracking-wider border-b border-[#e5e5df]">
                
                {/* 1. Column: UUID */}
                <th 
                  onClick={() => handleSort('id')}
                  className="py-3.5 px-5 cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por UUID"
                >
                  <div className="flex items-center gap-1.5">
                    <span>UUID do Pedido</span>
                    {renderSortIcon('id')}
                  </div>
                </th>

                {/* 2. Column: Customer Name */}
                <th 
                  onClick={() => handleSort('customer_name')}
                  className="py-3.5 px-5 cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por Nome do Cliente"
                >
                  <div className="flex items-center gap-1.5">
                    <span>Cliente</span>
                    {renderSortIcon('customer_name')}
                  </div>
                </th>

                {/* 3. Column: Creation Date */}
                <th 
                  onClick={() => handleSort('inserted_at')}
                  className="py-3.5 px-5 cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por Data de Criação"
                >
                  <div className="flex items-center gap-1.5">
                    <span>Data de Criação</span>
                    {renderSortIcon('inserted_at')}
                  </div>
                </th>

                {/* 4. Column: Update Date */}
                <th 
                  onClick={() => handleSort('updated_at')}
                  className="py-3.5 px-5 cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por Data de Atualização"
                >
                  <div className="flex items-center gap-1.5">
                    <span>Data de Atualização</span>
                    {renderSortIcon('updated_at')}
                  </div>
                </th>

                {/* 5. Column: Total */}
                <th 
                  onClick={() => handleSort('total')}
                  className="py-3.5 px-5 text-right cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por Valor"
                >
                  <div className="flex items-center justify-end gap-1.5">
                    <span>Total</span>
                    {renderSortIcon('total')}
                  </div>
                </th>

                {/* 6. Column: Status */}
                <th 
                  onClick={() => handleSort('status_order')}
                  className="py-3.5 px-5 text-center cursor-pointer hover:underline transition-colors group select-none"
                  title="Clique para ordenar por Status"
                >
                  <div className="flex items-center justify-center gap-1.5">
                    <span>Status</span>
                    {renderSortIcon('status_order')}
                  </div>
                </th>

                <th className="py-3.5 px-3 text-center w-10"></th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#f0f0eb] text-sm">
              {displayedOrders.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#8a8a78]">
                    <div className="max-w-xs mx-auto space-y-3">
                      <div className="w-12 h-12 rounded-full bg-[#f5f5f0] flex items-center justify-center mx-auto text-[#8a8a78]">
                        <ShoppingBag className="w-6 h-6" />
                      </div>
                      <p className="font-serif text-[#3d3d33] text-lg font-bold">Nenhum pedido encontrado</p>
                      <p className="text-xs text-[#8a8a78]">
                        {searchTerm ? 'Tente buscar por outro termo ou limpar os filtros.' : 'Sua loja ainda não possui pedidos com este filtro.'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                displayedOrders.map((order) => {
                  const isSelected = selectedOrderId === order.id;
                  return (
                    <tr
                      key={order.id}
                      onClick={() => onSelectOrder(order)}
                      className={`cursor-pointer transition-colors duration-150 group ${
                        isSelected 
                          ? 'bg-[#f5f5f0] border-l-4 border-l-[#5A5A40]' 
                          : 'hover:bg-[#fdfdfb]'
                      }`}
                    >
                      {/* UUID */}
                      <td className="py-3.5 px-5 font-mono text-[11px] text-[#8a8a78] font-semibold group-hover:text-[#5A5A40] transition-colors">
                        <div className="flex items-center gap-2">
                          <span className="truncate max-w-[130px] sm:max-w-[180px]" title={order.id}>
                            {order.id}
                          </span>
                        </div>
                      </td>

                      {/* Customer Name */}
                      <td className="py-3.5 px-5 text-[#3d3d33] font-medium">
                        <div className="flex items-center gap-2.5">
                          <div className="w-8 h-8 rounded-full bg-[#d8d8ce] text-[#5A5A40] flex items-center justify-center font-bold text-xs uppercase">
                            {(order.customer_name || 'A').charAt(0)}
                          </div>
                          <div>
                            <p className="text-xs font-bold text-[#3d3d33] leading-tight">
                              {order.customer_name || 'Cliente Sem Nome'}
                            </p>
                            {order.customer_email && (
                              <p className="text-[10px] text-[#8a8a78] leading-tight">
                                {order.customer_email}
                              </p>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Creation Date */}
                      <td className="py-3.5 px-5 text-[#5A5A40] text-xs">
                        <div className="flex items-center gap-1.5">
                          <Clock className="w-3.5 h-3.5 text-[#8a8a78]" />
                          <span>{formatDate(order.inserted_at)}</span>
                        </div>
                      </td>

                      {/* Update Date */}
                      <td className="py-3.5 px-5 text-[#8a8a78] text-xs">
                        <div className="flex items-center gap-1.5">
                          <Clock className="w-3.5 h-3.5 text-[#8a8a78]" />
                          <span>{formatDate(order.updated_at)}</span>
                        </div>
                      </td>

                      {/* Total Cost */}
                      <td className="py-3.5 px-5 text-right font-serif font-bold text-[#5A5A40] text-base">
                        R$ {parseFloat(order.total || '0').toFixed(2)}
                        <span className="block text-[10px] text-[#8a8a78] font-sans font-normal">
                          {order.payment_method || 'PIX'}
                        </span>
                      </td>

                      {/* Status */}
                      <td className="py-3.5 px-5 text-center">
                        {getStatusBadge(order.status_order || order.status || 'ESPERANDO')}
                      </td>

                      {/* Chevron Arrow */}
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

        {/* Footer info & Show More button */}
        {pageSize !== -1 && filteredAndSortedOrders.length > pageSize && (
          <div className="p-4 bg-[#f9f9f7] border-t border-[#f0f0eb] text-center">
            <button
              id="btn-show-more-orders"
              onClick={() => setPageSize(prev => prev + 15)}
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full bg-white text-[#5A5A40] border border-[#5A5A40] font-bold text-xs hover:bg-[#5A5A40] hover:text-white shadow-xs transition-all"
            >
              <span>Ver mais pedidos (+15)</span>
            </button>
            <p className="text-[11px] text-[#8a8a78] mt-2">
              Você pode alterar o limite na opção "Mostrar" acima.
            </p>
          </div>
        )}
      </div>

    </div>
  );
};
