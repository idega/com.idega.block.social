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
			@NamedQuery(name = Saga.GET_ALL, query = "FROM Saga s"),
			@NamedQuery(name = Saga.GET_BY_NAME, query = "from Saga s where s.name = :"+Saga.nameProp),
			@NamedQuery(name = Saga.GET_BY_DATE, query = "from Saga s where s.date = :"+Saga.dateProp),
			@NamedQuery(name = Saga.GET_AUTHOR_BY_ID, query = "from Saga s where s.id = :"+Saga.idProp)
		}
)
public class Saga implements Serializable {

	public static final String GET_ALL = "saga.getAll";
	public static final String GET_BY_NAME = "saga.getByName";
	public static final String GET_BY_DATE = "saga.getByDate";
	public static final String GET_AUTHOR_BY_ID = "saga.getAuthorByID";
	
	public static final String SAGA_ID = "SAGA_ID_INDEX";
	public static final String SAGA_NAME = "SAGA_NAME_INDEX";
	public static final String SAGA_AUTHOR_ID = "SAGA_AUTHOR_ID_INDEX";
	
	private static final long serialVersionUID = -7986373392792826654L;
	
	public static final String idProp = "id";
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Index(columnNames={SAGA_ID}, name = "sagaIdIndex")
	private Long id;
	
	public static final String nameProp = "name";
	@Column(name = "NAME", nullable = false)
	@Index(columnNames={SAGA_NAME}, name = "nameIdIndex")
	private String name;
	
	public static final String descriptionProp = "description";
	@Column(name = "DESCRIPTION", length = 2000)
	private String description;
    
	public static final String dateProp = "date";
	@Temporal(TemporalType.DATE)
	@Column(name = "DATE")
	private Date date;
	
	/** authorId documentation reference to the user table*/
	public static final String authorIdProp = "authorId";
	@Index(columnNames={SAGA_AUTHOR_ID}, name = "authorIdIndex")
	@Column(name = "AUTHOR_ID",nullable = false)
	private Integer authorId;
	
	public static final String payableProp = "payable";
	@Column(name = "PAYABLE")
	Boolean payable;

	public Saga() { }
	
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
