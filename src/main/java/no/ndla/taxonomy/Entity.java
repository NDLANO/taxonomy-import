package no.ndla.taxonomy;

import java.net.URI;
import java.util.*;

public class Entity {

    public String type;
    public String name;
    public String nodeId;
    public URI contentUri;
    public Map<String, Translation> translations = new HashMap<>();
    public int rank;
    public List<ResourceType> resourceTypes = new ArrayList<>();
    public Boolean isPrimary = false;
    public Entity parent;
    public List<Filter> filters = new ArrayList<>();
    public String oldUrl;
    private URI id;

    public URI getId() {
        return id;
    }

    public void setId(String id) {
        this.id = URI.create(id);
    }

    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(type, entity.type) &&
                Objects.equals(name, entity.name) &&
                Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, id);
    }


    public static final class Builder {
        public String name;
        public String nodeId;
        public URI id;
        public URI contentUri;
        public int rank;
        public Map<String, Translation> translations = new HashMap<>();
        public List<ResourceType> resourceTypes = new ArrayList<>();
        public Entity parent;
        public List<Filter> filters = new ArrayList<>();
        public boolean isPrimary = true;
        private String type;

        public Builder() {
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder id(URI id) {
            this.id = id;
            return this;
        }

        public Builder contentUri(URI contentUri) {
            this.contentUri = contentUri;
            return this;
        }

        public Builder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public Builder translations(Map<String, Translation> translations) {
            this.translations = translations;
            return this;
        }

        public Builder resourceTypes(List<ResourceType> resourceTypes) {
            this.resourceTypes = resourceTypes;
            return this;
        }

        public Builder parent(Entity parent) {
            this.parent = parent;
            return this;
        }

        public Builder filters(List<Filter> filters) {
            this.filters = filters;
            return this;
        }

        public Builder isPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public Entity build() {
            Entity entity = new Entity();
            entity.id = this.id;
            entity.isPrimary = this.isPrimary;
            entity.filters = this.filters;
            entity.nodeId = this.nodeId;
            entity.resourceTypes = this.resourceTypes;
            entity.rank = this.rank;
            entity.name = this.name;
            entity.contentUri = this.contentUri;
            entity.translations = this.translations;
            entity.parent = this.parent;
            entity.type = this.type;
            return entity;
        }
    }
}
