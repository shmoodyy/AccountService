package account.presentation;

import account.business.models.payrolls.PayrollDTO;
import account.business.models.payrolls.PayrollDomain;
import account.business.services.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PayrollController {

    @Autowired
    private final PayrollService payrollService;

    @Autowired
    private final ModelMapper controllerModelMapper;

    @GetMapping("/empl/payment")
    public ResponseEntity<Object> getPayroll(@AuthenticationPrincipal UserDetails userDetails
            , @RequestParam(required = false) String period) {
        String email = userDetails.getUsername();
        if (period == null) {
            return ResponseEntity.ok(payrollService.getPayroll(email));
        } if (!period.matches("^(0[1-9]|1[0-2])-\\d{4}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format!");
        }
        return ResponseEntity.ok(payrollService.getPayrollForPeriod(email, period));
    }

    @PostMapping("/acct/payments")
    public ResponseEntity<Object> uploadPayrolls(@RequestBody List<@Valid PayrollDTO> payrollDTOList) {
        ListIterator<PayrollDTO> iterator = payrollDTOList.listIterator();
        validatePayrollList(iterator);

        var payrollDomainList = payrollDTOList.stream()
                .map(this::convertDTOToDomain)
                .toList();
        return ResponseEntity.ok(payrollService.uploadPayrolls(payrollDomainList));
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<Object> updatePayroll(@Valid @RequestBody PayrollDTO payrollDTO) {
        var email = payrollDTO.getEmployee();
        if (!payrollService.existsOnPayroll(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee doesn't exist!");
        }
        return ResponseEntity.ok(payrollService.updatePayroll(email, payrollDTO.getPeriod(), payrollDTO.getSalary()));
    }


    // Controller utility methods
    public void validatePayrollList(ListIterator<PayrollDTO> iterator) {
        Set<String> employeePeriodPairSet = new HashSet<>();
        while (iterator.hasNext()) {
            var currentPayroll = iterator.next();
            var email = currentPayroll.getEmployee();
            var period = currentPayroll.getPeriod();
            String employeePeriodPair = email + "-" + period;
            if (!period.matches("^(0[1-9]|1[0-2])-\\d{4}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format!");
            } if (currentPayroll.getSalary() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non-negative!");
            } if (!payrollService.existsAsUser(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is not a user of the service!");
            } if (employeePeriodPairSet.contains(employeePeriodPair)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee-Period pair is not unique!");
            } else {
                employeePeriodPairSet.add(employeePeriodPair);
            }
        }
    }
    public PayrollDomain convertDTOToDomain(PayrollDTO payrollDTO) {
        return controllerModelMapper.map(payrollDTO, PayrollDomain.class);
    }
}