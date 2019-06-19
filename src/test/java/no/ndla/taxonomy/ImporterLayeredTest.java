package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.resources.FilterIndexDocument;
import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static no.ndla.taxonomy.Importer.TOPIC_TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ImporterLayeredTest {
    private TaxonomyRestClient taxonomyRestClient;
    private Importer importer;

    @Before
    public void beforeTesting() {
        taxonomyRestClient = mock(TaxonomyRestClient.class);
        importer = new Importer(taxonomyRestClient);
    }

    @Test
    public void testImportTopicWithoutResourceTypes() throws URISyntaxException {
        given(taxonomyRestClient.updateEntity(new URI("urn:topic:2"), "Geometri", null, TOPIC_TYPE))
                .willReturn(new URI("/topics/urn:topic:2"));
        given(taxonomyRestClient.getFiltersForTopic(new URI("urn:topic:2")))
                .willReturn(new FilterIndexDocument[0]);
        given(taxonomyRestClient.getResourceTypesForTopic(new URI("urn:topic:2")))
                .willReturn(new ResourceTypeIndexDocument[0]);

        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .build();
        importer.doImport(topicEntity);

        verify(taxonomyRestClient, times(1))
                .getTopic(new URI("urn:topic:2")); // The check existing call
        verify(taxonomyRestClient, times(1))
                .updateEntity(new URI("urn:topic:2"), "Geometri", null, TOPIC_TYPE);
        verify(taxonomyRestClient, times(1))
                .getFiltersForTopic(new URI("urn:topic:2"));
        verify(taxonomyRestClient, times(1))
                .getResourceTypesForTopic(new URI("urn:topic:2"));
    }

    @Test
    public void testImportTopicWithResourceTypes() throws URISyntaxException {
        given(taxonomyRestClient.updateEntity(new URI("urn:topic:2"), "Geometri", null, TOPIC_TYPE))
                .willReturn(new URI("/topics/urn:topic:2"));
        given(taxonomyRestClient.getFiltersForTopic(new URI("urn:topic:2")))
                .willReturn(new FilterIndexDocument[0]);
        given(taxonomyRestClient.getResourceTypesForTopic(new URI("urn:topic:2")))
                .willReturn(new ResourceTypeIndexDocument[0]);

        no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument resourceTypeIndexDocument
                = new no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument();
        resourceTypeIndexDocument.id = new URI("urn:resourcetype:subjectMaterial");
        resourceTypeIndexDocument.name = "Fagstoff";
        resourceTypeIndexDocument.subtypes = new ArrayList<>();
        given(taxonomyRestClient.getResourceTypes())
                .willReturn(new no.ndla.taxonomy.client.resourceTypes.ResourceTypeIndexDocument[]{resourceTypeIndexDocument});

        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(new ResourceType("Fagstoff", null, URI.create("urn:resourcetype:subjectMaterial")));
        Entity topicEntity = new Entity.Builder()
                .type("Topic")
                .name("Geometri")
                .id(URI.create("urn:topic:2"))
                .resourceTypes(resourceTypes)
                .build();
        importer.doImport(topicEntity);

        verify(taxonomyRestClient, times(1))
                .getTopic(new URI("urn:topic:2")); // The check existing call
        verify(taxonomyRestClient, times(1))
                .updateEntity(new URI("urn:topic:2"), "Geometri", null, TOPIC_TYPE);
        verify(taxonomyRestClient, times(1))
                .getFiltersForTopic(new URI("urn:topic:2"));
        verify(taxonomyRestClient, times(1))
                .getResourceTypesForTopic(new URI("urn:topic:2"));
        verify(taxonomyRestClient, times(1))
                .addTopicResourceType(new URI("urn:topic:2"), new URI("urn:resourcetype:subjectMaterial"));
    }
}
