package api.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.ErroDTO;
import api.dto.PacienteRequestDTO;
import api.dto.PacienteResponseDTO;
import api.service.PacienteService;

@Tag(name = "Pacientes", description = "Cadastro e consulta de pacientes")
@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um paciente")
    @ApiResponse(responseCode = "201", description = "Paciente cadastrado: o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: a lista \"campos\" diz o que corrigir",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> criar(@Valid @RequestBody PacienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @Operation(summary = "Lista os pacientes", description = "Sem pacientes cadastrados, a lista vem vazia, não 404.")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes (pode vir vazia)")
    @GetMapping
    public List<PacienteResponseDTO> listar() {
        return service.listarTodos();
    }

    @Operation(summary = "Busca um paciente pelo id")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Não existe paciente com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}")
    public PacienteResponseDTO buscarPorId(
            @Parameter(description = "Id do paciente", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Atualiza um paciente", description = "Substitui nome, e-mail e telefone. O id não muda.")
    @ApiResponse(responseCode = "200", description = "Paciente atualizado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: a lista \"campos\" diz o que corrigir",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe paciente com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PutMapping("/{id}")
    public PacienteResponseDTO atualizar(
            @Parameter(description = "Id do paciente", example = "1") @PathVariable Long id,
            @Valid @RequestBody PacienteRequestDTO dto) {
        return service.atualizar(id, dto);
    }
}
