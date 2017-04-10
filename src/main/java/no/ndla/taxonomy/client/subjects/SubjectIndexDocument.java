package no.ndla.taxonomy.client.subjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class SubjectIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String name;

    @JsonProperty
    public String path;
}
