package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.Constants;
import is.idega.block.saga.presentation.HeaderWithElements;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;

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
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;

public class PostCreationView extends IWBaseComponent{
	public static final String BODY_PARAMETER_NAME = "post_body";
	public static final String RECEIVERS_PARAMETER_NAME = "post_receivers";
	public static final String POST_TO_GROUPS_PARAMETER_NAME = "post_to_groups";
	public static final String WALL_POST_PARAMETER_NAME = "post_on_wall";
	public static final String PRIVATE_MESSAGE = "private_message";

	private IWResourceBundle iwrb = null;
	private IWContext iwc = null;

	private FieldSet content = null;

	private HtmlForm form = null;

	private Layer main = null;

	private Layer buttonsLayer = null;

	private Layer accordionLayer = null;

	public PostCreationView(){
		iwc = CoreUtil.getIWContext();

		main = new Layer();
		super.add(main);

		form = new HtmlForm();
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
	public void encodeEnd(FacesContext context) throws IOException {
//	    List fc = this.form.getChildren();
//	    fc.remove(fc.size() - 1);
//	    fc.remove(fc.size() - 1);
//	    super.encodeEnd(context);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		iwrb = this.getBundle(context, Constants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

		form.setId("post-creation-view-main-form");

		this.addActions();

		Layer textAreaLayer = new Layer();
		this.addChild(1, textAreaLayer);
		textAreaLayer.setStyleClass("message-text-area-layer");

		TextArea textArea = new TextArea();
		textAreaLayer.add(textArea);
		textArea.setName(PostCreationView.BODY_PARAMETER_NAME);
		textArea.setStyleClass("message-text-area");


		// Private message options
		Layer layer = new Layer();
		this.addToaccordion(layer, iwrb.getLocalizedString("private_message", "Private Message"),PRIVATE_MESSAGE);
		Label label = new Label();
		layer.add(label);
		label.addText(iwrb.getLocalizedString("message_receivers", "Message Receivers") + CoreConstants.COLON);
		TextInput nameInput = new TextInput();
		layer.add(nameInput);
		nameInput.setName(RECEIVERS_PARAMETER_NAME);

		// Group message options
		layer = new Layer();
		this.addToaccordion(layer, iwrb.getLocalizedString("group_message", "Group Message"),POST_TO_GROUPS_PARAMETER_NAME);

		// Wall post options
		layer = new Layer();
		this.addToaccordion(layer, iwrb.getLocalizedString("wall_post", "Post on your wall"),WALL_POST_PARAMETER_NAME);


		// Submit button
		GenericButton buttonSubmit = new GenericButton("buttonSubmit", iwrb.getLocalizedString("submit", "Submit"));
		this.addToButtonsLayer(buttonSubmit);
		buttonSubmit.setOnClick("PostCreationView.savePost('#" + this.form.getId() + CoreConstants.JS_STR_PARAM_END);
//		buttonSubmit.setMarkupAttribute("type", "submit"); //will use revers ajax and js


	}

	private void addActions(){
		StringBuilder tabsCreator = new StringBuilder("PostCreationView.createAccordion('#").append(accordionLayer.getId())
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(".ui-accordion-header")
				.append(CoreConstants.JS_STR_PARAM_END);
		String action = PresentationUtil.getJavaScriptAction(tabsCreator.toString());
		main.add(action);

		String checkerScript = PresentationUtil.getJavaScriptAction(
				"PostCreationView.setTocheckOnClick('.post-creation-view-accordion-checkbox');");
		main.add(checkerScript);

	}

	public void addToButtonsLayer(UIComponent object){
		buttonsLayer.add(object);
	}

	public String getActionOnSubmit() {
		return form.getOnsubmit();
	}

	public void setActionOnSubmit(String onSubmit){
		form.setOnsubmit(onSubmit);
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

	protected void addToaccordion(UIComponent content, String header, String checkBoxName){
//		Heading3 h = new Heading3(CoreConstants.EMPTY);
//		h.addToText(new StringBuilder("<a>").append(header).append("</a>").toString());
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
}
