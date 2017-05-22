package no.ndla.taxonomy.client.topicResources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class UpdateTopicResourceCommand {
    @JsonProperty
    public URI id;

    @JsonProperty
    public boolean primary;

    @JsonProperty
    public int rank;
}
