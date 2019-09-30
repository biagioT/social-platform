package it.antonio.social;

import java.io.Serializable;

public class SocialData implements Serializable {

	private static final long serialVersionUID = -4230982400591122314L;
	
	public Object value;
	
	public SocialData(Object value) {
		super();
		this.value = value;
	}

	public boolean typeOf(Class<? extends Serializable> clz) {
		return clz.isAssignableFrom(value.getClass());
	}
	
	public <T extends Serializable> T getValue(Class<T> clz) {
		return clz.cast(value);
	}
	
}
