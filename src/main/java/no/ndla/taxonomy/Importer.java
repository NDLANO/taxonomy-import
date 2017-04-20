package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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
        if (null == entity.id) {
            if (isNotEmpty(entity.nodeId)) {
                entity.id = URI.create("urn:resource:1:" + entity.nodeId);
            } else {
                System.out.println("Unable to create ID for entity " + entity.name + ". Skipping.");
                return null;
            }
        }

        try {
            ResourceIndexDocument resource = restClient.getResource(entity.id);
            System.out.println("Updating resource: " + entity.id);
            URI location = updateResource(entity, resource);
            //get rts for resource, check if replace should be done
            updateResourceResourceTypeConnections(entity);
            return location;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Creating resource: " + entity.id + " with nodeId: " + entity.nodeId);
            return createResource(entity);
        }
    }

    private void updateResourceResourceTypeConnections(Entity entity) {
        List<ResourceTypeIndexDocument> currentResourceTypes = Arrays.asList(restClient.getResourceTypesForResource(entity.id));

        for (Entity.ResourceType resourceType : entity.resourceTypes) {
            if (currentResourceTypes.stream().noneMatch(rt -> rt.name.equalsIgnoreCase(resourceType.name))) {
                addResourceTypeToResource(entity.id, resourceType);
            }
        }

        for (ResourceTypeIndexDocument resourceType : currentResourceTypes) {
            if (entity.resourceTypes.stream().noneMatch(rt -> rt.name.equalsIgnoreCase(resourceType.name))) {
                removeResourceTypeFromResource(resourceType.connectionId);
            }
        }
    }

    private void removeResourceTypeFromResource(URI connectionId) {
        restClient.removeResourceResourceType(connectionId);
    }

    private URI updateResource(Entity entity, ResourceIndexDocument resource) {
        if (entity.contentUri == null) entity.contentUri = resource.contentUri;
        if (entity.name == null) entity.name = resource.name;
        URI location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, RESOURCE_TYPE);

        ResourceTypeIndexDocument[] resourceTypesForResource = restClient.getResourceTypesForResource(entity.id);
        if (resourceTypesForResource.length == 0) addResourceTypesToResource(entity.id, entity.resourceTypes);

        return location;
    }

    private URI createResource(Entity entity) {
        URI location = restClient.createResource(entity.id, entity.name, entity.contentUri);
        entity.id = getId(location);
        addResourceTypesToResource(entity.id, entity.resourceTypes);
        return location;
    }

    private void addResourceTypesToResource(URI resourceId, List<Entity.ResourceType> resourceTypes) {
        for (Entity.ResourceType resourceType : resourceTypes) {
            addResourceTypeToResource(resourceId, resourceType);
        }
    }

    private void addResourceTypeToResource(URI resourceId, Entity.ResourceType resourceType) {
        URI resourceTypeId = getOrCreateResourceTypeId(resourceType);
        restClient.addResourceResourceType(resourceId, resourceTypeId);
    }

    private URI getOrCreateResourceTypeId(Entity.ResourceType resourceType) {
        updateResourceTypeCache();
        if (!resourceTypeCache.containsKey(resourceType.name)) {
            URI location = restClient.createResourceType(resourceType.id, resourceType.name);
            URI id = getId(location);
            resourceType.id = id;
            resourceTypeCache.put(resourceType.name, id);
        }
        return resourceTypeCache.get(resourceType.name);
    }

    private void updateResourceTypeCache() {
        if (!resourceTypeCache.isEmpty()) return;

        no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument[] resourceTypes = restClient.getResourceTypes();
        for (no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument resourceType : resourceTypes) {
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
            System.out.println("Updating topic: " + entity.id);
            location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, TOPIC_TYPE);
        } catch (Exception e) {
            if (entity.nodeId != null && entity.id == null) {
                entity.id = URI.create("urn:topic:1:" + entity.nodeId);
                try {
                    restClient.getTopic(entity.id);
                    System.out.println("Updating topic: " + entity.id);
                    location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, TOPIC_TYPE);
                } catch (Exception ex) {
                    System.out.println("Creating topic: " + entity.id);
                    location = restClient.createTopic(entity.id, entity.name, entity.contentUri);
                }
            } else {
                System.out.println("Creating topic: " + entity.id);
                location = restClient.createTopic(entity.id, entity.name, entity.contentUri);
            }
        }
        return location;
    }

    private URI importSubject(Entity entity) {
        URI location;

        try {
            restClient.getSubject(entity.id);
            System.out.println("Updating subject: " + entity.id);
            location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, SUBJECT_TYPE);
        } catch (Exception e) {
            System.out.println("Creating subject: " + entity.id + " with name " + entity.name);
            location = restClient.createSubject(entity.id, entity.name, entity.contentUri);
        }
        return location;
    }

}
