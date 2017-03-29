package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;

import java.net.URI;
import java.util.Map;

public class Importer {
    private static final String SUBJECT_TYPE = "Subject";

    private TaxonomyRestClient restClient = new TaxonomyRestClient();


    public void doImport(Entity entity) {
        URI location = null;
        if (entity.type.equals(SUBJECT_TYPE)) {
            try {
                restClient.getSubject(entity.id);
                location = restClient.updateSubject(entity.id, entity.name, entity.contentUri);
            } catch (Exception e) {
                location = restClient.createSubject(entity.id, entity.name, entity.contentUri);
            }
        }

        for (Map.Entry<String, Translation> entry : entity.translations.entrySet()) {
            restClient.addTranslation(location, entry.getKey(), entry.getValue());
        }
    }

}
