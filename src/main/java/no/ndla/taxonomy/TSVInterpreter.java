package no.ndla.taxonomy;

import no.ndla.taxonomy.client.CreateSubjectCommand;
import no.ndla.taxonomy.client.SubjectIndexDocument;
import no.ndla.taxonomy.client.UpdateSubjectCommand;
import no.ndla.taxonomy.client.UpdateSubjectTranslationCommand;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class TSVInterpreter {
    private static final String SUBJECT_TYPE = "Subject";

    RestTemplate restTemplate = new RestTemplate();
    String urlBase = "http://localhost:5000/";

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
                updateSubject(id, name, contentURI);

            } catch (Exception e) {
                uri = createSubject(id, name, contentURI);
            }
        }

        if (isNotBlank(nn)) {
            addTranslation(uri, nn, "nn");
        }
    }

    private void updateSubject(String id, String name, String contentURI) {
        String urlTypeBase = urlBase + "subjects/";
        restTemplate.getForObject(urlTypeBase + id, SubjectIndexDocument.class);
        UpdateSubjectCommand cmd = new UpdateSubjectCommand();
        cmd.name = name;
        if (isNotBlank(contentURI)) cmd.contentUri = URI.create(contentURI);
        restTemplate.put(urlTypeBase + id, cmd);
    }

    private URI createSubject(String id, String name, String contentURI) {
        CreateSubjectCommand cmd = new CreateSubjectCommand();
        cmd.id = getUri(id, "urn:subject");
        cmd.name = name;
        cmd.contentUri = URI.create(contentURI);

        URI uri = restTemplate.postForLocation(urlBase + "subjects", cmd);
        URI subjectid = URI.create(uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
        System.out.println("created: " + subjectid);
        return subjectid;
    }

    private URI getUri(String id, String prefix) {
        if (isBlank(id)) return null;

        if (id.startsWith(prefix)) return URI.create(id);
        else return URI.create(prefix + ":" + id);
    }

    private void addTranslation(URI uri, String translatedName, String language) {
        UpdateSubjectTranslationCommand cmd = new UpdateSubjectTranslationCommand();
        cmd.name = translatedName;
        restTemplate.put(urlBase + "subjects/" + uri.toString() + "/translations/" + language, cmd);
    }

    private String getField(String[] columns, int column) {
        return getField(columns, column, false);
    }

    private String getField(String[] columns, int column, boolean required) {
        if (columns.length > column && isNotBlank(columns[column])) {
            return getString(columns[column]);
        }
        if (required) {
            throw new MissingParameterException(column);
        }
        return "";
    }

    private static boolean isNotBlank(String column) {
        return null != column && column.trim().length() > 0;
    }

    private static String getString(String value) {
        return value == null ? "" : value;
    }
}
