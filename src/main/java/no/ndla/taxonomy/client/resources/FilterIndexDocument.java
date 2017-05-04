package no.ndla.taxonomy.client.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public  class FilterIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;

    @JsonProperty
    public URI connectionId;

    @JsonProperty
    public URI relevanceId;
}