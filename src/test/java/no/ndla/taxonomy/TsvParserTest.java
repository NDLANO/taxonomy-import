package no.ndla.taxonomy;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TsvParserTest {

    TsvParser parser;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    Entity subject;

    @Before
    public void setUp() throws Exception {
        subject = new Entity() {{
            name = "Matematikk";
            id = URI.create("urn:subject:1");
        }};
    }

    @Test
    public void first_lines_contains_specification() throws Exception {
        String[] lines = new String[]{
                "\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        parser = new TsvParser(lines, subject);
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
        init("Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");

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


    @Test
    public void can_read_resource_type() throws Exception {
        init("Læringsressurs\tRessurstype", new String[]{"Introduksjon til algebra\tArtikkel"});
        Entity entity = parser.next();
        assertEquals("Artikkel", entity.resourceTypes.get(0).name);
    }

    @Test
    public void blank_lines_return_null() throws Exception {
        init("Læringsressurs", new String[]{"Resource 1", "Resource 2", "\t\t\t", "Resource 3"});
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
    public void lines_not_scheduled_for_import_return_null() throws Exception {
        init("Import\tLæringsressurs", new String[]{"x\tResource 1", "\tResource 2", "x\tResource 3"});
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        assertNotNull(entity1);
        assertNull(entity2);
        assertNotNull(entity3);
    }

    @Test
    public void can_read_filters() throws Exception {
        init("Læringsressurs\tFilter\tRelevans\tFilter\tRelevans", new String[]{"Introduksjon til calculus\tVG1\tTilvalgsstoff\tVG2\tKjernestoff"});
        Entity entity = parser.next();
        assertEquals("Introduksjon til calculus", entity.name);
        assertEquals(2, entity.filters.size());
    }

    @Test
    public void resources_have_rank() throws Exception {
        String[] lines = {"\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra oppgavesamling YF\tTal og algebra oppgåvesamling YF\thttp://red.ndla.no/nb/node/138014?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n"};
        init(lines);
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();

        assertEquals(1, entity1.rank);
        assertEquals(2, entity2.rank);
        assertEquals(3, entity3.rank);
    }

    @Test
    public void topics_have_rank() throws Exception {
        String[] lines = {"Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n"};
                init(lines);
        Entity entity1 = parser.next();
        Entity entity2 = parser.next();
        Entity entity3 = parser.next();
        assertEquals(1, entity1.rank);
        assertEquals(1, entity2.rank);
        assertEquals(2, entity3.rank);
    }

    @Test
    public void resources_under_new_topic_restarts_rank_count() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra fasit YF\tTal og algebra fasit YF\thttp://red.ndla.no/nb/node/138016?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og algebra løsningsforslag YF\tTal og algebra løysingsforslag YF\thttp://red.ndla.no/nb/node/138015?fag=54\tVedlegg\t1T-YF\tKjernestoff\t1T-ST\tTilleggsstoff\t\t\t\t\t\t\t\t\t\t\n",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                "\t\t\tTall og tallmengder\t\thttp://red.ndla.no/nb/node/175926?fag=54\tLæringssti\t1T-ST\tKjernestoff\t1T-YF\tKjernestoff\t\t\t\t\t\t\t\t\t\t\n"};
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
    public void topics_have_correct_rank() throws Exception {
        String[] lines = {
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTallregning\t\t\t\thttp://red.ndla.no/nb/node/165209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tPotenser\t\t\t\thttp://red.ndla.no/nb/node/165234?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "Geometri\t\t\t\tGeometri\thttp://red.ndla.no/nb/node/12345?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tTrigonometri\t\t\t\thttp://red.ndla.no/nb/node/15209?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\tFormer\t\t\t\thttp://red.ndla.no/nb/node/16534?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\tFirkanter\t\t\thttp://red.ndla.no/nb/node/16545?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                "\t\tSirkler\t\t\thttp://red.ndla.no/nb/node/16546?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
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

    private void init(String[] lines) {
        String[] header = new String[]{"\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans\tFilter\tRelevans"};

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
