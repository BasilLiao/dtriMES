package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.LabelList;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.ProductionSN;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.WorkHours;
import dtri.com.tw.db.entity.WorkType;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.LabelListDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductionRecordsDao;
import dtri.com.tw.db.pgsql.dao.ProductionSNDao;
import dtri.com.tw.db.pgsql.dao.WorkHoursDao;
import dtri.com.tw.db.pgsql.dao.WorkTypeDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_SN;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductionHeaderService {
	@Autowired
	private ProductionHeaderDao productionHeaderDao;

	@Autowired
	ProductionRecordsDao recordsDao;

	@Autowired
	private WorkstationProgramDao programDao;

	@Autowired
	private WorkstationDao workDao;

	@Autowired
	private ProductionBodyDao bodyDao;

	@Autowired
	private ProductionSNDao snDao;

	@Autowired
	private WorkTypeDao typeDao;

	@Autowired
	private WorkHoursDao hoursDao;

	@Autowired
	private LabelListDao labelListDao;

	@Autowired
	EntityManager em;

	// 取得當前 資料清單

	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<ProductionHeader> productionHeaders = new ArrayList<ProductionHeader>();
		ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("phid").descending());
		String search_phprid = "";
		String search_phorder_id = "";
		String search_phcname = "";
		Date search_phssdate = null;
		Date search_phsedate = null;
		Date search_sysmdate = null;
		String search_sysstatus = "1";
		// 產品資訊
		String search_prmodel = "";
		String search_pbsnvalue = "";
		String search_pbsnname = "";
		String search_pbsn = "";
		// 產品規格
		String search_prbom_id = "";
		String search_prbitem = "";
		String search_prsitem = "";
		// 製令單據
		String ph_id = "ID", ph_pb_g_id = "產品關聯ID", ph_type = "製令類型", ph_pr_id = "製令單號", //
				ph_mfg_p_no = "驗證碼(MFG Part No)", ph_ps_no = "組件號(Parts No)", ph_p_name = "產品號(Product Name)", //
				// 產品號(Product Name) 可能與 BOM不同[如果不是半成品 則以成品 BOM命名]
				ph_wp_id = "工作程序", ph_s_date = "開始時間", ph_e_date = "結束時間", ph_schedule = "進度(X／X)", //
				ph_e_s_date = "預計出貨日", ph_p_qty = "預計生產數", ph_p_ok_qty = "生產完成數", ph_p_a_ok_qty = "加工完成數", //
				ph_order_id = "訂單編號", ph_c_name = "客戶名稱", ph_c_from = "單據來源", ph_w_years = "保固(年)", //
				ph_wc_line = "生產線", ph_ll_a_json = "標籤組內容", ph_ll_g_name = "標籤組";
		// 製令單規格
		String pr_bom_id = "BOM料號(公司)", pr_bom_c_id = "BOM料號(客戶)", pr_p_model = "產品型號", pr_p_v = "產品版本", //
				pr_b_item = "規格定義", pr_s_item = "軟體定義", pr_name = "產品品名", pr_specification = "規格敘述", //
				pr_s_sn = "產品序號(開始)", pr_e_sn = "產品序號(結束)", pr_s_b_sn = "燒錄序號(開始)", pr_e_b_sn = "燒錄序號(結束)";
		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", sys_sort = "排序", sys_ver = "版本", sys_status = "狀態";
		List<Long> pbid = new ArrayList<Long>();
		// 工作站
		ArrayList<Workstation> w_s = workDao.findAllBySysheaderAndWidNot(true, 0L, PageRequest.of(0, 100));
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			// header-製令單
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph", FFS.h_t("ph", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_id", FFS.h_t(ph_id, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_pb_g_id", FFS.h_t(ph_pb_g_id, "130px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_type", FFS.h_t(ph_type, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_pr_id", FFS.h_t(ph_pr_id, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_mfg_p_no", FFS.h_t(ph_mfg_p_no, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_ps_no", FFS.h_t(ph_ps_no, "160px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_p_name", FFS.h_t(ph_p_name, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_wp_id", FFS.h_t(ph_wp_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_e_s_date", FFS.h_t(ph_e_s_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_s_date", FFS.h_t(ph_s_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_e_date", FFS.h_t(ph_e_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_p_qty", FFS.h_t(ph_p_qty, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_p_ok_qty", FFS.h_t(ph_p_ok_qty, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_p_a_ok_qty", FFS.h_t(ph_p_a_ok_qty, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_schedule", FFS.h_t(ph_schedule, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_order_id", FFS.h_t(ph_order_id, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_c_name", FFS.h_t(ph_c_name, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_c_from", FFS.h_t(ph_c_from, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_w_years", FFS.h_t(ph_w_years, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_wc_line", FFS.h_t(ph_wc_line, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll", FFS.h_t("ll", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_ll_g_name", FFS.h_t(ph_ll_g_name, "130px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_ll_a_json", FFS.h_t(ph_ll_a_json, "130px", FFM.Wri.W_N));

			// 工作站
			for (Workstation w_one : w_s) {
				object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_schedule", FFS.h_t(w_one.getWpbname() + "(完成數)", "180px", FFM.Wri.W_Y));
			}
			// 規格
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr", FFS.h_t("pr", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_bom_c_id", FFS.h_t(pr_bom_c_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_p_model", FFS.h_t(pr_p_model, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_p_model", FFS.h_t(pr_p_v, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_name", FFS.h_t(pr_name, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_specification", FFS.h_t(pr_specification, "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_b_item", FFS.h_t(pr_b_item, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_s_item", FFS.h_t(pr_s_item, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_s_sn", FFS.h_t(pr_s_sn, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_e_sn", FFS.h_t(pr_e_sn, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_s_b_sn", FFS.h_t(pr_s_b_sn, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_e_b_sn", FFS.h_t(pr_e_b_sn, "150px", FFM.Wri.W_Y));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t(sys_sort, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t(sys_ver, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_Y));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();
			// [製令單]
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "====製令單內容====", "", FFM.Wri.W_N, "col-md-12", false, n_val, "ph", ""));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_id", ph_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_pb_g_id", ph_pb_g_id));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "一般製令_No(sn)").put("key", "A511_no_sn"));
			a_val.put((new JSONObject()).put("value", "一般製令_Both(sn)").put("key", "A511_no_and_has_sn"));
			a_val.put((new JSONObject()).put("value", "一般製令_Create(sn)").put("key", "A511_has_sn"));
			a_val.put((new JSONObject()).put("value", "重工製令_Old(sn)").put("key", "A521_old_sn"));
			a_val.put((new JSONObject()).put("value", "重工製令_Both(sn)").put("key", "A521_no_and_has_sn"));
			a_val.put((new JSONObject()).put("value", "重工製令_Create(sn)").put("key", "A521_has_sn"));
			a_val.put((new JSONObject()).put("value", "維護製令_No(sn)").put("key", "A522_service"));
			a_val.put((new JSONObject()).put("value", "拆解製令_No(sn)").put("key", "A431_disassemble"));
			a_val.put((new JSONObject()).put("value", "委外製令_No(sn)").put("key", "A512_outside"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "A511", "A511", FFM.Wri.W_Y, "col-md-2", true, a_val, "ph_type", ph_type));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "ph_pr_id", ph_pr_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "ph_mfg_p_no", ph_mfg_p_no));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "ph_ps_no", ph_ps_no));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "ph_p_name", ph_p_name));

			ArrayList<WorkstationProgram> list_p = programDao.findAllBySysheader(true);
			JSONArray a_val_p = new JSONArray();
			list_p.forEach(p -> {
				a_val_p.put((new JSONObject()).put("value", p.getWpname()).put("key", p.getWpgid()));
			});
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, a_val_p, "ph_wp_id", "工作程序"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "ph_c_name", ph_c_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "ph_order_id", ph_order_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", false, n_val, "ph_p_qty", ph_p_qty));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_p_ok_qty", ph_p_ok_qty));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_p_a_ok_qty",
					ph_p_a_ok_qty));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "ph_schedule", ph_schedule));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "ph_s_date", ph_s_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "ph_e_date", ph_e_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "ph_e_s_date", ph_e_s_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", false, n_val, "ph_w_years", ph_w_years));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "ph_wc_line", ph_wc_line));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_c_from", ph_c_from));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "====標籤設置內容====", "", FFM.Wri.W_N, "col-md-12", false, n_val, "ll", ""));

			JSONArray a_val_g = new JSONArray();
			ArrayList<String> label_g = labelListDao.getLabelGroupDistinct();
			label_g.forEach(p -> {
				a_val_g.put((new JSONObject()).put("value", p).put("key", p));
			});
			obj_m.put(
					FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, a_val_g, "ph_ll_g_name", ph_ll_g_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "ph_ll_a_json", ph_ll_a_json));

			// 規格-ProductionRecords
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "====產品規格====", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pr", ""));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "pr_p_model", pr_p_model));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "pr_p_v", pr_p_v));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "pr_bom_id", pr_bom_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "pr_bom_c_id", pr_bom_c_id));
			// 流水號
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "無客製化SN,請空白", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "ps_b_f_sn",
					"SN_燒錄序號(固定)"));//
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-2", false, n_val, "ps_b_sn", "SN_燒錄序號(流水)"));//

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pr_name", pr_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pr_specification",
					pr_specification));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pr_b_item", pr_b_item));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pr_s_item", pr_s_item));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "13碼", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pr_s_sn", pr_s_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "13碼", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pr_e_sn", pr_e_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pr_s_b_sn", pr_s_b_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pr_e_b_sn", pr_e_b_sn));

			// sys
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", sys_ver));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", true, n_val, "sys_sort", sys_sort));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "待命中").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "生產中").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "已完成").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "暫停中").put("key", "8"));
			a_val.put((new JSONObject()).put("value", "已終止").put("key", "9"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "sys_status", "狀態"));
			bean.setCell_modify(obj_m);
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", sys_note));

			// 放入包裝(search)
			// 製令查詢
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ph_pr_id", ph_pr_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_p_model", pr_p_model, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "ph_s_s_date", "投線時間(始)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "ph_s_e_date", "投線時間(終)", n_val));

			// 規格查詢
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ph_c_name", ph_c_name, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ph_order_id", ph_order_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_bom_id", pr_bom_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pr_b_item", pr_b_item, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pr_s_item", pr_s_item, n_val));

			// 項目查詢(選單)
			a_val = new JSONArray();
			for (int j = 0; j < 50; j++) {
				String m_name = "getPbvalue" + String.format("%02d", j + 1);
				try {
					Method method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = String.format("pb_value" + String.format("%02d", j + 1));
					if (value != null && !value.equals("")) {
						a_val.put((new JSONObject()).put("value", value).put("key", name));
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "pb_sn_name", "SN_料件類型", a_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "0", "col-md-2", "pb_sn_value", "SN_料件序號", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pb_sn", "SN_產品序號", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "未開工").put("key", "not_yet_started"));
			a_val.put((new JSONObject()).put("value", "已開工").put("key", "started"));
			a_val.put((new JSONObject()).put("value", "已完成").put("key", "end"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "ph_schedule_today", "今日異動", a_val));
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "待命中").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "生產中").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "已完成").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "暫停中").put("key", "8"));
			a_val.put((new JSONObject()).put("value", "已終止").put("key", "9"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			// 製令單資訊
			search_phprid = body.getJSONObject("search").getString("ph_pr_id");
			search_phprid = (search_phprid.equals("")) ? null : search_phprid;
			if (search_pbsnname.equals("") || search_pbsnvalue.equals("")) {
				search_pbsnvalue = "";
				search_pbsnname = "";
			}
			search_phorder_id = body.getJSONObject("search").getString("ph_order_id");
			search_phorder_id = (search_phorder_id.equals("")) ? null : search_phorder_id;
			search_phcname = body.getJSONObject("search").getString("ph_c_name");
			search_phcname = (search_phcname.equals("")) ? null : search_phcname;
			search_sysstatus = body.getJSONObject("search").getString("sys_status");
			search_sysstatus = (search_sysstatus.equals("")) ? "-1" : search_sysstatus;
			if (!body.getJSONObject("search").getString("ph_s_s_date").equals("")) {
				search_phssdate = Fm_Time.toDateTime(body.getJSONObject("search").getString("ph_s_s_date"));
			}
			if (!body.getJSONObject("search").getString("ph_s_e_date").equals("")) {
				search_phsedate = Fm_Time.toDateTime(body.getJSONObject("search").getString("ph_s_e_date"));
			}
			// 產品資訊
			search_pbsn = body.getJSONObject("search").getString("pb_sn");
			search_pbsn = (search_pbsn == null) ? "" : search_pbsn;

			search_pbsnname = body.getJSONObject("search").getString("pb_sn_name");
			search_pbsnname = search_pbsnname == null ? "" : search_pbsnname;
			search_pbsnvalue = body.getJSONObject("search").getString("pb_sn_value");
			search_pbsnvalue = search_pbsnvalue == null ? "" : search_pbsnvalue;
			// 檢核-共同查詢
			if (!search_pbsnname.equals("") && search_pbsnvalue.equals("")) {
				bean.autoMsssage("102");
				return false;
			}
			if (search_pbsnname.equals("") && !search_pbsnvalue.equals("")) {
				bean.autoMsssage("102");
				return false;
			}
			// 產品規格
			search_prmodel = body.getJSONObject("search").getString("pr_p_model");
			search_prmodel = (search_prmodel.equals("")) ? null : search_prmodel;
			search_prbom_id = body.getJSONObject("search").getString("pr_bom_id");
			search_prbom_id = (search_prbom_id.equals("")) ? null : search_prbom_id;
			search_prbitem = body.getJSONObject("search").getString("pr_b_item");
			search_prbitem = (search_prbitem.equals("")) ? null : search_prbitem;
			search_prsitem = body.getJSONObject("search").getString("pr_s_item");
			search_prsitem = (search_prsitem.equals("")) ? null : search_prsitem;

			// 當前進度
			if (!body.getJSONObject("search").getString("ph_schedule_today").equals("")) {
				String today_ch = body.getJSONObject("search").getString("ph_schedule_today");
				search_sysmdate = Fm_Time.toDate(Fm_Time.to_y_M_d(new Date()));
				if (today_ch.equals("started")) {
					search_sysstatus = "1";
				} else if (today_ch.equals("not_yet_started")) {
					search_sysstatus = "0";
				} else if (today_ch.equals("end")) {
					search_sysstatus = "2";
				}
			}
		}

		// 查詢特定SN
		if (!search_pbsnvalue.equals("") && !search_pbsnname.equals("") || !search_pbsn.equals("")) {
			// 條件
			String nativeQuery = "SELECT b.pb_g_id FROM production_body b join production_header h on b.pb_g_id = h.ph_pb_g_id  WHERE ";
			if (!search_pbsnvalue.equals("") && !search_pbsnname.equals(""))
				nativeQuery += " (:pb_sn_value='' or " + search_pbsnname + " LIKE :pb_sn_value) and ";
			if (!search_pbsn.equals(""))
				nativeQuery += " (:pb_sn='' or b.pb_sn LIKE :pb_sn) and ";
			nativeQuery += " (b.pb_g_id != 0) group by b.pb_g_id ";
			// 項目
			Query query = em.createNativeQuery(nativeQuery);
			if (!search_pbsnvalue.equals("") && !search_pbsnname.equals(""))
				query.setParameter("pb_sn_value", "%" + search_pbsnvalue + "%");
			if (!search_pbsn.equals(""))
				query.setParameter("pb_sn", "%" + search_pbsn + "%");

			// 轉換LONG
			@SuppressWarnings("unchecked")
			List<BigInteger> pbid_obj = query.getResultList();
			for (BigInteger obj : pbid_obj) {
				String one = obj.toString();
				pbid.add(Long.parseLong(one));
			}
		} else {
			pbid = null;
		}
		// 如果有查SN 則要有值/沒查則pass
		if ((!search_pbsnvalue.equals("") && pbid.size() > 0) || search_pbsnvalue.equals("")) {
			productionHeaders = productionHeaderDao.findAllByProductionHeader(//
					search_prmodel, search_phprid, Integer.parseInt(search_sysstatus), pbid, //
					search_phorder_id, search_phcname, search_prbom_id, search_prbitem, //
					search_prsitem, search_phssdate, search_phsedate, search_sysmdate, page_r);
		}
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_all = new JSONObject();

		productionHeaders.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			// 製令單
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph", "====製令單內容====");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_id", one.getPhid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_pb_g_id", one.getPhpbgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_type", one.getPhtype());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_pr_id", one.getProductionRecords().getPrid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_mfg_p_no", one.getPhmfgpno() == null ? "" : one.getPhmfgpno());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_ps_no", one.getPhpsno() == null ? "" : one.getPhpsno());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_p_name", one.getPhpname() == null ? "" : one.getPhpname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_wp_id", one.getPhwpid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_e_s_date", one.getPhesdate() == null ? "" : one.getPhesdate());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_s_date", one.getPhsdate() == null ? "" : Fm_Time.to_yMd_Hms(one.getPhsdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_e_date", one.getPhedate() == null ? "" : Fm_Time.to_yMd_Hms(one.getPhedate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_p_qty", one.getPhpqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_p_ok_quantity", one.getPhpokqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_p_a_ok_quantity", one.getPhpaokqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_schedule", one.getPhschedule());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_order_id", one.getPhorderid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_c_name", one.getPhcname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_c_from", one.getPhcfrom());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_w_years", one.getPhwyears());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_wc_line", one.getPhwcline() == null ? "" : one.getPhwcline());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll", "====標籤設置內容====");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_ll_g_name", one.getPhllgname() == null ? "" : one.getPhllgname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_ll_a_json", one.getPhllajson() == null ? "" : one.getPhllajson());

			// 工作程序
			JSONObject ph_pb_s = new JSONObject();
			if (one.getPhpbschedule() != null && one.getPhpbschedule() != "") {
				ph_pb_s = new JSONObject(one.getPhpbschedule());
			}
			for (Workstation w_one : w_s) {
				if (ph_pb_s.has(w_one.getWcname())) {
					object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_" + w_one.getWcname(), ph_pb_s.getInt(w_one.getWcname()));
				} else {
					object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_" + w_one.getWcname(), "無");
				}
			}

			// 產品規格
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr", "====產品規格====");
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_id", one.getProductionRecords().getPrbomid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_c_id", one.getProductionRecords().getPrbomcid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_p_model", one.getProductionRecords().getPrpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_p_v", one.getProductionRecords().getPrpv());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_name", one.getProductionRecords().getPrname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_specification", one.getProductionRecords().getPrspecification());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_b_item", one.getProductionRecords().getPrbitem());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_s_item", one.getProductionRecords().getPrsitem());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_s_sn", one.getProductionRecords().getPrssn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_e_sn", one.getProductionRecords().getPresn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_s_b_sn", one.getProductionRecords().getPrsbsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_e_b_sn", one.getProductionRecords().getPrebsn());

			// sys
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
		object_bodys_all.put("search", object_bodys);

		// 刷新修改畫面 [(key)](modify/Create/Delete) 格式
		JSONArray obj_m = new JSONArray();
		bean.setCell_refresh(obj_m);
		bean.setBody(object_bodys_all);
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("create");
			for (Object one : list) {
				// 物件轉換
				ProductionHeader pro_h = new ProductionHeader();
				ProductionRecords pro_r = new ProductionRecords();
				List<ProductionBody> pro_bs = new ArrayList<ProductionBody>();
				ArrayList<ProductionSN> pro_sn = new ArrayList<ProductionSN>();
				JSONObject data = (JSONObject) one;

				// 查詢重複
				ProductionRecords search = new ProductionRecords();
				search.setPrid(data.getString("ph_pr_id"));
				List<ProductionHeader> headers = productionHeaderDao.findAllByProductionRecords(search);

				if (headers.size() > 0) {
					pro_h = headers.get(0);
				}
				// 無重複->新建
				if (headers.size() < 1) {
					// header
					Long id_b_g2 = 1l;
					// 工作站資訊
					JSONObject json_work = new JSONObject();
					JSONObject json_ph_pb_schedule = new JSONObject();
					ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(data.getLong("ph_wp_id"), false);
					for (WorkstationProgram p_one : programs) {
						ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
						JSONObject json_one = new JSONObject();
						json_one.put("name", works.get(0).getWpbname());
						json_one.put("type", works.get(0).getWcname() + "_N");
						json_one.put("id", works.get(0).getWid());
						json_one.put("w_pb_cell", works.get(0).getWpbcell());
						json_one.put("sort", p_one.getSyssort());
						json_work.put(works.get(0).getWcname(), json_one);
						json_ph_pb_schedule.put(works.get(0).getWcname(), 0);
					}
					// 無SN不需要指定新建
					JSONArray pbsn_list = new JSONArray();
					if (data.getString("ph_type").equals("A511_no_and_has_sn") || data.getString("ph_type").equals("A511_has_sn")
							|| data.getString("ph_type").equals("A521_no_and_has_sn") || data.getString("ph_type").equals("A521_has_sn")) {

						String sn_f = "";
						String sn_e = "";
						JSONObject sn_list = new JSONObject();
						// Step1. SN 類型 [系統SN][自訂義SN]
						if (data.isNull("ps_b_f_sn") && data.getString("ps_b_f_sn").equals("") && data.getInt("ps_b_sn") >= 0) {
							// 檢核[系統SN]選項
							if (data.getString("ps_sn_1").equals("no_") || //
									data.getString("ps_sn_2").equals("s") || //
									data.getString("ps_sn_3").equals("n") || //
									data.getString("ps_sn_5").equals("n")) {
								return false;
							}

							// 分類[系統SN] 規則
							pro_sn = new ArrayList<ProductionSN>();
							ProductionSN pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_1"));
							pro_sn_one.setPsgid(1L);
							pro_sn.add(pro_sn_one);

							pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_2"));
							pro_sn_one.setPsgid(2l);
							pro_sn.add(pro_sn_one);

							pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_3"));
							pro_sn_one.setPsgid(3l);
							pro_sn.add(pro_sn_one);

							pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_4"));
							pro_sn_one.setPsgid(4l);
							pro_sn_one.setPsname("[YYWW]");
							pro_sn.add(pro_sn_one);

							pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_5"));
							pro_sn_one.setPsgid(5l);
							pro_sn.add(pro_sn_one);

							pro_sn_one = new ProductionSN();
							pro_sn_one.setPsvalue(data.getString("ps_sn_6"));
							pro_sn_one.setPsgid(6l);
							pro_sn_one.setPsname("[000]");
							pro_sn.add(pro_sn_one);
							System.out.println("");
							sn_list = Fm_SN.analyze_Sn(pro_sn, true, data.getInt("ph_p_qty"));
							// 檢核[系統SN]_區間(頭尾)是否用過
							sn_f = sn_list.getJSONArray("sn_list").getString(0);
							sn_e = sn_list.getJSONArray("sn_list").getString(sn_list.getJSONArray("sn_list").length() - 1);
							if (bodyDao.findAllByPbbsn(sn_f).size() > 0 || bodyDao.findAllByPbbsn(sn_e).size() > 0) {
								return false;
							}
							// 檢核[系統SN]_區間(之內)是否用過
							if (recordsDao.findAllByRecordsESprssn(sn_f, sn_e).size() > 0) {
								return false;
							}

							productionHeaderDao.findAll();
							// 更新[系統SN]區段
							ProductionSN pro_sn_YYMM = snDao.findAllByPsid(11L).get(0);
							pro_sn_YYMM.setPsvalue(sn_list.getString("sn_YYWW"));
							ProductionSN pro_sn_SN = snDao.findAllByPsid(16L).get(0);
							pro_sn_SN.setPsvalue(sn_list.getString("sn_000"));
							snDao.save(pro_sn_SN);
							snDao.save(pro_sn_YYMM);
						}
						// Step2. SN 類型 [API_SN][MES_SN]
						if (data.has("sn_list") && data.getJSONArray("sn_list").length() > 0 && data.getBoolean("sys_sn_auto")) {
							// [API_SN] 的資料
							pbsn_list = data.getJSONArray("sn_list");
						} else {
							// [MES_SN] 自產資料 分兩類 [自動生成SN] or [自定義生成SN]
							if (pbsn_list.length() == 0) {
								// [自定義生成SN]
								for (int i = 0; i < data.getInt("ph_p_qty"); i++) {
									String pr_e_b_sn = data.getString("ps_b_f_sn")
											+ String.format("%0" + data.getString("ps_b_sn").length() + "d", (data.getInt("ps_b_sn") + i));
									pbsn_list.put(pr_e_b_sn);
								}
							} else {
								// [自動生成SN]
								pbsn_list = sn_list.getJSONArray("sn_list");
							}
						}

						// 檢核[SN]_區間是否用過
						sn_f = pbsn_list.get(0).toString();
						sn_e = pbsn_list.get(pbsn_list.length() - 1).toString();
						if (bodyDao.findAllByPbbsn(sn_f).size() > 0 || bodyDao.findAllByPbbsn(sn_e).size() > 0) {
							return false;
						}
						// 檢核[系統SN]_區間(之內)是否用過
						if (recordsDao.findAllByRecordsESprssn(sn_f, sn_e).size() > 0) {
							return false;
						}
						// Step3. 建立[產品資訊]
						Long id_b_g = bodyDao.getProductionBodyGSeq();

						for (int i = 0; i < pbsn_list.length(); i++) {
							// body
							ProductionBody pro_b = new ProductionBody();
							pro_b.setSysver(0);
							pro_b.setPbgid(id_b_g);
							pro_b.setSysheader(false);
							pro_b.setPbsn(pbsn_list.getString(i).replaceAll(" ", ""));
							pro_b.setPbbsn(pbsn_list.getString(i).replaceAll(" ", ""));
							pro_b.setPbcheck(false);
							pro_b.setPbusefulsn(0);
							pro_b.setPbwyears(data.getInt("ph_w_years"));
							pro_b.setSysstatus(0);
							pro_b.setSyssort(data.getInt("sys_sort"));
							pro_b.setPblpath("");
							pro_b.setPblsize("");
							pro_b.setPbltext("");
							pro_b.setPbschedule(json_work.toString());
							pro_b.setSysmuser(user.getSuaccount());
							pro_b.setSyscuser(user.getSuaccount());
							pro_bs.add(pro_b);
						}
						id_b_g2 = id_b_g;
					}

					// Step4. 建立[規格資訊]
					pro_r.setPrid(data.getString("ph_pr_id").toUpperCase());
					pro_r.setPrbomid(data.getString("pr_bom_id"));
					pro_r.setPrbomcid(data.has("pr_bom_c_id") ? data.getString("pr_bom_c_id") : "");
					pro_r.setPrpv(data.getString("pr_p_v"));
					pro_r.setPrpmodel(data.getString("pr_p_model"));
					pro_r.setPrname(data.has("pr_name") ? data.getString("pr_name") : "");
					pro_r.setPrspecification(data.has("pr_specification") ? data.getString("pr_specification") : "");
					pro_r.setPrbitem(data.get("pr_b_item").toString().equals("") ? "{}" : data.get("pr_b_item").toString());
					pro_r.setPrsitem(data.get("pr_s_item").toString().equals("") ? "{}" : data.get("pr_s_item").toString());
					// pro_r.setPrwcline();

					// 有序號登記
					if (!data.getString("ph_type").equals("A511_no_sn") //
							&& !data.getString("ph_type").equals("A521_old_sn")//
							&& !data.getString("ph_type").equals("A431_disassemble")) {

						// 系統建置 SN
						pro_r.setPrssn(pbsn_list.get(0).toString());
						pro_r.setPresn(pbsn_list.get(pbsn_list.length() - 1).toString());
						pro_r.setPrsbsn(pbsn_list.get(0).toString());
						pro_r.setPrebsn(pbsn_list.get(pbsn_list.length() - 1).toString());
					} else {
						// 沒SN不須登記
						pro_r.setPrssn("no_sn0000n000");
						pro_r.setPresn("no_sn0000n000");
						pro_r.setPrsbsn("no_sn0000n000");
						pro_r.setPrebsn("no_sn0000n000");
					}
					pro_r.setSysmuser(user.getSuaccount());
					pro_r.setSyscuser(user.getSuaccount());

					// Step5. 建立[製令單資訊]
					pro_h = new ProductionHeader();
					pro_h.setPhwpid(data.getLong("ph_wp_id"));
					pro_h.setProductionRecords(pro_r);
					pro_h.setPhmfgpno(data.getString("ph_mfg_p_no"));
					pro_h.setPhpsno(data.getString("ph_ps_no"));
					pro_h.setPhpname(data.getString("ph_p_name"));
					pro_h.setPhschedule(0 + "／" + data.getInt("ph_p_qty"));
					pro_h.setPhpbschedule(json_ph_pb_schedule.toString());
					pro_h.setSysheader(true);
					pro_h.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
					pro_h.setSyssort(data.getInt("sys_sort"));
					pro_h.setSysver(0);
					pro_h.setSysstatus(data.getInt("sys_status"));
					pro_h.setSysmuser(user.getSuaccount());
					pro_h.setSyscuser(user.getSuaccount());
					pro_h.setPhtype(data.getString("ph_type"));
					pro_h.setPhpbgid(id_b_g2);
					pro_h.setPhcfrom(data.getString("ph_c_from").equals("") ? "MES" : data.getString("ph_c_from"));
					pro_h.setPhcname(data.getString("ph_c_name"));
					pro_h.setPhorderid(data.getString("ph_order_id"));
					pro_h.setPhpqty(data.getInt("ph_p_qty"));
					pro_h.setPhpokqty(0);
					pro_h.setPhpaokqty(0);
					pro_h.setPhwyears(data.getInt("ph_w_years"));
					pro_h.setPhesdate(data.getString("ph_e_s_date"));
					pro_h.setPhwcline(data.getString("ph_wc_line"));
					// 標籤?
					if (data.has("ph_ll_g_name")) {
						pro_h.setPhllgname(data.getString("ph_ll_g_name"));
						ArrayList<LabelList> labels = labelListDao.findAllByLlgnameAndLlname(null, data.getString("ph_ll_g_name"), null);
						if (labels.size() >= 1) {
							String[] label_all = new String[labels.size()];
							for (int i = 0; i < labels.size(); i++) {
								label_all[i] = labels.get(i).getLlajson();
							}
							pro_h.setPhllajson(Arrays.toString(label_all));
						}
					}

					productionHeaderDao.save(pro_h);
					bodyDao.saveAll(pro_bs);

					// Step5. 建立[工時資訊] typeDao+hoursDao
					List<WorkHours> works_p = new ArrayList<WorkHours>();
					ArrayList<WorkType> types = typeDao.findAll();
					types.forEach(t -> {
						WorkHours work_p = new WorkHours();
						if (t.getWtid() == 0) {
							// 父類別
							work_p.setSysheader(true);
						} else {
							work_p.setSysheader(false);
							// 子類別
						}
						work_p.setProductionRecords(pro_r);
						work_p.setWhwtid(t);
						work_p.setWhdo("複製:範例樣本,不列入計算");
						work_p.setWhaccount("");
						work_p.setWhnb(0);
						work_p.setSysnote("");
						work_p.setSyssort(0);
						work_p.setSysstatus(2);

						// 製令單類型
						switch (data.getString("ph_type")) {
						case "A511_no_sn":
							// A511_no_sn 一般製令_No(sn)
							works_p.add(work_p);
							break;
						case "A511_no_and_has_sn":
							// A511_no_and_has_sn 一般製令_Both(sn)
							works_p.add(work_p);
							break;
						case "A511_has_sn":
							// A511_has_sn 一般製令_Create(sn)

							break;
						case "A521_has_sn":
							// A511_no_sn 重工製令_No(sn)

							break;
						case "A521_no_and_has_sn":
							// A511_no_and_has_sn 重工製令_Both(sn)
							works_p.add(work_p);
							break;
						case "A521_old_sn":
							// A521_old_sn 重工製令_Re(sn)

							break;
						case "A522_service":
							// A522_service 維護製令

							break;
						case "A431_disassemble":
							// A522_service 拆解製令

							break;
						case "A512_outside":
							// A522_service 委外製令

							break;

						default:
							break;
						}
					});
					hoursDao.saveAll(works_p);
				} else {
					return check;
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
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// 物件轉換
				ProductionHeader pro_h = new ProductionHeader();
				// ProductionBody pro_b = new ProductionBody();
				ProductionRecords pro_r = new ProductionRecords();
				ArrayList<ProductionSN> pro_sn = new ArrayList<ProductionSN>();
				List<ProductionBody> pro_bs = new ArrayList<ProductionBody>();
				JSONObject data = (JSONObject) one;

				// 查詢重複
				ProductionRecords search = new ProductionRecords();
				search.setPrid(data.getString("ph_pr_id"));
				List<ProductionHeader> headers = productionHeaderDao.findAllByProductionRecords(search);
				if (headers.size() > 0) {
					pro_h = headers.get(0);
				}
				// 無重複->新建
				if (headers.size() < 1) {
					// header
					Long id_b_g2 = 1l;
					// 工作站資訊
					JSONObject json_work = new JSONObject();
					ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(data.getLong("ph_wp_id"), false);
					for (WorkstationProgram p_one : programs) {
						ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
						JSONObject json_one = new JSONObject();
						json_one.put("name", works.get(0).getWpbname());
						json_one.put("type", works.get(0).getWcname() + "_N");
						json_one.put("id", works.get(0).getWid());
						json_one.put("w_pb_cell", works.get(0).getWpbcell());
						json_one.put("sort", p_one.getSyssort());
						json_work.put(works.get(0).getWcname(), json_one);
					}
					// 無SN不需要指定新建
					JSONArray sn_lists = new JSONArray();
					if (data.getString("ph_type").equals("A511_no_and_has_sn") || data.getString("ph_type").equals("A511_has_sn")
							|| data.getString("ph_type").equals("A521_no_and_has_sn") || data.getString("ph_type").equals("A521_has_sn")) {

						// 檢核SN選項
						if (data.getString("ps_sn_1").equals("no_") || //
								data.getString("ps_sn_2").equals("s") || //
								data.getString("ps_sn_3").equals("n") || //
								data.getString("ps_sn_5").equals("n")) {
							return false;
						}

						// sn
						pro_sn = new ArrayList<ProductionSN>();
						ProductionSN pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_1"));
						pro_sn_one.setPsgid(1l);
						pro_sn.add(pro_sn_one);

						pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_2"));
						pro_sn_one.setPsgid(2l);
						pro_sn.add(pro_sn_one);

						pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_3"));
						pro_sn_one.setPsgid(3l);
						pro_sn.add(pro_sn_one);

						pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_4"));
						pro_sn_one.setPsgid(4l);
						pro_sn_one.setPsname("[YYWW]");
						pro_sn.add(pro_sn_one);

						pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_5"));
						pro_sn_one.setPsgid(5l);
						pro_sn.add(pro_sn_one);

						pro_sn_one = new ProductionSN();
						pro_sn_one.setPsvalue(data.getString("ps_sn_6"));
						pro_sn_one.setPsgid(6l);
						pro_sn_one.setPsname("[000]");
						pro_sn.add(pro_sn_one);
						JSONObject sn_list = Fm_SN.analyze_Sn(pro_sn, true, data.getInt("ph_p_qty"));

						// 檢核_區間是否用過
						String sn_f = sn_list.getJSONArray("sn_list").get(0).toString();
						String sn_e = sn_list.getJSONArray("sn_list").get(sn_list.getJSONArray("sn_list").length() - 1).toString();
						if (bodyDao.findAllByPbsn(sn_f).size() > 0 || bodyDao.findAllByPbsn(sn_e).size() > 0) {
							return false;
						}

						// 更新SN區段
						ProductionSN pro_sn_YYMM = snDao.findAllByPsid(11L).get(0);
						pro_sn_YYMM.setPsvalue(sn_list.getString("sn_YYWW"));
						ProductionSN pro_sn_SN = snDao.findAllByPsid(16L).get(0);
						pro_sn_SN.setPsvalue(sn_list.getString("sn_000"));
						snDao.save(pro_sn_SN);
						snDao.save(pro_sn_YYMM);

						sn_lists = sn_list.getJSONArray("sn_list");

						Long id_b_g = bodyDao.getProductionBodyGSeq();

						for (int i = 0; i < sn_lists.length(); i++) {
							// body
							ProductionBody pro_b = new ProductionBody();
							pro_b.setSysver(0);
							pro_b.setPbgid(id_b_g);
							pro_b.setSysheader(false);
							pro_b.setPbsn(sn_lists.getString(i));
							if (data.getString("ps_b_f_sn") != null && !data.getString("ps_b_f_sn").equals("") && data.getInt("ps_b_sn") >= 0) {
								String pr_e_b_sn = data.getString("ps_b_f_sn")
										+ String.format("%0" + data.getString("ps_b_sn").length() + "d", (data.getInt("ps_b_sn") + i));
								pro_b.setPbbsn(pr_e_b_sn);
							} else {
								pro_b.setPbbsn(sn_lists.getString(i));
							}
							pro_b.setPbcheck(false);
							pro_b.setPbusefulsn(0);
							pro_b.setPbwyears(data.getInt("ph_w_years"));
							pro_b.setSyssort(data.getInt("sys_sort"));
							pro_b.setPblpath("");
							pro_b.setPblsize("");
							pro_b.setPbltext("");
							pro_b.setPbschedule(json_work.toString());
							pro_b.setSysmuser(user.getSuaccount());
							pro_b.setSyscuser(user.getSuaccount());
							pro_b.setSysstatus(0);
							pro_bs.add(pro_b);
						}

						id_b_g2 = id_b_g;
					}

					// 規格
					pro_r.setPrid(data.getString("ph_pr_id").toUpperCase());
					pro_r.setPrbomid(data.getString("pr_bom_id"));
					pro_r.setPrbomcid(data.has("pr_bom_c_id") ? data.getString("pr_bom_c_id") : "");
					pro_r.setPrpv(data.getString("pr_p_v"));
					pro_r.setPrpmodel(data.getString("pr_p_model"));
					pro_r.setPrbitem("");
					pro_r.setPrsitem("");
					pro_r.setPrname("");
					pro_r.setPrspecification("");

					if (!data.getString("ph_type").equals("A511_no_sn")) {
						pro_r.setPrssn(sn_lists.get(0).toString());
						pro_r.setPresn(sn_lists.get(sn_lists.length() - 1).toString());
						// 燒錄序號
						if (data.getString("ps_b_f_sn") != null && !data.getString("ps_b_f_sn").equals("") && data.getInt("ps_b_sn") >= 0) {
							int ps_b_sn = data.getInt("ps_b_sn");
							int ps_b_size = data.getString("ps_b_sn").length();
							ps_b_sn += data.getInt("ph_p_qty");
							String pr_e_b_sn = String.format("%0" + ps_b_size + "d", ps_b_sn);

							pro_r.setPrsbsn(data.getString("ps_b_f_sn") + data.getString("ps_b_sn"));
							pro_r.setPrebsn(data.getString("ps_b_f_sn") + pr_e_b_sn);
						} else {
							pro_r.setPrsbsn(sn_lists.get(0).toString());
							pro_r.setPrebsn(sn_lists.get(sn_lists.length() - 1).toString());
						}
					} else {
						// 沒SN不須登記
						pro_r.setPrssn("no_sn0000n000");
						pro_r.setPresn("no_sn0000n000");
						pro_r.setPrsbsn("no_sn0000n000");
						pro_r.setPrebsn("no_sn0000n000");
					}

					pro_r.setSysmuser(user.getSuaccount());
					pro_r.setSyscuser(user.getSuaccount());
					// header
					pro_h = new ProductionHeader();
					// pro_h.setPhid(id_h + 1);
					pro_h.setPhwpid(data.getLong("ph_wp_id"));
					pro_h.setProductionRecords(pro_r);
					pro_h.setPhmfgpno(data.getString("ph_mfg_p_no"));
					pro_h.setPhpsno(data.getString("ph_ps_no"));
					pro_h.setPhpname(data.getString("ph_p_name"));
					pro_h.setPhschedule(0 + "／" + data.getInt("ph_p_qty"));
					pro_h.setSysheader(true);
					pro_h.setSysnote(data.getString("sys_note"));
					pro_h.setSyssort(data.getInt("sys_sort"));
					pro_h.setSysver(0);
					pro_h.setSysstatus(data.getInt("sys_status"));
					pro_h.setSysmuser(user.getSuaccount());
					pro_h.setSyscuser(user.getSuaccount());
					pro_h.setPhtype(data.getString("ph_type"));
					pro_h.setPhpbgid(id_b_g2);
					pro_h.setPhcfrom("MES");
					pro_h.setPhcname(data.getString("ph_c_name"));
					pro_h.setPhorderid(data.getString("ph_order_id"));
					pro_h.setPhpqty(data.getInt("ph_p_qty"));
					pro_h.setPhpokqty(0);
					pro_h.setPhpaokqty(0);
					pro_h.setPhwyears(data.getInt("ph_w_years"));
					pro_h.setPhesdate(data.getString("ph_e_s_date"));
					pro_h.setPhwcline(data.getString("ph_wc_line"));
					// 標籤?
					if (data.has("ph_ll_g_name")) {
						pro_h.setPhllgname(data.getString("ph_ll_g_name"));
						ArrayList<LabelList> labels = labelListDao.findAllByLlgnameAndLlname(null, data.getString("ph_ll_g_name"), null);
						if (labels.size() >= 1) {
							String[] label_all = new String[labels.size()];
							for (int i = 0; i < labels.size(); i++) {
								label_all[i] = labels.get(i).getLlajson();
							}
							pro_h.setPhllajson(Arrays.toString(label_all));
						}
					}
					productionHeaderDao.save(pro_h);
					bodyDao.saveAll(pro_bs);

					// typeDao+hoursDao
					List<WorkHours> works_p = new ArrayList<WorkHours>();
					ArrayList<WorkType> types = typeDao.findAll();
					types.forEach(t -> {
						WorkHours work_p = new WorkHours();
						if (t.getWtid() == 0) {
							// 父類別
							work_p.setSysheader(true);
						} else {
							work_p.setSysheader(false);
							// 子類別
						}
						work_p.setProductionRecords(pro_r);
						work_p.setWhwtid(t);
						work_p.setWhdo("複製:範例樣本,不列入計算");
						work_p.setWhaccount("");
						work_p.setWhnb(0);
						work_p.setSysnote("");
						work_p.setSyssort(0);
						work_p.setSysstatus(2);

						// 製令單類型
						switch (data.getString("ph_type")) {
						case "A511_no_sn":
							// A511_no_sn 一般製令_No(sn)
							works_p.add(work_p);
							break;
						case "A511_no_and_has_sn":
							// A511_no_and_has_sn 一般製令_Both(sn)
							works_p.add(work_p);
							break;
						case "A511_has_sn":
							// A511_has_sn 一般製令_Create(sn)

							break;
						case "A521_has_sn":
							// A521_has_sn 重工製令_Re(sn)
							works_p.add(work_p);
							break;
						case "A521_no_and_has_sn":
							// A521_no_and_has_sn (無序號加工+有序號工作站)重工製令_No(sn)
							works_p.add(work_p);
							break;
						case "A521_old_sn":
							// A511_old_sn (舊序號加工)重工製令_No(sn)

							break;
						case "A522_service":
							// A522_service 維護製令

							break;
						case "A431_disassemble":
							// A522_service 拆解製令

							break;
						case "A512_outside":
							// A522_service 委外製令
							break;
						default:
							break;
						}
					});
					hoursDao.saveAll(works_p);

				} else {
					return check;
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
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				// 查詢重複-查詢資料?
				List<ProductionHeader> headers = productionHeaderDao.findAllByPhid(data.getLong("ph_id"));
				if (headers.size() != 1) {
					return check;
				}
				// 查詢是否 為已啟動(不可修改)
				ProductionHeader one_header = headers.get(0);
				ProductionRecords one_pecords = one_header.getProductionRecords();
				if (headers.get(0).getSysstatus() != 1 && headers.get(0).getSysstatus() != 2) {
					// 工作站資訊 (不同 工作站程序時 才作重製)
					if (one_header.getPhwpid() != data.getLong("ph_wp_id")) {
						JSONObject json_work = new JSONObject();
						ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(data.getLong("ph_wp_id"),
								false);
						for (WorkstationProgram p_one : programs) {
							ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
							JSONObject json_one = new JSONObject();
							json_one.put("name", works.get(0).getWpbname());
							json_one.put("type", works.get(0).getWcname() + "_N");
							json_one.put("id", works.get(0).getWid());
							json_one.put("w_pb_cell", works.get(0).getWpbcell());
							json_one.put("sort", p_one.getSyssort());
							json_work.put(works.get(0).getWcname(), json_one);
						}
						// 更新產品 過站設定
						JSONObject json_work_d = json_work;
						List<ProductionBody> pb_s = bodyDao.findAllByPbgidOrderByPbsnAsc(one_header.getPhpbgid());
						pb_s.forEach(s -> {
							s.setPbschedule(json_work_d.toString());
							s.setPbwyears(data.getInt("ph_w_years"));
						});
						bodyDao.saveAll(pb_s);
					}
					// 否:生產中 & 以生產
					// records
					one_pecords.setPrbomcid(data.getString("pr_bom_c_id"));
					one_pecords.setPrpmodel(data.getString("pr_p_model"));
					one_pecords.setPrpv(data.getString("pr_p_v"));
					one_pecords.setPrbomid(data.getString("pr_bom_id"));
					one_pecords.setSysmuser(user.getSuaccount());
					one_pecords.setSysmdate(new Date());
					one_header.setProductionRecords(one_pecords);

					// hearder
					one_header.setSysstatus(data.getInt("sys_status"));
					one_header.setPhmfgpno(data.getString("ph_mfg_p_no"));
					one_header.setPhpsno(data.getString("ph_ps_no"));
					one_header.setPhwpid(data.getLong("ph_wp_id"));
					one_header.setPhpname(data.getString("ph_p_name"));
					one_header.setPhschedule(data.getInt("ph_p_a_ok_qty") + "／" + one_header.getPhpqty());
					one_header.setPhcname(data.getString("ph_c_name"));
					one_header.setPhwyears(data.getInt("ph_w_years"));
					one_header.setPhwcline(data.getString("ph_wc_line"));
					one_header.setPhorderid(data.getString("ph_order_id"));
					one_header.setPhesdate(data.getString("ph_e_s_date"));
					one_header.setPhpokqty(data.getInt("ph_p_a_ok_qty"));
					one_header.setPhcfrom("MES");
					// 標籤?
					if (data.has("ph_ll_g_name")) {
						one_header.setPhllgname(data.getString("ph_ll_g_name"));
						ArrayList<LabelList> labels = labelListDao.findAllByLlgnameAndLlname(null, data.getString("ph_ll_g_name"), null);
						if (labels.size() >= 1) {
							String[] label_all = new String[labels.size()];
							for (int i = 0; i < labels.size(); i++) {
								label_all[i] = labels.get(i).getLlajson();
							}
							one_header.setPhllajson(Arrays.toString(label_all));
						}
					}
					// 系統
					one_header.setSysheader(true);
					one_header.setSysnote(data.getString("sys_note"));
					one_header.setSyssort(data.getInt("sys_sort"));
					one_header.setSysver(0);
					one_header.setSysmuser(user.getSuaccount());
					one_header.setSysmdate(new Date());

					// 工單類型 同類型可置換
					String pht_old = one_header.getPhtype();
					String pht_new = data.getString("ph_type");
					if (pht_old.indexOf("no_and_has_sn") >= 0 && pht_new.indexOf("no_and_has_sn") >= 0) {
						one_header.setPhtype(data.getString("ph_type"));
					} else if (pht_old.indexOf("has_sn") >= 0 && pht_new.indexOf("has_sn") >= 0) {
						one_header.setPhtype(data.getString("ph_type"));
					} else if ((pht_old.indexOf("no_sn") >= 0 || pht_old.indexOf("old_sn") >= 0 || pht_old.indexOf("service") >= 0
							|| pht_old.indexOf("outside") >= 0 || pht_old.indexOf("disassemble") >= 0)
							&& (pht_new.indexOf("no_sn") >= 0 || pht_new.indexOf("old_sn") >= 0 || pht_new.indexOf("service") >= 0
									|| pht_new.indexOf("outside") >= 0 || pht_new.indexOf("disassemble") >= 0)) {
						one_header.setPhtype(data.getString("ph_type"));
					}

					// 數量不為0 不可改回/待命狀態[0]
					if (data.getInt("sys_status") == 0) {
						if (data.getInt("ph_p_a_ok_qty") == 0) {
							one_header.setSysstatus(data.getInt("sys_status"));
						}
					} else {
						one_header.setPhpsno(data.getString("ph_ps_no"));
						one_header.setPhmfgpno(data.getString("ph_mfg_p_no"));
						one_header.setSysstatus(data.getInt("sys_status"));
						one_header.setSysnote(data.getString("sys_note"));
					}
					productionHeaderDao.save(one_header);

					check = true;
				} else {
					// 是:生產中 & 以生產
					// 已經結束
					if (data.getInt("sys_status") == 2) {
						one_header.setPhedate(new Date());
					} else if (data.getInt("sys_status") == 1) {
						one_header.setPhedate(null);
					}
					one_header.setSysstatus(data.getInt("sys_status"));
					productionHeaderDao.save(one_header);
					check = true;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			check = false;
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
				JSONObject data = (JSONObject) one;
				List<ProductionHeader> phs = productionHeaderDao.findAllByPhid(data.getLong("ph_id"));
				System.out.println(data.getLong("ph_id"));
				// 確認致令
				if (phs.size() != 0) {
					ProductionHeader pheader = phs.get(0);
					// 檢查 移除製令單 不能包含old 被繼承 or 繼承別張製令單
					if (bodyDao.findAllByPbgidAndPbbsnLikeOrPboldsnLike(data.getLong("ph_pb_g_id"), "old", "old").size() > 0) {
						return check;
					}
					// 移除
					hoursDao.deleteByproductionRecords(pheader.getProductionRecords());
					productionHeaderDao.deleteByPhidAndSysheader(data.getLong("ph_id"), true);
					if (data.getLong("ph_pb_g_id") != 1L) {// 不能移除 NO SN
						bodyDao.deleteByPbgid(data.getLong("ph_pb_g_id"));
					}
					check = true;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}
}