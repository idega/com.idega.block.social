package com.idega.block.social.data;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;

import com.idega.block.article.data.ArticleEntity;



@Entity
@Table(name = "social_post")
public class PostEntity   implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -7269208873766306969L;


	public static final String MESSAGE = "MESSAGE";
	public static final String PUBLIC = "PUBLIC";

	public static final String idProp = "ID";
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name="ID")
	private Long id;


	public static final String postTypeProp = "postType";
	@Index(name = "post_type_index")
	@Column(name="SOCIAL_POST_TYPE")
	private String postType;

	public static final String articleProp = "articleProp";
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	@JoinColumn(name="ARTICLE_ID")
    private ArticleEntity article;

	public static final String receiversProp = "receiversProp";
//	@ElementCollection
//	@CollectionTable(name="Receivers", joinColumns=@JoinColumn(name="RECEIVER_ID"))
//	@Column(name="RECEIVERS")
//	private Collection  <Integer> receivers;
	@CollectionOfElements
    @JoinTable(name="social_post_receivers", joinColumns=@JoinColumn(name="POST_ID"))
    @Column(name="RECEIVER_ID", nullable=false)
    private Set<Integer> receivers;

//	@CollectionOfElements
//	@Column(name="RECEIVERS")
//	private Map<Long, Integer> receivers;


	public static final String postCreatorProp = "postCreator";
	@Index(name = "post_creator_index")
	@Column(name="SOCIAL_POST_CREATOR")
	private int postCreator;

	public String getPostType() {
		return postType;
	}
	public void setPostType(String postType) {
		this.postType = postType;
	}
	public Long getId() {
		return id;
	}
	public ArticleEntity getArticle() {
		return article;
	}
	public void setArticle(ArticleEntity article) {
		this.article = article;
	}

	public Set<Integer>getReceivers() {
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
}
