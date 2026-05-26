output "instance_id" {
  value = aws_instance.this.id
}

output "public_ip" {
  value = aws_instance.this.public_ip
}

output "instance_role_name" {
  value = aws_iam_role.ec2.name
}

output "instance_arn" {
  value = aws_instance.this.arn
}
