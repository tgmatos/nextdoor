export function paymentMethodLabel(method?: string): string {
  switch (method) {
    case 'CC':
      return 'Cartão de Crédito';
    case 'CD':
      return 'Cartão de Débito';
    case 'PIX':
      return 'Pix';
    case 'DINHEIRO':
      return 'Dinheiro';
    default:
      return method || 'Pix';
  }
}

export function formatDate(dateStr?: string): string {
  if (!dateStr) return 'N/A';
  try {
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(new Date(dateStr));
  } catch {
    return dateStr;
  }
}
