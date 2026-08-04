import React, { useState, useEffect } from 'react';
import { Product } from '../types';
import { formatDate } from '../utils';
import { useDialog } from '../hooks';
import { 
  X, 
  Copy, 
  Check, 
  Plus, 
  Minus, 
  Save, 
  Trash2, 
  Image as ImageIcon,
  AlertCircle,
  RefreshCw
} from 'lucide-react';

interface ProductDetailsPanelProps {
  product: Product | null;
  onClose: () => void;
  onUpdateProduct: (id: string, updated: Partial<Product>) => Promise<void>;
  onDeleteProduct: (id: string) => Promise<void>;
  isUpdating: boolean;
}

export const ProductDetailsPanel: React.FC<ProductDetailsPanelProps> = ({
  product,
  onClose,
  onUpdateProduct,
  onDeleteProduct,
  isUpdating
}) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState<number>(0);
  const [image, setImage] = useState('');
  
  const [copied, setCopied] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);
  const dialogRef = useDialog(onClose);

  // Sync state when product changes
  useEffect(() => {
    if (product) {
      setName(product.name || '');
      setDescription(product.description || '');
      setPrice(String(product.price ?? 0));
      setQuantity(product.quantity ?? 0);
      setImage(product.image || '');
      setErrorMsg(null);
      setSuccessMsg(null);
      setShowConfirmDelete(false);
    }
  }, [product]);

  if (!product) return null;

  const handleCopyUUID = () => {
    navigator.clipboard.writeText(product.id);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);

    try {
      await onUpdateProduct(product.id, {
        name,
        description,
        price: parseFloat(price) || 0,
        quantity: Number(quantity) || 0,
        image
      });
      setSuccessMsg('Produto atualizado com sucesso!');
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao salvar alterações no produto.');
    }
  };

  const handleQuickStockChange = (delta: number) => {
    const nextQty = Math.max(0, quantity + delta);
    setQuantity(nextQty);
  };

  const handleDelete = async () => {
    try {
      await onDeleteProduct(product.id);
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao excluir o produto.');
    }
  };

  // Image file drop/pick handler
  const handleImageFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

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
        aria-label={`Detalhes do Produto ${product.name}`}
        className="relative w-full max-w-lg bg-white shadow-2xl h-full flex flex-col z-10 border-l border-slate-200/80 animate-in slide-in-from-right duration-200 focus:outline-none"
      >
        
        {/* Panel Header */}
        <div className="p-5 bg-white text-[#3d3d33] flex items-center justify-between border-b border-[#e5e5df]">
          <div>
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#5A5A40] bg-[#f0f0eb] px-2.5 py-0.5 rounded-full border border-[#e5e5df]">
              Detalhes do Produto
            </span>
            <div className="flex items-center gap-2 mt-1.5">
              <h3 className="font-serif font-bold text-base text-[#3d3d33] truncate max-w-[220px]" title={product.name}>
                {product.name}
              </h3>
              <button
                onClick={handleCopyUUID}
                className="text-[#8a8a78] hover:text-[#5A5A40] transition-colors p-1 rounded-lg hover:bg-[#f5f5f0]"
                title="Copiar UUID do produto"
                aria-label="Copiar UUID do produto"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              </button>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-[#f5f5f0] text-[#8a8a78] hover:text-[#3d3d33] hover:bg-[#ebebe5] flex items-center justify-center transition-colors"
            aria-label="Fechar painel de detalhes do produto"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Feedback alerts */}
        {errorMsg && (
          <div className="p-3 bg-rose-50 border-b border-rose-200 text-rose-700 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-500 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="p-3 bg-emerald-50 border-b border-emerald-200 text-emerald-700 text-xs flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-500 shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Form Body */}
        <form id="product-detail-form" onSubmit={handleSave} className="flex-1 overflow-y-auto p-6 space-y-5 bg-[#fdfdfb]">
          
          {/* Product Image Preview & Uploader */}
          <div className="space-y-2">
            <label className="block text-[10px] font-bold uppercase tracking-wider text-[#8a8a78]">
              Imagem do Produto
            </label>
            <div className="relative w-full h-44 bg-[#f9f9f7] rounded-2xl border-2 border-dashed border-[#e5e5df] overflow-hidden flex flex-col items-center justify-center group hover:border-[#5A5A40] transition-colors">
              {image ? (
                <>
                  <img src={image} alt={product.name} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-[#3d3d33]/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white text-xs font-semibold gap-2">
                    <ImageIcon className="w-4 h-4" />
                    <span>Alterar Imagem</span>
                  </div>
                </>
              ) : (
                <div className="text-center p-4 space-y-2">
                  <ImageIcon className="w-8 h-8 text-[#8a8a78] mx-auto" />
                  <span className="text-xs text-[#8a8a78] font-medium block">Clique para escolher imagem</span>
                </div>
              )}
              <input
                type="file"
                accept="image/*"
                onChange={handleImageFileChange}
                className="absolute inset-0 opacity-0 cursor-pointer"
              />
            </div>
          </div>

          {/* Product Name Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[#3d3d33]">
              Nome do Produto <span className="text-rose-600">*</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
            />
          </div>

          {/* Product Description */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[#3d3d33]">
              Descrição
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={e => setDescription(e.target.value)}
              className="w-full px-4 py-2.5 rounded-2xl border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
              placeholder="Descreva características, especificações e tamanho..."
            />
          </div>

          {/* Price & Quantity Controls Row */}
          <div className="grid grid-cols-2 gap-4">
            
            {/* Price */}
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[#3d3d33]">
                Preço Unitário (R$) <span className="text-rose-600">*</span>
              </label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-xs font-bold text-[#8a8a78]">R$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  required
                  value={price}
                  onChange={e => setPrice(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm font-serif font-bold text-[#5A5A40] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
                />
              </div>
            </div>

            {/* Quantity in Stock */}
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[#3d3d33]">
                Quantidade em Estoque <span className="text-rose-600">*</span>
              </label>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={() => handleQuickStockChange(-1)}
                  className="w-9 h-9 rounded-full bg-[#f5f5f0] hover:bg-[#ebebe5] text-[#5A5A40] border border-[#e5e5df] flex items-center justify-center font-bold"
                  title="Diminuir 1"
                >
                  <Minus className="w-3.5 h-3.5" />
                </button>

                <input
                  type="number"
                  min="0"
                  required
                  value={quantity}
                  onChange={e => setQuantity(Number(e.target.value))}
                  className="w-full py-2 px-2 text-center rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm font-extrabold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                />

                <button
                  type="button"
                  onClick={() => handleQuickStockChange(1)}
                  className="w-9 h-9 rounded-full bg-[#f5f5f0] hover:bg-[#ebebe5] text-[#5A5A40] border border-[#e5e5df] flex items-center justify-center font-bold"
                  title="Adicionar 1"
                >
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

          </div>

          {/* Quick Stock Shortcuts */}
          <div className="flex items-center gap-2 pt-1">
            <span className="text-[10px] font-bold text-[#8a8a78] uppercase">Ajuste Rápido:</span>
            <button
              type="button"
              onClick={() => handleQuickStockChange(+5)}
              className="px-3 py-1 rounded-full bg-[#f0f0eb] text-[#5A5A40] hover:bg-[#ebebe5] font-bold text-[11px] border border-[#e5e5df]"
            >
              +5
            </button>
            <button
              type="button"
              onClick={() => handleQuickStockChange(+10)}
              className="px-3 py-1 rounded-full bg-[#f0f0eb] text-[#5A5A40] hover:bg-[#ebebe5] font-bold text-[11px] border border-[#e5e5df]"
            >
              +10
            </button>
            <button
              type="button"
              onClick={() => setQuantity(0)}
              className="px-3 py-1 rounded-full bg-rose-100 text-rose-800 hover:bg-rose-200 font-bold text-[11px] border border-rose-200"
            >
              Zerar Estoque
            </button>
          </div>

          {/* UUID & Timestamps Metadata */}
          <div className="bg-[#f9f9f7] p-4 rounded-2xl border border-[#e5e5df] space-y-2 text-xs text-[#8a8a78]">
            <div className="flex justify-between items-center">
              <span className="text-[#8a8a78] font-medium">UUID do Produto:</span>
              <span className="font-mono text-[11px] font-semibold text-[#5A5A40] select-all">{product.id}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-[#8a8a78] font-medium">Data de Cadastro:</span>
              <span className="text-[#3d3d33] font-semibold">{formatDate(product.inserted_at)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-[#8a8a78] font-medium">Última Modificação:</span>
              <span className="text-[#3d3d33] font-semibold">{formatDate(product.updated_at)}</span>
            </div>
          </div>

        </form>

        {/* Panel Footer Actions */}
        <div className="p-4 bg-[#f9f9f7] border-t border-[#e5e5df] space-y-2">
          {showConfirmDelete ? (
            <div className="bg-rose-50 p-4 rounded-2xl border border-rose-200 text-center space-y-2">
              <p className="text-xs font-bold text-rose-900">
                Tem certeza que deseja excluir "{product.name}"?
              </p>
              <div className="flex items-center gap-2 justify-center">
                <button
                  type="button"
                  onClick={handleDelete}
                  className="px-4 py-2 rounded-full bg-rose-700 text-white font-bold text-xs hover:bg-rose-800 shadow-xs"
                >
                  Sim, Excluir
                </button>
                <button
                  type="button"
                  onClick={() => setShowConfirmDelete(false)}
                  className="px-4 py-2 rounded-full bg-[#f5f5f0] text-[#3d3d33] font-bold text-xs hover:bg-[#ebebe5] border border-[#e5e5df]"
                >
                  Cancelar
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-center justify-between gap-3">
              <button
                type="button"
                onClick={() => setShowConfirmDelete(true)}
                className="inline-flex items-center gap-1.5 px-3 py-2 rounded-full text-rose-700 hover:bg-rose-100/60 font-bold text-xs transition-colors"
              >
                <Trash2 className="w-4 h-4" />
                <span>Excluir</span>
              </button>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-2.5 rounded-full bg-[#f5f5f0] hover:bg-[#ebebe5] text-[#5A5A40] font-bold text-xs border border-[#e5e5df] transition-colors"
                >
                  Cancelar
                </button>

                <button
                  type="submit"
                  form="product-detail-form"
                  disabled={isUpdating}
                  className="inline-flex items-center gap-1.5 px-6 py-2.5 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all disabled:opacity-50"
                >
                  {isUpdating ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-4 h-4" />}
                  <span>Salvar Alterações</span>
                </button>
              </div>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
