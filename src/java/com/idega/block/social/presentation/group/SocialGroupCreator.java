package com.idega.block.social.presentation.group;


import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hsqldb.lib.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.social.Constants;
import com.idega.block.social.business.SocialServices;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table2;
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.app.SimpleGroupCreator;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class SocialGroupCreator extends SimpleGroupCreator{

	@Autowired
	private SocialServices sagaservices;
	private Layer userEditContent = null;

	@Autowired
	UserApplicationEngine userApplicationEngine;

	private boolean tageditFunctions = true;

	private IWContext iwc = null;
	private IWResourceBundle iwrb = null;

	private String userSearchFieldId = null;
	private String searchResultTableLayerId = null;

	@Override
	public String getBundleIdentifier() {
		return Constants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void main(IWContext iwc) throws IOException {
		ELUtil.getInstance().autowire(this);

		this.iwc = iwc;
		Layer container = new Layer();
		add(container);

		iwrb = getBundle(iwc).getResourceBundle(iwc);
		String parentGroupId = iwc.getParameter(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION);

		if(StringUtil.isEmpty(parentGroupId) || parentGroupId.equals("-1")){
			parentGroupId = sagaservices.getSagaRootGroup().getId();
		}

		setParentGroupId(parentGroupId);
		this.setType(Constants.SOCIAL_TYPE);
		super.main(iwc);

		userEditContent = new Layer();
		addNewNavigationTabAndContent("userEditTab", iwrb.getLocalizedString("users", "Users"),userEditContent);
		userEditContent.setStyleClass("user-edit-main-layer");
		String editedGroupId = iwc.getParameter(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION);
		if(StringUtil.isEmpty(editedGroupId)){
			editedGroupId = "-1";
		}

		Layer userSearchLayer = new Layer();
		userEditContent.add(userSearchLayer);
		userSearchLayer.setStyleClass("user-search-layer");

		userSearchLayer.add(this.createSearchArea());

		String groupName = null;
		String currentGroupId = iwc.getParameter(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION);
		GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
		try{
			groupName = groupBusiness.getGroupByGroupID(Integer.valueOf(currentGroupId)).getName();
		}catch(Exception e){
//			groupName = "this group";
		}

		groupName = groupName == null ? "this group" : groupName;
		Layer userTableLayer = createUserTable(Integer.valueOf(editedGroupId),iwrb,groupName);
		userEditContent.add(userTableLayer);

		addActions(container,userTableLayer.getId(),parentGroupId);

		this.addStyleClassForChildren(userEditContent,"SagaGroupCreator");
	}

	private Layer createSearchArea(){
		Layer searchFieldLayer = new Layer();
		searchFieldLayer.setStyleClass("searchFieldLayer");

		Label searchLabel = new Label();
		searchFieldLayer.add(searchLabel);
		searchLabel.addText(iwrb.getLocalizedString("search_for_users", "Search for users:"));
		searchLabel.setStyleClass("search-input-label");

		TextInput userSearchArea = new TextInput("search", CoreConstants.EMPTY);
		searchFieldLayer.add(userSearchArea);
		userSearchArea.setStyleClass("search-input");
		userSearchArea.setName("tag[19-a]");
		this.userSearchFieldId = userSearchArea.getId();
		userSearchArea.setStyleClass("user-search-input");

		GenericButton button = new GenericButton("buttonSearch", iwrb.getLocalizedString("search", "Search"));
		searchFieldLayer.add(button);
		button.setOnClick("searchForUsers('#"+this.userSearchFieldId+"');");
		button.setStyleClass("user-search-input");

		return searchFieldLayer;
	}

	private void addActions(Layer container,String userTablelayerId, String parentGroupId){
		//setting global variables
		StringBuilder actionString= new StringBuilder("setGlobalVariables('").append(this.nameField.getId())
		.append("', '").append(iwrb.getLocalizedString("loading", "Loading"))
		.append("', '").append(iwrb.getLocalizedString("saving_users", "saving users"))
		.append("', '").append(iwc.getParameter(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION))
		.append("', '").append(userTablelayerId)
		.append("', '").append(this.searchResultTableLayerId)
		.append("');\n")
		.append("prepareSearchAreaTextInput('")
		.append(userSearchFieldId )
		.append("', '").append(tageditFunctions)
		.append("');\n");
		String action = PresentationUtil.getJavaScriptAction(actionString.toString());
		container.add(action);

		//setting action on save
		StringBuilder actionOnKeyUp = new StringBuilder("checkIfNameExists('")
		.append(this.nameField.getId())
		.append("', '").append(iwrb.getLocalizedString("group_with_same_name_exists", "A group with this name already exists."))
		.append("', '").append(iwrb.getLocalizedString("group_name_field_can_not_be_empty", "Group name field can not be empty."))
		.append("', '").append(iwc.getParameter(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION))
		.append("'); ");
		this.nameField.setOnKeyUp(actionOnKeyUp.toString());

		//disableOrEnableUserEditTab
		StringBuilder actionAfterSave = new StringBuilder("prepareForNextSave('").append(this.getGroupInputId())
		.append("', '").append(this.getParentGroupInputId())
		.append("', '").append(parentGroupId)
		.append("'); ");
		String old = this.getActionOnSave();
		this.setActionOnSave(actionOnKeyUp +  old + actionAfterSave.toString());

		StringBuilder preparation = new StringBuilder("SagaGroupCreatorHelper.prepareForSaving = function(){\n")
				.append(actionAfterSave).append("}");
		String preparationScript = PresentationUtil.getJavaScriptAction(preparation.toString());
		container.add(preparationScript);


	}

	public static Layer createSearchResultsArea(Collection <User> users,IWResourceBundle iwrb){
		Layer mainSearchResultTableLayer = new Layer();
		mainSearchResultTableLayer.setStyleClass("main-search-table-layer");

		String noUsersByRequest = iwrb.getLocalizedString("no_users_found_by_this_request", "No users found by this request");
		if(ListUtil.isEmpty(users)){
			Label msgLabel = new Label();
			msgLabel.setLabel(noUsersByRequest);
			mainSearchResultTableLayer.add(msgLabel);
			//dublicated code
			GenericButton button = new GenericButton("buttonCancelSearch", iwrb.getLocalizedString("close", "Close"));
			mainSearchResultTableLayer.add(button);
			button.setOnClick("closeSearch()");

			return mainSearchResultTableLayer;
		}

		// Table Title
		String tableTitle = iwrb.getLocalizedString("users_found_by_request", "Users Found By Your Request") + ":";
		mainSearchResultTableLayer.setTitle(tableTitle);
		Label msgLabel = new Label();
		msgLabel.setLabel(tableTitle);
		msgLabel.setStyleClass("table-title");
		mainSearchResultTableLayer.add(msgLabel);

		mainSearchResultTableLayer.add(createSearchTable(users,iwrb));

		GenericButton button = new GenericButton("buttonCancelSearch", iwrb.getLocalizedString("close", "Close"));
		mainSearchResultTableLayer.add(button);
		button.setOnClick("closeSearch()");

		button = new GenericButton(iwrb.getLocalizedString("add_marked_users_to_group", "Add Marked Users To Group"),
				iwrb.getLocalizedString("add", "Add"));
		mainSearchResultTableLayer.add(button);
		button.setOnClick("addChosenUsers()");
		button.setStyleClass("button-add-checked-users");

		return mainSearchResultTableLayer;
	}

	private static Layer createSearchTable(Collection <User> users,IWResourceBundle iwrb){
		Layer tableLayer = new Layer();
		tableLayer.setStyleClass("containerLayer");
		Table2 userTable = SocialGroupCreator.createUserInfoTableByUsers(users, iwrb);

		Label name = new Label();
		name.setLabel(iwrb.getLocalizedString("mark", "Mark"));
		TableHeaderRowGroup header = userTable.createHeaderRowGroup();
		TableRow headerRow = header.getRow(0);
		headerRow.createCell(0).add(name);

		int i = 1;
		for(User user : users){
			TableRow row =  userTable.getRow(i);
			row.setStyleClass("rowOfSearchResultTable");

			//createCheckBox
			CheckBox checkToAdd = new CheckBox();
			StringBuilder actionOnKeyUp = new StringBuilder("checkUserToAdd('")
			.append(checkToAdd.getId())
			.append("', '").append(row.getId())
			.append("'); ");
			checkToAdd.setOnClick(actionOnKeyUp.toString());
			checkToAdd.setValue(user.getId());
			checkToAdd.setStyleClass("checkboxUserToAddIdContainer");
			row.createCell(0).add(checkToAdd);

			i++;
		}

		tableLayer.add(userTable);
		tableLayer.setStyleClass("user-search-table-layer");
		return tableLayer;
	}
	public static Table2 createUserInfoTableByUsers(Collection <User> users,IWResourceBundle iwrb){
		Table2 userTable = new Table2();

		//create header
		TableHeaderRowGroup header= userTable.createHeaderRowGroup();
		TableRow headerRow = header.createRow();

		Label cellInfo = new Label();
		headerRow.createCell().add(cellInfo);

		cellInfo = new Label();
		cellInfo.setLabel(iwrb.getLocalizedString("name", "Name"));
		headerRow.createCell().add(cellInfo);

		cellInfo = new Label();
		cellInfo.setLabel(iwrb.getLocalizedString("email", "Email"));
		headerRow.createCell().add(cellInfo);

		cellInfo = new Label();
		cellInfo.setLabel(iwrb.getLocalizedString("phone", "Phone"));
		headerRow.createCell().add(cellInfo);

		UserApplicationEngine userApplicationEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		// Create table
		for(User user : users){
			TableRow row = userTable.createRow();
			UserDataBean userData = userApplicationEngine.getUserInfo(user);
			row.setMarkupAttribute("title", getViewableUserData(userData));

			Image userImg = new Image(userData.getPictureUri());
			userImg.setStyleClass("userImg");
			row.createCell().add(userImg);

			cellInfo = new Label();
			cellInfo.setLabel(userData.getName());
			row.createCell().add(cellInfo);

			cellInfo = new Label();
			cellInfo.setLabel(userData.getEmail());
			row.createCell().add(cellInfo);

			String phone = userData.getPhone();
			cellInfo = new Label();
			cellInfo.setLabel(StringUtil.isEmpty(phone) ? CoreConstants.MINUS : phone);
			row.createCell().add(cellInfo);

		}
		userTable.setStyleClass("userTable");
		return userTable;
	}

	@SuppressWarnings("unchecked")
	public static Layer createUserTable(int groupId,IWResourceBundle iwrb, String groupName){
		Layer tableLayer = new Layer();
		tableLayer.setStyleClass("user-table-layer");
		String noUsersFoundMsg = iwrb.getLocalizedString("there_are_no_users_in", "There are no users in") + CoreConstants.SPACE + groupName;
		tableLayer.setStyleClass("containerLayer");
		if(groupId == -1){
			Label msgLabel = new Label();
			msgLabel.setLabel(noUsersFoundMsg);
			tableLayer.add(msgLabel);
			return tableLayer;
		}
		GroupBusiness groupBusiness = null;
		try{
			groupBusiness = IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(),GroupBusiness.class);
		}catch(IBOLookupException e){
			Logger.getLogger(SocialGroupCreator.class.getName()).log(Level.WARNING, CoreConstants.EMPTY, e);
			Label msgLabel = new Label();
			msgLabel.setLabel(iwrb.getLocalizedString("server_error", "Server error"));
			tableLayer.add(msgLabel);
			return tableLayer;
		}
		Collection <User> usersOfThisGroup = null;
		try{
			usersOfThisGroup = groupBusiness.getUsersNotDirectlyRelated(groupId);
		}catch(Exception e){
			Logger.getLogger(SocialGroupCreator.class.getName()).log(Level.WARNING, CoreConstants.EMPTY, e);
			Label msgLabel = new Label();
			msgLabel.setLabel(iwrb.getLocalizedString("server_error", "Server error"));
			tableLayer.add(msgLabel);
			return tableLayer;
		}

		if(ListUtil.isEmpty(usersOfThisGroup)){
			Label msgLabel = new Label();
			msgLabel.setLabel(noUsersFoundMsg);
			tableLayer.add(msgLabel);
			return tableLayer;
		}


		Table2 userTable = SocialGroupCreator.createUserInfoTableByUsers(usersOfThisGroup, iwrb);

		Label name = new Label();
		name.setLabel(iwrb.getLocalizedString("click_to_remove", "Click to remove"));
		TableHeaderRowGroup header = userTable.createHeaderRowGroup();
		TableRow headerRow = header.getRow(0);
		headerRow.createCell().add(name);
		// Create table
		int i = 1;
		for(User user : usersOfThisGroup){
			TableRow row =  userTable.getRow(i);
			GenericButton buttonDelete = new GenericButton("buttonDeleteUser",iwrb.getLocalizedString("x", "X"));
			StringBuilder action = new StringBuilder("removeUserFromGroup('").append(user.getId())
			.append("', '").append(row.getId()).append("');");
			buttonDelete.setOnClick(action.toString());
			row.createCell().add(buttonDelete);
			i++;
		}
		String tableTitle = iwrb.getLocalizedString("users_of", "Users Of");
		tableTitle = tableTitle + CoreConstants.SPACE + groupName;
		userTable.setTitle(tableTitle);
		Label msgLabel = new Label();
		msgLabel.setLabel(tableTitle);
		msgLabel.setStyleClass("table-title");
		tableLayer.add(msgLabel);
		tableLayer.add(userTable);
		return tableLayer;
	}


	public void addToUserEditContent(PresentationObject object){
		this.userEditContent.add(object);
	}

	private static String getViewableUserData(UserDataBean userData){
		StringBuilder data = new StringBuilder(" email: ")
		.append(userData.getEmail())
		.append(" login:").append(userData.getLogin());
		return data.toString();
	}

	public boolean isTageditFunctions() {
		return tageditFunctions;
	}

	public void setTageditFunctions(boolean tageditFunctions) {
		this.tageditFunctions = tageditFunctions;
	}
}
