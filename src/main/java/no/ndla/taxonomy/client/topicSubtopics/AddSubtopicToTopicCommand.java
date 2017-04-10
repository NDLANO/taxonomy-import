package no.ndla.taxonomy.client.topicSubtopics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddSubtopicToTopicCommand {
    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI subtopicid;

    @JsonProperty
    public boolean primary;
}
