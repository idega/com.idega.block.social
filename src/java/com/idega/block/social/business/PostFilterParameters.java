package com.idega.block.social.business;

import java.util.Collection;
import java.util.List;

import com.idega.user.data.User;


public class PostFilterParameters {
	private User user = null;
	private int max = 0;
	private Collection<Integer> creators = null;
	private Collection<Integer> receivers = null;
	private List <String> types = null;
	private String beginUri = null;
	private String getUp = null;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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

}
