package no.ndla.taxonomy;

import no.ndla.taxonomy.client.SubjectIndexDocument;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static org.junit.Assert.assertEquals;

public class ImporterTest {
    private Importer importer = new Importer();
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
}
