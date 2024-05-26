# Bazel Scala Example

This is an example project that demonstrates how to build a Scala project with Bazel.

## Prerequisites

- [Bazel](https://bazel.build)

## IDE

For some reason the current version of metals (v1.3.1) seems to use a version of `bazel-bsp` that breaks quick fix actions.
This can be fixed by now by running `bazel run //:bazel_bsp` and restarting metals.

## Build

```bash
bazel build //...
```

# Run

```bash
bazel run //backend
```