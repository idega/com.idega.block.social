/**
 * 
 */






var PostCreationViewHelper = {
		TAGEDIT_INPUT_SELECTOR : "[name=\"tag[]\"]"
		,nontrivialUserDefiningPhraseErrorMsg : null
		,seted : false
		,createAccordion : function(selector, childSelector){
			jQuery(document).ready(function(){
				jQuery(selector).accordion({ autoHeight: false
					, active : false
				});
				jQuery(selector + " " + childSelector).each(function(){
					var id = jQuery(this).attr('id');
					if(jQuery('#' + id + ' + div > *').size() < 1){
						jQuery(this).find('.ui-icon').css({visibility : 'hidden'});
						jQuery('#' + id + ' + div').css({padding : 0});
					}
				});
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
		,setTocheckOnClick : function(checkSelector, activeSelector){
			jQuery(document).ready(function(){
				// Sets to check checkbox when header is clicked 
				// TODO: think if it is convinient and logical
//				jQuery(".post-creation-view-accordion-header").click(function(){
//					if(!PostCreationViewHelper.seted){
//						PostCreationViewHelper.setToCheck(jQuery(this).find(checkSelector));
//					}
//					PostCreationViewHelper.seted = false;
//				});
				//enables checkbox checking
				jQuery(checkSelector).click(function(){
					PostCreationViewHelper.setChecked(jQuery(this));
					PostCreationViewHelper.seted = true;
				});
				if((activeSelector != undefined) && (activeSelector != null)){
					jQuery(activeSelector).prev().click();
					jQuery(activeSelector).prev().find(checkSelector).attr("checked",true);
				}
			});
		}
//		,savePost : function(selector){
////			dwr.engine.beginBatch();
////			dwr.engine.batch.create();
//			
//			var urlSearch = "/dwr/call/plaincall/SagaServices.savePost.dwr" 
//				+"?scriptSessionId="+ dwr.engine._scriptSessionId
//				+'&httpSessionId='+ dwr.engine._getHttpSessionId()
//				+'&page='+ window.location.pathname
//				+'&windowName='+ window.name
//				+"&callCount=1"
//				+"&batchId="+ dwr.engine._nextBatchId
//				+"&c0-id=0"
//				+"&c0-scriptName=SagaServices"
//				+"&c0-methodName=savePost";
//			
//			var form = jQuery(selector);
//			var parameters = form.serialize();
//			parameters=decodeURIComponent(parameters);
//			
//			
//			urlSearch += "&" + parameters;
//			
//			
//			jQuery.ajax({
//				url : urlSearch
//				,type: "POST"
//				//TODO: should return saved id too for enabling edition of post
//				,success: function(reply){
//					var replyPhrases = reply.split("\"");
//					if(replyPhrases.length < 7){
//						//the return value is null
//						return false;
//					}
//					var msg = replyPhrases[replyPhrases.length -2];
//					humanMsg.displayMsg(msg);
//				}
//			});
//			
////			dwr.engine.endBatch();
//		}
		,savePost : function(selector){
			showLoadingMessage("");
			var form = jQuery(selector);
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
					closeAllLoadingMessages();
					humanMsg.displayMsg(reply);
				}
			});
			
		}
		,createAutoresizing : function(selector){
			jQuery(document).ready(function(){
				jQuery(selector).autoResize({limit : 250});
//				jQuery(selector).autoGrow();
			});
		}
		
		,createAutocompleteWithImages : function(selector){
			jQuery(document).ready(function(){
				input = jQuery(selector);
				input.tagedit({
					autocompleteURL: function(request, response) {
						SagaServices.autocompleteUserSearchWithImagesRequest(request.term,-1, 20, 0, {
							callback: function(userDataCollection) {
								var arrayOfData = [];
								var i = 0;
								var end = userDataCollection.length - 1;
								while(i < end){
									var item = {
											label : userDataCollection[i],
											value : userDataCollection[i+1]
									};
									arrayOfData.push(item);
									i += 2;
								}
								response(arrayOfData);
							}
						});
					},
					allowEdit: true,
					allowAdd: true,
					delay: 100,
					autocompleteOptions: {
						minLength : 1,
						html: true,
						select: function( event, ui ) {
							jQuery(PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR).val(ui.item.value).trigger('transformToTag', [ui.item.id, ui.item.label]);
							return false;
						}
					},
					transform : function(event, id, label) {
						var obj = jQuery(PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR).data("tag-options-data");
						var oldValue = (typeof id != 'undefined' && id.length > 0);

						if(label == undefined){
							var request  = jQuery(PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR).val();
							SagaServices.autocompleteUserSearchWithImagesRequest(request,-1, 20, 0, {
								callback: function(userDataCollection) {
									if(userDataCollection.length == 2){
										jQuery(PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR).trigger('transformToTag', [undefined, userDataCollection[0]]);
									}
									else{
										humanMsg.displayMsg(PostCreationViewHelper.nontrivialUserDefiningPhraseErrorMsg);
										jQuery(PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR).focus();
									}
								}
							});
							return false;
						}
						var checkAutocomplete = oldValue == true? false : true;
						// check if the Value ist new
						var isNewResult = obj.isNew(label.toString(), checkAutocomplete);
						if(isNewResult[0] === true || (isNewResult[0] === false && typeof isNewResult[1] == 'string')) {

							if(oldValue == false && typeof isNewResult[1] == 'string') {
								oldValue = true;
								id = isNewResult[1];
							}

							if(obj.options.allowAdd == true || oldValue) {
								// Make a new tag in front the input
								html = '<li class="tagedit-listelement tagedit-listelement-old">';
								html += '<span dir="'+obj.options.direction+'">' + label + '</span>';
								html += "<input type='hidden' name = 'tag[]' disabled='disabled'" + " value=\"" + label.toString() +"\" />";
								html += '<a class="tagedit-close" title="'+obj.options.texts.removeLinkTitle+'">x</a>';
								html += '</li>';

								jQuery(this).parent().before(html);
							}
						}
						jQuery(this).val('');

						// close autocomplete
						if(obj.options.autocompleteOptions.source) {
							jQuery(this).autocomplete( "close" );
						}

					}
				});
				input.inputsToList = function(){};
			});
		}
		,createGroupAutocomplete : function(selector){
			jQuery(document).ready(function(){
				input = jQuery(selector);
				input.tagedit({
					autocompleteURL: function(request, response) {
						SagaServices.getSagaGroupSearchResultsAsAutoComplete(request.term, {
							callback: function(userDataCollection) {
								var arrayOfData = [];
								var i = 0;
								var end = userDataCollection.length - 1;
								while(i < end){
									var item = {
											label : userDataCollection[i],
											value : userDataCollection[i+1]
									};
									arrayOfData.push(item);
									i += 2;
								}
								response(arrayOfData);
							}
						});
					},
					allowEdit: true,
					allowAdd: true,
					delay: 100,
					autocompleteOptions: {
						minLength : 1,
						html: true,
						select: function( event, ui ) {
							jQuery(PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR).val(ui.item.value).trigger('transformToTag', [ui.item.id, ui.item.label]);
							return false;
						}
					},
					transform : function(event, id, label) {
						var obj = jQuery(PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR).data("tag-options-data");
						var oldValue = (typeof id != 'undefined' && id.length > 0);

						if(label == undefined){
							var request  = jQuery(PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR).val();
							SagaServices.getSagaGroupSearchResultsAsAutoComplete(request, {
								callback: function(userDataCollection) {
									if(userDataCollection.length == 2){
										jQuery(PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR).trigger('transformToTag', [undefined, userDataCollection[0]]);
									}
									else{
										humanMsg.displayMsg(PostCreationViewHelper.nontrivialUserDefiningPhraseErrorMsg);
										jQuery(PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR).focus();
									}
								}
							});
							return false;
						}
						var checkAutocomplete = oldValue == true? false : true;
						// check if the Value ist new
						var isNewResult = obj.isNew(label.toString(), checkAutocomplete);
						if(isNewResult[0] === true || (isNewResult[0] === false && typeof isNewResult[1] == 'string')) {

							if(oldValue == false && typeof isNewResult[1] == 'string') {
								oldValue = true;
								id = isNewResult[1];
							}

							if(obj.options.allowAdd == true || oldValue) {
								// Make a new tag in front the input
								html = '<li class="tagedit-listelement tagedit-listelement-old">';
								html += '<span dir="'+obj.options.direction+'">' + label + '</span>';
								html += "<input type='hidden' name = 'tag[]' disabled='disabled'" + " value=\"" + label.toString() +"\" />";
								html += '<a class="tagedit-close" title="'+obj.options.texts.removeLinkTitle+'">x</a>';
								html += '</li>';

								jQuery(this).parent().before(html);
							}
						}
						jQuery(this).val('');

						// close autocomplete
						if(obj.options.autocompleteOptions.source) {
							jQuery(this).autocomplete( "close" );
						}

					}
				});
				input.inputsToList = function(){};
			});
		}
		,createLocationInput : function(containerSelector,name){
			jQuery(containerSelector).children(".file-to-upload").each(function(){
				var fileInput = jQuery(this);
				fileInput.remove();
			});
			var files = FileUploadHelper.allUploadedFiles;
			if(files == null){
				return;
			}
			
			for(var i = 0;i < files.length;i++){
				var fileInput = "<input type='hidden' name = \"" + name + "\" value=\"" + 
						files[i] +"\" class = \"file-to-upload\" />";
				jQuery(containerSelector).append(fileInput);
			}
			
		}
		,someHelp : function(){
			jQuery(document).ready(function(){
				FileUploaderInitializer.initFileUploadHelper();
			});
		}
		,uploaderInitialized : false
};



//autoresize that was in web2 was modified and did not worked for me, so I added this

/*
 * jQuery autoResize (textarea auto-resizer)
 * @copyright James Padolsey http://james.padolsey.com
 * @version 1.04
 */
//
//(function(a){a.fn.autoResize=function(j){var b=a.extend({onResize:function(){},animate:true,animateDuration:150,animateCallback:function(){},extraSpace:20,limit:1000},j);this.filter('textarea').each(function(){var c=a(this).css({resize:'none','overflow-y':'hidden'}),k=c.height(),f=(function(){var l=['height','width','lineHeight','textDecoration','letterSpacing'],h={};a.each(l,function(d,e){h[e]=c.css(e)});return c.clone().removeAttr('id').removeAttr('name').css({position:'absolute',top:0,left:-9999}).css(h).attr('tabIndex','-1').insertBefore(c)})(),i=null,g=function(){f.height(0).val(a(this).val()).scrollTop(10000);var d=Math.max(f.scrollTop(),k)+b.extraSpace,e=a(this).add(f);if(i===d){return}i=d;if(d>=b.limit){a(this).css('overflow-y','');return}b.onResize.call(this);b.animate&&c.css('display')==='block'?e.stop().animate({height:d},b.animateDuration,b.animateCallback):e.height(d)};c.unbind('.dynSiz').bind('keyup.dynSiz',g).bind('keydown.dynSiz',g).bind('change.dynSiz',g)});return this}})(jQuery);

