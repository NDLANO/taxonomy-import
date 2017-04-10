package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.TopicSubtopicIndexDocument;
import no.ndla.taxonomy.client.subjects.SubjectIndexDocument;
import no.ndla.taxonomy.client.topics.TopicIndexDocument;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImporterTest {
    private RestTemplate restTemplate = new RestTemplate();
    private Importer importer = new Importer(new TaxonomyRestClient("http://localhost:5000", restTemplate));

    @Test
    public void can_add_a_subject() throws Exception {
        Entity entity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:3");
            contentUri = URI.create("urn:article:1");
        }};

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:3", SubjectIndexDocument.class);
        assertEquals("Matematikk", subject.name);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }


    @Test
    public void can_add_translation_to_a_subject() throws Exception {
        Entity entity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:4");
            translations.put("nn", new Translation() {{
                name = "Design og handverk";
            }});
        }};

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:4?language=nn", SubjectIndexDocument.class);
        assertEquals("Design og handverk", subject.name);
    }

    @Test
    public void can_add_existing_subject_without_updates() throws Exception {
        Entity entity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:5");
            contentUri = URI.create("urn:article:1");
        }};

        importer.doImport(entity);
        importer.doImport(entity);

        SubjectIndexDocument[] subjects = restTemplate.getForObject(baseUrl + "/v1/subjects", SubjectIndexDocument[].class);
        assertAnyTrue(subjects, s -> s.name.equals("Matematikk"));
        assertAnyTrue(subjects, s -> s.id.toString().equals("urn:subject:3"));
    }

    @Test
    public void can_update_subject() throws Exception {
        Entity entity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:6");
        }};

        importer.doImport(entity);

        entity.contentUri = URI.create("urn:article:1");
        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:6", SubjectIndexDocument.class);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void can_add_topic() throws Exception {
        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
        }};

        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals(topicEntity.id, topic.id);
    }

    @Test
    public void can_add_existing_topic_without_changes() throws Exception {
        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
        }};

        importer.doImport(topicEntity);
        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals(topicEntity.id, topic.id);
    }

    @Test
    public void can_update_topic() throws Exception {
        Entity entity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
        }};

        importer.doImport(entity);

        entity.contentUri = URI.create("urn:article:1");
        importer.doImport(entity);

        TopicIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void can_add_translation_to_a_topic() throws Exception {
        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Tall og algebra";
            id = URI.create("urn:topic:2");
            translations.put("nn", new Translation() {{
                name = "Tal og algebra";
            }});
        }};

        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2?language=nn", TopicIndexDocument.class);
        assertEquals("Tal og algebra", topic.name);
    }

    @Test
    public void can_add_subtopic_with_parent() throws Exception {
        Entity parentEntity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
        }};
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Trigonometri";
            id = URI.create("urn:topic:3");
            parent = parentEntity;
        }};

        importer.doImport(topicEntity);

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject(baseUrl + "/topic-subtopics", TopicSubtopicIndexDocument[].class);
        assertAnyTrue(topicSubtopics, t -> t.topicid.equals(parentEntity.id) && t.subtopicid.equals(topicEntity.id));
    }

    @Test
    public void nodeId_becomes_versioned_id_for_topic() throws Exception {
        Entity entity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            nodeId = "12345";
        }};

        importer.doImport(entity);

        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:1:12345", TopicIndexDocument.class);
        assertEquals(entity.name, topic.name);
    }
}
