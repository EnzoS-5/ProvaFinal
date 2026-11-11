package com.ProjSoft.ProvaFinal.Artigo;

import com.ProjSoft.ProvaFinal.Usuario.UsuarioCacheService;
import com.ProjSoft.ProvaFinal.Usuario.UsuarioResponseDTO;
import com.ProjSoft.ProvaFinal.Usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class ArtigoService {
    @Autowired
    private ArtigoRepository artigoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioCacheService usuarioCacheService;

    public Artigo criar(Artigo artigo){
        try {
            UsuarioResponseDTO criador = obterUsuario(artigo.getCriador());

            artigo.setId(artigo.getId());
            artigo.setTitulo(artigo.getTitulo());
            artigo.setDescricao(artigo.getDescricao());
            artigo.setPrioridade(artigo.getPrioridade());

            Artigo artigoSalvo = artigoRepository.save(artigo);

            return artigoSalvo;
        } catch (HttpClientErrorException e){
            throw new RuntimeException("Erro ao buscar dados do time no serviço externo", e);
        }
    }

    public Artigo buscarPorId(int id){
        return artigoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());
    }

    public Artigo deletePorId(int id){
        return artigoRepository.deleteById(id);
    }

    public List<Artigo> listarTodos() {
        return artigoRepository.findAll();
    }

    private  UsuarioResponseDTO obterUsuario(String id){
        if (usuarioCacheService.contemNoCache(id)) {
            return usuarioCacheService.obterDoCache(id);
        }

        UsuarioResponseDTO usuarioData = usuarioService.getUsuario(id);
        usuarioCacheService.armazenarNoCache(id,  usuarioData);
        return usuarioData;
    }
}
