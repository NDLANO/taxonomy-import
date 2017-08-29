package no.ndla.taxonomy;

import java.net.URI;

public class ResourceType {
    public URI id;
    public String name;
    public String parentName;
    public URI parentId;

    public ResourceType(String name) {
        this.name = name;
    }

    public ResourceType(String name, String parentName, URI id) {
        this.name = name;
        this.parentName = parentName;
        this.id = id;
    }
}
