package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Authentication {

    @JsonProperty
    public String access_token;

    @JsonProperty
    public String scope;

    @JsonProperty
    public Long expires_in;

    @JsonProperty
    public String token_type;

}
