package no.ndla.taxonomy.client.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class UpdateResourceCommand {
    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String name;
}
