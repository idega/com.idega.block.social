package com.idega.block.social.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.idega.dwr.business.DWRAnnotationPersistance;

@DataTransferObject
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PostFilterParameters implements Serializable, DWRAnnotationPersistance {
	private static final long serialVersionUID = -259958359200816358L;
	
	@RemoteProperty
	private int max = 0;
	@RemoteProperty
	private Collection<Integer> creators = null;
	@RemoteProperty
	private Collection<Integer> receivers = null;
	@RemoteProperty
	private List <String> types = null;
	@RemoteProperty
	private String beginUri = null;
	@RemoteProperty
	private String getUp = null;

	public PostFilterParameters(){
		super();
	}
	
	protected PostFilterParameters(PostFilterParameters postFilterParameters){
		if(postFilterParameters == null){
			return;
		}
		setBeginUri(postFilterParameters.getBeginUri());
		setCreators(postFilterParameters.getCreators());
		setGetUp(postFilterParameters.getGetUp());
		setMax(postFilterParameters.getMax());
		setReceivers(postFilterParameters.getReceivers());
		setTypes(postFilterParameters.getTypes());
	}
	
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public Collection<Integer> getCreators() {
		return creators;
	}
	public void setCreators(Collection<Integer> creators) {
		this.creators = creators;
	}
	public Collection<Integer> getReceivers() {
		return receivers;
	}
	public void setReceivers(Collection<Integer> receivers) {
		this.receivers = receivers;
	}
	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	public String getBeginUri() {
		return beginUri;
	}
	public void setBeginUri(String beginUri) {
		this.beginUri = beginUri;
	}
	public String getGetUp() {
		return getUp;
	}
	public void setGetUp(String getUp) {
		this.getUp = getUp;
	}
	
	public String toString(){
		return new Gson().toJson(this);
	}

}
