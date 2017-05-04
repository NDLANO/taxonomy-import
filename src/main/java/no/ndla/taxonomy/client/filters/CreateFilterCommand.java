package no.ndla.taxonomy.client.filters;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateFilterCommand {
    @JsonProperty
    public URI id, subjectId;

    @JsonProperty
    public String name;
}
