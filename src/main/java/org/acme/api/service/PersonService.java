package org.acme.api.service;

import java.util.List;

import org.acme.api.model.Person;
import org.springframework.stereotype.Service;

@Service
public class PersonService {
    public List<Person> findAll() {
        return List.of(
            Person.builder().name("John").email("john@example.com").build(),
            Person.builder().name("Jane").email("jane@example.com").build(),
            Person.builder().name("Jim").email("jim@example.com").build(),
            Person.builder().name("Jill").email("jill@example.com").build()
        );
    }
}
