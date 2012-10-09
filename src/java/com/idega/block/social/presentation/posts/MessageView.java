package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.business.SocialServices;
import com.idega.block.social.presentation.SocialUIBase;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.ui.Label;
import com.idega.user.presentation.user.UserAutocomplete;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class MessageView extends SocialUIBase {
	
	@Autowired
	private SocialServices socialServices;
	
	private StringBuilder scriptOnLoad = null;
	
	private Integer maxToShow = null;
	
	private int teaserLength = 200;
	
	private static final int DEFAULT_MAX_TO_SHOW = 20;
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		IWContext iwc = getIwc(context);
		IWResourceBundle iwrb = getIwrb();
		if(!iwc.isLoggedOn()){
			Layer msg = new Layer();
			add(msg);
			msg.add(iwrb.getLocalizedString("not_logged_in", "Not logged in"));
			return;
		}
		ELUtil.getInstance().autowire(this);
		Layer topControlls = new Layer("form");
		add(topControlls);
		topControlls.setStyleClass("top-controlls");
		topControlls.setMarkupAttribute("onsubmit", "return false;");
		
		
		Layer creatorLink = new Layer("a");
		topControlls.add(creatorLink);
		creatorLink.setMarkupAttribute("href", "#");
		
		Label messageTo = new Label();
		topControlls.add(messageTo);
		messageTo.add(iwrb.getLocalizedString("write_message_to", "Write message to")+CoreConstants.COLON);
		
		String userInputName = "chat-user";
		UserAutocomplete userAutocomplete = new UserAutocomplete();
		topControlls.add(userAutocomplete);
		userAutocomplete.setMarkupAttribute("name", userInputName);
		
		
		
		StringBuilder receiversFunction = new StringBuilder("function(){")
		.append("\n\t\n\tvar receiversInputs = jQuery('#").append(topControlls.getId())
		.append("').find('[name=\"").append(userInputName).append("\"]');")
		.append("var receivers = [];")
		.append("for(var i = 0;i<receiversInputs.length;i++){receivers.push(jQuery(receiversInputs[i]).val());}")
		.append("return receivers;}");
		ConversationCreator conversationCreator = new ConversationCreator();
		topControlls.add(conversationCreator);
		conversationCreator.setReceiversFunction(receiversFunction.toString());
		
		Layer button = new Layer("button");
		conversationCreator.add(button);
		button.setStyleClass("btn btn-primary");
		Span icon = new Span();
		button.add(icon);
		icon.setStyleClass("icon-envelope icon-white");
		button.setMarkupAttribute("title", iwrb.getLocalizedString("create_message", "Create message"));
		button.add(iwrb.getLocalizedString("start_conversation", "Start conversation"));
		
//		getScriptOnLoad().append("{var getReceivers = function(){")
//				.append("\n\t\n\tvar receiversInputs = jQuery('#").append(topControlls.getId())
//				.append("').find('[name=\"").append("chat-user").append("\"]);")
//				.append("\n\tfor(var i = 0;")
//				.append("\n\t\tLastMessagesList.createConversationPreview(jQuery('#").append(creatorLink.getId()).append("'),")
//				.append("{}")
//				.append(",\n\t\t\[]")
//				.append(");")
//				.append("\n\tjQuery('#").append(button.getId()).append("').click(function(){")
//				.append("\n\t\tvar comp = jQuery('#").append(creatorLink.getId()).append("'); \n\t\tcomp.click();")
//				.append("\n\t});}");
		
//		MessageCreator messageCreator = new MessageCreator();
//		add(messageCreator);
		LastMessagesList messageList = new LastMessagesList();
		add(messageList);
		messageList.setStyleClass("post-list");
		messageList.setPostFilterParameters(getPostFilterParameters(iwc));
		messageList.setTeaserLength(getTeaserLength());
		messageList.getPostFilterParameters().setMax(10);
		
		
		Layer footer = new Layer();
		footer.setStyleClass("public-posts-footer");
		add(footer);
	}
	
	@SuppressWarnings("unchecked")
	private PostFilterParameters getPostFilterParameters(IWContext iwc){
		PostFilterParameters postFilterParameters = new PostFilterParameters();
		Collection<Integer> receivers;
		try{
			receivers = CoreUtil.getIdsAsIntegers(socialServices.getUserBusiness().getUserGroups(iwc.getCurrentUser()));
		}catch (Exception e) {
			receivers = Collections.emptyList();
		}
		postFilterParameters.setReceivers(receivers);
		postFilterParameters.setMax(getMaxToShow());
		return postFilterParameters;
	}
	
	public int getMaxToShow() {
		if(maxToShow == null){
			return DEFAULT_MAX_TO_SHOW;
		}
		return maxToShow;
	}

	public void setMaxToShow(int maxToShow) {
		this.maxToShow = maxToShow;
	}

	@Override
	protected StringBuilder getScriptOnLoad() {
		return super.getScriptOnLoad();
//		if(scriptOnLoad == null){
//			scriptOnLoad = new StringBuilder("jQuery(document).ready(function(){");
//		}
//		return scriptOnLoad;
	}

	@Override
	protected void setScriptOnLoad(StringBuilder scriptOnLoad) {
		super.setScriptOnLoad(scriptOnLoad);
//		this.scriptOnLoad = scriptOnLoad;
	}

	public int getTeaserLength() {
		return teaserLength;
	}

	public void setTeaserLength(int teaserLength) {
		this.teaserLength = teaserLength;
	}

	@Override
	public List<String> getScripts() {
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		IWContext iwc = getIwc();
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		try{
			if (web2 != null) {
				JQuery  jQuery = web2.getJQuery();
				scripts.add(jQuery.getBundleURIToJQueryLib());
	
	
				scripts.add(web2.getBundleUriToHumanizedMessagesScript());
				scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
				
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery.autoresizev-textarea.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
	
			}else{
				Logger.getLogger(PublicPostViewer.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
			}
		}
		catch (Exception e) {
			Logger.getLogger(PublicPostViewer.class.getName()).log(Level.WARNING, "Failed adding scripts no jQuery and it's plugins files were added");
		}
		
		scripts.add("/dwr/interface/SocialServices.js");

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/posts/PublicPostViewerHelper.js"));

		return scripts;
	}

	@Override
	public List<String> getStyleSheets() {
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(getIwc(), Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			styles.add(web2.getBundleURIToFancyBoxStyleFile());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
			styles.add(web2.getBundleUriToBootstrapMainStyleFile("2.0.4"));
		}else{
			Logger.getLogger(PublicPostViewer.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		return styles;
	}
}
