package no.ndla.taxonomy;

import no.ndla.taxonomy.client.TaxonomyRestClient;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TSVInterpreter {
    private static final String SUBJECT_TYPE = "Subject";

    private TaxonomyRestClient restClient = new TaxonomyRestClient();

    public TSVInterpreter() {
    }

    void parse(String line) {
        String[] columns = line.split("\t");

        String entityType = getField(columns, 0, true);
        String name = getField(columns, 1, true);
        String id = getField(columns, 3);
        String contentURI = getField(columns, 5);
        String nn = getField(columns, 6);

        URI uri = URI.create(id);

        if (entityType.equals(SUBJECT_TYPE)) {
            try {
                restClient.updateSubject(id, name, contentURI);
            } catch (Exception e) {
                uri = restClient.createSubject(id, name, contentURI);
            }
        }

        if (isNotBlank(nn)) {
            restClient.addTranslation(uri, nn, "nn");
        }
    }

    private String getField(String[] columns, int column) {
        return getField(columns, column, false);
    }

    private String getField(String[] columns, int column, boolean required) {
        if (columns.length > column && isNotBlank(columns[column])) {
            return getString(columns[column]);
        }
        if (required) throw new MissingParameterException(column);
        return "";
    }

    private static String getString(String value) {
        return value == null ? "" : value;
    }
}
