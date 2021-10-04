package dtri.com.tw.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Fm_json {
	/**
	 * 測試JSON 格式
	 * 
	 * @param test JSON 規則
	 * 
	 **/
	public static boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			// edited, to include @Arthur's comment
			// e.g. in case JSONArray is valid as well...
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}
}
