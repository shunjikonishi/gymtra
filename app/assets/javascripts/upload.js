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
				form.find("input[name='key']").val("videos/" + data.key);
				form.find("input[name='AWSAccessKeyId']").val(data.accessKey);
				form.find("input[name='policy']").val(data.policy);
				form.find("input[name='signature']").val(data.signature);
				form.find("input[name='Content-Type']").val(data.contentType);
				
				form[0].submit();
			}
		});
	});
	function validate(data) {
		return true;
	}
}