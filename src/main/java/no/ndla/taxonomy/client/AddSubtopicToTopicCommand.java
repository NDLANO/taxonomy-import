package no.ndla.taxonomy.client;

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
