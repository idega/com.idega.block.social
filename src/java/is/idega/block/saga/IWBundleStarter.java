package is.idega.block.saga;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import is.idega.block.saga.data.Saga;
import is.idega.block.saga.data.dao.SagaDAO;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.util.expression.ELUtil;


public class IWBundleStarter implements IWBundleStartable {

	@Autowired
	private SagaDAO sagaDAO;
	
	public void start(IWBundle arg0) {
		ELUtil.getInstance().autowire(this);
		System.out.println("###################################################################################");
		Saga saga = new Saga();
		saga.setAuthorId(123);
		Timestamp date = new Timestamp(System.currentTimeMillis());
		saga.setDate(date);
		saga.setDescription("Velnias");
		saga.setPayable(true);
		saga.setName("Gauruotas");
		this.sagaDAO.persist(saga);
		System.out.println(this.sagaDAO.getAll());
		System.out.println("####################################################################################");
		//for(Object o : this.sagaDAO.getAuthorByID(Long.valueOf(23)){
			//System.out.println(o);
		//}

	}

	public void stop(IWBundle arg0) {
		// TODO Auto-generated method stub

	}

}
