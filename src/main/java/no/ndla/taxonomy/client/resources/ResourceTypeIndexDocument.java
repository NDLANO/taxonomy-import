package no.ndla.taxonomy.client.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class ResourceTypeIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public URI parentId;

    @JsonProperty
    public String name;
}
