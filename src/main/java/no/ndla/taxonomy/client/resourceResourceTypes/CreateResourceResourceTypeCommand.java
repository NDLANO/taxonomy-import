package no.ndla.taxonomy.client.resourceResourceTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateResourceResourceTypeCommand {
    @JsonProperty
    public URI resourceId;

    @JsonProperty
    public URI resourceTypeId;
}
