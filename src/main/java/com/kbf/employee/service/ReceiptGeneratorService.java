package com.kbf.employee.service;

import com.kbf.employee.dto.request.SalaryReceiptDTO;

public interface ReceiptGeneratorService {
    byte[] generatePdfReceipt(SalaryReceiptDTO receipt);
}