package models;

import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;

case class UploadInfo(
	filename: String,
	key: String,
	accessKey: String,
	policy: String,
	signature: String,
	contentType: String
)

object AWSManager {
	
	val ACCESS_KEY = sys.env("AWSAccessKeyId");
	val SECRET_KEY = sys.env("AWSSecretKey");
	
	val POLICY = """{"expiration" : "%s","conditions":[
{"bucket":"%s"},
{"starts-with","$key","videos/"},
{"acl":"private"},
{"success_action_redirect":"%s"},
["starts-with","$Content-Type",""],
["content-length-range",0,1048576000]]}""";

}

class AWSManager(accessKey: String, secretKey: String, bucket: String) {

	import AWSManager.POLICY;
	
	def prepare(filename: String, redirectUri: String): UploadInfo = {
		val key = UUID.randomUUID;
		val policy = POLICY.format(
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(System.currentTimeMillis + 60 * 60 * 1000)),
			bucket,
			redirectUri
		);
		val signature = sign(policy);
		val ext = filename.substring(filename.lastIndexOf('.'));
		val contentType = getContentType(ext);

		UploadInfo(
			filename,
			key + ext,
			accessKey,
			policy,
			signature,
			contentType);
	}

	private def getContentType(ext: String) = {
		"videos/mpeg";
	}

	private def sign(str: String) = {
		str;
	}
}