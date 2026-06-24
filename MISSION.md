# Mission: Understand LLM Inference at the Metal Level

**Why:** You've built AIRobot — a Java implementation of a Llama model inference engine — and you want to understand how LLMs actually work under the hood, from tensors to tokens. No black boxes, no abstractions — just the raw mechanics.

**Goal:** Be able to trace a single inference forward pass from input text to output token, accounting for every matrix multiply, quantization format, and sampling decision.

**Scope:**
- Tensor operations and the matrix math that drives attention
- GGUF model format and how weights are loaded
- Quantization (Q4_0, Q8_0, BF16, F16) — what they save and what they cost
- BPE tokenization and vocabulary
- RoPE (Rotary Position Embeddings)
- KV-cache management
- The autoregressive generation loop
- Sampling strategies (categorical, top-p, top-k)

**Non-goals:** This is not about training LLMs, fine-tuning, or deployment infrastructure. This is about understanding inference — what happens when you press "go."
