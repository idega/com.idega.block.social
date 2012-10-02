var LastMessagesList = {};

LastMessagesList.CREATOR_CONTENT_ID = "creator-content";
LastMessagesList.createConversationPreview = function(jQueryElements,presentationOptions, receivers){
	jQueryElements.fancybox({
		width :		windowinfo.getWindowWidth() * 0.5,
		height : 	windowinfo.getWindowHeight() * 0.8,
		autoSize:	false,
		content : '<div id="' + LastMessagesList.CREATOR_CONTENT_ID +'" style="width:100%; height:100%;"/>',
		afterShow : function(){LastMessagesList.getPostCreator(presentationOptions, receivers);}
	});
}


LastMessagesList.getPostCreator = function(presentationOptions,receivers){
	showLoadingMessage("");
	var content = jQuery("#" + LastMessagesList.CREATOR_CONTENT_ID );
	SocialServices.getMessageCreator(presentationOptions,receivers,{
		callback : function(reply){
			if(reply.status != "OK"){
				// Actions for saving failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			// Should be rendered like component, but it don't work somehow
			content.append(reply.content);
//			IWCORE.insertRenderedComponent(reply.content,{
//				container: content,
//				append: true
//			});
			closeAllLoadingMessages();
			return;
		},
		errorHandler:function(message) {
			closeAllLoadingMessages();
			alert(message);
		}
	});
}

