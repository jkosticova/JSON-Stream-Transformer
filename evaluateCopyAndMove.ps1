# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.
$tests = @(    
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_130elements" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_1000elements" },
@{ Spec = "specCopySrcFirst";    Input = "evaluationInput_7500elements" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_130elements" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_1000elements" },
@{ Spec = "specMoveSrcFirst";    Input = "evaluationInput_7500elements" }


#    @{ Spec = "specCopySrcFirstGiant";  Input = "evaluationInputGiant" },
#    @{ Spec = "specCopySrcFirstVeryBig"; Input = "evaluationInputGiant" },
#    @{ Spec = "specCopySrcFirstBig";    Input = "evaluationInputGiant" },
#    @{ Spec = "specCopySrcFirstMid";    Input = "evaluationInputGiant" },
#    @{ Spec = "specCopySrcFirstSmall";  Input = "evaluationInputGiant" },

#    @{ Spec = "specCopyDestFirstGiant";  Input = "evaluationInputGiant" },
#    @{ Spec = "specCopyDestFirstVeryBig"; Input = "evaluationInputGiant" },
#    @{ Spec = "specCopyDestFirstBig";    Input = "evaluationInputGiant" },
#    @{ Spec = "specCopyDestFirstMid";    Input = "evaluationInputGiant" },
#    @{ Spec = "specCopyDestFirstSmall";  Input = "evaluationInputGiant" },

#    @{ Spec = "specMoveSrcFirstGiant";  Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveSrcFirstVeryBig"; Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveSrcFirstBig";    Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveSrcFirstMid";    Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveSrcFirstSmall";  Input = "evaluationInputGiant" },

#    @{ Spec = "specMoveDestFirstGiant";  Input = "evaluationInputGiant" }
#    @{ Spec = "specMoveDestFirstVeryBig"; Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveDestFirstBig";    Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveDestFirstMid";    Input = "evaluationInputGiant" },
#    @{ Spec = "specMoveDestFirstSmall";  Input = "evaluationInputGiant" }
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path = "$env:JAVA_HOME\bin;$env:Path"


foreach ($test in $tests) {

    $spec = $test.Spec
    $input = $test.Input

    Write-Host "Running: baseline with $input"
    
    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            Measurements.Main `
            "JsonExamples\$input.json"

    Write-Host ""
    Write-Host "========================================"
    Write-Host "Running: $spec with $input"
    Write-Host "========================================"

    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:TLABSize=2k -XX:-ResizeTLAB `
        -cp "out\production\code;target\dependency\*;target\classes" `
        Measurements.Main `
        "JsonExamples\Evaluation\$spec.json" `
        "JsonExamples\$input.json"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Execution failed for $spec with $input"
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "All measurements completed."