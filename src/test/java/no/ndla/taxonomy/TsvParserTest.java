package no.ndla.taxonomy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TsvParserTest {

    TsvParser interpreter = new TsvParser();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void first_line_contains_specification() throws Exception {
        String[] lines = new String[]{
                "EntityType\tName\tId\tContentUri\tName-nn",
                "Topic\tProbability\turn:topic:1\turn:article:3\tSannsyn"
        };

        interpreter.init(lines);
        Entity entity = interpreter.next();

        assertEquals("Topic", entity.type);
        assertEquals("Probability", entity.name);
        assertEquals("urn:topic:1", entity.id.toString());
        assertEquals("Sannsyn", entity.translations.get("nn").name);
    }

    @Test
    public void can_determine_id_prefix_for_topic() throws Exception {
        String[] lines = new String[]{
                "EntityType\tName\tId",
                "Topic\tProbability\t1"
        };

        interpreter.init(lines);
        Entity entity = interpreter.next();

        assertEquals("urn:topic:1", entity.id.toString());
    }

    @Test
    public void can_determine_id_prefix_for_subject() throws Exception {
        String[] lines = new String[]{
                "EntityType\tName\tId",
                "Subject\tMathematics\t1"
        };

        interpreter.init(lines);
        Entity entity = interpreter.next();

        assertEquals("urn:subject:1", entity.id.toString());
    }

    @Test
    public void missing_entity_type_not_allowed() throws Exception {
        init("\t\tMatematikk\t\turn:subject:6");
        expectedException.expect(MissingParameterException.class);
        interpreter.next();
    }

    @Test
    public void missing_name_not_allowed() throws Exception {
        init("Subject\t\t\t\turn:subject:6\t\t\t");
        expectedException.expect(MissingParameterException.class);
        interpreter.next();
    }

    private void init(String line) {
        String defaultSpecification = "EntityType\tName\tResourceType\tId\tParentId\tContentUri\tnn";
        interpreter.init(new String[]{defaultSpecification, line});
    }
}
