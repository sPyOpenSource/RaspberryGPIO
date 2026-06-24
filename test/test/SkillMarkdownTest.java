package test;

import com.llama4j.SkillMarkdown;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;
import org.junit.Test;

public class SkillMarkdownTest {

    @Test
    public void parsesFullFrontmatter() {
        String input = "---\nname: my-skill\ndescription: A test skill\n---\n\n# Body\n\nSome markdown content.";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("my-skill", sm.name());
        assertEquals("A test skill", sm.description());
        assertTrue(sm.metadata().isEmpty());
        assertTrue(sm.body().startsWith("# Body"));
    }

    @Test
    public void parsesMetadataFields() {
        String input = "---\nname: test\nlicense: MIT\ncompatibility: opencode>=1.0\ncustom: value\n---\nbody";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("MIT", sm.metadata().get("license"));
        assertEquals("opencode>=1.0", sm.metadata().get("compatibility"));
        assertEquals("value", sm.metadata().get("custom"));
    }

    @Test
    public void returnsEmptyNameWhenMissing() {
        String input = "---\ndescription: only desc\n---\nbody";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("", sm.name());
        assertEquals("only desc", sm.description());
    }

    @Test
    public void handlesNoFrontmatter() {
        SkillMarkdown sm = SkillMarkdown.parse("Just plain markdown.\n\nNo frontmatter here.");
        assertEquals("", sm.name());
        assertEquals("", sm.description());
        assertTrue(sm.body().contains("plain markdown"));
    }

    @Test
    public void handlesEmptyOrBlankInput() {
        SkillMarkdown empty = SkillMarkdown.parse("");
        assertEquals("", empty.name());
        assertEquals("", empty.body());

        SkillMarkdown blank = SkillMarkdown.parse("   \n\n  ");
        assertEquals("", blank.name());
    }

    @Test
    public void handlesNullInput() {
        SkillMarkdown sm = SkillMarkdown.parse((String) null);
        assertEquals("", sm.name());
        assertEquals("", sm.body());
    }

    @Test
    public void handlesMalformedFrontmatter() {
        String input = "---\nname: broken\nno closing delimiter";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("", sm.name());
        assertTrue(sm.body().length() > 0);
    }

    @Test
    public void trimsWhitespace() {
        String input = "  \n---\nname: trimmed\n---\n  \nbody text  ";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("trimmed", sm.name());
        assertEquals("body text", sm.body());
    }

    @Test
    public void parsesFromPath() throws IOException {
        Path tmp = Files.createTempFile("skill", ".md");
        try {
            Files.writeString(tmp, "---\nname: tmp-skill\ndescription: from file\n---\ncontent");
            SkillMarkdown sm = SkillMarkdown.parse(tmp);
            assertEquals("tmp-skill", sm.name());
            assertEquals("from file", sm.description());
            assertEquals("content", sm.body());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void preservesBodyWithMarkdownFormatting() {
        String body = "# Title\n\n- list item\n- another item\n\n```java\nSystem.out.println(\"hi\");\n```";
        String input = "---\nname: docs\n---\n" + body;
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals(body, sm.body());
    }

    @Test
    public void metadataIsUnmodifiable() {
        String input = "---\nname: x\ncustom: val\n---";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        try {
            sm.metadata().put("new", "value");
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void handlesMultipleDashesInBody() {
        String input = "---\nname: test\n---\n---\nThis has --- in the body\n---more---";
        SkillMarkdown sm = SkillMarkdown.parse(input);
        assertEquals("test", sm.name());
        assertEquals("---\nThis has --- in the body\n---more---", sm.body());
    }
}
