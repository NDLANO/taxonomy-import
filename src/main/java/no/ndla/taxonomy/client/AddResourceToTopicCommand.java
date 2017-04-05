package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddResourceToTopicCommand {
    @JsonProperty
    public URI topicid;

    @JsonProperty
    URI resourceid;

    @JsonProperty
    public boolean primary;
}