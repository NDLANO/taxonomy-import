package no.ndla.taxonomy;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {

    public String type, name, nodeId;
    public URI id, contentUri;
    public int rank;

    public final Map<String, Translation> translations = new HashMap<>();

    public final List<ResourceType> resourceTypes = new ArrayList<>();

    public Entity parent;
    public List<Filter> filters = new ArrayList<>();
    public boolean shouldSetPrimary = false;

    public void setId(String id) {
        this.id = URI.create(id);
    }

}
