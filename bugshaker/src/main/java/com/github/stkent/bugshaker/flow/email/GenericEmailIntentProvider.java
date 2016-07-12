/**
 * Copyright 2016 Stuart Kent
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.bugshaker.flow.email;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

public final class GenericEmailIntentProvider {

	@NonNull
	Intent getEmailIntent(
		@NonNull final String[] emailAddresses,
		@NonNull final String emailSubjectLine,
		@NonNull final String emailBody) {


		final Intent result = new Intent(Intent.ACTION_SEND_MULTIPLE);
		result.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
		result.setType("message/rfc822");

		//	result.setData(Uri.parse("mailto:abc@gmail.com"));
		result.putExtra(Intent.EXTRA_EMAIL, emailAddresses);
		result.putExtra(Intent.EXTRA_SUBJECT, emailSubjectLine);
		result.putExtra(Intent.EXTRA_TEXT, emailBody);
		return result;
	}

	@NonNull
	Intent getEmailWithAttachmentIntent(
		@NonNull final String[] emailAddresses,
		@NonNull final String emailSubjectLine,
		@NonNull final String emailBody,
		@NonNull final Uri attachmentUri)
	{

		final Intent result = getEmailIntent(emailAddresses, emailSubjectLine, emailBody);

		result.putExtra(Intent.EXTRA_STREAM, attachmentUri);

		return result;
	}

	@NonNull
	Intent getEmailWithAttachmentIntent(
		@NonNull final String[] emailAddresses,
		@NonNull final String emailSubjectLine,
		@NonNull final String emailBody,
		@NonNull final Uri attachmentUri,
		@NonNull final Uri attachmentUri2
	) {

		final Intent result = getEmailIntent(emailAddresses, emailSubjectLine, emailBody);


//		result.putExtra(Intent.EXTRA_STREAM, attachmentUri);
//		result.putExtra(Intent.EXTRA_STREAM, attachmentUri2);

		//result.putExtra(Intent.EXTRA_STREAM, attachmentUri);
		//result.setType("image/jpeg");

		ArrayList<Uri> uris = new ArrayList<Uri>();
		uris.add(attachmentUri);
		uris.add(attachmentUri2);

		result.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		//setResult(RESULT_OK, result);
		//finish();



		return result;
	}

}
