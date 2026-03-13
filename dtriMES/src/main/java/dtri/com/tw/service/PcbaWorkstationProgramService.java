package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.PcbaWorkstation;
import dtri.com.tw.db.entity.PcbaWorkstationProgram;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.PcbaWorkstationDao;
import dtri.com.tw.db.pgsql.dao.PcbaWorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class PcbaWorkstationProgramService {
	@Autowired
	private PcbaWorkstationProgramDao programDao;
	@Autowired
	private PcbaWorkstationDao workstationDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<PcbaWorkstationProgram> workstationPrograms = new ArrayList<PcbaWorkstationProgram>(); //設定-工作站流程
		ArrayList<PcbaWorkstationProgram> workstationPrograms_son = new ArrayList<PcbaWorkstationProgram>(); //設定-工作站流程
		ArrayList<PcbaWorkstation> workstations = new ArrayList<PcbaWorkstation>(); //設定-工作站檢核
		JSONArray a_vals = new JSONArray();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("pwpid").descending());
		String pwp_name = null;
		String pwp_c_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t("群組", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t("UI_Group_ID", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_g_id", FFS.h_t("群組[ID]", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_name", FFS.h_t("流程序[名稱]", "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_c_name", FFS.h_t("流程序[代碼]", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_c_n_yield", FFS.h_t("測試計數 工作站[代碼]", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pwp_w_g_id", FFS.h_t("工作站[ID]", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_pb_name", FFS.h_t("工作站[名稱]", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pw_c_name", FFS.h_t("工作站[代碼]", "150px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "280px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pwp_id", "ID"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pwp_g_id", "群組ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "pwp_name", "流程序[名稱]"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "pwp_c_name", "流程序[代碼]"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "pwp_c_n_yield", "測試計數 工作站[代碼]"));
			// 查詢工作站代表
			workstations = workstationDao.findAllBySysheaderOrderByPwcnameAsc(true, PageRequest.of(0, 999));
			workstations.forEach(w -> {
				if (w.getPwgid() != 0)
					a_vals.put((new JSONObject()).put("value", w.getPwpbname()).put("key", w.getPwgid()));
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-2", true, a_vals, "pwp_w_g_id", "工作站[ID]"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", false, n_val, "pw_pb_name", "工作站[名稱]"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, " ", " ", FFM.Wri.W_N, "col-md-2", false, n_val, "pw_c_name", "工作站[代碼]"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, " ", " ", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", "備註"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", "版本"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", "狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "false", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", "群組"));
			bean.setCell_modify(obj_m);

			// 放入群主指定 [(key)](modify/Create/Delete) 格式
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "sys_sort", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "pwp_w_g_id", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "pwp_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "pwp_c_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "pwp_c_n_yield", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "pw_c_name", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "sys_sort", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "sys_ver", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "sys_status", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_header", "true"));

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pwp_c_name", "工作程序代號", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pwp_name", "工作程序名稱", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			pwp_name = body.getJSONObject("search").getString("pwp_name"); //工作站程序(名稱)
			pwp_name = pwp_name.equals("") ? null : pwp_name;
			pwp_c_name = body.getJSONObject("search").getString("pwp_c_name"); //工作程序代號
			pwp_c_name = pwp_c_name.equals("") ? null : pwp_c_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		// 查詢一部分   找每一群組的true代表
		workstationPrograms = programDao.findAllByProgram(pwp_name, pwp_c_name, Integer.parseInt(status), true, page_r);
		List<Long> pwpgid = new ArrayList<Long>();
		for (PcbaWorkstationProgram obj : workstationPrograms) {
			String one = obj.getPwpgid().toString();
			pwpgid.add(Long.parseLong(one));
		}
		// 查詢一部分-son
		workstationPrograms_son = programDao.findAllByProgram(pwp_name, pwp_c_name, pwpgid, false);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		
		workstationPrograms.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPwpgid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_id", one.getPwpid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_g_id", one.getPwpgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_name", one.getPwpname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_c_name", one.getPwpcname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_c_n_yield", one.getPwpcnyield() == null ? "" : one.getPwpcnyield());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_w_g_id", one.getPwpwgid());
			//檢核表
			PcbaWorkstation work = workstationDao.findAllByPwgidOrderBySyssortAsc(one.getPwpwgid()).get(0);
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_name", work.getPwpbname());  //
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_c_name", work.getPwcname());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys_son.put(one.getPwpgid() + "", new JSONArray());
			object_bodys.put(object_body);
			// 準備子類別容器
		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		// son
		workstationPrograms_son.forEach(one -> {
			JSONObject object_son = new JSONObject();
			int ord = 0;
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPwpgid());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_id", one.getPwpid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_g_id", one.getPwpgid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_name", one.getPwpname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_c_name", one.getPwpcname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_c_n_yield", one.getPwpcnyield() == null ? "" : one.getPwpcnyield());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pwp_w_g_id", one.getPwpwgid());
			//檢核表
			PcbaWorkstation work = workstationDao.findAllByPwgidOrderBySyssortAsc(one.getPwpwgid()).get(0);
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_pb_name", work.getPwpbname()); //工作站名稱
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pw_c_name", work.getPwcname()); //1維碼(登入工作時使用)

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_bodys_son.getJSONArray(one.getPwpgid() + "").put(object_son);
		});
		bean.setBody(bean.getBody().put("search_son", object_bodys_son));

		// 是否為群組模式? type:[group/general] || 新增時群組? createOnly:[all/general]
		bean.setBody_type(new JSONObject("{'type':'group','createOnly':'all'}"));
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		JSONArray list = body.getJSONArray("create");
		ArrayList<PcbaWorkstationProgram> workstationPrograms = new ArrayList<PcbaWorkstationProgram>();
		PcbaWorkstationProgram sys_wp_f = new PcbaWorkstationProgram();
		PcbaWorkstationProgram sys_wp = new PcbaWorkstationProgram();
		try {
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查名稱重複?     // 查詢全部 pwp_name or pwp_c_name
				workstationPrograms = programDao.findAllByPwpcnameOrPwpname(data.getString("pwp_c_name"), data.getString("pwp_name"));
				if (workstationPrograms.size() > 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				if (data.getBoolean("sys_header")) {
					// 父
					sys_wp_f = new PcbaWorkstationProgram();
					Long Pwpgid = programDao.getPcba_workstation_program_g_seq(); // 取得G_ID PCBA_WORKSTATION_PROGRAM_G_SEQ
					sys_wp_f.setPwpgid(Pwpgid);
					sys_wp_f.setPwpcnyield(data.getString("pwp_c_n_yield"));
					sys_wp_f.setPwpcname(data.getString("pwp_c_name"));
					sys_wp_f.setPwpname(data.getString("pwp_name"));
					sys_wp_f.setPwpwgid(0l);
					sys_wp_f.setSyssort(0);
					sys_wp_f.setSysnote("");
					sys_wp_f.setSysstatus(0);
					sys_wp_f.setSysheader(true);
					sys_wp_f.setSysmuser(user.getSuaccount());
					sys_wp_f.setSyscuser(user.getSuaccount());
					programDao.save(sys_wp_f);
				} else {
					// 如果為子目錄 非群組
					if (!sys_wp_f.getSysheader()) {
						// 查詢全部 By Group 排除代表
						sys_wp_f = programDao.findAllByPwpgidAndSysheaderOrderBySyssortAsc(data.getLong("pwp_g_id"), true).get(0);
					}
					// 子
					sys_wp = new PcbaWorkstationProgram();
					sys_wp.setPwpcnyield(sys_wp_f.getPwpcnyield());
					sys_wp.setPwpcname(sys_wp_f.getPwpcname());
					sys_wp.setPwpname(sys_wp_f.getPwpname());
					sys_wp.setPwpgid(sys_wp_f.getPwpgid());
					sys_wp.setSysnote("");
					sys_wp.setSyssort(data.getInt("sys_sort"));
					sys_wp.setSysstatus(data.getInt("sys_status"));
					sys_wp.setPwpwgid(data.getLong("pwp_w_g_id"));
					sys_wp.setSysheader(false);
					sys_wp.setSysmuser(user.getSuaccount());
					sys_wp.setSyscuser(user.getSuaccount());
					programDao.save(sys_wp);
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
		JSONArray list = body.getJSONArray("save_as");
		ArrayList<PcbaWorkstationProgram> workstationPrograms = new ArrayList<PcbaWorkstationProgram>();
		PcbaWorkstationProgram sys_wp_f = new PcbaWorkstationProgram();
		PcbaWorkstationProgram sys_wp = new PcbaWorkstationProgram();
		try {
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查名稱重複?
				workstationPrograms = programDao.findAllByPwpcnameOrPwpname(data.getString("pwp_c_name"), data.getString("pwp_name"));
				if (workstationPrograms.size() > 0 && data.getBoolean("sys_header")) {
					return false;
				}

				// Step3.資料分類建置
				if (data.getBoolean("sys_header")) {
					// 父
					sys_wp_f = new PcbaWorkstationProgram();
					Long pwpgid = programDao.getPcba_workstation_program_g_seq();  // 取得G_ID PCBA_WORKSTATION_PROGRAM_G_SEQ

					sys_wp_f.setPwpgid(pwpgid);
					sys_wp_f.setPwpcnyield(data.getString("pwp_c_n_yield"));
					sys_wp_f.setPwpcname(data.getString("pwp_c_name"));
					sys_wp_f.setPwpname(data.getString("pwp_name"));
					sys_wp_f.setPwpwgid(0l);
					sys_wp_f.setSyssort(0);
					sys_wp_f.setSysnote("");
					sys_wp_f.setSysstatus(0);
					sys_wp_f.setSysheader(true);
					sys_wp_f.setSysmuser(user.getSuaccount());
					sys_wp_f.setSyscuser(user.getSuaccount());
					programDao.save(sys_wp_f);
				} else {
					// 子
					sys_wp = new PcbaWorkstationProgram();
					sys_wp.setPwpcnyield(sys_wp_f.getPwpcnyield());
					sys_wp.setPwpcname(sys_wp_f.getPwpcname());
					sys_wp.setPwpname(sys_wp_f.getPwpname());
					sys_wp.setPwpgid(sys_wp_f.getPwpgid());
					sys_wp.setSysnote("");
					sys_wp.setSyssort(data.getInt("sys_sort"));
					sys_wp.setSysstatus(data.getInt("sys_status"));
					sys_wp.setPwpwgid(data.getLong("pwp_w_g_id"));
					sys_wp.setSysheader(false);
					sys_wp.setSysmuser(user.getSuaccount());
					sys_wp.setSyscuser(user.getSuaccount());
					programDao.save(sys_wp);
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
			PcbaWorkstationProgram sys_p_f = new PcbaWorkstationProgram();
			PcbaWorkstationProgram sys_p = new PcbaWorkstationProgram();

			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				// 父類別
				if (data.getBoolean("sys_header")) {
					// 檢查名稱重複?
					if (programDao.findAllByPwpcnameAndPwpcnameNot(data.getString("pwp_c_name"), data.getString("pwp_c_name")).size() > 0 || // 查詢全部 wp_c_name + 排除自己
							programDao.findAllByPwpnameAndPwpnameNot(data.getString("pwp_name"), data.getString("pwp_name")).size() > 0) { // 查詢全部 wp_name + 排除自己
						return false;
					}
					sys_p_f = new PcbaWorkstationProgram();
					sys_p_f.setPwpid(data.getLong("pwp_id"));
					sys_p_f.setPwpgid(data.getLong("pwp_g_id"));
					sys_p_f.setPwpname(data.getString("pwp_name"));
					sys_p_f.setPwpcname(data.getString("pwp_c_name"));
					sys_p_f.setPwpcnyield(data.getString("pwp_c_n_yield"));
					sys_p_f.setPwpwgid(0l); // ０列
					sys_p_f.setSysnote("");
					sys_p_f.setSyssort(0);
					sys_p_f.setSysstatus(data.getInt("sys_status"));
					sys_p_f.setSysmuser(user.getSuaccount());
					sys_p_f.setSysmdate(new Date());
					sys_p_f.setSysheader(true);
					programDao.save(sys_p_f);

					// 更新子類別
					ArrayList<PcbaWorkstationProgram> sys_p_s = programDao.findAllByPwpgidOrderBySyssortAsc(data.getLong("pwp_g_id")); //// 查詢全部 By Group
					sys_p_s.forEach(wp -> {
						wp.setPwpname(data.getString("pwp_name"));
						wp.setPwpcname(data.getString("pwp_c_name"));
						wp.setPwpcnyield(data.getString("pwp_c_n_yield"));
					});
					programDao.saveAll(sys_p_s);
				} else {
					sys_p = new PcbaWorkstationProgram();
					sys_p.setPwpid(data.getLong("pwp_id"));
					sys_p.setPwpgid(sys_p_f.getPwpgid());
					sys_p.setPwpname(sys_p_f.getPwpname());
					sys_p.setPwpcname(sys_p_f.getPwpcname());
					sys_p.setPwpcnyield(sys_p_f.getPwpcnyield());
					sys_p.setPwpwgid(data.getLong("pwp_w_g_id"));
					sys_p.setSysnote("");
					sys_p.setSyssort(data.getInt("sys_sort"));
					sys_p.setSysstatus(data.getInt("sys_status"));
					sys_p.setSysmuser(user.getSuaccount());
					sys_p.setSysmdate(new Date());
					programDao.save(sys_p);
				}
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
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				PcbaWorkstationProgram sys_p = new PcbaWorkstationProgram();
				JSONObject data = (JSONObject) one;
				// 群組移除
				if (data.getBoolean("sys_header")) {
					programDao.deleteByPwpgid(data.getLong("pwp_g_id"));
					continue;
				}
				sys_p.setPwpid(data.getLong("pwp_id"));
				programDao.deleteByPwpidAndSysheader(sys_p.getPwpid(), false);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
