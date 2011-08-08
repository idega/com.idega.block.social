
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


var PostCreationView = {
		seted : false
		,createAccordion : function(selector, childSelector){
			jQuery(selector).accordion({ autoHeight: false
				, active : false
//				, change : function(e,ui){
//					PostCreationView.setToCheck(jQuery(ui.newHeader).find("input"));
//				}
			});
			jQuery(selector + " " + childSelector).each(function(){
				var id = jQuery(this).attr('id');
				if(jQuery('#' + id + ' + div > *').size() < 1){
					jQuery(this).find('.ui-icon').css({visibility : 'hidden'});
					jQuery('#' + id + ' + div').css({padding : 0});
				}
			});
		}

		,setToCheck : function(oThis){
			if(jQuery(oThis).filter(":checked").size() == 0){
				setTimeout(function(){
					jQuery(oThis).attr("checked",true);
				},100);
			}else{
				setTimeout(function(){
					jQuery(oThis).attr("checked",false);
				},100);
			}
		}
		//jQuery ui accordion disables checking in header, these two functions below enables that
		,setChecked : function(oThis) {
			if(jQuery(oThis).filter(":checked").size() == 1){
				setTimeout(function(){
					jQuery(oThis).attr("checked",true);
				},10);
			}else{
				setTimeout(function(){
					jQuery(oThis).attr("checked",false);
				},10);
			}
		}
		,setTocheckOnClick : function(checkSelector){
			jQuery(document).ready(function(){
				jQuery(".post-creation-view-accordion-header").click(function(){
					if(!PostCreationView.seted){
						PostCreationView.setToCheck(jQuery(this).find(checkSelector));
					}
					PostCreationView.seted = false;
				});
				jQuery(checkSelector).click(function(){
					PostCreationView.setChecked(jQuery(this));
					PostCreationView.seted = true;
				});
			});
		}
		,savePost : function(selector){
			var urlSearch = "/dwr/call/plaincall/SagaServices.savePost.dwr" 
				+"?scriptSessionId="+ dwr.engine._scriptSessionId
				+'&httpSessionId='+ dwr.engine._getHttpSessionId()
				+'&page='+ window.location.pathname
				+'&windowName='+ window.name
				+"&callCount=1"
				+"&batchId="+ dwr.engine._nextBatchId
				+"&c0-id=0"
				+"&c0-scriptName=SagaServices"
				+"&c0-methodName=savePost";
			
//			a = jQuery("#javax.faces.ViewState");
//			a.remove();
//			b = jQuery(selector + " .post-creation-view-main-form_SUBMIT");
//			b.remove();
			jQuery(selector + " input[type = 'hidden']").remove();
			var form = jQuery(selector);
			var parameters = form.serialize();
			
			
			urlSearch += "&" + parameters;
			jQuery.ajax({
				url : urlSearch
				,type: "POST"
			});
//			dwr.engine.setParameters(parameters);
//			SagaServices.savePost({
//				callback: function() {
//					
//				}
//			});
		}
};

