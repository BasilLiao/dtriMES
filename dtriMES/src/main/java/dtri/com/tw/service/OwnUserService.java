package dtri.com.tw.service;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.SystemGroupDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;

@Service
public class OwnUserService {
	@Autowired
	private SystemUserDao userDao;
	@Autowired
	private SystemGroupDao groupDao;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size, SystemUser user) {
		PackageBean bean = new PackageBean();
		SystemUser systemUsers = new SystemUser();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_name", FFS.h_t("使用者名稱", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_e_name", FFS.h_t("使用者名稱(英)", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_position", FFS.h_t("職位名稱", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_account", FFS.h_t("帳號", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_password", FFS.h_t("密碼", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_sgid", FFS.h_t("群組", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_email", FFS.h_t("E-mail", "100px", FFM.Wri.W_Y));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "su_id", "ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "su_name", "使用者名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "su_e_name", "使用者名稱(英)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "su_position", "職位名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "su_account", "帳號"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.PASS, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "su_password", "密碼"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "su_sgid", "群組"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-3", true, n_val, "su_email", "E-mail"));
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
		}
		systemUsers = userDao.findAllBySuid(user.getSuid()).get(0);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();

		JSONObject object_body = new JSONObject();
		int ord = 0;
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_id", systemUsers.getSuid());
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_name", systemUsers.getSuname());
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_e_name", systemUsers.getSuename());
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_position", systemUsers.getSuposition());
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_account", systemUsers.getSuaccount());

		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_password", "");
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_sgid", groupDao.findBySgidOrderBySgidAscSyssortAsc(systemUsers.getSusggid()).get(0).getSgname());
		object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_email", systemUsers.getSuemail());
		object_bodys.put(object_body);

		bean.setBody(new JSONObject().put("search", object_bodys));
		return bean;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("modify");
			PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				if (!data.getString("su_password").equals("")) {
					user.setSupassword(pwdEncoder.encode(data.getString("su_password")));
				}
				user.setSuname(data.getString("su_name"));
				user.setSuename(data.getString("su_e_name"));
				user.setSuemail(data.getString("su_email"));
				user.setSysmuser(user.getSuaccount());
				user.setSysmdate(new Date());
				userDao.save(user);
			}
			// 有更新才正確
			if (list.length() > 0) {
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
