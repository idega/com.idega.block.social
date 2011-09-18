/**
 * 
 */

var PostContentViewerHelper = {};

PostContentViewerHelper.createAutoresizing = function(mainSelector, selector){
	jQuery(document).ready(function(){
		jQuery(mainSelector).find(selector).autoResize({extraSpace : 5, animate : false });
	});
}

PostContentViewerHelper.savePost = function(buttonSelector,textSelector){
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
	
	SocialServices.savePost(map,{
		callback: function(reply){
			humanMsg.displayMsg(reply);
			jQuery(textSelector).attr("value","");
			jQuery(textSelector).keyup();
			closeAllLoadingMessages();
		}
	});
}

PostContentViewerHelper.createPostPreviews = function(){
	jQuery(".post-content-viewer-post-preview-not-ready").fancybox().removeClass(".post-content-viewer-post-preview-not-ready");
}

PostContentViewerHelper.prepareAdvancedLink = function(selector){
	jQuery(selector).fancybox({
		hideOnContentClick: false
		,zoomSpeedIn: 300
		,zoomSpeedOut: 300
		,overlayShow: true
		,width: windowinfo.getWindowWidth() * 0.8
		,height: windowinfo.getWindowHeight() * 0.8
		,autoDimensions: false
		,onClosed : PostContentViewerHelper.addPosts
	});
}

PostContentViewerHelper.openAdvancedPostForm = function(selector){
	jQuery(selector).click();
}


PostContentViewerHelper.addPosts = function(postListSelector){
	var firstElement = jQuery("ul.posts-contents-list").children().filter(":first");
	var firstUri = firstElement.find(".post-uri-container").attr("value");
	SocialServices.getPosts(
			firstUri,
			true,
			PostContentViewerHelper.showPrivate,
			PostContentViewerHelper.showGroup,
			PostContentViewerHelper.showSent,
			{
		callback: function(list) {
			var listElements = jQuery(list).children();
			if((firstUri != undefined) && (firstUri != null)){
				listElements.splice(listElements.length - 1,1);
			}
			jQuery("ul.posts-contents-list").prepend(listElements);
		}
	});
}

PostContentViewerHelper.addPostsdown = function(postListSelector){
	var lastElement = jQuery("ul.posts-contents-list").children().filter(":last");
	var firstUri = lastElement.find(".post-uri-container").attr("value");
	showLoadingMessage("");
	SocialServices.getPosts(
			firstUri,
			false,
			PostContentViewerHelper.showPrivate,
			PostContentViewerHelper.showGroup,
			PostContentViewerHelper.showSent,
			10,
			{
		callback: function(list) {
			var listElements = jQuery(list).children();
			if((firstUri != undefined) && (firstUri != null)){
				listElements.splice(0,1);
			}
			jQuery("ul.posts-contents-list").append(listElements);
			PostContentViewerHelper.createPostPreviews();
			PostContentViewerHelper.loadMoreButtonNeeded();
			closeAllLoadingMessages();
		}
	});
}

PostContentViewerHelper.LOAD_BUTTON_SELECTOR = ".post-button-load-more";
PostContentViewerHelper.loadMoreButtonNeeded = function(){
	var lastElement = jQuery("ul.posts-contents-list").children().filter(":last");
	if(lastElement.length == 0){
		jQuery(PostContentViewerHelper.LOAD_BUTTON_SELECTOR).hide();
		return;
	}
	var firstUri = lastElement.find(".post-uri-container").attr("value");
	SocialServices.getPosts(
			firstUri,
			false,
			PostContentViewerHelper.showPrivate,
			PostContentViewerHelper.showGroup,
			PostContentViewerHelper.showSent,
			2,
			{
		callback: function(list) {
			var listElements = jQuery(list).children();
			if(listElements.length >= 2){ //becouse one will be same article that exists
				jQuery(PostContentViewerHelper.LOAD_BUTTON_SELECTOR).show();
			}else{
				jQuery(PostContentViewerHelper.LOAD_BUTTON_SELECTOR).hide();
			}
			closeAllLoadingMessages();
		}
	});
}

jQuery(document).ready(function(){
	jQuery( ".selector" ).bind( "tabsselect", function(event, ui) {
		  jQuery(".post-content-ciewer").empty();
	});
});

