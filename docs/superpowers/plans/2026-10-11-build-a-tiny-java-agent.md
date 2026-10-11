# Build a Tiny Java Agent Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship Lesson 5 — a runnable minimal Java agent on top of AIRobot that answers questions by calling a whitelisted shell tool, plus the lesson, reference doc, and learning record that teach it.

**Architecture:** A pure, testable control loop (`AgentLoop`) holds the protocol and the think → act → observe → repeat logic behind an injected `Llm` functional interface. A thin runnable edge (`MiniAgent`) wires the real AIRobot engine (`Llama` + `ChatFormat` + `Sampler`) into that interface and runs a REPL. The tool is a shell-command runner that executes **without a shell** and only allows `date`, `ls`, `pwd`, `whoami`, `echo`.

**Tech Stack:** Java 25 (Homebrew OpenJDK), JUnit 4.13.2, `jdk.incubator.vector`, AIRobot's `com.llama4j` engine, pre-existing `assets/lesson.css`.

## Global Constraints

- **Toolchain:** `jbang` is NOT installed. Use `/opt/homebrew/opt/openjdk@25/bin/javac` and `/opt/homebrew/opt/openjdk@25/bin/java`. Class files in `build/classes` are Java 25 — do not use the system JDK 11.
- **Vector module:** every `java` run of engine code needs `--add-modules jdk.incubator.vector`.
- **Classpath jars:** `LIBS="$(ls /Users/xuyi/Source/Java/lib/*.jar | tr '\n' ':')"`.
- **Test classpath:** JUnit `4.13.2` at `/Users/xuyi/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar`; Hamcrest at `/Users/xuyi/Source/Java/lib/hamcrest-core-1.3.jar`.
- **Model (default):** `/Users/xuyi/.ollama/models/blobs/sha256-74701a8c35f6c8d9a4b91f3f3497643001d63e0c7a84e085bed452548fa88d45` (exists, 1.3 GB, Llama 3.2 1B q4_0).
- **Lesson HTML:** reuse `../assets/lesson.css`; must be clean — no malformed inline `file://` artifacts (lesson 0004 has them).
- **Quiz rule (teach skill):** every quiz answer must have exactly the same number of words.
- **No source comments** in the `.java` files (repo convention; teaching prose lives in the lesson).
- **Commits:** stage only the files named in each task; never sweep in the pre-existing dirty files (`.DS_Store`, `nbproject/project.properties`, `Llama3.java`, `Llama2.java`, `tokenizer.bin`).

---

## File Structure

| File | Responsibility |
| --- | --- |
| `src/com/llama4j/AgentLoop.java` | **Create.** Pure control loop: protocol prompt, `parseToolCall`, allowlist, `runShell`, `answer(question)`. Imports only `java.*`. |
| `src/com/llama4j/MiniAgent.java` | **Create.** Runnable jbang-style edge: owns `Llama`/`Sampler`/`State`/history, builds the real `AgentLoop.Llm`, runs the REPL. |
| `test/test/AgentLoopTest.java` | **Create.** JUnit 4 tests for parsing, allowlist safety, and loop termination. |
| `reference/agent-loop.html` | **Create.** Compact reference: loop diagram, `TOOL:` grammar, tool contract. |
| `lessons/0005-build-a-tiny-agent.html` | **Create.** The lesson (primary deliverable). |
| `learning-records/0003-agent-loop.md` | **Create.** Session learning record. |
| `MISSION.md` | **Modify.** Add one scope bullet for the agent loop. |
| `docs/superpowers/specs/2026-10-11-build-a-tiny-java-agent-design.md` | Already committed (design). |

---

## Task 1: `AgentLoop` — protocol, parsing, and the shell tool

**Files:**
- Create: `src/com/llama4j/AgentLoop.java`
- Test: `test/test/AgentLoopTest.java`

**Interfaces:**
- Consumes: nothing (pure Java).
- Produces:
  - `interface AgentLoop.Llm { String generate(String userContent); }`
  - `String AgentLoop.SYSTEM_PROMPT` (public constant)
  - `Optional<String> AgentLoop.parseToolCall(String text)`
  - `boolean AgentLoop.isAllowed(String cmd)`
  - `String AgentLoop.runShell(String cmd)`
  - `String AgentLoop.buildContent(String question, List<String> observations)`
  - `constructor AgentLoop(Llm llm)`, `String AgentLoop.answer(String question)`

- [ ] **Step 1: Write the failing tests**

Create `test/test/AgentLoopTest.java`:

```java
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
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
JUNIT=/Users/xuyi/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar
HAM=/Users/xuyi/Source/Java/lib/hamcrest-core-1.3.jar
/opt/homebrew/opt/openjdk@25/bin/javac -cp "$JUNIT:$HAM" -d build/test/classes \
  test/test/AgentLoopTest.java 2>&1 | head
```
Expected: FAIL — `package com.llama4j does not exist` / `cannot find symbol: class AgentLoop`.

- [ ] **Step 3: Write the minimal implementation**

Create `src/com/llama4j/AgentLoop.java`:

```java
package com.llama4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AgentLoop {

    public interface Llm {
        String generate(String userContent);
    }

    public static final String SYSTEM_PROMPT = """
            You are a tiny agent that answers questions by running shell commands.
            You do NOT know today's date, the filesystem, or anything outside this chat.
            To find something out, reply with exactly one line:
            TOOL: <command>
            Allowed commands: date, ls, pwd, whoami, echo.
            Example: the user asks "what day is it", so you reply "TOOL: date".
            You will then receive a line beginning with "OBSERVATION:" containing the output.
            After that, answer in plain language with NO TOOL line.
            """;

    static final Set<String> ALLOWED = Set.of("date", "ls", "pwd", "whoami", "echo");
    static final int MAX_STEPS = 5;

    private static final Pattern TOOL_PATTERN =
            Pattern.compile("TOOL:[ \\t]*([^\\n\\r]+)", Pattern.CASE_INSENSITIVE);

    private final Llm llm;

    public AgentLoop(Llm llm) {
        this.llm = llm;
    }

    public static Optional<String> parseToolCall(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher matcher = TOOL_PATTERN.matcher(text);
        return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
    }

    public static boolean isAllowed(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return false;
        }
        return ALLOWED.contains(cmd.trim().split("\\s+")[0]);
    }

    public static String runShell(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return "error: empty command";
        }
        String[] parts = cmd.trim().split("\\s+");
        if (!ALLOWED.contains(parts[0])) {
            return "error: command not allowed: " + parts[0];
        }
        try {
            Process process = new ProcessBuilder(parts).redirectErrorStream(true).start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "error: timeout";
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return output.isEmpty() ? "(no output)" : output;
        } catch (IOException | InterruptedException ex) {
            return "error: " + ex.getMessage();
        }
    }

    public static String buildContent(String question, List<String> observations) {
        StringBuilder builder = new StringBuilder(question);
        for (String observation : observations) {
            builder.append("\nOBSERVATION: ").append(observation);
        }
        return builder.toString();
    }

    public String answer(String question) {
        List<String> observations = new ArrayList<>();
        for (int step = 0; step < MAX_STEPS; step++) {
            String reply = llm.generate(buildContent(question, observations));
            Optional<String> toolCall = parseToolCall(reply);
            if (toolCall.isEmpty()) {
                return reply.strip();
            }
            observations.add(runShell(toolCall.get()));
        }
        String forced = buildContent(question, observations)
                + "\nYou must answer now in plain language with no TOOL line.";
        return llm.generate(forced).strip();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
JUNIT=/Users/xuyi/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar
HAM=/Users/xuyi/Source/Java/lib/hamcrest-core-1.3.jar
JAVA=/opt/homebrew/opt/openjdk@25/bin/java
/opt/homebrew/opt/openjdk@25/bin/javac -cp "$JUNIT:$HAM" -d build/test/classes \
  src/com/llama4j/AgentLoop.java test/test/AgentLoopTest.java
$JAVA -cp "build/test/classes:$JUNIT:$HAM" org.junit.runner.JUnitCore test.AgentLoopTest
```
Expected: `OK (11 tests)`.

- [ ] **Step 5: Commit**

```bash
git add src/com/llama4j/AgentLoop.java test/test/AgentLoopTest.java
git commit -m "agent: pure control loop with safe shell tool"
```

---

## Task 2: `MiniAgent` — wire the real engine and run the REPL

**Files:**
- Create: `src/com/llama4j/MiniAgent.java`

**Interfaces:**
- Consumes: `AgentLoop`, `AgentLoop.Llm`, `AgentLoop.SYSTEM_PROMPT`, `AgentLoop.answer` (Task 1); `com.llama4j.Llama`, `ModelLoader`, `ChatFormat`, `Sampler`.
- Produces:
  - `MiniAgent(Llama model, int maxTokens)`
  - `String MiniAgent.answer(String question)`
  - `static void MiniAgent.main(String[] args)`

- [ ] **Step 1: Write the implementation**

Create `src/com/llama4j/MiniAgent.java`:

```java
package com.llama4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class MiniAgent {

    static final String DEFAULT_MODEL =
            "/Users/xuyi/.ollama/models/blobs/sha256-74701a8c35f6c8d9a4b91f3f3497643001d63e0c7a84e085bed452548fa88d45";

    private final Llama model;
    private final Sampler sampler;
    private final Llama.State state;
    private final ChatFormat chatFormat;
    private final List<Integer> conversationTokens = new ArrayList<>();
    private final int maxTokens;

    MiniAgent(Llama model, int maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.sampler = Sampler.ARGMAX;
        this.state = model.createNewState(16);
        this.chatFormat = new ChatFormat(model.tokenizer());
        this.conversationTokens.add(chatFormat.beginOfText);
        this.conversationTokens.addAll(chatFormat.encodeMessage(
                new ChatFormat.Message(ChatFormat.Role.SYSTEM, AgentLoop.SYSTEM_PROMPT)));
    }

    private String generate(String userContent) {
        int startPosition = conversationTokens.size();
        conversationTokens.addAll(chatFormat.encodeMessage(
                new ChatFormat.Message(ChatFormat.Role.USER, userContent)));
        conversationTokens.addAll(chatFormat.encodeHeader(
                new ChatFormat.Message(ChatFormat.Role.ASSISTANT, "")));
        List<Integer> responseTokens = Llama.generateTokens(
                model, state, startPosition,
                conversationTokens.subList(startPosition, conversationTokens.size()),
                chatFormat.getStopTokens(), maxTokens, sampler, false, null);
        conversationTokens.addAll(responseTokens);
        List<Integer> visible = new ArrayList<>(responseTokens);
        if (!visible.isEmpty() && chatFormat.getStopTokens().contains(visible.get(visible.size() - 1))) {
            visible.remove(visible.size() - 1);
        }
        return model.tokenizer().decode(visible);
    }

    public String answer(String question) {
        return new AgentLoop(this::generate).answer(question);
    }

    public static void main(String[] args) throws IOException {
        Path modelPath = Paths.get(args.length > 0 ? args[0] : DEFAULT_MODEL);
        int maxTokens = 512;
        Llama model = ModelLoader.loadModel(modelPath, maxTokens, true);
        MiniAgent agent = new MiniAgent(model, maxTokens);
        System.out.println("Tiny Agent — ask a question, or type 'quit'.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            System.out.flush();
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine();
            if (line.equals("quit") || line.equals("exit")) {
                break;
            }
            System.out.println(agent.answer(line));
        }
    }
}
```

- [ ] **Step 2: Compile it against the prebuilt engine classes**

```bash
LIBS="$(ls /Users/xuyi/Source/Java/lib/*.jar | tr '\n' ':')"
/opt/homebrew/opt/openjdk@25/bin/javac -cp "build/classes:$LIBS" -d build/classes \
  src/com/llama4j/AgentLoop.java src/com/llama4j/MiniAgent.java
```
Expected: no errors (warnings about deprecated `Unsafe` are from `FloatTensor`, ignore).

- [ ] **Step 3: Run the end-to-end demo against the real model**

```bash
LIBS="$(ls /Users/xuyi/Source/Java/lib/*.jar | tr '\n' ':')"
printf "What's today's date?\nquit\n" | \
  /opt/homebrew/opt/openjdk@25/bin/java --add-modules jdk.incubator.vector \
  -cp "build/classes:$LIBS" com.llama4j.MiniAgent
```
Expected transcript shape:
```
> Tiny Agent — ask a question, or type 'quit'.
> TOOL: date
> OBSERVATION: <the real system date>
> The current date is <the real system date>.
> quit
```
Expected: the final answer contains the **actual** current date (2026-10-11), NOT a hallucinated one.

**Risk note (real, observed):** the 1B model is unreliable at following the protocol. During plan validation, a *weaker* prompt made it answer "December 8, 2006" (a hallucination) instead of emitting `TOOL:`. If Step 3 shows a hallucinated answer with no `TOOL:` line:
1. Confirm `AgentLoop.SYSTEM_PROMPT` is loaded (it is injected as the SYSTEM message in the constructor).
2. Re-run; `Sampler.ARGMAX` (temperature 0) makes it deterministic per prompt.
3. If it still refuses, try a larger local model via `MiniAgent <model-path>` (alternates are commented in `src/com/llama4j/Llama3.java:291-292`).

- [ ] **Step 4: Commit**

```bash
git add src/com/llama4j/MiniAgent.java
git commit -m "agent: MiniAgent REPL wiring the AIRobot engine"
```

---

## Task 3: Reference doc `reference/agent-loop.html`

**Files:**
- Create: `reference/agent-loop.html`

**Interfaces:**
- Consumes: `assets/lesson.css`, the `TOOL:` grammar and tool contract defined in Task 1.
- Produces: an HTML page linked from the lesson (Task 4).

- [ ] **Step 1: Write the reference document**

Create `reference/agent-loop.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Reference: The Agent Loop</title>
<link rel="stylesheet" href="../assets/lesson.css">
<style>
  .diagram { font-family: "Fira Code", monospace; font-size: 0.8rem; line-height: 1.5;
             background: #f5f2f0; padding: 1rem; border-radius: 4px; margin: 1.5rem 0; }
</style>
</head>
<body>

<h1>The Agent Loop</h1>
<p class="meta">Reference · AIRobot · LLM Inference at the Metal Level</p>

<p>An <strong>agent</strong> is an LLM wrapped in a control loop that can act on the
world and read back the result. The LLM proposes; the loop executes and feeds the
result back until the LLM can answer.</p>

<h2>The loop</h2>
<pre class="diagram">
          question
             |
             v
   +--------------------+        no TOOL line
   |  build prompt      |---------------------------> final answer
   |  (question +       |
   |   observations)    |
   +--------------------+
             |
             v
   +--------------------+     TOOL: &lt;cmd&gt;
   |  llm.generate()    |--------------------> validate &amp; run command
   +--------------------+                              |
             ^                                         v
             |                              append "OBSERVATION: ..."
             +-----------------------------------------+
                        repeat (max 5 steps)
</pre>

<h2>Protocol grammar</h2>
<table>
  <tr><th>Element</th><th>Rule</th></tr>
  <tr><td>Tool request</td><td>One line: <code>TOOL: &lt;command&gt;</code> (case-insensitive)</td></tr>
  <tr><td>Allowed commands</td><td><code>date</code>, <code>ls</code>, <code>pwd</code>, <code>whoami</code>, <code>echo</code></td></tr>
  <tr><td>Observation</td><td>Appended to the next prompt as <code>OBSERVATION: &lt;output&gt;</code></td></tr>
  <tr><td>Final answer</td><td>Any reply with no <code>TOOL:</code> line</td></tr>
  <tr><td>Termination</td><td>No tool line, or 5 steps reached</td></tr>
</table>

<h2>Tool contract</h2>
<p>The tool (<code>AgentLoop.runShell</code>) executes commands <em>without a shell</em>
via <code>ProcessBuilder(argv)</code>. No shell means <code>;</code>, <code>|</code>,
<code>&amp;&amp;</code> and backticks are never interpreted, so the allowlist checks only
the program name. Timeout is 5 seconds; stdout and stderr are merged and trimmed.</p>

<h2>Source map</h2>
<ul>
  <li><code>src/com/llama4j/AgentLoop.java</code> — the loop, protocol, and tool</li>
  <li><code>src/com/llama4j/MiniAgent.java</code> — wires <code>Llama</code> into the loop, REPL</li>
  <li><code>src/com/llama4j/Llama.java:342</code> — <code>generateTokens</code>, the engine call</li>
</ul>

<p><a href="../lessons/0005-build-a-tiny-agent.html">← Back to Lesson 5</a></p>

</body>
</html>
```

- [ ] **Step 2: Verify it renders and links resolve**

```bash
python3 -c "import html.parser,sys; html.parser.HTMLParser().feed(open('reference/agent-loop.html').read())" && echo "parses"
grep -c "lessons/0005-build-a-tiny-agent.html" reference/agent-loop.html
```
Expected: `parses` then `1`.

- [ ] **Step 3: Commit**

```bash
git add reference/agent-loop.html
git commit -m "docs: agent-loop reference sheet"
```

---

## Task 4: Lesson `lessons/0005-build-a-tiny-agent.html`

**Files:**
- Create: `lessons/0005-build-a-tiny-agent.html`

**Interfaces:**
- Consumes: `assets/lesson.css`, `reference/agent-loop.html`, `AgentLoop.java`, `MiniAgent.java`.
- Produces: the primary teaching artifact; links to prior lessons and the reference.

- [ ] **Step 1: Write the lesson**

Create `lessons/0005-build-a-tiny-agent.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Lesson 5: Build a Tiny Java Agent</title>
<link rel="stylesheet" href="../assets/lesson.css">
<style>
  .code-example { background: #fafaf5; border-left: 3px solid #4a6fa5; padding: 1rem;
                  margin: 1.5rem 0; font-family: "Fira Code", monospace; font-size: 0.85rem;
                  overflow-x: auto; }
  .diagram { font-family: "Fira Code", monospace; font-size: 0.78rem; line-height: 1.5;
             background: #f5f2f0; padding: 1rem; border-radius: 4px; margin: 1.5rem 0; overflow-x: auto; }
</style>
</head>
<body>

<h1>Build a Tiny Java Agent</h1>
<p class="meta">Lesson 5 · AIRobot · LLM Inference at the Metal Level</p>

<p class="sidenote">You already understand the engine: tokens in, logits out, a sampled
next token. This lesson adds the smallest possible thing on top of it — a loop that lets
the model act on the world and read back what happened.</p>

<p>You have built an inference engine. This lesson turns it into an <strong>agent</strong>:
a system that can <em>do</em> something and use the result. The whole agent is about
60 lines of Java, and it reuses <code>Llama.generateTokens</code> exactly as it is.</p>

<h2>The gap a lone LLM cannot cross</h2>

<p>An LLM predicts the next token from its weights and the text in its context. It has no
senses. Ask it today's date and it will produce a fluent, confident, <em>wrong</em> answer,
because the date is nowhere in its weights and nowhere in the prompt. The same is true of
"what is in this directory" — the model cannot look.</p>

<p>We validated exactly this while building the lesson: asked to reply with the token
stream <code>TOOL: date</code>, the 1B model instead answered <code>December 8, 2006</code>.
It <em>guessed</em>. That guess is the problem an agent solves.</p>

<h2>The agent loop</h2>

<p>An agent wraps the engine in a loop: <strong>think → act → observe → repeat → answer</strong>.</p>

<pre class="diagram">
          question
             |
             v
   +--------------------+        no TOOL line
   |  build prompt      |---------------------------> final answer
   |  (question +       |
   |   observations)    |
   +--------------------+
             |
             v
   +--------------------+     TOOL: &lt;cmd&gt;
   |  llm.generate()    |--------------------> validate &amp; run command
   +--------------------+                              |
             ^                                         v
             |                              append "OBSERVATION: ..."
             +-----------------------------------------+
                        repeat (max 5 steps)
</pre>

<ol>
  <li><strong>Think</strong> — build the prompt: the question plus any observations so far.</li>
  <li><strong>Act</strong> — the model replies. If the reply contains <code>TOOL: &lt;cmd&gt;</code>, that is the action.</li>
  <li><strong>Observe</strong> — run the command and append <code>OBSERVATION: &lt;output&gt;</code>.</li>
  <li><strong>Repeat</strong> — regenerate with the observation in context, up to 5 steps.</li>
  <li><strong>Answer</strong> — any reply with no <code>TOOL:</code> line is the final answer.</li>
</ol>

<p class="callout"><strong>Key idea:</strong> the model never "knows" the date. It knows
how to <em>ask</em> for it. The loop supplies the knowledge from the real world.</p>

<p>The loop lives in <code>src/com/llama4j/AgentLoop.java</code>. The engine call is the
same <code>Llama.generateTokens(...)</code> you met in
<a href="0004-tokenization-the-first-step.html">Lesson 4</a> (<code>Llama.java:342</code>).</p>

<h2>Walk the loop</h2>

<p><strong>1. The protocol.</strong> The model needs to be told the grammar. This is the
whole instruction set:</p>

<pre class="code-example">To find something out, reply with exactly one line:
TOOL: &lt;command&gt;
Allowed commands: date, ls, pwd, whoami, echo.</pre>

<p><strong>2. Parsing the action.</strong> A single regex extracts the command from the
generated text. If there is no match, the model has finished:</p>

<pre class="code-example">static final Pattern TOOL_PATTERN =
        Pattern.compile("TOOL:[ \\t]*([^\\n\\r]+)", Pattern.CASE_INSENSITIVE);</pre>

<p><strong>3. Running the tool, safely.</strong> The command is split into arguments and
executed <em>without a shell</em>. That is why <code>ls; rm -rf /</code> cannot sneak a
second command past the allowlist — <code>ls;</code> is not a program name:</p>

<pre class="code-example">String[] parts = cmd.trim().split("\\s+");
if (!ALLOWED.contains(parts[0])) {
    return "error: command not allowed: " + parts[0];
}
Process process = new ProcessBuilder(parts).redirectErrorStream(true).start();</pre>

<p><strong>4. The loop.</strong> Generate, parse, run, append, repeat — with a hard cap so
a stubborn model cannot loop forever:</p>

<pre class="code-example">for (int step = 0; step &lt; MAX_STEPS; step++) {
    String reply = llm.generate(buildContent(question, observations));
    Optional&lt;String&gt; toolCall = parseToolCall(reply);
    if (toolCall.isEmpty()) {
        return reply.strip();          // no action -> final answer
    }
    observations.add(runShell(toolCall.get()));
}</pre>

<p><strong>5. The seam.</strong> The loop never mentions <code>Llama</code>. It depends on
one method — <code>Llm.generate(String)</code> — which is why it is testable with a fake
model and reusable with the real one:</p>

<pre class="code-example">public interface Llm {
    String generate(String userContent);
}</pre>

<h2>The wiring</h2>

<p><code>src/com/llama4j/MiniAgent.java</code> connects the loop to the engine: it owns the
<code>Llama</code> model, a <code>ChatFormat</code>, the KV state, and the growing token
history. One method ties the two together:</p>

<pre class="code-example">public String answer(String question) {
    return new AgentLoop(this::generate).answer(question);
}</pre>

<p><code>this::generate</code> is the bridge: the loop asks for text, and
<code>MiniAgent</code> encodes that text with <code>ChatFormat</code>, calls
<code>Llama.generateTokens</code>, decodes the result, and hands the string back.</p>

<h2>Run it yourself</h2>

<pre class="code-example">LIBS="$(ls /Users/xuyi/Source/Java/lib/*.jar | tr '\n' ':')"
/opt/homebrew/opt/openjdk@25/bin/javac -cp "build/classes:$LIBS" -d build/classes \
  src/com/llama4j/AgentLoop.java src/com/llama4j/MiniAgent.java
/opt/homebrew/opt/openjdk@25/bin/java --add-modules jdk.incubator.vector \
  -cp "build/classes:$LIBS" com.llama4j.MiniAgent
</pre>

<p>Then ask <code>What's today's date?</code> and watch the three-line trace — the model's
<code>TOOL: date</code>, the <code>OBSERVATION</code>, and the final answer carrying the
real date. Try <code>what directory am I in?</code> and <code>who am I?</code> too.</p>

<p><em>See the compressed version in the <a href="../reference/agent-loop.html">agent-loop reference</a>.</em></p>

<h2>Check your understanding</h2>

<div class="quiz" id="quiz1">
  <h3>Question 1</h3>
  <p>After the agent receives an observation, what happens next?</p>
  <button onclick="reveal('a1')">Show answer</button>
  <div class="answer" id="a1">
    <p><strong>Answer:</strong> It adds the observation and generates another reply.</p>
  </div>
</div>

<div class="quiz" id="quiz2">
  <h3>Question 2</h3>
  <p>Why does the tool run commands without invoking a shell?</p>
  <button onclick="reveal('a2')">Show answer</button>
  <div class="answer" id="a2">
    <p><strong>Answer:</strong> No shell means pipes and semicolons cannot run.</p>
  </div>
</div>

<div class="quiz" id="quiz3">
  <h3>Question 3</h3>
  <p>What stops the loop when the model never emits a final answer?</p>
  <button onclick="reveal('a3')">Show answer</button>
  <div class="answer" id="a3">
    <p><strong>Answer:</strong> The five step limit forces one final answer.</p>
  </div>
</div>

<h2>Further reading</h2>

<ul>
  <li><a href="https://arxiv.org/abs/2210.03629">ReAct: Synergizing Reasoning and Acting in Language Models (Yao et al., 2022)</a> — the paper that named the think→act→observe loop.</li>
  <li><a href="https://www.anthropic.com/engineering/building-effective-agents">Building Effective Agents (Anthropic, 2024)</a> — why simple loops beat frameworks.</li>
  <li><a href="../reference/agent-loop.html">Agent-loop reference sheet</a> — the grammar, contract, and diagram on one page.</li>
</ul>

<p><strong>Primary source</strong>: the <a href="https://arxiv.org/abs/2210.03629">ReAct paper</a> — read the prompting protocol in Section 3, then compare it with the tiny <code>TOOL:</code> grammar you just built.</p>

<p><em>Ask followup questions — the agent is your teacher.</em></p>

<script>
function reveal(id) {
  document.getElementById(id).classList.toggle('show');
}
</script>

</body>
</html>
```

- [ ] **Step 2: Verify the lesson renders and answers are equal length**

```bash
python3 -c "import html.parser; html.parser.HTMLParser().feed(open('lessons/0005-build-a-tiny-agent.html').read())" && echo parses
```
Expected: `parses`.

Then confirm the three quiz answers are each **8 words** (a human eyeball check; the teach-skill rule): "It adds the observation and generates another reply." / "No shell means pipes and semicolons cannot run." / "The five step limit forces one final answer."

- [ ] **Step 3: Commit**

```bash
git add lessons/0005-build-a-tiny-agent.html
git commit -m "lesson 5: build a tiny Java agent"
```

---

## Task 5: Learning record + mission update

**Files:**
- Create: `learning-records/0003-agent-loop.md`
- Modify: `MISSION.md`

**Interfaces:**
- Consumes: the completed lesson and code.
- Produces: workspace state that future lessons read to compute the zone of proximal development.

- [ ] **Step 1: Write the learning record**

Create `learning-records/0003-agent-loop.md`:

```markdown
# Session 5: The Agent Loop — an engine that can act

**Session:** 5

**What I learned:** An agent is not a new model — it is an inference engine wrapped in a loop: generate text, look for an action in that text (`TOOL: <command>`), run the action, append the result as an observation, and generate again until the reply contains no action. The engine call (`Llama.generateTokens`) is unchanged; all the new behaviour lives in ~60 lines of orchestration (`AgentLoop.java`) behind an injectable `Llm` interface.

**Why it matters:** This is the bridge from the mission's "metal level" to goal-seeking systems. Everything the agent "knows" about the outside world arrives through the loop as text appended to the context — the same token pipeline from Lesson 4, fed by a tool instead of a user.

**Key insight:** The tool boundary is the security boundary. Running the allowlisted command *without a shell* (via `ProcessBuilder(argv)`) is what makes the allowlist meaningful — with `sh -c`, `ls; rm -rf /` would still execute the `rm`.

**Revisiting this:** If we add more tools or real tool-calling models (native special tokens), the `Llm` seam and the protocol layer are where that work lands. A 1B model is unreliable at the text protocol — a larger model or a fine-tuned one removes the ambiguity.
```

- [ ] **Step 2: Add the mission scope bullet**

In `MISSION.md`, find the `**Scope:**` list (ends with the sampling-strategies bullet) and append this line after it:

```markdown
- The agent control loop (generate → parse action → run a tool → observe) built on top of inference
```

- [ ] **Step 3: Verify**

```bash
grep -n "agent control loop" MISSION.md
test -f learning-records/0003-agent-loop.md && echo "record ok"
```
Expected: one matching line in `MISSION.md`, then `record ok`.

- [ ] **Step 4: Commit**

```bash
git add learning-records/0003-agent-loop.md MISSION.md
git commit -m "learning record + mission: agent loop"
```

---

## Self-Review

**1. Spec coverage**
- Lesson HTML → Task 4 ✔
- Runnable Java agent → Tasks 1 + 2 (`MiniAgent`, with the loop split into the testable `AgentLoop`) ✔
- Reference doc → Task 3 ✔
- Learning record → Task 5 ✔
- MISSION update → Task 5 ✔
- Build-up, code-anchored structure → Task 4 content matches the spec outline (gap → loop → walk code → wiring → run → quiz → sources) ✔
- Prompt-based `TOOL:` protocol on the existing model → Task 1 system prompt + Task 2 uses `Sampler.ARGMAX` ✔
- Shell tool with allowlist + no-shell safety → Task 1 `runShell` + tests ✔
- Error handling (disallowed command, timeout, iteration cap, empty output) → Task 1 `runShell` and `answer` ✔
- Verification criteria (real date in answer; injection refused; clean HTML; equal-length quiz answers) → Tasks 2, 1, 3/4 ✔

**2. Placeholder scan:** No TBD/TODO; all steps contain full code or full file content.

**3. Type consistency:** `AgentLoop.Llm.generate(String)` is defined in Task 1 and used via `this::generate` in Task 2; `SYSTEM_PROMPT`, `answer(String)`, `parseToolCall`, `isAllowed`, `runShell`, `buildContent` names are identical across tasks and tests. `MiniAgent(Llama, int)` and `ModelLoader.loadModel(Path, int, boolean)` match the engine in `src/com/llama4j/ModelLoader.java:51`.

**Deviation from spec:** the design's single "MiniAgent.java" is split into `AgentLoop` (pure, testable) + `MiniAgent` (engine wiring + REPL). This preserves every spec artifact and behaviour while making the loop unit-testable without loading a 1.3 GB model.
