package no.ndla.taxonomy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TsvParserTest {

    TsvParser parser = new TsvParser();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void first_lines_contains_specification() throws Exception {
        String[] lines = new String[]{
                "\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7",
                "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans",
                "Tall og algebra\t\t\t\tTal og algebra\thttp://red.ndla.no/nb/node/165193?fag=161000\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
        };

        parser.init(lines, "Matematikk");
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


    private void init(String line) {
        String filterHeader = "\t\t\t\t\t\t\tFilter 1\t\tFilter 2\t\tFilter 3\t\tFilter 4\t\tFilter 5\t\tFilter 6\t\tFilter 7";
        String specification = "Emne nivå 1\tEmne nivå 2\tEmne nivå 3\tLæringsressurs\tnn\tLenke til gammelt system\tRessurstype\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans\tFilter \tRelevans";

        parser.init(new String[]{filterHeader, specification, line}, "Matematikk");
    }
}
