package no.ndla.taxonomy;

import no.ndla.taxonomy.client.Authentication;
import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.TokenUpdateCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import java.time.Instant;

import static no.ndla.taxonomy.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportAuthTest {


    @Mock
    RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);

    @InjectMocks
    TaxonomyRestClient restClient = new TaxonomyRestClient(BASE_URL, "NOT_ITEST", CLIENT_SECRET, TOKEN_SERVER, mockRestTemplate);

    @Test
    public void works_with_authentication() {

        ResponseEntity<Authentication> response = Mockito.mock(ResponseEntity.class);
        Authentication authentication = new Authentication();
        authentication.expires_in = 1800l;
        authentication.access_token = "test123";
        authentication.scope = "test";
        authentication.token_type = "test";

        when(response.getBody()).thenReturn(authentication);
        when(mockRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Authentication.class))
        ).thenReturn(response);

        assertNull(restClient.last_token_update);

        restClient.updateHeaders();

        assertNotNull(restClient.last_token_update);

    }

    @Test
    public void expired_updates_authentication() {
        Authentication authentication = new Authentication();
        authentication.expires_in = 1800l;
        authentication.access_token = "test123";
        authentication.scope = "test";
        authentication.token_type = "test";

        Long token_is_expired = Instant.now().minusSeconds(1800).toEpochMilli();
        Long token_is_about_to_expired = Instant.now().minusSeconds(1500).toEpochMilli();
        Long token_is_valid = Instant.now().minusSeconds(1498).toEpochMilli();

        assertTrue(TokenUpdateCheck.shouldUpdateToken(token_is_expired, authentication));
        assertTrue(TokenUpdateCheck.shouldUpdateToken(token_is_about_to_expired, authentication));
        assertFalse(TokenUpdateCheck.shouldUpdateToken(token_is_valid, authentication));


    }

}
