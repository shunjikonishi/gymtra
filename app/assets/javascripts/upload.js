if (typeof fullfull == "undefined") fullfull = {};

fullfull.Uploader = function(formId) {
	function fileChanged() {
		var filename = $(this).val();
		if (filename) {
			$.ajax({
				"url" : "/videos/prepareUpload",
				"type" : "post",
				"data" : {
					"filename" : filename
				},
				"success" : function(data) {
					console.log(JSON.stringify(data));
					form.find("input[name='key']").val("videos/" + data.key);
					form.find("input[name='AWSAccessKeyId']").val(data.accessKey);
					form.find("input[name='policy']").val(data.policy);
					form.find("input[name='signature']").val(data.signature);
					form.find("input[name='Content-Type']").val(data.contentType);
					
					form[0].submit();
				}
			});
		}
	}
	var form = $(formId);
	form.find("input[name='file']").change(fileChanged);
	
}