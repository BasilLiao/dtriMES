package dtri.com.tw.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.ProductionTest;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.ProductionTestDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductionTestService {
	@Autowired
	private ProductionTestDao testDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<ProductionTest> tests = new ArrayList<ProductionTest>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by(Sort.Direction.DESC, "sysmdate").and(Sort.by(Sort.Direction.ASC, "ptpbbsn")));
		// 查詢
		String search_pt_pb_b_sn = null, search_pt_pr_id = null, //
				search_pt_pr_model = null, search_pt_pr_bom_id = null, //
				search_pt_pr_b_item = null, search_pt_pr_s_item = null;

		// 功能-名稱編譯
		String pt_id = "ID", pt_pb_g_id = "關聯KEY", //
				pt_pr_id = "製令單", pt_pr_model = "產品型號", //
				pt_pr_bom_id = "產品BOM", pt_pb_b_sn = "產品SN號", //
				pt_pr_b_item = "產品規格", pt_pr_s_item = "產品軟體", //
				pt_l_dt = "測試LOG時間", pt_l_path = "測試LOG位置", //
				pt_l_size = "測試LOG大小", pt_l_text = "測試LOG內容";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", //
				sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", sys_sort = "排序", //
				sys_ver = "版本", sys_status = "狀態" /* , sys_header = "群組", ui_group_id = "UI_Group_ID" */;

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_id", FFS.h_t(pt_id, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pb_g_id", FFS.h_t(pt_pb_g_id, "100px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pr_id", FFS.h_t(pt_pr_id, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pr_model", FFS.h_t(pt_pr_model, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pr_bom_id", FFS.h_t(pt_pr_bom_id, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pb_b_sn", FFS.h_t(pt_pb_b_sn, "180px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pr_b_item", FFS.h_t(pt_pr_b_item, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_pr_s_item", FFS.h_t(pt_pr_s_item, "150px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_l_dt", FFS.h_t(pt_l_dt, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_l_size", FFS.h_t(pt_l_size, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_l_path", FFS.h_t(pt_l_path, "1000px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pt_l_text", FFS.h_t(pt_l_text, "250px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "300px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t(sys_sort, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t(sys_ver, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_N));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pt_id", pt_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pt_pb_g_id", pt_pb_g_id));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pt_pr_id", pt_pr_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pt_pr_model", pt_pr_model));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pt_pr_bom_id", pt_pr_bom_id));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pt_pb_b_sn", pt_pb_b_sn));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pt_l_size", pt_l_size));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pt_l_dt", pt_l_dt));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pt_l_path", pt_l_path));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pt_l_text", pt_l_text));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pt_pr_b_item", pt_pr_b_item));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pt_pr_s_item", pt_pr_s_item));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_date", sys_c_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_user", sys_c_user));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_sort", sys_sort));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", sys_ver));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", sys_status));
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pb_b_sn", pt_pb_b_sn, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pr_id", pt_pr_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pr_model", pt_pr_model, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pr_bom_id", pt_pr_bom_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pr_b_item", pt_pr_b_item, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pt_pr_s_item", pt_pr_s_item, n_val));
			bean.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			search_pt_pb_b_sn = body.getJSONObject("search").getString("pt_pb_b_sn");
			search_pt_pb_b_sn = search_pt_pb_b_sn.equals("") ? null : search_pt_pb_b_sn;
			search_pt_pr_id = body.getJSONObject("search").getString("pt_pr_id");
			search_pt_pr_id = search_pt_pr_id.equals("") ? null : search_pt_pr_id;
			search_pt_pr_model = body.getJSONObject("search").getString("pt_pr_model");
			search_pt_pr_model = search_pt_pr_model.equals("") ? null : search_pt_pr_model;

			search_pt_pr_bom_id = body.getJSONObject("search").getString("pt_pr_bom_id");
			search_pt_pr_bom_id = search_pt_pr_bom_id.equals("") ? null : search_pt_pr_bom_id;
			search_pt_pr_b_item = body.getJSONObject("search").getString("pt_pr_b_item");
			search_pt_pr_b_item = search_pt_pr_b_item.equals("") ? null : search_pt_pr_b_item;
			search_pt_pr_s_item = body.getJSONObject("search").getString("pt_pr_s_item");
			search_pt_pr_s_item = search_pt_pr_s_item.equals("") ? null : search_pt_pr_s_item;
		}
		tests = testDao.findAllByTest(search_pt_pb_b_sn, search_pt_pr_id, //
				search_pt_pr_model, search_pt_pr_bom_id, //
				search_pt_pr_b_item, search_pt_pr_s_item, //
				page_r);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		tests.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_id", one.getPtid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pb_g_id", one.getPtpbgid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pr_id", one.getPtprid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pr_model", one.getPtprmodel() == null ? "" : one.getPtprmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pr_bom_id", one.getPtprbomid() == null ? "" : one.getPtprbomid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pb_b_sn", one.getPtpbbsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pr_b_item", one.getPtprbitem());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_pr_s_item", one.getPtprsitem());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_l_dt", one.getPtldt() == null ? "" : one.getPtldt());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_l_size", one.getPtlsize());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_l_path", one.getPtlpath());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pt_l_text", one.getPtltext());

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
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("create");
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("save_as");
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("modify");
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("delete");

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
