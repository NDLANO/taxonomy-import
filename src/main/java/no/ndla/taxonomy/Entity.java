package no.ndla.taxonomy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Entity {

    public String type, name, nodeId;
    public URI id, parentId, contentUri;

    public final Map<String, Translation> translations = new HashMap<>();

}
