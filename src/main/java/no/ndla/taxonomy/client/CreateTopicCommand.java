package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateTopicCommand {
    @JsonProperty
    public URI id;

    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String name;
}
