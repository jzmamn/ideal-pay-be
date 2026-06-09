package com.payroll.service;

import java.util.List;
import java.util.Map;

public interface PayrollPivotService {

    List<Map<String, Object>> getEmployeeFixedAllowancePivot(String month);

    List<Map<String, Object>> getEmployeeFixedDeductionPivot(String month);

    List<Map<String, Object>> getEmployeeNopayPivot(String month);

    List<Map<String, Object>> getEmployeeOvertimePivot(String month);

    List<Map<String, Object>> getEmployeeVariableAllowancePivot(String month);

    List<Map<String, Object>> getEmployeeVariableDeductionPivot(String month);

    List<Map<String, Object>> getPayrollMonthlySummary(String month);

    List<Map<String, Object>> getPayrollMonthlyDetail(String month);

    List<Map<String, Object>> getEmployeeSalaryAdvancePivot(String month);

    List<Map<String, Object>> getPayrollSummaryReport(String month);

    List<Map<String, Object>> getBankTransferReport(String month);

    List<Map<String, Object>> getNopayReport(String month);
}
