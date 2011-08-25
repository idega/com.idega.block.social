
ContentSharingHelper = {
//		showSendMsgWindow : function (link,parameters){
//			for(i = 0;i < parameters.length;i += 2){
//				link += "&" + parameters[i] + '=' + parameters[i+1];
//			}
//			jQuery('a.invisibleLink').attr("href", link);
//			jQuery('a.invisibleLink').trigger('click');
//
//		}
		showSendMsgWindow : function (link,linkId,parameters){
			//jQuery('#' + linkId).attr("href", link);
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
//					FileUploaderInitializer.initFileUploadHelper();
				}
				,onClosed:function() {
					
				}
			});
		}
		
		,bindOnclick : function(selector,action){
			jQuery(document).ready(function(){
				jQuery(selector).click(function(){
					eval(action)
				});
			});
		}
		,bindAction : function(selector,event,action){
			jQuery(document).ready(function(){
				jQuery(selector).bind(event,function(){eval(action);});
			});
		}
		
		,addEnlargeScript : function(areaSelector,contentSelector,helpMsgSelector){
			ContentSharingHelper.height = jQuery(areaSelector).parent().children(contentSelector).css("height");
			ContentSharingHelper.width = jQuery(areaSelector).parent().children(contentSelector).css("width");
			ContentSharingHelper.fontSize = jQuery(areaSelector).parent().children(contentSelector).css("font-size");
			
			jQuery(areaSelector).hover(
					function() {
						var height = parseFloat(ContentSharingHelper.height) * 1.5;
						var width = parseFloat(ContentSharingHelper.width)* 1.5;
						var fontSize = parseFloat(ContentSharingHelper.fontSize)* 1.5;
						jQuery(this).parent().children(contentSelector).animate({
								height : height
								,width : width
								,fontSize : fontSize
						});
						jQuery(this).siblings().children(helpMsgSelector).delay(500).fadeIn("fast");
					},
					function() {
						jQuery(this).parent().children(contentSelector).animate({
								height : ContentSharingHelper.height
								,width : ContentSharingHelper.width
								,fontSize : ContentSharingHelper.fontSize
						});
						jQuery(this).siblings().children(helpMsgSelector).fadeOut('fast');
					 }
			);
		}
		,resizeSrolable : function(element){
			parent = element.parent();
			pw = parseFloat(parent.css("width"));
			ph = parseFloat(parent.css("height"));
			w = ((parseFloat(element.css("width")) - 15) / pw * 100) + "%";
			h = ((parseFloat(element.css("height")) - 5) / ph * 100) + "%";
			element.css({width : w,height : h});
		}
}

jQuery(document).ready(function(){
	ContentSharingHelper.createInvisibleLink();
	jQuery(".scrolable").each(function(){
		element = jQuery(this);
		ContentSharingHelper.resizeSrolable(element); 
	});
});
