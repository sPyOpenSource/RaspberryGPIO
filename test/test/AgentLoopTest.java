package test;

import com.llama4j.AgentLoop;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class AgentLoopTest {

    @Test
    public void parsesToolCall() {
        assertEquals("date", AgentLoop.parseToolCall("Checking.\nTOOL: date\n").orElseThrow());
    }

    @Test
    public void parsesToolCallCaseInsensitively() {
        assertEquals("pwd", AgentLoop.parseToolCall("tool: pwd").orElseThrow());
    }

    @Test
    public void parsesNoToolCall() {
        assertTrue(AgentLoop.parseToolCall("The answer is 42.").isEmpty());
    }

    @Test
    public void parsesNullAsEmpty() {
        assertTrue(AgentLoop.parseToolCall(null).isEmpty());
    }

    @Test
    public void allowsWhitelistedCommands() {
        assertTrue(AgentLoop.isAllowed("date"));
        assertTrue(AgentLoop.isAllowed("echo hi"));
    }

    @Test
    public void rejectsDisallowedCommands() {
        assertFalse(AgentLoop.isAllowed("rm -rf /"));
    }

    @Test
    public void runShellRefusesShellMetacharacters() {
        String out = AgentLoop.runShell("ls; rm -rf /");
        assertTrue(out.startsWith("error: command not allowed"));
    }

    @Test
    public void runShellExecutesEcho() {
        assertEquals("hi", AgentLoop.runShell("echo hi"));
    }

    @Test
    public void buildContentAppendsObservations() {
        String content = AgentLoop.buildContent("what date?", List.of("Fri Oct 11"));
        assertTrue(content.contains("what date?"));
        assertTrue(content.contains("OBSERVATION: Fri Oct 11"));
    }

    @Test
    public void loopCallsToolThenAnswers() {
        List<String> seen = new ArrayList<>();
        AgentLoop.Llm fake = content -> {
            seen.add(content);
            if (content.contains("OBSERVATION:")) {
                return "The date is in the observation.";
            }
            return "TOOL: echo today";
        };
        AgentLoop loop = new AgentLoop(fake);
        assertEquals("The date is in the observation.", loop.answer("what date?"));
        assertEquals(2, seen.size());
        assertTrue(seen.get(1).contains("OBSERVATION: today"));
    }

    @Test
    public void loopStopsAtMaxStepsWhenModelNeverFinishes() {
        AgentLoop.Llm alwaysTool = content -> "TOOL: echo loop";
        AgentLoop loop = new AgentLoop(alwaysTool);
        assertNotNull(loop.answer("go"));
    }
}
