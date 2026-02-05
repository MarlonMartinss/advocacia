import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { ContratoService, ContratoResponse, ContratoRequest, ContratoAnexo } from '../../services/contrato.service';

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
    { number: 1, title: 'Vendedor', subtitle: 'Vendedor PJ + Sócio' },
    { number: 2, title: 'Comprador', subtitle: 'Comprador + Cônjuge' },
    { number: 3, title: 'Imóveis', subtitle: 'Imóvel + Permuta + Veículo' },
    { number: 4, title: 'Negócio', subtitle: 'Valores + Honorários' }
  ];

  // Lista de contratos
  contratos: ContratoResponse[] = [];
  showList = true;
  editingContrato: ContratoResponse | null = null;

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
    this.loadContratos();
  }

  initForm(): void {
    this.contratoForm = this.fb.group({
      // Página 1: Vendedor PJ (obrigatórios desativados por enquanto)
      vendedorNome: [''],
      vendedorCnpj: [''],
      vendedorEmail: [''],
      vendedorTelefone: [''],
      vendedorEndereco: [''],

      // Página 1: Sócio Administrador
      socioNome: [''],
      socioNacionalidade: [''],
      socioProfissao: [''],
      socioEstadoCivil: [''],
      socioRegimeBens: [''],
      socioCpf: [''],
      socioRg: [''],
      socioCnh: [''],
      socioEmail: [''],
      socioTelefone: [''],
      socioEndereco: [''],

      // Página 2: Comprador (obrigatórios desativados por enquanto)
      compradorNome: [''],
      compradorNacionalidade: [''],
      compradorProfissao: [''],
      compradorEstadoCivil: [''],
      compradorRegimeBens: [''],
      compradorCpf: [''],
      compradorRg: [''],
      compradorCnh: [''],
      compradorEmail: [''],
      compradorTelefone: [''],
      compradorEndereco: [''],

      // Página 2: Cônjuge
      conjugeNome: [''],
      conjugeNacionalidade: [''],
      conjugeProfissao: [''],
      conjugeCpf: [''],
      conjugeRg: [''],

      // Página 3: Imóvel Objeto (obrigatórios desativados por enquanto)
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

      // Página 4: Negócio (obrigatórios desativados por enquanto)
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
    this.contratoForm.reset();
    this.error = null;
    this.successMessage = null;
  }

  editarContrato(contrato: ContratoResponse): void {
    this.showList = false;
    this.editingContrato = contrato;
    this.contratoId = contrato.id;
    this.currentPage = contrato.paginaAtual || 1;
    this.contratoForm.patchValue(contrato);
    this.error = null;
    this.successMessage = null;
    this.carregarAnexos();
  }

  voltarParaLista(): void {
    this.showList = true;
    this.editingContrato = null;
    this.contratoId = null;
    this.currentPage = 1;
    this.contratoForm.reset();
    this.anexos = [];
    this.loadContratos();
  }

  excluirContrato(contrato: ContratoResponse): void {
    if (contrato.status === 'FINAL') {
      this.error = 'Não é possível excluir um contrato finalizado.';
      return;
    }
    if (!confirm(`Deseja excluir o contrato de ${contrato.vendedorNome || 'Rascunho'}?`)) {
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

  // Campos obrigatórios por página
  getPageFields(page: number): string[] {
    switch (page) {
      case 1:
        return ['vendedorNome', 'vendedorCnpj', 'socioNome', 'socioCpf'];
      case 2:
        return ['compradorNome', 'compradorCpf'];
      case 3:
        return ['imovelMatricula'];
      case 4:
        return ['negocioValorTotal'];
      default:
        return [];
    }
  }

  isCurrentPageValid(): boolean {
    const fields = this.getPageFields(this.currentPage);
    for (const field of fields) {
      const control = this.contratoForm.get(field);
      if (control && control.invalid) {
        return false;
      }
    }
    return true;
  }

  markCurrentPageAsTouched(): void {
    const fields = this.getPageFields(this.currentPage);
    for (const field of fields) {
      const control = this.contratoForm.get(field);
      if (control) {
        control.markAsTouched();
      }
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.contratoForm.get(fieldName);
    return control ? control.invalid && control.touched : false;
  }

  async nextPage(): Promise<void> {
    this.markCurrentPageAsTouched();

    if (!this.isCurrentPageValid()) {
      this.error = 'Preencha todos os campos obrigatórios antes de avançar.';
      return;
    }

    this.error = null;
    await this.saveDraft();

    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      // Ao entrar na página 4 (anexos), carregar a lista se já tiver contrato salvo
      if (this.currentPage === 4 && this.contratoId) {
        this.carregarAnexos();
      }
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.error = null;
    }
  }

  goToPage(page: number): void {
    // Só permite ir para páginas anteriores ou a atual
    if (page <= this.currentPage && page >= 1) {
      this.currentPage = page;
      this.error = null;
      if (page === 4 && this.contratoId) {
        this.carregarAnexos();
      }
    }
  }

  async saveDraft(): Promise<void> {
    this.isSaving = true;
    this.error = null;

    const formData = this.contratoForm.value;
    const request: ContratoRequest = {
      ...formData,
      paginaAtual: this.currentPage
    };

    try {
      if (this.contratoId) {
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

  async finalizar(): Promise<void> {
    // Validar página atual primeiro
    this.markCurrentPageAsTouched();
    if (!this.isCurrentPageValid()) {
      this.error = 'Preencha todos os campos obrigatórios antes de finalizar.';
      return;
    }

    // Salvar antes de finalizar
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

  formatCurrency(value: number | null): string {
    if (value === null || value === undefined) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  formatDate(dateString: string | null): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  getStatusLabel(status: string): string {
    return status === 'FINAL' ? 'Finalizado' : 'Rascunho';
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

    // Limpar o input para permitir selecionar o mesmo arquivo novamente
    input.value = '';
  }

  uploadAnexo(file: File): void {
    if (!this.contratoId) {
      this.error = 'Salve o contrato antes de anexar documentos.';
      return;
    }

    // Validar tamanho (10MB máximo)
    const maxSize = 10 * 1024 * 1024; // 10MB
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
