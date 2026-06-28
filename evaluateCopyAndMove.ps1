# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.
$tests = @(
    @{ Spec = "specCopySrcFirstGiant";  Input = "evaluationInputGiant" },
    @{ Spec = "specCopySrcFirstVeryBig"; Input = "evaluationInputVeryBig" },
    @{ Spec = "specCopySrcFirstBig";    Input = "evaluationInputBig" },
    @{ Spec = "specCopySrcFirstMid";    Input = "evaluationInputMid" },
    @{ Spec = "specCopySrcFirstSmall";  Input = "evaluationInputSmall" },

    @{ Spec = "specCopyDestFirstGiant";  Input = "evaluationInputGiant" },
    @{ Spec = "specCopyDestFirstVeryBig"; Input = "evaluationInputVeryBig" },
    @{ Spec = "specCopyDestFirstBig";    Input = "evaluationInputBig" },
    @{ Spec = "specCopyDestFirstMid";    Input = "evaluationInputMid" },
    @{ Spec = "specCopyDestFirstSmall";  Input = "evaluationInputSmall" },

    @{ Spec = "specMoveSrcFirstGiant";  Input = "evaluationInputGiant" },
    @{ Spec = "specMoveSrcFirstVeryBig"; Input = "evaluationInputVeryBig" },
    @{ Spec = "specMoveSrcFirstBig";    Input = "evaluationInputBig" },
    @{ Spec = "specMoveSrcFirstMid";    Input = "evaluationInputMid" },
    @{ Spec = "specMoveSrcFirstSmall";  Input = "evaluationInputSmall" },

    @{ Spec = "specMoveDestFirstGiant";  Input = "evaluationInputGiant" },
    @{ Spec = "specMoveDestFirstVeryBig"; Input = "evaluationInputVeryBig" },
    @{ Spec = "specMoveDestFirstBig";    Input = "evaluationInputBig" },
    @{ Spec = "specMoveDestFirstMid";    Input = "evaluationInputMid" },
    @{ Spec = "specMoveDestFirstSmall";  Input = "evaluationInputSmall" }
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path = "$env:JAVA_HOME\bin;$env:Path"

foreach ($test in $tests) {

    $spec = $test.Spec
    $input = $test.Input

    Write-Host ""
    Write-Host "========================================"
    Write-Host "Running: $spec with $input"
    Write-Host "========================================"

    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
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