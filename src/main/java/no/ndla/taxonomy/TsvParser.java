package no.ndla.taxonomy;

import java.net.URI;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TsvParser implements Iterator<Entity> {

    public static final String RESOURCE_TYPE = "Ressurstype";
    public static final String SUB_RESOURCE_TYPE = "Subressurstype";
    public static final String LEARNING_RESOURCE = "Læringsressurs";
    public static final String EMNE = "Emne";
    private static final String NODE_ID_FIELD = "Lenke til gammelt system";
    private static final String TOPIC_LEVEL_ONE = "Emne nivå 1";
    public static final String TOPIC_LEVEL_TWO = "Emne nivå 2";
    private static final String TOPIC_LEVEL_THREE = "Emne nivå 3";
    private Map<String, ResourceType> resourceTypes;

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
        assertCorrectHeaderFieldsPresent();
        buildResourceTypeParents();
    }

    private void assertCorrectHeaderFieldsPresent() {
        if(!hasField(RESOURCE_TYPE)) {
            throw new MissingParameterException(RESOURCE_TYPE);
        }
        if (!hasField(SUB_RESOURCE_TYPE)) {
            throw new MissingParameterException(SUB_RESOURCE_TYPE);
        }
        if (!hasField(NODE_ID_FIELD)) {
            throw new MissingParameterException(NODE_ID_FIELD);
        }
        if (!hasField(TOPIC_LEVEL_ONE)) {
            throw new MissingParameterException(TOPIC_LEVEL_ONE);
        }
        if (!hasField(TOPIC_LEVEL_TWO)) {
            throw new MissingParameterException(TOPIC_LEVEL_TWO);
        }
        if (!hasField(TOPIC_LEVEL_THREE)) {
            throw new MissingParameterException(TOPIC_LEVEL_THREE);
        }
    }

    private void buildResourceTypeParents() {
        resourceTypes = new HashMap<>();
        String fagstoff = "Fagstoff";
        String oppgaverOgAktiviteter = "Oppgaver og aktiviteter";
        String vurderingsressurs = "Vurderingsressurs";
        String eksternRessurs = "Ekstern læringsressurs";
        String kildemateriale = "Kildemateriale";
        String emne = "Emne";

        resourceTypes.put("Læringssti", new ResourceType("Læringssti", null, URI.create("urn:resourcetype:learningPath")));
        resourceTypes.put("Begrep", new ResourceType("Begrep", null, URI.create("urn:resourcetype:concept")));
        resourceTypes.put(fagstoff, new ResourceType(fagstoff, null, URI.create("urn:resourcetype:subjectMaterial")));
        resourceTypes.put(oppgaverOgAktiviteter, new ResourceType(oppgaverOgAktiviteter, null, URI.create("urn:resourcetype:tasksAndActivities")));
        resourceTypes.put(vurderingsressurs, new ResourceType(vurderingsressurs, null, URI.create("urn:resourcetype:reviewResource")));
        resourceTypes.put(eksternRessurs, new ResourceType(eksternRessurs, null, URI.create("urn:resourcetype:externalResource")));
        resourceTypes.put(kildemateriale, new ResourceType(kildemateriale, null, URI.create("urn:resourcetype:SourceMaterial")));
        resourceTypes.put(emne, new ResourceType(emne, null, URI.create("urn:resourcetype:topic")));

        resourceTypes.put("Film og filmklipp", new ResourceType("Film og filmklipp", fagstoff, URI.create("urn:resourcetype:movieAndClip")));
        resourceTypes.put("Forelesning og presentasjon", new ResourceType("Forelesning og presentasjon", fagstoff, URI.create("urn:resourcetype:lectureAndPresentation")));
        resourceTypes.put("Fagartikkel", new ResourceType("Fagartikkel", fagstoff, URI.create("urn:resourcetype:academicArticle")));
        resourceTypes.put("Tegning og illustrasjon", new ResourceType("Tegning og illustrasjon", fagstoff, URI.create("urn:resourcetype:drawingAndIllustration")));
        resourceTypes.put("Simulering", new ResourceType("Simulering", fagstoff, URI.create("urn:resourcetype:simulation")));
        resourceTypes.put("Verktøy og mal", new ResourceType("Verktøy og mal", fagstoff, URI.create("urn:resourcetype:toolAndTemplate")));
        resourceTypes.put("Veiledning", new ResourceType("Veiledning", fagstoff, URI.create("urn:resourcetype:guidance")));
        resourceTypes.put("Lydopptak", new ResourceType("Lydopptak", fagstoff, URI.create("urn:resourcetype:soundRecording")));
        resourceTypes.put("Oppslagsverk og ordliste", new ResourceType("Oppslagsverk og ordliste", fagstoff, URI.create("urn:resourcetype:dictionary")));

        resourceTypes.put("Oppgave", new ResourceType("Oppgave", oppgaverOgAktiviteter, URI.create("urn:resourcetype:task")));
        resourceTypes.put("Øvelse", new ResourceType("Øvelse", oppgaverOgAktiviteter, URI.create("urn:resourcetype:exercise")));
        resourceTypes.put("Arbeidsoppdrag", new ResourceType("Arbeidsoppdrag", oppgaverOgAktiviteter, URI.create("urn:resourcetype:workAssignment")));
        resourceTypes.put("Forsøk", new ResourceType("Forsøk", oppgaverOgAktiviteter, URI.create("urn:resourcetype:experiment")));
        resourceTypes.put("Spill", new ResourceType("Spill", oppgaverOgAktiviteter, URI.create("urn:resourcetype:game")));

        resourceTypes.put("Lærervurdering", new ResourceType("Lærervurdering", vurderingsressurs, URI.create("urn:resourcetype:teacherEvaluation")));
        resourceTypes.put("Egenvurdering", new ResourceType("Egenvurdering", vurderingsressurs, URI.create("urn:resourcetype:selfEvaluation")));
        resourceTypes.put("Medelevvurdering", new ResourceType("Medelevvurdering", vurderingsressurs, URI.create("urn:resourcetype:peerEvaulation")));

        resourceTypes.put("Ekstern lenke", new ResourceType("Ekstern lenke", eksternRessurs, URI.create("urn:resourcetype:externalLink")));
        resourceTypes.put("Delt læringsressurs", new ResourceType("Delt læringsressurs", eksternRessurs, URI.create("urn:resourcetype:sharedLearningResource")));
        resourceTypes.put("FYR-ressurs", new ResourceType("FYR-ressurs", eksternRessurs, URI.create("urn:resourcetype:FYRResource")));

        resourceTypes.put("Spillefilm", new ResourceType("Spillefilm", kildemateriale, URI.create("urn:resourcetype:featureFilm")));
        resourceTypes.put("Kortfilm", new ResourceType("Kortfilm", kildemateriale, URI.create("urn:resourcetype:shortFilm")));
        resourceTypes.put("Historisk materiale", new ResourceType("Historisk materiale", kildemateriale, URI.create("urn:resourcetype:historicalMaterial")));
        resourceTypes.put("Malerier- grafikk -kunstfoto", new ResourceType("Malerier- grafikk -kunstfoto", kildemateriale, URI.create("urn:resourcetype:paintingGraphicsPhoto")));
        resourceTypes.put("Litterære tekster", new ResourceType("Litterære tekster", kildemateriale, URI.create("urn:resourcetype:literaryText")));
        resourceTypes.put("Musikk", new ResourceType("Musikk", kildemateriale, URI.create("urn:resourcetype:music")));

        resourceTypes.put("Emnebeskrivelse", new ResourceType("Emnebeskrivelse", emne, URI.create("urn:resourcetype:topicDescription")));

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

        System.out.println("Importing line: ");
        System.out.println(line);
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

            System.out.println("Setting filter " + filterName + " with relevance " + relevanceName);
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
        System.out.println("Parent: " + result.parent.nodeId);

    }

    private void setEntityLevelInformation() {
        String topicLevel1 = getField(TOPIC_LEVEL_ONE);
        String topicLevel2 = getField(TOPIC_LEVEL_TWO);
        String topicLevel3 = getField(TOPIC_LEVEL_THREE);
        String resourceName = getField(LEARNING_RESOURCE);

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
            System.out.println("Setting topic level 1");
        } else if (isNotBlank(topicLevel2)) {
            result.type = Importer.TOPIC_TYPE;
            result.name = topicLevel2;
            currentLevelTwoTopic = result;
            currentLevelThreeTopic = null;
            result.rank = ++currentTopicLevelTwoRank;
            currentTopicLevelThreeRank = 0;
            currentResourceRank = 0;
            System.out.println("Setting topic level 2");
        } else if (isNotBlank(topicLevel3)) {
            result.type = Importer.TOPIC_TYPE;
            result.name = topicLevel3;
            currentLevelThreeTopic = result;
            result.rank = ++currentTopicLevelThreeRank;
            currentResourceRank = 0;
            System.out.println("Setting topic level 3");
        } else if (isNotBlank(resourceName)) {
            result.type = Importer.RESOURCE_TYPE;
            result.name = resourceName;
            result.rank = ++currentResourceRank;
            System.out.println("Setting resource");
        } else {
            throw new MissingParameterException("Entity must be named", lines.getLineNumber());
        }
    }

    private void setNodeId() {
        String urlString = getField(NODE_ID_FIELD);
        if (isBlank(urlString)) {
            System.out.println("Nodeid not found.");
            return;
        }

        String[] urlParts = urlString.split("/");
        String parametersString = urlParts[urlParts.length - 1];
        String[] parameters = parametersString.split("\\?");
        if (parameters[0].equals("verktoy") || parameters[0].equals("naturbruk")) {
            result.nodeId = parameters[0] + ":" + parameters[1].split("=")[0];
        } else {
            assertNumber(parameters[0]);
            result.nodeId = parameters[0];
        }
    }

    private void assertNumber(String nodeId) {
        try {
            new Integer(nodeId);
        } catch (Exception e) {
            System.out.println("Line " + lines.getLineNumber() + " Node id: " + nodeId + " is not a number.");
            throw e;
        }
    }

    private void setTranslatedName() {
        String nn = getField("nn");
        if (isBlank(nn)) return;

        result.translations.put("nn", new Translation() {{
            name = nn;
        }});
    }


    private void setResourceType() {
        String subresourceType = getField(SUB_RESOURCE_TYPE);
        String resourceType = getField(RESOURCE_TYPE);
        if (isBlank(resourceType) && isBlank(subresourceType)) {
            System.out.println("No resource type found.");
            return;
        }

        assertValidResourceType(resourceType, subresourceType);
        if (isNotBlank(subresourceType)) {
            subresourceType = subresourceType.trim();
            result.resourceTypes.add(resourceTypes.get(resourceTypes.get(subresourceType).parentName));
            result.resourceTypes.add(resourceTypes.get(subresourceType));
            System.out.println("Adding rt " + subresourceType + "w parent: " + resourceTypes.get(subresourceType).parentName);
        } else {
            resourceType = resourceType.trim();
            result.resourceTypes.add(resourceTypes.get(resourceType));
            System.out.println("Adding rt: " + resourceType);
        }
    }

    private void assertValidResourceType(String resourceType, String subresourceType) {

        if (isNotBlank(resourceType)) {
            if (resourceTypes.get(resourceType) == null) {
                throw new MissingParameterException("Unknown resource type: " + resourceType, lines.getLineNumber());
            }
        }
        if (isNotBlank(subresourceType)) {
            if (resourceTypes.get(subresourceType) == null) {
                throw new MissingParameterException("Unknown resource type: " + subresourceType, lines.getLineNumber());
            }
        }
    }

    public static abstract class StringIterator implements Iterator<String> {
        public abstract int getLineNumber();
    }

    private class ColumnMap {
        Map<String, List<Integer>> entries = new HashMap<>();

        ColumnMap() {
            String line = "";
            while (!line.contains(LEARNING_RESOURCE)) line = lines.next();

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
