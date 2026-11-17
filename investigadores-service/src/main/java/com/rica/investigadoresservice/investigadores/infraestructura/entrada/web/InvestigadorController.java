package com.rica.investigadoresservice.investigadores.infraestructura.entrada.web;

import com.rica.investigadoresservice.investigadores.aplicacion.InvestigadorUseCase;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Adaptador de entrada REST: traduce HTTP al puerto InvestigadorUseCase. */
@RestController
@RequestMapping("/api/investigadores")
public class InvestigadorController {

    private final InvestigadorUseCase investigadorService;

    public InvestigadorController(InvestigadorUseCase investigadorService) {
        this.investigadorService = investigadorService;
    }

    @GetMapping
    public List<InvestigadorResponse> listar() {
        return investigadorService.listarTodos().stream()
                .map(InvestigadorMapper::aResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public InvestigadorResponse buscarPorId(@PathVariable Long id) {
        return InvestigadorMapper.aResponse(investigadorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<InvestigadorResponse> registrar(@Valid @RequestBody InvestigadorRequest request) {
        Investigador guardado = investigadorService.registrar(
                request.getNombreCompleto(),
                request.getCorreoInstitucional(),
                request.getGrupoInvestigacion());
        return ResponseEntity
                .created(URI.create("/api/investigadores/" + guardado.getId()))
                .body(InvestigadorMapper.aResponse(guardado));
    }

}
