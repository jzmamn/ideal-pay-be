package com.payroll.service;

import com.payroll.dto.request.BatchSaveRequestDTO;

import java.util.List;
import java.util.Map;

public interface BatchPayrollService {

    /**
     * Load pivot data for all 6 component types for the given month/year.
     * Calls sp_emp_fa_pivot, sp_emp_fd_pivot, sp_emp_va_pivot,
     * sp_emp_vd_pivot, sp_emp_ot_pivot, sp_emp_np_pivot.
     *
     * @param periodMonth 1-12
     * @param periodYear  e.g. 2026
     * @return map keyed by component section — each value is the raw SP result
     */
    Map<String, List<Map<String, Object>>> load(Integer periodMonth, Integer periodYear);

    /**
     * Full CRUD on batch entries for emp_fa / emp_fd / emp_va / emp_vd / emp_ot / emp_np.
     * <ul>
     *   <li>amount &gt; 0  → upsert (insert if new, update if exists)</li>
     *   <li>amount == 0 or null → delete the existing record if found</li>
     * </ul>
     * Throws {@link IllegalStateException} if any employee's month is LOCKED.
     *
     * @param requestDTO entries payload
     * @param modifiedBy user performing the save
     */
    void save(BatchSaveRequestDTO requestDTO, Long modifiedBy);
}
