package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.google.gson.Gson;
import com.idega.block.social.SocialConstants;
import com.idega.block.social.presentation.SocialUIBase;
import com.idega.content.upload.presentation.UploadArea;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.user.presentation.user.UserAutocomplete;

public class ConversationCreator extends SocialUIBase{

	private String receiversFunction;
	private Map<String, String> presentationOptions;
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		if(getTag() == null){
			setTag("div");
		}
		String presentationMapString;
		Map<String, String> presentationOptions = getPresentationOptions();
		if(presentationOptions == null){
			presentationMapString = "{}";
		}else{
			presentationMapString = new Gson().toJson(presentationOptions);
		}
		getScriptOnLoad().append("ConversationCreator.initialize('").append(getId())
				.append("',").append(presentationMapString).append(",").append(getReceiversFunction())
				.append(");");
	}
	
	@Override
	public List<String> getScripts() {
		IWContext iwc = getIwc();
		List<String> scripts = new ArrayList<String>();
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/posts/conversation-creator.js"));
		
		//TODO: this should be load with lazyloading somehow, but it don't work at this time
		Conversation conversation = new Conversation();
		MessageList msglist = new MessageList();
		UploadArea uploadArea = new UploadArea();
		scripts.addAll(conversation.getScripts());
		scripts.addAll(msglist.getScripts());
		scripts.addAll(uploadArea.getScriptFiles(getIwc()));
		MessageCreator msgCreator = new MessageCreator();
		scripts.addAll(msgCreator.getScripts());
		UserAutocomplete userAutocomplete = new UserAutocomplete();
		scripts.addAll(userAutocomplete.getScripts());
		return scripts;
	}

	@Override
	public List<String> getStyleSheets() {
		List<String> styles = new ArrayList<String>();
		
		//TODO: this should be load with lazyloading somehow, but it don't work at this time
		Conversation conversation = new Conversation();
		styles.addAll(conversation.getStyleSheets());
		MessageList msglist = new MessageList();
		styles.addAll(msglist.getStyleSheets());
		UploadArea uploadArea = new UploadArea();
		styles.addAll(uploadArea.getStyleFiles(getIwc()));
		MessageCreator msgCreator = new MessageCreator();
		styles.addAll(msgCreator.getStyleSheets());
		UserAutocomplete userAutocomplete = new UserAutocomplete();
		styles.addAll(userAutocomplete.getStyleSheets());
		
		return styles;
	}

	public String getReceiversFunction() {
		return receiversFunction;
	}

	public void setReceiversFunction(String receiversFunction) {
		this.receiversFunction = receiversFunction;
	}

	public Map<String, String> getPresentationOptions() {
		return presentationOptions;
	}

	public void setPresentationOptions(Map<String, String> presentationOptions) {
		this.presentationOptions = presentationOptions;
	}

}
