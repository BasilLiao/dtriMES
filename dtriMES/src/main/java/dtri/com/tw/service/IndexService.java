package dtri.com.tw.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;

@Service
public class IndexService {
	// 取得當前用戶可使用的 功能清單
	public PackageBean getNav(List<SystemGroup> nav) {
		PackageBean bean = new PackageBean();
		JSONArray array = new JSONArray();
		for (SystemGroup systemGroup : nav) {
			JSONObject one = new JSONObject();
			// 有訪問權限
			int aInt = Integer.parseInt("0001000000", 2);
			int bInt = 0;

			bInt = Integer.parseInt(systemGroup.getSgpermission(), 2);
			if (aInt == (aInt & bInt)) {
				// 有權限
				one.put("g_name", systemGroup.getSystemPermission().getSpgname());
				one.put("i_name", systemGroup.getSystemPermission().getSpname());
				one.put("i_url", systemGroup.getSystemPermission().getSpcontrol());
				one.put("i_sort", systemGroup.getSystemPermission().getSyssort());
				one.put("sp_type", systemGroup.getSystemPermission().getSptype());
				array.put(one);
			}
		}
		bean.setBody(new JSONObject().put("nav", array));
		return bean;
	}

	// 簡單包裝回傳
	public JSONObject getUserInfo(SystemUser user) {
		JSONObject user_Obj = new JSONObject();
		user_Obj.put("name", user.getSuname());
		user_Obj.put("position", user.getSuposition());
		user_Obj.put("email", user.getSuemail());
		user_Obj.put("status", user.getSysstatus() == 0 ? "Normal" : "Warning");
		user_Obj.put("c_date", user.getSyscdate());
		return user_Obj;
	}

}
