package is.idega.block.saga.presentation.group;

import is.idega.block.saga.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.user.business.UserConstants;
import com.idega.user.presentation.group.GroupTreeViewer;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;



public class GroupEditor extends GroupTreeViewer{

	@Override
	public void main(IWContext iwc) {
		Layer main = new Layer();
		main.setName("GroupEdittor");

		//creating "create" button
		//parameters for button action function
		StringBuffer parameters = new StringBuffer("openGroupCreationDialog('");
		parameters.append(BuilderLogic.getInstance().getUriToObject(SagaGroupCreator.class)).append("'");
		parameters.append(",'").append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append("'");	//set to selected group or -1
		parameters.append(")");
		GenericButton button = new GenericButton("buttonCreateGroup", "Create");
		button.setOnClick(parameters.toString());
		main.add(button);

		//creating "edit" button
		//parameters for button action function
		parameters = new StringBuffer("openGroupEditDialog('");
		parameters.append(BuilderLogic.getInstance().getUriToObject(SagaGroupCreator.class)).append("'");
		parameters.append(",'").append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append("'");		//set to -1 for creating
		parameters.append(")");
		button = new GenericButton("buttonEditGroup", "Edit");
		button.setOnClick(parameters.toString());
		main.add(button);



		add(main);


		super.main(iwc);

		addFiles(iwc,main.getId());

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
		}else{
			this.log(Level.WARNING, "Failed getting Web2Business no fancybox files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/GroupEditorHelper.js"));

		IWBundle bundle = getBundle(iwc);
		//user com.idega.user styles
		styles.add(bundle.getVirtualPathWithFileNameString("style/user.css"));
		styles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));

		//user com.idega.user scripts
		scripts.add(bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));

		//Saga styles
		styles.add(iwb.getVirtualPathWithFileNameString("style/group.css"));

		scripts.add("/dwr/engine.js");
		scripts.add("/dwr/interface/SagaServices.js");

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);

	}

}
