package dtri.com.tw.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import dtri.com.tw.db.entity.ProductionSN;

public class Fm_SN {

	/**
	 * 解析
	 * 
	 * @param entity 規則
	 * @param total  總數
	 **/
	public static JSONObject analyze_Sn(ArrayList<ProductionSN> entity,boolean added, int total) {
		JSONObject obj = new JSONObject();
		if (entity.size() < 1) {
			return obj;
		}
		String sn_item = "";
		String sn_000_value = "";
		String sn_000_name = "";
		String sn_YYWW_value = "";
		String sn_YYWW_name = "";
		ArrayList<String> sn_list = new ArrayList<String>();

		// 取出 現在(動態部分)序列規則->[機種別][生產廠別][保固期限][生產年周][Panel廠商][流水號]
		for (ProductionSN s : entity) {
			if (s.getPsvalue() != null && s.getPsvalue() != "") {
				if (s.getPsgid()!=null && s.getPsgid() == 4) {
					// 年週期
					sn_item += s.getPsname();
					sn_YYWW_value = s.getPsvalue();
					sn_YYWW_name = s.getPsname();
				} else if (s.getPsgid()!=null && s.getPsgid() == 6) {
					// 序號
					sn_item += s.getPsname();
					sn_000_value = s.getPsvalue();
					sn_000_name = s.getPsname();
				} else {
					sn_item += s.getPsvalue();
				}
			}
		}

		// 生產產品序號
		String sn_for_item = sn_item;
		for (int i = 0; i < total; i++) {

			// 流水號
			sn_000_value = get000(sn_000_value,added);
			added = true;
			// 如果數字為0 則在 年週期 加1(進位)
			if (sn_000_value.equals("000")) {
				sn_YYWW_value = ("" + (Integer.parseInt(sn_YYWW_value) + 1));
				sn_000_value = String.format("%0" + sn_000_value.length() + "d", 1);
			}
			sn_for_item = sn_item.replace(sn_000_name, sn_000_value);

			// 年周
			sn_YYWW_value = getYYWW(sn_YYWW_value);
			sn_for_item = sn_for_item.replace(sn_YYWW_name, sn_YYWW_value);

			sn_list.add(sn_for_item);
		}
		obj.put("sn_list", sn_list);
		obj.put("sn_YYWW", sn_YYWW_value);
		obj.put("sn_000", sn_000_value);
		return obj;
	}

	// 規則 流水號+1/(如果是更新需要起始,不累加+0)
	public static String get000(String nb,boolean added) {
		int l = nb.length();
		int s = Integer.parseInt(nb);
		if(added) {
			s += 1;			
		}
		String nb_new = String.format("%0" + l + "d", s);
		if (nb_new.length() > l) {
			nb_new = nb_new.substring(1, 4);
		}
		return nb_new;
	}
	
	// 規則 取得年周
	public static String getYYWW(String old_YYWW) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		// 如果發現 舊的大於 新的 年週期 ,則保持舊的周期數 ,反之在使用新的周期數
		String week = String.format("%02d", (cal.get(Calendar.WEEK_OF_YEAR) - 1));
		String year = (cal.get(Calendar.YEAR) + "").substring(2, 4);
		int new_YYWW = Integer.parseInt(year + week);
		if (new_YYWW < Integer.parseInt(old_YYWW)) {
			return old_YYWW + "";
		}
		return new_YYWW + "";
	}
}
