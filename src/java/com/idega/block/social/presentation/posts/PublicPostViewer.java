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
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.business.SocialServices;
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
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class PublicPostViewer extends IWBaseComponent {
	
	@Autowired
	private SocialServices socialServices;
	
	private StringBuilder scriptOnLoad = null;
	
	private StringBuilder defaultValuesString = null;
	
	private Integer maxToShow = null;
	
	private int teaserLength = 200;
	
	private static final int DEFAULT_MAX_TO_SHOW = 20;
	
	private static final String DEFAULT_VALUES_FUNCTION = "PublicPostViewerHelper.setDefaultValues";
	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		IWContext iwc = IWContext.getIWContext(context);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		ELUtil.getInstance().autowire(this);
		
		if(iwc.isLoggedOn()){
			add(getPostingForm(iwc, iwrb));
		}
		PostList postList = new PostList();
		add(postList);
		postList.setStyleClass("post-list");
		postList.setPostFilterParameters(getPostFilterParameters(iwc));
		postList.setTeaserLength(getTeaserLength());
		
		
		Layer footer = new Layer();
		footer.setStyleClass("public-posts-footer");
		add(footer);
		
		GenericButton loadMore = new GenericButton();
		footer.add(loadMore);
		loadMore.setStyleClass("btn btn-success");
		loadMore.setContent(iwrb.getLocalizedString("load_more", "Load more"));
		loadMore.setOnClick("jQuery('.post-list').trigger('append-posts')");
		
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
	
	private Form getPostingForm(IWContext iwc, IWResourceBundle iwrb){
		Form form = new Form();
		form.setStyleClass("public-post-form");
		form.setOnSubmit("return false;");


		TextArea postBody = new TextArea(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		form.add(postBody);
		postBody.setContent(CoreConstants.EMPTY);
		postBody.setStyleClass("empty");
		String postBodyId = postBody.getId();
		getDefaultValuesString().append("\n\tjQuery('#").append(postBodyId).append("').val('');");
		getDefaultValuesString().append("\n\tjQuery('#").append(postBodyId).append("').keyup();"); // to get area small like it was (it is autoresize)
		getScriptOnLoad().append("jQuery('#").append(postBodyId).append("').autoResize({extraSpace : 30, animate : false });");
		postBody.setStyleClass("post-content-viewer-post-creation-form-body");
		
		UploadArea uploadArea = new UploadArea();
		form.add(uploadArea);
		PostItemBean postItemBean = ELUtil.getInstance().getBean("postItemBean");
		String resourcePath = postItemBean.getResourcePath();
		
		HiddenInput resourcePathInput = new HiddenInput(SocialConstants.POST_URI_PARAMETER,resourcePath);
		form.add(resourcePathInput);
		
		uploadArea.setUploadPath(postItemBean.getFilesResourcePath());
		uploadArea.setDropZonesSelectionFunction("jQuery('#"+ form.getId() +"')");
		uploadArea.setAutoUpload(true);
		uploadArea.setName(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		
		SubmitButton postButton = new SubmitButton();
		form.add(postButton);
		postButton.setValue(iwrb.getLocalizedString("send", "Send"));
		postButton.setStyleClass("btn btn-primary start");
		StringBuilder action = new StringBuilder("PublicPostViewerHelper.createPost('#").append(postButton.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(uploadArea.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(resourcePathInput.getId())
				.append(CoreConstants.JS_STR_PARAM_END);
		postButton.setOnClick(action.toString());
		
		
		return form;
	}

	private PostFilterParameters getPostFilterParameters(IWContext iwc){
		PostFilterParameters postFilterParameters = new PostFilterParameters();
		Collection<Integer> receivers;
		try{
			receivers = socialServices.getUserGroupIds(iwc.getCurrentUser());
		}catch (Exception e) {
			receivers = Collections.emptyList();
		}
		postFilterParameters.setReceivers(receivers);
		postFilterParameters.setMax(getMaxToShow());
		return postFilterParameters;
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
	
	public List<String> getStyleFiles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			styles.add(web2.getBundleURIToFancyBoxStyleFile());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
		}else{
			Logger.getLogger(PublicPostViewer.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
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

	public int getMaxToShow() {
		if(maxToShow == null){
			return DEFAULT_MAX_TO_SHOW;
		}
		return maxToShow;
	}

	public void setMaxToShow(int maxToShow) {
		this.maxToShow = maxToShow;
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

	public int getTeaserLength() {
		return teaserLength;
	}

	public void setTeaserLength(int teaserLength) {
		this.teaserLength = teaserLength;
	}

}
