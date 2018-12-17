package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CreateAuthCommand {

    @JsonProperty
    public String grant_type;

    @JsonProperty
    public String client_id;

    @JsonProperty
    public String client_secret;

    @JsonProperty
    public String audience;


}
