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

import api.dto.ConsultaRequestDTO;
import api.dto.ConsultaResponseDTO;
import api.dto.ErroDTO;
import api.service.ConsultaService;

@Tag(name = "Consultas", description = "Marcação, consulta, cancelamento e conclusão de consultas, com as regras R1 a R4")
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @Operation(summary = "Marca uma consulta",
            description = "R1: o profissional não pode ter duas consultas no mesmo horário. "
                    + "R2: o paciente só pode ter uma consulta agendada por especialidade. "
                    + "R3: atendimento de segunda a sexta, das 08:00 às 18:00, de 30 em 30 minutos.")
    @ApiResponse(responseCode = "201", description = "Consulta marcada, com status AGENDADA")
    @ApiResponse(responseCode = "400", description = "Corpo inválido, ou regra R1, R2 ou R3 violada (a mensagem diz qual)",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "O paciente ou o profissional não existe",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> agendar(@Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(dto));
    }

    @Operation(summary = "Lista as consultas", description = "Da mais próxima para a mais distante. Sem consultas, a lista vem vazia, não 404.")
    @ApiResponse(responseCode = "200", description = "Lista de consultas (pode vir vazia)")
    @GetMapping
    public List<ConsultaResponseDTO> listar() {
        return service.listarTodas();
    }

    @Operation(summary = "Busca uma consulta pelo id")
    @ApiResponse(responseCode = "200", description = "Consulta encontrada")
    @ApiResponse(responseCode = "404", description = "Não existe consulta com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}")
    public ConsultaResponseDTO buscarPorId(
            @Parameter(description = "Id da consulta", example = "1") @PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cancela uma consulta", description = "O horário volta a ficar livre. "
            + "R4: o cancelamento precisa ser feito com pelo menos 2 horas de antecedência.")
    @ApiResponse(responseCode = "200", description = "Consulta cancelada")
    @ApiResponse(responseCode = "400", description = "A consulta não está mais agendada, ou a regra R4 foi violada",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe consulta com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PutMapping("/{id}/cancelar")
    public ConsultaResponseDTO cancelar(
            @Parameter(description = "Id da consulta", example = "1") @PathVariable Long id) {
        return service.cancelar(id);
    }

    @Operation(summary = "Conclui uma consulta", description = "Registra que o paciente foi atendido (status REALIZADA). "
            + "Só uma consulta agendada pode ser concluída.")
    @ApiResponse(responseCode = "200", description = "Consulta concluída")
    @ApiResponse(responseCode = "400", description = "A consulta não está mais agendada",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe consulta com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PutMapping("/{id}/concluir")
    public ConsultaResponseDTO concluir(
            @Parameter(description = "Id da consulta", example = "1") @PathVariable Long id) {
        return service.concluir(id);
    }
}
