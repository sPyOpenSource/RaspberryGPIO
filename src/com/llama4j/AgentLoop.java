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
            You can run commands. To get the date reply with TOOL: date
            """;

    static final Set<String> ALLOWED = Set.of("date", "ls", "pwd", "whoami", "echo");
    static final int MAX_STEPS = 5;

    private static final Pattern TOOL_PATTERN =
            Pattern.compile("(?m)^[ \\t]*TOOL:[ \\t]*(\\S[^\\r\\n]*)", Pattern.CASE_INSENSITIVE);

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
            StringBuilder output = new StringBuilder();
            Thread reader = new Thread(() -> {
                try (var in = process.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        output.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    }
                } catch (IOException ignored) {
                }
            });
            reader.setDaemon(true);
            reader.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "error: timeout";
            }
            reader.join(1000);
            String text = output.toString().trim();
            return text.isEmpty() ? "(no output)" : text;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "error: interrupted";
        } catch (IOException ex) {
            return "error: " + ex.getMessage();
        }
    }

    public static String buildContent(String question, List<String> observations) {
        StringBuilder builder = new StringBuilder(question);
        for (String observation : observations) {
            builder.append("\nOBSERVATION: ").append(observation);
        }
        if (!observations.isEmpty()) {
            builder.append("\nFinal answer:");
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
