package com.idega.block.social.presentation.group;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.idega.block.social.SocialConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.presentation.group.GroupTreeViewer;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;

public class GroupEditor extends GroupTreeViewer {
	
	private IWResourceBundle iwrb = null;
	private StringBuilder errorMsg = null;

	private boolean tageditFunctions = true;

	private boolean userGroupsAsRootGroups;
	
	private Group rootGroup;
	
	private Collection<Group> getRootGroups(IWContext iwc) throws RemoteException {
		GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
		
		if (rootGroup != null) {
			return Arrays.asList(rootGroup);
		} else if (isUserGroupsAsRootGroups() && iwc.isLoggedOn()) {
			UserBusiness userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			@SuppressWarnings("unchecked")
			Collection<Group> userGroups = userBusiness.getUserGroups(iwc.getCurrentUser());
			return userGroups;
		} else {
			@SuppressWarnings("unchecked")
			Collection<Group> socialRootGroups = groupBusiness.getGroupsByGroupName(SocialConstants.SOCIAL_ROOT_GROUP_NAME);
			return socialRootGroups;
		}
	}
	
	@Override
	public void main(IWContext iwc) {
		iwrb = getBundle(iwc).getResourceBundle(iwc);

		Layer main = new Layer();
		main.setName("GroupEditor");

		Collection<Group> groups = null;
		try {
			groups = getRootGroups(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (groups == null)
			groups = Collections.emptyList();
		
		String containerId = "UniqueGroupTreeContainerId15647989";
		this.setGroupsTreeViewerId(containerId);
		StringBuilder treeLoadScript = new StringBuilder("loadSagaBookGroupsTree('")
		.append(containerId)
		.append("', '").append(iwrb.getLocalizedString("there_are_no_groups_available", "There are no groups available"))
		.append("', '").append(groups.iterator().next().getUniqueId())
		.append("', '").append(this.getStyleClass())
		.append("');");
		this.setLoadRemoteGroupsFunction(treeLoadScript.toString());
		super.main(iwc);

		//creating "create" button
		//parameters for button action function
		if (!ListUtil.isEmpty(groups)) {
			StringBuffer parameters = new StringBuffer("openGroupCreationDialog('");
			parameters.append(BuilderLogic.getInstance().getUriToObject(SocialGroupCreator.class))
			.append("','").append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)	//set to selected group or -1
			.append("','").append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)
			.append("');");
			GenericButton create = new GenericButton("buttonCreateGroup", iwrb.getLocalizedString("create_group", "Create group"));
			create.setOnClick(parameters.toString());
			main.add(create);
		}
		
		//creating "edit" button
		//parameters for button action function
		StringBuffer parameters = new StringBuffer("openGroupEditDialog('");
		parameters.append(BuilderLogic.getInstance().getUriToObject(SocialGroupCreator.class))
		.append("','").append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)	//set to selected group or -1
		.append("','").append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)
		.append("');");
		GenericButton edit = new GenericButton("buttonEditGroup", iwrb.getLocalizedString("edit", "Edit"));
		edit.setOnClick(parameters.toString());
		edit.setStyleClass("editTool");
		edit.setStyleAttribute("display:none");
		main.add(edit);
		add(main);

		addFiles(iwc,main.getId());

		addHumanizedErrorMsgScript(main);

//		Layer treeContainer = new Layer();
//		this.add(treeContainer);
//		treeContainer.setStyleAttribute("height : 200px; width:300px; overflow:auto;");
//		treeContainer.setStyleClass("dynamicTreeContainerClass");
//
//		treeContainer.add(GroupEditor.createTreeView(0,new ArrayList<Integer>(0),10));
	}

	//TODO: make this work :D
	public static Layer createTreeView(int scrollPos, Collection<Integer> openedGroups,int maxGroupsToShow){
		Layer treeContainerContent = new Layer();
//		SocialServices SocialServices = ELUtil.getInstance().getBean(SocialServices.class);
//		GroupService groupService =   ELUtil.getInstance().getBean(GroupService.class);
//		GroupBusiness groupBusiness = ELUtil.getInstance().getBean(GroupBusiness.class);//SocialServices.getSagaRootGroup().getUniqueId()
//		ArrayList topGroups = new ArrayList(groupService.getChildrenOfGroup("10"));
//		for(Integer i : openedGroups){
//			int pos = 0;
//			Group group = null;
//			try{
//				group = groupBusiness.getGroupByGroupID(i);
//				pos = GroupEditor.getIndexOfInsertion(topGroups, group);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			topGroups.addAll(pos, groupService.getChildrenOfGroup(group.getUniqueId()));
//		}
//		int height = 15 * topGroups.size();
//		treeContainerContent.setStyleAttribute("height :"+ height +"px; width:280px;");
		treeContainerContent.setStyleAttribute("height :10000px; width:280px;");
		return treeContainerContent;
	}
//	private void addErrorMsg(String errorMsg){
//		if(this.errorMsg == null){
//			this.errorMsg = new StringBuilder(errorMsg);
//			return;
//		}
//		this.errorMsg.append(", ").append(errorMsg);
//	}

	private void addHumanizedErrorMsgScript(Layer container){
		if(errorMsg == null){
			return;
		}
		StringBuilder actionString = new StringBuilder("showHumanizedMessage('")
		.append(errorMsg)
		.append("');\n");
		String action = PresentationUtil.getJavaScriptAction(actionString.toString());
		container.add(action);
	}

	private void addFiles(IWContext iwc, String id){
		if(id == null){
			return;
		}

		List<String> scripts = new ArrayList<String>();
		List<String> styles = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add("/dwr/interface/UserApplicationEngine.js");
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		//needed for fancybox
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			scripts.add(web2.getJQuery().getBundleURIToJQueryLib());
			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","js/jquery-ui-1.8.14.custom.min.js"));
			scripts.addAll(web2.getBundleURIsToTageditLib());
			styles.addAll(web2.getBundleURIsToTageditStyleFiles());
			styles.add(jQuery.getBundleURIToJQueryUILib("1.8.14","css/ui-lightness/jquery-ui-1.8.14.custom.css"));

			scripts.add(web2.getBundleUriToMootabsScript());
			styles.add(web2.getBundleUriToMootabsStyle());
		}else{
			this.log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWBundle bundle = getBundle(iwc);
//		//user com.idega.user styles
//		IWBundle userBundle = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
//		styles.add(userBundle.getVirtualPathWithFileNameString("style/user.css"));
//		styles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));

		//user com.idega.user scripts
		scripts.add(bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/GroupEditorHelper.js"));



		//Saga styles
		styles.add(iwb.getVirtualPathWithFileNameString("style/group.css"));

		scripts.add("/dwr/engine.js");
		scripts.add("/dwr/interface/SocialServices.js");

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);

	}

	public void setTageditFunctions(boolean tageditFunctions){
		this.tageditFunctions = tageditFunctions;
	}

	public boolean isTageditFunctions() {
		return tageditFunctions;
	}

	public boolean isUserGroupsAsRootGroups() {
		return userGroupsAsRootGroups;
	}

	public void setUserGroupsAsRootGroups(boolean userGroupsAsRootGroups) {
		this.userGroupsAsRootGroups = userGroupsAsRootGroups;
	}

	public Group getRootGroup() {
		return rootGroup;
	}

	public void setRootGroup(Group rootGroup) {
		this.rootGroup = rootGroup;
	}

}