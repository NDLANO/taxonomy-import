package no.ndla.taxonomy.client.topicResources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class TopicResourceIndexDocument {


    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI resourceId;

    @JsonProperty
    public URI id;

    @JsonProperty
    public boolean primary;

    @JsonProperty
    public int rank;

    TopicResourceIndexDocument() {
    }
}
