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
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.content.upload.presentation.UploadArea;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.TextArea;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class PostCreator  extends IWBaseComponent {
	
	
	private StringBuilder scriptOnLoad = null;
	private StringBuilder defaultValuesString = null;
	
	private static final String DEFAULT_VALUES_FUNCTION = "PostCreator.setDefaultValues";
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		IWContext iwc = IWContext.getIWContext(context);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		add(getPostingForm(iwc, iwrb));
		
		addFiles(iwc);
		addScriptOnLoad(iwc);
	}
	
	private void addScriptOnLoad(IWContext iwc){
		Layer scriptLayer = new Layer();
		add(scriptLayer);
		if(defaultValuesString != null){
			defaultValuesString.append("\n}");
			scriptLayer.add(PresentationUtil.getJavaScriptAction(defaultValuesString.toString()));
		}
		StringBuilder scriptOnLoad = getScriptOnLoad().append("\n\t").append(DEFAULT_VALUES_FUNCTION).append("();");
		
		scriptOnLoad.append("\n});");
		scriptLayer.add(PresentationUtil.getJavaScriptAction(scriptOnLoad.toString()));
	}
	
	protected UIComponent getBodyArea(IWResourceBundle iwrb){
		TextArea postBody = new TextArea(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		postBody.setMarkupAttribute("placeholder", iwrb.getLocalizedString("write_text_or_drop_files", "Write text or drop files"));
		postBody.setContent(CoreConstants.EMPTY);
		postBody.setStyleClass("empty");
		String postBodyId = postBody.getId();
		getDefaultValuesString().append("\n\tjQuery('#").append(postBodyId).append("').val('');");
		getDefaultValuesString().append("\n\tjQuery('#").append(postBodyId).append("').keyup();"); // to get area small like it was (it is autoresize)
		getScriptOnLoad().append("jQuery('#").append(postBodyId).append("').autoResize({extraSpace : 30, animate : false });");
		postBody.setStyleClass("post-content-viewer-post-creation-form-body");
		return postBody;
	}
	
	private UIComponent getPostingForm(IWContext iwc, IWResourceBundle iwrb){
		Form form = new Form();
		form.setStyleClass("public-post-form");
		form.setOnSubmit("return false;");


		Layer postContentEditor = new Layer();
		form.add(postContentEditor);
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
		form.add(resourcePathInput);
		
		uploadArea.setUploadPath(postItemBean.getFilesResourcePath());
		uploadArea.setDropZonesSelectionFunction("jQuery('#"+ form.getId() +"')");
		uploadArea.setAutoUpload(true);
		uploadArea.setName(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		
		Layer fileListLayer = new Layer();
		postContentEditor.add(fileListLayer);
		uploadArea.setFilesListContainerSelectFunction(new StringBuilder("jQuery('#").append(fileListLayer.getId()).append("');").toString());
		
		
		GenericButton postButton = new GenericButton();
		editorControls.add(postButton);
		postButton.setValue(iwrb.getLocalizedString("send", "Send"));
		postButton.setStyleClass("btn btn-primary send-btn");
		StringBuilder action = new StringBuilder("PostCreator.createPost('#").append(form.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(uploadArea.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(resourcePathInput.getId())
				.append(CoreConstants.JS_STR_PARAM_END);
		postButton.setOnClick(action.toString());
		
		
		return form;
	}
	
	public List<String> getScriptFiles(IWContext iwc){
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

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
	
	public List<String> getStyleFiles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			styles.add(web2.getBundleURIToFancyBoxStyleFile());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
		}else{
			getLogger().log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		return styles;
	}
	
	private void addFiles(IWContext iwc){
		PresentationUtil.addStyleSheetsToHeader(iwc, getStyleFiles(iwc));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getScriptFiles(iwc));
	}

	private StringBuilder getDefaultValuesString() {
		if(defaultValuesString == null){
			defaultValuesString = new StringBuilder(DEFAULT_VALUES_FUNCTION).append(" = function(){");
		}
		return defaultValuesString;
	}
	protected StringBuilder getScriptOnLoad() {
		if(scriptOnLoad == null){
			scriptOnLoad = new StringBuilder("jQuery(document).ready(function(){");
		}
		return scriptOnLoad;
	}

	protected void setScriptOnLoad(StringBuilder scriptOnLoad) {
		this.scriptOnLoad = scriptOnLoad;
	}
	
	protected Logger getLogger(){
		return Logger.getLogger(this.getClass().getName());
	}
}