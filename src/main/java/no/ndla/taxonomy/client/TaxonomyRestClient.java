package no.ndla.taxonomy.client;

import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TaxonomyRestClient {
    RestTemplate restTemplate = new RestTemplate();

    String urlBase = "http://localhost:5000/";

    public void updateSubject(String id, String name, String contentURI) {
        String urlTypeBase = urlBase + "subjects/";
        restTemplate.getForObject(urlTypeBase + id, SubjectIndexDocument.class);
        UpdateSubjectCommand cmd = new UpdateSubjectCommand();
        cmd.name = name;
        if (isNotBlank(contentURI)) cmd.contentUri = URI.create(contentURI);
        restTemplate.put(urlTypeBase + id, cmd);
    }

    public URI createSubject(String id, String name, String contentURI) {
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

    public void addTranslation(URI uri, String translatedName, String language) {
        UpdateSubjectTranslationCommand cmd = new UpdateSubjectTranslationCommand();
        cmd.name = translatedName;
        restTemplate.put(urlBase + "subjects/" + uri.toString() + "/translations/" + language, cmd);
    }

}
