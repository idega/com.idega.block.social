package is.idega.block.saga;
import org.springframework.beans.factory.annotation.Autowired;

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
		System.out.println(this.sagaDAO.getAuthorByID(Long.valueOf(23)));
		System.out.println("####################################################################################");
		//for(Object o : this.sagaDAO.getAuthorByID(Long.valueOf(23)){
			//System.out.println(o);
		//}

	}

	public void stop(IWBundle arg0) {
		// TODO Auto-generated method stub

	}

}
