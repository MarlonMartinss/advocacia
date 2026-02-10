import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ContratoStatus = 'DRAFT' | 'FINAL';

export interface ParcelaItem {
  numero: number;
  vencimento: string; // ISO date (YYYY-MM-DD)
  valor: number;
}

export interface VendedorData {
  id?: number;
  ordem?: number;
  nome?: string;
  documento?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  socioNome?: string;
  socioCpf?: string;
  socioNacionalidade?: string;
  socioProfissao?: string;
  socioEstadoCivil?: string;
  socioRegimeBens?: string;
  socioRg?: string;
  socioCnh?: string;
  socioEmail?: string;
  socioTelefone?: string;
  socioEndereco?: string;
}

export interface CompradorData {
  id?: number;
  ordem?: number;
  nome?: string;
  documento?: string;
  nacionalidade?: string;
  profissao?: string;
  estadoCivil?: string;
  regimeBens?: string;
  rg?: string;
  cnh?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  conjugeNome?: string;
  conjugeCpf?: string;
  conjugeNacionalidade?: string;
  conjugeProfissao?: string;
  conjugeRg?: string;
}

export interface ContratoRequest {
  paginaAtual?: number;

  vendedores?: VendedorData[];
  compradores?: CompradorData[];

  // Página 3: Imóvel Objeto
  imovelMatricula?: string;
  imovelLivro?: string;
  imovelOficio?: string;
  imovelProprietario?: string;
  imovelMomentoPosse?: string;
  imovelPrazoTransferencia?: string;
  imovelPrazoEscritura?: string;
  imovelDescricao?: string;

  // Página 3: Imóvel Permuta
  permutaImovelMatricula?: string;
  permutaImovelLivro?: string;
  permutaImovelOficio?: string;
  permutaImovelProprietario?: string;
  permutaImovelMomentoPosse?: string;
  permutaImovelPrazoTransferencia?: string;
  permutaImovelPrazoEscritura?: string;
  permutaImovelDescricao?: string;

  // Página 3: Veículo Permuta
  veiculoMarca?: string;
  veiculoAno?: string;
  veiculoModelo?: string;
  veiculoPlaca?: string;
  veiculoChassi?: string;
  veiculoCor?: string;
  veiculoMotor?: string;
  veiculoRenavam?: string;
  veiculoDataEntrega?: string;
  veiculoKm?: number;

  // Página 4: Negócio
  negocioValorTotal?: number;
  negocioValorEntrada?: number;
  negocioFormaPagamento?: string;
  negocioNumParcelas?: number;
  negocioValorParcela?: number;
  negocioVencimentos?: string;
  negocioValorImovelPermuta?: number;
  negocioValorVeiculoPermuta?: number;
  negocioValorFinanciamento?: number;
  negocioPrazoPagamento?: string;
  negocioDataPrimeiraParcela?: string;
  parcelas?: ParcelaItem[];

  // Página 4: Conta Bancária
  contaTitular?: string;
  contaBanco?: string;
  contaAgencia?: string;
  contaPix?: string;

  // Página 4: Honorários
  honorariosValor?: number;
  honorariosFormaPagamento?: string;
  honorariosDataPagamento?: string;

  // Página 4: Observações e Assinaturas
  observacoes?: string;
  dataContrato?: string;
  assinaturaCorretor?: string;
  assinaturaAgenciador?: string;
  assinaturaGestor?: string;
}

export interface ContratoResponse extends ContratoRequest {
  id: number;
  status: ContratoStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface ContratoAnexo {
  id: number;
  contratoId: number;
  nomeOriginal: string;
  nomeArquivo: string;
  tipoMime: string;
  tamanho: number;
  createdAt: string;
}

export interface FieldChange {
  path: string;
  oldValue: any;
  newValue: any;
  /** Rótulo amigável (ex.: "Vendedor - Nome") */
  label?: string;
  /** Valor antigo formatado para exibição (ex.: "Rascunho") */
  displayOld?: string;
  /** Valor novo formatado para exibição */
  displayNew?: string;
}

export interface ContratoAlteracao {
  id: number;
  contratoId: number;
  changedAt: string;
  username: string;
  changes: FieldChange[];
}

@Injectable({
  providedIn: 'root'
})
export class ContratoService {
  private readonly apiUrl = `${environment.apiUrl}/contratos`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ContratoResponse[]> {
    return this.http.get<ContratoResponse[]>(this.apiUrl);
  }

  getById(id: number): Observable<ContratoResponse> {
    return this.http.get<ContratoResponse>(`${this.apiUrl}/${id}`);
  }

  create(data?: ContratoRequest): Observable<ContratoResponse> {
    return this.http.post<ContratoResponse>(this.apiUrl, data || {});
  }

  update(id: number, data: ContratoRequest): Observable<ContratoResponse> {
    return this.http.put<ContratoResponse>(`${this.apiUrl}/${id}`, data);
  }

  updateVendedores(id: number, vendedores: VendedorData[]): Observable<ContratoResponse> {
    return this.http.put<ContratoResponse>(`${this.apiUrl}/${id}/vendedores`, vendedores);
  }

  updateCompradores(id: number, compradores: CompradorData[]): Observable<ContratoResponse> {
    return this.http.put<ContratoResponse>(`${this.apiUrl}/${id}/compradores`, compradores);
  }

  finalizar(id: number): Observable<ContratoResponse> {
    return this.http.post<ContratoResponse>(`${this.apiUrl}/${id}/finalizar`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ========== HISTÓRICO ==========

  getHistorico(id: number): Observable<ContratoAlteracao[]> {
    return this.http.get<ContratoAlteracao[]>(`${this.apiUrl}/${id}/historico`);
  }

  // ========== ANEXOS ==========

  getAnexos(contratoId: number): Observable<ContratoAnexo[]> {
    return this.http.get<ContratoAnexo[]>(`${this.apiUrl}/${contratoId}/anexos`);
  }

  uploadAnexo(contratoId: number, file: File): Observable<ContratoAnexo> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<ContratoAnexo>(`${this.apiUrl}/${contratoId}/anexos`, formData);
  }

  deleteAnexo(contratoId: number, anexoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${contratoId}/anexos/${anexoId}`);
  }

  getAnexoDownloadUrl(contratoId: number, anexoId: number): string {
    return `${this.apiUrl}/${contratoId}/anexos/${anexoId}/download`;
  }
}
