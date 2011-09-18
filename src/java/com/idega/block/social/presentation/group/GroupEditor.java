package com.idega.block.social.presentation.group;




import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import com.idega.block.social.Constants;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.presentation.group.GroupTreeViewer;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;

public class GroupEditor extends GroupTreeViewer{
	private IWResourceBundle iwrb = null;
	private StringBuilder errorMsg = null;

	private boolean tageditFunctions = true;

	@SuppressWarnings("unchecked")
	@Override
	public void main(IWContext iwc) {
		iwrb = getBundle(iwc).getResourceBundle(iwc);

		Layer main = new Layer();
		main.setName("GroupEdittor");

		GroupBusiness groupBusiness = null;
		try{
			groupBusiness = IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(),GroupBusiness.class);
		}catch(IBOLookupException e){
			this.getLogger().log(Level.WARNING, CoreConstants.EMPTY, e);
			addErrorMsg(iwrb.getLocalizedString("failed_to_add_script_toload_group_tree","Failed loading group tree"));
		}
		Collection <Group> sagaRootGroups = null;
		try{
			sagaRootGroups = groupBusiness.getGroupsByGroupName(Constants.SAGA_ROOT_GROUP_NAME);
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "Failed to call remote method", e);
			String serverError = iwrb.getLocalizedString("server_error", "Server error");
			String error = iwrb.getLocalizedString("failed_to_call_remote_method", "failed to call remote method");
			addErrorMsg(serverError + " : " + error);
		}

		String containerId = "UniqueGroupTreeContainerId15647989";
		this.setGroupsTreeViewerId(containerId);
		StringBuilder treeLoadScript= new StringBuilder("loadSagaBookGroupsTree('")
		.append(containerId)
		.append("', '").append(iwrb.getLocalizedString("there_is_no_groups_in_saga_groups", "There is no groups in saga book"))
		.append("', '").append(sagaRootGroups.iterator().next().getUniqueId())
		.append("', '").append(this.getStyleClass())
		.append("'); ");
		this.setLoadRemoteGroupsFunction(treeLoadScript.toString());
		super.main(iwc);

		//creating "create" button
		//parameters for button action function
		StringBuffer parameters = new StringBuffer("openGroupCreationDialog('");
		parameters.append(BuilderLogic.getInstance().getUriToObject(SocialGroupCreator.class))
		.append("','").append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)	//set to selected group or -1
		.append("','").append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)
		.append("')");
		GenericButton button = new GenericButton("buttonCreateGroup", iwrb.getLocalizedString("create", "Create"));
		button.setOnClick(parameters.toString());
		main.add(button);

		//creating "edit" button
		//parameters for button action function
		parameters = new StringBuffer("openGroupEditDialog('");
		parameters.append(BuilderLogic.getInstance().getUriToObject(SocialGroupCreator.class))
		.append("','").append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)	//set to selected group or -1
		.append("','").append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION)
		.append("')");
		button = new GenericButton("buttonEditGroup", iwrb.getLocalizedString("edit", "Edit"));
		button.setOnClick(parameters.toString());
		button.setStyleClass("editTool");
		button.setStyleAttribute("display:none");
		main.add(button);
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
	private void addErrorMsg(String errorMsg){
		if(this.errorMsg == null){
			this.errorMsg = new StringBuilder(errorMsg);
			return;
		}
		this.errorMsg.append(", ").append(errorMsg);
	}

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
			this.log(Level.WARNING, "Failed getting Web2Business no jQuery and it's pligins files were added");
		}

		IWBundle bundle = getBundle(iwc);
//		//user com.idega.user styles
//		IWBundle userBundle = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
//		styles.add(userBundle.getVirtualPathWithFileNameString("style/user.css"));
//		styles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));

		//user com.idega.user scripts
		scripts.add(bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
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

}
