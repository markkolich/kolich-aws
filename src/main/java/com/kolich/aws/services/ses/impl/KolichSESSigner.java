package com.kolich.aws.services.ses.impl;

import static com.kolich.aws.signing.impl.KolichAwsSigner.AwsSigningAlgorithm.HmacSHA256;
import static com.kolich.aws.transport.AwsHeaders.X_AMZN_AUTHORIZATION;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.DATE;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;

import java.util.Date;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import com.kolich.aws.services.AbstractAwsSigner;
import com.kolich.aws.signing.AwsCredentials;
import com.kolich.aws.signing.AwsSigner;
import com.kolich.aws.signing.impl.KolichAwsSigner;
import com.kolich.aws.transport.AwsHttpRequest;
import com.kolich.common.date.RFC822DateFormat;

public final class KolichSESSigner extends AbstractAwsSigner {
	
	private static final String APPLICATION_FORM_URLENCODED_TYPE =
		APPLICATION_FORM_URLENCODED.toString();
	
	private static final String SES_AWS3_HTTPS = "AWS3-HTTPS";
	private static final String SES_ACCESSKEY_ID = "AWSAccessKeyId";
	private static final String SES_ALGORITHM = "Algorithm";
	private static final String SES_SIGNATURE = "Signature";
	
	public KolichSESSigner(final AwsCredentials credentials,
		final AwsSigner signer) {
		super(credentials, signer);
	}
	
	public KolichSESSigner(final AwsCredentials credentials) {
		this(credentials, new KolichAwsSigner(HmacSHA256));
	}
	
	public KolichSESSigner(final String key, final String secret) {
		this(new AwsCredentials(key, secret));
	}

	@Override
	public void signHttpRequest(final AwsHttpRequest request)
		throws Exception {
		final String date = RFC822DateFormat.format(new Date());
		// Create a Date header to be used in the request.
		request.addHeader(DATE, date);
		// Only add a Content-Type header to the request if one is not
    	// already there.  AWS expects something useful here.
    	if(request.getFirstHeader(CONTENT_TYPE) == null) {
    		request.addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_TYPE);
    	}
		// Calculate an RFC 2104-compliant HMAC hash with the Date header
		// value, your Secret Access Key as the key, and SHA256 or SHA1 as
		// the hash algorithm. For more information, go to
		// http://www.ietf.org/rfc/rfc2104.txt. (Use only the value of
		// the header when calculating the hash; do not include the word
		// "Date", nor the trailing colon and space.)
    	final String signature = signer_.sign(credentials_, date);
    	// Append the signature to the request using the X-Amzn-Authorization
    	// request header. All of the elements, except for AWS3-HTTPS, must be
    	// separated by commas.
    	//   X-Amzn-Authorization: AWS3-HTTPS AWSAccessKeyId=<Your AWS Access Key ID>,
    	//     Algorithm=HmacSHA256, Signature=<Signature>
    	request.addHeader(X_AMZN_AUTHORIZATION,
    		String.format("%s %s=%s, %s=%s, %s=%s",
    			// AWS3-HTTPS
    			SES_AWS3_HTTPS,
    			// AWSAccessKeyId=your AWS Access Key ID.
    			SES_ACCESSKEY_ID, credentials_.getKey(),
    			// Algorithm=the algorithm you used when creating the
    			// string to sign—either HmacSHA1 or HmacSHA256.
    			SES_ALGORITHM, signer_.getAlgorithm().toString(),
    			// Signature=the signature for this request.
    			SES_SIGNATURE, signature
    			));
    	// Now that we've computed a signature and have signed the request
    	// go ahead and add all of the request parameters to the POST body.
    	((HttpPost)request.getRequestBase())
    		.setEntity(new UrlEncodedFormEntity(request.getParameters(),
    			UTF_8));
	}

}