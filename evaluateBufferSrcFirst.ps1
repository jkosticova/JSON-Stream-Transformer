# Memory allocations of buffer-class transformations 
# for variable size of buffered data (src-first scenario)

# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.

$tests = @(    
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements0130" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements1000" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements2500" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements5000" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements7500" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements10000" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_elements15000" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements0130" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements1000" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements2500" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements5000" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements7500" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements10000" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_elements15000" }
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path = "$env:JAVA_HOME\bin;$env:Path"


foreach ($test in $tests) {

    $spec = $test.Spec
    $input = $test.Input

   
    Write-Host "Running: baseline with $input"   
    
    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            measurement.Main `
            "measurements\inputs\bufferSrcFirst\$input.json"
  
  
    Write-Host "Running: $spec with $input"  

    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:TLABSize=2k -XX:-ResizeTLAB `
        -cp "out\production\code;target\dependency\*;target\classes" `
        measurement.Main `
        "measurements\specifications\basic\$spec.json" `
        "measurements\inputs\bufferSrcFirst\$input.json"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Execution failed for $spec with $input"
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "All measurements completed."