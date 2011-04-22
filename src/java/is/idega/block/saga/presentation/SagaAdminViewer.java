package is.idega.block.saga.presentation;

import is.idega.block.saga.Constants;

import javax.faces.context.FacesContext;

import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.PresentationUtil;

public class SagaAdminViewer extends IWBaseComponent {
	private IWBundle iwb;
	private IWResourceBundle iwrb;

	@Override
	public void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		this.iwb = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		this.iwrb = this.iwb.getResourceBundle(iwc.getCurrentLocale());
		
		PresentationUtil.addStyleSheetToHeader(iwc, this.iwb.getVirtualPathWithFileNameString("style/sagaAdminView.css"));
		
		this.present(context);
	}
	
	private void present(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		
		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(iwb.getFaceletURI("admin/admin.xhtml"));
		this.add(facelet);
		
	}
	
	protected void createSaga(){
		
	}
	
	protected void editSaga(){
		
	}
	
	protected void deleteSaga(){
		
	}

}
