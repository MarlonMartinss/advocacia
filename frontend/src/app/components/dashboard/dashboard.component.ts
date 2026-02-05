import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ContratoRecente {
  numero: string;
  nome: string;
  tipo: string;
  status: 'EM_ANALISE' | 'ASSINATURA_PENDENTE' | 'FINALIZADO';
  valor: number | null;
  data: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  // Dados mock conforme a imagem de referência
  stats = {
    emAberto: 42,
    aguardandoAssinatura: 15,
    vencendo30Dias: 7
  };

  contratosRecentes: ContratoRecente[] = [
    {
      numero: 'CT-2024-089',
      nome: 'Empresa Alpha Ltda',
      tipo: 'Prestação de Serviços',
      status: 'EM_ANALISE',
      valor: 150000,
      data: '25/10/2024'
    },
    {
      numero: 'CT-2024-090',
      nome: 'João da Silva',
      tipo: 'Compra e Venda',
      status: 'ASSINATURA_PENDENTE',
      valor: 1200000,
      data: '24/10/2024'
    },
    {
      numero: 'CT-2024-091',
      nome: 'Beta Tech S.A',
      tipo: 'NDA',
      status: 'FINALIZADO',
      valor: null,
      data: '23/10/2024'
    }
  ];

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'EM_ANALISE': 'Em Análise',
      'ASSINATURA_PENDENTE': 'Assinatura Pendente',
      'FINALIZADO': 'Finalizado'
    };
    return labels[status] || status;
  }

  formatCurrency(value: number | null): string {
    if (value === null) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }
}
