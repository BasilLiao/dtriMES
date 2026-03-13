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
import dtri.com.tw.db.entity.PcbaBody;
import dtri.com.tw.db.entity.PcbaWorkstation;
import dtri.com.tw.db.entity.PcbaWorkstationItem;
import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.PcbaBodyDao;
import dtri.com.tw.db.pgsql.dao.PcbaWorkstationDao;
import dtri.com.tw.db.pgsql.dao.PcbaWorkstationItemDao;
import dtri.com.tw.db.pgsql.dao.PcbaWorkstationProgramDao;
import dtri.com.tw.db.pgsql.dao.SystemGroupDao;

import dtri.com.tw.tools.Fm_Time;

@Service
public class PcbaWorkstationService {
	@Autowired
	private PcbaWorkstationDao workstationDao;
	@Autowired
	private PcbaWorkstationItemDao itemDao;
	@Autowired
	private PcbaBodyDao bodyDao;
	@Autowired
	private SystemGroupDao groupDao;
	@Autowired
	private PcbaWorkstationProgramDao workpDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<PcbaWorkstation> workstations = new ArrayList<PcbaWorkstation>();
		ArrayList<PcbaWorkstation> workstations_son = new ArrayList<PcbaWorkstation>();
		ArrayList<PcbaWorkstationItem> workstationItems = new ArrayList<PcbaWorkstationItem>();
		List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("pwid").descending());
		String pw_sg_name = null;
		String pw_pb_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t("群組?", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t("UI_Group_ID", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_g_id", FFS.h_t("群組ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_i_id", FFS.h_t("項目ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_i_name", FFS.h_t("[料件SN]名稱", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_option", FFS.h_t("[料件SN]設定", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_only", FFS.h_t("[料件SN]唯一", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_length", FFS.h_t("[料件SN]長度", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_format", FFS.h_t("[料件SN]格式", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_must", FFS.h_t("[料件SN]必填", "100px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_c_name", FFS.h_t("工作站[條碼]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_pb_name", FFS.h_t("工作站[名稱]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_pb_cell", FFS.h_t("工作站[欄位]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_sg_id", FFS.h_t("使用群組[ID]", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_sg_name", FFS.h_t("使用群組[名稱]", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_replace", FFS.h_t("可重複登記?", "180px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_pi_check", FFS.h_t("檢查產品[規格]", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_pi_name", FFS.h_t("對應產品[規格]", "180px", FFM.Wri.W_Y));

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

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "pw_id", "W_ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "pw_g_id", "W_群組ID"));

			workstationItems = itemDao.findAll();
			JSONArray a_vals1 = new JSONArray();
			workstationItems.forEach(s -> {
				if (s.getPwiid() != 0) {
					a_vals1.put((new JSONObject()).put("value", s.getPwipbvalue()).put("key", s.getPwiid()));
				}
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", true, a_vals1, "pw_i_id", "[工作站]綁定"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pw_i_name", "[料件SN]名稱"));

			a_val.put((new JSONObject()).put("value", "顯示(可手動-存檔)").put("key", 0));
			a_val.put((new JSONObject()).put("value", "不顯示(資訊欄-隱藏)").put("key", 1));
			a_val.put((new JSONObject()).put("value", "唯讀(不可手動-存檔)").put("key", 2));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "pw_option", "[料件SN]設定"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制(可重複)").put("key", 0));
			a_val.put((new JSONObject()).put("value", "唯一值(不可重複)").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-2", false, a_val, "pw_only", "[SN]唯一值"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "不限制請留空", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "pw_length", "[SN]長度"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制").put("key", 0));
			a_val.put((new JSONObject()).put("value", "只能輸入(A-Z,0-9)").put("key", 1));
			a_val.put((new JSONObject()).put("value", "只能輸入(A-Z)").put("key", 2));
			a_val.put((new JSONObject()).put("value", "只能輸入(0-9)").put("key", 3));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "pw_format", "[SN]格式"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "無限制").put("key", 0));
			a_val.put((new JSONObject()).put("value", "必須填").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", false, a_val, "pw_must", "[SN]必填"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "五碼[AA000]", "", FFM.Wri.W_N, "col-md-2", true, n_val, "pw_c_name", "工作站碼"));

			// sn關聯表-工作站
			a_val = new JSONArray();
			int j = 0;
			Method method;
			PcbaBody body_one = bodyDao.findAllByPbid(0l).get(0);
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
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", true, a_val, "pw_pb_cell", "工作站名稱"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "[重複]過站").put("key", true));
			a_val.put((new JSONObject()).put("value", "[不重複]過站").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "true", "true", FFM.Wri.W_N, "col-md-2", true, a_val, "pw_replace", "可重複?"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "不檢核").put("key", 0));
			a_val.put((new JSONObject()).put("value", "[手動輸入]檢核").put("key", 1));
			a_val.put((new JSONObject()).put("value", "[LOG輸入]檢核").put("key", 2));
			a_val.put((new JSONObject()).put("value", "[(LOG&手動)輸入]檢核").put("key", 3));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "pw_pi_check", "[規格]檢查"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, " ", " ", FFM.Wri.W_Y, "col-md-2", false, n_val, "pw_pi_name", "[規格]名稱"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序"));
			JSONArray a_vals2 = new JSONArray();
			systemGroup = groupDao.findAllBySysheader(true, PageRequest.of(0, 999));
			systemGroup.forEach(e -> {
				a_vals2.put((new JSONObject()).put("value", e.getSgname()).put("key", e.getSggid()));
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", true, a_vals2, "pw_sg_id", "群組限制"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "false", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", "群組?"));
			bean.setCell_modify(obj_m);

			// 放入群主指定 [(key)](modify/Create/Delete) 格式
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "pw_c_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_option", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_only", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_length", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_format", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_must", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_sort", "0"));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "pw_pb_cell", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_i_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "pw_sg_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "pw_replace", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "sys_header", "true"));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_pi_check", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pw_pi_name", ""));

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pw_pb_name", "工作站[名稱]", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pw_sg_name", "可使用者[群組]Name", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			pw_sg_name = body.getJSONObject("search").getString("pw_sg_name");
			pw_sg_name = pw_sg_name.equals("") ? null : pw_sg_name;
			pw_pb_name = body.getJSONObject("search").getString("pw_pb_name");
			pw_pb_name = pw_pb_name.equals("") ? null : pw_pb_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		workstations = workstationDao.findAllByPcbaWorkstation(pw_sg_name, pw_pb_name, Integer.parseInt(status), true, null, page_r);
		List<Long> wgid = new ArrayList<Long>();
		for (PcbaWorkstation obj : workstations) {
			String one = obj.getPwgid().toString();
			wgid.add(Long.parseLong(one));
		}
		workstations_son = workstationDao.findAllByPcbaWorkstation(pw_sg_name, pw_pb_name, Integer.parseInt(status), false, wgid, null);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		workstations.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPwgid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_id", one.getPwid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_g_id", one.getPwgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_i_id", one.getPcbaWorkstationItem().getPwiid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_i_name", one.getPcbaWorkstationItem().getPwipbvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_option", one.getPwoption());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_only", one.getPwonly() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_length", one.getPwlength() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_format", one.getPwformat() + "");

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_must", one.getPwmust() + "");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_c_name", one.getPwcname());  //"w_c_name", "工作站碼")
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_name", one.getPwpbname()); //"w_pb_name", "工作站[名稱]"
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_cell", one.getPwpbcell()); // "w_pb_cell", "工作站名稱"
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_sg_id", one.getPwsgid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_sg_name", one.getPwsgname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_replace", one.getPwreplace());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pi_check", one.getPwpicheck());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pi_name", one.getPwpiname());

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
			object_bodys_son.put(one.getPwgid() + "", new JSONArray());
		});
		bean.setBody(new JSONObject().put("search", object_bodys));

		// 子類別
		workstations_son.forEach(one -> {
			JSONObject object_son = new JSONObject();
			int ord = 0;
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPwgid());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_id", one.getPwid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_g_id", one.getPwgid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_i_id", one.getPcbaWorkstationItem().getPwiid());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_i_name", one.getPcbaWorkstationItem().getPwipbvalue());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_option", one.getPwoption());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_only", one.getPwonly());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_length", one.getPwlength());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_format", one.getPwformat());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_must", one.getPwmust());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_c_name", one.getPwcname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_name", one.getPwpbname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_cell", one.getPwpbcell());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_sg_id", one.getPwsgid());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_sg_name", one.getPwsgname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_replace", one.getPwreplace());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pi_check", one.getPwpicheck());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pi_name", one.getPwpiname());

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_t_user", one.getSyscuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys_son.getJSONArray(one.getPwgid() + "").put(object_son);
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
			PcbaWorkstation sys_t = new PcbaWorkstation();
			PcbaWorkstation sys_t_f = new PcbaWorkstation();
			JSONArray list = body.getJSONArray("create");
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查_工作站碼重複
				ArrayList<PcbaWorkstation> pwcname = workstationDao.findAllByPwcname(data.getString("pw_c_name"), PageRequest.of(0, 1));
				// 檢查_工作站 w_pb_cell 欄位重複
				ArrayList<PcbaWorkstation> pwpbcell = workstationDao.findAllByPwpbcell(data.getString("pw_pb_cell"), PageRequest.of(0, 1));
				// 檢查_父類別
				if ((pwcname.size() != 0 && data.getBoolean("sys_header")) || //
						pwpbcell.size() != 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("pw_sg_id"));
					PcbaBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String pw_pb_name = data.getString("pw_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						Method method = body_one.getClass().getMethod(pw_pb_name);
						String pw_pb_value = (String) method.invoke(body_one);
						String pw_pb_cell = data.getString("pw_pb_cell");

						PcbaWorkstationItem sys_ti_f = new PcbaWorkstationItem();
						sys_ti_f.setPwiid(0l);
						sys_t_f = new PcbaWorkstation();
						sys_t_f.setPwgid(workstationDao.getPcba_workstation_g_seq());
						sys_t_f.setPwoption(0);
						sys_t_f.setPwonly(0);
						sys_t_f.setPwlength(0);
						sys_t_f.setPwformat(0);
						sys_t_f.setPwmust(0);
						sys_t_f.setPcbaWorkstationItem(sys_ti_f); //存入一張表單 PcbaWorkstationItem sys_ti_f
						sys_t_f.setPwcname(data.getString("pw_c_name"));
						sys_t_f.setPwpbcell(pw_pb_cell);
						sys_t_f.setPwpbname(pw_pb_value);
						sys_t_f.setPwreplace(data.getBoolean("pw_replace"));
						sys_t_f.setPwsgid(systemGroup.get(0).getSggid());
						sys_t_f.setPwsgname(systemGroup.get(0).getSgname());

						sys_t_f.setPwpicheck(0);
						sys_t_f.setPwpiname("");
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
						sys_t_f = workstationDao.findAllByPwgidAndSysheaderOrderBySyssortAsc(data.getLong("pw_g_id"), true).get(0);
					}
					// 子類別
					PcbaWorkstationItem sys_ti = new PcbaWorkstationItem();
					sys_ti.setPwiid(data.getLong("pw_i_id"));
					sys_t = new PcbaWorkstation();
					sys_t.setPwoption(data.has("pw_option") ? data.getInt("pw_option") : 0);
					sys_t.setPwonly(data.has("pw_only") ? data.getInt("pw_only") : 0);
					sys_t.setPwlength(data.has("pw_length") ? data.getInt("pw_length") : 0);
					sys_t.setPwformat(data.has("pw_format") ? data.getInt("pw_format") : 0);
					sys_t.setPwmust(data.has("pw_must") ? data.getInt("pw_must") : 0);
					sys_t.setPwgid(sys_t_f.getPwgid());
					sys_t.setPwcname(sys_t_f.getPwcname());
					sys_t.setSysheader(false);
					sys_t.setPwpbcell(sys_t_f.getPwpbcell());
					sys_t.setPwpbname(sys_t_f.getPwpbname());
					sys_t.setPcbaWorkstationItem(sys_ti);
					sys_t.setPwreplace(sys_t_f.getPwreplace());
					sys_t.setPwpicheck(data.has("pw_pi_check") ? data.getInt("pw_pi_check") : 0);
					sys_t.setPwpiname(data.getString("pw_pi_name"));

					sys_t.setSysnote("");
					sys_t.setSyssort(data.getInt("sys_sort"));
					sys_t.setSysstatus(0);
					sys_t.setPwsgid(sys_t_f.getPwsgid());
					sys_t.setPwsgname(sys_t_f.getPwsgname());
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
			PcbaWorkstation sys_t = new PcbaWorkstation();
			PcbaWorkstation sys_t_f = new PcbaWorkstation();
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查_工作站碼重複
				ArrayList<PcbaWorkstation> pwcname = workstationDao.findAllByPwcname(data.getString("pw_c_name"), PageRequest.of(0, 1));
				// 檢查_工作站 w_pb_cell 欄位重複
				ArrayList<PcbaWorkstation> pwpbcell = workstationDao.findAllByPwpbcell(data.getString("pw_pb_cell"), PageRequest.of(0, 1));
				// 檢查_父類別
				if ((pwcname.size() != 0 && data.getBoolean("sys_header")) || //
						pwpbcell.size() != 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("pw_sg_id"));
					PcbaBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String pw_pb_name = data.getString("pw_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						Method method = body_one.getClass().getMethod(pw_pb_name);
						String pw_pb_value = (String) method.invoke(body_one);
						String pw_pb_cell = data.getString("pw_pb_cell");

						PcbaWorkstationItem sys_ti_f = new PcbaWorkstationItem();
						sys_ti_f.setPwiid(0l);
						sys_t_f = new PcbaWorkstation();
						sys_t_f.setPwgid(workstationDao.getPcba_workstation_g_seq());
						sys_t_f.setPwoption(0);
						sys_t_f.setPcbaWorkstationItem(sys_ti_f);
						sys_t_f.setPwcname(data.getString("w_c_name"));
						sys_t_f.setPwpbcell(pw_pb_cell);
						sys_t_f.setPwpbname(pw_pb_value);
						sys_t_f.setPwreplace(data.getBoolean("w_replace"));
						sys_t_f.setPwpicheck(0);
						sys_t_f.setPwpiname("");
						sys_t_f.setPwsgid(systemGroup.get(0).getSggid());
						sys_t_f.setPwsgname(systemGroup.get(0).getSgname());
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
					PcbaWorkstationItem sys_ti = new PcbaWorkstationItem();
					sys_ti.setPwiid(data.getLong("pw_i_id"));
					sys_t = new PcbaWorkstation();
					sys_t.setPwoption(data.has("pw_option") ? data.getInt("pw_option") : 0);
					sys_t.setPwonly(data.has("pw_only") ? data.getInt("pw_only") : 0);
					sys_t.setPwlength(data.has("pw_length") ? data.getInt("pw_length") : 0);
					sys_t.setPwformat(data.has("pw_format") ? data.getInt("pw_format") : 0);
					sys_t.setPwmust(data.has("pw_must") ? data.getInt("pw_must") : 0);
					sys_t.setPwgid(sys_t_f.getPwgid());
					sys_t.setPwcname(sys_t_f.getPwcname());
					sys_t.setSysheader(false);
					sys_t.setPwpbcell(sys_t_f.getPwpbcell());
					sys_t.setPwpbname(sys_t_f.getPwpbname());
					sys_t.setPcbaWorkstationItem(sys_ti);
					sys_t.setPwreplace(sys_t_f.getPwreplace());
					sys_t.setPwpicheck(data.has("pw_pi_check") ? data.getInt("pw_pi_check") : 0);
					sys_t.setPwpiname(data.getString("pw_pi_name"));

					sys_t.setSysnote("");
					sys_t.setSyssort(data.getInt("sys_sort"));
					sys_t.setSysstatus(0);
					sys_t.setPwsgid(sys_t_f.getPwsgid());
					sys_t.setPwsgname(sys_t_f.getPwsgname());
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
			PcbaWorkstation sys_t = new PcbaWorkstation();
			String pw_pb_value = "";
			String pw_pb_cell = "";
			String pwc_name = "";
			Long pw_g_id = 0L;
			String pw_c_name = "";
			Boolean pw_replace = true;
			Method method;
			List<SystemGroup> systemGroup = new ArrayList<SystemGroup>();
			// 物件轉換
			ArrayList<PcbaWorkstation> workstations_save = new ArrayList<PcbaWorkstation>();
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				sys_t = new PcbaWorkstation();
				sys_t.setPwid(data.getLong("pw_id"));

				// 父類別
				if (data.getBoolean("sys_header")) {
					// 使用者群組
					systemGroup = groupDao.findBySggidOrderBySggid(data.getLong("pw_sg_id"));
					PcbaBody body_one = bodyDao.findAllByPbid(0l).get(0);
					// 取得工作欄位 位置
					String pw_pb_name = data.getString("pw_pb_cell").replace("pb_w_name", "getPbwname");
					try {
						method = body_one.getClass().getMethod(pw_pb_name);
						pw_pb_value = (String) method.invoke(body_one);
						pw_replace = data.getBoolean("pw_replace");
						pw_pb_cell = data.getString("pw_pb_cell");
						pw_g_id = data.getLong("pw_g_id");
						pw_c_name = data.getString("pw_c_name");
					} catch (Exception e) {
						e.printStackTrace();
					}

					// 檢查_工作站碼重複
					ArrayList<PcbaWorkstation> sys_t_g = workstationDao.findAllByPwcnameAndPwcnameNot(data.getString("pw_c_name"), data.getString("pw_c_name"),
							PageRequest.of(0, 1));
					// 檢查工作站 w_pb_cell 欄位重複
					if (sys_t_g.size() == 0) {
						sys_t_g = workstationDao.findAllByPwpbcellAndPwpbcellNot(data.getString("pw_pb_cell"), data.getString("pw_pb_cell"), PageRequest.of(0, 1));
					}
					// 如果重複則 退回
					if (sys_t_g.size() > 0) {
						return false;
					}
					PcbaWorkstationItem sys_ti_f = new PcbaWorkstationItem();
					sys_ti_f.setPwiid(0l);
					sys_t.setPcbaWorkstationItem(sys_ti_f);
					sys_t.setPwoption(0);
					sys_t.setPwgid(pw_g_id);
					sys_t.setPwcname(pw_c_name);
					sys_t.setPwpbname(pw_pb_value);
					sys_t.setPwpbcell(pw_pb_cell);
					sys_t.setPwreplace(pw_replace);
					sys_t.setPwsgid(systemGroup.get(0).getSggid());
					sys_t.setPwsgname(systemGroup.get(0).getSgname());
					sys_t.setSysmuser(user.getSuaccount());
					sys_t.setSyscuser(user.getSuaccount());
					sys_t.setSysheader(true);
					sys_t.setSyssort(0);
					workstationDao.save(sys_t);

					// 更新每一筆資料
					ArrayList<PcbaWorkstation> workstations = workstationDao.findAllByPwgidOrderBySyssortAsc(data.getLong("pw_g_id"));

					for (PcbaWorkstation w : workstations) {
						w.setPwcname(data.getString("pw_c_name"));
						w.setPwpbname(pw_pb_value);
						w.setPwpbcell(pw_pb_cell);
						w.setPwsgid(systemGroup.get(0).getSggid());
						w.setPwsgname(systemGroup.get(0).getSgname());
						w.setSysmuser(user.getSuaccount());
						w.setSyscuser(user.getSuaccount());
						workstations_save.add(w);
					}
					pwc_name = data.getString("pw_c_name");
				} else {
					// 子類別
					PcbaWorkstationItem sys_ti = new PcbaWorkstationItem();
					sys_ti.setPwiid(data.getLong("pw_i_id"));
					sys_t.setPwcname(pwc_name);
					sys_t.setPwoption(data.has("pw_option") ? data.getInt("pw_option") : 0);
					sys_t.setPwonly(data.has("pw_only") ? data.getInt("pw_only") : 0);
					sys_t.setPwlength(data.has("pw_length") ? data.getInt("pw_length") : 0);
					sys_t.setPwformat(data.has("pw_format") ? data.getInt("pw_format") : 0);
					sys_t.setPwmust(data.has("pw_must") ? data.getInt("pw_must") : 0);
					sys_t.setPcbaWorkstationItem(sys_ti);
					sys_t.setSysheader(false);
					sys_t.setPwgid(pw_g_id);
					sys_t.setPwcname(pw_c_name);
					sys_t.setPwpbname(pw_pb_value);
					sys_t.setPwpbcell(pw_pb_cell);
					sys_t.setPwreplace(pw_replace);
					sys_t.setPwpicheck(data.has("pw_pi_check") ? data.getInt("pw_pi_check") : 0);
					sys_t.setPwpiname(data.getString("pw_pi_name"));
					sys_t.setPwsgid(systemGroup.get(0).getSggid());
					sys_t.setPwsgname(systemGroup.get(0).getSgname());
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
				PcbaWorkstation sys_t = new PcbaWorkstation();
				JSONObject data = (JSONObject) one;
				// 群組?
				// 如果程序正在使用-不能移除
				sys_t.setPwid(data.getLong("pw_id"));
				sys_t.setPwgid(data.getLong("pw_g_id"));
				if (data.getBoolean("sys_header")) {
					if (workpDao.findAllByPwpwgid(data.getLong("pw_g_id")).size() > 0) {
						return false;
					}
					workstationDao.deleteByPwgid(sys_t.getPwgid());
					check = true;
					continue;
				}
				workstationDao.deleteByPwidAndSysheader(sys_t.getPwid(), false);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}