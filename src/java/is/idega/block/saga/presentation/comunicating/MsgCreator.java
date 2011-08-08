package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.Constants;

import javax.faces.context.FacesContext;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreUtil;

public class MsgCreator extends PostCreationView {
	private IWContext iwc = null;
	private IWResourceBundle iwrb = null;


	@Override
	protected void initializeComponent(FacesContext context) {
		iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		iwrb = bundle.getResourceBundle(iwc);

	}
}
