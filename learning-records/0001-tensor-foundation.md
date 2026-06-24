# Session 1: Tensors — the computational atom of LLM inference

**Session:** 1

**What I learned:** Every operation in LLM inference — from attention to feed-forward to final classifier — reduces to tensor operations (`dot`, `matmul`, `softmax`, elementwise ops). The `FloatTensor` abstract class in AIRobot defines these core primitives, and each quantized subclass (`Q4_0FloatTensor`, `BF16FloatTensor`, etc.) implements the *same* interface with *different* storage/bandwidth trade-offs.

**Why it matters:** The tensor abstraction is the cost model for inference. Quantized formats save memory but add decompression overhead per element. The `dot` product is where the CPU spends its cycles — understanding this is necessary before you can reason about performance, memory budgets, or quantization choices.

**Revisiting this:** If we later add GPU support (CUDA, etc.), the tensor abstraction boundary becomes critical — the GPU tensors would implement `getFloat`/`setFloat`/`dot` backed by device memory.
