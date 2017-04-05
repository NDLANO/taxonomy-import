package no.ndla.taxonomy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Entity {

    public String type, name, nodeId;
    public URI id, contentUri;

    public final Map<String, Translation> translations = new HashMap<>();

    public Entity parent;

    public void setId(String id) {
        this.id = URI.create(id);
    }
}
