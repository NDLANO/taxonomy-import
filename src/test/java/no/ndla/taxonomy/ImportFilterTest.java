package no.ndla.taxonomy;

import no.ndla.taxonomy.client.relevances.RelevanceIndexDocument;
import no.ndla.taxonomy.client.resources.FilterIndexDocument;
import org.junit.Test;

import java.net.URI;

import static no.ndla.taxonomy.Importer.*;
import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;
import static org.junit.Assert.assertEquals;

public class ImportFilterTest extends ImporterTest {

    @Test
    public void can_import_filter() throws Exception {
        Entity subject = new Entity() {{
            type = SUBJECT_TYPE;
            name = "Matematikk";
            id = URI.create("urn:subject:10");
        }};

        Entity topic = new Entity() {{
            type = TOPIC_TYPE;
            name = "Algebra";
            id = URI.create("urn:topic:10");
            parent = subject;
        }};

        Entity resource = new Entity() {{
            type = RESOURCE_TYPE;
            name = "Introduksjon til algebra";
            id = URI.create("urn:resource:10");
            parent = topic;

            filters.add(new Filter() {{
                name = "VG1";
                relevance = new Relevance() {{
                    name = Importer.TILLEGGSSSTOFF;
                }};
            }});

            filters.add(new Filter() {{
                name = "VG2";
                relevance = new Relevance() {{
                    name = Importer.KJERNESTOFF;
                }};
            }});
        }};

        importer.doImport(subject);
        importer.doImport(topic);
        importer.doImport(resource);

        //resources should have filters
        FilterIndexDocument[] result = restTemplate.getForObject(baseUrl + "/v1/resources/" + resource.id + "/filters", FilterIndexDocument[].class);
        assertEquals(2, result.length);
        assertAnyTrue(result, r -> "VG1".equals(r.name) && "urn:relevance:supplementary".equals(r.relevanceId.toString()));
        assertAnyTrue(result, r -> "VG2".equals(r.name) && "urn:relevance:core".equals(r.relevanceId.toString()));

        //relevances should be created
        RelevanceIndexDocument[] relevances = restTemplate.getForObject(baseUrl + "/v1/relevances", RelevanceIndexDocument[].class);
        assertEquals(2, relevances.length);
        assertAnyTrue(relevances, r -> "urn:relevance:core".equals(r.id.toString()) && "Kjernestoff".equals(r.name));
        assertAnyTrue(relevances, r -> "urn:relevance:supplementary".equals(r.id.toString()) && "Tilleggsstoff".equals(r.name));

        //filters should be added to subject
        no.ndla.taxonomy.client.subjects.FilterIndexDocument[] subjectFilters = restTemplate.getForObject(baseUrl + "/v1/subjects/" + subject.id + "/filters", no.ndla.taxonomy.client.subjects.FilterIndexDocument[].class);
        assertEquals(2, subjectFilters.length);
        assertAnyTrue(subjectFilters, f -> "VG1".equals(f.name));
        assertAnyTrue(subjectFilters, f -> "VG2".equals(f.name));
    }
}
