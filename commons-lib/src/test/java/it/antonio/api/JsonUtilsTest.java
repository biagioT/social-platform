package it.antonio.api;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonUtilsTest {

	@Test
	void test() {
		
		Survey survey = new Survey();
		survey.setName("name");
		survey.getData().put("c1", "ok");
		survey.getData().put("c2", 100);
		survey.getData().put("c3", true);
		
		Map<String, Object> subData = new HashMap<String, Object>();
		subData.put("sd1", "subdata1");
		subData.put("sd2", 999);
		survey.getData().put("c4", subData);
		
		String serialized = JsonUtils.toJson(survey);
		System.out.println(serialized);
		
		Survey survey2 = JsonUtils.fromJson(serialized, Survey.class);
		System.out.println(survey2);
		
	}

}
