package no.ndla.taxonomy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.client.RestTemplate;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static org.junit.Assert.assertEquals;

public class TSVInterpreterTest {

    RestTemplate restTemplate = new RestTemplate();
    TSVInterpreter interpreter = new TSVInterpreter();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void can_add_a_subject() throws Exception {

        String inputLine = "Subject\tMatematikk\t\turn:subject:3\t\turn:article:1\t";
        interpreter.parse(inputLine);

        Import.SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/" + "urn:subject:3", Import.SubjectIndexDocument.class);
        assertEquals("Matematikk", subject.name);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void can_add_translation_to_a_subject() throws Exception {
        String inputLine = "Subject\tDesign og håndverk\t\turn:subject:4\t\turn:article:44\tDesign og handverk";
        interpreter.parse(inputLine);

        Import.SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/urn:subject:4?language=nn", Import.SubjectIndexDocument.class);
        assertEquals("Design og handverk", subject.name);
    }

    @Test
    public void can_add_existing_subject_without_updates() throws Exception {
        String inputLine = "Subject\tMatematikk\t\turn:subject:5\t\turn:article:1\t";
        interpreter.parse(inputLine);

        interpreter.parse(inputLine);

        Import.SubjectIndexDocument[] subjects = restTemplate.getForObject("http://localhost:5000/subjects", Import.SubjectIndexDocument[].class);
        assertAnyTrue(subjects, s -> s.name.equals("Matematikk"));
        assertAnyTrue(subjects, s -> s.id.toString().equals("urn:subject:3"));
    }

    @Test
    public void can_update_subject() throws Exception {
        String inputLine = "Subject\tMatematikk\t\turn:subject:6\t\t\t";
        interpreter.parse(inputLine);
        inputLine = "Subject\tMatematikk\t\turn:subject:6\t\turn:article:1\t";
        interpreter.parse(inputLine);

        Import.SubjectIndexDocument subject = restTemplate.getForObject("http://localhost:5000/subjects/" + "urn:subject:6", Import.SubjectIndexDocument.class);
        assertEquals("urn:article:1", subject.contentUri.toString());
    }

    @Test
    public void missing_entity_type_not_allowed() throws Exception {
        String inputLine = "\t\tMatematikk\t\turn:subject:6";
        expectedException.expect(MissingParameterException.class);
        interpreter.parse(inputLine);
    }

    @Test
    public void missing_name_not_allowed() throws Exception {
        String inputLine = "Subject\t\t\t\turn:subject:6\t\t\t";
        expectedException.expect(MissingParameterException.class);
        interpreter.parse(inputLine);
    }
}
