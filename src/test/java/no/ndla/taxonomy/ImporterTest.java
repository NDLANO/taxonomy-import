package no.ndla.taxonomy;

import no.ndla.taxonomy.client.*;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static org.junit.Assert.assertEquals;

public class ImporterTest {
    private Importer importer = new Importer(new TaxonomyRestClient());
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void can_add_a_subject() throws Exception {
        Entity entity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:3");
            contentUri = URI.create("urn:article:1");
        }};

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/urn:subject:3", SubjectIndexDocument.class);
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

        SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/urn:subject:4?language=nn", SubjectIndexDocument.class);
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

        SubjectIndexDocument[] subjects = restTemplate.getForObject("http://localhost:5000/subjects", SubjectIndexDocument[].class);
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

        SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/urn:subject:6", SubjectIndexDocument.class);
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
        TopicIndexDocument topic = restTemplate.getForObject("http://localhost:5000/topics/urn:topic:2", TopicIndexDocument.class);
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
        TopicIndexDocument topic = restTemplate.getForObject("http://localhost:5000/topics/urn:topic:2", TopicIndexDocument.class);
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

        TopicIndexDocument subject = restTemplate.getForObject("http://localhost:5000/topics/urn:topic:2", TopicIndexDocument.class);
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
        TopicIndexDocument topic = restTemplate.getForObject("http://localhost:5000/topics/urn:topic:2?language=nn", TopicIndexDocument.class);
        assertEquals("Tal og algebra", topic.name);
    }

    @Test
    public void can_add_topic_with_subject_parent() throws Exception {
        Entity parentEntity = new Entity() {{
            type = "Subject";
            name = "Matematikk";
            id = URI.create("urn:subject:1");
        }};
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
            parent = parentEntity;
        }};

        importer.doImport(topicEntity);
        TopicIndexDocument[] topics = restTemplate.getForObject("http://localhost:5000/subjects/urn:subject:1/topics", TopicIndexDocument[].class);
        assertAnyTrue(topics, t -> t.parent.equals(parentEntity.id));
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

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject("http://localhost:5000/topic-subtopics", TopicSubtopicIndexDocument[].class);
        assertAnyTrue(topicSubtopics, t -> t.topicid.equals(parentEntity.id) && t.subtopicid.equals(topicEntity.id));
    }

    @Test
    public void can_add_resource() throws Exception {
        Entity entity = new Entity() {{
            type = "Resource";
            name = "Tall og algebra fasit YF";
            id = URI.create("urn:resource:4");
        }};

        importer.doImport(entity);

        ResourceIndexDocument result = restTemplate.getForObject("http://localhost:5000/resources/urn:resource:4", ResourceIndexDocument.class);
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

        ResourceIndexDocument result = restTemplate.getForObject("http://localhost:5000/resources/urn:resource:4", ResourceIndexDocument.class);
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

        ResourceIndexDocument result = restTemplate.getForObject("http://localhost:5000/resources/urn:resource:4", ResourceIndexDocument.class);
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

        ResourceIndexDocument[] resources = restTemplate.getForObject("http://localhost:5000/topics/urn:topic:2/resources", ResourceIndexDocument[].class);
        assertAnyTrue(resources, t -> t.id.equals(resourceEntity.id));
    }
}
