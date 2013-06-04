package com.kolich.aws.services.s3;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.http.Header;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.kolich.common.either.Either;
import com.kolich.common.util.URLEncodingUtils;
import com.kolich.http.common.response.HttpFailure;

public interface S3Client {
		
	/**
	 * List all buckets.
	 * @return a list of {@link Bucket} objects
	 */
	public Either<HttpFailure,List<Bucket>> listBuckets();
	
	/**
	 * List all objects inside of a bucket.  Note that the caller should always
	 * assume the keys contained in the resulting {@link ObjectListing} may be
	 * URL-encoded, and therefore, the caller should call
	 * {@link URLEncodingUtils#urlDecode(String)} on each key to obtain its
	 * "real" unencoded String value.
	 * @param bucketName the name of the bucket
	 * @param marker the pagination marker, may be null
	 * @param path limits the response to keys that begin with
	 * the specified path. The path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName,
		final String marker, final String... path);
	
	public Either<HttpFailure,ObjectListing> listObjects(final String bucketName);
	
	/**
	 * Create a bucket.
	 * @param bucketName the name of the bucket
	 * @return a {@link Bucket} object
	 */
	public Either<HttpFailure,Bucket> createBucket(final String bucketName);

	/**
	 * Without the "recursive" parameter, we don't try to delete the
	 * contents first, and we will immediately fail if it is not empty.
	 * @param bucketName the name of the bucket to delete
	 */
	public Either<HttpFailure,Void> deleteBucket(final String bucketName);
	
	/**
	 * Put an object into a bucket.
	 * @param bucketName the name of the bucket
	 * @param type the {@link AwsContentType} of the object
	 * @param object the byte[] array representing the object iself
	 * @param rrs set to true to suggest the storage engine use
	 * reduced redundancy storage mode. Set to false to use a standard
	 * storage mode
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 * @return a {@link PutObjectResult} object that contains metadata
	 * about the object flushed to S3
	 */
	public Either<HttpFailure,PutObjectResult> putObject(final String bucketName,
		final Header[] headers, final boolean rrs, final InputStream input,
		final long contentLength, final String... path);
	
	/**
	 * Delete an object.
	 * @param bucketName the name of the bucket
	 * @param name the name (key) of the object in the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Either<HttpFailure,Void> deleteObject(final String bucketName,
		final String... path);
	
	/**
	 * Get an object.
	 * @param bucketName the name of the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 */
	public Either<HttpFailure,List<Header>> getObject(final String bucketName,
		final OutputStream destination, final String... path);
	
	public Either<HttpFailure,byte[]> getObject(final String bucketName,
		final String... path);
		
	/**
	 * Check if an object exists.
	 * @param bucketName the name of the bucket
	 * @param path path are joined using a standard
	 * "/" path separator then properly URL encoded to produce a unique
	 * path to a key in the bucket
	 * @return true if an object with the given path (key) exists in
	 * the bucket, false if it does not
	 */
	public boolean objectExists(final String bucketName,
		final String... path);
	
}
