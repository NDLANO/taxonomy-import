package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;

import java.net.URI;
import java.util.Map;

public class Importer {
    public static final String SUBJECT_TYPE = "Subject";
    public static final String TOPIC_TYPE = "Topic";
    public static final String RESOURCE_TYPE = "Resource";

    private TaxonomyRestClient restClient;

    public Importer(TaxonomyRestClient restClient) {
        this.restClient = restClient;
    }

    void doImport(Entity entity) {
        URI location = importEntity(entity);
        entity.id = URI.create(location.toString().substring(location.toString().lastIndexOf("/") + 1));

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
        URI location;
        try {
            location = restClient.createResource(entity.id, entity.name, entity.contentUri);
        } catch (Exception e) {
            location = restClient.updateEntity(entity.id, entity.name, entity.contentUri, RESOURCE_TYPE);
        }
        return location;
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
