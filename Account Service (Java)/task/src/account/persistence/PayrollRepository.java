package account.persistence;

import account.business.models.payrolls.PayrollEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PayrollRepository extends CrudRepository<PayrollEntity, Long> {
    PayrollEntity findByEmployeeIgnoreCase(String employee);
    PayrollEntity findByEmployeeIgnoreCaseAndPeriod(String employee, String period);

    List<PayrollEntity> findAllByEmployeeIgnoreCase(String employee);
    List<PayrollEntity> findByEmployeeIgnoreCaseOrderByPeriodDesc(String employee);
    PayrollEntity findByEmployeeAndPeriod(String employee, String period);
    boolean existsByEmployeeIgnoreCase(String employee);
}
