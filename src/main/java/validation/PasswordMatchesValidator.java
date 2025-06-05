package validation;

import com.ems.employee_management.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        System.out.println("🔍 PasswordMatchesValidator çalıştı"); // Debug için log

        if (user == null) {
            return true;
        }

        String password = user.getPassword();
        String confirm = user.getConfirmPassword();

        if (password == null || confirm == null) {
            return false;
        }

        return password.equals(confirm);
    }
}
