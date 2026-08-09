## Overview

This repository contains a Java-based prototype for JSON transformation and mapping.
It is build upon a Master Thesis project of Michelle Gavlák.

The project supports JSON transformation specifications for the operations:
- `identity`
- `add`
- `copy`
- `move`
- `remove`
- `rename`
- `replace`

It includes a prototype transformation engine, a measurement harness, JSON schema validation, and sample input/specification datasets.

## Repository Structure

- `code/`
  - `src/` - Java source files
    - `prototype/` - main transformation engine, path automaton, mapper, state architecture, and specification parser
    - `measurement/` - performance measurement harness using com.sun.management.ThreadMXBean for counting total heap allocations
  - `test/` - JUnit tests for transformation correctness
- `measurements/`
  - `specification/` - sample transformation specifications
  - `inputs/` - sample JSON input files
- `evaluateAll.ps1` - PowerShell script to run full evaluation workload
- `evaluateBufferSrcFirst.ps1` - PowerShell script for evaluation of copy/move src-first scenario
- `evaluateBufferDestFirst.ps1` - PowerShell script for evaluation of copy/move dest-first scenario
- `pom.xml` - Maven build configuration

## Dependencies

- com.fasterxml.jackson.core:jackson-databind:2.15.2
- com.networknt:json-schema-validator:1.0.87
- org.junit.jupiter:junit-jupiter:5.10.0
- org.slf4j:slf4j-simple:2.0.13


## Build Requirements

- Java 21+ JDK
- Apache Maven

## Build

```powershell
mvn clean compile
```

## Test

```powershell
mvn test
```

## Run

For JSON transformation with input from user.

```powershell
mvn exec:java@prototype
```

For the evaluation.

```powershell
mvn exec:java@measurements
```

## Evaluation / Measurements

Evaluation and benchmarking are supported by the `measurements.Main` harness.

- evaluateAll.ps1: Memory allocations of stack-class transformations and buffer-class transformations for fixed size of buffered data
- evaluateCopyAndMove.ps1: Memory allocations of buffer-class transformations for variable size of buffered data

# Usage
Copy dependencies to target

```powershell
mvn dependency:copy-dependencies
```
Adjust JAVA_HOME and $env:Path in the corresponding script.

```powershell
./evaluateAll.ps1
```

```powershell
./evaluateCopyAndMove.ps1
```

This runs measurement scenarios across multiple specification and input sizes, producing results in `measurements/outputs`.

## Specification Format

Specifications are JSON arrays of transformation objects. Example:

```json
[
  {
    "move": {
      "path": "$[1].contact",
      "destPath": "$[0].array",
      "index": 4
    }
  }
]
```

Specifications are validated against the bundled JSON schema before execution.

## Notes

- `copy` and `move` transformations use a buffer-based transducer, while other transformations use stack-based or identity transducers.
- `JsonExamples/SpecificationFiles/` contains sample specification files for each supported transformation type.

## AI usage
AI has been used for
- exception handling
- scripts for running benchmarks

