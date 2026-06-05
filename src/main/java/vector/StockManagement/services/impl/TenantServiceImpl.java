package vector.StockManagement.services.impl;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vector.StockManagement.auth.AuthenticationResponse;
import vector.StockManagement.auth.AuthenticationService;
import vector.StockManagement.auth.RegisterRequest;
import vector.StockManagement.config.JwtService;
//import vector.StockManagement.config.TenantContext;
import vector.StockManagement.model.Tenant;
import vector.StockManagement.model.Token;
import vector.StockManagement.model.User;
import vector.StockManagement.model.dto.TenantDTO;
import vector.StockManagement.model.enums.Gender;
import vector.StockManagement.model.enums.Role;
import vector.StockManagement.model.enums.TokenType;
import vector.StockManagement.repositories.TenantRepository;
import vector.StockManagement.repositories.TokenRepository;
import vector.StockManagement.repositories.UserRepository;
import vector.StockManagement.services.TenantService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {


    private static final Logger logger = Logger.getLogger(TenantServiceImpl.class.getName());
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;


    @Override
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Override
    public Tenant findById(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public AuthenticationResponse save(TenantDTO dto) {
//        TenantContext.clear();

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken: " + dto.getEmail());
        }

        if (dto.getGender() == null || dto.getGender().isEmpty()) {
            throw new IllegalStateException("Gender is required");
        }



        Tenant tenant = new Tenant();
        tenant.setName(dto.getCompanyName());
        tenant.setCode(dto.getCompanyCode());
        tenant.setDescription(dto.getCompanyDescription());
        tenant.setPhone(dto.getPhone());
        tenant.setEmail(dto.getEmail());
        tenant.setAddress(dto.getAddress());
        tenantRepository.saveAndFlush(tenant);


        RegisterRequest request = new RegisterRequest();
        request.setFirstName(dto.getFirstName());
        request.setLastName(dto.getLastName());
        request.setEmail(dto.getEmail());
        request.setPassword(dto.getPassword());
        request.setConfirmPassword(dto.getConfirmPassword());
        request.setGender(dto.getGender());
        request.setPhone(dto.getPhone());
        request.setTenant(tenant);

//        TenantContext.setTenantId(tenant.getId());

        return authenticationService.registerAdmin(request);

    }



    @Override
    public void delete(Long id) {
        tenantRepository.deleteById(id);
    }

    @Override
    public Tenant update(Long id, Tenant tenant) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        if (existing != null) {
            existing.setActive(tenant.getActive());
            return tenantRepository.save(existing);
        }
        else {
            throw new IllegalArgumentException("Tenant not found");
        }
    }
}