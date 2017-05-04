package no.ndla.taxonomy.client.relevances;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class RelevanceIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;
}
