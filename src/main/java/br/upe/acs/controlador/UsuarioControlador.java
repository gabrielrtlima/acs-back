package br.upe.acs.controlador;

import br.upe.acs.config.JwtService;
import br.upe.acs.controlador.respostas.UsuarioResposta;
import br.upe.acs.dominio.dto.AlterarSenhaDTO;
import br.upe.acs.servico.UsuarioServico;
import br.upe.acs.utils.AcsExcecao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/usuario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioControlador {
    private final UsuarioServico servico;
    
    private final JwtService jwtService;
    
    @Operation(
            summary = "Buscar usuário por id",
            description = "Esta rota permite buscar um usuário via seu id. As informações retornadas incluem " +
                    "informações como id, nome completo, número de matricula, telefone, email, perfis, curso, " +
                    "periodo e se é verificado. Essa rota séra util para gerenciamento de usuarios."
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarUsuarioPorId(@PathVariable("id") Long id){
    	ResponseEntity<?> resposta;
    	try {
    		UsuarioResposta usuarioResposta = new UsuarioResposta(servico.buscarUsuarioPorId(id));
    		resposta = ResponseEntity.ok(usuarioResposta);
    	} catch(AcsExcecao e){
    		resposta = ResponseEntity.badRequest().body(e.getMessage());  		
    	}
    	
    	return resposta;
    }
    
    @Operation(
            summary = "Listar requisicões do aluno páginada",
            description = "Essa rota permite buscar requisições já submetidas por um aluno de forma páginada. " +
                    "Possui com retorno uma Map 'requisicoes' com uma lista de de requisições, paginaAtual, totalItens e totalPaginas. " +
                    "Essa rota séra util para análise de requisições por parte dos coordenação e comissão."
    )
    @GetMapping("/requisicao/paginacao")
    public  ResponseEntity<?> listarRequisicaoPorAlunoPaginacao(
            @RequestParam Long alunoId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int quantidade
    ) {
        ResponseEntity<?> resposta;
        try {
            resposta = ResponseEntity.ok(servico.listarRequisicoesPorAlunoPaginadas(alunoId, pagina, quantidade));

        } catch (AcsExcecao e) {
            resposta = ResponseEntity.badRequest().body(e.getMessage());
        }

        return resposta;
    }

    @Operation(
            summary = "Retornar dados de perfil do usuário",
            description = "Essa Rota permite o usuário ter acesso a suas informações no registrados. Possui com retorno " +
                    "informações como id, nome completo, número de matricula, telefone, email, perfis, curso, " +
                    "periodo e se é verificado. Essa rota séra util para acesso de suas informações pelo usuário."
    )
    @GetMapping("/me")
    public ResponseEntity<?> retornarPerfilDoUsuario(HttpServletRequest request) {

        ResponseEntity<?> resposta;
        try {
            String email = jwtService.extractUsername(request.getHeader("Authorization").substring(7));
            var usuarioResposta = new UsuarioResposta(servico.buscarUsuarioPorEmail(email));
            resposta = ResponseEntity.ok(usuarioResposta);
        } catch (AcsExcecao e) {
            resposta = ResponseEntity.badRequest().body(e.getMessage());
        }

        return resposta;
    }

    @Operation(summary = "Verificar usuário")
    @PostMapping("/verificacao")
    public ResponseEntity<?> verificarUsuario(HttpServletRequest request,
                                              @RequestParam(value = "codigoDeVerificacao") String codigo) {
        ResponseEntity<?> resposta;
        try {
            String email = jwtService.extractUsername(request.getHeader("Authorization").substring(7));
            resposta = ResponseEntity.ok(servico.verificarUsuario(email, codigo));
        } catch (AcsExcecao e) {
            resposta = ResponseEntity.status(406).body(e.getMessage());
        }

        return resposta;
    }

    @Operation(summary = "Alterar senha do usuário")
    @PatchMapping("/senha")
    public ResponseEntity<?> alterarSenha(
            HttpServletRequest request,
            @RequestBody AlterarSenhaDTO alterarSenhaDTO
    ) {
        String token = request.getHeader("Authorization").substring(7);
        ResponseEntity<?> resposta;
        try {
            servico.alterarSenha(token, alterarSenhaDTO.getSenha(), alterarSenhaDTO.getNovaSenha());
            resposta = ResponseEntity.noContent().build();
        } catch (AcsExcecao e) {
            resposta = ResponseEntity.badRequest().body(e.getMessage());
        }

        return resposta;
    }

}
