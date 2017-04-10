package no.ndla.taxonomy.client.resourceTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateResourceTypeCommand {
    @JsonProperty
    public URI parentId;

    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;
}