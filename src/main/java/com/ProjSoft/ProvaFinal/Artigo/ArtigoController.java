package com.ProjSoft.ProvaFinal.Artigo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artigos")
public class ArtigoController {
    @Autowired
    private ArtigoService artigoService;

    @GetMapping
    public List<Artigo> listarTodos(){
        return artigoService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artigo criar(@RequestBody Artigo artigo){
        return artigoService.criar(artigo);
    }

    @GetMapping("/{id}")
    public Artigo buscarPorId(@PathVariable Integer id){
        return artigoService.buscarPorId(id);
    }

    @GetMapping("/{id}")
    public Artigo deletar(@PathVariable Integer id){
        return artigoService.deletePorId(id);
    }
}
