package com.payroll.controller;

import com.payroll.dto.request.UserGroupRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.UserGroupResponseDTO;
import com.payroll.service.UserGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/usr-grp")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UserGroupResponseDTO>>> getAllUserGroups(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User groups fetched successfully",
                userGroupService.getAllUserGroups(showDefaultRow)));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<UserGroupResponseDTO>>> getUserGroupsByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User groups fetched successfully",
                userGroupService.getUserGroupsByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserGroupResponseDTO>> getUserGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User group fetched successfully",
                userGroupService.getUserGroupById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserGroupResponseDTO>> createUserGroup(
            @Valid @RequestBody UserGroupRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "User group created successfully",
                        userGroupService.createUserGroup(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserGroupResponseDTO>> updateUserGroup(
            @PathVariable Long id,
            @Valid @RequestBody UserGroupRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User group updated successfully",
                userGroupService.updateUserGroup(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUserGroup(@PathVariable Long id) {
        userGroupService.deleteUserGroup(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User group deleted successfully", null));
    }
}
