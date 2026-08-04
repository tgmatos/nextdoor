import React, { useState } from 'react';
import { Order, OrderStatusType } from '../types';
import { formatDate, paymentMethodLabel } from '../utils';
import { useDialog } from '../hooks';
import { 
  X, 
  Copy, 
  Check, 
  User, 
  MapPin, 
  CreditCard, 
  AlertCircle,
  RefreshCw,
  ShoppingBag
} from 'lucide-react';

interface OrderDetailsPanelProps {
  order: Order | null;
  onClose: () => void;
  onUpdateStatus: (orderId: string, before: OrderStatusType, after: OrderStatusType) => Promise<void>;
  isUpdating: boolean;
}

export const OrderDetailsPanel: React.FC<OrderDetailsPanelProps> = ({
  order,
  onClose,
  onUpdateStatus,
  isUpdating
}) => {
  const [copied, setCopied] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const dialogRef = useDialog(onClose);

  if (!order) return null;

  const handleCopyUUID = () => {
    navigator.clipboard.writeText(order.id);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const currentStatus = order.status_order || order.status || 'ESPERANDO';

  const handleTransition = async (nextStatus: OrderStatusType) => {
    setActionError(null);
    try {
      await onUpdateStatus(order.id, currentStatus, nextStatus);
    } catch (err: any) {
      setActionError(err.message || 'Falha ao atualizar status do pedido');
    }
  };

  // Status Stepper Status Logic
  const getNextActions = (status: OrderStatusType) => {
    switch (status) {
      case 'ESPERANDO':
        return [
          { label: 'Aceitar Pedido', target: 'ACEITO' as OrderStatusType, color: 'bg-emerald-600 hover:bg-emerald-700 text-white' },
          { label: 'Recusar Pedido', target: 'RECUSADO' as OrderStatusType, color: 'bg-rose-600 hover:bg-rose-700 text-white' }
        ];
      case 'ACEITO':
        return [
          { label: 'Iniciar Preparação', target: 'PREPARACAO' as OrderStatusType, color: 'bg-indigo-600 hover:bg-indigo-700 text-white' },
          { label: 'Cancelar Pedido', target: 'CANCELADO' as OrderStatusType, color: 'bg-rose-600 hover:bg-rose-700 text-white' }
        ];
      case 'PREPARACAO':
        return [
          { label: 'Despachar (Em Rota)', target: 'ROTA' as OrderStatusType, color: 'bg-purple-600 hover:bg-purple-700 text-white' },
          { label: 'Cancelar Pedido', target: 'CANCELADO' as OrderStatusType, color: 'bg-rose-600 hover:bg-rose-700 text-white' }
        ];
      case 'ROTA':
        return [
          { label: 'Marcar como Concluído', target: 'CONCLUIDO' as OrderStatusType, color: 'bg-emerald-600 hover:bg-emerald-700 text-white' },
          { label: 'Cancelar Pedido', target: 'CANCELADO' as OrderStatusType, color: 'bg-rose-600 hover:bg-rose-700 text-white' }
        ];
      default:
        return [];
    }
  };

  const nextActions = getNextActions(currentStatus);

  const clientName = order.client?.username || 'Anônimo';
  const clientUsername = order.client?.username;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-900/50 backdrop-blur-xs flex justify-end transition-opacity">
      
      {/* Backdrop overlay */}
      <div 
        className="absolute inset-0"
        onClick={onClose}
      />

      {/* Slide-over Right Panel */}
      <div
        ref={dialogRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label="Detalhes do Pedido"
        className="relative w-full max-w-lg bg-white shadow-2xl h-full flex flex-col z-10 border-l border-slate-200/80 animate-in slide-in-from-right duration-200 focus:outline-none"
      >
        
        {/* Panel Header */}
        <div className="p-5 bg-white text-[#3d3d33] flex items-center justify-between border-b border-[#e5e5df]">
          <div>
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#5A5A40] bg-[#f0f0eb] px-2.5 py-0.5 rounded-full border border-[#e5e5df]">
              Detalhes do Pedido
            </span>
            <div className="flex items-center gap-2 mt-1.5">
              <h3 className="font-mono text-xs font-bold text-[#3d3d33] truncate max-w-[220px]" title={order.id}>
                {order.id}
              </h3>
              <button
                onClick={handleCopyUUID}
                className="text-[#8a8a78] hover:text-[#5A5A40] transition-colors p-1 rounded-lg hover:bg-[#f5f5f0]"
                title="Copiar UUID do pedido"
                aria-label="Copiar UUID do pedido"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              </button>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-[#f5f5f0] text-[#8a8a78] hover:text-[#3d3d33] hover:bg-[#ebebe5] flex items-center justify-center transition-colors"
            aria-label="Fechar painel de detalhes"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Action Error Alert */}
        {actionError && (
          <div className="p-3 bg-rose-50 border-b border-rose-200 text-rose-700 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-500 shrink-0" />
            <span>{actionError}</span>
          </div>
        )}

        {/* Panel Body Scrollable */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-[#fdfdfb]">
          
          {/* Status & Transitions */}
          <div className="bg-[#f9f9f7] p-4 rounded-2xl border border-[#e5e5df] space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-[#8a8a78] uppercase tracking-wider">Status Atual</span>
              <span className="font-bold text-xs uppercase px-3 py-1 rounded-full bg-[#5A5A40] text-white">
                {currentStatus}
              </span>
            </div>

            {/* Transition Buttons */}
            {nextActions.length > 0 ? (
              <div className="pt-2 border-t border-[#e5e5df] flex items-center gap-2">
                {nextActions.map((action) => (
                  <button
                    key={action.target}
                    onClick={() => handleTransition(action.target)}
                    disabled={isUpdating}
                    className={`flex-1 py-2 px-3 rounded-xl text-xs font-bold transition-all shadow-xs active:scale-95 disabled:opacity-50 flex items-center justify-center gap-1.5 ${
                      action.target === 'RECUSADO' || action.target === 'CANCELADO'
                        ? 'bg-rose-700 hover:bg-rose-800 text-white'
                        : 'bg-[#5A5A40] hover:bg-[#4a4a34] text-white'
                    }`}
                  >
                    {isUpdating && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                    <span>{action.label}</span>
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-xs text-[#8a8a78] text-center italic pt-1">
                {currentStatus === 'CONCLUIDO' && '✓ Este pedido já foi finalizado com sucesso.'}
                {currentStatus === 'CANCELADO' && '✕ Este pedido foi cancelado.'}
                {currentStatus === 'RECUSADO' && '✕ Este pedido foi recusado.'}
              </p>
            )}
          </div>

          {/* Customer Details Section */}
          <div className="space-y-2">
            <h4 className="text-[10px] font-bold uppercase tracking-wider text-[#5A5A40] flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-[#8a8a78]" />
              <span>Informações do Cliente</span>
            </h4>

            <div className="bg-white p-4 rounded-2xl border border-[#e5e5df] shadow-xs space-y-2.5 text-xs text-[#3d3d33]">
              <div className="flex justify-between items-center pb-2 border-b border-[#f0f0eb]">
                <span className="text-[#8a8a78] font-semibold">Nome do Cliente:</span>
                <span className="font-bold text-[#3d3d33]">{clientName}</span>
              </div>

              {order.client?.email && (
                <div className="flex justify-between items-center pb-2 border-b border-[#f0f0eb]">
                  <span className="text-[#8a8a78] font-semibold">E-mail:</span>
                  <span className="font-medium text-[#3d3d33]">{order.client.email}</span>
                </div>
              )}

              {clientUsername && clientUsername !== clientName && (
                <div className="flex justify-between items-center">
                  <span className="text-[#8a8a78] font-semibold">Usuário:</span>
                  <span className="font-mono text-[#5A5A40]">@{clientUsername}</span>
                </div>
              )}
            </div>
          </div>

          {/* Delivery Address Section if available */}
          {order.address && (
            <div className="space-y-2">
              <h4 className="text-[10px] font-bold uppercase tracking-wider text-[#5A5A40] flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5 text-[#8a8a78]" />
                <span>Endereço de Entrega</span>
              </h4>

              <div className="bg-white p-4 rounded-2xl border border-[#e5e5df] shadow-xs text-xs text-[#3d3d33] space-y-1">
                <p className="font-bold text-[#3d3d33]">
                  {order.address.street}, {order.address.address_number}
                </p>
                <p className="text-[#8a8a78]">
                  {order.address.neighborhood} — CEP: {order.address.cep}
                </p>
              </div>
            </div>
          )}

          {/* Timestamps & Payment Method */}
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-[#f9f9f7] p-3.5 rounded-2xl border border-[#e5e5df] text-xs">
              <span className="text-[10px] font-bold text-[#8a8a78] uppercase block">Data de Criação</span>
              <span className="font-semibold text-[#3d3d33] mt-1 block">{formatDate(order.inserted_at)}</span>
            </div>

            <div className="bg-[#f9f9f7] p-3.5 rounded-2xl border border-[#e5e5df] text-xs">
              <span className="text-[10px] font-bold text-[#8a8a78] uppercase block">Última Atualização</span>
              <span className="font-semibold text-[#3d3d33] mt-1 block">{formatDate(order.updated_at)}</span>
            </div>
          </div>

          <div className="bg-[#f9f9f7] p-3.5 rounded-2xl border border-[#e5e5df] text-xs flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-[#8a8a78]" />
              <span className="text-[#3d3d33] font-semibold">Forma de Pagamento:</span>
            </div>
            <span className="font-bold text-[#5A5A40] bg-white px-3 py-1 rounded-full border border-[#e5e5df]">
              {paymentMethodLabel(order.payment_method)}
            </span>
          </div>

          {/* Order Items Table */}
          <div className="space-y-2">
            <h4 className="text-[10px] font-bold uppercase tracking-wider text-[#5A5A40] flex items-center gap-1.5">
              <ShoppingBag className="w-3.5 h-3.5 text-[#8a8a78]" />
              <span>Itens do Pedido ({order.order_product?.length || 0})</span>
            </h4>

            <div className="bg-white rounded-2xl border border-[#e5e5df] shadow-xs overflow-hidden">
              {(!order.order_product || order.order_product.length === 0) ? (
                <div className="p-4 text-center text-xs text-[#8a8a78]">
                  Nenhum item especificado neste pedido.
                </div>
              ) : (
                <div className="divide-y divide-[#f0f0eb]">
                  {order.order_product.map((item, idx) => (
                    <div key={item.id || idx} className="p-3.5 flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-[#f5f5f0] border border-[#e5e5df] flex items-center justify-center shrink-0 overflow-hidden">
                        {item.image ? (
                          <img src={item.image} alt={item.name} className="w-full h-full object-cover" />
                        ) : (
                          <ShoppingBag className="w-5 h-5 text-[#8a8a78]" />
                        )}
                      </div>

                      <div className="flex-1 min-w-0">
                        <p className="font-bold text-xs text-[#3d3d33] truncate">{item.name}</p>
                        <p className="text-[10px] text-[#8a8a78] truncate">{item.description}</p>
                        <p className="text-xs text-[#8a8a78] mt-0.5">
                          {item.quantity || 1}x R$ {parseFloat(String(item.price || 0)).toFixed(2)}
                        </p>
                      </div>

                      <div className="text-right font-serif font-bold text-xs text-[#5A5A40]">
                        R$ {((item.quantity || 1) * (item.price || 0)).toFixed(2)}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Subtotal & Total Footer */}
              <div className="p-4 bg-[#f9f9f7] border-t border-[#e5e5df] flex items-center justify-between text-[#3d3d33]">
                <span className="font-bold text-xs uppercase tracking-wider text-[#8a8a78]">Total do Pedido:</span>
                <span className="font-serif font-extrabold text-lg text-[#5A5A40]">
                  R$ {parseFloat(order.total || '0').toFixed(2)}
                </span>
              </div>
            </div>
          </div>

        </div>

        {/* Panel Footer */}
        <div className="p-4 bg-[#f9f9f7] border-t border-[#e5e5df] flex items-center justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2.5 rounded-full bg-[#f5f5f0] hover:bg-[#ebebe5] text-[#5A5A40] font-bold text-xs border border-[#e5e5df] transition-colors"
          >
            Fechar Painel
          </button>
        </div>

      </div>
    </div>
  );
};
