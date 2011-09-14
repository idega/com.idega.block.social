package is.idega.block.saga.business;

import java.util.Date;
import java.util.List;

import com.idega.util.text.Item;

public class PostInfo {
	private String uriToAuthorPicture = null;
	private String author = null;
	private String title = null;
	private String teaser = null;
	private String uriToBody = null;
	private String body = null;
	private Date date = null;
	private List <Item> attachments = null;
	private String uriToBodyPreview = null;

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUriToAuthorPicture() {
		return uriToAuthorPicture;
	}
	public void setUriToAuthorPicture(String uriToAuthorPicture) {
		this.uriToAuthorPicture = uriToAuthorPicture;
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
	public String getUriToBody() {
		return uriToBody;
	}
	public void setUriToBody(String uriToBody) {
		this.uriToBody = uriToBody;
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
	public String getUriToBodyPreview() {
		return uriToBodyPreview;
	}
	public void setUriToBodyPreview(String uriToBodyPreview) {
		this.uriToBodyPreview = uriToBodyPreview;
	}
}
