package com.idega.block.social.presentation.group;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.social.Constants;
import com.idega.block.social.business.SocialServices;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class GroupInfoViewer  extends IWBaseComponent{

	public static final String GROUP_ID_PARAMETER = "group_id_parameter";

	private IWContext iwc = null;
	private IWResourceBundle iwrb = null;

	private Layer main = null;

	private static final String FALSE = "false";

	private boolean needFiles = true;


	@Autowired
	private SocialServices socialservices;

	public GroupInfoViewer(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		iwrb = this.getBundle(context, Constants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

		main = new Layer();
		this.add(main);

		Group group = getGroup();
		if(group == null){
			main.addText(iwrb.getLocalizedString("error_getting_group", "Error getting group"));
			return;
		}



		Heading1 title = new Heading1();
		main.add(title);
		title.addToText(group.getName());

		Layer description = new Layer();
		main.add(description);
		description.addText(group.getDescription());

		String neededFiles = iwc.getParameter(Constants.NEEDED_SCRIPT_AND_STYLE_FILES);
		if((neededFiles != null) && (neededFiles.equals(FALSE))){
			needFiles = false;
		}
		if(needFiles){
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getNeededScripts(iwc));
			PresentationUtil.addStyleSheetsToHeader(iwc, getNeededStyles(iwc));
		}

//		this.addActions();
	}

	private Group getGroup(){
		String groupId = iwc.getParameter(GROUP_ID_PARAMETER);
		if(groupId == null){
			return null;
		}
		Group group = null;
		try{
			group =	socialservices.getGroupBusiness().getGroupByGroupID(Integer.valueOf(groupId));
		}catch(Exception e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting group with id " + groupId, e);
		}

		return group;


	}

//	private void addActions(){
////		StringBuilder actions = new StringBuilder();
////		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
////		main.add(actionString);
//	}


	/**
	 * Gets the scripts that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * scriptFiles.
	 * @return script files uris
	 */
	public static List<String> getNeededScripts(IWContext iwc){
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","js/jquery-ui-1.8.14.custom.min.js"));

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery.autoresizev-textarea.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
			}catch(RemoteException e){
				Logger.getLogger("PostcreationView").log(Level.WARNING,CoreConstants.EMPTY,e);
			}
		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/WhatsNewHelper.js"));

		return scripts;
	}

	/**
	 * Gets the stylesheets that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * files.
	 * @return style files uris
	 */
	public static List<String> getNeededStyles(IWContext iwc){

		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();

			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			styles.add(jQuery.getBundleURIToJQueryUILib("1.8.14","css/ui-lightness/jquery-ui-1.8.14.custom.css"));

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/WhatsNewView.css"));
		return styles;
	}

	public static UIComponent getGroupListView(Collection<Group> groups){
		Lists list = new Lists();
		for(Group group : groups){
			ListItem li = new ListItem();
			list.add(li);

			li.addText(group.getName());
		}
		return list;
	}
}

