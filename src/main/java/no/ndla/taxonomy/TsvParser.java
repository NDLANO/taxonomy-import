package no.ndla.taxonomy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TsvParser implements Iterator<Entity> {

    private String[] lines;
    private int currentLine;
    private Map<String, Integer> columnMap = new HashMap<>();
    private Entity currentSubject = null;
    private Entity currentLevelOneTopic = null;
    private Entity currentLevelTwoTopic = null;
    private Entity currentLevelThreeTopic = null;

    public TsvParser() {
    }

    void init(String[] lines, Entity subject) {
        this.lines = lines;
        columnMap.clear();
        String[] specification = lines[1].split("\t");
        for (int i = 0; i < specification.length; i++) {
            columnMap.put(specification[i], i);
        }
        this.currentLine = 0;
        this.currentSubject = subject;
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
        getParent(result);

        return result;
    }

    private String getField(String[] line, String columnName) {
        if (!columnMap.containsKey(columnName)) return null;
        int columnIndex = columnMap.get(columnName);
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

    private void getParent(Entity result) {
        if (currentLevelThreeTopic != null && currentLevelThreeTopic != result) {
            result.parent = currentLevelThreeTopic;
        }
        else if (currentLevelTwoTopic != null && currentLevelTwoTopic != result) {
            result.parent = currentLevelTwoTopic;
        }
        else if (currentLevelOneTopic != null && currentLevelOneTopic != result) {
            result.parent = currentLevelOneTopic;
        } else {
            result.parent = currentSubject;
        }
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
            currentLevelOneTopic = result;
            currentLevelTwoTopic = null;
            currentLevelThreeTopic = null;
        }

        if (isNotBlank(topicLevel2)) {
            result.type = "Topic";
            result.name = topicLevel2;
            currentLevelTwoTopic = result;
            currentLevelThreeTopic = null;
        }

        if (isNotBlank(topicLevel3)) {
            result.type = "Topic";
            result.name = topicLevel3;
            currentLevelThreeTopic = result;
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
