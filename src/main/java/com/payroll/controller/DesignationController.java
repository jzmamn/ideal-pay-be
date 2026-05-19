package com.payroll.controller;

import com.payroll.dto.request.DesignationRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.DesignationResponseDTO;
import com.payroll.service.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/designation")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    // GET /payroll/designation
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DesignationResponseDTO>>> getAllDesignations(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Designations fetched successfully",
                designationService.getAllDesignations(showDefaultRow, isActive)));
    }

    // GET /payroll/designation/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DesignationResponseDTO>> getDesignationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Designation fetched successfully",
                designationService.getDesignationById(id)));
    }

    // POST /payroll/designation
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DesignationResponseDTO>> createDesignation(
            @Valid @RequestBody DesignationRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Designation created successfully",
                        designationService.createDesignation(requestDTO)));
    }

    // PUT /payroll/designation/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DesignationResponseDTO>> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Designation updated successfully",
                designationService.updateDesignation(id, requestDTO)));
    }

    // DELETE /payroll/designation/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Designation deleted successfully", null));
    }
}
