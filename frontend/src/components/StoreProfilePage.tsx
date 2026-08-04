import React, { useState, useEffect } from 'react';
import { Store, Account, Address, CategoryType } from '../types';
import { 
  Store as StoreIcon, 
  User, 
  MapPin, 
  Phone, 
  Mail, 
  Save, 
  Image as ImageIcon, 
  RefreshCw, 
  Check, 
  AlertCircle, 
  Trash2,
  Lock,
  Plus
} from 'lucide-react';

interface StoreProfilePageProps {
  store: Store | null;
  account: Account | null;
  addresses: Address[];
  onUpdateStore: (data: Partial<Store>) => Promise<void>;
  onUpdateAccount: (data: Partial<Account>) => Promise<void>;
  onUpdateAddress: (id: string, data: Partial<Address>) => Promise<void>;
  onDeleteStore: () => Promise<void>;
  isLoading: boolean;
}

export const StoreProfilePage: React.FC<StoreProfilePageProps> = ({
  store,
  account,
  addresses,
  onUpdateStore,
  onUpdateAccount,
  onUpdateAddress,
  onDeleteStore,
  isLoading
}) => {
  // Store Form State
  const [storeName, setStoreName] = useState('');
  const [storeDescription, setStoreDescription] = useState('');
  const [storeTelephone, setStoreTelephone] = useState('');
  const [storeCategory, setStoreCategory] = useState<CategoryType>('VESTUARIO');
  const [storeImage, setStoreImage] = useState('');

  // Account Form State
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');

  // Address Edit State
  const [editingAddressId, setEditingAddressId] = useState<string | null>(null);
  const [addrNumber, setAddrNumber] = useState('');
  const [addrStreet, setAddrStreet] = useState('');
  const [addrNeighborhood, setAddrNeighborhood] = useState('');
  const [addrCep, setAddrCep] = useState('');

  // Status Alerts
  const [storeMsg, setStoreMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [accountMsg, setAccountMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [addrMsg, setAddrMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const [isSavingStore, setIsSavingStore] = useState(false);
  const [isSavingAccount, setIsSavingAccount] = useState(false);
  const [isSavingAddr, setIsSavingAddr] = useState(false);
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);

  // Sync Store State
  useEffect(() => {
    if (store) {
      setStoreName(store.name || '');
      setStoreDescription(store.description || '');
      setStoreTelephone(store.telephone || '');
      setStoreCategory(store.category || 'VESTUARIO');
      setStoreImage(store.image || '');
    }
  }, [store]);

  // Sync Account State
  useEffect(() => {
    if (account) {
      setUsername(account.username || '');
      setEmail(account.email || '');
    }
  }, [account]);

  // Handle Store Image Upload
  const handleStoreImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setStoreImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Submit Store Profile
  const handleSaveStore = async (e: React.FormEvent) => {
    e.preventDefault();
    setStoreMsg(null);
    setIsSavingStore(true);

    try {
      await onUpdateStore({
        name: storeName,
        description: storeDescription,
        telephone: storeTelephone,
        category: storeCategory,
        image: storeImage
      });
      setStoreMsg({ type: 'success', text: 'Perfil da loja atualizado com sucesso!' });
      setTimeout(() => setStoreMsg(null), 3000);
    } catch (err: any) {
      setStoreMsg({ type: 'error', text: err.message || 'Erro ao atualizar dados da loja.' });
    } finally {
      setIsSavingStore(false);
    }
  };

  // Submit Account Data
  const handleSaveAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    setAccountMsg(null);
    setIsSavingAccount(true);

    try {
      await onUpdateAccount({ username, email });
      setAccountMsg({ type: 'success', text: 'Dados da conta atualizados com sucesso!' });
      setTimeout(() => setAccountMsg(null), 3000);
    } catch (err: any) {
      setAccountMsg({ type: 'error', text: err.message || 'Erro ao atualizar conta.' });
    } finally {
      setIsSavingAccount(false);
    }
  };

  // Start editing address
  const startEditAddress = (addr: Address) => {
    setEditingAddressId(addr.id);
    setAddrNumber(addr.address_number || '');
    setAddrStreet(addr.street || '');
    setAddrNeighborhood(addr.neighborhood || '');
    setAddrCep(addr.cep || '');
  };

  // Save address edit
  const handleSaveAddress = async (id: string) => {
    setAddrMsg(null);
    setIsSavingAddr(true);

    try {
      await onUpdateAddress(id, {
        address_number: addrNumber,
        street: addrStreet,
        neighborhood: addrNeighborhood,
        cep: addrCep
      });
      setEditingAddressId(null);
      setAddrMsg({ type: 'success', text: 'Endereço atualizado com sucesso!' });
      setTimeout(() => setAddrMsg(null), 3000);
    } catch (err: any) {
      setAddrMsg({ type: 'error', text: err.message || 'Erro ao atualizar endereço.' });
    } finally {
      setIsSavingAddr(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      
      {/* Page Heading */}
      <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs">
        <h2 className="text-2xl font-serif font-bold text-[#3d3d33] tracking-tight flex items-center gap-2">
          <StoreIcon className="w-6 h-6 text-[#5A5A40]" />
          Perfil da Loja & Configurações de Conta
        </h2>
        <p className="text-xs text-[#8a8a78] mt-1">
          Gerencie as informações públicas da sua loja, contatos, categoria e os dados da conta proprietária.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* Left Column: Store Information */}
        <div className="lg:col-span-7 space-y-6">
          <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs space-y-5">
            <div className="flex items-center justify-between border-b border-[#f0f0eb] pb-3">
              <h3 className="font-serif font-bold text-lg text-[#3d3d33] flex items-center gap-2">
                <StoreIcon className="w-5 h-5 text-[#5A5A40]" />
                <span>Dados Públicos da Loja</span>
              </h3>
              <span className="text-[10px] font-mono text-[#5A5A40] bg-[#f0f0eb] px-2.5 py-1 rounded-full border border-[#e5e5df]">
                UUID: {store?.id || 'N/A'}
              </span>
            </div>

            {storeMsg && (
              <div className={`p-3.5 rounded-2xl text-xs flex items-center gap-2 ${
                storeMsg.type === 'success' ? 'bg-emerald-100/70 text-emerald-900 border border-emerald-200' : 'bg-rose-100/70 text-rose-900 border border-rose-200'
              }`}>
                {storeMsg.type === 'success' ? <Check className="w-4 h-4 text-emerald-600" /> : <AlertCircle className="w-4 h-4 text-rose-600" />}
                <span>{storeMsg.text}</span>
              </div>
            )}

            <form id="store-profile-form" onSubmit={handleSaveStore} className="space-y-4">
              
              {/* Store Image */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">Logo / Imagem da Loja</label>
                <div className="relative w-full h-40 bg-[#f9f9f7] rounded-2xl border-2 border-dashed border-[#e5e5df] overflow-hidden flex flex-col items-center justify-center group hover:border-[#5A5A40] transition-colors">
                  {storeImage ? (
                    <img src={storeImage} alt="Store logo" className="w-full h-full object-cover" />
                  ) : (
                    <div className="text-center p-3 space-y-1">
                      <ImageIcon className="w-6 h-6 text-[#8a8a78] mx-auto" />
                      <span className="text-xs text-[#8a8a78] font-medium block">Clique para alterar imagem</span>
                    </div>
                  )}
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleStoreImageChange}
                    className="absolute inset-0 opacity-0 cursor-pointer"
                  />
                </div>
              </div>

              {/* Store Name */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">Nome da Loja</label>
                <input
                  type="text"
                  required
                  value={storeName}
                  onChange={e => setStoreName(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-sm font-bold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
                />
              </div>

              {/* Description */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">Descrição</label>
                <textarea
                  rows={3}
                  value={storeDescription}
                  onChange={e => setStoreDescription(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-2xl border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40]"
                  placeholder="Resumo dos produtos e diferenciais da loja..."
                />
              </div>

              {/* Category & Telephone */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-[#3d3d33]">Categoria Comercial</label>
                  <select
                    value={storeCategory}
                    onChange={e => setStoreCategory(e.target.value as CategoryType)}
                    className="w-full py-2.5 px-4 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-bold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                  >
                    <option value="VESTUARIO">VESTUÁRIO</option>
                    <option value="ELETRONICOS">ELETRÔNICOS</option>
                    <option value="COSMETICOS">COSMÉTICOS</option>
                    <option value="PETS">PETS & ANIMAIS</option>
                    <option value="LIVRARIA">LIVRARIA & PAPELARIA</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-[#3d3d33]">Telefone / WhatsApp</label>
                  <div className="relative">
                    <Phone className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                    <input
                      type="text"
                      value={storeTelephone}
                      onChange={e => setStoreTelephone(e.target.value)}
                      placeholder="(11) 99999-8888"
                      className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                    />
                  </div>
                </div>
              </div>

              {/* Submit Store */}
              <div className="pt-3 flex justify-end">
                <button
                  type="submit"
                  disabled={isSavingStore}
                  className="inline-flex items-center gap-1.5 px-6 py-2.5 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all active:scale-95 disabled:opacity-50"
                >
                  {isSavingStore ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                  <span>Salvar Dados da Loja</span>
                </button>
              </div>

            </form>
          </div>
        </div>

        {/* Right Column: Account & Address Details */}
        <div className="lg:col-span-5 space-y-6">
          
          {/* Account Profile Box */}
          <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-[#f0f0eb] pb-3">
              <h3 className="font-serif font-bold text-base text-[#3d3d33] flex items-center gap-2">
                <User className="w-4 h-4 text-[#5A5A40]" />
                <span>Conta do Proprietário</span>
              </h3>
            </div>

            {accountMsg && (
              <div className={`p-3.5 rounded-2xl text-xs flex items-center gap-2 ${
                accountMsg.type === 'success' ? 'bg-emerald-100/70 text-emerald-900 border border-emerald-200' : 'bg-rose-100/70 text-rose-900 border border-rose-200'
              }`}>
                {accountMsg.type === 'success' ? <Check className="w-4 h-4 text-emerald-600" /> : <AlertCircle className="w-4 h-4 text-rose-600" />}
                <span>{accountMsg.text}</span>
              </div>
            )}

            <form onSubmit={handleSaveAccount} className="space-y-3">
              <div className="space-y-1">
                <label className="block text-xs font-bold text-[#3d3d33]">Nome de Usuário</label>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  className="w-full px-4 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-xs font-bold text-[#3d3d33]">E-mail da Conta</label>
                <div className="relative">
                  <Mail className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    className="w-full pl-9 pr-4 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end">
                <button
                  type="submit"
                  disabled={isSavingAccount}
                  className="inline-flex items-center gap-1.5 px-5 py-2 rounded-full bg-[#3d3d33] hover:bg-[#2c2c25] text-white font-bold text-xs shadow-xs transition-all disabled:opacity-50"
                >
                  {isSavingAccount ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                  <span>Atualizar Conta</span>
                </button>
              </div>
            </form>
          </div>

          {/* Owner Address List & Edit */}
          <div className="bg-white p-6 rounded-3xl border border-[#e5e5df] shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-[#f0f0eb] pb-3">
              <h3 className="font-serif font-bold text-base text-[#3d3d33] flex items-center gap-2">
                <MapPin className="w-4 h-4 text-[#5A5A40]" />
                <span>Endereços Cadastrados</span>
              </h3>
            </div>

            {addrMsg && (
              <div className={`p-3.5 rounded-2xl text-xs flex items-center gap-2 ${
                addrMsg.type === 'success' ? 'bg-emerald-100/70 text-emerald-900 border border-emerald-200' : 'bg-rose-100/70 text-rose-900 border border-rose-200'
              }`}>
                {addrMsg.type === 'success' ? <Check className="w-4 h-4 text-emerald-600" /> : <AlertCircle className="w-4 h-4 text-rose-600" />}
                <span>{addrMsg.text}</span>
              </div>
            )}

            {addresses.length === 0 ? (
              <p className="text-xs text-[#8a8a78] text-center py-3">Nenhum endereço cadastrado para esta conta.</p>
            ) : (
              <div className="space-y-3">
                {addresses.map((addr) => {
                  const isEditing = editingAddressId === addr.id;

                  if (isEditing) {
                    return (
                      <div key={addr.id} className="p-4 bg-[#f9f9f7] rounded-2xl border border-[#5A5A40] space-y-2 text-xs">
                        <div className="grid grid-cols-3 gap-2">
                          <div className="col-span-2">
                            <label className="text-[10px] font-bold text-[#8a8a78]">Rua</label>
                            <input
                              type="text"
                              value={addrStreet}
                              onChange={e => setAddrStreet(e.target.value)}
                              className="w-full px-3 py-1.5 rounded-xl border border-[#e5e5df] bg-white text-[#3d3d33] font-medium"
                            />
                          </div>
                          <div>
                            <label className="text-[10px] font-bold text-[#8a8a78]">Nº</label>
                            <input
                              type="text"
                              value={addrNumber}
                              onChange={e => setAddrNumber(e.target.value)}
                              className="w-full px-3 py-1.5 rounded-xl border border-[#e5e5df] bg-white text-[#3d3d33] font-medium"
                            />
                          </div>
                        </div>

                        <div className="grid grid-cols-2 gap-2">
                          <div>
                            <label className="text-[10px] font-bold text-[#8a8a78]">Bairro</label>
                            <input
                              type="text"
                              value={addrNeighborhood}
                              onChange={e => setAddrNeighborhood(e.target.value)}
                              className="w-full px-3 py-1.5 rounded-xl border border-[#e5e5df] bg-white text-[#3d3d33] font-medium"
                            />
                          </div>
                          <div>
                            <label className="text-[10px] font-bold text-[#8a8a78]">CEP</label>
                            <input
                              type="text"
                              value={addrCep}
                              onChange={e => setAddrCep(e.target.value)}
                              className="w-full px-3 py-1.5 rounded-xl border border-[#e5e5df] bg-white text-[#3d3d33] font-medium"
                            />
                          </div>
                        </div>

                        <div className="pt-2 flex justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => setEditingAddressId(null)}
                            className="px-3 py-1 rounded-full bg-[#f5f5f0] text-[#3d3d33] font-semibold border border-[#e5e5df]"
                          >
                            Cancelar
                          </button>
                          <button
                            type="button"
                            onClick={() => handleSaveAddress(addr.id)}
                            disabled={isSavingAddr}
                            className="px-4 py-1 rounded-full bg-[#5A5A40] text-white font-bold"
                          >
                            Salvar
                          </button>
                        </div>
                      </div>
                    );
                  }

                  return (
                    <div key={addr.id} className="p-3.5 bg-[#f9f9f7] rounded-2xl border border-[#e5e5df] text-xs flex items-center justify-between">
                      <div>
                        <p className="font-bold text-[#3d3d33]">{addr.street}, {addr.address_number}</p>
                        <p className="text-[#8a8a78]">{addr.neighborhood} — CEP: {addr.cep}</p>
                      </div>
                      <button
                        onClick={() => startEditAddress(addr)}
                        className="text-xs font-bold text-[#5A5A40] hover:underline px-2 py-1"
                      >
                        Editar
                      </button>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

        </div>

      </div>

    </div>
  );
};
