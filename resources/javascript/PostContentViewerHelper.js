/**
 * 
 */

var PostContentViewerHelper = {};

PostContentViewerHelper.createAutoresizing = function(mainSelector, selector){
	jQuery(document).ready(function(){
		jQuery(mainSelector).find(selector).autoResize({extraSpace : 5, animate : false });
	});
}

PostContentViewerHelper.savePost = function(buttonSelector){
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
	
	SagaServices.savePost(map,{
		callback: function(reply){
			humanMsg.displayMsg(reply);
		}
	});
}

PostContentViewerHelper.createPostPreviews = function(){
	jQuery(".post-content-viewer-post-preview").fancybox();
}

