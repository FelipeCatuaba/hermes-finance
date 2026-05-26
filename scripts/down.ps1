param(
  [string]$AwsRegion = "us-east-1",
  [string]$TerraformEnvDir = ".\infra\terraform\environments\dev",
  [string]$RdsIdentifier = ""
)

$ErrorActionPreference = "Stop"

if ($TerraformEnvDir -notmatch "[\\/](dev)$" -and $TerraformEnvDir -notmatch "[\\/]dev[\\/]?") {
  throw "This script is restricted to DEV environment only. Current TerraformEnvDir: $TerraformEnvDir"
}

Write-Host "Reading Terraform outputs from $TerraformEnvDir..."
$ec2InstanceId = terraform -chdir="$TerraformEnvDir" output -raw ec2_instance_id
if ([string]::IsNullOrWhiteSpace($RdsIdentifier)) {
  try {
    $RdsIdentifier = terraform -chdir="$TerraformEnvDir" output -raw rds_instance_identifier
  }
  catch {
    Write-Host "Output rds_instance_identifier not found in state. Use -RdsIdentifier to inform manually."
  }
}

Write-Host "Stopping EC2 instance: $ec2InstanceId"
aws ec2 stop-instances --region $AwsRegion --instance-ids $ec2InstanceId --output table | Out-Host

if (-not [string]::IsNullOrWhiteSpace($RdsIdentifier)) {
  Write-Host "Stopping RDS instance: $RdsIdentifier"
  aws rds stop-db-instance --region $AwsRegion --db-instance-identifier $RdsIdentifier --output table | Out-Host
} else {
  Write-Host "RDS stop skipped because no identifier was provided."
}

Write-Host "Done. EC2 and RDS stop requests were sent."
