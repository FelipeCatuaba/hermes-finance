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
    throw "Output rds_instance_identifier not found in state. Run terraform apply first or pass -RdsIdentifier."
  }
}

Write-Host "Starting RDS instance: $RdsIdentifier"
aws rds start-db-instance --region $AwsRegion --db-instance-identifier $RdsIdentifier --output table | Out-Host

Write-Host "Waiting for RDS to become available..."
aws rds wait db-instance-available --region $AwsRegion --db-instance-identifier $RdsIdentifier

Write-Host "Starting EC2 instance: $ec2InstanceId"
aws ec2 start-instances --region $AwsRegion --instance-ids $ec2InstanceId --output table | Out-Host

Write-Host "Done. RDS is available and EC2 start request was sent."
