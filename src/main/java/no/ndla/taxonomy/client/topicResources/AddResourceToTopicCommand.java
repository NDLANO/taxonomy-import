package no.ndla.taxonomy.client.topicResources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddResourceToTopicCommand {
    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI resourceid;

    @JsonProperty
    public boolean primary;
}