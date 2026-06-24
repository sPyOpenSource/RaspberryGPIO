package com.llama4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.yaml.snakeyaml.Yaml;

public final class SkillMarkdown {

    private final String name;
    private final String description;
    private final Map<String, String> metadata;
    private final String body;

    public SkillMarkdown(String name, String description, Map<String, String> metadata, String body) {
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
        this.body = body == null ? "" : body;
    }

    public String name() { return name; }
    public String description() { return description; }
    public Map<String, String> metadata() { return metadata; }
    public String body() { return body; }

    public static SkillMarkdown parse(String content) {
        if (content == null || content.isBlank()) {
            return new SkillMarkdown("", "", Map.of(), content == null ? "" : content);
        }
        String trimmed = content.stripLeading();
        if (!trimmed.startsWith("---")) {
            return new SkillMarkdown("", "", Map.of(), content);
        }
        String afterFirst = trimmed.substring(3);
        int endIndex = afterFirst.indexOf("\n---");
        if (endIndex == -1) {
            return new SkillMarkdown("", "", Map.of(), content);
        }
        String yamlBlock = afterFirst.substring(0, endIndex).strip();
        String body = afterFirst.substring(endIndex + 4).strip();

        Map<String, String> metadata = new LinkedHashMap<>();
        String name = "";
        String description = "";

        if (!yamlBlock.isBlank()) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(yamlBlock);
            Map<String, Object> raw = loaded instanceof Map ? (Map<String, Object>) loaded : null;
            if (raw != null) {
                for (var entry : raw.entrySet()) {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    String strVal = val == null ? "" : val.toString();
                    switch (key) {
                        case "name":
                            name = strVal;
                            break;
                        case "description":
                            description = strVal;
                            break;
                        default:
                            metadata.put(key, strVal);
                            break;
                    }
                }
            }
        }
        return new SkillMarkdown(name, description, metadata, body);
    }

    public static SkillMarkdown parse(Path path) throws IOException {
        return parse(Files.readString(path));
    }
    
    public static void main(String[] arg){
        SkillMarkdown skill = null;
        try {
            skill = SkillMarkdown.parse(Path.of("/Users/xuyi/.hermes/skills/understand-anything/understand/SKILL.md"));
        } catch (IOException ex) {
            System.getLogger(SkillMarkdown.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        System.out.println(skill);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillMarkdown)) return false;
        SkillMarkdown that = (SkillMarkdown) o;
        return name.equals(that.name)
                && description.equals(that.description)
                && metadata.equals(that.metadata)
                && body.equals(that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, metadata, body);
    }

    @Override
    public String toString() {
        return "SkillMarkdown{name='" + name + "', description='" + description + "'}";
    }
}
