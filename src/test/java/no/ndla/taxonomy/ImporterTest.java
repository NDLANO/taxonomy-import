package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;
import org.springframework.web.client.RestTemplate;

public class ImporterTest {
    RestTemplate restTemplate = new RestTemplate();
    Importer importer = new Importer(new TaxonomyRestClient("http://localhost:5000", restTemplate));
}
