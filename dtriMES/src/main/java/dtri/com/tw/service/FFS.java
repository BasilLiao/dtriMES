package dtri.com.tw.service;

import org.json.JSONArray;
import org.json.JSONObject;

import dtri.com.tw.service.FFM.Dno;
import dtri.com.tw.service.FFM.Group_createOnly;
import dtri.com.tw.service.FFM.Group_type;
import dtri.com.tw.service.FFM.Hmb;
import dtri.com.tw.service.FFM.Tag;
import dtri.com.tw.service.FFM.Type;
import dtri.com.tw.service.FFM.Wri;

/**
 * 
 * FrontFormatService = FFS
 * 
 */
public class FFS {
	// 屬性設置
	// public static final String INP = "input", TTA = "textarea", SEL = "select",
	// CHE = "checkbox";
	// public static final String TEXT = "text", NUMB = "number", PASS = "password",
	// DATE = "date";
	// public static final String DIS = "disabled", SHO = "write";
	// 大小
	// public static final String S10 = "100px", S15 = "150px", S20 = "200px", S30 =
	// "300px";
	// 規則設定
	// public static final String H = "_h__", M = "m__", B = "_b__";

	// 格式(CUD)需求包裝
	/**
	 * h_m = h_modify
	 * 
	 * @param none        :是否顯示
	 * @param tag         : 標籤 INP=input,TTA=textarea,SEL=select,CHE=checkbox
	 * @param type        : 類型 EXT=text,NUMB=number,PASS=password,DATE=date
	 * @param placeholder : 預設文字
	 * @param value       : 預設值
	 * @param write       : disabled/write 顯示?
	 * @param col         : 規格
	 * @param required    : 是否必寫
	 * @param values      : 多選項select 才需要[{key:value,key:value}]
	 * @param id          : ID
	 * @param name        : 名稱
	 * 
	 * @return 格式 : {type:"select/input/textarea", <br>
	 *         required:"true/false",placeholder:"XXXX",<br>
	 *         write:"d-none/disabled/write", values:[{key:value,key:value}]}
	 * 
	 **/
	public static JSONObject h_m(Dno none, Tag tag, Type type, String placeholder, String value, Wri write, String col, boolean required,
			JSONArray values, String id, String name) {
		JSONObject object_value = new JSONObject();
		object_value.put("name", name);
		object_value.put("id", FFM.choose(FFM.Hmb.M.toString()) + id);// id
		object_value.put("tag", FFM.choose(tag.toString()));// 類型
		object_value.put("type", FFM.choose(type.toString()));// 驗證格式
		object_value.put("required", required);// 是否必寫
		object_value.put("placeholder", placeholder);// 預設文字
		object_value.put("value", value);// 預設值
		object_value.put("write", FFM.choose(write.toString()));// 可填寫??
		object_value.put("d_none", FFM.choose(none.toString()));// 顯示?

		object_value.put("col", col);// 寬度?
		object_value.put("values", values);// 多選項select (可能有)
		return object_value;
	}

	// 格式(R)需求包裝
	/**
	 * h_search = h_s
	 * 
	 * @param name   :查詢名稱
	 * @param tag    :類型 select/input/textarea
	 * @param type   :驗證格式 text/number/date
	 * @param value  :預設值
	 * @param col    :格是寬度col-(md/lg/sm)-數字
	 * @param id     :該欄位ID
	 * @param values :多選項select 才需要[{key:value,key:value}]
	 * 
	 * @return 格式 :
	 *         {name:xxx,type:(select/input/textarea),col:(col-(md/lg/sm)-數字),id:"",values:[{key:value,key:value}]}
	 **/
	public static JSONObject h_s(Tag tag, Type type, String value, String col, String id, String name, JSONArray values) {
		JSONObject one_search = new JSONObject();
		one_search.put("name", name);
		one_search.put("tag", FFM.choose(tag.toString()));
		one_search.put("type", FFM.choose(type.toString()));
		one_search.put("value", value);// 預設值
		one_search.put("col", col);
		one_search.put("id", id);
		one_search.put("values", values);
		return one_search;
	}

	// Table header 格式(標題/寬度)
	/**
	 * h_t=h_title
	 * 
	 * @param name 欄位名稱
	 * @param size 欄位寬度
	 * 
	 * @return 格式 : {name:xxx,size:50px}
	 **/
	public static JSONObject h_t(String name, String size, Wri write) {
		JSONObject one_title = new JSONObject();
		one_title.put("name", name);// 欄位名稱
		one_title.put("size", size);// 寬度?
		one_title.put("write", FFM.choose(write.toString()));// 顯示?
		return one_title;
	}

	// Table group 群組格式(標題/寬度/顯示?)
	/**
	 * h_g=h_group
	 * 
	 * @param id   欄位ID
	 * @param size 欄位寬度
	 * 
	 * @return 格式 : {name:xxx,size:50px}
	 **/
	public static JSONObject h_g(Wri write, Dno none, String col, String id, String value) {
		JSONObject one_group = new JSONObject();
		one_group.put("id", FFM.choose(FFM.Hmb.M.toString()) + id);// id
		one_group.put("col", col);// 寬度?
		one_group.put("write", FFM.choose(write.toString()));// 可填寫?
		one_group.put("d_none", FFM.choose(none.toString()));// 顯示?
		one_group.put("df_value", value);// 預設內容?

		return one_group;
	}

	// 自動排序
	/**
	 * @param ord 排序 雙位數
	 * 
	 **/
	public static String ord(int ord, Hmb w) {
		return String.format("%02d", ord) + FFM.choose(w.toString());
	}

	/** 群組設定 **/
	public static JSONObject group_set(Group_type type, Group_createOnly create) {
		// 是否為群組模式? type:[group/general] || 新增時群組? createOnly:[all/general]
		return new JSONObject("{'type':'" + type.toString() + "','createOnly':'" + create.toString() + "'}");
	}
}
