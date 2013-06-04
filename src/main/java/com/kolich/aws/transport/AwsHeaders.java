package com.kolich.aws.transport;

public interface AwsHeaders {
	
	public static final String AMAZON_PREFIX = "x-amz-";
	
	public static final String STORAGE_CLASS = "x-amz-storage-class";
	
	public static final String S3_VERSION_ID = "x-amz-version-id";	
	public static final String S3_ALTERNATE_DATE = "x-amz-date";	
	public static final String S3_STANDARD_REDUNDANCY = "STANDARD";
    public static final String S3_REDUCED_REDUNDANCY = "REDUCED_REDUNDANCY";
    
    public static final String X_AMZN_AUTHORIZATION = "X-Amzn-Authorization";

}