package com.ProjSoft.ProvaFinal.Usuario;

import lombok.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UsuarioService {
    @Value("${usuario.app.url}")
    private String usuarioUrl;

    public UsuarioResponseDTO getUsuario(String idUsuario){
        RestClient restClient = RestClient.builder().build();

        return restClient.get().uri(usuarioUrl+"/"+idUsuario).retrieve().body(UsuarioResponseDTO.class);
    }
}
