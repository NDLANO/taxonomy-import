package no.ndla.taxonomy;

import no.ndla.taxonomy.client.topicSubtopics.TopicSubtopicIndexDocument;
import no.ndla.taxonomy.client.topics.TopicIndexDocument;
import org.junit.Test;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImportTopicTest extends ImporterTest {
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

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject(baseUrl + "/v1/topic-subtopics", TopicSubtopicIndexDocument[].class);
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

    @Test
    public void can_add_rank_for_topic() throws Exception {
        Entity subject = new Entity() {{
           type = "Subject";
           name = "Mathematics";
           id = URI.create("urn:subject:1");
        }};

        importer.doImport(subject);

        Entity parentEntity = new Entity() {{
            type = "Topic";
            name = "Geometri";
            id = URI.create("urn:topic:2");
            parent = subject;
        }};
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity() {{
            type = "Topic";
            name = "Trigonometri";
            id = URI.create("urn:topic:3");
            parent = parentEntity;
            rank = 1;
        }};
        importer.doImport(topicEntity);

        Entity subtopic = new Entity() {{
            type = "Topic";
            name = "Shapes";
            id = URI.create("urn:topic:4");
            parent = parentEntity;
            rank = 2;
        }};

        importer.doImport(subtopic);

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:1/topics?recursive=true", TopicSubtopicIndexDocument[].class);
        assertEquals(parentEntity.id, topicSubtopics[0].id);
        assertEquals(topicEntity.id, topicSubtopics[1].id);
        assertEquals(subtopic.id, topicSubtopics[2].id);
    }
}
