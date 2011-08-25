package is.idega.block.saga.business;

import com.idega.user.data.User;


public class PostFilterParameters {
	private User user = null;
	private int max = 0;
	private boolean getAll = false;

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
	public boolean isGetAll() {
		return getAll;
	}
	public void setGetAll(boolean getAll) {
		this.getAll = getAll;
	}

}
