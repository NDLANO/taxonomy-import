package no.ndla.taxonomy.client;

import no.ndla.taxonomy.Importer;
import no.ndla.taxonomy.Translation;
import no.ndla.taxonomy.client.resourceResourceTypes.CreateResourceResourceTypeCommand;
import no.ndla.taxonomy.client.resourceTypes.CreateResourceTypeCommand;
import no.ndla.taxonomy.client.resources.CreateResourceCommand;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.subjectTopics.AddTopicToSubjectCommand;
import no.ndla.taxonomy.client.subjects.CreateSubjectCommand;
import no.ndla.taxonomy.client.subjects.SubjectIndexDocument;
import no.ndla.taxonomy.client.subjects.UpdateSubjectCommand;
import no.ndla.taxonomy.client.topicResources.AddResourceToTopicCommand;
import no.ndla.taxonomy.client.topicSubtopics.AddSubtopicToTopicCommand;
import no.ndla.taxonomy.client.topics.CreateTopicCommand;
import no.ndla.taxonomy.client.topics.TopicIndexDocument;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TaxonomyRestClient {
    RestTemplate restTemplate;
    private String urlBase;

    public TaxonomyRestClient(String urlBase, RestTemplate restTemplate) {
        this.urlBase = urlBase;
        this.restTemplate = restTemplate;
    }

    private static final Map<String, String> controllerNames = new HashMap<String, String>() {
        {
            put(Importer.SUBJECT_TYPE, "/subjects");
            put(Importer.TOPIC_TYPE, "/topics");
            put(Importer.RESOURCE_TYPE, "/resources");
        }
    };

    public SubjectIndexDocument getSubject(URI id) {
        String url = urlBase + "/subjects/" + id;
        return restTemplate.getForObject(url, SubjectIndexDocument.class);
    }

    public URI updateEntity(URI id, String name, URI contentUri, String entityType) {
        URI location = getLocation(id, entityType);
        UpdateSubjectCommand cmd = new UpdateSubjectCommand();
        cmd.name = name;
        cmd.contentUri = contentUri;
        restTemplate.put(urlBase + location, cmd);
        return location;
    }

    private URI getLocation(URI id, String entityType) {
        return URI.create(controllerNames.get(entityType) + "/" + id);
    }

    public URI createSubject(URI id, String name, URI contentUri) {
        CreateSubjectCommand cmd = new CreateSubjectCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.contentUri = contentUri;

        URI location = restTemplate.postForLocation(urlBase + "/subjects", cmd);
        URI subjectid = getId(location);
        System.out.println("created: " + subjectid);
        return location;
    }

    private URI getId(URI location) {
        String locationString = location.toString();
        String id = locationString.substring(locationString.lastIndexOf("/") + 1);
        return URI.create(id);
    }

    public void addTranslation(URI location, String language, Translation translation) {
        UpdateTranslationCommand cmd = new UpdateTranslationCommand();
        cmd.name = translation.name;
        restTemplate.put(urlBase + location + "/translations/" + language, cmd);
    }

    public URI createTopic(URI id, String name, URI contentUri) {
        CreateTopicCommand cmd = new CreateTopicCommand();
        cmd.name = name;
        cmd.id = id;
        cmd.contentUri = contentUri;
        return restTemplate.postForLocation(urlBase + "/topics", cmd);
    }

    public URI addSubjectTopic(URI subjectId, URI topicId) {
        AddTopicToSubjectCommand cmd = new AddTopicToSubjectCommand();
        cmd.subjectid = subjectId;
        cmd.topicid = topicId;
        return restTemplate.postForLocation(urlBase + "/subject-topics", cmd);
    }

    public TopicIndexDocument getTopic(URI id) {
        String url = urlBase + "/topics/" + id;
        return restTemplate.getForObject(url, TopicIndexDocument.class);
    }

    public URI addTopicSubtopic(URI topicId, URI subtopicId) {
        AddSubtopicToTopicCommand cmd = new AddSubtopicToTopicCommand();
        cmd.topicid = topicId;
        cmd.subtopicid = subtopicId;
        return restTemplate.postForLocation(urlBase + "/topic-subtopics", cmd);
    }

    public URI createResource(URI id, String name, URI contentUri) {
        CreateResourceCommand cmd = new CreateResourceCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.contentUri = contentUri;

        return restTemplate.postForLocation(urlBase + "/resources", cmd);
    }

    public URI addTopicResource(URI topicId, URI resourceId) {
        AddResourceToTopicCommand cmd = new AddResourceToTopicCommand();
        cmd.resourceid = resourceId;
        cmd.topicid = topicId;

        return restTemplate.postForLocation(urlBase + "/topic-resources", cmd);
    }

    public ResourceIndexDocument getResource(URI id) {
        String url = urlBase + "/resources/" + id;
        return restTemplate.getForObject(url, ResourceIndexDocument.class);
    }

    public ResourceTypeIndexDocument[] getResourceTypes() {
        String url = urlBase + "/v1/resource-types";
        return restTemplate.getForObject(url, ResourceTypeIndexDocument[].class);
    }

    public URI createResourceType(URI id, String name) {
        CreateResourceTypeCommand cmd = new CreateResourceTypeCommand();
        cmd.id = id;
        cmd.name = name;
        return restTemplate.postForLocation(urlBase + "/v1/resource-types", cmd);
    }

    public URI addResourceResourceType(URI resourceId, URI resourceTypeId) {
        CreateResourceResourceTypeCommand cmd = new CreateResourceResourceTypeCommand();
        cmd.resourceId = resourceId;
        cmd.resourceTypeId = resourceTypeId;
        return restTemplate.postForLocation(urlBase + "/v1/resource-resourcetypes", cmd);
    }

    public ResourceTypeIndexDocument[] getResourceTypesForResource(URI id) {
        String url = urlBase + "/resources/" + id + "/resource-types";
        return restTemplate.getForObject(url, ResourceTypeIndexDocument[].class);
    }

}
