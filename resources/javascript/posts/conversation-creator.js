
var ConversationCreator = {};

ConversationCreator.CREATOR_CONTENT_ID = "creator-content";

ConversationCreator.initialize = function(id,presentationOptions,receiversFunction){
	var creator = jQuery('#'+id);
	var link = jQuery("<a></a>");
	creator.parent().append(link);
	link.fancybox({
		width :		windowinfo.getWindowWidth() * 0.5,
		height : 	windowinfo.getWindowHeight() * 0.8,
		autoSize:	false,
		content : '<div id="' + ConversationCreator.CREATOR_CONTENT_ID +'" style="width:100%; height:100%;"/>',
		afterShow : function(){ConversationCreator.createConversation(presentationOptions, receiversFunction);}
	});
	creator.click({link : link,receiversFunction : receiversFunction},function(e){
		var receivers = e.data.receiversFunction();
		if(receivers.length < 1){
			return;
		}
		e.data.link.click();
	});
}
ConversationCreator.createConversation = function(presentationOptions,receiversFunction){
	var receivers = receiversFunction();
	var content = jQuery("#" + ConversationCreator.CREATOR_CONTENT_ID );
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