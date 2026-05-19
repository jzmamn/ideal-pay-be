package com.payroll.controller;

import com.payroll.dto.request.DepartmentRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.DepartmentResponseDTO;
import com.payroll.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // GET /payroll/department
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DepartmentResponseDTO>>> getAllDepartments(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Departments fetched successfully",
                departmentService.getAllDepartments(showDefaultRow, isActive)));
    }

    // GET /payroll/department/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DepartmentResponseDTO>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Department fetched successfully",
                departmentService.getDepartmentById(id)));
    }

    // POST /payroll/department
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DepartmentResponseDTO>> createDepartment(
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Department created successfully",
                        departmentService.createDepartment(requestDTO)));
    }

    // PUT /payroll/department/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DepartmentResponseDTO>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Department updated successfully",
                departmentService.updateDepartment(id, requestDTO)));
    }

    // DELETE /payroll/department/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Department deleted successfully", null));
    }
}
