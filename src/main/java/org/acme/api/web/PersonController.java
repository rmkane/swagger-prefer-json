package org.acme.api.web;

import java.util.List;

import org.acme.api.model.Person;
import org.acme.api.service.PersonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    
    @Deprecated
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Person>> getPeople() {
        return ResponseEntity.ok(personService.findAll());
    }

    @GetMapping(path = "/corrected", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<People> getPeopleCorrected() {
        return ResponseEntity.ok(new People(personService.findAll()));
    }

    @Value
    @XmlRootElement(name = "people")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class People {
        @XmlElement(name = "person")
        private List<Person> people;
    }
}
