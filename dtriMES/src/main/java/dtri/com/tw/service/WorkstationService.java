package dtri.com.tw.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationItem;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.SystemGroupDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationItemDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class WorkstationService {
	@Autowired
	private WorkstationDao workstationDao;
	@Autowired
	private WorkstationItemDao itemDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private SystemGroupDao groupDao;
	@Autowired
	private WorkstationProgramDao workpDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<Workstation> workstations = new ArrayList<Workstation>();
		ArrayList<Workstation> workstations_son = new ArrayList<Workstation>();
		ArrayList<WorkstationItem> workstationItems = new ArrayList<WorkstationItem>();
		List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("wid").descending());
		String w_sg_name = null;
		String w_pb_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t("群組?", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t("UI_Group_ID", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_g_id", FFS.h_t("群組ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_i_id", FFS.h_t("項目ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_i_name", FFS.h_t("[料件SN]名稱", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_option", FFS.h_t("[料件SN]設定", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_only", FFS.h_t("[料件SN]唯一", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_length", FFS.h_t("[料件SN]長度", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_format", FFS.h_t("[料件SN]格式", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_must", FFS.h_t("[料件SN]必填", "100px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_c_name", FFS.h_t("工作站[條碼]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_pb_name", FFS.h_t("工作站[名稱]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_pb_cell", FFS.h_t("工作站[欄位]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_sg_id", FFS.h_t("使用群組[ID]", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_sg_name", FFS.h_t("使用群組[名稱]", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_replace", FFS.h_t("可重複登記?", "180px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_pi_check", FFS.h_t("檢查產品[規格]", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "w_pi_name", FFS.h_t("對應產品[規格]", "180px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_t_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_t_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "w_id", "W_ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "w_g_id", "W_群組ID"));

			workstationItems = itemDao.findAll();
			JSONArray a_vals1 = new JSONArray();
			workstationItems.forEach(s -> {
				if (s.getWiid() != 0) {
					a_vals1.put((new JSONObject()).put("value", s.getWipbvalue()).put("key", s.getWiid()));
				}
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", true, a_vals1, "w_i_id", "[工作站]綁定"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "w_i_name", "[料件SN]名稱"));

			a_val.put((new JSONObject()).put("value", "顯示(可手動-存檔)").put("key", 0));
			a_val.put((new JSONObject()).put("value", "不顯示(資訊欄-隱藏)").put("key", 1));
			a_val.put((new JSONObject()).put("value", "唯讀(不可手動-存檔)").put("key", 2));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "w_option", "[料件SN]設定"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制(可重複)").put("key", 0));
			a_val.put((new JSONObject()).put("value", "唯一值(不可重複)").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-2", false, a_val, "w_only", "[SN]唯一值"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "不限制請留空", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "w_length", "[SN]長度"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制").put("key", 0));
			a_val.put((new JSONObject()).put("value", "只能輸入(A-Z,0-9)").put("key", 1));
			a_val.put((new JSONObject()).put("value", "只能輸入(A-Z)").put("key", 2));
			a_val.put((new JSONObject()).put("value", "只能輸入(0-9)").put("key", 3));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "w_format", "[SN]格式"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制").put("key", 0));
			a_val.put((new JSONObject()).put("value", "必須填").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "w_must", "[SN]必填"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "五碼[AA000]", "", FFM.Wri.W_N, "col-md-2", true, n_val, "w_c_name", "工作站碼"));

			// sn關聯表-工作站
			a_val = new JSONArray();
			int j = 0;
			Method method;
			ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
			for (j = 0; j < 20; j++) {
				String m_name = "getPbwname" + String.format("%02d", j + 1);
				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = "pb_w_name" + String.format("%02d", j + 1);
					if (value != null && !value.equals("")) {
						a_val.put((new JSONObject()).put("value", value).put("key", name));
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", true, a_val, "w_pb_cell", "工作站名稱"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "[重複]過站").put("key", true));
			a_val.put((new JSONObject()).put("value", "[不重複]過站").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "true", "true", FFM.Wri.W_N, "col-md-2", true, a_val, "w_replace", "可重複?"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "不檢核").put("key", 0));
			a_val.put((new JSONObject()).put("value", "[手動輸入]檢核").put("key", 1));
			a_val.put((new JSONObject()).put("value", "[LOG輸入]檢核").put("key", 2));
			a_val.put((new JSONObject()).put("value", "[(LOG&手動)輸入]檢核").put("key", 3));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "w_pi_check", "[規格]檢查"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, " ", " ", FFM.Wri.W_Y, "col-md-2", false, n_val, "w_pi_name", "[規格]名稱"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序"));
			JSONArray a_vals2 = new JSONArray();
			systemGroup = groupDao.findAllBySysheader(true, PageRequest.of(0, 999));
			systemGroup.forEach(e -> {
				a_vals2.put((new JSONObject()).put("value", e.getSgname()).put("key", e.getSggid()));
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", true, a_vals2, "w_sg_id", "群組限制"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "false", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", "群組?"));
			bean.setCell_modify(obj_m);

			// 放入群主指定 [(key)](modify/Create/Delete) 格式
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "w_c_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_option", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_only", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_length", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_format", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_must", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_sort", "0"));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "w_pb_cell", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_i_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "w_sg_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "w_replace", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "sys_header", "true"));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_pi_check", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "w_pi_name", ""));

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "w_pb_name", "工作站[名稱]", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "w_sg_name", "可使用者[群組]Name", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			w_sg_name = body.getJSONObject("search").getString("w_sg_name");
			w_sg_name = w_sg_name.equals("") ? null : w_sg_name;
			w_pb_name = body.getJSONObject("search").getString("w_pb_name");
			w_pb_name = w_pb_name.equals("") ? null : w_pb_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		workstations = workstationDao.findAllByWorkstation(w_sg_name, w_pb_name, Integer.parseInt(status), true, null, page_r);
		List<Long> wgid = new ArrayList<Long>();
		for (Workstation obj : workstations) {
			String one = obj.getWgid().toString();
			wgid.add(Long.parseLong(one));
		}
		workstations_son = workstationDao.findAllByWorkstation(w_sg_name, w_pb_name, Integer.parseInt(status), false, wgid, null);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		workstations.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getWgid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_id", one.getWid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_g_id", one.getWgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_i_id", one.getWorkstationItem().getWiid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_i_name", one.getWorkstationItem().getWipbvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_option", one.getWoption());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_only", one.getWonly() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_length", one.getWlength() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_format", one.getWformat() + "");

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_must", one.getWmust() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_c_name", one.getWcname());  //"w_c_name", "工作站碼")
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_name", one.getWpbname()); //"w_pb_name", "工作站[名稱]"
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_cell", one.getWpbcell()); // "w_pb_cell", "工作站名稱"
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_sg_id", one.getWsgid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_sg_name", one.getWsgname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_replace", one.getWreplace());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pi_check", one.getWpicheck());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pi_name", one.getWpiname());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys.put(object_body);
			// 準備子類別容器
			object_bodys_son.put(one.getWgid() + "", new JSONArray());
		});
		bean.setBody(new JSONObject().put("search", object_bodys));

		// 子類別
		workstations_son.forEach(one -> {
			JSONObject object_son = new JSONObject();
			int ord = 0;
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getWgid());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_id", one.getWid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_g_id", one.getWgid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_i_id", one.getWorkstationItem().getWiid());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_i_name", one.getWorkstationItem().getWipbvalue());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_option", one.getWoption());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_only", one.getWonly());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_length", one.getWlength());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_format", one.getWformat());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_must", one.getWmust());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_c_name", one.getWcname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_name", one.getWpbname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_cell", one.getWpbcell());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_sg_id", one.getWsgid());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_sg_name", one.getWsgname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_replace", one.getWreplace());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pi_check", one.getWpicheck());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pi_name", one.getWpiname());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_user", one.getSyscuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys_son.getJSONArray(one.getWgid() + "").put(object_son);
		});
		bean.setBody(bean.getBody().put("search_son", object_bodys_son));

		// 是否為群組模式? type:[group/general] || 新增群組? createOnly:[all/general]
		bean.setBody_type(new JSONObject("{'type':'group','createOnly':'all'}"));
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 子/父類別
			Workstation sys_t = new Workstation();
			Workstation sys_t_f = new Workstation();
			JSONArray list = body.getJSONArray("create");
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查_工作站碼重複
				ArrayList<Workstation> wcname = workstationDao.findAllByWcname(data.getString("w_c_name"), PageRequest.of(0, 1));
				// 檢查_工作站 w_pb_cell 欄位重複
				ArrayList<Workstation> wpbcell = workstationDao.findAllByWpbcell(data.getString("w_pb_cell"), PageRequest.of(0, 1));
				// 檢查_父類別
				if ((wcname.size() != 0 && data.getBoolean("sys_header")) || //
						wpbcell.size() != 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("w_sg_id"));
					ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String w_pb_name = data.getString("w_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						Method method = body_one.getClass().getMethod(w_pb_name);
						String w_pb_value = (String) method.invoke(body_one);
						String w_pb_cell = data.getString("w_pb_cell");

						WorkstationItem sys_ti_f = new WorkstationItem();
						sys_ti_f.setWiid(0l);
						sys_t_f = new Workstation();
						sys_t_f.setWgid(workstationDao.getProduction_workstation_g_seq());
						sys_t_f.setWoption(0);
						sys_t_f.setWonly(0);
						sys_t_f.setWlength(0);
						sys_t_f.setWformat(0);
						sys_t_f.setWmust(0);
						sys_t_f.setWorkstationItem(sys_ti_f);
						sys_t_f.setWcname(data.getString("w_c_name"));
						sys_t_f.setWpbcell(w_pb_cell);
						sys_t_f.setWpbname(w_pb_value);
						sys_t_f.setWreplace(data.getBoolean("w_replace"));
						sys_t_f.setWsgid(systemGroup.get(0).getSggid());
						sys_t_f.setWsgname(systemGroup.get(0).getSgname());

						sys_t_f.setWpicheck(0);
						sys_t_f.setWpiname("");
						sys_t_f.setSysnote("");
						sys_t_f.setSyssort(data.getInt("sys_sort"));
						sys_t_f.setSysstatus(0);
						sys_t_f.setSysheader(true);
						sys_t_f.setSysmuser(user.getSuaccount());
						sys_t_f.setSyscuser(user.getSuaccount());
						workstationDao.save(sys_t_f);

					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

				} else {
					// 如果為子目錄 非群組
					if (!sys_t_f.getSysheader()) {
						sys_t_f = workstationDao.findAllByWgidAndSysheaderOrderBySyssortAsc(data.getLong("w_g_id"), true).get(0);
					}
					// 子類別
					WorkstationItem sys_ti = new WorkstationItem();
					sys_ti.setWiid(data.getLong("w_i_id"));
					sys_t = new Workstation();
					sys_t.setWoption(data.has("w_option") ? data.getInt("w_option") : 0);
					sys_t.setWonly(data.has("w_only") ? data.getInt("w_only") : 0);
					sys_t.setWlength(data.has("w_length") ? data.getInt("w_length") : 0);
					sys_t.setWformat(data.has("w_format") ? data.getInt("w_format") : 0);
					sys_t.setWmust(data.has("w_must") ? data.getInt("w_must") : 0);
					sys_t.setWgid(sys_t_f.getWgid());
					sys_t.setWcname(sys_t_f.getWcname());
					sys_t.setSysheader(false);
					sys_t.setWpbcell(sys_t_f.getWpbcell());
					sys_t.setWpbname(sys_t_f.getWpbname());
					sys_t.setWorkstationItem(sys_ti);
					sys_t.setWreplace(sys_t_f.getWreplace());
					sys_t.setWpicheck(data.has("w_pi_check") ? data.getInt("w_pi_check") : 0);
					sys_t.setWpiname(data.getString("w_pi_name"));

					sys_t.setSysnote("");
					sys_t.setSyssort(data.getInt("sys_sort"));
					sys_t.setSysstatus(0);
					sys_t.setWsgid(sys_t_f.getWsgid());
					sys_t.setWsgname(sys_t_f.getWsgname());
					sys_t.setSysmuser(user.getSuaccount());
					sys_t.setSyscuser(user.getSuaccount());
					workstationDao.save(sys_t);
				}
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 子/父類別
			Workstation sys_t = new Workstation();
			Workstation sys_t_f = new Workstation();
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查_工作站碼重複
				ArrayList<Workstation> wcname = workstationDao.findAllByWcname(data.getString("w_c_name"), PageRequest.of(0, 1));
				// 檢查_工作站 w_pb_cell 欄位重複
				ArrayList<Workstation> wpbcell = workstationDao.findAllByWpbcell(data.getString("w_pb_cell"), PageRequest.of(0, 1));
				// 檢查_父類別
				if ((wcname.size() != 0 && data.getBoolean("sys_header")) || //
						wpbcell.size() != 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("w_sg_id"));
					ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String w_pb_name = data.getString("w_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						Method method = body_one.getClass().getMethod(w_pb_name);
						String w_pb_value = (String) method.invoke(body_one);
						String w_pb_cell = data.getString("w_pb_cell");

						WorkstationItem sys_ti_f = new WorkstationItem();
						sys_ti_f.setWiid(0l);
						sys_t_f = new Workstation();
						sys_t_f.setWgid(workstationDao.getProduction_workstation_g_seq());
						sys_t_f.setWoption(0);
						sys_t_f.setWorkstationItem(sys_ti_f);
						sys_t_f.setWcname(data.getString("w_c_name"));
						sys_t_f.setWpbcell(w_pb_cell);
						sys_t_f.setWpbname(w_pb_value);
						sys_t_f.setWreplace(data.getBoolean("w_replace"));
						sys_t_f.setWpicheck(0);
						sys_t_f.setWpiname("");
						sys_t_f.setWsgid(systemGroup.get(0).getSggid());
						sys_t_f.setWsgname(systemGroup.get(0).getSgname());
						sys_t_f.setSysnote("");
						sys_t_f.setSyssort(data.getInt("sys_sort"));
						sys_t_f.setSysstatus(0);
						sys_t_f.setSysheader(true);
						sys_t_f.setSysmuser(user.getSuaccount());
						sys_t_f.setSyscuser(user.getSuaccount());
						workstationDao.save(sys_t_f);

					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

				} else {
					// 子類別
					WorkstationItem sys_ti = new WorkstationItem();
					sys_ti.setWiid(data.getLong("w_i_id"));
					sys_t = new Workstation();
					sys_t.setWoption(data.has("w_option") ? data.getInt("w_option") : 0);
					sys_t.setWonly(data.has("w_only") ? data.getInt("w_only") : 0);
					sys_t.setWlength(data.has("w_length") ? data.getInt("w_length") : 0);
					sys_t.setWformat(data.has("w_format") ? data.getInt("w_format") : 0);
					sys_t.setWmust(data.has("w_must") ? data.getInt("w_must") : 0);
					sys_t.setWgid(sys_t_f.getWgid());
					sys_t.setWcname(sys_t_f.getWcname());
					sys_t.setSysheader(false);
					sys_t.setWpbcell(sys_t_f.getWpbcell());
					sys_t.setWpbname(sys_t_f.getWpbname());
					sys_t.setWorkstationItem(sys_ti);
					sys_t.setWreplace(sys_t_f.getWreplace());
					sys_t.setWpicheck(data.has("w_pi_check") ? data.getInt("w_pi_check") : 0);
					sys_t.setWpiname(data.getString("w_pi_name"));

					sys_t.setSysnote("");
					sys_t.setSyssort(data.getInt("sys_sort"));
					sys_t.setSysstatus(0);
					sys_t.setWsgid(sys_t_f.getWsgid());
					sys_t.setWsgname(sys_t_f.getWsgname());
					sys_t.setSysmuser(user.getSuaccount());
					sys_t.setSyscuser(user.getSuaccount());
					workstationDao.save(sys_t);
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
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("modify");
			Workstation sys_t = new Workstation();
			String w_pb_value = "";
			String w_pb_cell = "";
			String wc_name = "";
			Long w_g_id = 0L;
			String w_c_name = "";
			Boolean w_replace = true;
			Method method;
			List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
			// 物件轉換
			ArrayList<Workstation> workstations_save = new ArrayList<Workstation>();
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				sys_t = new Workstation();
				sys_t.setWid(data.getLong("w_id"));

				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("w_sg_id"));
					ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String w_pb_name = data.getString("w_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						method = body_one.getClass().getMethod(w_pb_name);
						w_pb_value = (String) method.invoke(body_one);
						w_replace = data.getBoolean("w_replace");
						w_pb_cell = data.getString("w_pb_cell");
						w_g_id = data.getLong("w_g_id");
						w_c_name = data.getString("w_c_name");
					} catch (Exception e) {
						e.printStackTrace();
					}

					// 檢查_工作站碼重複
					ArrayList<Workstation> sys_t_g = workstationDao.findAllByWcnameAndWcnameNot(data.getString("w_c_name"), data.getString("w_c_name"),
							PageRequest.of(0, 1));
					// 檢查工作站 w_pb_cell 欄位重複
					if (sys_t_g.size() == 0) {
						sys_t_g = workstationDao.findAllByWpbcellAndWpbcellNot(data.getString("w_pb_cell"), data.getString("w_pb_cell"), PageRequest.of(0, 1));
					}
					// 如果重複則 退回
					if (sys_t_g.size() > 0) {
						return false;
					}
					WorkstationItem sys_ti_f = new WorkstationItem();
					sys_ti_f.setWiid(0l);
					sys_t.setWorkstationItem(sys_ti_f);
					sys_t.setWoption(0);
					sys_t.setWgid(w_g_id);
					sys_t.setWcname(w_c_name);
					sys_t.setWpbname(w_pb_value);
					sys_t.setWpbcell(w_pb_cell);
					sys_t.setWreplace(w_replace);
					sys_t.setWsgid(systemGroup.get(0).getSggid());
					sys_t.setWsgname(systemGroup.get(0).getSgname());
					sys_t.setSysmuser(user.getSuaccount());
					sys_t.setSyscuser(user.getSuaccount());
					sys_t.setSysheader(true);
					sys_t.setSyssort(0);
					workstationDao.save(sys_t);

					// 更新每一筆資料
					ArrayList<Workstation> workstations = workstationDao.findAllByWgidOrderBySyssortAsc(data.getLong("w_g_id"));

					for (Workstation w : workstations) {
						w.setWcname(data.getString("w_c_name"));
						w.setWpbname(w_pb_value);
						w.setWpbcell(w_pb_cell);
						w.setWsgid(systemGroup.get(0).getSggid());
						w.setWsgname(systemGroup.get(0).getSgname());
						w.setSysmuser(user.getSuaccount());
						w.setSyscuser(user.getSuaccount());
						workstations_save.add(w);
					}
					wc_name = data.getString("w_c_name");
				} else {
					// 子類別
					WorkstationItem sys_ti = new WorkstationItem();
					sys_ti.setWiid(data.getLong("w_i_id"));
					sys_t.setWcname(wc_name);
					sys_t.setWoption(data.has("w_option") ? data.getInt("w_option") : 0);
					sys_t.setWonly(data.has("w_only") ? data.getInt("w_only") : 0);
					sys_t.setWlength(data.has("w_length") ? data.getInt("w_length") : 0);
					sys_t.setWformat(data.has("w_format") ? data.getInt("w_format") : 0);
					sys_t.setWmust(data.has("w_must") ? data.getInt("w_must") : 0);
					sys_t.setWorkstationItem(sys_ti);
					sys_t.setSysheader(false);
					sys_t.setWgid(w_g_id);
					sys_t.setWcname(w_c_name);
					sys_t.setWpbname(w_pb_value);
					sys_t.setWpbcell(w_pb_cell);
					sys_t.setWreplace(w_replace);
					sys_t.setWpicheck(data.has("w_pi_check") ? data.getInt("w_pi_check") : 0);
					sys_t.setWpiname(data.getString("w_pi_name"));
					sys_t.setWsgid(systemGroup.get(0).getSggid());
					sys_t.setWsgname(systemGroup.get(0).getSgname());
					sys_t.setSysmuser(user.getSuaccount());
					sys_t.setSyscuser(user.getSuaccount());
					sys_t.setSyssort(data.getInt("sys_sort"));
					workstations_save.add(sys_t);
					// workstationDao.save(sys_t);
				}
			}
			workstationDao.saveAll(workstations_save);
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
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				Workstation sys_t = new Workstation();
				JSONObject data = (JSONObject) one;
				// 群組?
				// 如果程序正在使用-不能移除
				sys_t.setWid(data.getLong("w_id"));
				sys_t.setWgid(data.getLong("w_g_id"));
				if (data.getBoolean("sys_header")) {
					if (workpDao.findAllByWpwgid(data.getLong("w_g_id")).size() > 0) {
						return false;
					}
					workstationDao.deleteByWgid(sys_t.getWgid());
					check = true;
					continue;
				}
				workstationDao.deleteByWidAndSysheader(sys_t.getWid(), false);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}