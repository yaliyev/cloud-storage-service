terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = "us-east-1"
  access_key                  = "mock_access_key"
  secret_key                  = "mock_secret_key"

  # Bypass AWS account/metadata checks
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  # Required for LocalStack S3 routing
  s3_use_path_style           = true

  # Redirect all AWS calls to LocalStack
  endpoints {
    s3  = "http://localhost:4566"
    sqs = "http://localhost:4566"
  }
}