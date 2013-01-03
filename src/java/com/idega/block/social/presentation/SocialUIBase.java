package com.idega.block.social.presentation;

import com.idega.block.social.SocialConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWUIBase;

public abstract class SocialUIBase extends IWUIBase{
	IWResourceBundle iwrb = null;
	
	protected IWResourceBundle getIwrb() {
		if(iwrb == null){
			iwrb = getIwc().getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(getIwc());
		}
		return iwrb;
	}

	protected void setIwrb(IWResourceBundle iwrb) {
		this.iwrb = iwrb;
	}

}
