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
    private int startPosition;

    MiniAgent(Llama model, int maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.sampler = Sampler.ARGMAX;
        this.state = model.createNewState(16);
        this.chatFormat = new ChatFormat(model.tokenizer());
        this.conversationTokens.add(chatFormat.beginOfText);
        this.conversationTokens.addAll(chatFormat.encodeMessage(
                new ChatFormat.Message(ChatFormat.Role.SYSTEM, AgentLoop.SYSTEM_PROMPT)));
        this.startPosition = 0;
    }

    private String generate(String userContent) {
        conversationTokens.addAll(chatFormat.encodeMessage(
                new ChatFormat.Message(ChatFormat.Role.USER, userContent)));
        conversationTokens.addAll(chatFormat.encodeHeader(
                new ChatFormat.Message(ChatFormat.Role.ASSISTANT, "")));
        List<Integer> responseTokens = Llama.generateTokens(
                model, state, startPosition,
                conversationTokens.subList(startPosition, conversationTokens.size()),
                chatFormat.getStopTokens(), maxTokens, sampler, false, null);
        conversationTokens.addAll(responseTokens);
        startPosition = conversationTokens.size();
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
