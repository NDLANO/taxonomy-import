package no.ndla.taxonomy.client.subjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class UpdateSubjectCommand {
    @JsonProperty
    public URI contentUri;

    @JsonProperty
    public String name;
}
