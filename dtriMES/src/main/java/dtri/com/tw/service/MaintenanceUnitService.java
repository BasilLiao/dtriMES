package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.MaintenanceUnit;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.MaintenanceUnitDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class MaintenanceUnitService {
	@Autowired
	private MaintenanceUnitDao unitDao;
	@Autowired
	private SystemUserDao systemUserDao;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size, SystemUser user) {
		PackageBean bean = new PackageBean();
		List<MaintenanceUnit> mUnits = new ArrayList<MaintenanceUnit>();
		List<MaintenanceUnit> mUnit_sons = new ArrayList<MaintenanceUnit>();

		ArrayList<SystemUser> users = new ArrayList<SystemUser>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("mugid").descending());
		String mu_g_name = null;
		String mu_su_name = null;
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t("群組代表", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t("UI_Group_ID", "100px", FFM.Wri.W_N));// 群組專用-必須放前面

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_su_name", FFS.h_t("負責人(單位)", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_su_id", FFS.h_t("負責人(ID)", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_id", FFS.h_t("ID", "50px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_g_id", FFS.h_t("單位ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_g_name", FFS.h_t("名稱(單位)", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_content", FFS.h_t("工作內容(負責事項)", "400px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mu_cell_mail", FFS.h_t("通知Mail", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "100px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mu_id", "ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mu_g_id", "單位ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "mu_su_name", "單位(成員)"));
			//
			JSONArray st_val = new JSONArray();
			users = systemUserDao.findAllBySystemUserNotAdmin(null, null);
			for (SystemUser sysUser : users) {
				// 保持長度 4
				if (sysUser.getSutemplate().length() < 4) {
					sysUser.setSutemplate(sysUser.getSutemplate() + "　　");
				} else if (sysUser.getSutemplate().length() < 3) {
					sysUser.setSutemplate(sysUser.getSutemplate() + "　");
				}
				String value = sysUser.getSutemplate() + " | " + sysUser.getSuposition() + " | " + sysUser.getSuname();
				st_val.put((new JSONObject()).put("value", value).put("key", sysUser.getSuid()));
			}
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, st_val, "mu_su_id", "人員清單"));
			//
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "否").put("key", false));
			s_val.put((new JSONObject()).put("value", "是").put("key", true));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "false", FFM.Wri.W_Y, "col-md-1", true, s_val, "mu_cell_mail", "是否寄信"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "mu_g_name", "單位名稱(部門)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-4", false, n_val, "mu_content", "工作內容(負責事項)"));
			//
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "開啟").put("key", "0"));
			s_val.put((new JSONObject()).put("value", "關閉").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val, "sys_status", "狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", "群組代表"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", "備註"));

			bean.setCell_modify(obj_m);

			// 放入群主指定 [(key)](modify/Create/Delete) 格式
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "mu_g_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "mu_content", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_header", "true"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mu_g_id", ""));// 父類別

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "mu_su_name", "負責人", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "mu_g_name", "單位名稱", n_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			mu_g_name = body.getJSONObject("search").getString("mu_g_name");
			mu_g_name = mu_g_name.equals("") ? null : mu_g_name;
			mu_su_name = body.getJSONObject("search").getString("mu_su_name");
			mu_su_name = mu_su_name.equals("") ? null : mu_su_name;
		}

		// 查詢子類別?全查?
		if (mu_su_name != null) {
			mUnits = unitDao.findAllByMaintenanceUnit(0L, mu_g_name, mu_su_name, true, page_r);
			if (mUnits.size() > 0) {
				mUnit_sons = unitDao.findAllByMaintenanceUnit(0L, mUnits.get(0).getMugname(), null, false, null);
			}
		} else {
			mUnits = unitDao.findAllByMaintenanceUnit(0L, mu_g_name, null, true, page_r);
			if (mUnits.size() > 0) {
				mUnit_sons = unitDao.findAllByMaintenanceUnit(0L, mu_g_name, null, false, null);
			}
		}

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		// 父類別物件
		mUnits.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getMugid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_su_name", one.getMusuname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_su_id", one.getMusuid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_id", one.getMuid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_g_id", one.getMugid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_g_name", one.getMugname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_content", one.getMucontent());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_cell_mail", one.getMucellmail());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());

			object_bodys.put(object_body);
			// 準備子類別容器
			object_bodys_son.put(one.getMugid() + "", new JSONArray());
		});
		bean.setBody(new JSONObject().put("search", object_bodys));

		// 子類別物件
		mUnit_sons.forEach(one -> {
			JSONObject object_son = new JSONObject();
			int ord = 0;
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getMugid());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_su_name", one.getMusuname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_su_id", one.getMusuid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_id", one.getMuid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_g_id", one.getMugid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_g_name", one.getMugname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_content", one.getMucontent());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mu_cell_mail", one.getMucellmail());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_bodys_son.getJSONArray(one.getMugid() + "").put(object_son);
		});
		bean.setBody(bean.getBody().put("search_son", object_bodys_son));

		// 是否為群組模式? type:[group/general] || 新增時群組? createOnly:[all/general]
		bean.setBody_type(new JSONObject("{'type':'group','createOnly':'all'}"));
		return bean;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("create");
			Long mu_g_id = 0l;// 部門ID
			String mu_g_name = "";// 部門名稱
			String mu_content = "";// 工作事項
			// 如果沒資料則不做事
			if (list.length() == 0) {
				return true;
			}
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				MaintenanceUnit obj = new MaintenanceUnit();
				SystemUser users = systemUserDao.findAllBySuid(data.getLong("mu_su_id")).get(0);
				String musuName = users.getSutemplate() + " | " + users.getSuposition() + " | " + users.getSuname();
				obj.setMusuname(musuName);
				obj.setMusuid(data.getLong("mu_su_id"));
				obj.setMucellmail(data.getBoolean("mu_cell_mail"));
				obj.setSysnote(data.getString("sys_note"));
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());
				obj.setSysnote("");

				// 如果是特定的子類別
				if (data.getString("mu_g_name") != null && !data.getString("mu_g_id").equals("")) {
					mu_g_id = data.getLong("mu_g_id");
					mu_g_name = data.getString("mu_g_name");
				}

				// 新建 群組代表名稱-父類別
				if (mu_g_id == 0) {
					// 存入資料
					MaintenanceUnit obj_h = obj;
					obj_h.setMugid(unitDao.getMaintenance_unit_g_seq());
					obj_h.setMugname(data.getString("mu_g_name"));
					obj_h.setMucontent(data.getString("mu_content"));

					obj_h.setSysheader(true);
					unitDao.save(obj_h);

					mu_g_id = obj_h.getMugid();
					mu_g_name = obj_h.getMugname();
					mu_content = obj_h.getMucontent();
				} else {
					// 登記子類別
					obj.setSysheader(false);
					obj.setMugid(mu_g_id);
					obj.setMugname(mu_g_name);
					obj.setMucontent(mu_content);

					unitDao.save(obj);
				}
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 另存檔 資料清單
	@Transactional
	public boolean save_asData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("save_as");
			Long mu_g_id = 0l;// 部門ID
			String mu_g_name = "";// 部門名稱
			String mu_content = "";// 工作事項
			// 如果沒資料則不做事
			if (list.length() == 0) {
				return true;
			}

			// 檢查群組-名稱重複(沒重複 則定義 group_name)
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				if (data.getBoolean("sys_header")) {
					ArrayList<MaintenanceUnit> obj_h = unitDao.findAllByGroupTop1(data.getString("mu_g_name"), PageRequest.of(0, 1));
					if (obj_h != null && obj_h.size() > 0) {
						return false;
					} else {
						mu_g_name = data.getString("mu_g_name");
					}
				}
			}
			// 檢查群組-沒定義到 mu_g_name 則排除
			if (mu_g_name.equals("")) {
				return false;
			}
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				MaintenanceUnit obj = new MaintenanceUnit();
				SystemUser users = systemUserDao.findAllBySuid(data.getLong("mu_su_id")).get(0);
				String musuName = users.getSutemplate() + " | " + users.getSuposition() + " | " + users.getSuname();
				obj.setMusuname(musuName);
				obj.setMusuid(data.getLong("mu_su_id"));
				obj.setMucellmail(data.getBoolean("mu_cell_mail"));
				obj.setSysnote(data.getString("sys_note"));
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());
				obj.setSysnote("");

				// 新建 群組代表名稱-父類別
				if (mu_g_id == 0) {
					// 存入資料
					MaintenanceUnit obj_h = obj;
					obj_h.setMugid(unitDao.getMaintenance_unit_g_seq());
					obj_h.setMugname(data.getString("mu_g_name"));
					obj_h.setMucontent(data.getString("mu_content"));

					obj_h.setSysheader(true);
					unitDao.save(obj_h);

					mu_g_id = obj_h.getMugid();
					mu_g_name = obj_h.getMugname();
					mu_content = obj_h.getMucontent();
				} else {
					// 登記子類別
					obj.setSysheader(false);
					obj.setMugid(mu_g_id);
					obj.setMugname(mu_g_name);
					obj.setMucontent(mu_content);

					unitDao.save(obj);
				}
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("modify");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				SystemUser users = systemUserDao.findAllBySuid(data.getLong("mu_su_id")).get(0);
				MaintenanceUnit obj = unitDao.getOne(data.getLong("mu_id"));
				String musuName = users.getSutemplate() + " | " + users.getSuposition() + " | " + users.getSuname();
				obj.setMusuname(musuName);
				obj.setMusuid(data.getLong("mu_su_id"));
				obj.setMucellmail(data.getBoolean("mu_cell_mail"));
				obj.setSysnote(data.getString("sys_note"));
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSysmuser(user.getSuaccount());
				obj.setSysnote("");

				// 檢查[群組名稱]重複
				ArrayList<MaintenanceUnit> obj_h = unitDao.findAllByGroupTop1(data.getString("mu_g_name"), PageRequest.of(0, 1));
				if (obj_h != null || data.getBoolean("sys_header")) {
					// 如果是 父類別(限定修改)+(子類別全數修改)
					if (data.getBoolean("sys_header")) {
						List<MaintenanceUnit> obj_h_old = unitDao.findByMugidOrderBySyssortAsc(data.getLong("mu_g_id"));
						obj_h_old.forEach(d -> {
							d.setMugname(data.getString("mu_g_name"));
							d.setMucontent(data.getString("mu_content"));
							d.setSysmuser(user.getSuaccount());
							unitDao.save(d);
						});

						// 父類別(限定修改) 還原
						MaintenanceUnit obj_h_one = unitDao.getOne(data.getLong("mu_id"));
						obj_h_one.setMusuname(users.getSutemplate() + " | " + users.getSuposition() + " | " + users.getSuname());
						obj_h_one.setSysmuser(user.getSuaccount());
						obj_h_one.setMucellmail(data.getBoolean("mu_cell_mail"));
						obj_h_one.setMucontent(data.getString("mu_content"));
						obj_h_one.setSysheader(true);
						unitDao.save(obj_h_one);
					} else {
						// 子類別
						unitDao.save(obj);
					}
					check = true;
				} else {
					// 如果 不是修改類 則
					check = false;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(JSONObject body, SystemUser user) {

		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;

				MaintenanceUnit obj = new MaintenanceUnit();
				obj.setMuid(data.getLong("mu_id"));

				if (data.getBoolean("sys_header")) {
					// 父類別 關聯全清除
					unitDao.deleteByMugid(data.getLong("mu_g_id"));
				} else {
					unitDao.delete(obj);
				}
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}
}
