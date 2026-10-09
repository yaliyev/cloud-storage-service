# 1. Declare the SQS Event Queue
resource "aws_sqs_queue" "file_events" {
  name = "file-events-queue"
}

# 2. Declare the S3 Bucket
resource "aws_s3_bucket" "storage_bucket" {
  bucket        = "my-test-bucket"
  force_destroy = true
}

# 3. Configure S3 -> SQS Event Notification
resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.storage_bucket.id

  queue {
    queue_arn     = aws_sqs_queue.file_events.arn
    events        = ["s3:ObjectCreated:*"]
  }
}