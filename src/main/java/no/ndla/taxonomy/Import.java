package no.ndla.taxonomy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class Import {

    public static void main(String... args) {
    }

    public static class CreateSubjectCommand {
        @JsonProperty
        public URI id;

        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String name;
    }

    public static class UpdateSubjectCommand {
        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String name;
    }

    public static class SubjectIndexDocument {
        @JsonProperty
        public URI id;

        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String name;

        @JsonProperty
        public String path;
    }

    public static class TopicIndexDocument {
        @JsonProperty
        public URI id;

        @JsonProperty
        public String name;

        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public URI parent;

        @JsonProperty
        public String path;
    }

    public static class ResourceIndexDocument {
        @JsonProperty
        public URI id;

        @JsonProperty
        public URI topicId;

        @JsonProperty
        public String name;

        @JsonProperty
        public Set<ResourceTypeIndexDocument> resourceTypes = new HashSet<>();

        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String path;
    }

    public static class ResourceTypeIndexDocument {
        @JsonProperty
        public URI id;

        @JsonProperty
        public String name;

        @Override
        @JsonIgnore
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ResourceTypeIndexDocument)) return false;

            ResourceTypeIndexDocument that = (ResourceTypeIndexDocument) o;

            return id.equals(that.id);
        }

        @Override
        @JsonIgnore
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static class AddSubtopicToTopicCommand {
        @JsonProperty
        public URI topicid;

        @JsonProperty
        public URI subtopicid;

        @JsonProperty
        public boolean primary;
    }

    public static class UpdateTopicSubtopicCommand {
        @JsonProperty
        public URI id;

        @JsonProperty
        public boolean primary;
    }

    public static class TopicSubtopicIndexDocument {
        @JsonProperty
        public URI topicid;

        @JsonProperty
        public URI subtopicid;

        @JsonProperty
        public URI id;

        @JsonProperty
        public boolean primary;

        TopicSubtopicIndexDocument() {
        }

    }

    public static class CreateTopicCommand {
        @JsonProperty
        public URI id;

        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String name;
    }

    public static class UpdateTopicCommand {
        @JsonProperty
        public URI contentUri;

        @JsonProperty
        public String name;
    }

    public static class AddTopicToSubjectCommand {
        @JsonProperty
        public URI subjectid;

        @JsonProperty
        public URI topicid;

        @JsonProperty
        public boolean primary;
    }

    public static class UpdateSubjectTopicCommand {
        @JsonProperty
        public URI id;

        @JsonProperty
        public boolean primary;
    }

    public static class SubjectTopicIndexDocument {
        @JsonProperty
        public URI subjectid;

        @JsonProperty
        public URI topicid;

        @JsonProperty
        public URI id;

        @JsonProperty
        public boolean primary;

        SubjectTopicIndexDocument() {
        }

    }

    public static class SubjectTranslationIndexDocument {
        @JsonProperty
        public String name;

        @JsonProperty
        public String language;
    }

    public static class UpdateSubjectTranslationCommand {
        @JsonProperty
        public String name;
    }
}
