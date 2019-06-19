package no.ndla.taxonomy.client.topicResourceTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateTopicResourceTypeCommand {
    @JsonProperty
    public URI topicId;

    @JsonProperty
    public URI resourceTypeId;
}
