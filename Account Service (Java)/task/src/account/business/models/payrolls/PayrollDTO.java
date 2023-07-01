package account.business.models.payrolls;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDTO {
    @Pattern(regexp = ".+@acme.com")
    private String employee; // input value is a user email

    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$")
    private String period;

    @Min(value = 0, message = "Salary must be non-negative!")
    private Long salary;
}