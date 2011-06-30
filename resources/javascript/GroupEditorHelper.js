/**
 * 
 */

var UNSELECTED_GROUP_STYLE = 'normal';	
var SELECTED_GROUP_STYLE = 'bold';
var SELECTED_GROUP_STYLE_PARAMETER = 'font-weight';
var ERROR_MESSAGE_CLASS = "error_message_class";
var GROUP_DUBLICATION_MESSAGE = "A group with this name already exists!";
var groupDublicationMessageCreated = false;
var groupNameFieldId = null; //used to access group name field

jQuery(document).ready(function(){
	jQuery('body').append('<a class = "invisibleLink">create</a>');
	jQuery("a.invisibleLink").hide().fancybox({ 'hideOnContentClick': false,'zoomSpeedIn': 300, 'zoomSpeedOut': 300, 
		'overlayShow': true, 'width': 200, 'height': 300});
});

function openGroupCreationDialog(link,parentGroupParameterName){
	link += "&" + parentGroupParameterName + '=' + getSelectedGroup();
	jQuery('a.invisibleLink').attr("href", link);
	jQuery('a.invisibleLink').trigger('click');

}

function openGroupEditDialog(link,edittedGroup){
	link += "&" + edittedGroup + '=' + getSelectedGroup();
	jQuery('a.invisibleLink').attr("href", link);
	jQuery('a.invisibleLink').trigger('click');

}



//overriding :D
function registerActionsForGroupTreeSpan() {
	jQuery('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).click( function() {
				var element = jQuery(this);
				var style = UNSELECTED_GROUP_STYLE;
				if(element.css(SELECTED_GROUP_STYLE_PARAMETER) != SELECTED_GROUP_STYLE){
					style = SELECTED_GROUP_STYLE;
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

function getSelectedGroup(){
	selectedGroup = "-1";
	jQuery('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(function() {
			var element = jQuery(this);
			if(element.css(SELECTED_GROUP_STYLE_PARAMETER) == SELECTED_GROUP_STYLE){
				selectedGroup = element.attr('value');
				return false; //breaking
			}
	});
	return selectedGroup;
}

function checkIfNameExists(inputId){
	if(!groupDublicationMessageCreated){
		jQuery('#' + groupNameFieldId).after("<span id = 'text_field_error_id' class = '" + ERROR_MESSAGE_CLASS +
			"' style = 'display: none;'>" + GROUP_DUBLICATION_MESSAGE + "</span>");
		groupDublicationMessageCreated = true;
	}
	value = jQuery('#' + groupNameFieldId).val();
	SagaServices.isAllowedToSave(value, {
		callback: function(isGroupNameUnique) {
			IS_GROUP_NAME_OK = isGroupNameUnique;
			if(isGroupNameUnique){
				jQuery('#text_field_error_id').hide();
				return;
			}
			jQuery('#text_field_error_id').css('display', 'block');
		}
	});
}

function setGroupNameFieldId(id){
	groupNameFieldId = id;
}
