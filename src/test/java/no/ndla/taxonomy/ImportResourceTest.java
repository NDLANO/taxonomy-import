package no.ndla.taxonomy;

import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import no.ndla.taxonomy.client.resources.UpdateResourceCommand;
import no.ndla.taxonomy.client.topicResources.TopicResourceIndexDocument;
import org.junit.Test;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImportResourceTest extends ImporterTest {

    @Test
    public void can_add_resource() throws Exception {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:4");
        }};

        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(baseUrl + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.name, result.name);
    }

    @Test
    public void ignores_null() throws Exception {
        importer.doImport(null);
    }

    @Test
    public void can_add_existing_resource_without_changes() throws Exception {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:4");
        }};

        importer.doImport(entity);
        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(baseUrl + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.name, result.name);
    }

    @Test
    public void can_add_resource_in_different_context() throws Exception {
        Entity parentEntity = new Entity() {{
            type = "Topic";
            name = "Tall og algebra";
            id = URI.create("urn:topic:2");
        }};
        importer.doImport(parentEntity);

        Entity entity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:4");
            parent = parentEntity;
        }};
        importer.doImport(entity);

        Entity parentEntity2 = new Entity() {{
            type = "Topic";
            name = "Sannsynlighet";
            id = URI.create("urn:topic:3");
        }};

        entity.parent = parentEntity2;

        importer.doImport(parentEntity2);
        importer.doImport(entity);

        TopicResourceIndexDocument[] topicResources = restTemplate.getForObject(baseUrl + "/v1/topic-resources/", TopicResourceIndexDocument[].class);
        assertTrue(2 <= topicResources.length);
        assertAnyTrue(topicResources, tr -> tr.topicid.equals(parentEntity.id) && tr.resourceid.equals(entity.id));
        assertAnyTrue(topicResources, tr -> tr.topicid.equals(parentEntity2.id) && tr.resourceid.equals(entity.id));

    }

    @Test
    public void can_update_existing_resource() throws Exception {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:4");
        }};

        importer.doImport(entity);
        entity.contentUri = URI.create("urn:article:10");
        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject(baseUrl + "/v1/resources/urn:resource:4", ResourceIndexDocument.class);
        assertEquals(entity.contentUri, result.contentUri);
    }

    @Test
    public void can_add_resource_to_topic() throws Exception {
        Entity parentEntity = new Entity() {{
            type = "Topic";
            name = "Tall og algebra";
            id = URI.create("urn:topic:2");
        }};
        importer.doImport(parentEntity);

        Entity resourceEntity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:5");
            parent = parentEntity;
        }};

        importer.doImport(resourceEntity);

        ResourceIndexDocument[] resources = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2/resources", ResourceIndexDocument[].class);
        assertAnyTrue(resources, t -> t.id.equals(resourceEntity.id));
    }

    @Test
    public void can_set_resource_type() throws Exception {
        Entity resource = new Entity() {{
            type = "Resource";
            name = "Trigonometry explained";
            id = URI.create("urn:resource:1");
            resourceTypes.add(new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial")));
        }};

        importer.doImport(resource);
        ResourceTypeIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Fagstoff", result[0].name);
        assertEquals(URI.create("urn:resourcetype:subjectMaterial"), result[0].id);
    }

    @Test
    public void can_replace_resource_type() throws Exception {
        Entity resource = new Entity() {{
            type = "Resource";
            resourceTypes.add(new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial")));
            id = URI.create("urn:resource:6");
        }};

        importer.doImport(resource);
        resource.resourceTypes.clear();
        resource.resourceTypes.add(new ResourceType("Vedlegg", null, URI.create("urn:resourcetype:attachment")));
        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Vedlegg", result[0].name);
    }

    @Test
    public void can_add_resource_and_sub_resource_type() throws Exception {
        Entity resource = new Entity() {{
            type = "Resource";
            name = "Kildekritikk";
            ResourceType parent = new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial"));
            resourceTypes.add(parent);
            ResourceType child = new ResourceType("Fagartikkel", parent.name, URI.create("urn:resourcetype:article"));
            child.parentName = "Fagstoff";
            child.parentId = parent.id;
            resourceTypes.add(child);
            id = URI.create("urn:resource:11");
        }};

        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/resource-types", ResourceTypeIndexDocument[].class);
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
    public void nodeId_becomes_versioned_id_for_resource() throws Exception {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Sinus og cosinus";
            nodeId = "12345";
        }};

        importer.doImport(entity);

        ResourceIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/resources/urn:resource:1:12345", ResourceIndexDocument.class);
        assertEquals(entity.name, topic.name);
    }

    @Test
    public void can_add_rank_for_topic_resources() throws Exception {
        Entity parentEntity = new Entity() {{
            type = "Topic";
            name = "Tall og algebra";
            id = URI.create("urn:topic:26");
            rank = 2;
        }};
        importer.doImport(parentEntity);

        Entity entity = new Entity() {{
            type = "Resource";
            name = "Sinus og cosinus";
            nodeId = "12345";
            parent = parentEntity;
            rank = 2;
        }};
        importer.doImport(entity);

        Entity resourceEntity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:56");
            parent = parentEntity;
            rank = 1;
        }};
        importer.doImport(resourceEntity);

        ResourceIndexDocument[] resources = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:26/resources", ResourceIndexDocument[].class);
        assertEquals(resourceEntity.name, resources[0].name);
        assertEquals(entity.name, resources[1].name);
    }

    @Test
    public void existing_contentURI_is_not_changed_upon_import() throws Exception {
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

        restTemplate.put(baseUrl + "/v1/resources/urn:resource:1:12345", command);

        importer.doImport(entity);

        ResourceIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/resources/urn:resource:1:12345", ResourceIndexDocument.class);
        assertEquals(command.contentUri, topic.contentUri);
    }
}
