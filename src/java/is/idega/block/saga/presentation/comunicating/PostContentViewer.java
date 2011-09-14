package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.Constants;
import is.idega.block.saga.bean.PostRequestBean;
import is.idega.block.saga.business.PostBusiness;
import is.idega.block.saga.business.PostFilterParameters;
import is.idega.block.saga.presentation.SimpleForm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.directwebremoting.ScriptBuffer;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.TextArea;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class PostContentViewer extends IWBaseComponent {

	private static final String POST_LOAD_SCRIPT_STRING =
			"if((PostCreationViewHelper != undefined) && (PostCreationViewHelper.getThePosts != undefined)){\n"+
				"PostCreationViewHelper.getThePosts();" +
			"\n}";
	public static final ScriptBuffer POST_LOAD_SCRIPT = new ScriptBuffer(POST_LOAD_SCRIPT_STRING);
	@Autowired
	private PostBusiness postBusiness;

	private IWContext iwc = null;
	private IWBundle bundle = null;
	private IWResourceBundle iwrb = null;

	private Layer main = null;
	private UIViewRoot uiViewRoot = null;

	private String postCreationFormId = null;
	private String postCreationButtonId = null;

	private static String GENERATED_ID_PREFIX = "is.idega.block.saga.presentation.comunicating.post-content-viewer";
	private int idGenerationCounter = 0;

	private String advancedLinkId = null;

	@Autowired
	PostRequestBean postRequestBean;

	private boolean isLoggedOn = false;

	private static final String ENDING = "';\n";


	public PostContentViewer(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
		bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		this.uiViewRoot = iwc.getViewRoot();
		Random random = new Random();
		this.idGenerationCounter = random.nextInt();
		isLoggedOn = iwc.isLoggedOn();
		iwrb = bundle.getResourceBundle(iwc);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		main = new Layer();
		this.add(main);

		if(isLoggedOn){
			main.add(getPostingForm());
		}
		main.setStyleClass("post-content-ciewer");


		main.add(getPostList(iwc));

		GenericButton button = new GenericButton();
		main.add(button);
		button.setValue(iwrb.getLocalizedString("load_more", "Load More"));
		button.setOnClick("PostContentViewerHelper.addPostsdown()");
		button.setStyleClass("post-button-load-more");

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, PostContentViewer.getNeededScripts(iwc));
		PresentationUtil.addStyleSheetsToHeader(iwc, PostContentViewer.getNeededStyles(iwc));

		this.addActions();
	}

	private void setPostFilterParameters(IWContext iwc){
		PostFilterParameters filterParameters = new PostFilterParameters();

		postRequestBean.setGetUp(iwc.getParameter(PostRequestBean.Parameters.GET_UP));
		String max = iwc.getParameter(PostRequestBean.Parameters.MAX_TO_SHOW);
		postRequestBean.setMaxToShow(max == null ? 10 : Integer.valueOf(max));
		postRequestBean.setFirstUri(iwc.getParameter(PostRequestBean.Parameters.FIRST_URI));

		if(isLoggedOn){
			postRequestBean.setShowGroup(iwc.getParameter(PostRequestBean.Parameters.SHOW_GROUP));
			postRequestBean.setShowPrivate(iwc.getParameter(PostRequestBean.Parameters.SHOW_PRIVATE));
			postRequestBean.setShowSent(iwc.getParameter(PostRequestBean.Parameters.SENT));
		}
	}

	public static UIComponent getPostList(IWContext iwc){
		IWBundle bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("communicating/postContentViewer.xhtml"));
		return facelet;
	}

	public static UIComponent getPostList(IWContext iwc,String beginUri, Boolean up,
			String getPrivate, String getGroup, String getSent, int maxResult){

		PostRequestBean postRequestBean = ELUtil.getInstance().getBean(Constants.POST_REQUEST_BEAN_ID);
		postRequestBean.setFirstUri(beginUri);
		postRequestBean.setGetUp(up ? up.toString() : null);
		postRequestBean.setShowPrivate(getPrivate);
		postRequestBean.setShowGroup(getGroup);
		postRequestBean.setShowSent(getSent);
		postRequestBean.setMaxToShow(maxResult);

		IWBundle bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("communicating/postContentViewer.xhtml"));
		return facelet;
	}

	private SimpleForm getPostingForm(){
		SimpleForm form = new SimpleForm();

		TextArea postBody = new TextArea();
		form.add(postBody);
		postBody.setName(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		postBody.setStyleClass("post-content-viewer-post-creation-form-body");

		HiddenInput postToGroups = new HiddenInput(PostBusiness.ParameterNames.POST_TO_ALL_USER_GROUPS,
				"true");
		form.add(postToGroups);

		GenericButton postButton = new GenericButton();
		form.add(postButton);
		postButton.setValue("post"/*this.iwrb.getLocalizedString("post", "Post")*/);
		StringBuilder action = new StringBuilder("PostContentViewerHelper.savePost('#").append(postButton.getId())
		.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(CoreConstants.NUMBER_SIGN).append(postBody.getId())
		.append(CoreConstants.JS_STR_PARAM_END);
		postButton.setOnClick(action.toString());



		Link link = new Link(CoreConstants.EMPTY);
		form.add(link);
		this.advancedLinkId = link.getId();
		String uriToAdvanced = BuilderLogic.getInstance().getUriToObject(PostCreationView.class);
		link.setURL(uriToAdvanced);

		GenericButton advanced = new GenericButton();
		form.add(advanced);
		advanced.setValue(this.iwrb.getLocalizedString("advanced", "Advanced"));
		action = new StringBuilder("PostContentViewerHelper.openAdvancedPostForm('#").append(advancedLinkId)
		.append(CoreConstants.JS_STR_PARAM_END);
		advanced.setOnClick(action.toString());

		return form;
	}

	private void addActions(){
		StringBuilder autogrow = new StringBuilder("PostContentViewerHelper.createAutoresizing('#").append(main.getId())
		.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append("[name = \"").append(PostBusiness.ParameterNames.BODY_PARAMETER_NAME)
		.append("\"]');\n");
		StringBuilder actions = new StringBuilder(autogrow);

		String postPreviews = "jQuery(document).ready(PostContentViewerHelper.createPostPreviews);\n";
		actions.append(postPreviews);

		String parameter = iwc.getParameter(PostRequestBean.Parameters.GET_UP);
		if(parameter != null){
			postRequestBean.setGetUp(parameter);
		}
		parameter = iwc.getParameter(PostRequestBean.Parameters.MAX_TO_SHOW);
		if(parameter != null){
			HiddenInput input = new HiddenInput();
			main.add(input);
			input.setValue(parameter);
			postRequestBean.setGetUp(parameter);
		}
		parameter = iwc.getParameter(PostRequestBean.Parameters.FIRST_URI);
		if(parameter != null){
			postRequestBean.setGetUp(parameter);
		}
		if(isLoggedOn){
			parameter = iwc.getParameter(PostRequestBean.Parameters.SHOW_GROUP);
			if(parameter != null){
				actions.append("PostContentViewerHelper.showGroup = '");
				actions.append(parameter);
				actions.append(ENDING);
				postRequestBean.setGetUp(parameter);
			}else{
				actions.append("PostContentViewerHelper.showGroup = null;\n");
			}
			parameter = iwc.getParameter(PostRequestBean.Parameters.SHOW_PRIVATE);
			if(parameter != null){
				actions.append("PostContentViewerHelper.showPrivate = '");
				actions.append(parameter);
				actions.append(ENDING);
				postRequestBean.setGetUp(parameter);
			}else{
				actions.append("PostContentViewerHelper.showPrivate = null;\n");
			}
			parameter = iwc.getParameter(PostRequestBean.Parameters.SENT);
			if(parameter != null){
				actions.append("PostContentViewerHelper.showSent = '");
				actions.append(parameter);
				actions.append(ENDING);
				postRequestBean.setGetUp(parameter);
			}else{
				actions.append("PostContentViewerHelper.showSent = null;\n");
			}
		}

		actions.append("PostCreationViewHelper.getThePosts = function(){PostContentViewerHelper.addPosts();}\n");
		actions.append("jQuery(document).ready(function(){\n PostContentViewerHelper.prepareAdvancedLink('#").append(advancedLinkId)
				.append("');\n});\n");
		actions.append("jQuery(document).ready(function(){\nPostContentViewerHelper.loadMoreButtonNeeded();\n});\n");
		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
		main.add(actionString);
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


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

//			scripts.addAll(web2.getBundleURIsToTageditLib());
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
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/PostContentViewerHelper.js"));

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

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/postListStyle.css"));
		return styles;
	}

	private String getGeneretedId(){
		return uiViewRoot.createUniqueId() + GENERATED_ID_PREFIX + (this.idGenerationCounter++);
	}

	public String getPostCreationFormId(){
		if(this.postCreationFormId == null){
			this.postCreationFormId = this.getGeneretedId();
		}
		return this.postCreationFormId;
	}

	public String getPostCreationButtonId(){
		if(this.postCreationButtonId == null){
			this.postCreationButtonId = this.getGeneretedId();
		}
		return this.postCreationButtonId;
	}

	public String getPostCreationBodyParameterName(){
		return PostBusiness.ParameterNames.BODY_PARAMETER_NAME;
	}

	public String getPostCreationAction(){
		StringBuilder action = new StringBuilder("PostContentViewerHelper.createAutoresizing('#").append(getPostCreationButtonId())
		.append(CoreConstants.JS_STR_PARAM_END);
		return action.toString();
	}

}
