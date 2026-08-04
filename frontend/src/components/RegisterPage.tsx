import React, { useState } from 'react';
import { Store, CategoryType } from '../types';
import { registerAccount, createStore } from '../api/client';
import { 
  User, 
  Mail, 
  Lock, 
  Store as StoreIcon, 
  Phone, 
  MapPin, 
  Image as ImageIcon, 
  ArrowRight, 
  CheckCircle2, 
  AlertCircle,
  Building2,
  Sparkles
} from 'lucide-react';

interface RegisterPageProps {
  onComplete: () => void;
  onGoToLogin: () => void;
}

export const RegisterPage: React.FC<RegisterPageProps> = ({ onComplete, onGoToLogin }) => {
  const [step, setStep] = useState<1 | 2>(1);

  // Step 1: Account State
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [street, setStreet] = useState('');
  const [number, setNumber] = useState('');
  const [neighborhood, setNeighborhood] = useState('');
  const [cep, setCep] = useState('');

  // Step 2: Store State
  const [storeName, setStoreName] = useState('');
  const [storeDescription, setStoreDescription] = useState('');
  const [storeTelephone, setStoreTelephone] = useState('');
  const [storeCategory, setStoreCategory] = useState<CategoryType>('VESTUARIO');
  const [storeImage, setStoreImage] = useState<string>('');

  // Status states
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Image Upload Handler
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setStoreImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Step 1 Submit: Account Registration (POST /api/account/register)
  const handleAccountSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!email || !username || !password) {
      setErrorMsg('Por favor, preencha todos os campos obrigatórios da conta.');
      return;
    }

    setIsSubmitting(true);
    try {
      await registerAccount({
        email,
        username,
        password,
        address: {
          street,
          number: number || '1',
          neighborhood,
          cep
        }
      });
      // Advance to Step 2 (Store Creation)
      setStep(2);
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao registrar conta. Tente novamente.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Step 2 Submit: Store Creation (POST /api/store)
  const handleStoreSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!storeName) {
      setErrorMsg('Por favor, informe o nome da sua loja.');
      return;
    }

    setIsSubmitting(true);
    try {
      await createStore({
        name: storeName,
        description: storeDescription,
        telephone: storeTelephone,
        category: storeCategory,
        image: storeImage
      });
      // Complete registration and go to dashboard
      onComplete();
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao cadastrar loja. Tente novamente.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#fdfdfb] flex flex-col justify-center items-center p-4 sm:p-6 text-[#3d3d33]">
      
      {/* Background Orbs */}
      <div className="fixed -top-24 -left-24 w-96 h-96 bg-[#f0f0eb] rounded-full blur-3xl opacity-60 pointer-events-none" />
      <div className="fixed -bottom-24 -right-24 w-96 h-96 bg-[#e5e5df]/50 rounded-full blur-3xl opacity-60 pointer-events-none" />

      <div className="w-full max-w-lg relative z-10 space-y-6">
        
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-3xl bg-[#5A5A40] text-white shadow-md border border-[#e5e5df] mx-auto">
            <Building2 className="w-7 h-7 text-[#f5f5f0]" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-serif font-extrabold text-[#3d3d33] tracking-tight">
            Criar Nova Conta & Loja
          </h1>
          <p className="text-xs sm:text-sm text-[#8a8a78] font-medium">
            Cadastre seu perfil de lojista e publique sua loja no NextDoor
          </p>
        </div>

        {/* Step Indicator */}
        <div className="bg-white p-3 rounded-2xl border border-[#e5e5df] shadow-xs flex items-center justify-between">
          <div className={`flex items-center gap-2 px-3 py-1.5 rounded-xl font-bold text-xs transition-colors ${
            step === 1 ? 'bg-[#5A5A40] text-white' : 'bg-emerald-100 text-emerald-900 border border-emerald-200'
          }`}>
            {step === 2 ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-700" />
            ) : (
              <span className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center text-[10px]">1</span>
            )}
            <span>1. Registrar Conta</span>
          </div>

          <div className="w-6 h-0.5 bg-[#e5e5df]" />

          <div className={`flex items-center gap-2 px-3 py-1.5 rounded-xl font-bold text-xs transition-colors ${
            step === 2 ? 'bg-[#5A5A40] text-white' : 'bg-[#f0f0eb] text-[#8a8a78]'
          }`}>
            <span className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center text-[10px]">2</span>
            <span>2. Cadastrar Loja</span>
          </div>
        </div>

        {/* Form Container */}
        <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e5e5df] shadow-xs space-y-6">
          
          {errorMsg && (
            <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-800 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* STEP 1: Account Registration */}
          {step === 1 && (
            <form onSubmit={handleAccountSubmit} className="space-y-4">
              <div className="border-b border-[#f0f0eb] pb-3">
                <h2 className="font-serif font-bold text-base text-[#3d3d33] flex items-center gap-2">
                  <User className="w-4 h-4 text-[#5A5A40]" />
                  <span>Passo 1: Dados da Conta Proprietária</span>
                </h2>
              </div>

              {/* Username */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">
                  Nome de Usuário <span className="text-rose-600">*</span>
                </label>
                <div className="relative">
                  <User className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                  <input
                    type="text"
                    required
                    placeholder="ex: joaostore"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                  />
                </div>
              </div>

              {/* Email */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">
                  E-mail de Acesso <span className="text-rose-600">*</span>
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                  <input
                    type="email"
                    required
                    placeholder="seu.email@exemplo.com"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                  />
                </div>
              </div>

              {/* Password */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">
                  Senha <span className="text-rose-600">*</span>
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                  <input
                    type="password"
                    required
                    placeholder="Mínimo 6 caracteres"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                  />
                </div>
              </div>

              {/* Optional Address Fields */}
              <div className="pt-2 border-t border-[#f0f0eb] space-y-3">
                <span className="text-[11px] font-bold text-[#8a8a78] uppercase tracking-wider flex items-center gap-1.5">
                  <MapPin className="w-3.5 h-3.5 text-[#5A5A40]" />
                  <span>Endereço Comercial (Opcional)</span>
                </span>

                <div className="grid grid-cols-3 gap-2">
                  <div className="col-span-2">
                    <input
                      type="text"
                      placeholder="Rua / Avenida"
                      value={street}
                      onChange={e => setStreet(e.target.value)}
                      className="w-full px-3.5 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33]"
                    />
                  </div>
                  <div>
                    <input
                      type="text"
                      placeholder="Nº"
                      value={number}
                      onChange={e => setNumber(e.target.value)}
                      className="w-full px-3.5 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33]"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    placeholder="Bairro"
                    value={neighborhood}
                    onChange={e => setNeighborhood(e.target.value)}
                    className="w-full px-3.5 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33]"
                  />
                  <input
                    type="text"
                    placeholder="CEP"
                    value={cep}
                    onChange={e => setCep(e.target.value)}
                    className="w-full px-3.5 py-2 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33]"
                  />
                </div>
              </div>

              {/* Submit Step 1 */}
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3 px-6 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-2 active:scale-98 disabled:opacity-50 mt-4"
              >
                {isSubmitting ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Registrando Conta...</span>
                  </>
                ) : (
                  <>
                    <span>Criar Conta e Prosseguir</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          )}

          {/* STEP 2: Store Creation */}
          {step === 2 && (
            <form onSubmit={handleStoreSubmit} className="space-y-4">
              <div className="border-b border-[#f0f0eb] pb-3">
                <h2 className="font-serif font-bold text-base text-[#3d3d33] flex items-center gap-2">
                  <StoreIcon className="w-4 h-4 text-[#5A5A40]" />
                  <span>Passo 2: Cadastrar Sua Loja</span>
                </h2>
              </div>

              {/* Logo Upload */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">Logo / Imagem da Loja</label>
                <div className="relative w-full h-32 bg-[#f9f9f7] rounded-2xl border-2 border-dashed border-[#e5e5df] overflow-hidden flex flex-col items-center justify-center group hover:border-[#5A5A40] transition-colors">
                  {storeImage ? (
                    <img src={storeImage} alt="Preview" className="w-full h-full object-cover" />
                  ) : (
                    <div className="text-center p-4">
                      <ImageIcon className="w-6 h-6 text-[#8a8a78] mx-auto" />
                      <span className="text-xs text-[#8a8a78] font-medium block">Clique para carregar logo</span>
                    </div>
                  )}
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageUpload}
                    className="absolute inset-0 opacity-0 cursor-pointer"
                  />
                </div>
              </div>

              {/* Store Name */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">
                  Nome da Loja <span className="text-rose-600">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Boutique Elegance, TechStore..."
                  value={storeName}
                  onChange={e => setStoreName(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-bold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                />
              </div>

              {/* Category & Phone */}
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
                    <option value="PETS">PETS</option>
                    <option value="LIVRARIA">LIVRARIA</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-[#3d3d33]">Telefone / WhatsApp</label>
                  <div className="relative">
                    <Phone className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                    <input
                      type="text"
                      placeholder="(11) 99999-8888"
                      value={storeTelephone}
                      onChange={e => setStoreTelephone(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                    />
                  </div>
                </div>
              </div>

              {/* Description */}
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-[#3d3d33]">Descrição da Loja</label>
                <textarea
                  rows={2}
                  placeholder="Resumo dos produtos e diferenciais da loja..."
                  value={storeDescription}
                  onChange={e => setStoreDescription(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-2xl border border-[#e5e5df] bg-[#f9f9f7] text-xs text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40]"
                />
              </div>

              {/* Submit Step 2 */}
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3 px-6 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-2 active:scale-98 disabled:opacity-50 mt-4"
              >
                {isSubmitting ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Cadastrando Loja...</span>
                  </>
                ) : (
                  <>
                    <Sparkles className="w-4 h-4 text-amber-200" />
                    <span>Concluir Cadastro e Abrir Painel</span>
                  </>
                )}
              </button>
            </form>
          )}

          {/* Go to Login Link */}
          <div className="text-center pt-2 border-t border-[#f0f0eb]">
            <p className="text-xs text-[#8a8a78]">
              Já possui uma conta registrada?{' '}
              <button
                type="button"
                onClick={onGoToLogin}
                className="font-bold text-[#5A5A40] hover:underline"
              >
                Fazer Login
              </button>
            </p>
          </div>

        </div>

      </div>
    </div>
  );
};
