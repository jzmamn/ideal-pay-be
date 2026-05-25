package com.payroll.controller;

import com.payroll.dto.request.GrpRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.GrpResponseDTO;
import com.payroll.service.GrpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/group")
@RequiredArgsConstructor
public class GrpController {

    private final GrpService grpService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<GrpResponseDTO>>> getAllGroups(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Groups fetched successfully",
                grpService.getAllGroups(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GrpResponseDTO>> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Group fetched successfully",
                grpService.getGroupById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<GrpResponseDTO>> createGroup(
            @Valid @RequestBody GrpRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Group created successfully",
                        grpService.createGroup(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GrpResponseDTO>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GrpRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Group updated successfully",
                grpService.updateGroup(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteGroup(@PathVariable Long id) {
        grpService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Group deleted successfully", null));
    }
}
