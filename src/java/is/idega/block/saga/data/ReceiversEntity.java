package is.idega.block.saga.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
/**
 * This entity was created to join OneToMany PostEntity and
 * com.idega.user.data.Group(that is not hibernate)
 * @author alex
 *
 */
@Entity
@Table(name = "social_post_receivers")
public class ReceiversEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5428272610130267492L;


	public static final String idProp = "id";
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID")
	private Long id;

	public static final String receiverProp = "SOCIAL_POST_RECEIVER";
	@Index(name = "post_receiver_index")
	@Column(name="SOCIAL_POST_RECEIVER")
	private int receiverId;


	public static final String postProp = "SOCIAL_POST_ID";
	@Index(name = "post_index")
	@Column(name="SOCIAL_POST_ID")
	private Long socialPostId;

	public int getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}
	public Long getSocialPostId() {
		return socialPostId;
	}
	public void setSocialPostId(Long socialPostId) {
		this.socialPostId = socialPostId;
	}

}
