import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray } from '@angular/forms';
import { ContratoService, ContratoResponse, ContratoRequest, ContratoAnexo, VendedorData, CompradorData, ContratoAlteracao, FieldChange, ParcelaItem } from '../../services/contrato.service';

interface Step {
  number: number;
  title: string;
  subtitle: string;
}

@Component({
  selector: 'app-contratos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contratos.component.html',
  styleUrl: './contratos.component.scss'
})
export class ContratosComponent implements OnInit {
  contratoForm!: FormGroup;
  currentPage = 1;
  totalPages = 4;
  contratoId: number | null = null;
  isSaving = false;
  isLoading = false;
  error: string | null = null;
  successMessage: string | null = null;

  steps: Step[] = [
    { number: 1, title: 'Vendedor', subtitle: 'Vendedores + Sócios' },
    { number: 2, title: 'Comprador', subtitle: 'Compradores + Cônjuges' },
    { number: 3, title: 'Imóveis', subtitle: 'Imóvel + Permuta + Veículo' },
    { number: 4, title: 'Negócio', subtitle: 'Valores + Honorários' }
  ];

  // Lista de contratos
  contratos: ContratoResponse[] = [];
  showList = true;
  editingContrato: ContratoResponse | null = null;

  // Tipo de documento (CPF ou CNPJ) por indice
  vendedorDocTipos: ('CPF' | 'CNPJ')[] = ['CNPJ'];
  compradorDocTipos: ('CPF' | 'CNPJ')[] = ['CPF'];

  // Accordion collapsed state
  vendedorCollapsed: boolean[] = [false];
  compradorCollapsed: boolean[] = [false];

  // Parcelamento: sempre disponível quando há saldo a pagar; lista carregada do backend ao editar
  parcelasList: ParcelaItem[] = [];
  /** Último saldo usado para gerar parcelas (para aviso de recálculo). */
  ultimoSaldoParcelado: number | null = null;
  /** Exibir aviso "Parcelas recalculadas pelo novo saldo." */
  parcelasRecalculadasAviso = false;
  private skipNextSaldoCheck = false;

  // Histórico de alterações
  showHistoricoModal = false;
  historicoAlteracoes: ContratoAlteracao[] = [];
  historicoLoading = false;
  historicoError: string | null = null;
  historicoContratoId: number | null = null;

  // Anexos
  anexos: ContratoAnexo[] = [];
  isUploading = false;

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  @ViewChild('cameraInput') cameraInput!: ElementRef<HTMLInputElement>;

  constructor(
    private fb: FormBuilder,
    private contratoService: ContratoService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.setupParcelamentoSaldoWatch();
    this.loadContratos();
  }

  /** Quando valores do negócio mudam, detecta recálculo de parcelas para exibir aviso. */
  private setupParcelamentoSaldoWatch(): void {
    const checkRecalc = () => {
      if (this.skipNextSaldoCheck) {
        this.skipNextSaldoCheck = false;
        return;
      }
      if (this.currentPage !== 4) return;
      const saldoAtual = this.getSaldoAPagar();
      const parcelas = this.getParcelasList();
      if (parcelas.length > 0 && this.ultimoSaldoParcelado != null && saldoAtual !== this.ultimoSaldoParcelado) {
        this.parcelasRecalculadasAviso = true;
      }
      this.ultimoSaldoParcelado = parcelas.length > 0 ? saldoAtual : null;
    };
    this.contratoForm.get('negocioValorTotal')?.valueChanges.subscribe(checkRecalc);
    this.contratoForm.get('negocioValorImovelPermuta')?.valueChanges.subscribe(checkRecalc);
    this.contratoForm.get('negocioValorVeiculoPermuta')?.valueChanges.subscribe(checkRecalc);
  }

  initForm(): void {
    this.contratoForm = this.fb.group({
      vendedores: this.fb.array([this.createVendedorGroup()]),
      compradores: this.fb.array([this.createCompradorGroup()]),

      // Página 3: Imóvel Objeto
      imovelMatricula: [''],
      imovelLivro: [''],
      imovelOficio: [''],
      imovelProprietario: [''],
      imovelMomentoPosse: [''],
      imovelPrazoTransferencia: [''],
      imovelPrazoEscritura: [''],
      imovelDescricao: [''],

      // Página 3: Imóvel Permuta
      permutaImovelMatricula: [''],
      permutaImovelLivro: [''],
      permutaImovelOficio: [''],
      permutaImovelProprietario: [''],
      permutaImovelMomentoPosse: [''],
      permutaImovelPrazoTransferencia: [''],
      permutaImovelPrazoEscritura: [''],
      permutaImovelDescricao: [''],

      // Página 3: Veículo Permuta
      veiculoMarca: [''],
      veiculoAno: [''],
      veiculoModelo: [''],
      veiculoPlaca: [''],
      veiculoChassi: [''],
      veiculoCor: [''],
      veiculoMotor: [''],
      veiculoRenavam: [''],
      veiculoDataEntrega: [''],
      veiculoKm: [null],

      // Página 4: Negócio
      negocioValorTotal: [null],
      negocioValorEntrada: [null],
      negocioFormaPagamento: [''],
      negocioNumParcelas: [null],
      negocioValorParcela: [null],
      negocioVencimentos: [''],
      negocioValorImovelPermuta: [null],
      negocioValorVeiculoPermuta: [null],
      negocioValorFinanciamento: [null],
      negocioPrazoPagamento: [''],
      negocioDataPrimeiraParcela: [null as string | null],

      // Página 4: Conta Bancária
      contaTitular: [''],
      contaBanco: [''],
      contaAgencia: [''],
      contaPix: [''],

      // Página 4: Honorários
      honorariosValor: [null],
      honorariosFormaPagamento: [''],
      honorariosDataPagamento: [''],

      // Página 4: Observações e Assinaturas
      observacoes: [''],
      dataContrato: [''],
      assinaturaCorretor: [''],
      assinaturaAgenciador: [''],
      assinaturaGestor: ['']
    });
  }

  // ========== VENDEDOR FORMARRAY ==========

  createVendedorGroup(): FormGroup {
    return this.fb.group({
      nome: [''],
      documento: [''],
      email: [''],
      telefone: [''],
      endereco: [''],
      socioNome: [''],
      socioCpf: [''],
      socioNacionalidade: [''],
      socioProfissao: [''],
      socioEstadoCivil: [''],
      socioRegimeBens: [''],
      socioRg: [''],
      socioCnh: [''],
      socioEmail: [''],
      socioTelefone: [''],
      socioEndereco: ['']
    });
  }

  get vendedoresArray(): FormArray {
    return this.contratoForm.get('vendedores') as FormArray;
  }

  addVendedor(): void {
    this.vendedoresArray.push(this.createVendedorGroup());
    this.vendedorDocTipos.push('CNPJ');
    this.vendedorCollapsed.push(false);
    // Collapse all others
    for (let i = 0; i < this.vendedorCollapsed.length - 1; i++) {
      this.vendedorCollapsed[i] = true;
    }
  }

  removeVendedor(index: number): void {
    if (this.vendedoresArray.length <= 1) return;
    this.vendedoresArray.removeAt(index);
    this.vendedorDocTipos.splice(index, 1);
    this.vendedorCollapsed.splice(index, 1);
  }

  toggleVendedor(index: number): void {
    this.vendedorCollapsed[index] = !this.vendedorCollapsed[index];
  }

  getVendedorLabel(index: number): string {
    const nome = this.vendedoresArray.at(index).get('nome')?.value;
    return nome ? `Vendedor ${index + 1} — ${nome}` : `Vendedor ${index + 1}`;
  }

  // ========== COMPRADOR FORMARRAY ==========

  createCompradorGroup(): FormGroup {
    return this.fb.group({
      nome: [''],
      documento: [''],
      nacionalidade: [''],
      profissao: [''],
      estadoCivil: [''],
      regimeBens: [''],
      rg: [''],
      cnh: [''],
      email: [''],
      telefone: [''],
      endereco: [''],
      conjugeNome: [''],
      conjugeCpf: [''],
      conjugeNacionalidade: [''],
      conjugeProfissao: [''],
      conjugeRg: ['']
    });
  }

  get compradoresArray(): FormArray {
    return this.contratoForm.get('compradores') as FormArray;
  }

  addComprador(): void {
    this.compradoresArray.push(this.createCompradorGroup());
    this.compradorDocTipos.push('CPF');
    this.compradorCollapsed.push(false);
    for (let i = 0; i < this.compradorCollapsed.length - 1; i++) {
      this.compradorCollapsed[i] = true;
    }
  }

  removeComprador(index: number): void {
    if (this.compradoresArray.length <= 1) return;
    this.compradoresArray.removeAt(index);
    this.compradorDocTipos.splice(index, 1);
    this.compradorCollapsed.splice(index, 1);
  }

  toggleComprador(index: number): void {
    this.compradorCollapsed[index] = !this.compradorCollapsed[index];
  }

  getCompradorLabel(index: number): string {
    const nome = this.compradoresArray.at(index).get('nome')?.value;
    return nome ? `Comprador ${index + 1} — ${nome}` : `Comprador ${index + 1}`;
  }

  showConjuge(index: number): boolean {
    const ec = this.compradoresArray.at(index).get('estadoCivil')?.value || '';
    return ec === 'Casado(a)' || ec === 'União Estável';
  }

  // ========== CONTRATO CRUD ==========

  loadContratos(): void {
    this.isLoading = true;
    this.contratoService.getAll().subscribe({
      next: (contratos) => {
        this.contratos = contratos;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Erro ao carregar contratos.';
        this.isLoading = false;
      }
    });
  }

  novoContrato(): void {
    this.showList = false;
    this.editingContrato = null;
    this.contratoId = null;
    this.currentPage = 1;
    this.ultimoSaldoParcelado = null;
    this.parcelasRecalculadasAviso = false;
    this.parcelasList = [];
    this.contratoForm.reset();
    // Reset form arrays to single item
    this.vendedoresArray.clear();
    this.vendedoresArray.push(this.createVendedorGroup());
    this.compradoresArray.clear();
    this.compradoresArray.push(this.createCompradorGroup());
    this.vendedorDocTipos = ['CNPJ'];
    this.compradorDocTipos = ['CPF'];
    this.vendedorCollapsed = [false];
    this.compradorCollapsed = [false];
    this.error = null;
    this.successMessage = null;
  }

  editarContrato(contrato: ContratoResponse): void {
    this.showList = false;
    this.editingContrato = contrato;
    this.contratoId = contrato.id;
    this.currentPage = contrato.paginaAtual || 1;

    // Reset arrays
    this.vendedoresArray.clear();
    this.compradoresArray.clear();
    this.vendedorDocTipos = [];
    this.compradorDocTipos = [];
    this.vendedorCollapsed = [];
    this.compradorCollapsed = [];

    // Populate vendedores
    const vendedores = contrato.vendedores || [];
    if (vendedores.length === 0) {
      this.vendedoresArray.push(this.createVendedorGroup());
      this.vendedorDocTipos.push('CNPJ');
      this.vendedorCollapsed.push(false);
    } else {
      for (const v of vendedores) {
        const g = this.createVendedorGroup();
        g.patchValue(v);
        this.vendedoresArray.push(g);
        this.vendedorDocTipos.push(this.inferDocTipo(v.documento || ''));
        this.vendedorCollapsed.push(false);
      }
      // Apply masks
      for (let i = 0; i < vendedores.length; i++) {
        this.applyDocMaskOnArray('vendedores', i, 'documento', this.vendedorDocTipos[i]);
      }
    }

    // Populate compradores
    const compradores = contrato.compradores || [];
    if (compradores.length === 0) {
      this.compradoresArray.push(this.createCompradorGroup());
      this.compradorDocTipos.push('CPF');
      this.compradorCollapsed.push(false);
    } else {
      for (const c of compradores) {
        const g = this.createCompradorGroup();
        g.patchValue(c);
        this.compradoresArray.push(g);
        this.compradorDocTipos.push(this.inferDocTipo(c.documento || ''));
        this.compradorCollapsed.push(false);
      }
      for (let i = 0; i < compradores.length; i++) {
        this.applyDocMaskOnArray('compradores', i, 'documento', this.compradorDocTipos[i]);
      }
    }

    // Patch remaining flat fields (skipNextSaldoCheck evita que valueChanges após patchValue limpe as parcelas)
    this.skipNextSaldoCheck = true;
    this.contratoForm.patchValue({
      imovelMatricula: contrato.imovelMatricula,
      imovelLivro: contrato.imovelLivro,
      imovelOficio: contrato.imovelOficio,
      imovelProprietario: contrato.imovelProprietario,
      imovelMomentoPosse: contrato.imovelMomentoPosse,
      imovelPrazoTransferencia: contrato.imovelPrazoTransferencia,
      imovelPrazoEscritura: contrato.imovelPrazoEscritura,
      imovelDescricao: contrato.imovelDescricao,
      permutaImovelMatricula: contrato.permutaImovelMatricula,
      permutaImovelLivro: contrato.permutaImovelLivro,
      permutaImovelOficio: contrato.permutaImovelOficio,
      permutaImovelProprietario: contrato.permutaImovelProprietario,
      permutaImovelMomentoPosse: contrato.permutaImovelMomentoPosse,
      permutaImovelPrazoTransferencia: contrato.permutaImovelPrazoTransferencia,
      permutaImovelPrazoEscritura: contrato.permutaImovelPrazoEscritura,
      permutaImovelDescricao: contrato.permutaImovelDescricao,
      veiculoMarca: contrato.veiculoMarca,
      veiculoAno: contrato.veiculoAno,
      veiculoModelo: contrato.veiculoModelo,
      veiculoPlaca: contrato.veiculoPlaca,
      veiculoChassi: contrato.veiculoChassi,
      veiculoCor: contrato.veiculoCor,
      veiculoMotor: contrato.veiculoMotor,
      veiculoRenavam: contrato.veiculoRenavam,
      veiculoDataEntrega: contrato.veiculoDataEntrega,
      negocioValorTotal: contrato.negocioValorTotal,
      negocioValorEntrada: contrato.negocioValorEntrada,
      negocioFormaPagamento: contrato.negocioFormaPagamento,
      negocioNumParcelas: contrato.negocioNumParcelas,
      negocioValorParcela: contrato.negocioValorParcela,
      negocioVencimentos: contrato.negocioVencimentos,
      negocioValorImovelPermuta: contrato.negocioValorImovelPermuta,
      negocioValorVeiculoPermuta: contrato.negocioValorVeiculoPermuta,
      negocioValorFinanciamento: contrato.negocioValorFinanciamento,
      negocioPrazoPagamento: contrato.negocioPrazoPagamento,
      negocioDataPrimeiraParcela: contrato.negocioDataPrimeiraParcela ?? null,
      contaTitular: contrato.contaTitular,
      contaBanco: contrato.contaBanco,
      contaAgencia: contrato.contaAgencia,
      contaPix: contrato.contaPix,
      honorariosValor: contrato.honorariosValor,
      honorariosFormaPagamento: contrato.honorariosFormaPagamento,
      honorariosDataPagamento: contrato.honorariosDataPagamento,
      observacoes: contrato.observacoes,
      dataContrato: contrato.dataContrato,
      assinaturaCorretor: contrato.assinaturaCorretor,
      assinaturaAgenciador: contrato.assinaturaAgenciador,
      assinaturaGestor: contrato.assinaturaGestor
    });

    this.error = null;
    this.successMessage = null;
    this.parcelasList = contrato.parcelas ?? [];
    if (this.parcelasList.length > 0) this.ultimoSaldoParcelado = this.getSaldoEmDinheiro();
    this.carregarAnexos();
  }

  voltarParaLista(): void {
    this.showList = true;
    this.editingContrato = null;
    this.contratoId = null;
    this.currentPage = 1;
    this.ultimoSaldoParcelado = null;
    this.parcelasRecalculadasAviso = false;
    this.parcelasList = [];
    this.contratoForm.reset();
    this.vendedoresArray.clear();
    this.vendedoresArray.push(this.createVendedorGroup());
    this.compradoresArray.clear();
    this.compradoresArray.push(this.createCompradorGroup());
    this.vendedorDocTipos = ['CNPJ'];
    this.compradorDocTipos = ['CPF'];
    this.vendedorCollapsed = [false];
    this.compradorCollapsed = [false];
    this.anexos = [];
    this.loadContratos();
  }

  editarVendedor(contrato: ContratoResponse): void {
    this.editarContrato(contrato);
    this.currentPage = 1;
  }

  editarComprador(contrato: ContratoResponse): void {
    this.editarContrato(contrato);
    this.currentPage = 2;
  }

  excluirContrato(contrato: ContratoResponse): void {
    const vendedorNome = contrato.vendedores?.[0]?.nome || 'Rascunho';
    if (!confirm(`Deseja excluir o contrato de ${vendedorNome}?`)) {
      return;
    }
    this.contratoService.delete(contrato.id).subscribe({
      next: () => {
        this.loadContratos();
        this.successMessage = 'Contrato excluído com sucesso!';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Erro ao excluir contrato.';
      }
    });
  }

  // ========== VALIDAÇÃO ==========

  isCurrentPageValid(): boolean {
    return true; // Validação desativada por enquanto
  }

  markCurrentPageAsTouched(): void {
    // No-op por enquanto
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.contratoForm.get(fieldName);
    return control ? control.invalid && control.touched : false;
  }

  // ========== NAVEGAÇÃO ==========

  async nextPage(): Promise<void> {
    this.error = null;
    await this.saveDraft();

    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      if (this.currentPage === 4 && this.contratoId) this.carregarAnexos();
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.error = null;
    }
  }

  goToPage(page: number): void {
    if (page <= this.currentPage && page >= 1) {
      this.currentPage = page;
      this.error = null;
      if (page === 4 && this.contratoId) this.carregarAnexos();
    }
  }

  async saveDraft(): Promise<void> {
    this.isSaving = true;
    this.error = null;

    const formData = this.contratoForm.value;

    // Build vendedores array with digits-only documento
    const vendedores: VendedorData[] = (formData.vendedores || []).map((v: any, i: number) => ({
      ...v,
      documento: this.digitsOnly(v.documento),
      ordem: i
    }));

    // Build compradores array with digits-only documento
    const compradores: CompradorData[] = (formData.compradores || []).map((c: any, i: number) => ({
      ...c,
      documento: this.digitsOnly(c.documento),
      ordem: i
    }));

    const valorTotal = (formData.negocioValorTotal === '' || formData.negocioValorTotal === null || formData.negocioValorTotal === undefined)
      ? null
      : Number(formData.negocioValorTotal);
    const valorImovelPermuta = this.toNumber(formData.negocioValorImovelPermuta);
    const valorVeiculoPermuta = this.toNumber(formData.negocioValorVeiculoPermuta);

    if (valorImovelPermuta < 0 || valorVeiculoPermuta < 0) {
      this.error = 'Valores de permuta não podem ser negativos.';
      this.isSaving = false;
      return;
    }
    if (valorTotal != null && valorTotal >= 0 && (valorImovelPermuta + valorVeiculoPermuta) > valorTotal) {
      this.error = 'A soma dos bens em permuta não pode exceder o valor total do negócio.';
      this.isSaving = false;
      return;
    }

    const parcelas = this.getParcelasList();
    const diaVenc = this.parseDiaVencimento(formData.negocioVencimentos);
    const request: ContratoRequest = {
      ...formData,
      vendedores,
      compradores,
      paginaAtual: this.currentPage,
      negocioValorTotal: valorTotal,
      negocioValorImovelPermuta: valorImovelPermuta,
      negocioValorVeiculoPermuta: valorVeiculoPermuta,
      negocioDataPrimeiraParcela: formData.negocioDataPrimeiraParcela || undefined,
      negocioValorParcela: this.getValorEstimadoParcela() || undefined,
      negocioVencimentos: diaVenc != null ? String(diaVenc) : formData.negocioVencimentos,
      parcelas: parcelas.length > 0 ? parcelas : undefined
    };

    try {
      if (this.contratoId) {
        console.log('[HISTORICO] saveDraft chamando update contratoId=', this.contratoId, 'negocioValorTotal=', request.negocioValorTotal);
        const response = await this.contratoService.update(this.contratoId, request).toPromise();
        if (response) {
          this.contratoId = response.id;
        }
      } else {
        const response = await this.contratoService.create(request).toPromise();
        if (response) {
          this.contratoId = response.id;
        }
      }
      this.isSaving = false;
    } catch (err: any) {
      this.error = err?.error?.message || 'Erro ao salvar rascunho.';
      this.isSaving = false;
    }
  }

  async saveVendedoresOnly(): Promise<void> {
    if (!this.contratoId) return;
    this.isSaving = true;
    this.error = null;
    const formData = this.contratoForm.value;
    const vendedores: VendedorData[] = (formData.vendedores || []).map((v: any, i: number) => ({
      ...v,
      documento: this.digitsOnly(v.documento),
      ordem: i
    }));
    try {
      await this.contratoService.updateVendedores(this.contratoId, vendedores).toPromise();
      this.successMessage = 'Vendedores atualizados com sucesso!';
      setTimeout(() => this.successMessage = null, 3000);
      this.voltarParaLista();
    } catch (err: any) {
      this.error = err?.error?.message || 'Erro ao salvar vendedores.';
    }
    this.isSaving = false;
  }

  async saveCompradoresOnly(): Promise<void> {
    if (!this.contratoId) return;
    this.isSaving = true;
    this.error = null;
    const formData = this.contratoForm.value;
    const compradores: CompradorData[] = (formData.compradores || []).map((c: any, i: number) => ({
      ...c,
      documento: this.digitsOnly(c.documento),
      ordem: i
    }));
    try {
      await this.contratoService.updateCompradores(this.contratoId, compradores).toPromise();
      this.successMessage = 'Compradores atualizados com sucesso!';
      setTimeout(() => this.successMessage = null, 3000);
      this.voltarParaLista();
    } catch (err: any) {
      this.error = err?.error?.message || 'Erro ao salvar compradores.';
    }
    this.isSaving = false;
  }

  async finalizar(): Promise<void> {
    await this.saveDraft();

    if (!this.contratoId) {
      this.error = 'Erro: contrato não foi salvo.';
      return;
    }

    this.isSaving = true;
    this.error = null;

    this.contratoService.finalizar(this.contratoId).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Contrato finalizado com sucesso!';
        setTimeout(() => {
          this.voltarParaLista();
        }, 2000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Erro ao finalizar contrato.';
        this.isSaving = false;
      }
    });
  }

  // ========== FORMATAÇÃO ==========

  formatCurrency(value: number | null): string {
    if (value === null || value === undefined) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  /** Saldo a pagar em dinheiro = valor total - imóvel permuta - veículo permuta (mín. 0). */
  getSaldoEmDinheiro(): number {
    const total = this.toNumber(this.contratoForm.get('negocioValorTotal')?.value);
    const imovel = this.toNumber(this.contratoForm.get('negocioValorImovelPermuta')?.value);
    const veiculo = this.toNumber(this.contratoForm.get('negocioValorVeiculoPermuta')?.value);
    return Math.max(0, total - imovel - veiculo);
  }

  /** Alias para uso no template (Saldo a pagar). */
  getSaldoAPagar(): number {
    return this.getSaldoEmDinheiro();
  }

  /** Lista de parcelas: gerada quando há saldo e n/data válidos; senão lista carregada do backend. */
  getParcelasList(): ParcelaItem[] {
    if (this.getSaldoAPagar() > 0) return this.buildParcelas();
    return this.parcelasList;
  }

  /** Monta parcelas a partir dos controles do formulário (delega para gerarParcelas). */
  buildParcelas(): ParcelaItem[] {
    const saldoAPagar = this.getSaldoAPagar();
    const n = this.toNumber(this.contratoForm.get('negocioNumParcelas')?.value);
    const firstStr = (this.contratoForm.get('negocioDataPrimeiraParcela')?.value as string) || null;
    const diaVencimento = this.parseDiaVencimento(this.contratoForm.get('negocioVencimentos')?.value);
    return this.gerarParcelas(saldoAPagar, n, firstStr, diaVencimento);
  }

  /** Gera array de parcelas: totalParcelar/n com última parcela ajustada; datas +1 mês com dia opcional. */
  gerarParcelas(saldoAPagar: number, n: number, dataPrimeira: string | null, diaVencimento?: number | null): ParcelaItem[] {
    if (!dataPrimeira || n < 1) return [];
    const valorBase = Math.round((saldoAPagar / n) * 100) / 100;
    const out: ParcelaItem[] = [];
    let soma = 0;
    for (let i = 1; i < n; i++) {
      const venc = this.addMonthsWithDay(dataPrimeira, i - 1, diaVencimento ?? null);
      out.push({ numero: i, vencimento: venc, valor: valorBase });
      soma += valorBase;
    }
    const lastValor = Math.round((saldoAPagar - soma) * 100) / 100;
    out.push({ numero: n, vencimento: this.addMonthsWithDay(dataPrimeira, n - 1, diaVencimento ?? null), valor: lastValor });
    return out;
  }

  /** Valor estimado da parcela (read-only): primeira parcela ou total/n quando n > 0. */
  getValorEstimadoParcela(): number {
    const list = this.getParcelasList();
    if (list.length > 0) return list[0].valor;
    const saldo = this.getSaldoAPagar();
    const n = this.toNumber(this.contratoForm.get('negocioNumParcelas')?.value);
    if (n > 0 && saldo > 0) return Math.round((saldo / n) * 100) / 100;
    return 0;
  }

  /** Dia do vencimento (1-31). Aceita number do input ou string ("10", "Todo dia 10"). */
  parseDiaVencimento(vencimentos: string | number | null | undefined): number | null {
    if (vencimentos == null) return null;
    if (typeof vencimentos === 'number') return vencimentos >= 1 && vencimentos <= 31 ? vencimentos : null;
    const t = String(vencimentos).trim();
    const match = t.match(/\b(\d{1,2})\b/);
    if (!match) return null;
    const day = parseInt(match[1], 10);
    return day >= 1 && day <= 31 ? day : null;
  }

  /** Adiciona months à data. Se diaVencimento for informado, usa esse dia (ou último dia do mês se menor). */
  private addMonthsWithDay(dateStr: string, months: number, diaVencimento: number | null): string {
    const [y, m, d] = dateStr.split('-').map(Number);
    const date = new Date(y, (m ?? 1) - 1, d ?? 1);
    date.setMonth(date.getMonth() + months);
    const year = date.getFullYear();
    const month = date.getMonth();
    let day: number;
    if (diaVencimento != null) {
      const lastDay = new Date(year, month + 1, 0).getDate();
      day = Math.min(diaVencimento, lastDay);
    } else {
      day = date.getDate();
    }
    const out = new Date(year, month, day);
    return out.toISOString().slice(0, 10);
  }

  private toNumber(v: any): number {
    if (v === null || v === undefined || v === '') return 0;
    const n = Number(v);
    return isNaN(n) ? 0 : n;
  }

  formatDate(dateString: string | null): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  getStatusLabel(status: string): string {
    return status === 'FINAL' ? 'Finalizado' : 'Rascunho';
  }

  getVendedorNomes(contrato: ContratoResponse): string {
    const vendedores = contrato.vendedores || [];
    if (vendedores.length === 0) return '-';
    if (vendedores.length === 1) return vendedores[0].nome || '-';
    return vendedores.map(v => v.nome).filter(Boolean).join(', ');
  }

  getCompradorNomes(contrato: ContratoResponse): string {
    const compradores = contrato.compradores || [];
    if (compradores.length === 0) return '-';
    if (compradores.length === 1) return compradores[0].nome || '-';
    return compradores.map(c => c.nome).filter(Boolean).join(', ');
  }

  // ========== CPF/CNPJ HELPERS ==========

  digitsOnly(value: string): string {
    return (value || '').replace(/\D/g, '');
  }

  formatCpf(digits: string): string {
    digits = digits.substring(0, 11);
    if (digits.length <= 3) return digits;
    if (digits.length <= 6) return digits.replace(/(\d{3})(\d+)/, '$1.$2');
    if (digits.length <= 9) return digits.replace(/(\d{3})(\d{3})(\d+)/, '$1.$2.$3');
    return digits.replace(/(\d{3})(\d{3})(\d{3})(\d+)/, '$1.$2.$3-$4');
  }

  formatCnpj(digits: string): string {
    digits = digits.substring(0, 14);
    if (digits.length <= 2) return digits;
    if (digits.length <= 5) return digits.replace(/(\d{2})(\d+)/, '$1.$2');
    if (digits.length <= 8) return digits.replace(/(\d{2})(\d{3})(\d+)/, '$1.$2.$3');
    if (digits.length <= 12) return digits.replace(/(\d{2})(\d{3})(\d{3})(\d+)/, '$1.$2.$3/$4');
    return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d+)/, '$1.$2.$3/$4-$5');
  }

  applyDocMaskOnArray(arrayName: string, index: number, controlName: string, docTipo: 'CPF' | 'CNPJ'): void {
    const arr = this.contratoForm.get(arrayName) as FormArray;
    const control = arr.at(index)?.get(controlName);
    if (!control) return;
    const digits = this.digitsOnly(control.value);
    const formatted = docTipo === 'CPF' ? this.formatCpf(digits) : this.formatCnpj(digits);
    control.setValue(formatted, { emitEvent: false });
  }

  onVendedorDocInput(index: number): void {
    this.applyDocMaskOnArray('vendedores', index, 'documento', this.vendedorDocTipos[index]);
  }

  onCompradorDocInput(index: number): void {
    this.applyDocMaskOnArray('compradores', index, 'documento', this.compradorDocTipos[index]);
  }

  setVendedorDocTipo(index: number, tipo: 'CPF' | 'CNPJ'): void {
    this.vendedorDocTipos[index] = tipo;
    const control = this.vendedoresArray.at(index)?.get('documento');
    if (control) {
      const digits = this.digitsOnly(control.value);
      const maxLen = tipo === 'CPF' ? 11 : 14;
      const trimmed = digits.substring(0, maxLen);
      const formatted = tipo === 'CPF' ? this.formatCpf(trimmed) : this.formatCnpj(trimmed);
      control.setValue(formatted, { emitEvent: false });
    }
  }

  setCompradorDocTipo(index: number, tipo: 'CPF' | 'CNPJ'): void {
    this.compradorDocTipos[index] = tipo;
    const control = this.compradoresArray.at(index)?.get('documento');
    if (control) {
      const digits = this.digitsOnly(control.value);
      const maxLen = tipo === 'CPF' ? 11 : 14;
      const trimmed = digits.substring(0, maxLen);
      const formatted = tipo === 'CPF' ? this.formatCpf(trimmed) : this.formatCnpj(trimmed);
      control.setValue(formatted, { emitEvent: false });
    }
  }

  inferDocTipo(value: string): 'CPF' | 'CNPJ' {
    const digits = this.digitsOnly(value);
    return digits.length > 11 ? 'CNPJ' : 'CPF';
  }

  getDocPlaceholder(tipo: 'CPF' | 'CNPJ'): string {
    return tipo === 'CPF' ? '000.000.000-00' : '00.000.000/0000-00';
  }

  getDocMaxLength(tipo: 'CPF' | 'CNPJ'): number {
    return tipo === 'CPF' ? 14 : 18;
  }

  // ========== HISTÓRICO DE ALTERAÇÕES ==========

  abrirHistorico(contrato: ContratoResponse): void {
    const contractId = contrato.id;
    console.log('[HISTORICO] abrirHistorico contractId=', contractId);
    this.historicoContratoId = contractId;
    this.showHistoricoModal = true;
    this.historicoLoading = true;
    this.historicoError = null;
    this.historicoAlteracoes = [];

    this.contratoService.getHistorico(contractId).subscribe({
      next: (alteracoes) => {
        console.log('[HISTORICO] GET historico contractId=', contractId, 'resposta size=', alteracoes?.length ?? 0, 'bruto=', alteracoes);
        this.historicoAlteracoes = alteracoes ?? [];
        this.historicoLoading = false;
      },
      error: (err) => {
        console.warn('[HISTORICO] GET historico erro contractId=', contractId, err);
        this.historicoError = err?.error?.message || 'Erro ao carregar histórico.';
        this.historicoLoading = false;
      }
    });
  }

  fecharHistorico(): void {
    this.showHistoricoModal = false;
    this.historicoAlteracoes = [];
    this.historicoError = null;
    this.historicoContratoId = null;
  }

  formatDateTime(dateString: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR') + ' ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatFieldPath(path: string): string {
    // Traduzir caminhos para português amigável
    const translations: { [key: string]: string } = {
      'vendedores': 'Vendedores',
      'compradores': 'Compradores',
      'nome': 'Nome',
      'documento': 'Documento',
      'email': 'E-mail',
      'telefone': 'Telefone',
      'endereco': 'Endereço',
      'socioNome': 'Sócio - Nome',
      'socioCpf': 'Sócio - CPF',
      'socioNacionalidade': 'Sócio - Nacionalidade',
      'socioProfissao': 'Sócio - Profissão',
      'socioEstadoCivil': 'Sócio - Estado Civil',
      'socioRegimeBens': 'Sócio - Regime de Bens',
      'socioRg': 'Sócio - RG',
      'socioCnh': 'Sócio - CNH',
      'socioEmail': 'Sócio - E-mail',
      'socioTelefone': 'Sócio - Telefone',
      'socioEndereco': 'Sócio - Endereço',
      'nacionalidade': 'Nacionalidade',
      'profissao': 'Profissão',
      'estadoCivil': 'Estado Civil',
      'regimeBens': 'Regime de Bens',
      'rg': 'RG',
      'cnh': 'CNH',
      'conjugeNome': 'Cônjuge - Nome',
      'conjugeCpf': 'Cônjuge - CPF',
      'conjugeNacionalidade': 'Cônjuge - Nacionalidade',
      'conjugeProfissao': 'Cônjuge - Profissão',
      'conjugeRg': 'Cônjuge - RG',
      'imovelMatricula': 'Imóvel - Matrícula',
      'imovelLivro': 'Imóvel - Livro',
      'imovelOficio': 'Imóvel - Ofício',
      'imovelProprietario': 'Imóvel - Proprietário',
      'imovelMomentoPosse': 'Imóvel - Momento da Posse',
      'imovelPrazoTransferencia': 'Imóvel - Prazo Transferência',
      'imovelPrazoEscritura': 'Imóvel - Prazo Escritura',
      'imovelDescricao': 'Imóvel - Descrição',
      'permutaImovelMatricula': 'Permuta Imóvel - Matrícula',
      'permutaImovelLivro': 'Permuta Imóvel - Livro',
      'permutaImovelOficio': 'Permuta Imóvel - Ofício',
      'permutaImovelProprietario': 'Permuta Imóvel - Proprietário',
      'permutaImovelMomentoPosse': 'Permuta Imóvel - Momento da Posse',
      'permutaImovelPrazoTransferencia': 'Permuta Imóvel - Prazo Transferência',
      'permutaImovelPrazoEscritura': 'Permuta Imóvel - Prazo Escritura',
      'permutaImovelDescricao': 'Permuta Imóvel - Descrição',
      'veiculoMarca': 'Veículo - Marca',
      'veiculoAno': 'Veículo - Ano',
      'veiculoModelo': 'Veículo - Modelo',
      'veiculoPlaca': 'Veículo - Placa',
      'veiculoChassi': 'Veículo - Chassi',
      'veiculoCor': 'Veículo - Cor',
      'veiculoMotor': 'Veículo - Motor',
      'veiculoRenavam': 'Veículo - Renavam',
      'veiculoDataEntrega': 'Veículo - Data Entrega',
      'veiculoKm': 'Veículo - Km',
      'negocioValorTotal': 'Negócio - Valor Total',
      'negocioValorEntrada': 'Negócio - Valor Entrada',
      'negocioFormaPagamento': 'Negócio - Forma de Pagamento',
      'negocioNumParcelas': 'Negócio - Nº Parcelas',
      'negocioValorParcela': 'Negócio - Valor Parcela',
      'negocioVencimentos': 'Negócio - Vencimentos',
      'negocioValorImovelPermuta': 'Negócio - Valor Imóvel Permuta',
      'negocioValorVeiculoPermuta': 'Negócio - Valor Veículo Permuta',
      'negocioValorFinanciamento': 'Negócio - Valor Financiamento',
      'negocioPrazoPagamento': 'Negócio - Prazo Pagamento',
      'contaTitular': 'Conta - Titular',
      'contaBanco': 'Conta - Banco',
      'contaAgencia': 'Conta - Agência',
      'contaPix': 'Conta - PIX',
      'honorariosValor': 'Honorários - Valor',
      'honorariosFormaPagamento': 'Honorários - Forma de Pagamento',
      'honorariosDataPagamento': 'Honorários - Data Pagamento',
      'observacoes': 'Observações',
      'dataContrato': 'Data do Contrato',
      'assinaturaCorretor': 'Assinatura Corretor',
      'assinaturaAgenciador': 'Assinatura Agenciador',
      'assinaturaGestor': 'Assinatura Gestor',
      'status': 'Status',
      'ordem': 'Ordem',
      'id': 'ID'
    };

    // Attempt to translate each part of the path
    return path.replace(/([a-zA-Z]+)/g, (match) => translations[match] || match);
  }

  formatValue(value: any): string {
    if (value === null || value === undefined || value === 'null') return '(vazio)';
    if (value === '(removido)') return '(removido)';
    if (typeof value === 'string' && value.trim() === '') return '(vazio)';
    return String(value);
  }

  /** Paths que representam valores monetários (ex.: negocioValorTotal, honorariosValor). */
  private static readonly MONETARY_PATH_REGEX = /(negociovalor|honorariosvalor|valorparcela|valorentrada|valorfinanciamento)/i;

  /**
   * Formata valor para exibição no histórico de alterações.
   * Valores monetários (incl. notação científica como 5E+1) são exibidos como R$ X.XXX,XX.
   */
  formatAuditValue(path: string, value: any): string {
    if (value === null || value === undefined || value === 'null') return '(vazio)';
    if (value === '(removido)') return '(removido)';
    if (typeof value === 'string' && value.trim() === '') return '(vazio)';

    const pathLower = (path || '').toLowerCase();
    if (ContratosComponent.MONETARY_PATH_REGEX.test(pathLower)) {
      const n = Number(value);
      if (!Number.isFinite(n)) return String(value);
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(n);
    }

    if (pathLower.endsWith('status') || pathLower.includes('status')) {
      const v = String(value).toUpperCase();
      if (v === 'DRAFT') return 'Rascunho';
      if (v === 'FINAL') return 'Finalizado';
    }

    return String(value);
  }

  /** Camada de sanitização no frontend (defesa em profundidade): só exibe alterações auditáveis. */
  sanitizeAuditChanges(changes: FieldChange[]): FieldChange[] {
    if (!changes?.length) return [];
    return changes.filter(c => {
      const path = (c.path || '').toLowerCase();
      if (path.endsWith('.id') || path === 'id') return false;
      if (path.endsWith('.ordem') || path === 'ordem') return false;
      if (path.includes('createdat') || path.includes('updatedat') || path.includes('paginaatual')) return false;
      const oldV = c.oldValue == null ? '' : String(c.oldValue).trim();
      const newV = c.newValue == null ? '' : String(c.newValue).trim();
      if (oldV === newV) return false;
      return true;
    });
  }

  getHistoricoChangesForDisplay(alteracao: ContratoAlteracao): FieldChange[] {
    return this.sanitizeAuditChanges(alteracao.changes ?? []);
  }

  getHistoricoChangesCount(alteracao: ContratoAlteracao): number {
    return this.getHistoricoChangesForDisplay(alteracao).length;
  }

  hasAnyHistoricoChangesToDisplay(): boolean {
    return this.historicoAlteracoes.some(a => this.getHistoricoChangesCount(a) > 0);
  }

  // ========== ANEXOS ==========

  carregarAnexos(): void {
    if (!this.contratoId) {
      this.anexos = [];
      return;
    }
    this.contratoService.getAnexos(this.contratoId).subscribe({
      next: (anexos) => {
        this.anexos = anexos;
      },
      error: (err) => {
        console.error('Erro ao carregar anexos:', err);
      }
    });
  }

  abrirCamera(): void {
    if (this.cameraInput) {
      this.cameraInput.nativeElement.click();
    }
  }

  selecionarArquivo(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];
    this.uploadAnexo(file);
    input.value = '';
  }

  uploadAnexo(file: File): void {
    if (!this.contratoId) {
      this.error = 'Salve o contrato antes de anexar documentos.';
      return;
    }

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      this.error = 'Arquivo muito grande. O tamanho máximo é 10MB.';
      return;
    }

    this.isUploading = true;
    this.error = null;

    this.contratoService.uploadAnexo(this.contratoId, file).subscribe({
      next: (anexo) => {
        this.anexos.unshift(anexo);
        this.isUploading = false;
        this.successMessage = 'Documento anexado com sucesso!';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Erro ao enviar arquivo.';
        this.isUploading = false;
      }
    });
  }

  downloadAnexo(anexo: ContratoAnexo): void {
    if (!this.contratoId) return;
    const url = this.contratoService.getAnexoDownloadUrl(this.contratoId, anexo.id);
    window.open(url, '_blank');
  }

  excluirAnexo(anexo: ContratoAnexo): void {
    if (!this.contratoId) return;
    if (!confirm(`Deseja excluir o documento "${anexo.nomeOriginal}"?`)) {
      return;
    }

    this.contratoService.deleteAnexo(this.contratoId, anexo.id).subscribe({
      next: () => {
        this.anexos = this.anexos.filter(a => a.id !== anexo.id);
        this.successMessage = 'Documento excluído com sucesso!';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Erro ao excluir documento.';
      }
    });
  }

  isImage(tipoMime: string): boolean {
    return tipoMime?.startsWith('image/') || false;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
