package com.advocacia.service;

import com.advocacia.dto.*;
import com.advocacia.entity.Contrato;
import com.advocacia.entity.ContratoComprador;
import com.advocacia.entity.ContratoStatus;
import com.advocacia.entity.ContratoVendedor;
import com.advocacia.repository.ContratoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratoService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ContratoRepository contratoRepository;
    private final ContratoAuditService auditService;

    @Transactional(readOnly = true)
    public List<ContratoResponse> findAll() {
        return contratoRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
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
        log.info("[HISTORICO] UPDATE contrato id={}, payload negocioValorTotal={}", id, request.getNegocioValorTotal());

        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        // Snapshot antes da alteração
        ContratoResponse before = toResponse(contrato);
        log.info("[HISTORICO] contrato id={} BEFORE negocioValorTotal={}", id, before.getNegocioValorTotal());

        updateContratoFromRequest(contrato, request);
        contrato = contratoRepository.save(contrato);

        ContratoResponse after = toResponse(contrato);
        log.info("[HISTORICO] contrato id={} AFTER negocioValorTotal={}", id, after.getNegocioValorTotal());

        try {
            auditService.recordChanges(id, before, after);
        } catch (Exception e) {
            log.warn("Auditoria não registrada para contrato {}: {}", id, e.getMessage(), e);
            if (!Objects.equals(before.getNegocioValorTotal(), after.getNegocioValorTotal())) {
                try {
                    auditService.recordSimpleChange(id, "negocioValorTotal",
                            before.getNegocioValorTotal() != null ? before.getNegocioValorTotal().toPlainString() : "null",
                            after.getNegocioValorTotal() != null ? after.getNegocioValorTotal().toPlainString() : "null");
                } catch (Exception fallbackEx) {
                    log.warn("Fallback de auditoria (negocioValorTotal) também falhou para contrato {}: {}", id, fallbackEx.getMessage());
                }
            }
        }

        return after;
    }

    @Transactional
    public ContratoResponse finalizar(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        if (contrato.getStatus() == ContratoStatus.FINAL) {
            throw new RuntimeException("Contrato já está finalizado");
        }

        String oldStatus = contrato.getStatus().name();
        contrato.setStatus(ContratoStatus.FINAL);
        contrato = contratoRepository.save(contrato);

        try {
            auditService.recordSimpleChange(id, "status", oldStatus, "FINAL");
        } catch (Exception e) {
            log.warn("Auditoria não registrada para contrato {} (finalizar): {}", id, e.getMessage());
        }

        return toResponse(contrato);
    }

    @Transactional
    public void delete(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
        contratoRepository.delete(contrato);
    }

    @Transactional
    public void deleteAll() {
        contratoRepository.deleteAll();
    }

    @Transactional
    public ContratoResponse updateVendedores(Long id, List<VendedorRequest> vendedores) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        // Snapshot antes
        ContratoResponse before = toResponse(contrato);

        contrato.getVendedores().clear();
        if (vendedores != null) {
            for (int i = 0; i < vendedores.size(); i++) {
                VendedorRequest vr = vendedores.get(i);
                ContratoVendedor v = ContratoVendedor.builder()
                        .contrato(contrato)
                        .ordem(i)
                        .nome(vr.getNome())
                        .documento(sanitizeDigits(vr.getDocumento()))
                        .email(vr.getEmail())
                        .telefone(vr.getTelefone())
                        .endereco(vr.getEndereco())
                        .socioNome(vr.getSocioNome())
                        .socioCpf(vr.getSocioCpf())
                        .socioNacionalidade(vr.getSocioNacionalidade())
                        .socioProfissao(vr.getSocioProfissao())
                        .socioEstadoCivil(vr.getSocioEstadoCivil())
                        .socioRegimeBens(vr.getSocioRegimeBens())
                        .socioRg(vr.getSocioRg())
                        .socioCnh(vr.getSocioCnh())
                        .socioEmail(vr.getSocioEmail())
                        .socioTelefone(vr.getSocioTelefone())
                        .socioEndereco(vr.getSocioEndereco())
                        .build();
                contrato.getVendedores().add(v);
            }
        }
        contrato = contratoRepository.save(contrato);

        ContratoResponse after = toResponse(contrato);
        try {
            auditService.recordChanges(id, before, after);
        } catch (Exception e) {
            log.warn("Auditoria não registrada para contrato {} (vendedores): {}", id, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return after;
    }

    @Transactional
    public ContratoResponse updateCompradores(Long id, List<CompradorRequest> compradores) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

        // Snapshot antes
        ContratoResponse before = toResponse(contrato);

        contrato.getCompradores().clear();
        if (compradores != null) {
            for (int i = 0; i < compradores.size(); i++) {
                CompradorRequest cr = compradores.get(i);
                ContratoComprador c = ContratoComprador.builder()
                        .contrato(contrato)
                        .ordem(i)
                        .nome(cr.getNome())
                        .documento(sanitizeDigits(cr.getDocumento()))
                        .nacionalidade(cr.getNacionalidade())
                        .profissao(cr.getProfissao())
                        .estadoCivil(cr.getEstadoCivil())
                        .regimeBens(cr.getRegimeBens())
                        .rg(cr.getRg())
                        .cnh(cr.getCnh())
                        .email(cr.getEmail())
                        .telefone(cr.getTelefone())
                        .endereco(cr.getEndereco())
                        .conjugeNome(cr.getConjugeNome())
                        .conjugeCpf(cr.getConjugeCpf())
                        .conjugeNacionalidade(cr.getConjugeNacionalidade())
                        .conjugeProfissao(cr.getConjugeProfissao())
                        .conjugeRg(cr.getConjugeRg())
                        .build();
                contrato.getCompradores().add(c);
            }
        }
        contrato = contratoRepository.save(contrato);

        ContratoResponse after = toResponse(contrato);
        try {
            auditService.recordChanges(id, before, after);
        } catch (Exception e) {
            log.warn("Auditoria não registrada para contrato {} (compradores): {}", id, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return after;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sanitizeDigits(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    private List<ParcelaItemDto> parseParcelas(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<ParcelaItemDto>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Erro ao deserializar parcelas: {}", e.getMessage());
            return null;
        }
    }

    private void updateContratoFromRequest(Contrato contrato, ContratoRequest request) {
        if (request == null) return;

        if (request.getPaginaAtual() != null) contrato.setPaginaAtual(request.getPaginaAtual());

        // ========== VENDEDORES ==========
        if (request.getVendedores() != null) {
            contrato.getVendedores().clear();
            for (int i = 0; i < request.getVendedores().size(); i++) {
                VendedorRequest vr = request.getVendedores().get(i);
                ContratoVendedor vendedor = ContratoVendedor.builder()
                        .contrato(contrato)
                        .ordem(i)
                        .nome(vr.getNome())
                        .documento(sanitizeDigits(vr.getDocumento()))
                        .email(vr.getEmail())
                        .telefone(vr.getTelefone())
                        .endereco(vr.getEndereco())
                        .socioNome(vr.getSocioNome())
                        .socioCpf(vr.getSocioCpf())
                        .socioNacionalidade(vr.getSocioNacionalidade())
                        .socioProfissao(vr.getSocioProfissao())
                        .socioEstadoCivil(vr.getSocioEstadoCivil())
                        .socioRegimeBens(vr.getSocioRegimeBens())
                        .socioRg(vr.getSocioRg())
                        .socioCnh(vr.getSocioCnh())
                        .socioEmail(vr.getSocioEmail())
                        .socioTelefone(vr.getSocioTelefone())
                        .socioEndereco(vr.getSocioEndereco())
                        .build();
                contrato.getVendedores().add(vendedor);
            }
        }

        // ========== COMPRADORES ==========
        if (request.getCompradores() != null) {
            contrato.getCompradores().clear();
            for (int i = 0; i < request.getCompradores().size(); i++) {
                CompradorRequest cr = request.getCompradores().get(i);
                ContratoComprador comprador = ContratoComprador.builder()
                        .contrato(contrato)
                        .ordem(i)
                        .nome(cr.getNome())
                        .documento(sanitizeDigits(cr.getDocumento()))
                        .nacionalidade(cr.getNacionalidade())
                        .profissao(cr.getProfissao())
                        .estadoCivil(cr.getEstadoCivil())
                        .regimeBens(cr.getRegimeBens())
                        .rg(cr.getRg())
                        .cnh(cr.getCnh())
                        .email(cr.getEmail())
                        .telefone(cr.getTelefone())
                        .endereco(cr.getEndereco())
                        .conjugeNome(cr.getConjugeNome())
                        .conjugeCpf(cr.getConjugeCpf())
                        .conjugeNacionalidade(cr.getConjugeNacionalidade())
                        .conjugeProfissao(cr.getConjugeProfissao())
                        .conjugeRg(cr.getConjugeRg())
                        .build();
                contrato.getCompradores().add(comprador);
            }
        }

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
        if (request.getVeiculoKm() != null) contrato.setVeiculoKm(request.getVeiculoKm());

        // Página 4: Negócio (valor 0 é aplicado quando enviado; null = campo não enviado)
        if (request.getNegocioValorTotal() != null) contrato.setNegocioValorTotal(request.getNegocioValorTotal());
        if (request.getNegocioValorEntrada() != null) contrato.setNegocioValorEntrada(request.getNegocioValorEntrada());
        if (request.getNegocioFormaPagamento() != null) contrato.setNegocioFormaPagamento(request.getNegocioFormaPagamento());
        if (request.getNegocioNumParcelas() != null) contrato.setNegocioNumParcelas(request.getNegocioNumParcelas());
        if (request.getNegocioValorParcela() != null) contrato.setNegocioValorParcela(request.getNegocioValorParcela());
        if (request.getNegocioVencimentos() != null) contrato.setNegocioVencimentos(request.getNegocioVencimentos());
        // Valores de permuta: null do request vira 0 (compatibilidade)
        contrato.setNegocioValorImovelPermuta(request.getNegocioValorImovelPermuta() != null ? request.getNegocioValorImovelPermuta() : BigDecimal.ZERO);
        contrato.setNegocioValorVeiculoPermuta(request.getNegocioValorVeiculoPermuta() != null ? request.getNegocioValorVeiculoPermuta() : BigDecimal.ZERO);
        if (request.getNegocioValorFinanciamento() != null) contrato.setNegocioValorFinanciamento(request.getNegocioValorFinanciamento());
        if (request.getNegocioPrazoPagamento() != null) contrato.setNegocioPrazoPagamento(request.getNegocioPrazoPagamento());
        if (request.getNegocioDataPrimeiraParcela() != null) contrato.setNegocioDataPrimeiraParcela(request.getNegocioDataPrimeiraParcela());
        if (request.getParcelas() != null) {
            try {
                contrato.setNegocioParcelas(OBJECT_MAPPER.writeValueAsString(request.getParcelas()));
            } catch (JsonProcessingException e) {
                log.warn("Erro ao serializar parcelas: {}", e.getMessage());
                contrato.setNegocioParcelas(null);
            }
        }

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

        // Validação jurídica: valores de permuta não negativos e soma não pode exceder valor total
        BigDecimal imovelPermuta = contrato.getNegocioValorImovelPermuta() != null ? contrato.getNegocioValorImovelPermuta() : BigDecimal.ZERO;
        BigDecimal veiculoPermuta = contrato.getNegocioValorVeiculoPermuta() != null ? contrato.getNegocioValorVeiculoPermuta() : BigDecimal.ZERO;
        if (imovelPermuta.compareTo(BigDecimal.ZERO) < 0 || veiculoPermuta.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Valores de permuta não podem ser negativos.");
        }
        if (contrato.getNegocioValorTotal() != null && contrato.getNegocioValorTotal().compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal somaPermuta = imovelPermuta.add(veiculoPermuta);
            if (somaPermuta.compareTo(contrato.getNegocioValorTotal()) > 0) {
                throw new RuntimeException("A soma dos bens em permuta não pode exceder o valor total do negócio.");
            }
        }
    }

    private ContratoResponse toResponse(Contrato contrato) {
        List<VendedorResponse> vendedorResponses = contrato.getVendedores() != null
                ? contrato.getVendedores().stream().map(this::toVendedorResponse).collect(Collectors.toList())
                : new ArrayList<>();

        List<CompradorResponse> compradorResponses = contrato.getCompradores() != null
                ? contrato.getCompradores().stream().map(this::toCompradorResponse).collect(Collectors.toList())
                : new ArrayList<>();

        return ContratoResponse.builder()
                .id(contrato.getId())
                .status(contrato.getStatus())
                .paginaAtual(contrato.getPaginaAtual())
                .vendedores(vendedorResponses)
                .compradores(compradorResponses)
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
                .veiculoKm(contrato.getVeiculoKm())
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
                .negocioDataPrimeiraParcela(contrato.getNegocioDataPrimeiraParcela())
                .parcelas(parseParcelas(contrato.getNegocioParcelas()))
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

    private VendedorResponse toVendedorResponse(ContratoVendedor v) {
        return VendedorResponse.builder()
                .id(v.getId())
                .ordem(v.getOrdem())
                .nome(v.getNome())
                .documento(v.getDocumento())
                .email(v.getEmail())
                .telefone(v.getTelefone())
                .endereco(v.getEndereco())
                .socioNome(v.getSocioNome())
                .socioCpf(v.getSocioCpf())
                .socioNacionalidade(v.getSocioNacionalidade())
                .socioProfissao(v.getSocioProfissao())
                .socioEstadoCivil(v.getSocioEstadoCivil())
                .socioRegimeBens(v.getSocioRegimeBens())
                .socioRg(v.getSocioRg())
                .socioCnh(v.getSocioCnh())
                .socioEmail(v.getSocioEmail())
                .socioTelefone(v.getSocioTelefone())
                .socioEndereco(v.getSocioEndereco())
                .build();
    }

    private CompradorResponse toCompradorResponse(ContratoComprador c) {
        return CompradorResponse.builder()
                .id(c.getId())
                .ordem(c.getOrdem())
                .nome(c.getNome())
                .documento(c.getDocumento())
                .nacionalidade(c.getNacionalidade())
                .profissao(c.getProfissao())
                .estadoCivil(c.getEstadoCivil())
                .regimeBens(c.getRegimeBens())
                .rg(c.getRg())
                .cnh(c.getCnh())
                .email(c.getEmail())
                .telefone(c.getTelefone())
                .endereco(c.getEndereco())
                .conjugeNome(c.getConjugeNome())
                .conjugeCpf(c.getConjugeCpf())
                .conjugeNacionalidade(c.getConjugeNacionalidade())
                .conjugeProfissao(c.getConjugeProfissao())
                .conjugeRg(c.getConjugeRg())
                .build();
    }
}
