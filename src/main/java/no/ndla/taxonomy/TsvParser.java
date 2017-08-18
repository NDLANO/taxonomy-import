package no.ndla.taxonomy;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TsvParser implements Iterator<Entity> {

    public static final String RESSURSTYPE = "Ressurstype";
    public static final String LÆRINGSRESSURS = "Læringsressurs";
    public static final String EMNE = "Emne";
    private StringIterator lines;
    private ColumnMap columnMap;
    private Entity currentSubject;
    private Entity currentLevelOneTopic;
    private Entity currentLevelTwoTopic;
    private Entity currentLevelThreeTopic;

    private Entity result;
    private String[] columns;
    private int currentResourceRank;
    private int currentTopicLevelOneRank;
    private int currentTopicLevelTwoRank;
    private int currentTopicLevelThreeRank;

    public TsvParser(String[] lines, Entity subject) {
        this(new ArrayStringIterator(lines), subject);
    }

    public TsvParser(StringIterator lines, Entity subject) {
        this.lines = lines;
        this.currentSubject = subject;
        this.columnMap = new ColumnMap();
    }


    @Override
    public Entity next() {
        String line = lines.next();
        if (isBlank(line)) return null;

        columns = line.split("\t");

        if (hasField("Import")) {
            String doImport = getField("Import");
            if (isBlank(doImport)) return null;
        }

        result = new Entity();

        setEntityLevelInformation();
        setTranslatedName();
        setNodeId();
        setParent();
        setResourceType();
        setFilters();

        return result;
    }

    private void setFilters() {
        for (int i = 0; i < columnMap.count("Filter"); i++) {
            int columnIndex = columnMap.get("Filter", i);
            String filterName = getField(columnIndex);
            String relevanceName = getField(columnMap.get("Relevans", i));

            if (isBlank(filterName)) continue;

            result.filters.add(new Filter() {{
                name = filterName;
                relevance = new Relevance() {{
                    name = relevanceName;
                }};
            }});
        }
    }

    private boolean hasField(String columnName) {
        return columnMap.containsKey(columnName);
    }

    private String getField(String columnName) {
        if (!hasField(columnName)) return null;
        int columnIndex = columnMap.get(columnName);
        return getField(columnIndex);
    }

    private String getField(int column) {
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
        return lines.hasNext();
    }

    private void setParent() {
        if (currentLevelThreeTopic != null && currentLevelThreeTopic != result) {
            result.parent = currentLevelThreeTopic;
        } else if (currentLevelTwoTopic != null && currentLevelTwoTopic != result) {
            result.parent = currentLevelTwoTopic;
        } else if (currentLevelOneTopic != null && currentLevelOneTopic != result) {
            result.parent = currentLevelOneTopic;
        } else {
            result.parent = currentSubject;
        }
    }

    private void setEntityLevelInformation() {
        String topicLevel1 = getField("Emne nivå 1");
        String topicLevel2 = getField("Emne nivå 2");
        String topicLevel3 = getField("Emne nivå 3");
        String resourceName = getField(LÆRINGSRESSURS);

        if (isNotBlank(topicLevel1)) {
            result.type = Importer.TOPIC_TYPE;
            result.name = topicLevel1;
            currentLevelOneTopic = result;
            currentLevelTwoTopic = null;
            currentLevelThreeTopic = null;
            result.rank = ++currentTopicLevelOneRank;
            currentTopicLevelTwoRank = 0;
            currentTopicLevelThreeRank = 0;
            currentResourceRank = 0;
        } else if (isNotBlank(topicLevel2)) {
            result.type = Importer.TOPIC_TYPE;
            result.name = topicLevel2;
            currentLevelTwoTopic = result;
            currentLevelThreeTopic = null;
            result.rank = ++currentTopicLevelTwoRank;
            currentTopicLevelThreeRank = 0;
            currentResourceRank = 0;
        } else if (isNotBlank(topicLevel3)) {
            result.type = Importer.TOPIC_TYPE;
            result.name = topicLevel3;
            currentLevelThreeTopic = result;
            result.rank = ++currentTopicLevelThreeRank;
            currentResourceRank = 0;
        } else if (isNotBlank(resourceName)) {
            result.type = Importer.RESOURCE_TYPE;
            result.name = resourceName;
            result.rank = ++currentResourceRank;
        } else {
            throw new MissingParameterException("Entity must be named", lines.getLineNumber());
        }
    }

    private void setNodeId() {
        String urlString = getField("Lenke til gammelt system");
        if (isBlank(urlString)) return;

        String[] urlParts = urlString.split("/");
        String parametersString = urlParts[urlParts.length - 1];
        String[] parameters = parametersString.split("\\?");
        result.nodeId = parameters[0];
    }

    private void setTranslatedName() {
        String nn = getField("nn");
        if (isBlank(nn)) return;

        result.translations.put("nn", new Translation() {{
            name = nn;
        }});
    }


    private void setResourceType() {
        String resourceType = getField(RESSURSTYPE);
        if (isBlank(resourceType)) return;

        result.resourceTypes.add(new ResourceType(resourceType));
    }

    public static abstract class StringIterator implements Iterator<String> {
        public abstract int getLineNumber();
    }

    private class ColumnMap {
        Map<String, List<Integer>> entries = new HashMap<>();

        ColumnMap() {
            String line = "";
            while (!(line.contains(EMNE) || line.contains(LÆRINGSRESSURS))) line = lines.next();

            String[] specification = line.split("\t");
            for (int i = 0; i < specification.length; i++) {
                put(specification[i], i);
            }
        }

        private void put(String columnName, int i) {
            columnName = columnName.trim();
            List<Integer> indices = entries.computeIfAbsent(columnName, k -> new ArrayList<>());
            indices.add(i);
        }

        public boolean containsKey(String columnName) {
            return entries.containsKey(columnName);
        }

        public int get(String columnName) {
            return get(columnName, 0);
        }

        public int get(String columnName, int i) {
            return entries.get(columnName).get(i);
        }

        public int count(String columnName) {
            if (!entries.containsKey(columnName)) return 0;
            return entries.get(columnName).size();
        }
    }

    private static class ArrayStringIterator extends StringIterator {
        private int i;
        private final String[] lines;

        public ArrayStringIterator(String[] lines) {
            this.lines = lines;
            i = -1;
        }

        @Override
        public boolean hasNext() {
            return i + 1 <= lines.length - 1;
        }

        @Override
        public String next() {
            return lines[++i];
        }

        @Override
        public int getLineNumber() {
            return i;
        }
    }
}
