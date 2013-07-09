package models;

import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import play.api.libs.json.Json;

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
["starts-with","$key","videos/"],
{"acl":"private"},
{"success_action_redirect":"%s"},
["starts-with","$Content-Type",""],
["content-length-range",0,1048576000]]}""";

	def apply(bucket: String) = new AWSManager(ACCESS_KEY, SECRET_KEY, bucket);
	implicit val uploadInfoFormat = Json.format[UploadInfo];
}

class AWSManager(accessKey: String, secretKey: String, bucket: String) {

	import AWSManager.POLICY;

	private def generatePolicy(filename: String, redirectUri: String) = {
		val policy = POLICY.format(
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(System.currentTimeMillis + 60 * 60 * 1000)),
			bucket,
			redirectUri
		);
println("policy: " + policy);
		new String(Base64.encodeBase64(policy.getBytes("utf-8")), "utf-8");
	}
	
	def prepare(filename: String, redirectUri: String): UploadInfo = {
		val key = UUID.randomUUID;
		val policy = generatePolicy(filename, redirectUri);
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
		val hmac = Mac.getInstance("HmacSHA1");
		hmac.init(new SecretKeySpec(secretKey.getBytes("utf-8"), "HmacSHA1"));
		new String(Base64.encodeBase64(hmac.doFinal(str.getBytes("utf-8"))))
			.replaceAll("¥n", "");
	}
}