package is.idega.block.saga.data.dao;

import is.idega.block.saga.data.Saga;

import java.util.Date;
import java.util.List;

import com.idega.core.persistence.GenericDao;

public interface SagaDAO extends GenericDao{
	public abstract List<Saga> getAll();
	
	public abstract List<Saga> getByName(String name);
	
	public abstract List<Saga> getByDate(Date date);
	
	public abstract Integer getAuthorByID(Long id);
}
