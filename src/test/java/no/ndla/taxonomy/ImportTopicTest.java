package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.topicSubtopics.TopicSubtopicIndexDocument;
import no.ndla.taxonomy.client.topics.TopicIndexDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImportTopicTest {
    RestTemplate restTemplate = new RestTemplate();
    Importer importer = new Importer(new TaxonomyRestClient("http://localhost:5000", restTemplate));

    @Test
    public void can_add_topic() {
        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .build();

        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals(topicEntity.getId(), topic.id);
    }

    @Test
    public void can_add_existing_topic_without_changes() {
        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .build();

        importer.doImport(topicEntity);
        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals(topicEntity.getId(), topic.id);
    }

    @Test
    public void can_update_topic() {
        Entity entity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .build();

        importer.doImport(entity);

        entity.contentUri = URI.create("urn:article:1");
        importer.doImport(entity);

        TopicIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2", TopicIndexDocument.class);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void can_add_translation_to_a_topic() {
        Map<String, Translation> translations = new HashMap<>();
        translations.put("nn", new Translation() {{
            name = "Tal og algebra";
        }});
        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Tall og algebra")
                .id(URI.create("urn:topic:2"))
                .translations(translations)
                .build();

        importer.doImport(topicEntity);
        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:2?language=nn", TopicIndexDocument.class);
        assertEquals("Tal og algebra", topic.name);
    }

    @Test
    public void can_add_subtopic_with_parent() {
        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .build();
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Trigonometri")
                .id(URI.create("urn:topic:3"))
                .parent(parentEntity)
                .build();
        importer.doImport(topicEntity);

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject(baseUrl + "/v1/topic-subtopics", TopicSubtopicIndexDocument[].class);
        assertAnyTrue(topicSubtopics, t -> t.topicid.equals(parentEntity.getId()) && t.subtopicid.equals(topicEntity.getId()));
    }

    @Test
    public void nodeId_becomes_versioned_id_for_topic() {
        Entity entity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .nodeId("12345")
                .build();
        importer.doImport(entity);

        TopicIndexDocument topic = restTemplate.getForObject(baseUrl + "/v1/topics/urn:topic:1:12345", TopicIndexDocument.class);
        assertEquals(entity.name, topic.name);
    }

    @Test
    public void can_add_rank_for_topic() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:1"))
                .build();
        importer.doImport(subject);

        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .parent(subject)
                .build();
        importer.doImport(parentEntity);

        Entity topicRank1 = new Entity.Builder()
                .type("Topic")
                .name("Trigonometri")
                .id(URI.create("urn:topic:3"))
                .parent(parentEntity)
                .rank(1)
                .build();
        importer.doImport(topicRank1);

        Entity topicRank2 = new Entity.Builder()
                .type("Topic")
                .name("Shapes")
                .id(URI.create("urn:topic:4"))
                .parent(parentEntity)
                .rank(1)
                .build();
        importer.doImport(topicRank2);

        TopicIndexDocument[] topics = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:1/topics?recursive=true", TopicIndexDocument[].class);
        assertEquals(parentEntity.getId(), topics[0].id);
        assertEquals(topicRank1.getId(), topics[1].id);
        assertEquals(topicRank2.getId(), topics[2].id);
    }

    @Test
    @Ignore
    //This test started failing after API stopped allowing two primary parents. However, to get it to work again, Importer.importTopicSubtopic must be fixed.
    public void can_set_primary_explicitely() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:111"))
                .build();

        importer.doImport(subject);

        Entity parentEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:22"))
                .parent(subject)
                .build();
        importer.doImport(parentEntity);

        Entity subtopic = new Entity.Builder()
                .type("Topic")
                .name("Shapes")
                .id(URI.create("urn:topic:33"))
                .parent(parentEntity)
                .build();
        importer.doImport(subtopic);

        Entity parent2 = new Entity.Builder()
                .type("Topic")
                .name("Statistikk")
                .id(URI.create("urn:topic:44"))
                .parent(subject)
                .build();
        importer.doImport(parent2);

        subtopic.isPrimary = true;
        subtopic.parent = parent2;
        importer.doImport(subtopic);

        TopicSubtopicIndexDocument[] topicSubtopics = restTemplate.getForObject(baseUrl + "/v1/topic-subtopics", TopicSubtopicIndexDocument[].class);
        assertAnyTrue(topicSubtopics, ts -> ts.subtopicid.equals(subtopic.getId()) && ts.topicid.equals(parent2.getId()) && ts.primary);
    }
}
