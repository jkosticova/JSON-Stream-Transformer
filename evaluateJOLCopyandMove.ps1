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

# Define the file where memory stats will be written
$reportFile = "memory_evaluation_results.csv"

# Initialize the file with headers if it doesn't exist yet
if (-not (Test-Path $reportFile)) {
    New-Item -Path $reportFile -ItemType File -Force
    Add-Content -Path $reportFile -Value "Specification,InputFile,PeakMemoryBytes"
}

foreach ($input in $inputFiles) {

    Write-Host ""
    Write-Host "========================================"
    Write-Host "Input file: $input"
    Write-Host "========================================"

    foreach ($spec in $specifications) {

        Write-Host "Running: $spec with $input"

        # Capturing the execution console output into a variable array
        $output = java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
                              '-Djdk.attach.allowAttachSelf=true' `
                              '-XX:+EnableDynamicAgentLoading' `
                              -cp "target/classes;out/production/code;target/dependency/*" `
                              Prototype.Main `
                              "JsonExamples\Evaluation\$spec.json" `
                              "JsonExamples\$input.json" `
                              "JsonExamples\output\$input.json"

        if ($LASTEXITCODE -ne 0) {
            Write-Host "Execution failed for $spec with $input"
            exit $LASTEXITCODE
        }

        # Filter out our memory trace from Java's output stream
        foreach ($line in $output) {
            if ($line -like "EXPORT,*") {
                # Trim the "EXPORT," tag (7 characters) to leave clean comma-separated values
                $csvData = $line.Substring(7)

                # Append the values right into your final csv file
                Add-Content -Path $reportFile -Value $csvData
                Write-Host "Log saved: $csvData" -ForegroundColor Green
            }
        }
    }
}

Write-Host ""
Write-Host "All measurements completed. Final data stored in: $reportFile"