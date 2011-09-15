package com.idega.block.social.presentation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.idega.presentation.IWBaseComponent;
import com.idega.util.CoreConstants;

public class SimpleForm   extends IWBaseComponent{


	private String styleClass = null;

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
	       ResponseWriter writer = context.getResponseWriter();
	    writer.startElement("form", this);
	    writer.writeAttribute("id", this.getId(), null);
	    writer.writeAttribute("class", this.styleClass, null);

	    super.encodeBegin(context);
	}
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
	       ResponseWriter writer = context.getResponseWriter();
	    writer.endElement("form");

	    super.encodeEnd(context);
	}
	public String getStyleClass() {
		return styleClass;
	}
	public void setStyleClass(String styleClass) {
		if(this.styleClass == null){
			this.styleClass = styleClass;
		}
		else{
			this.styleClass = this.styleClass + CoreConstants.SPACE + styleClass;
		}
	}

}
