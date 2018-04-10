package no.ndla.taxonomy.client.topicFilters;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class AddFilterToTopicCommand {
    @JsonProperty
    public URI topicId;

    @JsonProperty
    public URI filterId;

    @JsonProperty
    public URI relevanceId;
}
