package no.ndla.taxonomy;

import no.ndla.taxonomy.client.subjects.SubjectIndexDocument;
import no.ndla.taxonomy.client.subjects.TopicIndexDocument;
import org.junit.Test;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImportSubjectTest extends ImporterTest {
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
        TopicIndexDocument[] topics = restTemplate.getForObject(baseUrl + "/v1/subjects/urn:subject:1/topics", TopicIndexDocument[].class);
        assertAnyTrue(topics, t -> t.parent.equals(parentEntity.id));
    }
}
