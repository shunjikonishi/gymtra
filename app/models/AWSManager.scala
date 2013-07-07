package models;

object AWSManager {
	
	val ACCESS_KEY = sys.env("AWSAccessKeyId");
	val SECRET_KEY = sys.env("AWSSecretKey");
	
	val POLICY = """{"expiration" : "%s","conditions":[
{"bucket":"%s"},
{"starts-with","%s","%s"},
{"acl":"private"},
{"success_action_redirect":"%s"},
["starts-with","$Content-Type",""],
["content-length-range",0,1048576000]]}""";

}
