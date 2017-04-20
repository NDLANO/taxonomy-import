package no.ndla.taxonomy.client.topics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

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
}
