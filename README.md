# swagger-prefer-json

Small Spring Boot API showing JSON and XML responses with Swagger UI.

## Requirements

- Java 21+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

## Endpoints

- Home: `GET /`
- People: `GET /api/v1/people`
- People (corrected wrapper): `GET /api/v1/people/corrected`
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

## Content Types

`/api/v1/people` supports both:

- `application/json`
- `application/xml`

Examples:

```bash
curl -H "Accept: application/json" http://localhost:8080/api/v1/people
curl -H "Accept: application/xml" http://localhost:8080/api/v1/people
```

Response shape:

JSON (`application/json`):

```json
[
  {
    "name": "John",
    "email": "john@example.com"
  }
]
```

XML (`application/xml`):

```xml
<List>
  <item>
    <name>John</name>
    <email>john@example.com</email>
  </item>
</List>
```

## XML Caveat

This endpoint returns a raw `List<Person>`. With Jackson XML, that produces generic wrapper/item
names (`<List>` and `<item>`).

The practical middle-ground is a wrapper object around the list (for example a `People` DTO with a
`List<Person> people` field). That gives domain-specific XML names like `<people>` / `<person>`
without per-endpoint OpenAPI customizer code.

This project also includes a corrected wrapper endpoint:

- `GET /api/v1/people/corrected`

That endpoint returns:

- JSON: `{"people":[...]}`
- XML: `<people><person>...</person></people>`
