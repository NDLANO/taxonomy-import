package no.ndla.taxonomy.client.resourceTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class ResourceTypeIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;
}
