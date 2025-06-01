package com.server.validators;

import com.server.interfaces.Validatable;
import com.server.models.User;

public class UserValidator implements Validatable<User> {
    public boolean isFullValid(User user) {
        return isValid(user) &&
                new PersonValidator().isValid(user.getPerson()) &&
                new RoleValidator().isValid(user.getRole());
    }

    @Override
    public boolean isValid(User user) {
        return user != null &&
                user.getUsername() != null &&
                !user.getUsername().isEmpty() &&
                user.getPassword() != null &&
                !user.getPassword().isEmpty();
    }
}
