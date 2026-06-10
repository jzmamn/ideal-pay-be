package com.payroll.service;

import com.payroll.dto.request.EmpGratuityRequest;
import com.payroll.dto.response.EmpGratuityResponse;
import com.payroll.enums.GratuityStatus;

import java.util.List;

public interface EmpGratuityService {

    List<EmpGratuityResponse> getAll();

    List<EmpGratuityResponse> getByStatus(GratuityStatus status);

    List<EmpGratuityResponse> getByEmployee(Long empId);

    EmpGratuityResponse getById(Long id);

    EmpGratuityResponse create(EmpGratuityRequest request);

    EmpGratuityResponse update(Long id, EmpGratuityRequest request);

    void delete(Long id);

    EmpGratuityResponse approve(Long id);

    EmpGratuityResponse markPaid(Long id);

    EmpGratuityResponse cancel(Long id);

    String nextCode();
}
