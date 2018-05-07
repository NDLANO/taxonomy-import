package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.relevances.RelevanceIndexDocument;
import no.ndla.taxonomy.client.resources.FilterIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import no.ndla.taxonomy.client.subjectTopics.SubjectTopicIndexDocument;
import no.ndla.taxonomy.client.subjects.TopicIndexDocument;
import no.ndla.taxonomy.client.topicResources.TopicResourceIndexDocument;
import no.ndla.taxonomy.client.topicSubtopics.TopicSubtopicIndexDocument;
import no.ndla.taxonomy.client.topics.SubtopicIndexDocument;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Importer {
    public static final String SUBJECT_TYPE = "Subject";
    public static final String TOPIC_TYPE = "Topic";
    public static final String RESOURCE_TYPE = "Resource";
    public static final String KJERNESTOFF = "Kjernestoff";
    public static final String TILLEGGSSSTOFF = "Tilleggsstoff";

    private Entity currentSubject;

    private static final Map<String, URI> resourceTypeCache = new HashMap<>();
    private static final Map<String, URI> filterCache = new HashMap<>();
    private static final Map<String, URI> relevanceCache = new HashMap<>();

    private TaxonomyRestClient restClient;

    public Importer(TaxonomyRestClient restClient) {
        this.restClient = restClient;
    }

    void doImport(Entity entity) {
        if (entity == null) return;

        if (entity.type.equals(SUBJECT_TYPE)) {
            currentSubject = entity;
        }

        URI location = importEntity(entity);
        System.out.println("Entity imported: " + entity.nodeId);
        entity.setId(getId(location).toString());

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
            boolean currentTopicConnectionFound = false;
            no.ndla.taxonomy.client.topics.ResourceIndexDocument[] resourcesForTopic = restClient.getResourcesForTopic(entity.parent.getId());
            for (no.ndla.taxonomy.client.topics.ResourceIndexDocument resource : resourcesForTopic) {
                if (resource.id.equals(entity.getId())) {
                    TopicResourceIndexDocument topicResource = restClient.getTopicResource(resource.connectionId);
                    System.out.println("Updating topic resource for resource: " + entity.getId());
                    topicResource.rank = entity.rank;
                    topicResource.topicid = entity.parent.getId();
                    topicResource.primary = entity.isPrimary;
                    restClient.updateTopicResource(topicResource);
                    currentTopicConnectionFound = topicResource.topicid.equals(entity.parent.getId());
                }
            }
            if (!currentTopicConnectionFound) {
                System.out.println("Adding topic resource for: " + entity.getId());
                restClient.addTopicResource(entity.parent.getId(), entity.getId(), entity.rank, entity.isPrimary);
            }
        } catch (Exception e) {
            System.out.println("entity failed: " + entity.getId());
            e.printStackTrace();
        }
    }

    private void importTopicSubtopic(Entity entity) {
        try {
            boolean hasFoundCurrentTopicConnection = false;
            if (currentSubject != null) {
                TopicIndexDocument[] topicsForSubject = restClient.getTopicsForSubject(currentSubject.getId());
                for (TopicIndexDocument topic : topicsForSubject) {
                    if (topic.id.equals(entity.getId())) {
                        TopicSubtopicIndexDocument topicSubtopic = restClient.getTopicSubtopic(topic.connectionId);
                        System.out.println("Updating topic subtopic connection for topic: " + entity.getId() + " with rank " + entity.rank);
                        topicSubtopic.rank = entity.rank;
                        topicSubtopic.primary = entity.isPrimary;
                        restClient.updateTopicSubtopic(topicSubtopic);
                        if (!hasFoundCurrentTopicConnection) {
                            hasFoundCurrentTopicConnection = topicSubtopic.topicid.equals(entity.parent.getId());
                        }
                    }
                }
            }
            if (!hasFoundCurrentTopicConnection) {
                System.out.println("Adding topic subtopics connection for topic: " + entity.getId() + " with rank " + entity.rank);
                restClient.addTopicSubtopic(entity.parent.getId(), entity.getId(), entity.rank, entity.isPrimary);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (null == entity.getId()) {
            if (isNotEmpty(entity.nodeId)) {
                try {
                    entity.setId("urn:resource:1:" + entity.nodeId);
                } catch (Exception e) {
                    System.out.println("Error creating ID for entity " + entity.name + " with nodeid: '" + entity.nodeId + "': " + e.getMessage() + " Skipping.");
                }
            } else {
                System.out.println("Unable to create ID for entity " + entity.name + ". Skipping.");
                return null;
            }
        }

        try {
            ResourceIndexDocument resource = restClient.getResource(entity.getId());
            URI location = updateResource(entity, resource);
            System.out.println("Updated resource: " + entity.getId());
            //get rts for resource, check if replace should be done
            updateResourceResourceTypeConnections(entity);
            updateFiltersForResource(entity);
            return location;
        } catch (Exception e) {
            System.out.println("Creating resource: " + entity.getId() + " with nodeId: " + entity.nodeId);
            URI resourceURI = createResource(entity);
            updateFiltersForResource(entity);
            return resourceURI;
        }
    }

    private void updateFiltersForResource(Entity entity) {
        Entity subject = getSubject(entity);

        List<FilterIndexDocument> currentFilters = Arrays.asList(restClient.getFiltersForResource(entity.getId()));

        for (Filter filter : entity.filters) {
            if (currentFilters.stream().noneMatch(f -> f.name.equalsIgnoreCase(filter.name))) {
                addFilterToResource(entity.getId(), filter, subject.getId());
                System.out.println("Added filter resource connection: " + filter.name);
            }
        }
    }

    private void updateFiltersForTopic(Entity entity) {
        Entity subject = getSubject(entity);
        List<FilterIndexDocument> currentFilters = Arrays.asList(restClient.getFiltersForTopic(entity.getId()));

        for (Filter filter : entity.filters) {
            if (currentFilters.stream().noneMatch(f -> f.name.equalsIgnoreCase(filter.name))) {
                addFilterToTopic(entity.getId(), filter, subject.getId());
                System.out.println("Added filter topic connection: " + filter.name);
            }
        }
    }

    private void addFilterToResource(URI resourceId, Filter filter, URI subjectId) {
        URI filterId = getOrCreateFilterId(filter, subjectId);
        URI relevanceId = getOrCreateRelevanceId(filter.relevance);
        restClient.addResourceFilter(resourceId, filterId, relevanceId);
    }

    private void addFilterToTopic(URI topicId, Filter filter, URI subjectId) {
        URI filterId = getOrCreateFilterId(filter, subjectId);
        URI relevanceId = getOrCreateRelevanceId(filter.relevance);
        restClient.addTopicFilter(topicId, filterId, relevanceId);
    }


    private URI getOrCreateRelevanceId(Relevance relevance) {
        updateRelevanceCache();
        if (!relevanceCache.containsKey(relevance.name)) {
            if (relevance.name.equals(KJERNESTOFF)) {
                relevance.id = URI.create("urn:relevance:core");
            } else if (relevance.name.equals(TILLEGGSSSTOFF)) {
                relevance.id = URI.create("urn:relevance:supplementary");
            }

            URI location = restClient.createRelevance(relevance.id, relevance.name);
            URI id = getId(location);
            relevance.id = id;
            relevanceCache.put(relevance.name, id);
        }
        return relevanceCache.get(relevance.name);
    }

    private void updateRelevanceCache() {
        if (!relevanceCache.isEmpty()) return;

        RelevanceIndexDocument[] relevances = restClient.getRelevances();
        for (RelevanceIndexDocument relevance : relevances) {
            relevanceCache.put(relevance.name, relevance.id);
        }
    }

    private URI getOrCreateFilterId(Filter filter, URI subjectId) {
        updateFilterCache(subjectId);
        if (!filterCache.containsKey(filter.name)) {
            URI location = restClient.createFilter(filter.id, filter.name, subjectId);
            URI id = getId(location);
            filter.id = id;
            filterCache.put(filter.name, id);
        }
        return filterCache.get(filter.name);
    }

    private void updateFilterCache(URI subjectId) {
        if (!filterCache.isEmpty()) return;

        no.ndla.taxonomy.client.subjects.FilterIndexDocument[] filters = restClient.getFiltersForSubject(subjectId);
        for (no.ndla.taxonomy.client.subjects.FilterIndexDocument filter : filters) {
            filterCache.put(filter.name, filter.id);
        }
    }


    private Entity getSubject(Entity entity) {
        while (true) {
            if (SUBJECT_TYPE.equals(entity.type)) return entity;
            if (entity.parent == null) return null;
            entity = entity.parent;
        }
    }

    private void updateResourceResourceTypeConnections(Entity entity) {
        List<ResourceTypeIndexDocument> currentResourceTypes = Arrays.asList(restClient.getResourceTypesForResource(entity.getId()));

        for (ResourceType resourceType : entity.resourceTypes) {
            if (currentResourceTypes.stream().noneMatch(rt -> rt.name.equalsIgnoreCase(resourceType.name))) {
                addResourceTypeToResource(entity.getId(), resourceType);
                System.out.println("Importing resource resource type connection: " + resourceType.name);
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
        URI location = restClient.updateEntity(entity.getId(), entity.name, entity.contentUri, RESOURCE_TYPE);

        ResourceTypeIndexDocument[] resourceTypesForResource = restClient.getResourceTypesForResource(entity.getId());
        if (resourceTypesForResource.length == 0) addResourceTypesToResource(entity.getId(), entity.resourceTypes);

        return location;
    }

    private URI createResource(Entity entity) {
        URI location = restClient.createResource(entity.getId(), entity.name, entity.contentUri);
        entity.setId(getId(location).toString());
        System.out.println("Added resource: " + entity.getId());
        addResourceTypesToResource(entity.getId(), entity.resourceTypes);
        return location;
    }

    private void addResourceTypesToResource(URI resourceId, List<ResourceType> resourceTypes) {
        for (ResourceType resourceType : resourceTypes) {
            addResourceTypeToResource(resourceId, resourceType);
        }
    }

    private void addResourceTypeToResource(URI resourceId, ResourceType resourceType) {
        URI resourceTypeId = getOrCreateResourceTypeId(resourceType);
        restClient.addResourceResourceType(resourceId, resourceTypeId);
    }

    private URI getOrCreateResourceTypeId(ResourceType resourceType) {
        updateResourceTypeCache();
        if (!resourceTypeCache.containsKey(resourceType.name)) {
            if (isNotBlank(resourceType.parentName)) {
                resourceType.parentId = resourceTypeCache.get(resourceType.parentName);
            }
            System.out.println("Creating resource type " + resourceType.name + " with id: " + resourceType.id);
            URI location = restClient.createResourceType(resourceType.id, resourceType.name, resourceType.parentId);
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
            for (no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument subtype : resourceType.subtypes) {
                resourceTypeCache.put(subtype.name, subtype.id);
            }
        }
    }

    private void importSubjectTopic(Entity entity) {
        try {
            TopicIndexDocument[] topicsForSubject = restClient.getTopicsForSubject(entity.parent.getId());
            for (TopicIndexDocument topic : topicsForSubject) {
                if (topic.id.equals(entity.getId())) {
                    SubjectTopicIndexDocument subjectTopic = restClient.getSubjectTopic(topic.connectionId);
                    subjectTopic.rank = entity.rank;
                    System.out.println("Updating subject topic for topic: " + entity.getId());
                    restClient.updateSubjectTopic(subjectTopic);
                    return;
                }
            }
            System.out.println("Adding topic " + entity.getId() + " to subject " + entity.parent.getId());
            restClient.addSubjectTopic(entity.parent.getId(), entity.getId(), entity.rank);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URI importTopic(Entity entity) {
        URI location;
        try {
            restClient.getTopic(entity.getId());
            System.out.println("Importing topic");
            location = restClient.updateEntity(entity.getId(), entity.name, entity.contentUri, TOPIC_TYPE);
            System.out.println("Updated topic: " + entity.getId());
        } catch (Exception e) {
            if (entity.nodeId != null && entity.getId() == null) {
                entity.setId("urn:topic:1:" + entity.nodeId);
                try {
                    restClient.getTopic(entity.getId());
                    System.out.println("Updating topic: " + entity.getId());
                    location = restClient.updateEntity(entity.getId(), entity.name, entity.contentUri, TOPIC_TYPE);
                } catch (Exception ex) {
                    System.out.println("Creating topic: " + entity.getId());
                    location = restClient.createTopic(entity.getId(), entity.name, entity.contentUri);
                }
            } else {
                System.out.println("Creating topic: " + entity.getId());
                location = restClient.createTopic(entity.getId(), entity.name, entity.contentUri);
            }
        }
        updateFiltersForTopic(entity);
        return location;
    }

    private URI importSubject(Entity entity) {
        URI location;

        try {
            restClient.getSubject(entity.getId());
            System.out.println("Updating subject: " + entity.getId());
            location = restClient.updateEntity(entity.getId(), entity.name, entity.contentUri, SUBJECT_TYPE);
        } catch (Exception e) {
            System.out.println("Creating subject: " + entity.getId() + " with name " + entity.name);
            location = restClient.createSubject(entity.getId(), entity.name, entity.contentUri);
        }
        return location;
    }

    public void doImportWithoutBatchmode(Entity entity) {
        restClient.setNoBatchMode();
        doImport(entity);
    }

    public List<Entity> listResourcesAndTopicsForSubjects(URI subjectUri) {
        List<Entity> resultList = new ArrayList<>();
        TopicIndexDocument[] topics = restClient.getTopicsForSubject(subjectUri, false);
        for (TopicIndexDocument topic : topics) {
            Entity topicEntity = new Entity.Builder()
                    .type("Topic")
                    .name(topic.name)
                    .id(topic.id)
                    .parent(null)
                    .build();
            resultList.add(topicEntity);
            resultList.addAll(listResourcesAndTopics(topicEntity));
            no.ndla.taxonomy.client.topics.ResourceIndexDocument[] resources = restClient.getResourcesForTopic(topic.id);
            for (no.ndla.taxonomy.client.topics.ResourceIndexDocument resource : resources) {
                if (resource.isPrimary) {
                    Entity resourceEntity = new Entity.Builder()
                            .type("Resource")
                            .name(resource.name)
                            .id(resource.id)
                            .isPrimary(resource.isPrimary)
                            .build();
                    resultList.add(resourceEntity);
                }
            }
        }
        return resultList;
    }

    public List<Entity> listResourcesAndTopics(Entity topicEntity) {
        List<Entity> resultList = new ArrayList<>();
        SubtopicIndexDocument[] topics = restClient.getSubtopicsForTopic(topicEntity.getId());
        for (SubtopicIndexDocument topic : topics) {
            Entity entity = new Entity.Builder()
                    .type("Topic")
                    .name(topic.name)
                    .id(topic.id)
                    .parent(topicEntity)
                    .isPrimary(topic.isPrimary)
                    .build();
            resultList.add(entity);
            if (topic.isPrimary) {
                resultList.addAll(listResourcesAndTopics(entity));
            }
            no.ndla.taxonomy.client.topics.ResourceIndexDocument[] resources = restClient.getResourcesForTopic(topic.id);
            for (no.ndla.taxonomy.client.topics.ResourceIndexDocument resource : resources) {
                if (resource.isPrimary) {
                    Entity resourceEntity = new Entity.Builder()
                            .type("Resource")
                            .name(resource.name)
                            .id(resource.id)
                            .isPrimary(resource.isPrimary)
                            .build();
                    resultList.add(resourceEntity);
                }
            }
        }
        return resultList;
    }

    public void deleteList(List<Entity> entities) {
        for (Entity entity : entities) {
            try {
                restClient.removeEntity(entity);
            } catch (HttpClientErrorException ignore) {
                System.out.println("Failed to remove: " + entity + " due to " + ignore.toString() + " - maybe node occurred twice in taxonomy?");
            }
        }
    }
}
