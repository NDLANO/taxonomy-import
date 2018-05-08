package no.ndla.taxonomy.client.topics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class ResourceIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public URI topicId;

    @JsonProperty
    public String name;

    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String path;

    @JsonProperty
    public Set<ResourceTypeIndexDocument> resourceTypes = new HashSet<>();

    @JsonProperty
    public URI connectionId;

    @JsonProperty
    public Boolean isPrimary;
}
