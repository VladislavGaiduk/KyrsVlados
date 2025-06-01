package com.server.validators;

import com.server.interfaces.Validatable;
import com.server.models.Role;

public class RoleValidator implements Validatable<Role> {

    @Override
    public boolean isValid(Role role) {
        return role != null &&
                role.getName() != null &&
                !role.getName().isEmpty();
    }
}

