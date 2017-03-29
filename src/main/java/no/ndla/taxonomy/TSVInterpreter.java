package no.ndla.taxonomy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TSVInterpreter {

    private String[] lines;
    private int currentLine;
    private Map<String, Integer> columns = new HashMap<>();

    public TSVInterpreter() {
    }

    void init(String[] lines) {
        this.lines = lines;
        columns.clear();
        String[] specification = lines[0].split("\t");
        for (int i = 0; i < specification.length; i++) {
            columns.put(specification[i], i);
        }
        this.currentLine = 0;
    }

    private URI getId(String id, String entityType) {
        return getIdWithPrefix(id, getPrefix(entityType));
    }

    private String getPrefix(String entityType) {
        switch (entityType) {
            case "Subject":
                return "urn:subject";
            case "Topic":
                return "urn:topic";
        }
        return null;
    }

    private URI getIdWithPrefix(String id, String prefix) {
        if (isBlank(id)) return null;

        if (id.startsWith(prefix)) return URI.create(id);
        else return URI.create(prefix + ":" + id);
    }

    private URI getUriField(String[] line, String columnName, boolean required) {
        String field = getField(line, columnName, required);
        if (isBlank(field)) return null;
        return URI.create(field);
    }

    private String getField(String[] line, String columnName, boolean required) {
        if (!columns.containsKey(columnName)) return null;
        int columnIndex = columns.get(columnName);
        return getField(line, columnIndex, required);
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

    public Entity next() {
        String[] columns = lines[++currentLine].split("\t");

        Entity result = new Entity();

        result.type = getField(columns, "EntityType", true);
        result.name = getField(columns, "Name", true);
        result.id = getId(getField(columns, "Id", false), result.type);
        result.contentUri = getUriField(columns, "ContentURI", false);
        String nameNn = getField(columns, "Name-nn", false);
        if (isNotBlank(nameNn)) {
            Translation nn = new Translation();
            nn.name = nameNn;
            result.translations.put("nn", nn);
        }
        return result;

    }
}
