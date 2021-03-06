package no.ndla.taxonomy;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.junit.Assert.*;

public class TsvParserTest {

    TsvParser parser;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    Entity subject;

    @Before
    public void setUp() {
        subject = new Entity.Builder()
                .name("Matematikk")
                .type("Subject")
                .id(URI.create("urn:subject:1"))
                .build()
        ;
    }

    @Test
    public void first_lines_contains_specification() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        parser = new TsvParser(lines, subject);
        Entity entity = parser.next();

        assertEquals("Tall og algebra", entity.name);
        assertEquals("Topic", entity.type);
    }

    @Test
    public void specification_must_have_resource_type_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void specification_must_have_subresourcetype_id_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\t\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void specification_must_have_node_id_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\t\tRessurstype\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void specification_must_have_top_level_topic_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\t\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void specification_must_have_level_two_topic_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\t\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void specification_must_have_level_three_topic_field() {
        String[] lines = new String[]{
                "Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\t\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("Specification is missing header: ");
        parser = new TsvParser(lines, subject);
    }

    @Test
    public void missing_name_not_allowed() {
        init("x\t\t\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        expectedException.expect(MissingParameterException.class);
        parser.next();
    }

    @Test
    public void missing_resource_type_not_allowed() {
        init("x\t\t\t\tTittel\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        Entity entity = parser.next();
        assertNull(entity);
    }

    @Test
    public void missing_node_id_not_allowed() {
        init("x\t\t\t\tTittel\t\t\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        Entity entity = parser.next();
        assertNull(entity);
    }

    @Test
    public void missing_node_id_for_topic_raises_error() {
        init("x\tEmnetittel\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        expectedException.expect(MissingParameterException.class);
        expectedException.expectMessage("nodeid");
        parser.next();
    }

    @Test
    public void can_have_translation() {
        init("x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        Entity entity = parser.next();
        assertEquals("Tal og algebra", entity.translations.get("nn").name);
    }

    @Test
    public void can_get_nodeid_from_fagstoff_type_url() {
        init("x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("165193", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_node_type_url() {
        init("x\t\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("138016", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_quiz_type_url() {
        init("x\t\t\t\tTallregning\t\thttp://red.ndla.no/nb/quiz/5960?fag=54\tOppgave\t\t1T-ST\tTilleggsstoff\t1T-YF\tTilleggsstoff\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("5960", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_h5pcontent_type_url() {
        init("x\t\t\t\tTallregning\t\thttp://red.ndla.no/nb/h5pcontent/125735?fag=54\tOppgave\t\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("125735", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_incomplete_url() {
        init("x\t\t\t\tTallregning\t\tred.ndla.no/nb/h5pcontent/125735?fag=54\tOppgave\t\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("125735", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_verktoy_path_url() {
        init("x\t\t\t\tTallregning\t\tverktoy/blikkenslageren\tOppgave\t\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t");
        Entity entity = parser.next();
        assertEquals("verktoy:blikkenslageren", entity.nodeId);
    }

    @Test
    public void level_one_topic_has_parent() {
        init("x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();

        assertEquals(subject.getId(), entity.parent.getId());
    }

    @Test
    public void resource_can_have_level_one_topic_parent() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Fagstoff	\t1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity entity = parser.next();

        assertEquals(topic.getId(), entity.parent.getId());
    }

    @Test
    public void resource_can_have_level_two_topic_parent() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Fagstoff\t	1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity result = parser.next();

        assertEquals(levelTwo.getId(), result.parent.getId());
    }

    @Test
    public void resource_can_have_level_three_topic_parent() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Fagstoff\t	1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity levelThree = parser.next();
        levelThree.setId("urn:topic:3");
        Entity result = parser.next();

        assertEquals(levelThree.getId(), result.parent.getId());
    }

    @Test
    public void top_level_topic_removes_subtopics_in_parent_hierarchy() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\tGeometri\t\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\t\tGeometri fasit YF\tGeometri fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54	Fagstoff														"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity levelThree = parser.next();
        levelThree.setId("urn:topic:3");
        topic = parser.next();
        topic.setId("urn:topic:4");
        Entity result = parser.next();

        assertEquals(topic.getId(), result.parent.getId());
    }

    @Test
    public void level_two_topic_removes_subtopics_in_parent_hierarchy() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tPotenser\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\t\tPotenser og rotuttrykk\t\thttp://red.ndla.no/nb/node/138016?fag=54\tFagstoff\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity levelThree = parser.next();
        levelThree.setId("urn:topic:3");
        levelTwo = parser.next();
        levelTwo.setId("urn:topic:4");
        Entity result = parser.next();

        assertEquals(levelTwo.getId(), result.parent.getId());
    }

    @Test
    public void can_read_resource_type() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\thttp://red.ndla.no/nb/node/138014?fag=54\tFagstoff\t"});
        Entity entity = parser.next();
        assertEquals("Fagstoff", entity.resourceTypes.get(0).name);
    }

    @Test
    public void unknown_resource_type_skips_resource() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\t\tUkjent\t"});
        Entity entity = parser.next();
        assertNull(entity);
    }

    @Test
    public void learning_path_is_top_level_resource_type() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\thttp://red.ndla.no/nb/node/138014?fag=54\tLæringssti\t"});
        Entity entity = parser.next();
        assertEquals("Læringssti", entity.resourceTypes.get(0).name);
        assertEquals(1, entity.resourceTypes.size());
        assertEquals("urn:resourcetype:learningPath", entity.resourceTypes.get(0).id.toString());
    }

    @Test
    public void sub_resource_type_gets_correct_parent_by_inference() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\thttp://red.ndla.no/nb/node/138014?fag=54\tOppgaver og aktiviteter\tArbeidsoppdrag"});
        Entity entity = parser.next();
        assertEquals("Oppgaver og aktiviteter", entity.resourceTypes.get(1).parentName);
    }

    @Test
    public void parent_resource_type_is_listed_first() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\thttp://red.ndla.no/nb/node/138014?fag=54\t\tArbeidsoppdrag"});
        Entity entity = parser.next();
        assertEquals("Oppgaver og aktiviteter", entity.resourceTypes.get(0).name);
        assertEquals("Arbeidsoppdrag", entity.resourceTypes.get(1).name);
        assertEquals(2, entity.resourceTypes.size());
    }

    @Test
    public void sub_resource_type_overrides_resource_type() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"\t\t\tIntroduksjon til algebra\thttp://red.ndla.no/nb/node/138014?fag=54\tFagstoff\tArbeidsoppdrag"});
        Entity entity = parser.next();
        assertEquals("Oppgaver og aktiviteter", entity.resourceTypes.get(0).name);
        assertEquals("Arbeidsoppdrag", entity.resourceTypes.get(1).name);
        assertEquals(2, entity.resourceTypes.size());
    }

    @Test
    public void blank_lines_return_null() {
        init("Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tLenke til gammelt system\tRessurstype\tSubressurstype",
                new String[]{"Resource 1\t\t\t\thttp://red.ndla.no/nb/node/138014?fag=54",
                        "Resource 2\t\t\t\thttp://red.ndla.no/nb/node/136014?fag=54",
                        "\t\t\t",
                        "Resource 3\t\t\t\thttp://red.ndla.no/nb/node/128014?fag=54"});
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        Entity entity4 = parser.next();
        assertEquals("Resource 1", entity1.name);
        assertEquals("Resource 2", entity2.name);
        assertNull(entity3);
        assertEquals("Resource 3", entity4.name);
    }

    @Test
    public void lines_not_scheduled_for_import_return_null() {
        init("Import\tEmne nivå 1	Emne nivå 2	Emne nivå 3	Læringsressurs	Lenke til gammelt system	Ressurstype	Subressurstype",
                new String[]{"x\tTopic 1\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t",
                        "\tTopic 2\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t",
                        "x\tTopic 3\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/125193?fag=161000\t\t\t\t\t\t\t\t\t"});
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        assertNotNull(entity1);
        assertNull(entity2);
        assertNotNull(entity3);
    }

    @Test
    public void can_read_filters() {
        init("Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter\tRelevans\tFilter\tRelevans",
                new String[]{"x\t\t\t\tIntroduksjon til calculus\t\thttp://red.ndla.no/nb/node/138014?fag=54\tFagstoff\t\tVG1\tTilvalgsstoff\tVG2\tKjernestoff\t\t\t\t\t"});
        Entity entity = parser.next();
        assertEquals("Introduksjon til calculus", entity.name);
        assertEquals(2, entity.filters.size());
    }

    @Test
    public void resources_have_rank() {
        String[] lines = {"x\t\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra oppgavesamling YF\tTal og algebra oppgåvesamling YF\thttp://red.ndla.no/nb/node/138014?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n"};
        init(lines);
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();

        assertEquals(1, entity1.rank);
        assertEquals(2, entity2.rank);
        assertEquals(3, entity3.rank);
    }

    @Test
    public void topics_have_rank() {
        String[] lines = {"x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n"};
                init(lines);
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        Entity entity4 = parser.next();
        assertEquals(1, entity1.rank);
        assertEquals(1, entity2.rank);
        assertEquals(2, entity3.rank);
        assertEquals(1, entity4.rank);
    }

    @Test
    public void resources_under_new_topic_restarts_rank_count() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tFagstoff\t\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "x\t\t\t\tTall og tallmengder\t\thttp://red.ndla.no/nb/node/175926?fag=54\tLæringssti\t\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t\t\n"};
        init(lines);
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        Entity entity4 = parser.next();
        Entity entity5 = parser.next();

        assertEquals(1, entity1.rank);
        assertEquals(1, entity2.rank);
        assertEquals(2, entity3.rank);
        assertEquals(1, entity4.rank);
        assertEquals(1, entity5.rank);
    }

    @Test
    public void topics_have_correct_rank() {
        String[] lines = {
                "x\tTall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tPotenser\t\t\t\thttp://red.ndla.no/nb/node/165234?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\tGeometri\t\t\t\tGeometri\thttp://red.ndla.no/nb/node/12345?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tTrigonometri\t\t\t\thttp://red.ndla.no/nb/node/15209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\tFormer\t\t\t\thttp://red.ndla.no/nb/node/16534?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\tFirkanter\t\t\thttp://red.ndla.no/nb/node/16545?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "x\t\t\tSirkler\t\t\thttp://red.ndla.no/nb/node/16546?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };
        init(lines);

        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        Entity entity4 = parser.next();
        Entity entity5 = parser.next();
        Entity entity6 = parser.next();
        Entity entity7 = parser.next();
        Entity entity8 = parser.next();

        assertEquals(1, entity1.rank);
        assertEquals(1, entity2.rank);
        assertEquals(2, entity3.rank);
        assertEquals(2, entity4.rank);
        assertEquals(1, entity5.rank);
        assertEquals(2, entity6.rank);
        assertEquals(1, entity7.rank);
        assertEquals(2, entity8.rank);

    }

    @Test
    public void entity_can_have_secondary_connection_indication() {
        init("Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tSekundærtilknytning\tnn\trevidert?\tPublisert\tKvalitetssikring\tLenke til gammelt system\tRessurstype\tSubressurstype\tFlere ressurstyper? (x)\tHvilke andre ressurstyper?\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevansFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                new String[] {
                        "x\tHelse og sykdom\t\t\t\tx\t\t\tx\t\thttp://red.ndla.no/nb/node/172816?fag=52\t\t\t\t\tYF VG2\tKjernestoff",
                        "x\t\tAllmennstilstand og helsefagarbeiderens rolle ved observasjon\t\t\t\t\t\tx\t\thttp://red.ndla.no/nb/node/173684?fag=52\t\t\t\t\tYF VG2\tKjernestoff"}
        );
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        assertFalse(entity1.isPrimary);
        assertTrue(entity2.isPrimary);
    }

    @Test
    public void can_read_simulation_with_spaces() {
        init("Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\trevidert?\tPublisert\tKvalitetssikring\tLenke til gammelt system\tRessurstype\tSubressurstype\tFlere ressurstyper? (x)\tHvilke andre ressurstyper?\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                new String[] {"x\t\t\t\tNyhetsdrama på E18\t\t\t\thttp://red.ndla.no/node/103442\tOppgaver og aktiviteter\tSimulering \t\t\tMIK 1\tKjernestoff"});
        Entity entity = parser.next();
    }

    private void init(String[] lines) {
        String[] header = new String[]{"Klar for import\tHovedemne\tEmneområde\tEmne\tTittelen på ressursen\tOversettelse (ikke påkrevd)\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Import\tEmne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tSubressurstype\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans"};

        init(header, lines);
    }

    private void init(String header, String[] lines) {
        init(new String[]{header}, lines);
    }

    private void init(String[] header, String[] lines) {
        parser = new TsvParser(ArrayUtils.addAll(header, lines), subject);
    }

    private void init(String line) {
        init(new String[]{line});
    }
}
