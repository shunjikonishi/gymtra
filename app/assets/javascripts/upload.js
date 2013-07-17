if (typeof fullfull == "undefined") fullfull = {};

fullfull.Uploader = function() {
	var prepareForm = $("#prepareForm");
	var uploadForm = $("#uploadForm");
	$("#btnPrepare").click(function() {
console.log("test1");
		var data = {};
		prepareForm.find(":input").each(function() {
			var $el = $(this);
			var name = $el.attr("name");
console.log("test2: " + name);
			if ($el.attr("type") == "radio") {
				if ($el.is(":checked")) {
					data[name] = $el.val();
				}
			} else {
				data[name] = $el.val();
			}
		});
		data.filename = $("#file").val();
console.log("test3: " + JSON.stringify(data));
		if (!validate(data)) {
			return;
		}
		$.ajax({
			"url" : "/videos/prepareUpload",
			"type" : "post",
			"data" : data,
			"success" : function(data) {
console.log("test4: " + JSON.stringify(data));
				uploadForm.find("input[name='key']").val("videos/" + data.key);
				uploadForm.find("input[name='AWSAccessKeyId']").val(data.accessKey);
				uploadForm.find("input[name='policy']").val(data.policy);
				uploadForm.find("input[name='signature']").val(data.signature);
				uploadForm.find("input[name='Content-Type']").val(data.contentType);
				
				uploadForm[0].submit();
			}
		});
	});
	function validate(data) {
		return true;
	}
}