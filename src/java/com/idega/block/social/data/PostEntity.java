package com.idega.block.social.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;

import com.google.gson.Gson;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.social.data.dao.PostDao;
import com.idega.util.expression.ELUtil;

@Entity
@Table(name = "soc_post")
public class PostEntity extends ArticleEntity {

	private static final long serialVersionUID = 8163484471464890L;

	public static final String POST_TYPE_MESSAGE = "MESSAGE";
	public static final String POST_TYPE_PUBLIC = "PUBLIC";

	public PostEntity() {
		super();

//		setTheClass(getClass().getSimpleName());
	}

	public PostEntity(ArticleEntity article) {
		super(article);
	}

	public PostEntity(PostEntity post){
		this((ArticleEntity) post);

		setPostType(post.getPostType());
		setReceivers(post.getReceivers());
		setPostCreator(post.getPostCreator());
	}

	public static final String postTypeProp = "postType";
	@Index(name = "post_type_index")
	@Column(name = "SOCIAL_POST_TYPE")
	private String postType;

	public static final String receiversProp = "receiversProp";
	@CollectionOfElements
	@JoinTable(name = "soc_post_receivers", joinColumns = @JoinColumn(name = "POST_ID"))
	@Column(name = "RECEIVER_ID", nullable = false)
	private Set<Integer> receivers;

	public static final String postCreatorProp = "postCreator";
	@Index(name = "post_creator_index")
	@Column(name = "SOCIAL_POST_CREATOR")
	private int postCreator;

	public String getPostType() {
		return postType;
	}

	public void setPostType(String postType) {
		this.postType = postType;
	}

	public Set<Integer> getReceivers(){
		if(Hibernate.isInitialized(receivers)){
			return receivers;
		}
		PostDao postDao =  ELUtil.getInstance().getBean(PostDao.BEAN_NAME);
		receivers = new HashSet<Integer>( postDao.getReceivers(getId()));
		return receivers;
	}

	public void setReceivers(Set<Integer> receivers) {
		this.receivers = receivers;
	}

	public int getPostCreator() {
		return postCreator;
	}

	public void setPostCreator(int postCreator) {
		this.postCreator = postCreator;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
