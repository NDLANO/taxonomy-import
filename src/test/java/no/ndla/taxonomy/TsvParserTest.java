package no.ndla.taxonomy;

import no.ndla.taxonomy.client.SubjectIndexDocument;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TsvParserTest {

    TsvParser parser = new TsvParser();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    Entity subject = new Entity() {{
        name = "Matematikk";
        id = URI.create("urn:subject:1");
    }};

    @Test
    public void first_lines_contains_specification() throws Exception {
        String[] lines = new String[]{
                "\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        parser.init(lines, subject);
        Entity entity = parser.next();

        assertEquals("Tall og algebra", entity.name);
        assertEquals("Topic", entity.type);
    }

    @Test
    public void missing_name_not_allowed() throws Exception {
        init("\t\t\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        expectedException.expect(MissingParameterException.class);
        parser.next();
    }

    @Test
    public void can_have_translation() throws Exception {
        init("Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        Entity entity = parser.next();
        assertEquals("Tal og algebra", entity.translations.get("nn").name);
    }

    @Test
    public void can_get_nodeid_from_fagstoff_type_url() throws Exception {
        init( "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("165193", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_node_type_url() throws Exception {
        init("\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("138016", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_quiz_type_url() throws Exception {
        init("\t\t\tTallregning\t\thttp://red.ndla.no/nb/quiz/5960?fag=54\tInteraktivitet\t1T-ST\tTilleggsstoff\t1T-YF\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("5960", entity.nodeId);
    }

    @Test
    public void can_get_nodeid_from_h5pcontent_type_url() throws Exception {
        init("\t\t\tTallregning\t\thttp://red.ndla.no/nb/h5pcontent/125735?fag=54\tInteraktivitet\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();
        assertEquals("125735", entity.nodeId);
    }

    @Test
    public void level_one_topic_has_parent() throws Exception {
        init("Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

        Entity entity = parser.next();

        assertEquals(subject.id, entity.parent.id);
    }

    @Test
    public void resource_can_have_level_one_topic_parent() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Vedlegg	1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity entity = parser.next();

        assertEquals(topic.id, entity.parent.id);
    }

    @Test
    public void resource_can_have_level_two_topic_parent() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Vedlegg	1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity result = parser.next();

        assertEquals(levelTwo.id, result.parent.id);
    }

    @Test
    public void resource_can_have_level_three_topic_parent() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\t\tTall og algebra fasit YF	Tal og algebra fasit YF	http://red.ndla.no/nb/node/138016?fag=54	Vedlegg	1T-YF	Kjernestoff	1T-ST	Tilleggsstoff										"
        };
        init(lines);

        Entity topic = parser.next();
        topic.setId("urn:topic:1");
        Entity levelTwo = parser.next();
        levelTwo.setId("urn:topic:2");
        Entity levelThree = parser.next();
        levelThree.setId("urn:topic:3");
        Entity result = parser.next();

        assertEquals(levelThree.id, result.parent.id);
    }

    @Test
    public void top_level_topic_removes_subtopics_in_parent_hierarchy() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "Geometri\t\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\t\tGeometri fasit YF\tGeometri fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54															"
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

        assertEquals(topic.id, result.parent.id);
    }

    @Test
    public void level_two_topic_removes_subtopics_in_parent_hierarchy() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\tTallmengder\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tPotenser\t\t\t\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\t\tPotenser og rotuttrykk\t\thttp://red.ndla.no/nb/node/138016?fag=54\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
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

        assertEquals(levelTwo.id, result.parent.id);
    }

    private void init(String[] lines) {
        String[] headerLines = new String[]{"\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans"};
                parser.init(ArrayUtils.addAll(headerLines, lines), subject);

    }

    private void init(String line) {
        init(new String[]{line});
    }
}
