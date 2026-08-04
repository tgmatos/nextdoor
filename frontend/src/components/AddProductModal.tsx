import React, { useState } from 'react';
import { Product } from '../types';
import { useDialog } from '../hooks';
import { X, Package, Image as ImageIcon, Save, RefreshCw, AlertCircle } from 'lucide-react';

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddProduct: (product: Partial<Product>) => Promise<void>;
  isSubmitting: boolean;
}

export const AddProductModal: React.FC<AddProductModalProps> = ({
  isOpen,
  onClose,
  onAddProduct,
  isSubmitting
}) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState<number>(10);
  const [image, setImage] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const dialogRef = useDialog(onClose, isOpen);

  if (!isOpen) return null;

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!name.trim()) {
      setErrorMsg('O nome do produto é obrigatório.');
      return;
    }

    try {
      await onAddProduct({
        name,
        description,
        price: parseFloat(price) || 0,
        quantity: Number(quantity) || 0,
        image: image || "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300' viewBox='0 0 300 300'><rect width='100%' height='100%' fill='%2310b981'/><text x='50%' y='50%' font-size='20' fill='white' font-family='sans-serif' text-anchor='middle' dominant-baseline='middle'>Produto</text></svg>"
      });

      // Reset form
      setName('');
      setDescription('');
      setPrice('');
      setQuantity(10);
      setImage('');
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao cadastrar novo produto.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-[#3d3d33]/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div
        ref={dialogRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label="Cadastrar Novo Produto"
        className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden border border-[#e5e5df] animate-in fade-in zoom-in duration-200 focus:outline-none"
      >
        
        {/* Modal Header */}
        <div className="p-5 bg-white text-[#3d3d33] flex items-center justify-between border-b border-[#e5e5df]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-[#f0f0eb] border border-[#e5e5df] flex items-center justify-center text-[#5A5A40]">
              <Package className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-serif font-bold text-base text-[#3d3d33]">Cadastrar Novo Produto</h3>
              <p className="text-xs text-[#8a8a78]">Adicione o item ao catálogo e ajuste o estoque inicial</p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-[#f5f5f0] text-[#8a8a78] hover:text-[#3d3d33] hover:bg-[#ebebe5] flex items-center justify-center transition-colors"
            aria-label="Fechar modal de cadastro de produto"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {errorMsg && (
          <div className="p-3 bg-rose-50 border-b border-rose-200 text-rose-800 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="p-6 space-y-4 bg-[#fdfdfb]">
          
          {/* Image Uploader */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[#3d3d33]">Foto do Produto</label>
            <div className="relative w-full h-36 bg-[#f9f9f7] rounded-2xl border-2 border-dashed border-[#e5e5df] overflow-hidden flex flex-col items-center justify-center group hover:border-[#5A5A40] transition-colors">
              {image ? (
                <img src={image} alt="Preview" className="w-full h-full object-cover" />
              ) : (
                <div className="text-center p-3 space-y-1">
                  <ImageIcon className="w-6 h-6 text-[#8a8a78] mx-auto" />
                  <span className="text-xs text-[#8a8a78] font-medium block">Clique para carregar imagem</span>
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

          {/* Name */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[#3d3d33]">
              Nome do Produto <span className="text-rose-600">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="Ex: Camiseta Polo Algodão"
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
            />
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[#3d3d33]">Descrição</label>
            <textarea
              rows={2}
              placeholder="Descreva detalhes, material, dimensões..."
              value={description}
              onChange={e => setDescription(e.target.value)}
              className="w-full px-4 py-2.5 rounded-2xl border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
            />
          </div>

          {/* Price & Quantity */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[#3d3d33]">
                Preço (R$) <span className="text-rose-600">*</span>
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                placeholder="0.00"
                value={price}
                onChange={e => setPrice(e.target.value)}
                className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-serif font-bold text-[#5A5A40] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[#3d3d33]">
                Estoque Inicial <span className="text-rose-600">*</span>
              </label>
              <input
                type="number"
                min="0"
                required
                value={quantity}
                onChange={e => setQuantity(Number(e.target.value))}
                className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-bold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
              />
            </div>
          </div>

          {/* Modal Actions */}
          <div className="pt-4 border-t border-[#f0f0eb] flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 rounded-full bg-[#f5f5f0] hover:bg-[#ebebe5] text-[#5A5A40] font-bold text-xs border border-[#e5e5df] transition-colors"
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex items-center gap-1.5 px-6 py-2.5 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all disabled:opacity-50"
            >
              {isSubmitting ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-4 h-4" />}
              <span>Cadastrar Produto</span>
            </button>
          </div>

        </form>

      </div>
    </div>
  );
};
