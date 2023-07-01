package account.business.services;

import account.business.models.payrolls.PayrollDomain;
import account.business.models.payrolls.PayrollEntity;
import account.business.models.users.UserEntity;
import account.persistence.PayrollRepository;
import account.persistence.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PayrollService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PayrollRepository payrollRepository;

    @Autowired
    private final ModelMapper serviceModelMapper;

    public List<PayrollDomain> getPayroll(String employee) {
        return payrollRepository.findByEmployeeIgnoreCaseOrderByPeriodDesc(employee).stream()
                .map(this::formatPeriodAndSalary) // returns domain
                .toList();
    }

    public PayrollDomain getPayrollForPeriod(String employee, String period) {
        var entity = payrollRepository.findByEmployeeAndPeriod(employee, period);
        return formatPeriodAndSalary(entity); // returns domain
    }

    @Transactional
    public Map<String, String> uploadPayrolls(List<PayrollDomain> payrollDomainList) {
        Map<String, String> statusMap = new ConcurrentHashMap<>(1);
        var payrollEntityList = payrollDomainList.stream()
                .map(domain -> {
                    PayrollEntity payrollEntity = convertDomainToEntity(domain);
                    UserEntity userEntity = userRepository.findByEmailIgnoreCase(payrollEntity.getEmployee());
                    payrollEntity.setName(userEntity.getName());
                    payrollEntity.setLastName(userEntity.getLastname());
                    return payrollEntity;
                })
                .toList();
        payrollRepository.saveAll(payrollEntityList);
        statusMap.put("status", "Added successfully!");
        return statusMap;
    }

    @Transactional
    public Map<String, String> updatePayroll(String employee, String period, Long salary) {
        Map<String, String> statusMap = new ConcurrentHashMap<>(1);
        var entity = payrollRepository.findByEmployeeIgnoreCaseAndPeriod(employee, period);
        entity.setSalary(String.valueOf(salary));
        statusMap.put("status", "Updated successfully!");
        return statusMap;
    }

    public boolean existsOnPayroll(String employee) {
        return payrollRepository.existsByEmployeeIgnoreCase(employee);
    }
    public boolean existsAsUser(String employee) {
        return userRepository.existsByEmailIgnoreCase(employee);
    }


    // Service utility methods
    public PayrollDomain formatPeriodAndSalary(PayrollEntity entity) {
        PayrollDomain payrollDomain = convertEntityToDomain(entity);
        String period = entity.getPeriod();
        YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String date = monthName + "-" + yearMonth.getYear();
        payrollDomain.setPeriod(date);
        payrollDomain.setSalary(formatSalary(entity.getSalary()));
        return payrollDomain;
    }

    public String formatSalary(String salary) {
        int length = salary.length();
        String dollars = salary.substring(0, length - 2);
        String cents = salary.substring(length - 2);
        return String.format("%s dollar(s) %s cent(s)", dollars.isBlank() ? "0" : dollars, cents);
    }

    public PayrollEntity convertDomainToEntity(PayrollDomain payrollDomain) {
        return serviceModelMapper.map(payrollDomain, PayrollEntity.class);
    }

    public PayrollDomain convertEntityToDomain(PayrollEntity payrollEntity) {
        return serviceModelMapper.map(payrollEntity, PayrollDomain.class);
    }
}