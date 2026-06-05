package vector.StockManagement.model.dto;

import lombok.Data;

@Data
public class TenantDTO {
    private String companyName;
    private String companyCode;
    private String companyDescription;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String confirmPassword;
    private String gender;
    private String phone;
    private String tenantPhone;
    private String tenantEmail;
    private String address;

}
