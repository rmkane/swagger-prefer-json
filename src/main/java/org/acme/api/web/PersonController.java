package org.acme.api.web;

import java.util.List;

import org.acme.api.model.Person;
import org.acme.api.service.PersonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Person>> getPeople() {
        return ResponseEntity.ok(personService.findAll());
    }
}
