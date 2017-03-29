package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class TopicSubtopicIndexDocument {
    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI subtopicid;

    @JsonProperty
    public URI id;

    @JsonProperty
    public boolean primary;

    TopicSubtopicIndexDocument() {
    }

}
