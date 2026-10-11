# Session 5: The Agent Loop — an engine that can act

**Session:** 5

**What I learned:** An agent is not a new model — it is an inference engine wrapped in a loop: generate text, look for an action in that text (`TOOL: <command>`), run the action, append the result as an `OBSERVATION`, and generate again until the reply contains no action. The engine call (`Llama.generateTokens`) is unchanged; all the new behaviour lives in ~60 lines of orchestration (`AgentLoop.java`) behind an injectable `Llm` interface. The loop is capped at five steps and, once an observation exists, appends a `Final answer:` cue to stop the model calling tools forever.

**Why it matters:** This is the bridge from the mission's "metal level" to goal-seeking systems. Everything the agent "knows" about the outside world arrives through the loop as text appended to the context — the same token pipeline from Lesson 4, fed by a tool instead of a user.

**Key insight:** The tool boundary is the security boundary. Running the allowlisted command *without a shell* (via `ProcessBuilder(argv)`) is what makes the allowlist meaningful — with `sh -c`, `ls; rm -rf /` would still execute the `rm`. The second insight is that reliability had to come from code, not the model: the 1B model ignored a rich system prompt and hallucinated the date, so the shipped prompt is a single short line and the loop enforces safety and termination itself.

**Revisiting this:** If we add more tools or switch to a model with native tool-calling tokens, the `Llm` seam and the protocol layer are where that work lands. A 1B model is unreliable at a free-text protocol — a larger or fine-tuned model would remove the need for the `Final answer:` nudge and the date-specific prompt.
