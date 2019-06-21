package no.ndla.taxonomy;

import no.ndla.taxonomy.client.Authentication;
import no.ndla.taxonomy.client.HeaderRequestInterceptor;
import no.ndla.taxonomy.client.TaxonomyRestClient;
import no.ndla.taxonomy.client.TokenUpdateCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static no.ndla.taxonomy.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImportAuthTest {
    @Test
    public void works_with_authentication() {
        (new Runnable() {
            private ClientHttpRequestInterceptor theLambdaInterceptor;

            @Override
            public void run() {
                RestTemplate mockRestTemplate = mock(RestTemplate.class);

                doAnswer(inv -> {
                    List<ClientHttpRequestInterceptor> interceptors = inv.getArgumentAt(0, List.class);
                    theLambdaInterceptor = interceptors.stream()
                            .filter(interc -> !(interc instanceof HeaderRequestInterceptor))
                            .reduce((a, b) -> {throw new RuntimeException("Single interceptor match expected!");})
                            .orElseThrow(() -> new NullPointerException("Expected one single match"));

                    return null;
                }).when(mockRestTemplate).setInterceptors(any(List.class));

                TaxonomyRestClient restClient = new TaxonomyRestClient(BASE_URL, "NOT_ITEST", CLIENT_SECRET, TOKEN_SERVER, mockRestTemplate);

                assertNotNull(theLambdaInterceptor);

                ResponseEntity<Authentication> response = mock(ResponseEntity.class);
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

                try {
                    HttpHeaders headers = new HttpHeaders();
                    HttpRequest request = mock(HttpRequest.class);
                    given(request.getHeaders()).willReturn(headers);
                    ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

                    theLambdaInterceptor.intercept(request, null, execution);

                    assertEquals("Bearer test123", headers.get("Authorization").stream().reduce((a, b) -> {throw new RuntimeException("Single result expected");}).orElse(null));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                assertNotNull(restClient.last_token_update);
            }
        }).run();
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
