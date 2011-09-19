/**
 * 
 */
jQuery(document).ready(function() {
	jQuery( ".whats-new-main" ).tabs();
	WhatsNewHeLper.createGroupPreview(jQuery(WhatsNewHeLper.GROUP_INFO_PREVIEW_LINKS_SELECTOR));
	
	var inputSelector = ".whats-new-group-search-input";
	jQuery(inputSelector).keyup(function(){
		WhatsNewHeLper.prepareSearch(inputSelector,".whats-new-group-search-results");
	});
});

var WhatsNewHeLper = {};

WhatsNewHeLper.GROUP_INFO_PREVIEW_LINKS_SELECTOR = ".whats-new-view-group-info-preview-link";
WhatsNewHeLper.SEARCH_DELAY = 300;
WhatsNewHeLper._searching = null;

WhatsNewHeLper.createGroupPreview = function(elements){
	elements.fancybox({
		width: windowinfo.getWindowWidth() * 0.5
		,height: windowinfo.getWindowHeight() * 0.7
		,autoDimensions: false
	});
}

WhatsNewHeLper.prepareSearch = function(inputSelector,layerSelector){
	clearTimeout(WhatsNewHeLper._searching);
	var request = jQuery(inputSelector).val();
	if((request == undefined) || (request == "")){
		return;
	}
	WhatsNewHeLper._searching = setTimeout(
			function(){
					WhatsNewHeLper.getSearchResults(layerSelector,request)
			},
			WhatsNewHeLper.SEARCH_DELAY
	);
}


WhatsNewHeLper.getSearchResults = function(layerSelector,request){
	showLoadingMessage("");
	jQuery(layerSelector).empty();
	SocialServices.getGroupSearchResults(request,10,{
		callback: function(response) {
			var list = jQuery(response);
			jQuery(layerSelector).append(list);
			WhatsNewHeLper.createGroupPreview(list.find(WhatsNewHeLper.GROUP_INFO_PREVIEW_LINKS_SELECTOR));
			closeAllLoadingMessages();
		}
	});
}
