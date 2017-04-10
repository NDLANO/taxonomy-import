package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.resources.ResourceIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImportResourceTest {
    private RestTemplate restTemplate = new RestTemplate();
    private Importer importer = new Importer(new TaxonomyRestClient("http://localhost:5000", restTemplate));
    
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
            id = URI.create("urn:topic:4");
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
            resourceTypes.add(new Entity.ResourceType("Fagstoff"));
        }};

        importer.doImport(resource);
        ResourceTypeIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Fagstoff", result[0].name);
    }

    @Test
    @Ignore
    public void can_replace_resource_type() throws Exception {
        Entity resource = new Entity() {{
            type = "Resource";
            resourceTypes.add(new Entity.ResourceType("Fagstoff"));
        }};

        importer.doImport(resource);
        resource.resourceTypes.clear();
        resource.resourceTypes.add(new Entity.ResourceType("Vedlegg"));
        importer.doImport(resource);

        ResourceTypeIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/resource-types", ResourceTypeIndexDocument[].class);
        assertEquals(1, result.length);
        assertEquals("Vedlegg", result[0].name);
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
}
