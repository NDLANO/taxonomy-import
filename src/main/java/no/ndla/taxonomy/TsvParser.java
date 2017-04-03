package no.ndla.taxonomy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TsvParser implements Iterator<Entity> {

    private String[] lines;
    private int currentLine;
    private Map<String, Integer> columns = new HashMap<>();

    public TsvParser() {
    }

    void init(String[] lines, String subjectName) {
        this.lines = lines;
        columns.clear();
        String[] specification = lines[1].split("\t");
        for (int i = 0; i < specification.length; i++) {
            columns.put(specification[i], i);
        }
        this.currentLine = 0;
    }

    @Override
    public Entity next() {
        String[] columns = lines[++currentLine].split("\t");
        if (lines[currentLine].startsWith("Emne niv")) {
            columns = lines[++currentLine].split("\t");
        }

        Entity result = new Entity();

        getEntityName(columns, result);
        getTranslatedName(columns, result);
        getNodeId(columns, result);

        return result;
    }

    private String getField(String[] line, String columnName) {
        if (!columns.containsKey(columnName)) return null;
        int columnIndex = columns.get(columnName);
        return getField(line, columnIndex);
    }

    private String getField(String[] columns, int column) {
        if (columns.length > column && isNotBlank(columns[column])) {
            return getString(columns[column]);
        }
        return "";
    }

    private static String getString(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean hasNext() {
        return lines.length > currentLine - 1;
    }

    private void getEntityName(String[] columns, Entity result) {
        String topicLevel1 = getField(columns, "Emne nivå 1");
        String topicLevel2 = getField(columns, "Emne nivå 2");
        String topicLevel3 = getField(columns, "Emne nivå 3");
        String resourceName = getField(columns, "Læringsressurs");

        if (!(isNotBlank(topicLevel1) || isNotBlank(topicLevel2) || isNotBlank(topicLevel3) || isNotBlank(resourceName))) {
            throw new MissingParameterException("Entity must be named");
        }


        if (isNotBlank(topicLevel1)) {
            result.type = "Topic";
            result.name = topicLevel1;
        }

        if (isNotBlank(topicLevel2)) {
            result.type = "Topic";
            result.name = topicLevel2;
        }

        if (isNotBlank(topicLevel3)) {
            result.type = "Topic";
            result.name = topicLevel3;
        }

        if (isNotBlank(resourceName)) {
            result.type = "Resource";
            result.name = resourceName;
        }
    }

    private void getNodeId(String[] columns, Entity result) {
        String urlString = getField(columns, "Lenke til gammelt system");
        String[] urlParts = urlString.split("/");
        String parametersString = urlParts[urlParts.length - 1];
        String[] parameters = parametersString.split("\\?");
        result.nodeId = parameters[0];
    }

    private void getTranslatedName(String[] columns, Entity result) {
        String nn = getField(columns, "nn");
        if (isNotBlank(nn)) {
            result.translations.put("nn", new Translation() {{
                name = nn;
            }});
        }
    }
}
