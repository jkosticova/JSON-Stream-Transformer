# Memory allocations of 
# - stack-class transformations and 
# - buffer-class transformations for fixed size of buffered data

# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.

# List of specification names
$specifications = @(
    "specIdentity",
    "specRename",
    "specRemove",
    "specReplace",
    "specAdd"
    "specCopyDestFirst"
    "specCopySrcFirst",
    "specMoveDestFirst",
    "specMoveSrcFirst"
)

# List of input files
$inputFiles = @(
    "evaluationInput_1_6447",
    "evaluationInput_2_118149",
    "evaluationInput_3_2466657",
    "evaluationInput_4_9866619",
    "evanluationInput_5_24666543"
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path="$env:JAVA_HOME\bin;$env:Path"

foreach ($input in $inputFiles) {

    Write-Host ""
    Write-Host "========================================"
    Write-Host "Input file: $input"
    Write-Host "========================================"

    Write-Host "Running: baseline with $input"
    
    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            measurements.Main `
            "measurements\inputs\basic\$input.json"
    
    foreach ($spec in $specifications) {

        Write-Host "Running: $spec with $input"
        
        java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            measurements.Main `
            "measurements\specifications\basic\$spec.json" `
            "measurements\inputs\basic\$input.json"

        if ($LASTEXITCODE -ne 0) {
            Write-Host "Execution failed for $spec with $input"
            exit $LASTEXITCODE
        }        
    }
}

Write-Host ""
Write-Host "All measurements completed."