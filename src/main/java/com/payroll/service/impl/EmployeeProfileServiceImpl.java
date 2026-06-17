package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeProfileSaveRequestDTO;
import com.payroll.dto.response.*;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.repository.*;
import com.payroll.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payroll.service.PayrollPeriodService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final PayrollPeriodService payrollPeriodService;
    private final EmployeeService employeeService;
    private final EmployeeFixedAllowanceService employeeFixedAllowanceService;
    private final EmployeeFixedDeductionService employeeFixedDeductionService;
    private final EmployeeVariableAllowanceService employeeVariableAllowanceService;
    private final EmployeeVariableDeductionService employeeVariableDeductionService;
    private final EmployeeNopayService employeeNopayService;
    private final EmployeeOvertimeService employeeOvertimeService;

    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final VariableAllowanceRepository variableAllowanceRepository;
    private final VariableDeductionRepository variableDeductionRepository;
    private final OvertimeRepository overtimeRepository;
    private final NopayDaysRepository nopayDaysRepository;
    private final EmpPayrollRunRepository empPayrollRunRepository;

    private static final Sort ID_ASC = Sort.by("id").ascending();

    @Override
    public EmployeePayrollComponentsResponseDTO getEmployeeProfile(Long empId, boolean assignedOnly) {
        return getEmployeeProfile(empId, assignedOnly, null);
    }

    @Override
    public EmployeePayrollComponentsResponseDTO getEmployeeProfile(Long empId, boolean assignedOnly, String payrollMonth) {
        return EmployeePayrollComponentsResponseDTO.builder()
                .employee(employeeService.getEmployeeById(empId))
                .fixedAllowances(mergeFixedAllowances(empId, assignedOnly, payrollMonth))
                .fixedDeductions(mergeFixedDeductions(empId, assignedOnly, payrollMonth))
                .variableAllowances(mergeVariableAllowances(empId, assignedOnly, payrollMonth))
                .variableDeductions(mergeVariableDeductions(empId, assignedOnly, payrollMonth))
                .nopays(mergeNopays(empId, assignedOnly, payrollMonth))
                .overtimes(mergeOvertimes(empId, assignedOnly, payrollMonth))
                .build();
    }

    // ── Merge helpers ────────────────────────────────────────────────────────

    private List<EmployeeFixedAllowanceResponseDTO> mergeFixedAllowances(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeFixedAllowanceResponseDTO> assigned = (payrollMonth != null
                ? employeeFixedAllowanceService.getByEmployeeId(empId, payrollMonth)
                : employeeFixedAllowanceService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeFixedAllowanceResponseDTO::getFaId, dto -> dto, (a, b) -> b));

        return fixedAllowanceRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> master.getId() != -1L)
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeFixedAllowanceResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeFixedAllowanceResponseDTO.builder()
                            .isAssigned(false)
                            .faId(master.getId())
                            .faCode(master.getCode())
                            .faName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<EmployeeFixedDeductionResponseDTO> mergeFixedDeductions(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeFixedDeductionResponseDTO> assigned = (payrollMonth != null
                ? employeeFixedDeductionService.getByEmployeeId(empId, payrollMonth)
                : employeeFixedDeductionService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeFixedDeductionResponseDTO::getFdId, dto -> dto, (a, b) -> b));

        return fixedDeductionRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> master.getId() != -1L)
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeFixedDeductionResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeFixedDeductionResponseDTO.builder()
                            .isAssigned(false)
                            .fdId(master.getId())
                            .fdCode(master.getCode())
                            .fdName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<EmployeeVariableAllowanceResponseDTO> mergeVariableAllowances(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeVariableAllowanceResponseDTO> assigned = (payrollMonth != null
                ? employeeVariableAllowanceService.getByEmployeeId(empId, payrollMonth)
                : employeeVariableAllowanceService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeVariableAllowanceResponseDTO::getVaId, dto -> dto, (a, b) -> b));

        return variableAllowanceRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeVariableAllowanceResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeVariableAllowanceResponseDTO.builder()
                            .isAssigned(false)
                            .vaId(master.getId())
                            .vaCode(master.getCode())
                            .vaName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<EmployeeVariableDeductionResponseDTO> mergeVariableDeductions(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeVariableDeductionResponseDTO> assigned = (payrollMonth != null
                ? employeeVariableDeductionService.getByEmployeeId(empId, payrollMonth)
                : employeeVariableDeductionService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeVariableDeductionResponseDTO::getVdId, dto -> dto, (a, b) -> b));

        return variableDeductionRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeVariableDeductionResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeVariableDeductionResponseDTO.builder()
                            .isAssigned(false)
                            .vdId(master.getId())
                            .vdCode(master.getCode())
                            .vdName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<EmployeeNopayResponseDTO> mergeNopays(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeNopayResponseDTO> assigned = (payrollMonth != null
                ? employeeNopayService.getByEmployeeId(empId, payrollMonth)
                : employeeNopayService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeNopayResponseDTO::getNopayId, dto -> dto, (a, b) -> b));

        return nopayDaysRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeNopayResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeNopayResponseDTO.builder()
                            .isAssigned(false)
                            .nopayId(master.getId())
                            .nopayCode(master.getCode())
                            .nopayName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<EmployeeOvertimeResponseDTO> mergeOvertimes(Long empId, boolean assignedOnly, String payrollMonth) {
        Map<Long, EmployeeOvertimeResponseDTO> assigned = (payrollMonth != null
                ? employeeOvertimeService.getByEmployeeId(empId, payrollMonth)
                : employeeOvertimeService.getByEmployeeId(empId))
                .stream().collect(Collectors.toMap(EmployeeOvertimeResponseDTO::getOvertimeId, dto -> dto, (a, b) -> b));

        return overtimeRepository.findAllByIsActive(true, ID_ASC).stream()
                .filter(master -> !assignedOnly || assigned.containsKey(master.getId()))
                .map(master -> {
                    if (assigned.containsKey(master.getId())) {
                        EmployeeOvertimeResponseDTO dto = assigned.get(master.getId());
                        dto.setIsAssigned(true);
                        return dto;
                    }
                    return EmployeeOvertimeResponseDTO.builder()
                            .isAssigned(false)
                            .overtimeId(master.getId())
                            .overtimeCode(master.getCode())
                            .overtimeName(master.getName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeePayrollComponentsResponseDTO saveEmployeeProfile(Long empId, EmployeeProfileSaveRequestDTO requestDTO) {

        // Collect all unique payroll months from the request
        java.util.Set<String> months = new java.util.HashSet<>();
        if (requestDTO.getFixedAllowances() != null)
            requestDTO.getFixedAllowances().forEach(fa -> { if (fa.getPayrollMonth() != null) months.add(fa.getPayrollMonth()); });
        if (requestDTO.getFixedDeductions() != null)
            requestDTO.getFixedDeductions().forEach(fd -> { if (fd.getPayrollMonth() != null) months.add(fd.getPayrollMonth()); });
        if (requestDTO.getVariableAllowances() != null)
            requestDTO.getVariableAllowances().forEach(va -> { if (va.getPayrollMonth() != null) months.add(va.getPayrollMonth()); });
        if (requestDTO.getVariableDeductions() != null)
            requestDTO.getVariableDeductions().forEach(vd -> { if (vd.getPayrollMonth() != null) months.add(vd.getPayrollMonth()); });
        if (requestDTO.getNopays() != null)
            requestDTO.getNopays().forEach(np -> { if (np.getPayrollMonth() != null) months.add(np.getPayrollMonth()); });
        if (requestDTO.getOvertimes() != null)
            requestDTO.getOvertimes().forEach(ot -> { if (ot.getPayrollMonth() != null) months.add(ot.getPayrollMonth()); });

        // Guard: reject edits for closed periods or months with a LOCKED run
        for (String month : months) {
            if (!payrollPeriodService.isPeriodOpen(month)) {
                throw new IllegalStateException(
                        "Cannot modify payroll components — payroll period " + month
                        + " is closed. Use a correction run instead.");
            }
            if (empPayrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                    empId, month, PayrollRunStatus.LOCKED)) {
                throw new IllegalStateException(
                        "Cannot modify payroll components — payroll is already locked for month: " + month
                        + ". Use a correction run instead.");
            }
        }

        if (requestDTO.getFixedAllowances() != null) {
            requestDTO.getFixedAllowances().forEach(fa -> {
                fa.setEmpId(empId);
                employeeFixedAllowanceService.createEmployeeFixedAllowance(fa);
            });
        }

        if (requestDTO.getFixedDeductions() != null) {
            requestDTO.getFixedDeductions().forEach(fd -> {
                fd.setEmpId(empId);
                employeeFixedDeductionService.createEmployeeFixedDeduction(fd);
            });
        }

        if (requestDTO.getVariableAllowances() != null) {
            requestDTO.getVariableAllowances().forEach(va -> {
                va.setEmpId(empId);
                employeeVariableAllowanceService.createEmployeeVariableAllowance(va);
            });
        }

        if (requestDTO.getVariableDeductions() != null) {
            requestDTO.getVariableDeductions().forEach(vd -> {
                vd.setEmpId(empId);
                employeeVariableDeductionService.createEmployeeVariableDeduction(vd);
            });
        }

        if (requestDTO.getNopays() != null) {
            requestDTO.getNopays().forEach(np -> {
                np.setEmpId(empId);
                employeeNopayService.createEmployeeNopay(np);
            });
        }

        if (requestDTO.getOvertimes() != null) {
            requestDTO.getOvertimes().forEach(ot -> {
                ot.setEmpId(empId);
                employeeOvertimeService.createEmployeeOvertime(ot);
            });
        }

        // Return the profile filtered to the saved period (first month in the set, or null for all)
        String savedMonth = months.size() == 1 ? months.iterator().next() : null;
        return getEmployeeProfile(empId, false, savedMonth);
    }
}
