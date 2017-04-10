package no.ndla.taxonomy;

import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.ResourceTypeIndexDocument;
import no.ndla.taxonomy.client.TaxonomyRestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class Importer {
    public static final String SUBJECT_TYPE = "Subject";
    public static final String TOPIC_TYPE = "Topic";
    public static final String RESOURCE_TYPE = "Resource";

    private static final Map<String, URI> resourceTypeCache = new HashMap<>();

    private TaxonomyRestClient restClient;

    public Importer(TaxonomyRestClient restClient) {
        this.restClient = restClient;
    }

    void doImport(Entity entity) {
        URI location = importEntity(entity);
        entity.id = getId(location);

        if (entity.parent != null && entity.parent.type.equals(SUBJECT_TYPE) && entity.type.equals(TOPIC_TYPE)) {
            importSubjectTopic(entity);
        } else if (entity.parent != null && entity.parent.type.equals(TOPIC_TYPE) && entity.type.equals(TOPIC_TYPE)) {
            importTopicSubtopic(entity);
        } else if (entity.parent != null && entity.parent.type.equals(TOPIC_TYPE) && entity.type.equals(RESOURCE_TYPE)) {
            importTopicResource(entity);
        }

        for (Map.Entry<String, Translation> entry : entity.translations.entrySet()) {
            restClient.addTranslation(location, entry.getKey(), entry.getValue());
        }

    }


    private URI getId(URI location) {
        String id = substringAfterLast(location.toString(), "/");
        return URI.create(id);
    }

    private void importTopicResource(Entity entity) {
        try {
            restClient.addTopicResource(entity.parent.id, entity.id);
        } catch (Exception e) {
        }
    }

    private void importTopicSubtopic(Entity entity) {
        try {
            restClient.addTopicSubtopic(entity.parent.id, entity.id);
        } catch (Exception e) {
        }
    }

    private URI importEntity(Entity entity) {
        switch (entity.type) {
            case SUBJECT_TYPE:
                return importSubject(entity);
            case TOPIC_TYPE:
                return importTopic(entity);
            default:
                return importResource(entity);
        }
    }

    private URI importResource(Entity entity) {
        if (null == entity.id) return createResource(entity);

        try {
            ResourceIndexDocument resource = restClient.getResource(entity.id);
            if (entity.contentUri == null) entity.contentUri = resource.contentUri;
            if (entity.name == null) entity.name = resource.name;
            return updateResource(entity);
        } catch (Exception e) {
            return createResource(entity);
        }

    }

    private URI updateResource(Entity entity) {
        URI location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, RESOURCE_TYPE);

    }

    private URI createResource(Entity entity) {
        URI location = restClient.createResource(entity.id, entity.name, entity.contentUri);
        entity.id = getId(location);
        addResourceTypesToResource(entity.id, entity.resourceTypes);
        return location;
    }

    private void addResourceTypesToResource(URI resourceId, List<Entity.ResourceType> resourceTypes) {
        for (Entity.ResourceType resourceType : resourceTypes) {

            URI resourceTypeId = getOrCreateResourceTypeId(resourceType);
            restClient.addResourceResourceType(resourceId, resourceTypeId);
        }
    }

    private URI getOrCreateResourceTypeId(Entity.ResourceType resourceType) {
        updateResourceTypeCache();
        if (!resourceTypeCache.containsKey(resourceType)) {
            URI location = restClient.createResourceType(resourceType.id, resourceType.name);
            URI id = getId(location);
            resourceType.id = id;
            resourceTypeCache.put(resourceType.name, id);
        }
        return resourceTypeCache.get(resourceType.name);
    }

    private void updateResourceTypeCache() {
        if (!resourceTypeCache.isEmpty()) return;

        ResourceTypeIndexDocument[] resourceTypes = restClient.getResourceTypes();
        for (ResourceTypeIndexDocument resourceType : resourceTypes) {
            resourceTypeCache.put(resourceType.name, resourceType.id);
        }
    }


    private void importSubjectTopic(Entity entity) {
        try {
            restClient.addSubjectTopic(entity.parent.id, entity.id);
        } catch (Exception e) {
        }
    }

    private URI importTopic(Entity entity) {
        URI location;
        try {
            restClient.getTopic(entity.id);
            location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, TOPIC_TYPE);
        } catch (Exception e) {
            location = restClient.createTopic(entity.id, entity.name, entity.contentUri);
        }
        return location;
    }

    private URI importSubject(Entity entity) {
        URI location;
        try {
            restClient.getSubject(entity.id);
            location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, SUBJECT_TYPE);
        } catch (Exception e) {
            location = restClient.createSubject(entity.id, entity.name, entity.contentUri);
        }
        return location;
    }

}
