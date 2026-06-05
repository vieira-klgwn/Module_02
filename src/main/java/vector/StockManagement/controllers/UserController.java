package vector.StockManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vector.StockManagement.model.ChangePasswordRequest;
import vector.StockManagement.model.User;
import vector.StockManagement.model.dto.UserDTO;
import vector.StockManagement.model.enums.Role;
import vector.StockManagement.repositories.UserRepository;
import vector.StockManagement.services.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<User> createUser(@AuthenticationPrincipal User currentUser,@Valid @RequestBody UserDTO user) {
        logger.debug("Creating user: {}", user.getEmail());

        User createdUser = userService.createUser(currentUser,user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    @GetMapping("/accountant_at_store")
    public ResponseEntity<User> distributorLevelAccountant(@AuthenticationPrincipal User user) {
        List<User> accountants = userRepository.findByDistributor_Id((user.getId())).stream().filter(user1 -> user1.getRole().equals(Role.ACCOUNTANT_AT_STORE)).toList();
        return ResponseEntity.ok(accountants.getFirst());


    }

    @GetMapping("/tin/{tin}")
    public ResponseEntity<User> getUserByTin(@PathVariable Long id){
        return ResponseEntity.ok(userRepository.findById(id).orElseThrow(()-> new IllegalStateException("User not found with tin: " + id)));
    }


    @GetMapping("/store_manager")
    public ResponseEntity<User> storeManager(@AuthenticationPrincipal User user) {
        List<User> store_managers = userRepository.findByDistributor_Id(user.getId()).stream().filter(user1 -> user1.getRole().equals(Role.STORE_MANAGER)).toList();
        return new ResponseEntity<>(store_managers.getFirst(), HttpStatus.OK);
    }



    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'DISTRIBUTOR', 'ACCOUNTANT', 'SALES_MANAGER','STORE_MANAGER','WAREHOUSE_MANAGER','MANAGING_DIRECTOR')")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user ID: {}", id);
        return userService.getUserById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> {
                    logger.warn("User ID {} not found", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }


    //sorry for not handling business logic in controller
    @GetMapping("/retailer")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ACCOUNTANT', 'STORE_MANAGER', 'ACCOUNTANT_AT_STORE')")
    public ResponseEntity<List<User>> getAllRetailers(@AuthenticationPrincipal User user){
        logger.debug("Fetching all retailers");
        List<User> users = userRepository.findAll();
        List<User> retailers = new ArrayList<>();
        for (User user1 : users) {
            if (user1.getRole() == Role.RETAILER && user.getId().equals(user1.getDistributor().getId())) {
                retailers.add(user1);
            }
        }
        return new ResponseEntity<>(retailers, HttpStatus.OK);
    }


    @GetMapping("/accountant/retailer")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ACCOUNTANT', 'STORE_MANAGER', 'ACCOUNTANT_AT_STORE')")
    public ResponseEntity<List<User>> getAllRetailersByAccountantAtStore(){
        logger.debug("Fetching all retailers");
        List<User> users = userRepository.findAll();
        List<User> retailers = new ArrayList<>();
        for (User user1 : users) {
            if (user1.getRole() == Role.RETAILER ) {
                retailers.add(user1);
            }
        }
        return new ResponseEntity<>(retailers, HttpStatus.OK);
    }

    @GetMapping("/distributors")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public ResponseEntity<List<User>> getAllDistributors(){
        List<User> users = userRepository.findAll();
        List<User> distributors = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == Role.DISTRIBUTOR){
                distributors.add(user);
            }
        }
        return new ResponseEntity<>(distributors, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        logger.debug("Updating user ID: {}", id);
        try {
            User updatedUser = userService.updateUser(id, user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Failed to update user ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.debug("Deleting user ID: {}", id);
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            logger.error("Failed to delete user ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal connectedUser) {
        logger.debug("Changing password for user: {}", connectedUser.getName());
        try {
            userService.changePassword(changePasswordRequest, connectedUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Failed to change password for user {}: {}", connectedUser.getName(), e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            logger.warn("Unauthorized access attempt to /api/users/me");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


        String email = authentication.getName();
        logger.debug("Fetching current user with email: {}", email);
        return userService.getUserByEmail(email)
                .map(user -> {
                    logger.info("User found: {}", user.getEmail());
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setFirstName(user.getFirstName());
                    userDTO.setLastName(user.getLastName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setRole(user.getRole().toString());
                    userDTO.setGender(user.getGender() != null ? user.getGender().toString() : null);
                    userDTO.setTenantId(Long.toString(user.getTenant().getId()));
                    userDTO.setNationality(user.getNationality());
                    userDTO.setBirthDate(user.getBirthDate());
                    userDTO.setCreatedAt(user.getCreatedAt());
                    userDTO.setPhoneNumber(user.getPhone());
                    userDTO.setImageUrl(user.getImageUrl());
                    return new ResponseEntity<>(userDTO, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for email: {}", email);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    @PostMapping("/{id}/upload-image")  // Admins or self-upload (assuming email as principal name)
    public ResponseEntity<User> uploadUserImage(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal User principal) {  // Renamed to avoid confusion
        // Validate file (same as product)
        if (file.isEmpty()) {
            throw new IllegalStateException("File is empty");
        }
        if (!isValidImage(file)) {
            throw new IllegalStateException("Invalid file type. Only JPG, PNG, GIF allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalStateException("File too large (max 5MB)");
        }

        User targetUser = userService.getUserById(id).orElse(null);  // Fetch target
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Security: Ensure self-upload or admin
        if (!targetUser.getEmail().equals(principal.getEmail()) && principal.getRole() != Role.ADMIN) {
            throw new IllegalStateException("You can only upload your own image or be an admin");
        }

        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
            String filename = "user_" + id + "_" + UUID.randomUUID() + ext.toLowerCase();  // Use target id
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            // Store relative URL for frontend serving
            targetUser.setImageUrl("/uploads/" + filename);
            logger.info("Image uploaded for user {}: {}", id, filename);  // Add logging
            return ResponseEntity.ok(userService.updateUser(id, targetUser));
        } catch (IOException e) {
            logger.error("Failed to upload image for user ID {}: {}", id, e.getMessage());
            throw new IllegalStateException("Failed to upload image: " + e.getMessage());
        }
    }



    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif"));
    }

    private boolean hasAdminRole(Collection<?> authorities) {
        return authorities.stream().anyMatch(auth -> auth.toString().contains("ADMIN"));
    }
}