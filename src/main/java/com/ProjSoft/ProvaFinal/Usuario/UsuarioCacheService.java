package com.ProjSoft.ProvaFinal.Usuario;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UsuarioCacheService {
    private final Map<String, UsuarioResponseDTO> cache = new HashMap<>();

    public UsuarioResponseDTO obterDoCache(String id) {
        return cache.get(id);
    }

    public UsuarioResponseDTO armazenarNoCache(String id, UsuarioResponseDTO usuarioData) {
        return cache.put(id, usuarioData);
    }

    public boolean contemNoCache(String id) {
        return cache.containsKey(id);
    }

    public void limparCache() {
        cache.clear();
    }
}
