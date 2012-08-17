package com.idega.block.social.business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.idega.user.bean.UserDataBean;
import com.idega.util.text.Item;

public class PostInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2345137556986918953L;
	private UserDataBean author = null;
	private String title = null;
	private String teaser = null;
	private String uri = null;
	private String body = null;
	private Date date = null;
	private List <Item> attachments = null;

	public UserDataBean getAuthor() {
		return author;
	}
	public void setAuthor(UserDataBean author) {
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTeaser() {
		return teaser;
	}
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uriToBody) {
		this.uri = uriToBody;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public List<Item> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Item> attachments) {
		this.attachments = attachments;
	}
}
