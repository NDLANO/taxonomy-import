package no.ndla.taxonomy.client.resourceTypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ResourceTypeIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ResourceTypeIndexDocument> subtypes = new ArrayList<>();
}
