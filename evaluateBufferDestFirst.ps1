# Memory allocations of buffer-class transformations 
# for variable size of buffered data (dest-first scenario)

# This script was generated with the assistance of OpenAI ChatGPT.
# It automates the execution of evaluation measurements for multiple
# specification and input file combinations in the JSON transformation prototype.
$input = "evaluationInput_5_24666543"  

$specs = @(    
    "specCopyDestFirst_elements7",
    "specCopyDestFirst_elements130",
    "specCopyDestFirst_elements2800",
    "specCopyDestFirst_elements11400",
    "specCopyDestFirst_elements28400",
    "specMoveDestFirst_elements7",
    "specMoveDestFirst_elements130",
    "specMoveDestFirst_elements2800",
    "specMoveDestFirst_elements11400",
    "specMoveDestFirst_elements28400"
)

setx JAVA_HOME "c:\Users\kosticova\.jdks\openjdk-21.0.2"

$env:Path = "$env:JAVA_HOME\bin;$env:Path"



Write-Host "Running: baseline with $input"


    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch `
            -cp "target\classes;out\production\code;target\dependency\*" `
            measurement.Main `
            "measurements\inputs\basic\$input.json"

foreach ($spec in $specs) {
    
    
    Write-Host "Running: $spec with $input"    

    java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:TLABSize=2k -XX:-ResizeTLAB `
        -cp "out\production\code;target\dependency\*;target\classes" `
        measurement.Main `
        "measurements\specifications\bufferDestFirst\$spec.json" `
        "measurements\inputs\basic\$input.json"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Execution failed for $spec with $input"
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "All measurements completed."