package com.server.validators;

import com.server.interfaces.Validatable;
import com.server.models.Person;

public class PersonValidator implements Validatable<Person> {

    @Override
    public boolean isValid(Person person) {
        return person != null &&
                person.getFirstName() != null &&
                !person.getFirstName().isEmpty() &&
                person.getLastName() != null &&
                !person.getLastName().isEmpty();
    }
}
