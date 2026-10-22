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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.ErroDTO;
import api.dto.ProfissionalRequestDTO;
import api.dto.ProfissionalResponseDTO;
import api.service.ProfissionalService;

@Tag(name = "Profissionais", description = "Cadastro e consulta dos profissionais que atendem na clínica")
@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um profissional", description = "O id é definido pela API. "
            + "Dois profissionais não podem ter o mesmo registro profissional.")
    @ApiResponse(responseCode = "201", description = "Profissional cadastrado: o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido (a lista \"campos\" diz o que corrigir) "
            + "ou registro profissional já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> criar(@Valid @RequestBody ProfissionalRequestDTO dto) {
        ProfissionalResponseDTO criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @Operation(summary = "Lista os profissionais", description = "Sem profissionais cadastrados, a lista vem vazia, não 404.")
    @ApiResponse(responseCode = "200", description = "Lista de profissionais (pode vir vazia)")
    @GetMapping
    public List<ProfissionalResponseDTO> listar() {
        return service.listarTodos();
    }

    @Operation(summary = "Busca um profissional pelo id")
    @ApiResponse(responseCode = "200", description = "Profissional encontrado")
    @ApiResponse(responseCode = "404", description = "Não existe profissional com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}")
    public ProfissionalResponseDTO buscarPorId(
            @Parameter(description = "Id do profissional", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Atualiza um profissional", description = "Substitui nome, especialidade e registro. "
            + "O registro não pode ser o de outro profissional.")
    @ApiResponse(responseCode = "200", description = "Profissional atualizado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido (a lista \"campos\" diz o que corrigir) "
            + "ou registro profissional de outro cadastro",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe profissional com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PutMapping("/{id}")
    public ProfissionalResponseDTO atualizar(
            @Parameter(description = "Id do profissional", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProfissionalRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @Operation(summary = "Remove um profissional", description = "Não é possível remover quem já tem consultas registradas.")
    @ApiResponse(responseCode = "204", description = "Profissional removido")
    @ApiResponse(responseCode = "400", description = "O profissional tem consultas registradas",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe profissional com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "Id do profissional", example = "1") @PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
