package is.idega.block.saga.business;

import java.rmi.RemoteException;
import java.util.logging.Level;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.business.DefaultSpringBean;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.user.business.GroupBusiness;

@Service("sagaServices")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value="sagaServices"),
	@Param(name="javascript", value="SagaServices")
}, name="SagaServices")
public class SagaServices extends DefaultSpringBean implements
		DWRAnnotationPersistance {

	@RemoteMethod
	public Boolean isAllowedToSave(String name){
		GroupBusiness groupBusiness = null;
		try{
			groupBusiness = this.getServiceInstance(GroupBusiness.class);
			return  groupBusiness.getGroupsByGroupName(name).isEmpty();
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "Failed to access GroupBussiness services");
		}

		return Boolean.FALSE;
	}

}
