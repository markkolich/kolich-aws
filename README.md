# kolich-aws

A Java 7+ client for the Amazon Web-Services (AWS) API &mdash; currently supports S3, SQS, and SES.

## @Deprecated

This is no longer maintained.

Many moons ago, I wrote and released this because Amazon's own `aws-sdk-java` was using a deprecated Apache Commons `HttpClient` version 3.x.  I wanted to use `HttpClient` 4.x.
  
Amazon has since updated `aws-sdk-java` to use `HttpClient` 4.x.
  
So, there's no point in using this library anymore &mdash; just use `aws-sdk-java` directly as provided by Amazon.
