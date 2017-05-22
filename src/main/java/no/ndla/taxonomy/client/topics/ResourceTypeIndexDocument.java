package no.ndla.taxonomy.client.topics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class ResourceTypeIndexDocument {
    @JsonProperty
    public URI id;

    @JsonProperty
    public String name;

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceTypeIndexDocument)) return false;

        ResourceTypeIndexDocument that = (ResourceTypeIndexDocument) o;

        return id.equals(that.id);
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return id.hashCode();
    }
}

