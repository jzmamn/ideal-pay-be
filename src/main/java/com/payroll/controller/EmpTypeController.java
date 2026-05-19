package com.payroll.controller;

import com.payroll.dto.request.EmpTypeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmpTypeResponseDTO;
import com.payroll.service.EmpTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-type")
@RequiredArgsConstructor
public class EmpTypeController {

    private final EmpTypeService empTypeService;

    // GET /payroll/emp-type
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmpTypeResponseDTO>>> getAllEmpTypes(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee types fetched successfully",
                empTypeService.getAllEmpTypes(showDefaultRow, isActive)));
    }

    // GET /payroll/emp-type/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmpTypeResponseDTO>> getEmpTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee type fetched successfully",
                empTypeService.getEmpTypeById(id)));
    }

    // POST /payroll/emp-type
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmpTypeResponseDTO>> createEmpType(
            @Valid @RequestBody EmpTypeRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Employee type created successfully",
                        empTypeService.createEmpType(requestDTO)));
    }

    // PUT /payroll/emp-type/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmpTypeResponseDTO>> updateEmpType(
            @PathVariable Long id,
            @Valid @RequestBody EmpTypeRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee type updated successfully",
                empTypeService.updateEmpType(id, requestDTO)));
    }

    // DELETE /payroll/emp-type/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmpType(@PathVariable Long id) {
        empTypeService.deleteEmpType(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Employee type deleted successfully", null));
    }
}
