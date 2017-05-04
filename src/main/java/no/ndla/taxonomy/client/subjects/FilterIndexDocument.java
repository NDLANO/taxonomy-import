package no.ndla.taxonomy.client.subjects;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class FilterIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;
}
