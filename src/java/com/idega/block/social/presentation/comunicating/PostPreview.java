package com.idega.block.social.presentation.comunicating;

import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.hsqldb.lib.StringUtil;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.business.PostInfo;
import com.idega.content.repository.download.RepositoryItemDownloader;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Paragraph;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.Item;

public class PostPreview  extends IWBaseComponent {
	public static String URI_TO_POST_PARAMETER = "uri_to_post";
	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		String uri = iwc.getParameter(URI_TO_POST_PARAMETER);
		
		PostBusiness postBusiness = ELUtil.getInstance().getBean("postBusiness");
		PostInfo post = postBusiness.getPost(uri, iwc);
	
		if(post == null){
			Heading1 error = new Heading1(iwrb.getLocalizedString("nothing_found", "Nothing found"));
			error.setStyleAttribute("text-align:center;");
			add(error);
			return;
		}
		
		Layer main = new Layer();
		this.add(main);
	
		String articleTitle = post.getTitle();
		if(!StringUtil.isEmpty(articleTitle)){
			Heading1 title = new Heading1();
			main.add(title);
			articleTitle = StringEscapeUtils.escapeHtml(articleTitle);
			title.addToText(articleTitle);
		}
	
		String body = post.getBody();
		if(StringUtil.isEmpty(body)){
			body = post.getTeaser();
		}
		if(!StringUtil.isEmpty(body)){
			Paragraph bodyLayer = new Paragraph();
			main.add(bodyLayer);
			body = StringEscapeUtils.escapeHtml(body);
			bodyLayer.addText(body);
			bodyLayer.setStyleAttribute("text-align:justify; margin:2em;");
		}
		
		main.add(getAttachmentsLayer(post,iwrb));
		
		
		
		
		main.setStyleAttribute("overflow : auto; font-size : 18");
	
		StringBuilder script = new StringBuilder("jQuery(document).ready(function(){jQuery('#").append(main.getId())
				.append("').height(windowinfo.getWindowHeight() * 0.8).width(windowinfo.getWindowWidth() * 0.8);});");
		String scriptAction = PresentationUtil.getJavaScriptAction(script.toString());
		main.add(scriptAction);

	}
	
	private Layer getAttachmentsLayer(PostInfo post,IWResourceBundle iwrb){
		List <Item> attachments = post.getAttachments();
		Layer attachmentsLayer = new Layer();
		if(ListUtil.isEmpty(attachments)){
			return attachmentsLayer;
		}
		// All attachment download link
		DownloadLink attachmentsLink = new DownloadLink(iwrb.getLocalizedString("download_all", "Download all"));
		attachmentsLink.setMediaWriterClass(RepositoryItemDownloader.class);
		attachmentsLayer.add(attachmentsLink);
		Layer attachmentsList = new Layer();
		attachmentsLayer.add(attachmentsList);
		attachmentsList.setStyleAttribute("text-align:justify;margin:2em;");
		for(Item attachment : attachments){
			Layer li = new Layer();
			attachmentsList.add(li);
			li.setStyleAttribute("display:inline-block;padding-right:1em;margin-bottom:1em;text-align:center;");
			DownloadLink downloadLink = new DownloadLink();
			String filePath = attachment.getItemValue();
			downloadLink.setParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
			String mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(filePath);
			if((!StringUtil.isEmpty(mimeType)) && (mimeType.toLowerCase().contains("image"))){
//				Image image = new Image(filePath);
//				image.setStyleAttribute("width:5em;height:5em;display:block;");
//				downloadLink.setPresentationObject(image);
				StringBuilder image = new StringBuilder("<img src='").append(filePath).append("'");
				image.append(" style='width:5em;height:5em;display:block;' />");
				downloadLink.addToText(image.toString());
			}else{
				StringBuilder div = new StringBuilder("<div ");
				div.append(" style='width:5em;height:5em;display:block;' />");
				downloadLink.setMarkupAttribute("rel", "friend");
				downloadLink.addToText(div.toString());
			}
			downloadLink.addToText(attachment.getItemLabel());
			li.add(downloadLink);
			downloadLink.setMediaWriterClass(RepositoryItemDownloader.class);
			attachmentsLink.addParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
		}
		return attachmentsLayer;
	}

}
