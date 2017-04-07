package no.ndla.taxonomy.client;

import no.ndla.taxonomy.Importer;
import no.ndla.taxonomy.Translation;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TaxonomyRestClient {
    RestTemplate restTemplate = new RestTemplate();

    private String urlBase = "http://localhost:5000";
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

    public void addSubjectTopic(URI subjectId, URI topicId) {
        AddTopicToSubjectCommand cmd = new AddTopicToSubjectCommand();
        cmd.subjectid = subjectId;
        cmd.topicid = topicId;
        restTemplate.postForLocation(urlBase + "/subject-topics", cmd);
    }

    public TopicIndexDocument getTopic(URI id) {
        String url = urlBase + "/topics/" + id;
        return restTemplate.getForObject(url, TopicIndexDocument.class);
    }

    public void addTopicSubtopic(URI topicId, URI subtopicId) {
        AddSubtopicToTopicCommand cmd = new AddSubtopicToTopicCommand();
        cmd.topicid = topicId;
        cmd.subtopicid = subtopicId;
        restTemplate.postForLocation(urlBase + "/topic-subtopics", cmd);
    }

    public URI createResource(URI id, String name, URI contentUri) {
        CreateResourceCommand cmd = new CreateResourceCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.contentUri = contentUri;

        return restTemplate.postForLocation(urlBase + "/resources", cmd);
    }

    public void addTopicResource(URI topicId, URI resourceId) {
        AddResourceToTopicCommand cmd = new AddResourceToTopicCommand();
        cmd.resourceid = resourceId;
        cmd.topicid = topicId;

        restTemplate.postForLocation(urlBase + "/topic-resources", cmd);
    }
}
