package account.business.models.payrolls;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDomain {
    private String name;
    private String lastname;
    @JsonIgnore
    private String employee;
    private String period;
    private String salary;
}
