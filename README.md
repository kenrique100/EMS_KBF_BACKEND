sequenceDiagram
    participant User
    participant SalaryController
    participant SalaryService
    participant ReceiptGeneratorService
    participant TaskRepository
    participant EmployeeRepository

    User->>SalaryController: GET /salary/{paymentId}/receipt/pdf
    SalaryController->>SalaryService: generatePdfReceipt(paymentId)
    SalaryService->>SalaryService: generateSalaryReceipt(paymentId)
    SalaryService->>EmployeeRepository: find employee by paymentId
    SalaryService->>TaskRepository: find tasks for employee in period
    SalaryService->>SalaryService: build SalaryReceiptDTO
    SalaryService->>ReceiptGeneratorService: generatePdfReceipt(SalaryReceiptDTO)
    ReceiptGeneratorService-->>SalaryService: PDF byte[]
    SalaryService-->>SalaryController: PDF byte[]
    SalaryController-->>User: PDF file response
