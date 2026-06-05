package vector.StockManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String gender;
    private String phoneNumber;
    private String tenantId;
    private String nationality;
    private LocalDate birthDate;
    private String tin;
    private String imageUrl;
    private LocalDateTime createdAt;
}