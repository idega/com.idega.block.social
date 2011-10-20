package com.idega.block.social.presentation;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;

import com.idega.block.social.SocialConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;

public class TabMenu extends IWBaseComponent {

	private IWContext iwc = null;

	private Lists tabs = null;
	private Layer accordionLayer = null;
	private Layer main = null;

	private int tabCount = 0;

	public TabMenu(){
		iwc = CoreUtil.getIWContext();

		// The main layer of component
		main = new Layer();
		this.add(main);
		main.setStyleClass("main-tabmenu-layer");

		Layer tabsLayer = new Layer();
		main.add(tabsLayer);
		tabsLayer.setStyleClass("main-tabs-layer");
		tabsLayer.setStyleClass("the-unique-tab");

		tabs = new Lists();
		tabsLayer.add(tabs);

		accordionLayer = new Layer();
		main.add(accordionLayer);
		accordionLayer.setStyleClass("tabmenu-accordion-layer");
		StringBuilder accordionCreator = new StringBuilder("jQuery('#").append(accordionLayer.getId())
		.append("').accordion()");
		String action = PresentationUtil.getJavaScriptAction(accordionCreator.toString());
		main.add(action);

		String id = tabsLayer.getId();
		StringBuilder tabsCreator = new StringBuilder("jQuery('#").append(id)
		.append("').tabs({\n idPrefix: ").append(this.getId())
		.append("\n }).addClass('ui-tabs-vertical ui-helper-clearfix');\n jQuery('#")
		.append(id).append("').removeClass( 'ui-corner-top' ).addClass( 'ui-corner-left' );");
		action = PresentationUtil.getJavaScriptAction(tabsCreator.toString());
		main.add(action);


		String actionc = "jQuery('.tabmenu-accordion-header-content').each(function(){\n" +
				"var id = jQuery(this).attr('id');" +
				"if(jQuery('#' + id + ' + div > *').size() < 1){\n" +
					"jQuery(this).find('.ui-icon').css({visibility : 'hidden'});\n" +
					"jQuery('#' + id + ' + div').css({padding : 0});\n" +
				"}});";
		action = PresentationUtil.getJavaScriptAction(actionc);
		main.add(action);
	}
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		addFiles(iwc);
	}



	public void addTab(UIComponent tabView,String url){
		ListItem li = new ListItem();
		tabs.add(li);
		HtmlOutputLink link = new HtmlOutputLink();
		li.add(link);
		link.getChildren().add(tabView);
		link.setValue(url);
		link.setStyleClass("jquery-ui-tab-link");
	}

	public void addTab(String tabView,String url,UIComponent accordionContent){
		ListItem li = new ListItem();
		tabs.add(li);
		Link link = new Link();
		li.add(link);
		if(url != null){
			link.setURL(url);
		}
		link.setStyleClass("jquery-ui-tab-link");

		Heading3 h = new Heading3(CoreConstants.EMPTY);
		h.setId(iwc.getViewRoot().createUniqueId());
		h.setValueExpression("styleClass", WFUtil.createValueExpression(iwc.getELContext(),
				"tabmenu-accordion-header-content",String.class));
		accordionLayer.add(h);
		Link accordionLink = new Link();

		h.addToText(new StringBuilder("<a id= '").append(accordionLink.getId())
				.append("link'>").append(tabView).append("</a>").toString());
		if(accordionContent != null){
			accordionLayer.add(accordionContent);
		}
//		accordionLink.getChildren().add(tabView);

		StringBuilder tabsCreator = new StringBuilder("jQuery('#").append(accordionLink.getId() + "link")
		.append("').click(function(){\n  jQuery('#").append(link.getId())
		.append("').trigger('click');\n});");
		String action = PresentationUtil.getJavaScriptAction(tabsCreator.toString());
		main.add(action);

	}

	public void addTab(String tabView,String url,UIComponent accordionContent,String uriGenerationFunction){
		ListItem li = new ListItem();
		tabs.add(li);
		Link link = new Link();
		li.add(link);
		if(url != null){
			link.setURL(url);
		}
		link.setStyleClass("jquery-ui-tab-link");

		Heading3 h = new Heading3(CoreConstants.EMPTY);
		h.setId(iwc.getViewRoot().createUniqueId());
		h.setValueExpression("styleClass", WFUtil.createValueExpression(iwc.getELContext(),
				"tabmenu-accordion-header-content",String.class));
		accordionLayer.add(h);
		Link accordionLink = new Link();
		String tabLinkId = accordionLink.getId() + "link" + this.tabCount;

		h.addToText(new StringBuilder("<a id= '").append(tabLinkId)
				.append("'>").append(tabView).append("</a>").toString());
		String accordionContentId = null;
		if(accordionContent != null){
			accordionLayer.add(accordionContent);
			accordionContentId = accordionContent.getId();
		}
//		accordionLink.getChildren().add(tabView);

		StringBuilder tabsCreator = new StringBuilder(uriGenerationFunction)
				.append("('#").append(tabLinkId)
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(accordionContentId)
				.append("');\n")
				.append("jQuery('#").append(accordionLink.getId() + "link")
				.append("').click(function(){\n  jQuery('#").append(link.getId())
				.append("').trigger('click');\n});");
		String action = PresentationUtil.getJavaScriptAction(tabsCreator.toString());
		main.add(action);

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

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

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


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/tabs.css"));
		return styles;
	}

	private void addFiles(IWContext iwc){
		List<String> scripts = TabMenu.getNeededScripts(iwc);
		List<String> styles = TabMenu.getNeededStyles(iwc);
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	}

}
