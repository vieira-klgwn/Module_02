package vector.UtilityBillingMS.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RwandaPhoneValidator implements ConstraintValidator<RwandaPhone, String> {

    private static final String PATTERN = "^(\\+2507\\d{8}|07\\d{8})$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches(PATTERN);
    }
}
