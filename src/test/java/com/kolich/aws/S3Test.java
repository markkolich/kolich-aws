package com.kolich.aws;

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kolich.aws.services.s3.S3Client;
import com.kolich.aws.services.s3.impl.KolichS3Client;
import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.option.Option;
import com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory;
import com.kolich.http.common.response.HttpFailure;

public class S3Test {
	
	private static final String AWS_ACCESS_KEY_PROPERTY = "aws.key";
    private static final String AWS_SECRET_PROPERTY = "aws.secret";

	public static void main(String[] args) {

		final String key = System.getProperty(AWS_ACCESS_KEY_PROPERTY);
		final String secret = System.getProperty(AWS_SECRET_PROPERTY);
		if (key == null || secret == null) {
			throw new IllegalArgumentException("You are missing the "
				+ "-Daws.key and -Daws.secret required VM "
				+ "properties on your command line.");
		}

		final HttpClient client = KolichHttpClientFactory.getNewInstanceNoProxySelector();

		final S3Client s3 = new KolichS3Client(client, key, secret);

		final Option<HttpFailure> bucket = s3.createBucket("foobar.kolich.local");
		if (bucket.isNone()) {
			System.out.println("created successfully.");
		} else {
			System.err.println(bucket.get().getStatusCode());
		}
		
		final Either<HttpFailure,List<Bucket>> list = s3.listBuckets();
		if(list.success()) {
			for(final Bucket b : list.right()) {
				System.out.println("Bucket: " + b.getName());
			}
		} else {
			System.err.println("Failed to list buckets!");
		}
		
		final Either<HttpFailure,PutObjectResult> put = s3.putObject(
			"foobar.kolich.local",
			getBytesUtf8("zomg it works!"),
			"test", "foo", "bar/kewl", "test.txt");
		if(put.success()) {
			System.out.println("Put object worked!!");
		}
		
		final boolean exists = s3.objectExists("foobar.kolich.local",
			"test", "foo", "bar/kewl", "test.txt");
		if(exists) {
			System.out.println("Object confirmed exists!");
		}
		final boolean exists2 = s3.objectExists("foobar.kolich.local",
			"bogus");
		if(!exists2) {
			System.out.println("Bogus object confirmed missing.");
		}
		
		final Either<HttpFailure,ObjectListing> objList =
			s3.listObjects("foobar.kolich.local");
		if(objList.success()) {
			for(final S3ObjectSummary o : objList.right().getObjectSummaries()) {
				System.out.println("Object: " + o.getKey());
			}
		}
		
		final Option<HttpFailure> delete = s3.deleteObject(
			"foobar.kolich.local",
			"test", "foo", "bar/kewl", "test.txt");
		if(delete.isNone()) {
			System.out.println("Delete object worked too!");
		} else {
			System.out.println("Delete object failed: " +
				delete.get().getStatusCode());
		}
		
		/*
		try {
			final File f = new File("/home/mkolich/Desktop/foobar.pdf");
			final Either<HttpFailure,PutObjectResult> putLarge =
				s3.putObject("foobar.kolich.local",
					ContentType.APPLICATION_OCTET_STREAM,
					new FileInputStream(f), f.length(), f.getName());
			if(putLarge.success()) {
				System.out.println("Large upload worked!");
			}
		} catch (Exception e) {
			System.err.println("Large upload failed.");
		}
		*/
		
		final Option<HttpFailure> deleteBucket =
			s3.deleteBucket("foobar.kolich.local");
		if (deleteBucket.isNone()) {
			System.out.println("deleted bucket!");
		} else {
			System.err.println("Failed to delete bucket: " +
				deleteBucket.get().getStatusCode());
		}

	}

}
