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
			
			SocialServices.savePost(map,{
				callback: function(reply){
					closeAllLoadingMessages();
					humanMsg.displayMsg(reply);
					jQuery(document).trigger('idega-social-post-sent');
				}
			});
			
		}
		,createAutoresizing : function(selector){
			jQuery(document).ready(function(){
				jQuery(selector).autoResize({limit : 250});
			});
		}
		
		,createAutocompleteWithImages : function(selector){
			jQuery(document).ready(function(){
				input = jQuery(selector);
				input.tagedit({
					autocompleteURL: function(request, response) {
						SocialServices.autocompleteUserSearchWithImagesRequest(request.term,-1, 20, 0, {
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
							SocialServices.autocompleteUserSearchWithImagesRequest(request,-1, 20, 0, {
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
						SocialServices.getSagaGroupSearchResultsAsAutoComplete(request.term, {
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
							SocialServices.getSagaGroupSearchResultsAsAutoComplete(request, {
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

