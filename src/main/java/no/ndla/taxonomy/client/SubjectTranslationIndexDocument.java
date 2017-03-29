package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubjectTranslationIndexDocument {
    @JsonProperty
    public String name;

    @JsonProperty
    public String language;
}
