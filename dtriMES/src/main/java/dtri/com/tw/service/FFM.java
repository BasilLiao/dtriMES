package dtri.com.tw.service;

public class FFM {
	/** 限制規格-ID格式 **/
	public static enum Hmb {
		H, M, B
	}

	/** 限制規格-可填入? **/
	public static enum Wri {
		W_Y, W_N
	}

	/** 限制規格-顯示? **/
	public static enum Dno {
		D_N, D_S
	}

	/** 限制規格-標籤 **/
	public static enum Tag {
		INP, TTA, SEL, CHE
	}

	/** 限制規格-類型 **/
	public static enum Type {
		TEXT, NUMB, PASS, DATE, CHE
	}

	/** 限制規格-大小 **/
	public static enum Size {
		S10, S15, S20, S25, S30
	}

	/** 資料呈現型態(group 群組類/general一般類) **/
	public static enum Group_type {
		group, general
	}

	/** 群組-新增-類型(只要一般?general/全部?all) **/
	public static enum Group_createOnly {
		all, general
	}

	// 解析-代號-屬性設置
	public static String choose(String key) {
		String value = "";
		switch (key) {
		case "D_N":
			value = "d-none";
			break;
		// See
		case "W_N":
			value = "disabled";
			break;
		case "W_Y":
			value = "show";
			break;
		// Tag
		case "INP":
			value = "input";
			break;
		case "TTA":
			value = "textarea";
			break;
		case "SEL":
			value = "select";
			break;
		case "CHE":
			value = "checkbox";
			break;
		// Type
		case "TEXT":
			value = "text";
			break;
		case "NUMB":
			value = "number";
			break;
		case "PASS":
			value = "password";
			break;
		case "DATE":
			value = "date";
			break;
		// SIZE
		case "S10":
			value = "100px";
			break;
		case "S15":
			value = "150px";
			break;
		case "S20":
			value = "200px";
			break;
		case "S25":
			value = "250px";
			break;
		case "S30":
			value = "300px";
			break;
		// ID-格式
		case "H":
			value = "_h__";
			break;
		case "B":
			value = "_b__";
			break;
		case "M":
			value = "m__";
			break;
		// col
		}
		return value;
	}
}
