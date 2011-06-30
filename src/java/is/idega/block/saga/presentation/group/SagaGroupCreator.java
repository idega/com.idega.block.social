package is.idega.block.saga.presentation.group;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.app.SimpleGroupCreator;
import com.idega.user.business.GroupBusiness;
import com.idega.util.PresentationUtil;

public class SagaGroupCreator extends SimpleGroupCreator{
	protected String userEditTab = "userEditTab";

	private String userEditClass = "userEditClass";
	private IWContext iwc = null;

	@Override
	public void main(IWContext iwc) throws IOException {
		this.iwc = iwc;
		Layer container = new Layer();
		add(container);

		//creating tabs
		Lists titlesForTabs = new Lists();
		container.add(titlesForTabs);
		titlesForTabs.setStyleClass("mootabs_title");

		ListItem groupTab = new ListItem();
		groupTab.addText("Group");
		groupTab.setMarkupAttribute("title", this.groupTab);
		titlesForTabs.setStyleClass("SagaNavigationTabs");
		titlesForTabs.add(groupTab);

		groupTab = new ListItem();
		groupTab.addText("Users");
		groupTab.setMarkupAttribute("title", this.userEditTab);
		titlesForTabs.setStyleClass("SagaNavigationTabs");
		titlesForTabs.add(groupTab);

		Layer userEditContent = new Layer();
		container.add(userEditContent);
		userEditContent.setId(this.userEditTab);
		userEditContent.setStyleClass(this.mootabsPanel);
		userEditContent.setStyleClass(this.userEditClass);

		super.main(iwc);

		String action = PresentationUtil.getJavaScriptAction("setGroupNameFieldId('".concat(this.nameField.getId()).concat("');"));
		container.add(action);

		StringBuilder actionOnKeyUp = new StringBuilder("checkIfNameExists('").append(this.nameField.getId())
		.append("')");
		this.nameField.setOnKeyUp(actionOnKeyUp.toString());
		addFiles(iwc);
	}

	public Boolean isAllowedToSave(String name){
		if(this.iwc == null){
			this.log(Level.WARNING, "SagaGroupCreator is probably being used before  main() is called");
			return Boolean.FALSE;
		}
		GroupBusiness groupBusiness = null;
		try{
			groupBusiness = IBOLookup.getServiceInstance(this.iwc,GroupBusiness.class);
			return  groupBusiness.getGroupsByGroupName(name).isEmpty();
		}catch(RemoteException e){
			this.log(e);
		}

		return Boolean.FALSE;
	}

	private void addFiles(IWContext iwc){
//		IWBundle bundle = getBundle(iwc);
		List<String> files = new ArrayList<String>();
//
//		//user com.idega.user styles
//		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/user.css"));
//		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));
//
//		//Saga styles
//		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
//		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
//		cssFiles.add(iwb.getVirtualPathWithFileNameString("style/group.css"));
//		PresentationUtil.addStyleSheetsToHeader(iwc, cssFiles);
		files.add("/dwr/engine.js");
		files.add("/dwr/interface/SagaServices.js");
		this.add(files);
	}
}
