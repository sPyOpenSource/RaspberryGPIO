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
    public void ignoresToolMentionMidSentence() {
        assertTrue(AgentLoop.parseToolCall("there is no TOOL: date needed here").isEmpty());
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
    public void runShellHandlesLargeOutputWithoutTimeout() {
        String big = AgentLoop.runShell("echo " + "a".repeat(200000));
        assertEquals(200000, big.length());
    }

    @Test
    public void runShellReturnsPlaceholderWhenEmpty() {
        assertEquals("(no output)", AgentLoop.runShell("echo"));
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
    public void loopReportsDisallowedCommandAsObservation() {
        List<String> seen = new ArrayList<>();
        AgentLoop.Llm fake = content -> {
            seen.add(content);
            return seen.size() == 1 ? "TOOL: rm -rf /" : "done";
        };
        AgentLoop loop = new AgentLoop(fake);
        assertEquals("done", loop.answer("go"));
        assertTrue(seen.get(1).contains("error: command not allowed"));
    }

    @Test
    public void loopStopsAtMaxStepsAndForcesFinalAnswer() {
        List<String> seen = new ArrayList<>();
        AgentLoop.Llm alwaysTool = content -> {
            seen.add(content);
            return "TOOL: echo loop";
        };
        AgentLoop loop = new AgentLoop(alwaysTool);
        assertEquals("TOOL: echo loop", loop.answer("go"));
        assertEquals(6, seen.size());
        assertTrue(seen.get(seen.size() - 1).contains("You must answer now"));
    }
}
