package it.antonio.datapush;

import java.util.HashMap;
import java.util.Map;

import it.antonio.api.Survey;

public enum DataTypes {
	SURVEY{

		@Override
		public Class<?> objectType() {
			return Survey.class;
		}
		
	};
	
	
	
	
	public static Map<Class<?>, DataTypes> types = new HashMap<Class<?>, DataTypes>();
	static {
		types.put(DataTypes.SURVEY.objectType(), DataTypes.SURVEY);
	}
	
	public static DataTypes getFromClass(Class<?>clz) {
		return types.get(clz);
	}
	
	public abstract Class<?> objectType();
	
}
