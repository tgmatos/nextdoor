import React from 'react';
import { CheckCircle2, AlertCircle } from 'lucide-react';

export interface ToastData {
  message: string;
  type: 'success' | 'error';
}

interface ToastProps {
  toast: ToastData | null;
}

export const Toast: React.FC<ToastProps> = ({ toast }) => {
  if (!toast) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 animate-in fade-in slide-in-from-bottom-3 duration-200">
      <div className={`px-4 py-3 rounded-2xl shadow-xl flex items-center gap-3 border text-xs font-bold text-white ${
        toast.type === 'success' ? 'bg-[#5A5A40] border-[#4a4a34]' : 'bg-rose-700 border-rose-800'
      }`}>
        {toast.type === 'success' ? (
          <CheckCircle2 className="w-4 h-4 text-emerald-300 shrink-0" />
        ) : (
          <AlertCircle className="w-4 h-4 text-white shrink-0" />
        )}
        <span>{toast.message}</span>
      </div>
    </div>
  );
};
