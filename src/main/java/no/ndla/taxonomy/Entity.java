package no.ndla.taxonomy;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {

    public String type, name, nodeId;
    public URI id, contentUri;

    public final Map<String, Translation> translations = new HashMap<>();

    public final List<ResourceType> resourceTypes = new ArrayList<>();

    public Entity parent;

    public void setId(String id) {
        this.id = URI.create(id);
    }

    public static class ResourceType {
        public URI id;
        public String name;

        public ResourceType(String name) {
            this.name = name;
        }
    }
}
