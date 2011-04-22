package is.idega.block.saga.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity
@Table(name="SAGA")
@NamedQueries(
		{ 
			//get all
			//get by name
			//get by date
			//get author
			@NamedQuery(name = Saga.GET_ALL, query = "SELECT * FROM SAGA"),
			@NamedQuery(name = Saga.GET_BY_NAME, query = "SELECT * FROM SAGA"),
			@NamedQuery(name = Saga.GET_BY_DATE, query = "SELECT * FROM SAGA"),
			@NamedQuery(name = Saga.GET_AUTHOR, query = "SELECT * FROM SAGA")
		}
)
public class Saga implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7986373392792826654L;
	
	private static final String SAGA_ID = "SAGA_ID_INDEX";
	private static final String SAGA_NAME = "SAGA_NAME_INDEX";
	private static final String SAGA_AUTHOR_ID = "SAGA_AUTHOR_ID_INDEX";
	
	public static final String GET_ALL = "saga.getAll";
	public static final String GET_BY_NAME = "saga.getByName";
	public static final String GET_BY_DATE = "saga.getByDate";
	public static final String GET_AUTHOR = "saga.getAuthor";
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Index(columnNames={SAGA_ID}, name = "sagaIdIndex")
	private Long id;
	
	@Column(name = "name", nullable = false)
	@Index(columnNames={SAGA_NAME}, name = "nameIdIndex")
	private String name;
	
	@Column(length = 2000)
	private String description;
    
	@Temporal(TemporalType.DATE)
	private Date date;
	
	/** authorId documentation reference to the user table*/
	@Index(columnNames={SAGA_AUTHOR_ID}, name = "authorIdIndex")
	@Column(nullable = false)
	private Integer authorId;
	
	@Column(name = "payable")
	Boolean payable;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}

	public Boolean getPayable() {
		return payable;
	}

	public void setPayable(Boolean payable) {
		this.payable = payable;
	}
}
