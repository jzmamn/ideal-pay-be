package com.payroll.controller;

import com.payroll.dto.request.StatusRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.StatusResponseDTO;
import com.payroll.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<StatusResponseDTO>>> getAllStatuses(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Statuses fetched successfully",
                statusService.getAllStatuses(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<StatusResponseDTO>> getStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Status fetched successfully",
                statusService.getStatusById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<StatusResponseDTO>> createStatus(
            @Valid @RequestBody StatusRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Status created successfully",
                        statusService.createStatus(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<StatusResponseDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Status updated successfully",
                statusService.updateStatus(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteStatus(@PathVariable Long id) {
        statusService.deleteStatus(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Status deleted successfully", null));
    }
}
