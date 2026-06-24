# Resources

## Apple M1 Architecture

- [Apple M1 - Wikipedia](https://en.wikipedia.org/wiki/Apple_M1) — Die layout, transistor counts, GPU ALU counts, memory bandwidth specs for all M1 variants
- [Apple Silicon GPU Architecture Explained — Flopper.io](https://flopper.io/docs/apple-silicon-explained) — M1 through M5 TFLOPS, memory bandwidth, Unified Memory vs discrete GPU analysis
- [Tailor your apps for Apple GPUs and tile-based deferred rendering — Apple Developer](https://developer.apple.com/documentation/metal/tailor-your-apps-for-apple-gpus-and-tile-based-deferred-rendering) — Official TBDR docs: tile memory bandwidth/latency/energy benefits
- [Teardown: Identifying Apple M1's Distinct Circuit Blocks — EE Times](https://www.eetasia.com/teardown-identifying-apple-m1s-distinct-circuit-blocks/) — Thermal imaging confirms ANE/GPU/CPU floorplan, die analysis
- [Apple Silicon Accelerators — Asahi Linux Documentation](https://asahilinux.org/docs/hw/soc/accelerators) — Named accelerator blocks: AGX (GPU), AMX (matrix coprocessor), ANE (Neural Engine), AVE (video encode), etc.

## Apple Neural Engine

- [Apple Neural Engine — Wikipedia / Apple Wiki](https://en.wikipedia.org/wiki/Neural_Engine) — Version history: A11 (2-core, 0.6 TOPS) → M1 (16-core, 11 TOPS) → M4 (38 TOPS)
- [hollance/neural-engine — GitHub](https://github.com/hollance/neural-engine) — Community-reverse-engineered ANE knowledge: supported layers, Core ML partitioning, ANE vs GPU vs CPU breakdown
- [ANE vs GPU — hollance/neural-engine docs](https://github.com/hollance/neural-engine/blob/master/docs/ane-vs-gpu.md) — Three separate processors (CPU/GPU/ANE), shared memory, Core ML framework mapping to each
- [NPU information for Apple and Snapdragon — r/LocalLLaMA](https://www.reddit.com/r/LocalLLaMA/comments/1gy9wsx/npu_information_for_apple_and_snapdragon/) — TOPS comparison table across Apple M-series and Snapdragon NPU
