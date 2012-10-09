package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.presentation.SocialUIBase;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.webface.WFUtil;

public class Conversation extends SocialUIBase {
	
	private Collection<Integer> conversationWith;
	
	private Map<String,String> presentationOptions;
	
	public Conversation(){
		
	}
	
	public Conversation(Map<String,String> presentationOptions){
		
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		setTag("div");
		IWContext iwc = getIwc(context);
		IWResourceBundle iwrb = getIwrb();
		if(!iwc.isLoggedOn()){
			Layer msg = new Layer();
			add(msg);
			msg.add(iwrb.getLocalizedString("not_logged_in", "Not logged in"));
			return;
		}
		
		setMarkupAttribute("class", "conversation");
		getScriptOnLoad().append("\n\talert('script loaded TODO: remove this');");
		
		
		MessageCreator messageCreator = new MessageCreator();
		add(messageCreator);
		messageCreator.setMarkupAttribute("class", "messages-creator");
		messageCreator.setReceivers(getConversationWith());
		
		MessageList messageList = new MessageList();
		add(messageList);
		messageList.setPostFilterParameters(getPostFilterParameters(iwc));
		messageList.setPresentationOptions(getPresentationOptions());
		
		String msgListId = messageList.getId();
		StringBuilder callback = new StringBuilder("jQuery(\"#").append(msgListId).append("\").trigger(\"set-post-receivers\",post.receivers);")
				.append("jQuery(\"#").append(msgListId).append("\").trigger(\"prepend-posts\");");
		
		messageCreator.setCallback(callback);
		Layer footer = new Layer();
		add(footer);
		footer.setStyleClass("post-editor");
		
//		getScriptOnLoad().append("\n\tjQuery('#").append(getId()).append("').resize(function(){")
//				.append("\n\t\tvar main = jQuery(this);")
//				.append("\n\t\tvar editor = main.find('.message-creator');")
//				.append("\n\t\tvar list = main.find('.post-list');")
//				.append("\n\t\tvar height = main.outerHeight();")
//				.append("\n\t\teditor.resize();list.height(height - editor.outerHeight() - 50);alert('resized ' + height + ' '+ editor.outerHeight() + ' ' + 50);")
//				.append("\n\t});")
//				.append("\n\tjQuery('#").append(getId()).append("').resize();");
	}
	
	private PostFilterParameters getPostFilterParameters(IWContext iwc){
		PostFilterParameters postFilterParameters = new PostFilterParameters();
		if(!ListUtil.isEmpty(conversationWith)){
			postFilterParameters.setReceivers(conversationWith);
		}
		postFilterParameters.setMax(4);
		return postFilterParameters;
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

	public Collection<Integer> getConversationWith() {
		return conversationWith;
	}

	public void setConversationWith(Collection<Integer> conversationWith) {
		this.conversationWith = conversationWith;
	}

	public Map<String, String> getPresentationOptions() {
		return presentationOptions;
	}

	public void setPresentationOptions(Map<String, String> presentationOptions) {
		this.presentationOptions = presentationOptions;
	}
}