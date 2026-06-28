# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.

# List of specification names
$specifications = @(
    "specIdentity",
    "specRename",
    "specRemove",
    "specReplace",
    "specAdd",
    "specCopyDestFirst",
    "specCopySrcFirst",
    "specMoveDestFirst",
    "specMoveSrcFirst"
)

# List of input files
$inputFiles = @(
    "evaluationInputGiant",
    "evaluationInputVeryBig",
    "evaluationInputBig",
    "evaluationInputMid",
    "evaluationInputSmall"
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path="$env:JAVA_HOME\bin;$env:Path"

foreach ($input in $inputFiles) {

    Write-Host ""
    Write-Host "========================================"
    Write-Host "Input file: $input"
    Write-Host "========================================"

    foreach ($spec in $specifications) {

        Write-Host "Running: $spec with $input"
        

        java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            Measurements.Main `
            "JsonExamples\Evaluation\$spec.json" `
            "JsonExamples\$input.json"

        if ($LASTEXITCODE -ne 0) {
            Write-Host "Execution failed for $spec with $input"
            exit $LASTEXITCODE
        }
    }
}

Write-Host ""
Write-Host "All measurements completed."