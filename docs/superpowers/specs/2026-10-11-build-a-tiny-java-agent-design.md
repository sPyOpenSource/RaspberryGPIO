# Design: Lesson 5 — Build a Tiny Java Agent

**Date:** 2026-10-11
**Workspace:** AIRobot (teaching workspace — `teach` skill conventions)
**Status:** Approved design; ready for implementation planning

## Problem

The user has built AIRobot, a Java Llama inference engine, and is learning LLM
inference "at the metal level" (see `MISSION.md`). Lessons 0001–0004 cover
tensors, M1 silicon, GGUF, and tokenization. The user now wants a lesson on
**how to build an AI agent**, framed as a hands-on build of the smallest possible
agent on top of the inference engine they already understand.

An LLM alone only predicts tokens from its weights plus context. It cannot see
the filesystem or know the current date. An **agent** is that same engine wrapped
in a control loop that can recognize a gap in its knowledge, emit an action,
execute that action against the real world, feed the observation back into
context, and repeat until it can answer.

The lesson must be teaching-workspace-native: an HTML lesson in `lessons/`,
grounded in the user's own code, plus reusable reference material and a learning
record. It must be short and completable quickly, per the `teach` skill's working-
memory constraint.

## Goals

- Teach the agent control loop (think → act → observe → repeat → answer) using
  AIRobot's existing inference API as the "brain".
- Produce a **real, runnable** minimal agent in Java so the user can execute it.
- Use a prompt-based tool protocol so it runs on the user's existing
  `llama3.2-1b` (q4_0) model with no fine-tuning.
- Give the user a tangible win: a working REPL where they can ask a question that
  can only be answered by running a shell command.

## Non-Goals

- Native/tool-use-fine-tuned models, special tokens, or new chat templates.
- Multi-tool registries, planning, memory, RAG, or multi-agent systems.
- Framework comparisons (LangChain etc.).
- Changing the inference engine internals.
- Training or fine-tuning.

## Decisions (from the design dialogue)

| Decision | Choice |
| --- | --- |
| Deliverable | A new lesson in this repo (not a product feature) |
| Lesson angle | Build a tiny Java agent on top of AIRobot |
| Tool-calling protocol | Prompt-based text protocol (`TOOL: <cmd>`), works with existing model |
| The tool | Run a whitelisted shell command |
| Code location | Real, runnable `src/com/llama4j/MiniAgent.java` +
lesson walks through it |
| Demo | Interactive REPL; user types any question, date question suggested first |
| Mission | Extend `MISSION.md` with an agent-loop scope bullet |
| Lesson structure | Build-up, code-anchored (matches lessons 0003/0004 style) |

## Architecture — the agent loop

Four parts:

1. **LLM core** — the existing `Llama` record + `ChatFormat` + `Sampler`
   (unchanged). This is the brain built in lessons 1–4.
2. **Tool** — `runShell(cmd)`: validates against an allowlist (`date`, `ls`,
   `pwd`, `whoami`, `echo`), runs via `ProcessBuilder`, 5-second timeout,
   captures stdout + stderr.
3. **Control loop** — `MiniAgent`: build prompt → generate → parse → execute →
   append observation → repeat.
4. **Protocol** — a system prompt instructing the model to emit
   `TOOL: <command>` when it needs outside information.

### One iteration's data flow

1. `MiniAgent` builds the chat context: system prompt (protocol) + user question.
2. `Llama.generateTokens(model, state, startPosition, promptTokens, stopTokens,
   maxTokens, sampler, echo, onTokenGenerated)` produces assistant text.
3. Scan the assistant text for `TOOL: <cmd>` (regex `TOOL:[ \t]*([^\n\r]+)`,
   capturing a single line).
4. If found: validate against the allowlist, run the command, append
   `OBSERVATION: <output>` as a **user** message (Llama-3's chat template has no
   tool role).
5. Loop back to step 1 with the expanded context.
6. If no marker is found, that assistant text is the final answer: print and stop.
   A hard cap of 5 iterations prevents infinite loops.

### Component interfaces

- `MiniAgent` — owns `Llama model`, `Sampler sampler`, `Llama.State state`,
  `List<Integer> conversationTokens`, and an iteration counter. Exposes
  `String answer(String question)` returning the final text.
- `runShell(String cmd)` — returns the command's combined stdout+stderr as a
  string, or a descriptive error string (`error: command not allowed`,
  `error: timeout`, `error: ...`). Static allowlist of approved commands.
- Protocol system prompt — a single constant string; documents the `TOOL:` grammar
  and tells the model to stop and answer when it has enough information.
- Stop condition — no `TOOL:` marker in a turn, or the iteration cap reached.

## Error Handling

- **Disallowed command** → return `OBSERVATION: error: command not allowed: <cmd>`
  so the model can self-correct.
- **Nonzero exit / stderr output** → concatenated into the observation text.
- **Timeout (5s)** → destroy the process and return `error: timeout`.
- **Iteration cap reached** → force a final answer turn (instruct the model to
  answer now, no more tools).
- **Malformed marker** → treated as final text (harmless).
- **Missing model file** → fail fast with a clear message and usage hint.

## Lesson Content Outline (build-up, code-anchored)

1. Title + meta (`Lesson 5 · AIRobot · LLM Inference at the Metal Level`) +
   sidenote contrasting token prediction with knowledge of the world.
2. **The gap** — why the model confidently states a wrong date.
3. **The agent loop** — diagram + the 5 logical steps.
4. **Walk `MiniAgent.java`** in ~5 annotated chunks with `file:line` references.
5. **The shell tool + allowlist safety** — why allowlisting matters.
6. **Run it yourself** — the `jbang` command + suggested prompts (date first).
7. **Quiz** — 3 questions, each answer exactly the same number of words (per the
   `teach` skill's no-formatting-clues rule).
8. **Primary source + further reading** — the ReAct paper (Yao et al., 2022) and
   Anthropic's *Building Effective Agents* (2024), plus the "ask follow-up
   questions" reminder.

The lesson reuses `assets/lesson.css` and must produce clean HTML (avoid the
malformed inline `file://` artifacts present in lesson 0004).

## Artifacts Produced

| File | Purpose |
| --- | --- |
| `lessons/0005-build-a-tiny-agent.html` | The lesson (primary deliverable) |
| `src/com/llama4j/MiniAgent.java` | Runnable jbang agent, defaults to the user's ollama model path |
| `reference/agent-loop.html` | Compact reference: loop diagram, tool contract, protocol grammar |
| `learning-records/0003-agent-loop.md` | Learning record for the session |
| `MISSION.md` | Add one scope bullet for the agent control loop |

Cross-links: the lesson links to `reference/agent-loop.html` and prior lessons;
the reference links back to the lesson.

## Verification / Acceptance Criteria

- `jbang src/com/llama4j/MiniAgent.java` loads the default model and presents a
  REPL without errors.
- Asking "What's today's date?" prints a transcript that shows:
  1. a `TOOL: date` call emitted by the model,
  2. an `OBSERVATION:` line containing the real date,
  3. a final answer containing the actual date.
- A disallowed command (e.g. `rm -rf /`) is refused by the allowlist and reported
  as an observation; no side effects occur.
- The lesson HTML renders cleanly with no malformed inline file-link artifacts.
- Quiz answers are equal-length.

## Open Risks

- **1B model compliance:** a 1B model may not reliably follow the `TOOL:` protocol
  on the first try. Mitigation: a crisp, example-driven system prompt, a
  low temperature, and the iteration cap with a forced-answer fallback. If
  compliance is poor, the lesson notes trying a larger model or lowering
  temperature.
- **Model path portability:** the hardcoded ollama blob path is machine-specific.
  `MiniAgent` accepts a `--model` argument with the current path as default.
