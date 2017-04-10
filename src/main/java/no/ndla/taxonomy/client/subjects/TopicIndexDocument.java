package no.ndla.taxonomy.client.subjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class TopicIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;

    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public URI parent;

    @JsonProperty
    public String path;
}
