package is.idega.block.saga.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.idega.block.article.data.ArticleEntity;



@Entity
@Table(name = "social_post")
public class PostEntity   extends ArticleEntity{
	/**
	 *
	 */
	private static final long serialVersionUID = -7269208873766306969L;


	public static final String MESSAGE = "MESSAGE";
	public static final String PUBLIC = "PUBLIC";

	public static final String creatorProp = "SOCIAL_POST_CREATOR";
//	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	@Index(name = "post_type_index")
	@Column(name="SOCIAL_POST_CREATOR")
	private int creatorId;

//	public static final String receiversProp = "receivers";
//	@OneToMany(mappedBy="postEntity")
//	@JoinTable(name = "SOCIAL_POST_RECEIVERS",
//			joinColumns = @JoinColumn(name = "POST_ID"),
//			inverseJoinColumns = @JoinColumn(name = "IC_GROUP_ID"))
//	private Set<Integer> groups;

	public static final String postTypeProp = "postType";
	@Index(name = "post_type_index")
	@Column(name="SOCIAL_POST_TYPE")
	private String postType;

	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
//	public Set<Integer> getGroups() {
//		return groups;
//	}
//	public void setGroups(Set<Integer> groups) {
//		this.groups = groups;
//	}
	public String getPostType() {
		return postType;
	}
	public void setPostType(String postType) {
		this.postType = postType;
	}
}
