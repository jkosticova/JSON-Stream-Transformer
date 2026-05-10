# Diplomovka

## Overview

This repository contains a Java-based prototype for JSON transformation and mapping, developed as part of a thesis implementation.

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

## Run

For JSON transformation with input from user.

```powershell
mvn exec:java@prototype
```

For the evaluation.

```powershell
mvn exec:java@measurements
```

### Interactive Usage

1. Start the application.
2. Provide the path to a specification JSON file when prompted.
3. Provide the path to an input JSON file.
4. Output is written to `JsonExamples/outputINPUT.json`, with INPUT being the name of the input file.

## Run Tests

```powershell
mvn test
```

## Evaluation / Measurements

Evaluation and benchmarking are supported by the `Measurements.Main` harness.

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

