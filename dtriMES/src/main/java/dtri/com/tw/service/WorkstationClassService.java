package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.SystemConfig;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationClass;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class WorkstationClassService {
	@Autowired
	private WorkstationClassDao classDao;
	@Autowired
	private SystemUserDao systemUserDao;
	@Autowired
	private WorkstationDao workstationDao;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size) {
		PackageBean bean = new PackageBean();
		ArrayList<WorkstationClass> classes = new ArrayList<WorkstationClass>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("wcid").descending());
		// 查詢
		String search_wc_p_line = null;
		String search_wc_class = null;
		String search_status = "0";

		// 功能-名稱編譯
		String wc_id = "ID", wc_s_time = "班別(起始時)", wc_e_time = "班別(結束時)", wc_l_name = "組長名稱", wc_l_su_id = "組長選擇", //
				wc_m_name = "管理者(名稱)", wc_m_su_id = "管理者選擇", wc_p_line = "產線別", wc_class = "班別", //
				wc_w_c_name = "工作站(名稱)", wc_w_pb_name = "工作站選擇", wc_group = "工作站(模式)?", wc_w_time = "工時登記?", //
				wc_w_quantity = "數量統計?", wc_s_auto = "開始(自動)?", wc_e_auto = "結束(自動)?";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", sys_note = "備註", //
				sys_sort = "排序", sys_ver = "版本", sys_status = "狀態", sys_header = "群組", ui_group_id = "UI_Group_ID";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_id", FFS.h_t(wc_id, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_s_time", FFS.h_t(wc_s_time, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_e_time", FFS.h_t(wc_e_time, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_l_name", FFS.h_t(wc_l_name, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_l_su_id", FFS.h_t(wc_l_su_id, "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_m_name", FFS.h_t(wc_m_name, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_m_su_id", FFS.h_t(wc_m_su_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_p_line", FFS.h_t(wc_p_line, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_class", FFS.h_t(wc_class, "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_w_c_name", FFS.h_t(wc_w_c_name, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_w_pb_name", FFS.h_t(wc_w_pb_name, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_group", FFS.h_t(wc_group, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_w_time", FFS.h_t(wc_w_time, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_w_quantity", FFS.h_t(wc_w_quantity, "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_s_auto", FFS.h_t(wc_s_auto, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wc_e_auto", FFS.h_t(wc_e_auto, "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t(sys_sort, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t(sys_ver, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_N));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();
			JSONArray st_val = new JSONArray();
			ArrayList<SystemUser> users = new ArrayList<SystemUser>();
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

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "wc_id", wc_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TIME, "", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "wc_s_time", wc_s_time));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TIME, "", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "wc_e_time", wc_e_time));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "wc_l_name", wc_l_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, st_val, "wc_l_su_id", wc_l_su_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "wc_m_name", wc_m_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, st_val, "wc_m_su_id", wc_m_su_id));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "wc_p_line", wc_p_line));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "wc_class", wc_class));

			JSONArray a_vals = new JSONArray();
			ArrayList<Workstation> workstations = workstationDao.findAllBySysheader(true, PageRequest.of(0, 999));
			workstations.forEach(w -> {
				if (w.getWgid() != 0)
					a_vals.put((new JSONObject()).put("value", w.getWpbname()).put("key", w.getWgid()));
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "wc_w_c_name", wc_w_c_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, a_vals, "wc_w_pb_name", wc_w_pb_name));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "群組模式").put("key", "true"));
			a_val.put((new JSONObject()).put("value", "單人模式").put("key", "false"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, a_val, "wc_group", wc_group));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "自動").put("key", "true"));
			a_val.put((new JSONObject()).put("value", "手動").put("key", "false"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, a_val, "wc_s_auto", wc_s_auto));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, a_val, "wc_e_auto", wc_e_auto));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, a_val, "wc_w_time", wc_w_time));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, a_val, "wc_w_quantity", wc_w_quantity));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_date", sys_c_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_user", sys_c_user));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", true, n_val, "sys_sort", sys_sort));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", sys_ver));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", sys_status));
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "wc_p_line", wc_p_line, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "wc_class", wc_class, n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", sys_status, a_val));
			bean.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			search_wc_p_line = body.getJSONObject("search").getString("wc_p_line");
			search_wc_p_line = search_wc_p_line.equals("") ? null : search_wc_p_line;

			search_wc_class = body.getJSONObject("search").getString("wc_class");
			search_wc_class = search_wc_class.equals("") ? null : search_wc_class;

			search_status = body.getJSONObject("search").getString("sys_status");
			search_status = search_status.equals("") ? "0" : search_status;
		}
		classes = classDao.findAllByClass(search_wc_class, search_wc_p_line, Integer.parseInt(search_status), page_r);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		classes.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_id", one.getWcid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_s_time", one.getWcstime());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_e_time", one.getWcetime());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_l_name", one.getWclname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_l_su_id", one.getWclsuid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_m_name", one.getWcmname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_m_su_id", one.getWcmsuid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_p_line", one.getWcpline());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_class", one.getWcclass());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_w_c_name", one.getWcwcname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_w_pb_name", one.getWcwpbname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_group", one.getWcgroup());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_w_time", one.getWcwtime());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_w_quantity", one.getWcwquantity());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_s_auto", one.getWcsauto());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wc_e_auto", one.getWceauto());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_bodys.put(object_body);
		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		return bean;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("create");
			SystemUser sysUser = new SystemUser();
			for (Object one : list) {
				// 物件轉換
				WorkstationClass item = new WorkstationClass();
				JSONObject data = (JSONObject) one;
				sysUser = systemUserDao.findAllBySuid(data.getLong("wc_l_su_id")).get(0);
				String wc_l_name = sysUser.getSutemplate() + " | " + sysUser.getSuposition() + " | " + sysUser.getSuname();
				sysUser = systemUserDao.findAllBySuid(data.getLong("wc_m_su_id")).get(0);
				String wc_m_name = sysUser.getSutemplate() + " | " + sysUser.getSuposition() + " | " + sysUser.getSuname();

				item.setWcstime(data.getString("wc_s_time"));
				item.setWcetime(data.getString("wc_e_time"));
				item.setWclname(wc_l_name);
				item.setWclsuid(data.getLong("wc_l_su_id"));

				item.setWcmname(wc_m_name);
				item.setWcmsuid(data.getLong("wc_m_su_id"));

				item.setWcstime(data.getString("wc_p_line"));
				item.setWcstime(data.getString("wc_class"));

				Optional<Workstation>  lists = workstationDao.findById(data.getLong("wc_w_pb_name"));
				String  wc_w_c_name= lists.get().getWcname();
				item.setWcwcname(wc_w_c_name);
				item.setWcwpbname(data.getString("wc_w_pb_name"));

				item.setWcgroup(data.getString("wc_group"));
				item.setWcsauto(data.getString("wc_s_auto"));
				item.setWceauto(data.getString("wc_e_auto"));
				item.setWcwtime(data.getString("wc_w_time"));
				item.setWcwquantity(data.getString("wc_w_quantity"));

				item.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
				item.setSyssort(data.getInt("sys_sort"));
				item.setSysstatus(data.getInt("sys_status"));
				item.setSysmuser(user.getSuaccount());
				item.setSyscuser(user.getSuaccount());

				// 檢查名稱重複
				ArrayList<WorkstationClass> arrayList = classDao.findAllByClass(data.getString("wc_class"), data.getString("wc_p_line"), wc_w_c_name, null);
				if (arrayList != null && arrayList.size() > 0) {
					check = false;
					return check;
				}
				classDao.save(item);
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// 物件轉換
				SystemConfig sys_c = new SystemConfig();
				JSONObject data = (JSONObject) one;
				sys_c.setScname(data.getString("sc_name"));
				sys_c.setScgname(data.getString("sc_g_name"));
				sys_c.setScvalue(data.getString("sc_value"));
				sys_c.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
				sys_c.setSyssort(data.getInt("sys_sort"));
				sys_c.setSysstatus(data.getInt("sys_status"));
				sys_c.setSysmuser(user.getSuaccount());
				sys_c.setSyscuser(user.getSuaccount());

				// 檢查群組名稱重複
//				ArrayList<SystemConfig> sys_c_g = classDao.findAllByConfigGroupTop1(sys_c.getScgname(), PageRequest.of(0, 1));
//				if (sys_c_g != null && sys_c_g.size() > 0) {
//					// 重複 則取同樣G_ID
//					sys_c.setScgid(sys_c_g.get(0).getScgid());
//				} else {
//					// 取得最新G_ID
//					sys_c.setScgid(classDao.getSystem_config_g_seq());
//				}
//				classDao.save(sys_c);
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
				SystemConfig sys_p = new SystemConfig();
				JSONObject data = (JSONObject) one;
				sys_p.setScid(data.getLong("sc_id"));
				sys_p.setScname(data.getString("sc_name"));
				sys_p.setScgid(data.getLong("sc_g_id"));
				sys_p.setScgname(data.getString("sc_g_name"));
				sys_p.setScvalue(data.getString("sc_value"));
				sys_p.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
				sys_p.setSyssort(data.getInt("sys_sort"));
				sys_p.setSysstatus(data.getInt("sys_status"));
				sys_p.setSysmuser(user.getSuaccount());
				sys_p.setSysmdate(new Date());

				// 檢查群組名稱重複
//				ArrayList<SystemConfig> sys_p_g = classDao.findAllByConfigGroupTop1(sys_p.getScgname(), PageRequest.of(0, 1));
//				if (sys_p_g != null && sys_p_g.size() > 0) {
//					// 重複 則取同樣G_ID
//					sys_p.setScgid(sys_p_g.get(0).getScgid());
//				} else {
//					// 取得最新G_ID
//					sys_p.setScgid(classDao.getSystem_config_g_seq());
//				}
//				classDao.save(sys_p);
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

	// 移除 資料清單
	@Transactional
	public boolean deleteData(JSONObject body) {

		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				SystemConfig sys_p = new SystemConfig();
				JSONObject data = (JSONObject) one;
				sys_p.setScid(data.getLong("sc_id"));

				// classDao.deleteByScidAndSysheader(sys_p.getScid(), false);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
