package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.LoanResponseDTO;
import com.payroll.mapper.LoanMapper;
import com.payroll.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/loan-types")
@RequiredArgsConstructor
public class LoanController {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<LoanResponseDTO>>> getAll(
            @RequestParam(defaultValue = "false") boolean showDefaultRow) {
        List<LoanResponseDTO> result = loanRepository
                .findAll(Sort.by("id").ascending()).stream()
                .filter(l -> showDefaultRow || l.getId() != -1L)
                .map(loanMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(ApiResponseDTO.success("Loan types fetched successfully", result));
    }
}
