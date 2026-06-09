package com.payroll.service.impl;

import com.payroll.service.PayrollPivotService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollPivotServiceImpl implements PayrollPivotService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getEmployeeFixedAllowancePivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_fa_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeFixedDeductionPivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_fd_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeNopayPivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_np_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeOvertimePivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_ot_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeVariableAllowancePivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_va_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeVariableDeductionPivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_vd_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getPayrollMonthlySummary(String month) {
        return jdbcTemplate.queryForList("CALL sp_payroll_monthly_summary(?)", month);
    }

    @Override
    public List<Map<String, Object>> getPayrollMonthlyDetail(String month) {
        return jdbcTemplate.queryForList("CALL sp_payroll_monthly_detail(?)", month);
    }

    @Override
    public List<Map<String, Object>> getEmployeeSalaryAdvancePivot(String month) {
        return jdbcTemplate.queryForList("CALL sp_emp_sal_adv_pivot(?)", month);
    }

    @Override
    public List<Map<String, Object>> getPayrollSummaryReport(String month) {
        return jdbcTemplate.queryForList("CALL sp_rpt_payrollSummary(?)", month);
    }

    @Override
    public List<Map<String, Object>> getBankTransferReport(String month) {
        return jdbcTemplate.queryForList("CALL sp_rpt_bankTransfer(?)", month);
    }

    @Override
    public List<Map<String, Object>> getNopayReport(String month) {
        return jdbcTemplate.queryForList("CALL sp_rpt_nopay(?)", month);
    }
}
