package vector.StockManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tenant extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Size(max = 50)
    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "tenant_settings", joinColumns = @JoinColumn(name = "tenant_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value", length = 1000)
    private Map<String, String> settings = new HashMap<>();

    @Column(name = "active")
    private Boolean active = true;




    public Tenant(String name, String code) {
        this.name = name;
        this.code = code;
    }



   @OneToMany(mappedBy = "tenant")
   @JsonIgnore
   private List<User> users = new ArrayList<>();



    @Override
    public String toString() {
        return "Tenant{" +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", active=" + active +
                '}';
    }
}