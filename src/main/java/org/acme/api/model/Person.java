package org.acme.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JacksonXmlRootElement(localName = "person")
public class Person {
    private String name;
    private String email;
}
