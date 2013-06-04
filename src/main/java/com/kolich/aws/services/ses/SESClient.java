package com.kolich.aws.services.ses;

import java.util.List;

import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import com.amazonaws.services.simpleemail.model.GetSendStatisticsResult;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface SESClient {
	
	/**
	 * Verifies an email address. This action causes a confirmation
	 * email message to be sent to the specified address.
	 * @param emailAddress the email address to be verified
	 */
	public void verifyEmailAddress(final String emailAddress);
	
	/**
	 * Deletes the specified email address from the list of
	 * verified addresses.
	 * @param emailAddress the email address to be deleted
	 */
	public void deleteVerifiedEmailAddress(final String emailAddress);
	
	/**
	 * Returns a list containing all of the email addresses that
	 * have been verified.
	 * @return the email addresses that have been verified.
	 */
	public ListVerifiedEmailAddressesResult listVerifiedEmailAddresses();
	
	/**
	 * Returns the user's current sending limits.
	 * @return the user's email sending limits
	 */
	public GetSendQuotaResult getSendQuota();

	/**
	 * Returns the user's sending statistics. The result is a list of
	 * data points, representing the last two weeks of sending activity.
	 * each data point in the list contains statistics for a 15-minute
	 * interval.
	 * @return the user's sending statistics
	 */
	public GetSendStatisticsResult getSendStatistics();
	
	/**
	 * A convenience method to send a plain-text UTF-8 encoded email
	 * message.  This method converts the input data into a model/data
	 * structure understood by SES before sending the message so that
	 * the caller does not have to worry about those details.  If the
	 * caller needs finer control over the message content, destinations,
	 * bounce back return paths, etc. then they should not use this method
	 * but one of the the more finer grained methods.
	 * @param from
	 * @param to
	 * @param returnPath
	 * @param subject
	 * @param body
	 * @return
	 */
	public SendEmailResult sendEmail(final String from, final String to,
		final String returnPath, final String subject, final String body);
	
	/**
	 * Composes an email message based on input data, and then
	 * immediately queues the message for sending.
	 * @param destination the destination for this email, composed of
	 * To:, CC:, and BCC: fields.
	 * @param message the message to be sent
	 * @param replyToAddresses The reply-to email address(es) for the
	 * message. If the recipient replies to the message, each reply-to
	 * address will receive the reply.
	 * @param returnPath the email address to which bounce notifications
	 * are to be forwarded. If the message cannot be delivered to the
	 * recipient, then an error message will be returned from the
	 * recipient's ISP; this message will then be forwarded to the email
	 * address specified by the ReturnPath parameter
	 * @param senderAddress the sender's email address
	 * @return
	 */
	public SendEmailResult sendEmail(final Destination destination,
		final Message message, final List<String> replyToAddresses,
			final String returnPath, final String senderAddress);
	
	/**
	 * Sends an email message, with header and content specified by
	 * the caller. The SendRawEmail action is useful for sending
	 * multipart MIME emails. The raw text of the message must comply
	 * with Internet email standards; otherwise, the message cannot be sent.
	 * @param destinations a list of destination addresses for the message
	 * @param message the raw message. The client is responsible
	 * for ensuring the following: message must contain a header and a body,
	 * separated by a blank line. All required header fields must be present.
	 * Each part of a multipart MIME message must be formatted properly.
	 * MIME content types must be among those supported by Amazon SES. Refer
	 * to the Amazon SES Developer Guide for more details. Content must be
	 * base64-encoded, if MIME requires it.
	 * @param senderAddress the sender's email address
	 * @return
	 */
	public SendRawEmailResult sendRawEmail(final RawMessage message,
		final String senderAddress, final List<String> destinations);
		
}
