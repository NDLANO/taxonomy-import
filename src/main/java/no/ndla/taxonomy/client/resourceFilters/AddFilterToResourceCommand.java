package no.ndla.taxonomy.client.resourceFilters;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddFilterToResourceCommand {
    @JsonProperty
    public URI resourceId;

    @JsonProperty
    public URI filterId;

    @JsonProperty
    public URI relevanceId;
}