var PostCreator = {};

// Implemented by component
PostCreator.setDefaultValues = function(){}

PostCreator.createPost = function(formSelector,uploaderSelector,resourcePathSelector,theCallback){
	showLoadingMessage("");
	var form = jQuery(formSelector);
	var parameters = form.serializeArray();
	
	var map = {};
	for(var i = 0;i < parameters.length;i++){
		var element = parameters[i];
		if(map[element.name] == undefined){
			map[element.name] = [];
		}
		map[element.name].push(element.value);
	}
	
	SocialServices.savePost(map,{
		callback : function(reply){
			if(reply.status != "OK"){
				// Actions for saving failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			var post = reply.post;
			if((theCallback == undefined) || (theCallback == null)){
				// Default
				PostCreator.afterPostSent();
			}else{
				theCallback = "var clbck = function(post){" + theCallback + "}; clbck(" + post + ");";
				eval(theCallback);
			}
			PostCreator.setDefaultValues();
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
PostCreator.afterPostSent = function(){
	jQuery('head').trigger("prepend-posts");
}
