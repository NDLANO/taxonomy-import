package no.ndla.taxonomy.client.subjectTopics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class UpdateSubjectTopicCommand {
    @JsonProperty
    public URI id;

    @JsonProperty
    public boolean primary;
}
