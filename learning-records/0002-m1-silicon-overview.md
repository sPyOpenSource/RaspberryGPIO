# Session 2: M1 Silicon Overview — Three Processors, One Memory

**Session:** 2

**What I learned:** The M1 is a heterogeneous SoC with three compute engines (CPU, GPU, ANE) sharing one unified memory pool. AIRobot currently uses only the CPU via `jdk.incubator.vector` (compiled to NEON SIMD). The GPU offers ~10× more FP32 throughput but requires Metal (JNI bridge from Java). The ANE offers ~50× more INT8 throughput but is only accessible through Core ML's private frameworks. The unified memory architecture means there is no data-movement penalty between processors — the barrier is purely software.

**Why it matters:** Understanding the three processors explains why the same inference engine runs at different speeds depending on which hardware it reaches. The M1's GPU/ANE are not "hidden performance" — they're gated by language and framework boundaries. When benchmarking AIRobot, we're measuring CPU NEON throughput, not the M1's peak capability.

**Key insight:** The 10-50 µs kernel launch latency of the GPU means it loses to the CPU for autoregressive batch-1 inference where each matmul is small and sequential. The GPU wins only when (batch_size × dim) is large enough to amortize launch overhead across many ALUs. This is why llama.cpp on Metal shows the biggest gains at large batch sizes or prompt processing (prefill), not single-token generation.

**Revisiting this:** As the project evolves, the `FloatTensor` abstraction boundary is the natural seam for adding a GPU backend — each quantized tensor type would need a `dot` implementation that copies storage to a `MTLBuffer` and launches a Metal compute shader. The cost model for deciding CPU vs GPU per operation depends on matmul size, batch count, and quantization format.
