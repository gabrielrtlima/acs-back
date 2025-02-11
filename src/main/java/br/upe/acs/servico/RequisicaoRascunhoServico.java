package br.upe.acs.servico;

import br.upe.acs.dominio.Certificado;
import br.upe.acs.dominio.Requisicao;
import br.upe.acs.dominio.Usuario;
import br.upe.acs.dominio.enums.CertificadoStatusEnum;
import br.upe.acs.dominio.enums.RequisicaoStatusEnum;
import br.upe.acs.repositorio.CertificadoRepositorio;
import br.upe.acs.repositorio.RequisicaoRepositorio;
import br.upe.acs.utils.AcsExcecao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class RequisicaoRascunhoServico {

    private final RequisicaoRepositorio repositorio;

    private final CertificadoRepositorio certificadoRepositorio;

    private final RequisicaoServico requisicaoServico;

    private final UsuarioServico usuarioServico;

    private final EmailServico emailServico;

    public Long adicionarRequisicao(String email) throws AcsExcecao {
        Usuario aluno = usuarioServico.buscarUsuarioPorEmail(email);
        List<Requisicao> requisicoesRacunhos = aluno.getRequisicoes().stream()
                .filter(requisicao -> requisicao.getStatusRequisicao().equals(RequisicaoStatusEnum.RASCUNHO)).toList();

        if (aluno.getHorasEnsino() + aluno.getHorasExtensao() + aluno.getHorasGestao() + aluno.getHorasPesquisa() >= aluno.getCurso().getHorasComplementares()) {
            throw new AcsExcecao("O aluno já cumpriu suas horas complementares!");
        }

        if (requisicoesRacunhos.size() >= 2) {
            throw new AcsExcecao("aluno só pode possuir 2 requisições em rascunho!");
        }

        Requisicao requisicao = new Requisicao();
        requisicao.setStatusRequisicao(RequisicaoStatusEnum.RASCUNHO);
        requisicao.setCriacao(new Date());
        requisicao.setUsuario(aluno);
        requisicao.setCurso(aluno.getCurso());
        Requisicao requisicaoSalva = repositorio.save(requisicao);
        requisicaoSalva.setIdRequisicao(String.format("%s-%05d",aluno.getCurso().getSigla(), requisicaoSalva.getId()));
        repositorio.save(requisicaoSalva);
        return requisicaoSalva.getId();
    }

    public String submeterRequisicao(Long requisicaoId) throws AcsExcecao {
        Requisicao requisicao = requisicaoServico.buscarRequisicaoPorId(requisicaoId);

        if (requisicao.getStatusRequisicao() != RequisicaoStatusEnum.RASCUNHO) {
            throw new AcsExcecao("Essa requisição já foi submetido!");
        }

        if (requisicao.getCertificados().isEmpty()) {
            throw new AcsExcecao("Uma requisição precisa de pelo menos um certificado!");
        }
        List<Certificado> certificadosInvalidas = requisicao.getCertificados().stream()
                .filter(certificado -> !validarCertificado(certificado)).toList();
        if (!certificadosInvalidas.isEmpty()) {
            throw new AcsExcecao(
                    "Certificados: " + String.join( "; ", certificadosInvalidas.stream()
                            .map(certificado -> certificado.getId().toString()).toList())
                            + " possuem dados inválidos."
            );
        }
        String token = gerarTokenRequisicao();
        requisicao.setToken(token);
        requisicao.setDataDeSubmissao(new Date());
        requisicao.setStatusRequisicao(RequisicaoStatusEnum.TRANSITO);
        modificarCertificados(requisicao.getCertificados());
        repositorio.save(requisicao);

        CompletableFuture.runAsync(() -> emailServico.enviarEmailAlteracaoStatusRequisicao(requisicao));

        return token;
    }

    public void excluirRequisicao(Long requisicaoId, String email) throws AcsExcecao {
        Requisicao requisicao = requisicaoServico.buscarRequisicaoPorId(requisicaoId);
        if (!requisicao.getUsuario().getEmail().equals(email)) {
            throw new AcsExcecao("Usuário sem premissão para excluir esse requisição!");
        }

        if (!requisicao.getStatusRequisicao().equals(RequisicaoStatusEnum.RASCUNHO)) {
            throw new AcsExcecao("Um requisição já submetido não pode ser apagado!");
        }

        repositorio.deleteById(requisicaoId);
    }

    private boolean validarCertificado(Certificado certificado) {
        boolean isValid = true;

        if (certificado.getCertificado() == null){
            isValid = false;
        } else if (certificado.getTitulo() == null || certificado.getTitulo().isBlank()) {
            isValid = false;
        } else if (certificado.getDataInicial().after(new Date())) {
            isValid = false;
        } else if (certificado.getDataFinal().after(new Date())) {
            isValid = false;
        } else if (certificado.getCargaHoraria() < 1) {
            isValid = false;
        } else if (certificado.getAtividade() == null) {
            isValid = false;
        }

        return isValid;
    }

    private String gerarTokenRequisicao() {
        String caracteres = "0123456789!@#$%.*";
        Random random = new Random();
        StringBuilder tokenParcial = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            tokenParcial.append(caracteres.charAt(index));
        }

        Instant timeStamp = Instant.now();
        long epocaSegundos = timeStamp.getEpochSecond();

        return tokenParcial + Long.toString(epocaSegundos);
    }

    private void modificarCertificados(List<Certificado> certificados) {
        for (Certificado certificado: certificados) {
            certificado.setStatusCertificado(CertificadoStatusEnum.ENCAMINHADO_COORDENACAO);
            certificadoRepositorio.save(certificado);
        }
    }
}
