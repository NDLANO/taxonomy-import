package no.ndla.taxonomy.client.topicResources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddResourceToTopicCommand {
    @JsonProperty
    public URI topicid;

    @JsonProperty
    public URI resourceId;

    @JsonProperty
    public boolean primary;

    @JsonProperty
    public int rank;
}