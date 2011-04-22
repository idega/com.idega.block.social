package is.idega.block.saga.data.dao.impl;

import is.idega.block.saga.data.Saga;
import is.idega.block.saga.data.dao.SagaDAO;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.persistence.impl.GenericDaoImpl;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SagaDAOImpl extends GenericDaoImpl implements SagaDAO {

	public List<Saga> getAll() {
		List<Saga> allSagas = this.getResultList(Saga.GET_ALL, Saga.class);
		return allSagas;
	}

	public List<Saga> getByName(String name) {
		List<Saga> sagasByName = this.getResultList(Saga.GET_BY_NAME, Saga.class);
		return sagasByName;
	}

	public List<Saga> getByDate(Date date) {
		List<Saga> sagasByDate = this.getResultList(Saga.GET_BY_DATE, Saga.class);
		return sagasByDate;
	}

	public Integer getAuthorByID(Long id) {
		List<Integer> author = this.getResultList(Saga.GET_AUTHOR_BY_ID, Integer.class);
		return author.get(0);
	}

}
