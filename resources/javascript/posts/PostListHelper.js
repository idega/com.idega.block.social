(function($) {
   $.fn.postListHelper = function(options) {
	   
	   var opts = $.extend({}, $.fn.postListHelper.defaults, options);
	   
	   return this.each(function(){
		   var list = jQuery(this);
		   
		   var doc = jQuery("head");
		   opts.list = list;
		   // Global listeners
		   doc.bind("prepend-posts",list,function(e){
			   e.data.trigger("prepend-posts");
		   });
		   doc.bind("append-posts",list,function(e){
			   e.data.trigger("append-posts");
		   });
		   
		   list.data("social-post-list-data", {appending : false,prepending : false, options : opts});
		   // Local listeners
		   list.bind("prepend-posts",opts,function(e,additionalParameters){
			   if((additionalParameters == undefined) || (additionalParameters == null)){
				   additionalParameters = {};
			   }
			   var list = e.data.list;
			   var listData = list.data("social-post-list-data");
			   if(listData.prepending == true){
				   return;
			   }
			   listData.prepending = true;
//			   showLoadingMessage("");
			   var firstUri = list.find("." + e.data.postUriClass).first().val();
			   var firstDate = list.find("." + e.data.modificationDateClass).first().val();
			   var additional = {beginUri : firstUri, getUp : true, order : false,beginDate : firstDate};
			   var parameters = jQuery.extend({}, listData.options.filterParameters, additional,additionalParameters);
			   SocialServices.getPostListHtml(parameters,opts.presentationOptions,opts.postListClass,{
					callback : function(html){
						if(html == null){
							// Actions for failure
//							closeAllLoadingMessages();
							return;
						}
						list.prepend(html);
						var listData = list.data("social-post-list-data");
						listData.prepending = false;
						closeAllLoadingMessages();
						return;
					},
					errorHandler:function(message) {
						var listData = list.data("social-post-list-data");
						listData.prepending = false;
//						closeAllLoadingMessages();
						alert(message);
					}
				});
		   });
		   
		   list.bind("append-posts",opts,function(e,additionalParameters){
			   if((additionalParameters == undefined) || (additionalParameters == null)){
				   additionalParameters = {};
			   }
			   var list = e.data.list;
			   var listData = list.data("social-post-list-data");
			   if(listData.appending == true){
				   return;
			   }
			   listData.appending  = true;
			   showLoadingMessage("");
			   list.trigger("append-posts");
			   var lastUri = list.find("." + e.data.postUriClass).last().val();
			   var lastDate = list.find("." + e.data.modificationDateClass).last().val();
			   var additional = {beginUri : lastUri, getUp : false, order : false,beginDate : lastDate};
			   var parameters = jQuery.extend({}, listData.options.filterParameters, additional,additionalParameters);
			   SocialServices.getPostListHtml(parameters,opts.presentationOptions,opts.postListClass,{
					callback : function(html){
						if(html == null){
							// Actions for failure
							closeAllLoadingMessages();
							return;
						}
						list.append(html);
						var loadMoreButton = list.find(".load-more");
						loadMoreButton.remove();
						list.append(loadMoreButton);
						var listData = list.data("social-post-list-data");
						listData.appending = false;
						closeAllLoadingMessages();
						return;
					},
					errorHandler:function(message) {
						var listData = list.data("social-post-list-data");
						listData.appending = false;
						closeAllLoadingMessages();
						alert(message);
					}
				});
		   });
		   
		   doc.bind("delete-post-by-uri",list,function(e,uri){
			   e.data.trigger("delete-post-by-uri",uri);
		   });
		   
		   list.bind("delete-post-by-uri",opts,function(e,uri){
			   var list = e.data.list;
			   var listData = list.data("social-post-list-data");
			   if(listData.deleting == true){
				   return;
			   }
			   listData.deleting  = true;
//			   showLoadingMessage("");
			   
			   SocialServices.deletePost(uri,{
					callback : function(response){
						listData.deleting  = false;
						if(response.status != "OK"){
							// Actions for failure
							closeAllLoadingMessages();
							humanMsg.displayMsg(response.message);
							return;
						}
						var uriContainer = list.find("." + e.data.postUriClass).filter("[value='"+ uri +"']");
						var divToRemove = uriContainer.parent();
						divToRemove.fadeOut(500,function(){divToRemove.remove();});
						
						closeAllLoadingMessages();
						return;
					},
					errorHandler:function(message) {
						listData.deleting  = false;
						closeAllLoadingMessages();
						alert(message);
					}
				});
		   });
		   
		   list.bind("set-post-receivers",opts,function(e,receivers){
			   if(receivers === undefined){
				   return;
			   }
			   if(!jQuery.isArray(receivers)){
				   var list = [];
				   list.push(receivers);
				   receivers = list;
			   }
			   var list = e.data.list;
			   var listData = list.data("social-post-list-data");
			   listData.options.filterParameters.receivers = receivers;
		   });
		   
	   } ); 
   
   }
   
   $.fn.postListHelper.defaults = {};
   
})(jQuery);

jQuery.fn.getYYYYMMDDHHMM = function(){
	   var c = jQuery(this);
	   var time = c.find("input").val();
	   time = parseInt(time);
	   var d = new Date(time);
	   var getZero = function(n){
		   if(n < 10){
			   return '0' + n;
		   }
		   return n;
	   }
	   var str = ''+d.getFullYear()+'-'+ getZero(d.getMonth()+1) +"-"+ 
	   getZero(d.getDate()) +" "+ getZero(d.getHours()) +":"+ getZero(d.getMinutes());
	   c.find(".d-text").text(str);
	   c.find(".d-title").attr("title",str);
}

var PostListHelper = {};

PostListHelper.preparePostPreview = function(selector){
	jQuery(selector).fancybox({
		width :		windowinfo.getWindowWidth() * 0.8,
		height : 	windowinfo.getWindowHeight() * 0.8,
		autoSize:	false,
		type :		'ajax'	
	});
}
PostListHelper.prepareImagesPreview = function(paragraphSelector){
	jQuery(paragraphSelector).find('a').fancybox({
		minWidth	: windowinfo.getWindowWidth() * 0.6,
		minHeight	: windowinfo.getWindowHeight() * 0.6,
		maxWidth	: windowinfo.getWindowWidth() * 0.8,
		maxHeight	: windowinfo.getWindowHeight() * 0.8,
		fitToView   : false,
	    autoSize    : false,
		arrows	:	true
	});
}

