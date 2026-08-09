## Overview

This repository contains a Java-based prototype for JSON transformation and mapping.

The project supports JSON transformation specifications for operations such as:
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
    - `Prototype/` - main transformation engine, mapper, state architecture, and parser
    - `Measurements/` - performance measurement harness using JDK Flight Recorder
  - `test/` - JUnit tests for transformation correctness
- `JsonExamples/`
  - `SpecificationFiles/` - example transformation specifications
  - `InputData/` - sample JSON input files
  - `Evaluation/` - measurement and evaluation specs, expected outputs, results
- `evaluateAll.ps1` - PowerShell script to run full evaluation workload
- `evaluateCopyAndMove.ps1` - PowerShell script for copy/move workload evaluation
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

Adjust JAVA_HOME and $env:Path in the corresponding script before usage.

Example script usage:

```powershell
./evaluateAll.ps1
```

This runs measurement scenarios across multiple specification and input sizes, producing results in `JsonExamples/Evaluation/`.

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

