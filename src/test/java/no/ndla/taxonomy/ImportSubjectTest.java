package no.ndla.taxonomy;

import junit.framework.TestCase;
import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.subjects.SubjectIndexDocument;
import no.ndla.taxonomy.client.subjects.TopicIndexDocument;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ImportSubjectTest {
    public static final String HTTP_LOCALHOST_5000 = "http://localhost:5000";

    RestTemplate restTemplate = new RestTemplate();
    Importer importer = new Importer(new TaxonomyRestClient(HTTP_LOCALHOST_5000, clientId, clientSecret, tokenServer, restTemplate));

    @Test
    public void can_add_a_subject() {
        Entity entity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:3"))
                .contentUri(URI.create("urn:article:1"))
                .build();

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:3", SubjectIndexDocument.class);
        assertEquals("Matematikk", subject.name);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }


    @Test
    public void can_add_translation_to_a_subject() {
        Map<String, Translation> translations = new HashMap<>();
        translations.put("nn", new Translation() {{
            name = "Design og handverk";
        }});
        Entity entity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:4"))
                .translations(translations)
                .build();

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:4?language=nn", SubjectIndexDocument.class);
        assertEquals("Design og handverk", subject.name);
    }

    @Test
    public void can_add_existing_subject_without_updates() {
        Entity entity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:5"))
                .contentUri(URI.create("urn:article:1"))
                .build();

        importer.doImport(entity);
        importer.doImport(entity);

        SubjectIndexDocument[] subjects = restTemplate.getForObject(baseUrl + "/v1/subjects", SubjectIndexDocument[].class);
        assertAnyTrue(subjects, s -> s.name.equals("Matematikk"));
        assertAnyTrue(subjects, s -> s.id.toString().equals("urn:subject:3"));
    }

    @Test
    public void can_update_subject() {
        Entity entity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:6"))
                .build();
        importer.doImport(entity);
        entity.contentUri = URI.create("urn:article:1");

        importer.doImport(entity);

        SubjectIndexDocument subject = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:6", SubjectIndexDocument.class);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void can_add_topic_with_subject_parent() {
        Entity parentEntity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:1"))
                .build();
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .parent(parentEntity)
                .build();
        importer.doImport(topicEntity);

        TopicIndexDocument[] topics = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:1/topics", TopicIndexDocument[].class);
        assertAnyTrue(topics, t -> t.parent.equals(parentEntity.getId()));
    }

    @Test
    public void topics_for_subject_can_have_rank() {
        Entity parentEntity = new Entity.Builder()
                .type("Subject")
                .name("Matematikk")
                .id(URI.create("urn:subject:1"))
                .build();
        importer.doImport(parentEntity);

        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .parent(parentEntity)
                .rank(1)
                .build();
        importer.doImport(topicEntity);

        Entity topic2 = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:3"))
                .parent(parentEntity)
                .rank(2)
                .build();
        importer.doImport(topic2);

        TopicIndexDocument[] topics = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:1/topics", TopicIndexDocument[].class);
        assertEquals(topicEntity.getId(), topics[0].id);
        assertEquals(topic2.getId(), topics[1].id);
    }

    @Test
    /*
     Verify that we can get a list of all topics and resources of a given subject.
    */
    public void can_get_list_of_resources_and_topics_for_subject() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:12"))
                .build();
        importer.doImport(subject);

        Entity topic11 = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:T11"))
                .parent(subject)
                .build();
        importer.doImport(topic11);

        Entity resource111 = new Entity.Builder()
                .type("Resource")
                .name("Introduksjon til Geometri")
                .id(URI.create("urn:resource:111"))
                .parent(topic11)
                .build();
        importer.doImport(resource111);

        Entity topic111 = new Entity.Builder()
                .type("Topic")
                .name("Pytagoras")
                .id(URI.create("urn:topic:T111"))
                .parent(topic11)
                .build();
        importer.doImport(topic111);

        Entity resource1111 = new Entity.Builder()
                .type("Resource")
                .name("Pytagoras Setning")
                .id(URI.create("urn:resource:1111"))
                .parent(topic111)
                .build();
        importer.doImport(resource1111);

        Entity resource1112 = new Entity.Builder()
                .type("Resource")
                .name("Ulike Bevis Pytagoras Setning")
                .id(URI.create("urn:resource:1112"))
                .parent(topic111)
                .build();
        importer.doImport(resource1112);

        Entity resource1113 = new Entity.Builder()
                .type("Resource")
                .name("Bruksområder")
                .id(URI.create("urn:resource:1113"))
                .parent(topic111)
                .build();
        importer.doImport(resource1113);

        List<Entity> foundEntities = importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:12"));

        assertEquals(6, foundEntities.size());
        assertTrue(foundEntities.contains(topic111));
        assertTrue(foundEntities.contains(topic11));
        assertTrue(foundEntities.contains(resource111));
        assertTrue(foundEntities.contains(resource1111));
        assertTrue(foundEntities.contains(resource1112));
        assertTrue(foundEntities.contains(resource1113));

        foundEntities.stream().filter(entity -> entity.parent != null).map(entity -> entity.isPrimary).forEach(TestCase::assertTrue);
    }

    @Test
    /*
     Verify that primary topics are traversed into, but secondary topics are not
    */
    public void can_get_list_of_resources_and_topics_for_subject_but_no_secondary_topics() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:13"))
                .build();
        importer.doImport(subject);

        Entity topic11 = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:P11"))
                .parent(subject)
                .build();
        importer.doImport(topic11);

        Entity topic111 = new Entity.Builder()
                .type("Topic")
                .name("Pytagoras")
                .id(URI.create("urn:topic:P111"))
                .parent(topic11)
                .build();
        importer.doImport(topic111);

        Entity topic21 = new Entity.Builder()
                .type("Topic")
                .name("History of Ideas")
                .id(URI.create("urn:topic:P21"))
                .parent(topic11)
                .isPrimary(false)
                .build();
        importer.doImport(topic21);

        Entity topic211 = new Entity.Builder()
                .type("Topic")
                .name("How the Greeks Caught up with Babylonian Mathematics")
                .id(URI.create("urn:topic:P211"))
                .parent(topic21)
                .build();
        importer.doImport(topic211);

        List<Entity> foundEntities = importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:13"));
        assertEquals(2, foundEntities.size());
        assertTrue(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P11"))));
        assertTrue(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P111"))));
        assertFalse(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P21"))));
        assertFalse(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P211"))));

        assertTrue(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P11")) && entity.isPrimary));
        assertTrue(foundEntities.stream().anyMatch(entity -> entity.getId().equals(URI.create("urn:topic:P111")) && entity.isPrimary));
    }

    @Test
     /*
     Verify that only primary resources are listed. Secondary resources shall remain
    */
    public void can_get_list_of_resources_and_topics_for_subject_but_no_secondary_resources() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:14"))
                .build();
        importer.doImport(subject);

        Entity topic11 = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:R11"))
                .parent(subject)
                .build();
        importer.doImport(topic11);

        Entity resource11 = new Entity.Builder()
                .type("Resource")
                .name("Introduction")
                .id(URI.create("urn:resource:R11"))
                .parent(topic11)
                .build();
        importer.doImport(resource11);

        Entity resourceE11 = new Entity.Builder()
                .type("Resource")
                .name("History of geometry")
                .id(URI.create("urn:resource:E11"))
                .parent(topic11)
                .isPrimary(false)
                .build();
        importer.doImport(resourceE11);

        Entity topic111 = new Entity.Builder()
                .type("Topic")
                .name("Pytagoras")
                .id(URI.create("urn:topic:R111"))
                .parent(topic11)
                .build();
        importer.doImport(topic111);

        Entity resource111 = new Entity.Builder()
                .type("Resource")
                .name("Basic proof of pythagoras")
                .id(URI.create("urn:resource:R111"))
                .parent(topic111)
                .build();
        importer.doImport(resource111);

        Entity resourceE111 = new Entity.Builder()
                .type("Resource")
                .name("History of pythagoras")
                .id(URI.create("urn:resource:E111"))
                .parent(topic111)
                .isPrimary(false)
                .build();
        importer.doImport(resourceE111);

        List<Entity> foundEntities = importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:14"));
        assertEquals(4, foundEntities.size());
    }

    @Test
     /*
     Verify that only primary resources are listed. Secondary resources shall remain
    */
    public void can_get_list_of_resources_and_topics_for_subject_but_no_secondary_resources_2() {
        Entity subject = new Entity.Builder()
                .type("Subject")
                .name("Mathematics")
                .id(URI.create("urn:subject:15"))
                .build();
        importer.doImport(subject);

        Entity topic11 = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:m11"))
                .parent(subject)
                .build();
        importer.doImport(topic11);

        Entity topic12 = new Entity.Builder()
                .type("Topic")
                .name("Praksis")
                .id(URI.create("urn:topic:m12"))
                .parent(subject)
                .build();
        importer.doImport(topic12);

        Entity topic111 = new Entity.Builder()
                .type("Topic")
                .name("Pytagoras")
                .id(URI.create("urn:topic:m111"))
                .parent(topic11)
                .build();
        importer.doImport(topic111);
        topic111.parent = topic12;
        importer.doImport(topic111);

        List<Entity> foundEntities = importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:15"));
        assertEquals(3, foundEntities.size());
    }

    @Test
    public void canDeleteList() {
        can_get_list_of_resources_and_topics_for_subject_but_no_secondary_resources();
        List<Entity> foundEntities = importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:15"));
        importer.deleteList(foundEntities);
        assertEquals(0, importer.listResourcesAndTopicsForSubjects(URI.create("urn:subject:15")).size());
    }
}
