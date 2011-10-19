package com.idega.block.social.presentation.comunicating;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.social.Constants;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.presentation.HeaderWithElements;
import com.idega.block.social.presentation.SimpleForm;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.content.upload.presentation.FileUploadViewer;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.Legend;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;

public class PostCreationView extends IWBaseComponent{

	private static final String TAGEDIT_NAME = "tag[]";
	private static final String GROUP_TAGEDIT_NAME = "tag[1]";
	
	public static final String PUBLIC_NEEDED = "PostCreationView-public-needed";

	private IWResourceBundle iwrb = null;
	private IWContext iwc = null;

	private FieldSet content = null;

	private SimpleForm form = null;

	private Layer main = null;

	private Layer buttonsLayer = null;

	private Layer accordionLayer = null;

	private String postBodyInputId = null;

	protected Layer privateMsgOptionsLayer = null;
	
	private Boolean publicNeeded = null;

	public PostCreationView(){
		iwc = CoreUtil.getIWContext();

		main = new Layer();
		super.add(main);

		form = new SimpleForm();
		main.add(form);
		form.setStyleClass("post-creation-view-form");
		form.getChildren().clear();


		content = new FieldSet();
		form.getChildren().add(content);
		content.setStyleClass("post-creation-view-main-field");

		Layer accordionContainer = new Layer();
		this.add(accordionContainer);
		accordionContainer.setStyleClass("post-creation-view-accordion-layer");

		accordionLayer = new Layer();
		accordionContainer.add(accordionLayer);

		buttonsLayer = new Layer();
		this.form.getChildren().add(buttonsLayer);
		buttonsLayer.setStyleClass("post-creation-view-buttons-layer");
	}


	@Override
	protected void initializeComponent(FacesContext context) {
		iwrb = this.getBundle(context, Constants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

		form.setId("post-creation-view-main-form");

		Layer mainFieldsLayer = new Layer();
		this.addChild(1, mainFieldsLayer);
		mainFieldsLayer.setStyleClass("post-creation-view-main-fields-layer");

		FieldSet titleField = new FieldSet(iwrb.getLocalizedString("title", "title") + CoreConstants.COLON);
		mainFieldsLayer.add(titleField);
		titleField.setStyleClass("post-creation-view-post-title-field");

		TextInput title = new TextInput();
		titleField.add(title);
		title.setName(PostBusiness.ParameterNames.POST_TITLE_PARAMETER);
		title.setStyleClass("post-creation-view-post-title-field-input");

		FieldSet textAreaField = new FieldSet(iwrb.getLocalizedString("text", "text") + CoreConstants.COLON);
		mainFieldsLayer.add(textAreaField);
		textAreaField.setStyleClass("post-creation-view-post-textArea-field");
		TextArea textArea = new TextArea();
		textAreaField.add(textArea);
		textArea.setName(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		textArea.setStyleClass("message-text-area");
		this.postBodyInputId = textArea.getId();


		FileUploadViewer uploader = new FileUploadViewer();
		mainFieldsLayer.add(uploader);
		uploader.setAllowMultipleFiles(true);
		uploader.setAutoAddFileInput(false);
		uploader.setAutoUpload(false);
		uploader.setShowUploadedFiles(true);
		uploader.setFormId(form.getId());

		StringBuilder actionafterUpload = new StringBuilder("PostCreationViewHelper.createLocationInput('#").append(mainFieldsLayer.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME)
				.append(CoreConstants.JS_STR_PARAM_END);
		uploader.setActionAfterUploadedToRepository(actionafterUpload.toString());

		// Private message options
		this.privateMsgOptionsLayer = new Layer();
//		this.addToaccordion(this.privateMsgOptionsLayer, iwrb.getLocalizedString("private_message", "Private Message"),
//				PostBusiness.ParameterNames.PRIVATE_MESSAGE_PARAMETER_NAME);
		addToaccordion(this.privateMsgOptionsLayer, iwrb.getLocalizedString("people", "People"));
		Label label = new Label();
		this.privateMsgOptionsLayer.add(label);
//		layer.setId(iwc.getViewRoot().createUniqueId() + "PostCreationView");
		label.addText(iwrb.getLocalizedString("people", "People") + CoreConstants.COLON);

		FieldSet field = new FieldSet();
		this.privateMsgOptionsLayer.add(field);
		label = new Label();
		field.add(label);
		label.setStyleClass("post-creation-view-public-or-private-msg");
		label.addText(iwrb.getLocalizedString("private", "Private") + CoreConstants.COLON);
		RadioButton radio = new RadioButton(PostBusiness.ParameterNames.MESSAGE_TYPE,PostBusiness.ParameterNames.PRIVATE_MESSAGE);
		field.add(radio);
		radio.setSelected();

		label = new Label();
		field.add(label);
		label.setStyleClass("post-creation-view-public-or-private-msg");
		label.addText(iwrb.getLocalizedString("public", "Public") + CoreConstants.COLON);
		radio = new RadioButton(PostBusiness.ParameterNames.MESSAGE_TYPE,PostBusiness.ParameterNames.PUBLIC_MESSAGE);
		field.add(radio);

		TextInput nameInput = new TextInput();
		this.privateMsgOptionsLayer.add(nameInput);
		nameInput.setName(PostCreationView.TAGEDIT_NAME);
		StringBuilder actionForm = new StringBuilder("PostCreationViewHelper.TAGEDIT_INPUT_SELECTOR = '#")
		.append(nameInput.getId()).append("';");
		String actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);

		// Group message options
		Layer groupLayer = new Layer();
//		this.addToaccordion(layer, iwrb.getLocalizedString("groups", "Groups"),
//				PostBusiness.ParameterNames.POST_TO_GROUPS_PARAMETER_NAME);
		if(isPublicNeeded(iwc)){
			addToaccordion(groupLayer, iwrb.getLocalizedString("groups", "Groups"));
		}
		label = new Label();
		groupLayer.add(label);
		label.addText(iwrb.getLocalizedString("message_receivers", "Message Receivers") + CoreConstants.COLON);
		nameInput = new TextInput();
		groupLayer.add(nameInput);
		nameInput.setName(GROUP_TAGEDIT_NAME);
//		this.tageditId = nameInput.getId();
		actionForm = new StringBuilder("PostCreationViewHelper.TAGEDIT_GROUP_INPUT_SELECTOR = '#")
		.append(nameInput.getId()).append("';");
		actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);


		// Submit button
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("submit", "Submit"));
		this.addToButtonsLayer(buttonSubmit);
		buttonSubmit.setOnClick("PostCreationViewHelper.savePost('#" + this.form.getId() + CoreConstants.JS_STR_PARAM_END);
//		buttonSubmit.setMarkupAttribute("type", "submit"); //will use revers ajax and js


		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, PostCreationView.getNeededScripts(iwc));
		PresentationUtil.addStyleSheetsToHeader(iwc, PostCreationView.getNeededStyles(iwc));

		this.addActions();
		//TODO: temporary solution
		if(!isPublicNeeded(iwc)){
			field.setStyleClass("not-displayed");
		}
	}

	private void addActions(){
		StringBuilder tabsCreator = new StringBuilder("PostCreationViewHelper.createAccordion('#").append(accordionLayer.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(".ui-accordion-header")
				.append(CoreConstants.JS_STR_PARAM_END);
		String action = PresentationUtil.getJavaScriptAction(tabsCreator.toString());
		main.add(action);

		StringBuilder checker = new StringBuilder("PostCreationViewHelper.setTocheckOnClick('.post-creation-view-accordion-checkbox','#")
				.append(this.privateMsgOptionsLayer.getId())
				.append(CoreConstants.JS_STR_PARAM_END);
		String checkerScript = PresentationUtil.getJavaScriptAction(checker.toString());
		main.add(checkerScript);

		StringBuilder autogrow = new StringBuilder("PostCreationViewHelper.createAutoresizing('#").append(postBodyInputId)
		.append(CoreConstants.JS_STR_PARAM_END);
		String autogrowAction = PresentationUtil.getJavaScriptAction(autogrow.toString());
		main.add(autogrowAction);


		StringBuilder actionForm = new StringBuilder("PostCreationViewHelper.createAutocompleteWithImages('[name=\"")
		.append(PostCreationView.TAGEDIT_NAME).append("\"]');");
		String actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);

		actionForm = new StringBuilder("PostCreationViewHelper.nontrivialUserDefiningPhraseErrorMsg = '")
				.append(this.iwrb.getLocalizedString("entered_phrase_does_not_trivially_defines_the_receiver",
						"Entered phrase does not trivially defines the receiver")).append("';");
		actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);

		actionForm = new StringBuilder("PostCreationViewHelper.someHelp();");
		actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);

		actionForm = new StringBuilder("PostCreationViewHelper.createGroupAutocomplete('[name=\"")
		.append(GROUP_TAGEDIT_NAME).append("\"]');");
		actionString = PresentationUtil.getJavaScriptAction(actionForm.toString());
		main.add(actionString);



	}

	public void addToButtonsLayer(UIComponent object){
		buttonsLayer.add(object);
	}

	@Override
	public void add(UIComponent object){
		content.add(object);
	}

	public void addChild(int index, UIComponent object){
		content.addChild(index, object);
	}

	public void addLogicalGroup(UIComponent object){
		FieldSet logicalGroupContainer = new FieldSet();
		List <UIComponent> children = form.getChildren();
		children.add(children.size() - 2,logicalGroupContainer); //-2 because buttons layer is the last by default
		logicalGroupContainer.add(object);
	}

	public void addLogicalGroup(int index,UIComponent object){
		FieldSet logicalGroupContainer = new FieldSet();
		form.getChildren().add(index,logicalGroupContainer);
		logicalGroupContainer.add(object);
	}

	public void addLogicalGroup(UIComponent object,String legend){
		FieldSet logicalGroupContainer = new FieldSet();
		form.getChildren().add(logicalGroupContainer);
		Legend legendTag = new Legend(legend);
		logicalGroupContainer.add(legendTag);
		logicalGroupContainer.add(object);
	}

	public void addLogicalGroup(int index, UIComponent object, String legend){
		FieldSet logicalGroupContainer = new FieldSet();
		form.getChildren().add(index,logicalGroupContainer);
		Legend legendTag = new Legend(legend);
		logicalGroupContainer.add(legendTag);
		logicalGroupContainer.add(object);
	}

	public void addFieldset(FieldSet fieldset){
		List <UIComponent> children = this.form.getChildren();
		children.add(children.size() - 2,fieldset);
	}

	public void addFieldset(int index, FieldSet fieldset){
		this.form.getChildren().add(index,fieldset);
	}

	protected void addToaccordion(UIComponent content, String header){
		HeaderWithElements h = new HeaderWithElements();
		h.setId(iwc.getViewRoot().createUniqueId() + "PostCreationView");
		h.setStyleClass("post-creation-view-accordion-header");
		Link link = new Link();
		h.add(link);
		link.addToText(header);
		link.setStyleClass("post-creation-view-accordion-link");
		this.accordionLayer.add(h);
		if(!(content instanceof Layer)){
			Layer contentLayer = new Layer();
			contentLayer.add(content);
			this.accordionLayer.add(contentLayer);
			return;
		}
		this.accordionLayer.add(content);
	}

	protected void addToaccordion(UIComponent content, String header, String checkBoxName){
//		Heading3 h = new Heading3(CoreConstants.EMPTY);
//		h.addToText(new StringBuilder("<a>").append(header).append("</a>").toString());
		HeaderWithElements h = new HeaderWithElements();
		h.setId(iwc.getViewRoot().createUniqueId() + "PostCreationView");
		h.setStyleClass("post-creation-view-accordion-header");
		Link link = new Link();
		h.add(link);
		link.addToText(header);
		link.setStyleClass("post-creation-view-accordion-link");
		CheckBox checkBox = new CheckBox(checkBoxName);
//		checkBox.setName(checkBoxName);
//		System.out.println(checkBox.getName());
		h.getChildren().add(checkBox);
//		checkBox.setName(checkBox.getId());
		checkBox.setStyleClass("post-creation-view-accordion-checkbox");
		this.accordionLayer.add(h);
		if(!(content instanceof Layer)){
			Layer contentLayer = new Layer();
			contentLayer.add(content);
			this.accordionLayer.add(contentLayer);
			return;
		}
		this.accordionLayer.add(content);
	}

	protected void addToaccordion(int index,UIComponent content, String header, String checkBoxName){
		if(index < 0){
			return;
		}
		HeaderWithElements h = new HeaderWithElements();
		h.setId(iwc.getViewRoot().createUniqueId());
		h.setStyleClass("post-creation-view-accordion-header");
		Link link = new Link();
		h.add(link);
		link.addToText(header);
		link.setStyleClass("post-creation-view-accordion-link");
		CheckBox checkBox = new CheckBox();
		checkBox.setName(checkBoxName);
		h.getChildren().add(checkBox);
		checkBox.setName(checkBox.getId());
		checkBox.setStyleClass("post-creation-view-accordion-checkbox");
		this.accordionLayer.add(h);
		Layer contentLayer = null;
		if(!(content instanceof Layer)){
			contentLayer = new Layer();
			contentLayer.add(content);
		}else{
			contentLayer = (Layer)content;
		}
		index = index * 2;
		List <UIComponent> children = this.accordionLayer.getChildren();
		try{
			if(!(children.get(index - 1) instanceof Heading3)){
				if(index != children.size()){
					index += 1;
				}
			}
		}catch(IndexOutOfBoundsException e){
			if(index != children.size()){
				index = 0;
			}
		}
		this.accordionLayer.addChild(index,h);
		this.accordionLayer.addChild(index + 1,contentLayer);

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

			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","js/jquery-ui-1.8.14.custom.min.js"));
			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.14","development-bundle/ui/jquery-ui-autocomplete-html.js"));


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery-tagedit-remake.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
						.append(CoreConstants.SLASH)
						.append(Web2BusinessBean.TAGEDIT_SCRIPT_FILE_AUTOGROW).toString()));
			}catch(RemoteException e){
				Logger.getLogger("PostcreationView").log(Level.WARNING,CoreConstants.EMPTY,e);
			}

		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/PostCreationHelper.js"));
		scripts.add("/dwr/interface/SocialServices.js");

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

			styles.addAll(web2.getBundleURIsToTageditStyleFiles());


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/postCreationView.css"));
		return styles;
	}


	public Boolean isPublicNeeded(IWContext iwc) {
		if(publicNeeded == null){
			String param = iwc.getParameter(PUBLIC_NEEDED);
			if(param != null && param.equalsIgnoreCase("true")){
				publicNeeded = Boolean.TRUE;
			}else{
				publicNeeded = Boolean.FALSE;
			}
		}
		return publicNeeded;
	}


	public void setPublicNeeded(Boolean publicNeeded) {
		this.publicNeeded = publicNeeded;
	}
}
