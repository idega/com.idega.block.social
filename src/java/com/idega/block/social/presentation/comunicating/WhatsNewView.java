package com.idega.block.social.presentation.comunicating;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.presentation.group.GroupInfoViewer;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class WhatsNewView  extends IWBaseComponent{


	private IWContext iwc = null;

//	private Layer main = null;

	private static final String FALSE = "false";

	private boolean needFiles = true;

	public WhatsNewView(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);

		this.add(getNewsView(iwc));

		String neededFiles = iwc.getParameter(SocialConstants.NEEDED_SCRIPT_AND_STYLE_FILES);
		if((neededFiles != null) && (neededFiles.equals(FALSE))){
			needFiles = false;
		}
		if(needFiles){
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getNeededScripts(iwc));
			PresentationUtil.addStyleSheetsToHeader(iwc, getNeededStyles(iwc));
		}

	}
	public static UIComponent getNewsView(IWContext iwc){
		IWBundle bundle = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("communicating/WhatsNew.xhtml"));
		return facelet;
	}



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

			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());

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
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/WhatsNewHelper.js"));
		scripts.add("/dwr/interface/SocialServices.js");
		scripts.addAll(GroupInfoViewer.getNeededScripts(iwc));

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
		
		styles.addAll(GroupInfoViewer.getNeededStyles(iwc));

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
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/WhatsNewView.css"));
		return styles;
	}

	public static UIComponent getGroupListView(Collection<Group> groups){
		Lists list = new Lists();
		for(Group group : groups){
			ListItem li = new ListItem();
			list.add(li);

			Link groupLink = new Link();
			li.add(groupLink);

			ArrayList <AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
			parameters.add(new AdvancedProperty(SocialConstants.NEEDED_SCRIPT_AND_STYLE_FILES,FALSE));
			parameters.add(new AdvancedProperty(GroupInfoViewer.GROUP_ID_PARAMETER,group.getId()));

			String uri = BuilderLogic.getInstance().getUriToObject(GroupInfoViewer.class, parameters);
			groupLink.setURL(uri);
			groupLink.setStyleClass("whats-new-view-group-info-preview-link");
			groupLink.addToText(group.getName());
		}
		return list;
	}
}
