import React, { useState } from 'react';
import { Store } from '../types';
import { loginAccount } from '../api/client';
import { Store as StoreIcon, Lock, Mail, Eye, EyeOff, LogIn, ArrowRight, CheckCircle2, ShieldCheck } from 'lucide-react';

interface LoginPageProps {
  store: Store | null;
  onLogin: (email: string) => void;
  onGoToRegister?: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ store, onLogin, onGoToRegister }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!email || !password) {
      setError('Por favor, preencha o e-mail e a senha.');
      return;
    }

    setIsLoading(true);

    try {
      await loginAccount({ email, password });
      setIsLoading(false);
      onLogin(email);
    } catch (err: any) {
      setIsLoading(false);
      setError(err.message || 'Erro ao realizar login. Verifique suas credenciais.');
    }
  };

  return (
    <div className="min-h-screen bg-[#fdfdfb] flex flex-col justify-center items-center p-4 sm:p-6 text-[#3d3d33]">
      
      {/* Background Decorative Circles */}
      <div className="fixed -top-24 -left-24 w-96 h-96 bg-[#f0f0eb] rounded-full blur-3xl opacity-60 pointer-events-none" />
      <div className="fixed -bottom-24 -right-24 w-96 h-96 bg-[#e5e5df]/50 rounded-full blur-3xl opacity-60 pointer-events-none" />

      <div className="w-full max-w-md relative z-10 space-y-6">
        
        {/* Header / Store Branding */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-3xl bg-[#5A5A40] text-white shadow-md border border-[#e5e5df] mx-auto overflow-hidden">
            {store?.image ? (
              <img src={store.image} alt={store.name || 'Logo da Loja'} className="w-full h-full object-cover" />
            ) : (
              <StoreIcon className="w-8 h-8 text-[#f5f5f0]" />
            )}
          </div>
          
          <div>
            <h1 className="text-2xl sm:text-3xl font-serif font-extrabold text-[#3d3d33] tracking-tight">
              {store?.name || 'Portal Lojista'}
            </h1>
            <p className="text-xs sm:text-sm text-[#8a8a78] mt-1 font-medium">
              Acesse o painel de controle para gerenciar pedidos e estoque
            </p>
          </div>
        </div>

        {/* Login Card */}
        <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e5e5df] shadow-xs space-y-6">
          
          <div className="flex items-center justify-between border-b border-[#f0f0eb] pb-4">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-[#5A5A40]" />
              <h2 className="font-serif font-bold text-base text-[#3d3d33]">Iniciar Sessão</h2>
            </div>
            <span className="text-[10px] font-bold text-[#5A5A40] bg-[#f0f0eb] px-2.5 py-1 rounded-full border border-[#e5e5df]">
              Acesso Restrito
            </span>
          </div>

          {error && (
            <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-800 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-600 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            
            {/* Email Field */}
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[#3d3d33]">
                E-mail de Acesso
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                <input
                  type="email"
                  required
                  placeholder="exemplo@loja.com.br"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40] transition-colors"
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="block text-xs font-bold text-[#3d3d33]">
                  Senha
                </label>
                <button
                  type="button"
                  onClick={() => alert('Para redefinir sua senha, entre em contato com o suporte da plataforma.')}
                  className="text-[11px] font-semibold text-[#5A5A40] hover:underline"
                >
                  Esqueceu a senha?
                </button>
              </div>
              <div className="relative">
                <Lock className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78]" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-10 py-2.5 rounded-full border border-[#e5e5df] bg-[#f9f9f7] text-xs font-semibold text-[#3d3d33] focus:outline-none focus:ring-1 focus:ring-[#5A5A40] focus:border-[#5A5A40] transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#8a8a78] hover:text-[#3d3d33]"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Remember Me */}
            <div className="flex items-center justify-between pt-1">
              <label className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="w-4 h-4 rounded border-[#e5e5df] text-[#5A5A40] focus:ring-[#5A5A40] accent-[#5A5A40]"
                />
                <span className="text-xs text-[#8a8a78] font-medium">Lembrar neste navegador</span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-6 rounded-full bg-[#5A5A40] hover:bg-[#4a4a34] text-white font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-2 active:scale-98 disabled:opacity-50 mt-2"
            >
              {isLoading ? (
                <>
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Autenticando...</span>
                </>
              ) : (
                <>
                  <span>Entrar no Painel</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {onGoToRegister && (
            <div className="text-center pt-4 border-t border-[#f0f0eb]">
              <p className="text-xs text-[#8a8a78]">
                Ainda não tem uma conta?{' '}
                <button
                  type="button"
                  onClick={onGoToRegister}
                  className="font-bold text-[#5A5A40] hover:underline"
                >
                  Cadastrar Conta & Loja
                </button>
              </p>
            </div>
          )}

        </div>

        {/* Footer info */}
        <p className="text-center text-xs text-[#8a8a78]">
          Integração com Endpoints NextDoor API • Todos os direitos reservados
        </p>

      </div>
    </div>
  );
};
