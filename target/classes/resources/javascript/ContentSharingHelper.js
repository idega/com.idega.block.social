/**
 * 
 */

ContentSharingInfo = {};

jQuery(document).ready(function(){
	ContentSharingHelper.createInvisibleLink();
});

ContentSharingHelper = {
		showSendMsgWindow : function (link,parameters){
			for(i = 0;i < parameters.length;i += 2){
				link += "&" + parameters[i] + '=' + parameters[i+1];
			}
			jQuery('a.invisibleLink').attr("href", link);
			jQuery('a.invisibleLink').trigger('click');

		}
		
		,showMenu : function (objetsToHideSelector, objectToShowSelector){
			jQuery(objetsToHideSelector).hide();
			jQuery(objectToShowSelector).show();
		}
		
		,createInvisibleLink : function(){
			jQuery('body').append('<a class = "invisibleLink">create</a>');
			jQuery("a.invisibleLink").fancybox({
				hideOnContentClick: false
				,zoomSpeedIn: 300
				,zoomSpeedOut: 300
				,overlayShow: true
				,width: windowinfo.getWindowWidth() * 0.8
				,height: windowinfo.getWindowHeight() * 0.8
				,autoDimensions: false
				,onComplete: function() {
					
				}
				,onClosed:function() {
					
				}
			});
		}
		
		,bindOnclick : function(selector,action){
			jQuery(document).ready(function(){
				jQuery(selector).click(function(){eval(action)});
			});
		}
}
