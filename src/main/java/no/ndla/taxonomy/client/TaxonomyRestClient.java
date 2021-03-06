package no.ndla.taxonomy.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ndla.taxonomy.Entity;
import no.ndla.taxonomy.Importer;
import no.ndla.taxonomy.Translation;
import no.ndla.taxonomy.client.filters.CreateFilterCommand;
import no.ndla.taxonomy.client.relevances.CreateRelevanceCommand;
import no.ndla.taxonomy.client.relevances.RelevanceIndexDocument;
import no.ndla.taxonomy.client.resourceFilters.AddFilterToResourceCommand;
import no.ndla.taxonomy.client.resourceResourceTypes.CreateResourceResourceTypeCommand;
import no.ndla.taxonomy.client.resourceTypes.CreateResourceTypeCommand;
import no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument;
import no.ndla.taxonomy.client.resources.CreateResourceCommand;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.subjectTopics.AddTopicToSubjectCommand;
import no.ndla.taxonomy.client.subjectTopics.SubjectTopicIndexDocument;
import no.ndla.taxonomy.client.subjectTopics.UpdateSubjectTopicCommand;
import no.ndla.taxonomy.client.subjects.CreateSubjectCommand;
import no.ndla.taxonomy.client.subjects.FilterIndexDocument;
import no.ndla.taxonomy.client.subjects.SubjectIndexDocument;
import no.ndla.taxonomy.client.subjects.UpdateSubjectCommand;
import no.ndla.taxonomy.client.topicFilters.AddFilterToTopicCommand;
import no.ndla.taxonomy.client.topicResources.AddResourceToTopicCommand;
import no.ndla.taxonomy.client.topicResources.TopicResourceIndexDocument;
import no.ndla.taxonomy.client.topicResources.UpdateTopicResourceCommand;
import no.ndla.taxonomy.client.topicSubtopics.AddSubtopicToTopicCommand;
import no.ndla.taxonomy.client.topicSubtopics.TopicSubtopicIndexDocument;
import no.ndla.taxonomy.client.topicSubtopics.UpdateTopicSubtopicCommand;
import no.ndla.taxonomy.client.topics.CreateTopicCommand;
import no.ndla.taxonomy.client.topics.SubtopicIndexDocument;
import no.ndla.taxonomy.client.topics.TopicIndexDocument;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public class TaxonomyRestClient {
    private RestTemplate restTemplate;
    private String urlBase;
    public Authentication authentication;
    public Long last_token_update;
    private String clientId;
    private String clientSecret;
    private String token_server;

    public TaxonomyRestClient(String urlBase, String clientId, String clientSecret, String token_server, RestTemplate restTemplate) {
        this.urlBase = urlBase;
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.token_server = token_server;
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HeaderRequestInterceptor("batch", "1"));
        if(!this.clientId.equals("ITEST")) {
            interceptors.add((HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
                if(TokenUpdateCheck.shouldUpdateToken(last_token_update, authentication)) {
                    getAccessToken(this.clientId, this.clientSecret, this.token_server);
                }
                if (authentication != null) {
                    request.getHeaders().add("Authorization", "Bearer " + authentication.access_token);
                }
                return execution.execute(request, body);
            });
        }
        restTemplate.setInterceptors(interceptors);
    }

    private static final Map<String, String> controllerNames = new HashMap<String, String>() {
        {
            put(Importer.SUBJECT_TYPE, "/v1/subjects");
            put(Importer.TOPIC_TYPE, "/v1/topics");
            put(Importer.RESOURCE_TYPE, "/v1/resources");
        }
    };

    private void getAccessToken(String clientId, String clientSecret, String token_server){
        CreateAuthCommand cmd = new CreateAuthCommand();
        cmd.grant_type = "client_credentials";
        cmd.client_id = clientId;
        cmd.client_secret = clientSecret;
        cmd.audience = "ndla_system";

        HttpEntity<CreateAuthCommand> request = new HttpEntity<>(cmd);
        ResponseEntity<Authentication> response = null;
        try{
            /*
             * Need to use a different RestTemplate in order to not try to add
             * authorization to the request for authorization (loop).
             */
            response = new RestTemplate().exchange(token_server, HttpMethod.POST, request, Authentication.class);
            authentication = response.getBody();
            last_token_update = Instant.now().toEpochMilli();
        }catch (IllegalStateException | HttpClientErrorException e){
            System.out.println("401 Wrong Credentials? You are using the environment: " + token_server);
        }
    }

    public SubjectIndexDocument getSubject(URI id) {
        String url = urlBase + "/v1/subjects/" + id;
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

        URI location = restTemplate.postForLocation(urlBase + "/v1/subjects", cmd);
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
        return restTemplate.postForLocation(urlBase + "/v1/topics", cmd);
    }

    public URI addSubjectTopic(URI subjectId, URI topicId, int rank) {
        AddTopicToSubjectCommand cmd = new AddTopicToSubjectCommand();
        cmd.subjectid = subjectId;
        cmd.topicid = topicId;
        cmd.rank = rank;
        return restTemplate.postForLocation(urlBase + "/v1/subject-topics", cmd);
    }

    public TopicIndexDocument getTopic(URI id) {
        String url = urlBase + "/v1/topics/" + id;
        return restTemplate.getForObject(url, TopicIndexDocument.class);
    }

    public URI addTopicSubtopic(URI topicId, URI subtopicId, int rank) {
        AddSubtopicToTopicCommand cmd = new AddSubtopicToTopicCommand();
        cmd.topicid = topicId;
        cmd.subtopicid = subtopicId;
        cmd.rank = rank;
        return restTemplate.postForLocation(urlBase + "/v1/topic-subtopics", cmd);
    }


    public URI addTopicSubtopic(URI topicId, URI subtopicId, int rank, boolean shouldSetPrimary) {
        AddSubtopicToTopicCommand cmd = new AddSubtopicToTopicCommand();
        cmd.topicid = topicId;
        cmd.subtopicid = subtopicId;
        cmd.rank = rank;
        cmd.primary = shouldSetPrimary;
        return restTemplate.postForLocation(urlBase + "/v1/topic-subtopics", cmd);
    }

    public URI createResource(URI id, String name, URI contentUri) {
        CreateResourceCommand cmd = new CreateResourceCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.contentUri = contentUri;


        return restTemplate.postForLocation(urlBase + "/v1/resources", cmd);
    }

    public URI addTopicResource(URI topicId, URI resourceId, int rank) {
        AddResourceToTopicCommand cmd = new AddResourceToTopicCommand();
        cmd.resourceId = resourceId;
        cmd.topicid = topicId;
        cmd.rank = rank;

        return restTemplate.postForLocation(urlBase + "/v1/topic-resources", cmd);
    }

    public URI addTopicResource(URI topicId, URI resourceId, int rank, boolean primary) {
        AddResourceToTopicCommand cmd = new AddResourceToTopicCommand();
        cmd.resourceId = resourceId;
        cmd.topicid = topicId;
        cmd.rank = rank;
        cmd.primary = primary;

        return restTemplate.postForLocation(urlBase + "/v1/topic-resources", cmd);
    }

    public URI addTopicFilter(URI topicId, URI filterId, URI relevanceId) {
        AddFilterToTopicCommand cmd = new AddFilterToTopicCommand();
        cmd.filterId = filterId;
        cmd.topicId = topicId;
        cmd.relevanceId = relevanceId;
        return restTemplate.postForLocation(urlBase + "/v1/topic-filters", cmd);
    }

    public ResourceIndexDocument getResource(URI id) {
        String url = urlBase + "/v1/resources/" + id;
        return restTemplate.getForObject(url, ResourceIndexDocument.class);
    }

    public ResourceTypeIndexDocument[] getResourceTypes() {
        String url = urlBase + "/v1/resource-types";
        return restTemplate.getForObject(url, ResourceTypeIndexDocument[].class);
    }

    public URI createResourceType(URI id, String name, URI parentId) {
        CreateResourceTypeCommand cmd = new CreateResourceTypeCommand();
        cmd.id = id;
        cmd.name = name;
        if (parentId != null) {
            cmd.parentId = parentId;
        }
        return restTemplate.postForLocation(urlBase + "/v1/resource-types", cmd);
    }

    public URI addResourceResourceType(URI resourceId, URI resourceTypeId) {
        CreateResourceResourceTypeCommand cmd = new CreateResourceResourceTypeCommand();
        cmd.resourceId = resourceId;
        cmd.resourceTypeId = resourceTypeId;
        return restTemplate.postForLocation(urlBase + "/v1/resource-resourcetypes", cmd);
    }

    public no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument[] getResourceTypesForResource(URI id) {
        String url = urlBase + "/v1/resources/" + id + "/resource-types";
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument[].class);
    }

    public void removeResourceResourceType(URI connectionId) {
        restTemplate.delete(urlBase + "/v1/resource-resourcetypes/{id}", Collections.singletonMap("id", connectionId.toString()));
    }

    public FilterIndexDocument[] getFiltersForSubject(URI subjectId) {
        String url = urlBase + "/v1/subjects/" + subjectId + "/filters";
        return restTemplate.getForObject(url, FilterIndexDocument[].class);
    }

    public no.ndla.taxonomy.client.resources.FilterIndexDocument[] getFiltersForResource(URI resourceId) {
        String url = urlBase + "/v1/resources/" + resourceId + "/filters";
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.resources.FilterIndexDocument[].class);
    }

    public no.ndla.taxonomy.client.resources.FilterIndexDocument[] getFiltersForTopic(URI topicId) {
        String url = urlBase + "/v1/topics/" + topicId + "/filters";
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.resources.FilterIndexDocument[].class);
    }

    public void removeResourceFilter(URI connectionId) {
        restTemplate.delete(urlBase + "/v1/resource-filters/{id}", Collections.singletonMap("id", connectionId.toString()));
    }

    public URI addResourceFilter(URI resourceId, URI filterId, URI relevanceId) {
        AddFilterToResourceCommand cmd = new AddFilterToResourceCommand();
        cmd.resourceId = resourceId;
        cmd.filterId = filterId;
        cmd.relevanceId = relevanceId;

        return restTemplate.postForLocation(urlBase + "/v1/resource-filters", cmd);
    }

    public URI createFilter(URI id, String name, URI subjectId) {
        CreateFilterCommand cmd = new CreateFilterCommand();
        cmd.id = id;
        cmd.name = name;
        cmd.subjectId = subjectId;

        return restTemplate.postForLocation(urlBase + "/v1/filters", cmd);
    }

    public RelevanceIndexDocument[] getRelevances() {
        String url = urlBase + "/v1/relevances";
        return restTemplate.getForObject(url, RelevanceIndexDocument[].class);
    }

    public URI createRelevance(URI id, String name) {
        CreateRelevanceCommand cmd = new CreateRelevanceCommand();
        cmd.id = id;
        cmd.name = name;
        return restTemplate.postForLocation(urlBase + "/v1/relevances", cmd);
    }

    public TopicResourceIndexDocument getTopicResource(URI id) {
        String url = urlBase + "/v1/topic-resources/" + id;
        return restTemplate.getForObject(url, TopicResourceIndexDocument.class);
    }

    public void updateTopicResource(TopicResourceIndexDocument topicResource) {
        UpdateTopicResourceCommand command = new UpdateTopicResourceCommand();
        command.id = topicResource.id;
        command.primary = topicResource.primary;
        command.rank = topicResource.rank;

        String url = urlBase + "/v1/topic-resources/" + topicResource.id;
        restTemplate.put(url, command);
    }

    public no.ndla.taxonomy.client.topics.ResourceIndexDocument[] getResourcesForTopic(URI topicId) {
        String url = urlBase + "/v1/topics/" + topicId + "/resources";
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.topics.ResourceIndexDocument[].class);
    }

    public no.ndla.taxonomy.client.subjects.TopicIndexDocument[] getTopicsForSubject(URI id) {
        String url = urlBase + "/v1/subjects/" + id + "/topics?recursive=true";
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.subjects.TopicIndexDocument[].class);
    }

    public no.ndla.taxonomy.client.subjects.TopicIndexDocument[] getTopicsForSubject(URI id, Boolean recursive) {
        String url = urlBase + "/v1/subjects/" + id + "/topics?recursive=" + recursive;
        return restTemplate.getForObject(url, no.ndla.taxonomy.client.subjects.TopicIndexDocument[].class);
    }

    public TopicSubtopicIndexDocument getTopicSubtopic(URI connectionId) {
        String url = urlBase + "/v1/topic-subtopics/" + connectionId;
        return restTemplate.getForObject(url, TopicSubtopicIndexDocument.class);
    }

    public SubtopicIndexDocument[] getSubtopicsForTopic(URI id) {
        String url = urlBase + "/v1/topics/" + id + "/topics";
        return restTemplate.getForObject(url, SubtopicIndexDocument[].class);
    }

    public void updateTopicSubtopic(TopicSubtopicIndexDocument topicSubtopic) {
        UpdateTopicSubtopicCommand command = new UpdateTopicSubtopicCommand();
        command.id = topicSubtopic.id;
        command.primary = topicSubtopic.primary;
        command.rank = topicSubtopic.rank;

        String url = urlBase + "/v1/topic-subtopics/" + command.id;
        restTemplate.put(url, command);
    }

    public SubjectTopicIndexDocument getSubjectTopic(URI id) {
        String url = urlBase + "/v1/subject-topics/" + id;
        return restTemplate.getForObject(url, SubjectTopicIndexDocument.class);
    }

    public void updateSubjectTopic(SubjectTopicIndexDocument subjectTopic) {
        UpdateSubjectTopicCommand command = new UpdateSubjectTopicCommand();
        command.id = subjectTopic.id;
        command.primary = subjectTopic.primary;
        command.rank = subjectTopic.rank;

        String url = urlBase + "/v1/subject-topics/" + subjectTopic.id;
        restTemplate.put(url, command);
    }

    public void setNoBatchMode() {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(new HeaderRequestInterceptor("batch", "0"));
        System.out.println("Unsetting batch mode");
        restTemplate.setInterceptors(interceptors);
    }

    public void removeEntity(Entity entity) {
        if ("Topic".equals(entity.type)) {
            restTemplate.delete(urlBase + "/v1/topics/" + entity.getId().toString());
        } else if ("Resource".equals(entity.type)) {
            restTemplate.delete(urlBase + "/v1/resources/" + entity.getId().toString());
        }
    }

    public void addUrlMapping(String oldUrl, URI nodeId, URI oldSubject) {
        OldUrlMapping requestBody = new OldUrlMapping(oldUrl.substring(oldUrl.indexOf("ndla.no")), nodeId, oldSubject);
        restTemplate.exchange(urlBase + "/v1/url/mapping", HttpMethod.PUT, new HttpEntity<>(requestBody), OldUrlMapping.class);
    }



    public static class OldUrlMapping {
        @JsonProperty
        public String url;

        @JsonProperty
        public String nodeId;

        @JsonProperty
        public String subjectId;

        @JsonCreator
        public OldUrlMapping() {
        }

        public OldUrlMapping(String url, URI nodeId, URI subjectId) {
            this.url = url;
            this.nodeId = nodeId.toString();
            this.subjectId = subjectId.toString();
        }
    }

}
