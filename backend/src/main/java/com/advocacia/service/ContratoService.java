package com.advocacia.service;

import com.advocacia.dto.ContratoRequest;
import com.advocacia.dto.ContratoResponse;
import com.advocacia.entity.Contrato;
import com.advocacia.entity.ContratoStatus;
import com.advocacia.exception.UserNotFoundException;
import com.advocacia.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;

    public List<ContratoResponse> findAll() {
        return contratoRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContratoResponse findById(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
        return toResponse(contrato);
    }

    @Transactional
    public ContratoResponse create(ContratoRequest request) {
        Contrato contrato = Contrato.builder()
                .status(ContratoStatus.DRAFT)
                .paginaAtual(1)
                .build();
        
        updateContratoFromRequest(contrato, request);
        contrato = contratoRepository.save(contrato);
        return toResponse(contrato);
    }

    @Transactional
    public ContratoResponse update(Long id, ContratoRequest request) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        if (contrato.getStatus() == ContratoStatus.FINAL) {
            throw new RuntimeException("Não é possível editar um contrato finalizado");
        }

        updateContratoFromRequest(contrato, request);
        contrato = contratoRepository.save(contrato);
        return toResponse(contrato);
    }

    @Transactional
    public ContratoResponse finalizar(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        if (contrato.getStatus() == ContratoStatus.FINAL) {
            throw new RuntimeException("Contrato já está finalizado");
        }

        // Validação de campos obrigatórios desativada por enquanto (reativar quando solicitado)
        // List<String> erros = validarContratoCompleto(contrato);
        // if (!erros.isEmpty()) {
        //     throw new RuntimeException("Campos obrigatórios não preenchidos: " + String.join(", ", erros));
        // }

        contrato.setStatus(ContratoStatus.FINAL);
        contrato = contratoRepository.save(contrato);
        return toResponse(contrato);
    }

    @Transactional
    public void delete(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
        
        if (contrato.getStatus() == ContratoStatus.FINAL) {
            throw new RuntimeException("Não é possível excluir um contrato finalizado");
        }
        
        contratoRepository.delete(contrato);
    }

    @Transactional
    public void deleteAll() {
        contratoRepository.deleteAll();
    }

    private List<String> validarContratoCompleto(Contrato contrato) {
        List<String> erros = new ArrayList<>();

        // Página 1: Vendedor + Sócio
        if (isBlank(contrato.getVendedorNome())) erros.add("Nome do Vendedor");
        if (isBlank(contrato.getVendedorCnpj())) erros.add("CNPJ do Vendedor");
        if (isBlank(contrato.getSocioNome())) erros.add("Nome do Sócio");
        if (isBlank(contrato.getSocioCpf())) erros.add("CPF do Sócio");

        // Página 2: Comprador
        if (isBlank(contrato.getCompradorNome())) erros.add("Nome do Comprador");
        if (isBlank(contrato.getCompradorCpf())) erros.add("CPF do Comprador");

        // Página 3: Imóvel
        if (isBlank(contrato.getImovelMatricula())) erros.add("Matrícula do Imóvel");

        // Página 4: Negócio
        if (contrato.getNegocioValorTotal() == null) erros.add("Valor Total do Negócio");

        return erros;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void updateContratoFromRequest(Contrato contrato, ContratoRequest request) {
        if (request == null) return;

        if (request.getPaginaAtual() != null) contrato.setPaginaAtual(request.getPaginaAtual());

        // Página 1: Vendedor
        if (request.getVendedorNome() != null) contrato.setVendedorNome(request.getVendedorNome());
        if (request.getVendedorCnpj() != null) contrato.setVendedorCnpj(request.getVendedorCnpj());
        if (request.getVendedorEmail() != null) contrato.setVendedorEmail(request.getVendedorEmail());
        if (request.getVendedorTelefone() != null) contrato.setVendedorTelefone(request.getVendedorTelefone());
        if (request.getVendedorEndereco() != null) contrato.setVendedorEndereco(request.getVendedorEndereco());

        // Página 1: Sócio
        if (request.getSocioNome() != null) contrato.setSocioNome(request.getSocioNome());
        if (request.getSocioNacionalidade() != null) contrato.setSocioNacionalidade(request.getSocioNacionalidade());
        if (request.getSocioProfissao() != null) contrato.setSocioProfissao(request.getSocioProfissao());
        if (request.getSocioEstadoCivil() != null) contrato.setSocioEstadoCivil(request.getSocioEstadoCivil());
        if (request.getSocioRegimeBens() != null) contrato.setSocioRegimeBens(request.getSocioRegimeBens());
        if (request.getSocioCpf() != null) contrato.setSocioCpf(request.getSocioCpf());
        if (request.getSocioRg() != null) contrato.setSocioRg(request.getSocioRg());
        if (request.getSocioCnh() != null) contrato.setSocioCnh(request.getSocioCnh());
        if (request.getSocioEmail() != null) contrato.setSocioEmail(request.getSocioEmail());
        if (request.getSocioTelefone() != null) contrato.setSocioTelefone(request.getSocioTelefone());
        if (request.getSocioEndereco() != null) contrato.setSocioEndereco(request.getSocioEndereco());

        // Página 2: Comprador
        if (request.getCompradorNome() != null) contrato.setCompradorNome(request.getCompradorNome());
        if (request.getCompradorNacionalidade() != null) contrato.setCompradorNacionalidade(request.getCompradorNacionalidade());
        if (request.getCompradorProfissao() != null) contrato.setCompradorProfissao(request.getCompradorProfissao());
        if (request.getCompradorEstadoCivil() != null) contrato.setCompradorEstadoCivil(request.getCompradorEstadoCivil());
        if (request.getCompradorRegimeBens() != null) contrato.setCompradorRegimeBens(request.getCompradorRegimeBens());
        if (request.getCompradorCpf() != null) contrato.setCompradorCpf(request.getCompradorCpf());
        if (request.getCompradorRg() != null) contrato.setCompradorRg(request.getCompradorRg());
        if (request.getCompradorCnh() != null) contrato.setCompradorCnh(request.getCompradorCnh());
        if (request.getCompradorEmail() != null) contrato.setCompradorEmail(request.getCompradorEmail());
        if (request.getCompradorTelefone() != null) contrato.setCompradorTelefone(request.getCompradorTelefone());
        if (request.getCompradorEndereco() != null) contrato.setCompradorEndereco(request.getCompradorEndereco());

        // Página 2: Cônjuge
        if (request.getConjugeNome() != null) contrato.setConjugeNome(request.getConjugeNome());
        if (request.getConjugeNacionalidade() != null) contrato.setConjugeNacionalidade(request.getConjugeNacionalidade());
        if (request.getConjugeProfissao() != null) contrato.setConjugeProfissao(request.getConjugeProfissao());
        if (request.getConjugeCpf() != null) contrato.setConjugeCpf(request.getConjugeCpf());
        if (request.getConjugeRg() != null) contrato.setConjugeRg(request.getConjugeRg());

        // Página 3: Imóvel Objeto
        if (request.getImovelMatricula() != null) contrato.setImovelMatricula(request.getImovelMatricula());
        if (request.getImovelLivro() != null) contrato.setImovelLivro(request.getImovelLivro());
        if (request.getImovelOficio() != null) contrato.setImovelOficio(request.getImovelOficio());
        if (request.getImovelProprietario() != null) contrato.setImovelProprietario(request.getImovelProprietario());
        if (request.getImovelMomentoPosse() != null) contrato.setImovelMomentoPosse(request.getImovelMomentoPosse());
        if (request.getImovelPrazoTransferencia() != null) contrato.setImovelPrazoTransferencia(request.getImovelPrazoTransferencia());
        if (request.getImovelPrazoEscritura() != null) contrato.setImovelPrazoEscritura(request.getImovelPrazoEscritura());
        if (request.getImovelDescricao() != null) contrato.setImovelDescricao(request.getImovelDescricao());

        // Página 3: Imóvel Permuta
        if (request.getPermutaImovelMatricula() != null) contrato.setPermutaImovelMatricula(request.getPermutaImovelMatricula());
        if (request.getPermutaImovelLivro() != null) contrato.setPermutaImovelLivro(request.getPermutaImovelLivro());
        if (request.getPermutaImovelOficio() != null) contrato.setPermutaImovelOficio(request.getPermutaImovelOficio());
        if (request.getPermutaImovelProprietario() != null) contrato.setPermutaImovelProprietario(request.getPermutaImovelProprietario());
        if (request.getPermutaImovelMomentoPosse() != null) contrato.setPermutaImovelMomentoPosse(request.getPermutaImovelMomentoPosse());
        if (request.getPermutaImovelPrazoTransferencia() != null) contrato.setPermutaImovelPrazoTransferencia(request.getPermutaImovelPrazoTransferencia());
        if (request.getPermutaImovelPrazoEscritura() != null) contrato.setPermutaImovelPrazoEscritura(request.getPermutaImovelPrazoEscritura());
        if (request.getPermutaImovelDescricao() != null) contrato.setPermutaImovelDescricao(request.getPermutaImovelDescricao());

        // Página 3: Veículo Permuta
        if (request.getVeiculoMarca() != null) contrato.setVeiculoMarca(request.getVeiculoMarca());
        if (request.getVeiculoAno() != null) contrato.setVeiculoAno(request.getVeiculoAno());
        if (request.getVeiculoModelo() != null) contrato.setVeiculoModelo(request.getVeiculoModelo());
        if (request.getVeiculoPlaca() != null) contrato.setVeiculoPlaca(request.getVeiculoPlaca());
        if (request.getVeiculoChassi() != null) contrato.setVeiculoChassi(request.getVeiculoChassi());
        if (request.getVeiculoCor() != null) contrato.setVeiculoCor(request.getVeiculoCor());
        if (request.getVeiculoMotor() != null) contrato.setVeiculoMotor(request.getVeiculoMotor());
        if (request.getVeiculoRenavam() != null) contrato.setVeiculoRenavam(request.getVeiculoRenavam());
        if (request.getVeiculoDataEntrega() != null) contrato.setVeiculoDataEntrega(request.getVeiculoDataEntrega());

        // Página 4: Negócio
        if (request.getNegocioValorTotal() != null) contrato.setNegocioValorTotal(request.getNegocioValorTotal());
        if (request.getNegocioValorEntrada() != null) contrato.setNegocioValorEntrada(request.getNegocioValorEntrada());
        if (request.getNegocioFormaPagamento() != null) contrato.setNegocioFormaPagamento(request.getNegocioFormaPagamento());
        if (request.getNegocioNumParcelas() != null) contrato.setNegocioNumParcelas(request.getNegocioNumParcelas());
        if (request.getNegocioValorParcela() != null) contrato.setNegocioValorParcela(request.getNegocioValorParcela());
        if (request.getNegocioVencimentos() != null) contrato.setNegocioVencimentos(request.getNegocioVencimentos());
        if (request.getNegocioValorImovelPermuta() != null) contrato.setNegocioValorImovelPermuta(request.getNegocioValorImovelPermuta());
        if (request.getNegocioValorVeiculoPermuta() != null) contrato.setNegocioValorVeiculoPermuta(request.getNegocioValorVeiculoPermuta());
        if (request.getNegocioValorFinanciamento() != null) contrato.setNegocioValorFinanciamento(request.getNegocioValorFinanciamento());
        if (request.getNegocioPrazoPagamento() != null) contrato.setNegocioPrazoPagamento(request.getNegocioPrazoPagamento());

        // Página 4: Conta Bancária
        if (request.getContaTitular() != null) contrato.setContaTitular(request.getContaTitular());
        if (request.getContaBanco() != null) contrato.setContaBanco(request.getContaBanco());
        if (request.getContaAgencia() != null) contrato.setContaAgencia(request.getContaAgencia());
        if (request.getContaPix() != null) contrato.setContaPix(request.getContaPix());

        // Página 4: Honorários
        if (request.getHonorariosValor() != null) contrato.setHonorariosValor(request.getHonorariosValor());
        if (request.getHonorariosFormaPagamento() != null) contrato.setHonorariosFormaPagamento(request.getHonorariosFormaPagamento());
        if (request.getHonorariosDataPagamento() != null) contrato.setHonorariosDataPagamento(request.getHonorariosDataPagamento());

        // Página 4: Observações e Assinaturas
        if (request.getObservacoes() != null) contrato.setObservacoes(request.getObservacoes());
        if (request.getDataContrato() != null) contrato.setDataContrato(request.getDataContrato());
        if (request.getAssinaturaCorretor() != null) contrato.setAssinaturaCorretor(request.getAssinaturaCorretor());
        if (request.getAssinaturaAgenciador() != null) contrato.setAssinaturaAgenciador(request.getAssinaturaAgenciador());
        if (request.getAssinaturaGestor() != null) contrato.setAssinaturaGestor(request.getAssinaturaGestor());
    }

    private ContratoResponse toResponse(Contrato contrato) {
        return ContratoResponse.builder()
                .id(contrato.getId())
                .status(contrato.getStatus())
                .paginaAtual(contrato.getPaginaAtual())
                // Página 1
                .vendedorNome(contrato.getVendedorNome())
                .vendedorCnpj(contrato.getVendedorCnpj())
                .vendedorEmail(contrato.getVendedorEmail())
                .vendedorTelefone(contrato.getVendedorTelefone())
                .vendedorEndereco(contrato.getVendedorEndereco())
                .socioNome(contrato.getSocioNome())
                .socioNacionalidade(contrato.getSocioNacionalidade())
                .socioProfissao(contrato.getSocioProfissao())
                .socioEstadoCivil(contrato.getSocioEstadoCivil())
                .socioRegimeBens(contrato.getSocioRegimeBens())
                .socioCpf(contrato.getSocioCpf())
                .socioRg(contrato.getSocioRg())
                .socioCnh(contrato.getSocioCnh())
                .socioEmail(contrato.getSocioEmail())
                .socioTelefone(contrato.getSocioTelefone())
                .socioEndereco(contrato.getSocioEndereco())
                // Página 2
                .compradorNome(contrato.getCompradorNome())
                .compradorNacionalidade(contrato.getCompradorNacionalidade())
                .compradorProfissao(contrato.getCompradorProfissao())
                .compradorEstadoCivil(contrato.getCompradorEstadoCivil())
                .compradorRegimeBens(contrato.getCompradorRegimeBens())
                .compradorCpf(contrato.getCompradorCpf())
                .compradorRg(contrato.getCompradorRg())
                .compradorCnh(contrato.getCompradorCnh())
                .compradorEmail(contrato.getCompradorEmail())
                .compradorTelefone(contrato.getCompradorTelefone())
                .compradorEndereco(contrato.getCompradorEndereco())
                .conjugeNome(contrato.getConjugeNome())
                .conjugeNacionalidade(contrato.getConjugeNacionalidade())
                .conjugeProfissao(contrato.getConjugeProfissao())
                .conjugeCpf(contrato.getConjugeCpf())
                .conjugeRg(contrato.getConjugeRg())
                // Página 3
                .imovelMatricula(contrato.getImovelMatricula())
                .imovelLivro(contrato.getImovelLivro())
                .imovelOficio(contrato.getImovelOficio())
                .imovelProprietario(contrato.getImovelProprietario())
                .imovelMomentoPosse(contrato.getImovelMomentoPosse())
                .imovelPrazoTransferencia(contrato.getImovelPrazoTransferencia())
                .imovelPrazoEscritura(contrato.getImovelPrazoEscritura())
                .imovelDescricao(contrato.getImovelDescricao())
                .permutaImovelMatricula(contrato.getPermutaImovelMatricula())
                .permutaImovelLivro(contrato.getPermutaImovelLivro())
                .permutaImovelOficio(contrato.getPermutaImovelOficio())
                .permutaImovelProprietario(contrato.getPermutaImovelProprietario())
                .permutaImovelMomentoPosse(contrato.getPermutaImovelMomentoPosse())
                .permutaImovelPrazoTransferencia(contrato.getPermutaImovelPrazoTransferencia())
                .permutaImovelPrazoEscritura(contrato.getPermutaImovelPrazoEscritura())
                .permutaImovelDescricao(contrato.getPermutaImovelDescricao())
                .veiculoMarca(contrato.getVeiculoMarca())
                .veiculoAno(contrato.getVeiculoAno())
                .veiculoModelo(contrato.getVeiculoModelo())
                .veiculoPlaca(contrato.getVeiculoPlaca())
                .veiculoChassi(contrato.getVeiculoChassi())
                .veiculoCor(contrato.getVeiculoCor())
                .veiculoMotor(contrato.getVeiculoMotor())
                .veiculoRenavam(contrato.getVeiculoRenavam())
                .veiculoDataEntrega(contrato.getVeiculoDataEntrega())
                // Página 4
                .negocioValorTotal(contrato.getNegocioValorTotal())
                .negocioValorEntrada(contrato.getNegocioValorEntrada())
                .negocioFormaPagamento(contrato.getNegocioFormaPagamento())
                .negocioNumParcelas(contrato.getNegocioNumParcelas())
                .negocioValorParcela(contrato.getNegocioValorParcela())
                .negocioVencimentos(contrato.getNegocioVencimentos())
                .negocioValorImovelPermuta(contrato.getNegocioValorImovelPermuta())
                .negocioValorVeiculoPermuta(contrato.getNegocioValorVeiculoPermuta())
                .negocioValorFinanciamento(contrato.getNegocioValorFinanciamento())
                .negocioPrazoPagamento(contrato.getNegocioPrazoPagamento())
                .contaTitular(contrato.getContaTitular())
                .contaBanco(contrato.getContaBanco())
                .contaAgencia(contrato.getContaAgencia())
                .contaPix(contrato.getContaPix())
                .honorariosValor(contrato.getHonorariosValor())
                .honorariosFormaPagamento(contrato.getHonorariosFormaPagamento())
                .honorariosDataPagamento(contrato.getHonorariosDataPagamento())
                .observacoes(contrato.getObservacoes())
                .dataContrato(contrato.getDataContrato())
                .assinaturaCorretor(contrato.getAssinaturaCorretor())
                .assinaturaAgenciador(contrato.getAssinaturaAgenciador())
                .assinaturaGestor(contrato.getAssinaturaGestor())
                .createdAt(contrato.getCreatedAt())
                .updatedAt(contrato.getUpdatedAt())
                .build();
    }
}
