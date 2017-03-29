package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class UpdateTopicCommand {
    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String name;
}
