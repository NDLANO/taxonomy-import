package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class SubjectTopicIndexDocument {
    @JsonProperty
    public URI subjectid;

    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI id;

    @JsonProperty
    public boolean primary;

    SubjectTopicIndexDocument() {
    }

}
