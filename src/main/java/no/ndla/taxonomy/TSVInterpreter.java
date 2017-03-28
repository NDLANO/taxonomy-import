package no.ndla.taxonomy;

import org.springframework.web.client.RestTemplate;

import java.net.URI;

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
                uri = createSubject(getString(columns[1]), id, contentURI);
            }
        }

        if (isNotBlank(nn)) {
            addTranslation(uri, nn, "nn");
        }
    }

    private void updateSubject(String id, String name, String contentURI) {
        String urlTypeBase = urlBase + "subjects/";
        restTemplate.getForObject(urlTypeBase + id, Import.SubjectIndexDocument.class);
        Import.UpdateSubjectCommand cmd = new Import.UpdateSubjectCommand();
        cmd.name = name;
        if (isNotBlank(contentURI)) cmd.contentUri = URI.create(contentURI);
        restTemplate.put(urlTypeBase + id, cmd);
    }

    private URI createSubject(String name, String id, String contentURI) {
        Import.CreateSubjectCommand cmd = new Import.CreateSubjectCommand();
        if (null != id) {
            if (!id.contains("urn:")) {
                cmd.id = URI.create("urn:subject:" + id);
            } else {
                cmd.id = URI.create(id);
            }

        }
        cmd.name = name;
        cmd.contentUri = URI.create(contentURI);

        URI uri = restTemplate.postForLocation(urlBase + "subjects", cmd);
        URI subjectid = URI.create(uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
        System.out.println("created: " + subjectid);
        return subjectid;
    }

    private void addTranslation(URI uri, String translatedName, String language) {
        Import.UpdateSubjectTranslationCommand cmd = new Import.UpdateSubjectTranslationCommand();
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
