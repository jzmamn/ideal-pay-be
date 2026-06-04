package com.payroll.service;

import com.payroll.dto.request.PayslipEmailRequestDTO;
import com.payroll.dto.response.PayslipEmailResultDTO;

public interface PayslipEmailService {
    PayslipEmailResultDTO sendPayslips(PayslipEmailRequestDTO request);
}
