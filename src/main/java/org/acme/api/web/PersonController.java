package org.acme.api.web;

import java.util.List;
import java.util.stream.Collectors;

import org.acme.api.model.Person;
import org.acme.api.service.PersonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.RequiredArgsConstructor;

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

    @GetMapping(path = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<People> getPeopleXml() {
        return ResponseEntity.ok(new People(personService.findAll()));
    }

    @GetMapping(path = "/summary", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPeoplePlainSummary() {
        String line = personService.findAll().stream()
                .map(Person::getName)
                .collect(Collectors.joining(", "));
        return ResponseEntity.ok(line);
    }

    @JacksonXmlRootElement(localName = "people")
    public record People(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "person")
            List<Person> people) {
    }
}
