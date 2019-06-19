package no.ndla.taxonomy.client;

import no.ndla.taxonomy.client.resources.ResourceTypeIndexDocument;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class TaxonomyRestClientTest {
    MockRestServiceServer mockServer;
    private TaxonomyRestClient taxonomyRestClient;

    @Before
    public void beforeTesting() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        taxonomyRestClient = new TaxonomyRestClient("http://localhost", "ITEST", "", "", restTemplate);
    }

    @Test
    public void testAddTopicResourceType() throws URISyntaxException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(new URI("/topic-resourcetypes/urn:topic-resourcetype:1"));
        mockServer
                .expect(ExpectedCount.once(), requestTo("http://localhost/v1/topic-resourcetypes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string("{\"topicId\":\"urn:topic:1\",\"resourceTypeId\":\"urn:resourcetype:1\"}"))
                .andRespond(withStatus(HttpStatus.CREATED).headers(responseHeaders));

        assertEquals(new URI("/topic-resourcetypes/urn:topic-resourcetype:1"), taxonomyRestClient.addTopicResourceType(new URI("urn:topic:1"), new URI("urn:resourcetype:1")));

        mockServer.verify();
    }

    @Test
    public void testGetResourceTypesForTopic() throws URISyntaxException {
        String json = "{" +
                "\"id\": \"urn:resourcetype:1\"," +
                "\"name\": \"Test\"," +
                "\"parentId\": null," +
                "\"connectionId\": \"urn:topic-resourcetype:1\"" +
                "}";
        mockServer
                .expect(ExpectedCount.once(), requestTo("http://localhost/v1/topics/urn:topic:1/resource-types"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("[" + json + "]"));

        ResourceTypeIndexDocument[] resourceTypes = taxonomyRestClient.getResourceTypesForTopic(new URI("urn:topic:1"));
        assertEquals(1, resourceTypes.length);
        ResourceTypeIndexDocument resourceType = resourceTypes[0];
        assertEquals(new URI("urn:resourcetype:1"), resourceType.id);
        assertEquals("Test", resourceType.name);
        assertNull(resourceType.parentId);
        assertEquals(new URI("urn:topic-resourcetype:1"), resourceType.connectionId);

        mockServer.verify();
    }

    @Test
    public void testDeleteResourceTypeForTopic() throws URISyntaxException {
        mockServer
                .expect(ExpectedCount.once(), requestTo("http://localhost/v1/topic-resourcetypes/urn:topic-resourcetype:1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK));

        taxonomyRestClient.removeTopicResourceType(new URI("urn:topic-resourcetype:1"));

        mockServer.verify();
    }
}
