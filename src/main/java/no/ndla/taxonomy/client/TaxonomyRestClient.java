package no.ndla.taxonomy.client;

import no.ndla.taxonomy.Entity;
import no.ndla.taxonomy.Importer;
import no.ndla.taxonomy.Translation;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaxonomyRestClient {
    RestTemplate restTemplate = new RestTemplate();

    String urlBase = "http://localhost:5000";
    Map<String, String> controllerNames = Stream.of(
            new AbstractMap.SimpleEntry<>(Importer.SUBJECT_TYPE, "/subjects"),
            new AbstractMap.SimpleEntry<>(Importer.TOPIC_TYPE, "/topics"),
            new AbstractMap.SimpleEntry<String, String>(Importer.RESOURCE_TYPE, "/resources"))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));



        public SubjectIndexDocument getSubject(URI id) {
        String url = urlBase + "/subjects/" + id;
        return restTemplate.getForObject(url, SubjectIndexDocument.class);
    }

    public URI updateEntity(URI id, String name, URI contentUri, String entityType) {
        URI location = URI.create(controllerNames.get(entityType) + "/" + id);
        UpdateSubjectCommand cmd = new UpdateSubjectCommand();
        cmd.name = name;
        cmd.contentUri = contentUri;
        restTemplate.put(urlBase + location, cmd);
        return location;
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
        URI location = restTemplate.postForLocation(urlBase + "/topics", cmd);
        return location;
    }

    public void addSubjectTopic(Entity entity) {
        AddTopicToSubjectCommand cmd = new AddTopicToSubjectCommand();
        cmd.subjectid = entity.parent.id;
        cmd.topicid = entity.id;
        restTemplate.postForLocation(urlBase + "/subject-topics", cmd);
    }

    public TopicIndexDocument getTopic(URI id) {
        String url = urlBase + "/topics/" + id;
        return restTemplate.getForObject(url, TopicIndexDocument.class);
    }

    public void addTopicSubtopic(Entity entity) {
        AddSubtopicToTopicCommand cmd = new AddSubtopicToTopicCommand();
        cmd.topicid = entity.parent.id;
        cmd.subtopicid = entity.id;
        restTemplate.postForLocation(urlBase + "/topic-subtopics", cmd);
    }

    public URI createResource(URI id, String name, URI contentUri) {
        CreateResourceCommand cmd = new CreateResourceCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.contentUri = contentUri;

        return restTemplate.postForLocation(urlBase + "/resources", cmd);
    }

    public void addTopicResource(Entity entity) {
        AddResourceToTopicCommand cmd = new AddResourceToTopicCommand();
        cmd.resourceid = entity.id;
        cmd.topicid = entity.parent.id;

        restTemplate.postForLocation(urlBase + "/topic-resources", cmd);
    }
}
