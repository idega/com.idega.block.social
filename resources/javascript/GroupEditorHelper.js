/**
 * SimpleAppInfo object is from /com.idega.user/resources/javascript/SimpleUserAppHelper.js
 * 
 */

var UNSELECTED_GROUP_STYLE = 'normal';	
var SELECTED_GROUP_STYLE = 'bold';
var SELECTED_GROUP_STYLE_PARAMETER = 'font-weight';
var ERROR_MESSAGE_CLASS = "error_message_class";
var GroupEditorInfo = {};
var GroupTreeReloadInfo = {};
var UserSearchAreaInfo = {};
var SagaGroupCreatorHelper = {};

var uniqueNumberPrefix = -2147483648;


function setGlobalVariables(NameFieldId,loadingMsg,savingUsersMsg,currentGroupId,userTablelayerId,searchTableLayerId){
	GroupEditorInfo.groupNameErrorMessagesCreated = false;
	GroupEditorInfo.groupNameFieldId = NameFieldId;
	GroupEditorInfo.loadingMsg = loadingMsg;
	GroupEditorInfo.savingUsersMsg = savingUsersMsg;
	GroupEditorInfo.userTablelayerId = userTablelayerId;
	GroupEditorInfo.searchTableLayerId = searchTableLayerId;
	
	SimpleAppInfo.currentGroupId = currentGroupId;
	
	GroupEditorInfo.UsersToAdd = [];
	GroupEditorInfo.UsersToRemove = [];
	
}

//function setIds(parentContainerId, editedGroupId){
//	GroupEditorInfo.parentContainerId = parentContainerId;
//	GroupEditorInfo.parentContainerId = parentContainerId;
//}

jQuery(document).ready(function(){
	jQuery('body').append('<a class = "invisibleLink">create</a>');
	jQuery("a.invisibleLink").fancybox({
		hideOnContentClick: false,
		zoomSpeedIn: 300,
		zoomSpeedOut: 300, 
		overlayShow: true,
		width: windowinfo.getWindowWidth() * 0.8,
		height: windowinfo.getWindowHeight() * 0.8,
		autoDimensions: false,
		onComplete: function() {
			if(UserSearchAreaInfo.addTagEditFunctions){
				createAutocompleteForUserSearchWithTagEdit(UserSearchAreaInfo.inputSelector);
			}else{
				createAutocompleteForUserSearch(UserSearchAreaInfo.inputSelector);
			}
			disableOrEnableUserEditTab();
		},
		onClosed:function() {
			reloadGroupTree();
		}
	});
});

function openGroupCreationDialog(link,parentGroupParameter,edittedGroup){
	link += "&" + parentGroupParameter + '=' + SimpleAppInfo.currentGroupId;
	jQuery('a.invisibleLink').attr("href", link);
	jQuery('a.invisibleLink').trigger('click');

}

function openGroupEditDialog(link,parentGroupParameter,edittedGroup){
	link += "&" + edittedGroup + '=' + SimpleAppInfo.currentGroupId;
	SagaServices.getParentGroup(SimpleAppInfo.currentGroupId, {
		callback: function(parentGroup) {
			link += "&" + parentGroupParameter + '=' + parentGroup;
		}
	});
	jQuery('a.invisibleLink').attr("href", link);
	jQuery('a.invisibleLink').trigger('click');
}



//overriding :D
function registerActionsForGroupTreeSpan() {
	jQuery('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).unbind();
	jQuery('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).click( function() {
				var element = jQuery(this);
				var style = UNSELECTED_GROUP_STYLE;
				if(element.css(SELECTED_GROUP_STYLE_PARAMETER) != SELECTED_GROUP_STYLE){
					style = SELECTED_GROUP_STYLE;
					jQuery(".editTool").show();
					SimpleAppInfo.currentGroupId = element.attr("value");
				}
				else{
					SimpleAppInfo.currentGroupId = -1;
					jQuery(".editTool").hide();
				}
				unselectAllGroups();	//does not let to select more than one element
				element.css(SELECTED_GROUP_STYLE_PARAMETER,style);
			});
}

function unselectAllGroups(){
	jQuery('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(function() {
			var element = jQuery(this);
			element.css(SELECTED_GROUP_STYLE_PARAMETER, UNSELECTED_GROUP_STYLE);
	});
	
}

function checkIfNameExists(inputId,groupNameDublicationMsg,groupNameEmptyMsg){
	if(!GroupEditorInfo.groupNameErrorMessagesCreated){
		jQuery('#' + GroupEditorInfo.groupNameFieldId).after("<span id = 'group_dublication_message_id' class = '" + ERROR_MESSAGE_CLASS +
			"' style = 'display: none;'>" + groupNameDublicationMsg + "</span>");
		groupDublicationMessageCreated = true;
		jQuery('#' + GroupEditorInfo.groupNameFieldId).after("<span id = 'group_name_empty_error_id' class = '" + ERROR_MESSAGE_CLASS +
				"' style = 'display: none;'>" + groupNameEmptyMsg + "</span>");
		GroupEditorInfo.groupNameErrorMessagesCreated = true;
	}
	value = jQuery('#' + GroupEditorInfo.groupNameFieldId).val();
	if(value == ""){
		jQuery('#group_name_empty_error_id').css('display', 'block');
		SimpleAppInfo.isGroupNameOk = false;
		return;
	}else{
		jQuery('#group_name_empty_error_id').hide();
	}
	SagaServices.isGroupAllowedToSave(value, SimpleAppInfo.currentGroupId, {
		callback: function(isGroupNameOk) {
			SimpleAppInfo.isGroupNameOk = isGroupNameOk;
			if(isGroupNameOk){
				jQuery('#group_dublication_message_id').hide();
				return;
			}
			jQuery('#group_dublication_message_id').css('display', 'block');
		}
	});
}


function getUserSearchResults(requestValue){
	jQuery('.main-search-table-layer').remove();
	showLoadingMessage(GroupEditorInfo.loadingMsg);
	tableLayer = jQuery('.user-search-layer');
	if (!(requestValue instanceof Array) ){
		requestValue = [requestValue];
	}
	jQuery('.containerLayer').hide("normal");
	SagaServices.getUserSearchResultTableBySearchrequest(requestValue, SimpleAppInfo.currentGroupId,-1,-1,{
		callback: function(component) {
			IWCORE.insertRenderedComponent(component,{
				container: tableLayer,
				append: true
			});
			closeAllLoadingMessages();
		}
	});
	
	//some dinamic css modificationsCreatorWindowInfo.height = windowinfo.getWindowHeight() * 0.8;
//	height = windowinfo.getWindowHeight() * 0.8 - jQuery('.searchFieldLayer').height() - 100;
	jQuery('.user-search-table-layer').height(200);
}

function generateUniqueNumberPrefix(){
	uniqueNumberPrefix++;
	return uniqueNumberPrefix;
}

function addChosenUsers(){
	jQuery('#'+ GroupEditorInfo.userTablelayerId).empty();
	var userIds = [];
	jQuery(".checkboxUserToAddIdContainer").filter(":checked").each(function() {
		var element = jQuery(this);
		var userId = element.attr('value');
		userIds.push(userId);
	});
	tableLayer = jQuery('#'+ GroupEditorInfo.userTablelayerId);
	SagaServices.saveUsersAndGetUserTable(SimpleAppInfo.currentGroupId,userIds, {
		callback: function(component) {
			IWCORE.insertRenderedComponent(component,{
				container: tableLayer,
				rewrite: true
			});
		}
	});
	jQuery("tr.rowOfSearchResultTable[value = 'checkedToRemove']").remove();
	if(jQuery('.user-search-table-layer tr').length < 2){
		closeSearch();
	}
}

function checkUserToAdd(checkBoxId, userRowId){
	if(jQuery("#"+ checkBoxId).is(':checked')){
		jQuery("#"+ userRowId).attr("value","checkedToRemove");
	}else{
		jQuery("#"+ userRowId).attr("value","");
	}
}

function removeUserFromGroup(userId, rowId){
	SagaServices.removeUserFromGroup(userId,SimpleAppInfo.currentGroupId
//	,{callback: function(msg){
//			showHumanizedMessage(msg, null);
//		}
//	}
	);
	jQuery("#"+ rowId).remove();
	if(jQuery("#"+ GroupEditorInfo.userTablelayerId +"tr").length < 1){
		tableLayer = jQuery('#'+ GroupEditorInfo.userTablelayerId).empty();
		table = jQuery('#'+ GroupEditorInfo.userTablelayerId);
		SagaServices.getUsersOfSpecifiedGroupTable(SimpleAppInfo.currentGroupId,-1,0, {
			callback: function(component) {
				IWCORE.insertRenderedComponent(component,{
					container: tableLayer,
					rewrite: true
				});
			}
		});
	}
}

function loadSagaBookGroupsTree(containerId,noGroupsMsg,groupId, styleClass){
	//save info for reloading later
	GroupTreeReloadInfo.containerId = containerId;
	GroupTreeReloadInfo.noGroupsMsg = noGroupsMsg;
	GroupTreeReloadInfo.groupId = groupId;
	GroupTreeReloadInfo.styleClass = styleClass;
	reloadGroupTree();
}

//loads the group  tree by GroupTreeReloadInfo
function reloadGroupTree(){
	jQuery("#" + GroupTreeReloadInfo.containerId).empty();
	GroupService.getChildrenOfGroup(GroupTreeReloadInfo.groupId, {
		callback: function(nodes) {
			if (nodes == null) {
				closeAllLoadingMessages();
				return false;
			}
			addGroupsTree(nodes, GroupTreeReloadInfo.containerId, GroupTreeReloadInfo.noGroupsMsg, null, GroupTreeReloadInfo.styleClass);
		}
	});
	jQuery(".editTool").hide();
	SimpleAppInfo.currentGroupId = -1;
}

function bindEventsToSearchInput(inputSelector){
	jQuery(inputSelector).keypress(function(event) {
		var code = event.keyCode > 0 ? event.keyCode : event.which;
		if(code == 13) {
			getUserSearchResults(jQuery(inputSelector).val());
			event.preventDefault();
		}
	});
}

function prepareSearchAreaTextInput(inputId, addTagEditFunctions){
	UserSearchAreaInfo.inputSelector = "#" + inputId;
	UserSearchAreaInfo.addTagEditFunctions = Boolean(addTagEditFunctions);
}

function createAutocompleteForUserSearch(inputSelector){
	jQuery(inputSelector).autocomplete({
		source : function(request, response) {
			SagaServices.autocompleteUserSearchRequest(request.term,SimpleAppInfo.currentGroupId, 20, 0,{
				callback: function(userDataCollection) {
					var arrayOfData = [];
					for(i = 0;i < userDataCollection.length;i++){
						arrayOfData.push(userDataCollection[i]);
					}
					response(arrayOfData);
				}
				,appendTo :	".SimpleGroupCreatorContainer"
			});
		}
	});
	bindEventsToSearchInput(inputSelector);
}

function createAutocompleteForUserSearchWithTagEdit(inputSelector){
	jQuery(inputSelector).tagedit({
		autocompleteURL: function(request, response) {
			SagaServices.autocompleteUserSearchRequest(request.term,SimpleAppInfo.currentGroupId, 20, 0, {
				callback: function(userDataCollection) {
					var arrayOfData = [];
					for(i = 0;i < userDataCollection.length;i++){
						arrayOfData.push(userDataCollection[i]);
					}
					response(arrayOfData);
				}
			});
		},
		allowEdit: true,
		allowAdd: true,
		delay: 100,
		autocompleteOptions: {
			appendTo :	".SimpleGroupCreatorContainer"
		}
	});
//	jQuery("#tagedit-input").keypress(function(event) {
//		var code = event.keyCode > 0 ? event.keyCode : event.which;
//		if(code == 13) {
//			if(jQuery("#tagedit-input").val() == ""){
//				searchForUsersByTagEditInputs();
//			}
//			event.preventDefault();
//		}
//	});
}


function searchForUsersByTagEditInputs(){
	var values = [];
	jQuery(".tagedit-listelement").children("span").each(function(){
		textValue = jQuery(this).text();
		values.push(textValue);
	});
	getUserSearchResults(values);
}

SimpleAppInfo.actionsAfterSave = function(savedGroupId){
	SimpleAppInfo.currentGroupId = savedGroupId;
	SagaGroupCreatorHelper.prepareForSaving();
}
function prepareForNextSave(groupIdContainerId,parentGroupIdContainerId,parentGroupId){
	jQuery("#"+groupIdContainerId).attr("value",SimpleAppInfo.currentGroupId);
	jQuery("#"+parentGroupIdContainerId).attr("value",parentGroupId);
	disableOrEnableUserEditTab();
}

function disableOrEnableUserEditTab(){
	if((SimpleAppInfo.currentGroupId == -1) || (SimpleAppInfo.currentGroupId == "null") || (SimpleAppInfo.currentGroupId == null)){
		jQuery(".userEditTab").hide();
	}else{
		jQuery(".userEditTab").show();
	}
}

function closeSearch(){
	jQuery('.main-search-table-layer').remove();
	jQuery('.containerLayer').show("normal");
}

function searchForUsers(inputSelector){
	if(UserSearchAreaInfo.addTagEditFunctions == true){
		searchForUsersByTagEditInputs();
	}else{
		getUserSearchResults(jQuery(inputSelector).val());
	}
}
