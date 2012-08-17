PublicPostViewerHelper = {};

// Implemented by component
PublicPostViewerHelper.setDefaultValues = function(){}

PublicPostViewerHelper.createPost = function(buttonSelector,uploaderSelector,resourcePathSelector){
	showLoadingMessage("");
	var form = jQuery(buttonSelector).parent();
	var parameters = form.serializeArray();
	
	var map = {};
	for(var i = 0;i < parameters.length;i++){
		var element = parameters[i];
		if(map[element.name] == undefined){
			map[element.name] = [];
		}
		map[element.name].push(element.value);
	}
	
	SocialServices.savePublicPost(map,{
		callback : function(reply){
			if(reply.status != "OK"){
				// Actions for saving failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			jQuery('head').trigger("prepend-posts");
			PublicPostViewerHelper.setDefaultValues();
			var uploader = jQuery(uploaderSelector);
			uploader.trigger('clear-downloads');
			jQuery(resourcePathSelector).val(reply.newResourcePath);
			uploader.fileupload('option',{
				    'formData' : [{
				        name: 'idega-blueimp-upload-path',
				        value: reply.newUploadPath
				        }]
			});
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply.message);
			return;
		},
		errorHandler:function(message) {
			closeAllLoadingMessages();
			alert(message);
		}
	});
}