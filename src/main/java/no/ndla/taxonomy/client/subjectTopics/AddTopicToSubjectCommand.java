package no.ndla.taxonomy.client.subjectTopics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddTopicToSubjectCommand {
    @JsonProperty
    public URI subjectid;

    @JsonProperty
    public URI topicid;

    @JsonProperty
    public boolean primary;

    @JsonProperty
    public int rank;
}
