package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.subjects.TopicIndexDocument;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static no.ndla.taxonomy.TestUtils.assertAnyTrue;
import static no.ndla.taxonomy.TestUtils.baseUrl;

public class ImportSubjectTest {
    private Importer importer = new Importer(new TaxonomyRestClient());
    private RestTemplate restTemplate = new RestTemplate();

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
        TopicIndexDocument[] topics = restTemplate.getForObject(baseUrl + "/subjects/urn:subject:1/topics", TopicIndexDocument[].class);
        assertAnyTrue(topics, t -> t.parent.equals(parentEntity.id));
    }
}
