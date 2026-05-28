package com.payroll.controller;

import com.payroll.dto.request.TypeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.TypeResponseDTO;
import com.payroll.service.TypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/payroll/type")
@RequiredArgsConstructor
public class TypeController {

    private final TypeService typeService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TypeResponseDTO>>> getAllTypes(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Types fetched successfully",
                typeService.getAllTypes(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TypeResponseDTO>> getTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Type fetched successfully",
                typeService.getTypeById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<TypeResponseDTO>> createType(
            @Valid @RequestBody TypeRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Type created successfully",
                        typeService.createType(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TypeResponseDTO>> updateType(
            @PathVariable Long id,
            @Valid @RequestBody TypeRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Type updated successfully",
                typeService.updateType(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteType(@PathVariable Long id) {
        typeService.deleteType(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Type deleted successfully", null));
    }
}
