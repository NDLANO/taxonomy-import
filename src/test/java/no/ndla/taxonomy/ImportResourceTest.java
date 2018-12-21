package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import no.ndla.taxonomy.client.resources.UpdateResourceCommand;
import no.ndla.taxonomy.client.topicResources.TopicResourceIndexDocument;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.ndla.taxonomy.TestUtils.*;
import static no.ndla.taxonomy.TestUtils.TOKEN_SERVER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImportResourceTest {
    RestTemplate restTemplate = new RestTemplate();
    Importer importer = new Importer(new TaxonomyRestClient(BASE_URL, CLIENT_ID, CLIENT_SECRET, TOKEN_SERVER, restTemplate));

    @Test
    public void can_add_resource() {
        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:4"))
                .build();
        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.name, result.name);
    }

    @Test
    public void ignores_null() {
        importer.doImport(null);
    }

    @Test
    public void can_add_existing_resource_without_changes() {
        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:4"))
                .build();
        importer.doImport(entity);
        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.name, result.name);
    }

    @Test
    public void can_add_resource_in_different_context() {
        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Tall og algebra")
                .id(URI.create("urn:topic:2"))
                .build();
        importer.doImport(parentEntity);

        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:4"))
                .parent(parentEntity)
                .build();
        importer.doImport(entity);

        Entity parentEntity2 = new Entity.Builder()
                .type("Topic")
                .name("Sannsynlighet")
                .id(URI.create("urn:topic:3"))
                .build();
        importer.doImport(parentEntity2);

        entity.parent = parentEntity2;

        importer.doImport(entity);

        TopicResourceIndexDocument[] topicResources = restTemplate.getForObject(BASE_URL + "/v1/topic-resources/", TopicResourceIndexDocument[].class);
        assertTrue(2 <= topicResources.length);
        assertAnyTrue(topicResources, tr -> tr.topicid.equals(parentEntity.getId()) && tr.resourceId.equals(entity.getId()));
        assertAnyTrue(topicResources, tr -> tr.topicid.equals(parentEntity2.getId()) && tr.resourceId.equals(entity.getId()));

    }

    @Test
    public void can_update_existing_resource() {
        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:4"))
                .build();
        importer.doImport(entity);

        entity.contentUri = URI.create("urn:article:10");
        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.contentUri, result.contentUri);
    }

    @Test
    public void can_add_translation_to_a_resource() {
        Map<String, Translation> translations = new HashMap<>();
        translations.put("nn", new Translation() {{
            name = "Tal og algebra";
        }});
        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:4"))
                .translations(translations)
                .build();
        importer.doImport(entity);

        ResourceIndexDocument resource = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:4?language=nn", ResourceIndexDocument.class);
        assertEquals("Tal og algebra", resource.name);
    }

    @Test
    public void can_add_resource_to_topic() {
        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Tall og algebra")
                .id(URI.create("urn:topic:2"))
                .build();
        importer.doImport(parentEntity);

        Entity resourceEntity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:5"))
                .parent(parentEntity)
                .build();
        importer.doImport(resourceEntity);

        ResourceIndexDocument[] resources = restTemplate.getForObject(BASE_URL + "/v1/topics/urn:topic:2/resources", ResourceIndexDocument[].class);
        assertAnyTrue(resources, t -> t.id.equals(resourceEntity.getId()));
    }

    @Test
    public void can_set_resource_type() {
        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial")));
        Entity resource = new Entity.Builder()
                .type("Resource")
                .name("Trigonometry explained")
                .id(URI.create("urn:resource:1"))
                .resourceTypes(resourceTypes)
                .build();
        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(BASE_URL + "/v1/resources/" + resource.getId() + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Fagstoff", result[0].name);
        assertEquals(URI.create("urn:resourcetype:subjectMaterial"), result[0].id);
    }

    @Test
    public void can_replace_resource_type() {
        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial")));
        Entity resource = new Entity.Builder()
                .type("Resource")
                .resourceTypes(resourceTypes)
                .id(URI.create("urn:resource:6"))
                .build();
        importer.doImport(resource);
        resource.resourceTypes.clear();

        resource.resourceTypes.add(new ResourceType("Vedlegg", null, URI.create("urn:resourcetype:attachment")));
        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(BASE_URL + "/v1/resources/" + resource.getId() + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Vedlegg", result[0].name);
    }

    @Test
    public void can_add_resource_and_sub_resource_type() {
        List<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType parent = new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial"));
        ResourceType child = new ResourceType("Fagartikkel", parent.name, URI.create("urn:resourcetype:article"));
        child.parentName = "Fagstoff";
        child.parentId = parent.id;
        resourceTypes.add(parent);
        resourceTypes.add(child);
        Entity resource = new Entity.Builder()
                .type("Resource")
                .resourceTypes(resourceTypes)
                .id(URI.create("urn:resource:11"))
                .name("Kildekritikk")
                .build();
        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(BASE_URL + "/v1/resources/" + resource.getId() + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(2, result.length);
        ResourceTypeIndexDocument first = result[0];
        ResourceTypeIndexDocument second = result[1];
        if (first.name.equals("Fagartikkel")) {
            assertEquals(first.parentId, second.id);
        } else {
            assertEquals(second.parentId, first.id);
        }
    }

    @Test
    public void nodeId_becomes_versioned_id_for_resource() {
        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Sinus og cosinus")
                .nodeId("1234")
                .build();
        importer.doImport(entity);

        ResourceIndexDocument topic = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:1:12345", ResourceIndexDocument.class);
        assertEquals(entity.name, topic.name);
    }

    @Test
    public void can_add_rank_for_topic_resources() {
        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Tall og algebra")
                .id(URI.create("urn:topic:26"))
                .rank(2)
                .build();
        importer.doImport(parentEntity);

        Entity entity = new Entity.Builder()
                .type("Resource")
                .name("Sinus og cosinus")
                .nodeId("12345")
                .parent(parentEntity)
                .rank(2)
                .build();
        importer.doImport(entity);

        Entity resourceEntity = new Entity.Builder()
                .type("Resource")
                .name("Tall og algebra fasit YF")
                .id(URI.create("urn:resource:56"))
                .parent(parentEntity)
                .rank(1)
                .build();
        importer.doImport(resourceEntity);

        ResourceIndexDocument[] resources = restTemplate.getForObject(BASE_URL + "/v1/topics/urn:topic:26/resources", ResourceIndexDocument[].class);
        assertEquals(resourceEntity.name, resources[0].name);
        assertEquals(entity.name, resources[1].name);
    }

    @Test
    public void existing_contentURI_is_not_changed_upon_import() {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Sinus og cosinus";
            nodeId = "12345";
        }};

        importer.doImport(entity);

        UpdateResourceCommand command = new UpdateResourceCommand() {{
            name = "Sinus og cosinus";
            contentUri = URI.create("urn:article:1");
        }};

        restTemplate.put(BASE_URL + "/v1/resources/urn:resource:1:12345", command);

        importer.doImport(entity);

        ResourceIndexDocument topic = restTemplate.getForObject(BASE_URL + "/v1/resources/urn:resource:1:12345", ResourceIndexDocument.class);
        assertEquals(command.contentUri, topic.contentUri);
    }


    @Test
    public void can_set_primary_explicitely() {
        Entity topic1 = new Entity.Builder()
                .type("Topic")
                .name("Mathematics")
                .id(URI.create("urn:topic:111"))
                .build();
        importer.doImport(topic1);

        Entity resource = new Entity.Builder()
                .type("Resource")
                .name("Geometri")
                .id(URI.create("urn:resource:112"))
                .parent(topic1)
                .build();
        importer.doImport(resource);

        Entity topic2 = new Entity.Builder()
                .type("Topic")
                .name("Shapes")
                .id(URI.create("urn:topic:112"))
                .build();
        importer.doImport(topic2);

        resource.isPrimary = true;
        resource.parent = topic2;
        importer.doImport(resource);

        TopicResourceIndexDocument[] topicResources = restTemplate.getForObject(BASE_URL + "/v1/topic-resources", TopicResourceIndexDocument[].class);
        assertAnyTrue(topicResources, tr -> tr.topicid.equals(topic2.getId()) && tr.resourceId.equals(resource.getId()) && tr.primary);
    }
}
