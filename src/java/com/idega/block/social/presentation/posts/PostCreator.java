package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.presentation.SocialUIBase;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.content.upload.presentation.UploadArea;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.util.CoreConstants;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class PostCreator  extends SocialUIBase {
	
	
	private StringBuilder defaultValuesString = null;
	
	private static final String DEFAULT_VALUES_FUNCTION = "PostCreator.setDefaultValues";
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		getIwc(context);
		setTag("form");
		addUI();
	}
	
	@Override
	protected void addScriptOnLoad(){
		if(defaultValuesString != null){
			defaultValuesString.append("\n\t}");
			getScriptOnLoad().append("\n\t").append(defaultValuesString);
		}
		getScriptOnLoad().append("\n\t").append(DEFAULT_VALUES_FUNCTION).append("();");
		super.addScriptOnLoad();
	}
	
	protected UIComponent getBodyArea(IWResourceBundle iwrb){
		TextArea postBody = new TextArea(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		postBody.setMarkupAttribute("placeholder", iwrb.getLocalizedString("write_text_or_drop_files", "Write text or drop files"));
		postBody.setContent(CoreConstants.EMPTY);
		postBody.setStyleClass("empty");
		String postBodyId = postBody.getId();
		getDefaultValuesString().append("\n\t\tjQuery('#").append(postBodyId).append("').val('');");
		getDefaultValuesString().append("\n\t\tjQuery('#").append(postBodyId).append("').keyup();"); // to get area small like it was (it is autoresize)
		getScriptOnLoad().append("\n\tjQuery('#").append(postBodyId).append("').autoResize({extraSpace : 30, animate : false });");
		postBody.setStyleClass("post-content-viewer-post-creation-form-body");
		return postBody;
	}
	
	private void addUI(){
		IWResourceBundle iwrb = getIwrb();
		addStyleClass("post-creation-form");

		Layer postContentEditor = new Layer();
		add(postContentEditor);
		postContentEditor.setStyleClass("post-content-editor navbar-inner");
		
		UIComponent postBody = getBodyArea(iwrb);
		postContentEditor.add(postBody);
		
		// Controlls
		Layer editorControls = new Layer();
		postContentEditor.add(editorControls);
		editorControls.setStyleClass("post-content-editor-controls");
		UploadArea uploadArea = new UploadArea();
		editorControls.add(uploadArea);
		PostItemBean postItemBean = ELUtil.getInstance().getBean(PostItemBean.BEAN_NAME);
		String resourcePath = postItemBean.getResourcePath();
		
		HiddenInput resourcePathInput = new HiddenInput(SocialConstants.POST_URI_PARAMETER,resourcePath);
		add(resourcePathInput);
		
		uploadArea.setUploadPath(postItemBean.getFilesResourcePath());
		uploadArea.setDropZonesSelectionFunction("jQuery('#"+ getId() +"')");
		uploadArea.setAutoUpload(true);
		uploadArea.setName(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		
		Layer fileListLayer = new Layer();
		postContentEditor.add(fileListLayer);
		uploadArea.setFilesListContainerSelectFunction(new StringBuilder("jQuery('#").append(fileListLayer.getId()).append("');").toString());
		
		
		SubmitButton postButton = new SubmitButton();
		editorControls.add(postButton);
		postButton.setValue(iwrb.getLocalizedString("send", "Send"));
		postButton.setStyleClass("btn btn-primary send-btn");
		
		StringBuilder action = new StringBuilder("PostCreator.createPost('#").append(getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(uploadArea.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(resourcePathInput.getId())
				.append(CoreConstants.JS_STR_PARAM_END).append("return false;");
		setMarkupAttribute("onsubmit", action.toString());
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
				getLogger().log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
			}
		}
		catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed adding scripts no jQuery and it's plugins files were added");
		}
		
		scripts.add("/dwr/interface/SocialServices.js");

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/posts/post-creator.js"));

		return scripts;
	}
	
	@Override
	public List<String> getStyleSheets() {
		List<String> styles = new ArrayList<String>();
		IWContext iwc = getIwc();
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			styles.add(web2.getBundleURIToFancyBoxStyleFile());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
		}else{
			getLogger().log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		return styles;
	}
	
	private StringBuilder getDefaultValuesString() {
		if(defaultValuesString == null){
			defaultValuesString = new StringBuilder(DEFAULT_VALUES_FUNCTION).append(" = function(){");
		}
		return defaultValuesString;
	}
	
	@Override
	protected Logger getLogger(){
		return Logger.getLogger(this.getClass().getName());
	}

}