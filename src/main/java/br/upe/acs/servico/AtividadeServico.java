package br.upe.acs.servico;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.upe.acs.dominio.Atividade;
import br.upe.acs.dominio.enums.EixoEnum;
import br.upe.acs.repositorio.AtividadeRepositorio;
import br.upe.acs.utils.AcsExcecao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtividadeServico {

	private final AtividadeRepositorio repositorio;
	
	public List<Atividade> listarAtividades() {
		return repositorio.findAll();
	}
	
	public Atividade buscarAtividadePorId(Long id) throws AcsExcecao {
		Optional<Atividade> atividade = repositorio.findById(id);
		if (atividade.isEmpty()) {
			throw new AcsExcecao("Não existe uma atividade associada a este id!");
		}
		
		return atividade.get();
	}
	
	public List<Atividade> buscarAtividadePorEixo(String eixo) throws AcsExcecao{
		boolean existe = false;		
		EixoEnum eixoFormato = null;
		for(EixoEnum c : EixoEnum.values()) {
			if(c.toString().equalsIgnoreCase(eixo)) {
				existe = true;
				eixoFormato = c;
			};			
		}
		if(!existe) {
			throw new AcsExcecao("Não existe um eixo correspondente");
		}
		
		List<Atividade> atividade = repositorio.findByEixo(eixoFormato);

		if(atividade.isEmpty()) {
			throw new AcsExcecao("Não existe atividade associada a esse eixo");
		}
		
		return atividade;
	}
		
}
