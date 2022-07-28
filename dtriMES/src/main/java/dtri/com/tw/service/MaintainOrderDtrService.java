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
import dtri.com.tw.db.entity.Customer;
import dtri.com.tw.db.entity.MaintenanceDetail;
import dtri.com.tw.db.entity.MaintenanceOrder;
import dtri.com.tw.db.entity.MaintenanceRegister;
import dtri.com.tw.db.entity.MaintenanceUnit;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.CustomerDao;
import dtri.com.tw.db.pgsql.dao.MaintenanceDetailDao;
import dtri.com.tw.db.pgsql.dao.MaintenanceOrderDao;
import dtri.com.tw.db.pgsql.dao.MaintenanceRegisterDao;
import dtri.com.tw.db.pgsql.dao.MaintenanceUnitDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class MaintainOrderDtrService {
	@Autowired
	private MaintenanceUnitDao unitDao;
	@Autowired
	private MaintenanceOrderDao orderDao;
	@Autowired
	private MaintenanceDetailDao detailDao;
	@Autowired
	private MaintenanceRegisterDao registerDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private ProductionHeaderDao headerDao;

	@Autowired
	private CustomerDao customerDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<MaintenanceUnit> mUnits = new ArrayList<MaintenanceUnit>();
		// List<MaintenanceOrder> mOrder = new ArrayList<MaintenanceOrder>();
		List<String> moids = new ArrayList<String>();// 有相關的資料
		List<MaintenanceOrder> fatherOrder = new ArrayList<MaintenanceOrder>();
		// List<MaintenanceOrder> mOrder_sons = new ArrayList<MaintenanceOrder>();
		List<Customer> customers = new ArrayList<Customer>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("moid").descending());
		String search_mo_id = null;
		String search_md_mr_sn = null;
		String search_md_statement = null;

		String search_md_check = null;
		String search_mr_pb_type = null;

		Date mosramdate = null;
		Date moeramdate = null;
		Date mrspbsysmdate = null;
		Date mrepbsysmdate = null;
		String search_mo_s_ram_date = null;
		String search_mo_e_ram_date = null;
		String search_mr_s_pb_sys_m_date = null;
		String search_mr_e_pb_sys_m_date = null;

		// 功能-名稱編譯
		// 維修單據
		String mo_id = "維修單ID", mo_c_id = "客戶ID", //
				mo_check = "單據狀態", mo_from = "來源", //
				mo_e_date = "完成日", mo_s_date = "寄出日", mo_g_date = "收到日", mo_ram_date = "申請日";
		// 維修細節
		String md_id = "維修(子序號)", /* md_mo_id = "維修單", */ //
				md_mr_sn = "品件序號", md_u_qty = "品件數量", //
				md_mu_id = "分配單位ID", md_statement = "描述問題", //
				md_true = "實際問題", md_experience = "維修心得", md_check = "檢核狀態", //
				md_svg = "圖片", md_finally = "修復結果", md_u_finally = "修復員";
		// 維修登記(物件)
		String /* mr_sn = "品件序號", */ mr_c_sn = "客戶品件(序號)", //
				mr_pr_id = "製令單", mr_pr_p_qty = "製令數量", //
				mr_pr_p_model = "品件型號", mr_pr_w_years = "保固年份", //
				mr_pb_sys_m_date = "生產日期", mr_pb_type = "品件類型", //
				mr_v = "版本號", mr_f_ok = "品件狀態", mr_expired = "保固內?";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", /* sys_sort = "排序", sys_ver = "版本", */ sys_status = "狀態", //
				sys_header = "群組", ui_group_id = "UI_Group_ID";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t(sys_header, "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t(ui_group_id, "150px", FFM.Wri.W_N));// 群組專用-必須放前面
			// 維修單據
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_id", FFS.h_t(mo_id, "210px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_c_id", FFS.h_t(mo_c_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_check", FFS.h_t(mo_check, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_from", FFS.h_t(mo_from, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_e_date", FFS.h_t(mo_e_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_s_date", FFS.h_t(mo_s_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_g_date", FFS.h_t(mo_g_date, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mo_ram_date", FFS.h_t(mo_ram_date, "150px", FFM.Wri.W_Y));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "100px", FFM.Wri.W_Y));

			// 維修細節
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_id", FFS.h_t(md_id, "200px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_mo_id",
			// FFS.h_t(md_mo_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_mr_sn", FFS.h_t(md_mr_sn, "200px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_mu_id", FFS.h_t(md_mu_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_finally", FFS.h_t(md_finally, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_u_finally", FFS.h_t(md_u_finally, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_statement", FFS.h_t(md_statement, "250px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_svg", FFS.h_t(md_svg, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_true",
			// FFS.h_t(md_true, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_experience",
			// FFS.h_t(md_experience, "150px", FFM.Wri.W_N));

			// 產品資料
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_c_sn",
			// FFS.h_t(mr_c_sn, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pr_id", FFS.h_t(mr_pr_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pr_p_qty", FFS.h_t(mr_pr_p_qty, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pr_p_model",
			// FFS.h_t(mr_pr_p_model, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_expired",
			// FFS.h_t(mr_expired, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pr_w_years",
			// FFS.h_t(mr_pr_w_years, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pb_sys_m_date",
			// FFS.h_t(mr_pb_sys_m_date, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pb_type",
			// FFS.h_t(mr_pb_type, "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_v", FFS.h_t(mr_v,
			// "150px", FFM.Wri.W_N));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_f_ok",
			// FFS.h_t(mr_f_ok, "150px", FFM.Wri.W_N));

			bean.setHeader(new JSONObject().put("search_header", object_header));

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();
			// 維修單據
			obj_m = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-2", false, n_val, "mo_id", mo_id));
			customers = customerDao.findAll();
			for (Customer one : customers) {
				s_val.put((new JSONObject()).put("value", one.getCcname()).put("key", one.getCid()));
			}
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "3", FFM.Wri.W_N, "col-md-2", true, s_val, "mo_c_id", mo_c_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-1", false, n_val, "md_id", md_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "md_mr_sn", md_mr_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1", "1", FFM.Wri.W_N, "col-md-1", true, n_val, "md_u_qty", md_u_qty));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "未結單").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已結單").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, s_val, "mo_check", mo_check));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", 4));
			s_val.put((new JSONObject()).put("value", "已寄出(結單)").put("key", 5));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val, "md_check", md_check));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "RMA售後").put("key", "RMA"));
			s_val.put((new JSONObject()).put("value", "DTR廠內").put("key", "DTR"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "DTR", FFM.Wri.W_N, "col-md-1", true, s_val, "mo_from", mo_from));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mo_e_date", mo_e_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mo_s_date", mo_s_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mo_g_date", mo_g_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mo_ram_date", mo_ram_date));

			// 單據細節
			s_val = new JSONArray();
			mUnits = unitDao.findAllByMaintenanceUnit(0L, null, null, true, null);
			for (MaintenanceUnit oneUnit : mUnits) {
				s_val.put((new JSONObject()).put("value", oneUnit.getMugname()).put("key", oneUnit.getMuid()));
			}
			s_val.put((new JSONObject()).put("value", "全單位").put("key", 0));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, s_val, "md_mu_id", md_mu_id));
			// 第二行
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "修復中").put("key", false));
			s_val.put((new JSONObject()).put("value", "已修復").put("key", true));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "false", "false", FFM.Wri.W_N, "col-md-1", true, s_val, "md_finally", md_finally));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "md_u_finally", md_u_finally));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "修復品").put("key", true));
			s_val.put((new JSONObject()).put("value", "故障品").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "false", FFM.Wri.W_Y, "col-md-1", true, s_val, "mr_f_ok", mr_f_ok));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "mr_pr_id", mr_pr_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "mr_pr_p_qty", mr_pr_p_qty));

			// 產品或是物件 登記資訊
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "mr_c_sn", mr_c_sn));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "mr_pr_p_model", mr_pr_p_model));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "保固期內").put("key", true));
			s_val.put((new JSONObject()).put("value", "保固過期").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, s_val, "mr_expired", mr_expired));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "mr_pr_w_years", mr_pr_w_years));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.DATE, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "mr_pb_sys_m_date", mr_pb_sys_m_date));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "產品", FFM.Wri.W_Y, "col-md-1", true, s_val, "mr_pb_type", mr_pb_type));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "mr_v", mr_v));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "開啟(正常)").put("key", "0"));
			s_val.put((new JSONObject()).put("value", "關閉(作廢)").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val, "sys_status", sys_status));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", sys_header));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val, "md_statement", md_statement));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "md_true", md_true));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "md_experience", md_experience));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.IMG, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "md_svg", md_svg));
			bean.setCell_modify(obj_m);

			// 放入群主指定 [(key)](modify/Create/Delete) 格式
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-2", "mo_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "mo_c_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "mo_e_date", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "mo_s_date", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "mo_g_date", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-2", "mo_ram_date", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "mo_from", "DTR"));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "mo_check", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "md_check", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_experience", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_statement", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_true", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_svg", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_mr_sn", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_finally", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_u_finally", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pr_id", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pr_p_qty", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pr_p_model", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_c_sn", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_expired", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pr_w_years", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pb_sys_m_date", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_pb_type", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_v", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "mr_f_ok", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "md_u_qty", "0"));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_status", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_header", "true"));

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// 維修單
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "mo_id", mo_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "md_mr_sn", md_mr_sn, n_val));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", 4));
			s_val.put((new JSONObject()).put("value", "已寄出(結單)").put("key", 5));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "md_check", md_check, s_val));

			// 維修品登記
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "mr_pb_type", mr_pb_type, s_val));

			// 維修單細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "md_statement", md_statement, n_val));

			// 時間區間
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "mo_s_ram_date", mo_ram_date + "(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "mo_e_ram_date", mo_ram_date + "(終)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "mr_s_pb_sys_m_date", mr_pb_sys_m_date + "(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "mr_e_pb_sys_m_date", mr_pb_sys_m_date + "(終)", n_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			search_mo_id = body.getJSONObject("search").getString("mo_id");
			search_mo_id = search_mo_id.equals("") ? null : search_mo_id;
			search_md_mr_sn = body.getJSONObject("search").getString("md_mr_sn");
			search_md_mr_sn = search_md_mr_sn.equals("") ? null : search_md_mr_sn;
			search_md_check = body.getJSONObject("search").getString("md_check");
			search_md_check = search_md_check.equals("") ? null : search_md_check;
			search_mr_pb_type = body.getJSONObject("search").getString("mr_pb_type");
			search_mr_pb_type = search_mr_pb_type.equals("") ? null : search_mr_pb_type;
			search_md_statement = body.getJSONObject("search").getString("md_statement");
			search_md_statement = search_md_statement.equals("") ? null : search_md_statement;

			search_mo_s_ram_date = body.getJSONObject("search").getString("mo_s_ram_date");
			search_mo_e_ram_date = body.getJSONObject("search").getString("mo_e_ram_date");
			mosramdate = search_mo_s_ram_date.equals("") ? null : Fm_Time.toDateTime(search_mo_s_ram_date);
			moeramdate = search_mo_e_ram_date.equals("") ? null : Fm_Time.toDateTime(search_mo_e_ram_date);

			search_mr_s_pb_sys_m_date = body.getJSONObject("search").getString("mr_s_pb_sys_m_date");
			search_mr_e_pb_sys_m_date = body.getJSONObject("search").getString("mr_e_pb_sys_m_date");
			mrspbsysmdate = search_mr_s_pb_sys_m_date.equals("") ? null : Fm_Time.toDateTime(search_mr_s_pb_sys_m_date);
			mrepbsysmdate = search_mr_e_pb_sys_m_date.equals("") ? null : Fm_Time.toDateTime(search_mr_e_pb_sys_m_date);

		}

		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		// 父類別物件
		moids = orderDao.findAllByMaintenanceOrder(//
				search_mo_id, search_md_mr_sn, search_md_check, search_mr_pb_type, //
				search_md_statement, mosramdate, moeramdate, //
				mrspbsysmdate, mrepbsysmdate, page_r);

		//
		fatherOrder = orderDao.findAllByMaintenanceOrder(moids);
		fatherOrder.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getMoid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_id", one.getMoid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_c_id", one.getMocid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_check", one.getMocheck());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_from", one.getMofrom());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_e_date", one.getMoedate() == null ? "" : Fm_Time.to_y_M_d(one.getMoedate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_s_date", one.getMosdate() == null ? "" : Fm_Time.to_y_M_d(one.getMosdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_g_date", one.getMogdate() == null ? "" : Fm_Time.to_y_M_d(one.getMogdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_ram_date", one.getMoramdate() == null ? "" : Fm_Time.to_y_M_d(one.getMoramdate()));

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());

			object_bodys.put(object_body);
			// 準備子類別容器
			object_bodys_son.put(one.getMoid() + "", new JSONArray());
		});
		bean.setBody(new JSONObject().put("search", object_bodys));

		// 子類別物件
		fatherOrder.forEach(one -> {
			// 問題細節
			one.getDetails().forEach(x_son -> {
				JSONObject object_son = new JSONObject();

				int ord = 0;
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", false);// 群組專用-必須放前面
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", x_son.getOrder().getMoid());// 群組專用-必須放前面
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_id", one.getMoid());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_c_id", one.getMocid());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_check", one.getMocheck());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_from", one.getMofrom());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_e_date", one.getMoedate() == null ? "" : Fm_Time.to_y_M_d(one.getMoedate()));
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_s_date", one.getMosdate() == null ? "" : Fm_Time.to_y_M_d(one.getMosdate()));
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_g_date", one.getMogdate() == null ? "" : Fm_Time.to_y_M_d(one.getMogdate()));
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mo_ram_date", one.getMoramdate() == null ? "" : Fm_Time.to_y_M_d(one.getMoramdate()));

				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());

				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_id", x_son.getMdid().split("-")[1]);
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_mr_sn", x_son.getRegister().getMrsn());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_mu_id", x_son.getMdmuid());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_finally", x_son.getMdfinally());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_u_finally", x_son.getMdufinally());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_statement", x_son.getMdstatement());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_svg", x_son.getMdsvg());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_pr_id", x_son.getRegister().getMrprid());
				object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_pr_p_qty", x_son.getRegister().getMrprpqty());

				object_bodys_son.getJSONArray(x_son.getOrder().getMoid() + "").put(object_son);
			});
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
		try {

			JSONArray list = body.getJSONArray("create");
			JSONArray list_chok = new JSONArray();
			// 群組 統一資料用
			Long md_id_nb = 0L;
			Long md_mu_id = 0L;// 指派單位
			String mo_id = "";
			MaintenanceOrder obj_h = new MaintenanceOrder();// 維修單資料
			MaintenanceOrder obj = new MaintenanceOrder();// 維修單資料-共用
			// 如果沒資料則不做事
			if (list.length() == 0) {
				return true;
			}
			// 先行檢核
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				if (data.getBoolean("sys_header")) {
					list_chok.put(data);
				} else {
					// Step1.帶入 產品資訊
					List<ProductionBody> bodys = bodyDao.findAllByPbbsn(data.getString("md_mr_sn"));
					if (!data.getString("md_mr_sn").equals("") && bodys.size() == 1) {
						// Step2.帶入 製令資訊
						List<ProductionHeader> headers = headerDao.findAllByPhpbgid(bodys.get(0).getPbgid());
						if (headers.size() == 1) {
							data.put("mr_pr_id", headers.get(0).getProductionRecords().getPrid());
							data.put("mr_pr_p_qty", headers.get(0).getProductionRecords().getPrpquantity());
							data.put("mr_pr_w_years", headers.get(0).getProductionRecords().getPrwyears());
							data.put("mr_pr_p_model", headers.get(0).getProductionRecords().getPrpmodel());
							data.put("mr_pb_sys_m_date", Fm_Time.to_yMd_Hms(bodys.get(0).getSysmdate()));// 產品製造日期 = 產品最後修改時間

						} else {
							return false;
						}

						list_chok.put(data);
					} else {
						return false;
					}
				}
			}

			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				// List<MaintenanceDetail> details = new ArrayList<MaintenanceDetail>();
				MaintenanceDetail obj_b = new MaintenanceDetail();// 維修問題清單
				MaintenanceRegister register = new MaintenanceRegister();// 品件登記

				// 共用
				obj.setSysnote("");
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSyscdate(new Date());
				obj.setSysmdate(new Date());
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());

				// 新建 維修單-父類別
				if (data.getBoolean("sys_header")) {
					// 存入資料(間戳記)
					mo_id = data.getString("mo_from") + new Date().getTime();
					obj_h = new MaintenanceOrder();
					// 維修單資料
					obj_h.setMoid(mo_id);
					obj_h.setMocid(data.getLong("mo_c_id"));
					obj_h.setMocheck(data.getInt("mo_check"));
					obj_h.setMofrom(data.getString("mo_from"));
					obj_h.setMoramdate(new Date());
					obj_h.setDetails(null);

					obj_h.setSysnote(obj.getSysnote());
					obj_h.setSysstatus(obj.getSysstatus());
					obj_h.setSyscdate(obj.getSyscdate());
					obj_h.setSysmdate(obj.getSysmdate());
					obj_h.setSysmuser(obj.getSysmuser());
					obj_h.setSyscuser(obj.getSyscuser());
					obj_h.setSysheader(true);
					orderDao.save(obj_h);
					md_mu_id = (data.has("md_mu_id") && !data.get("md_mu_id").equals("")) ? data.getLong("md_mu_id") : 0L;

				} else {

					// 新建 單據內容+登記產品 登記子類別
					// 產品登記
					register = new MaintenanceRegister();
					register.setMrsn(data.getString("md_mr_sn"));
					register.setMrcsn(data.getString("mr_c_sn"));
					register.setMrprid(data.getString("mr_pr_id"));
					register.setMrprpqty(data.getInt("mr_pr_p_qty"));
					register.setMrprpmodel(data.getString("mr_pr_p_model"));
					register.setMrexpired(true);// mr_pr_w_years
					register.setMrprwyears(data.getInt("mr_pr_w_years"));
					register.setMrpbsysmdate(Fm_Time.toDateTime(data.getString("mr_pb_sys_m_date")));
					register.setMrpbtype(data.getString("mr_pb_type"));
					register.setMrv(data.getString("mr_v"));
					register.setMrfok(data.getBoolean("mr_f_ok"));
					register.setSysnote(obj.getSysnote());
					register.setSysstatus(obj.getSysstatus());
					register.setSyscdate(obj.getSyscdate());
					register.setSysmdate(obj.getSysmdate());
					register.setSysmuser(obj.getSysmuser());
					register.setSyscuser(obj.getSyscuser());
					registerDao.save(register);
					// 維修單細節
					String md_id = "D" + String.format("%03d", md_id_nb++);
					obj_b.setMdid(mo_id + '-' + md_id);
					obj_b.setMdstatement(data.getString("md_statement"));
					obj_b.setMdmuid(md_mu_id == 0L ? data.getLong("md_mu_id") : md_mu_id);
					obj_b.setMduqty(data.getInt("md_u_qty"));
					obj_b.setMdtrue("");
					obj_b.setMdexperience("");
					obj_b.setMdsvg(data.getString("md_svg"));
					obj_b.setMdcheck(data.getInt("md_check"));
					obj_b.setMdfinally("");
					obj_b.setMdufinally("");
					obj_b.setOrder(obj_h);

					obj_b.setSysnote(obj.getSysnote());
					obj_b.setSysstatus(obj.getSysstatus());
					obj_b.setSyscdate(obj.getSyscdate());
					obj_b.setSysmdate(obj.getSysmdate());
					obj_b.setSysmuser(obj.getSysmuser());
					obj_b.setSyscuser(obj.getSyscuser());
					obj_b.setSysheader(false);
					obj_b.setRegister(register);
					detailDao.save(obj_b);

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
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("save_as");
			JSONArray list_check = new JSONArray();
			// 群組 統一資料用
			Long md_id_nb = 0L;
			Long md_mu_id = 0L;// 指派單位
			String mo_id = "";
			MaintenanceOrder obj_h = new MaintenanceOrder();// 維修單資料
			MaintenanceOrder obj = new MaintenanceOrder();// 維修單資料-共用
			// 如果沒資料則不做事
			if (list.length() == 0) {
				return true;
			}
			// 先行檢核
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				if (data.getBoolean("sys_header")) {
					list_check.put(data);
				} else {
					// Step1.帶入 產品資訊
					List<ProductionBody> bodys = bodyDao.findAllByPbbsn(data.getString("md_mr_sn"));
					if (!data.getString("md_mr_sn").equals("") && bodys.size() == 1) {
						// Step2.帶入 製令資訊
						List<ProductionHeader> headers = headerDao.findAllByPhpbgid(bodys.get(0).getPbgid());
						if (headers.size() == 1) {
							data.put("mr_pr_id", headers.get(0).getProductionRecords().getPrid());
							data.put("mr_pr_p_qty", headers.get(0).getProductionRecords().getPrpquantity());
							data.put("mr_pr_w_years", headers.get(0).getProductionRecords().getPrwyears());
							data.put("mr_pr_p_model", headers.get(0).getProductionRecords().getPrpmodel());
							data.put("mr_pb_sys_m_date", Fm_Time.to_yMd_Hms(bodys.get(0).getSysmdate()));
							// 產品製造日期 = 產品最後修改時間
						} else {
							return false;
						}
						list_check.put(data);
					} else {
						return false;
					}
				}
			}

			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				// List<MaintenanceDetail> details = new ArrayList<MaintenanceDetail>();
				MaintenanceDetail obj_b = new MaintenanceDetail();// 維修問題清單
				MaintenanceRegister register = new MaintenanceRegister();// 品件登記

				// 共用
				obj.setSysnote("");
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSyscdate(new Date());
				obj.setSysmdate(new Date());
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());

				// 新建 維修單-父類別
				if (data.getBoolean("sys_header")) {
					// 存入資料(間戳記)
					mo_id = data.getString("mo_from") + new Date().getTime();
					obj_h = new MaintenanceOrder();
					// 維修單資料
					obj_h.setMoid(mo_id);
					obj_h.setMocid(data.getLong("mo_c_id"));
					obj_h.setMocheck(data.getInt("mo_check"));
					obj_h.setMofrom(data.getString("mo_from"));
					obj_h.setMoramdate(new Date());
					obj_h.setDetails(null);

					obj_h.setSysnote(obj.getSysnote());
					obj_h.setSysstatus(obj.getSysstatus());
					obj_h.setSyscdate(obj.getSyscdate());
					obj_h.setSysmdate(obj.getSysmdate());
					obj_h.setSysmuser(obj.getSysmuser());
					obj_h.setSyscuser(obj.getSyscuser());
					obj_h.setSysheader(true);
					orderDao.save(obj_h);
					md_mu_id = (data.has("md_mu_id") && !data.get("md_mu_id").equals("")) ? data.getLong("md_mu_id") : 0L;

				} else {

					// 新建 單據內容+登記產品 登記子類別
					// 產品登記
					register = new MaintenanceRegister();
					register.setMrsn(data.getString("md_mr_sn"));
					register.setMrcsn(data.getString("mr_c_sn"));
					register.setMrprid(data.getString("mr_pr_id"));
					register.setMrprpqty(data.getInt("mr_pr_p_qty"));
					register.setMrprpmodel(data.getString("mr_pr_p_model"));
					register.setMrexpired(true);// mr_pr_w_years
					register.setMrprwyears(data.getInt("mr_pr_w_years"));
					register.setMrpbsysmdate(Fm_Time.toDateTime(data.getString("mr_pb_sys_m_date")));
					register.setMrpbtype(data.getString("mr_pb_type"));
					register.setMrv(data.getString("mr_v"));
					register.setMrfok(data.getBoolean("mr_f_ok"));
					register.setSysnote(obj.getSysnote());
					register.setSysstatus(obj.getSysstatus());
					register.setSyscdate(obj.getSyscdate());
					register.setSysmdate(obj.getSysmdate());
					register.setSysmuser(obj.getSysmuser());
					register.setSyscuser(obj.getSyscuser());
					registerDao.save(register);
					// 維修單細節
					String md_id = "D" + String.format("%03d", md_id_nb++);
					obj_b.setMdid(mo_id + '-' + md_id);
					obj_b.setMdstatement(data.getString("md_statement"));
					obj_b.setMdmuid(md_mu_id == 0L ? data.getLong("md_mu_id") : md_mu_id);
					obj_b.setMduqty(data.getInt("md_u_qty"));
					obj_b.setMdtrue("");
					obj_b.setMdexperience("");
					obj_b.setMdsvg(data.getString("md_svg"));
					obj_b.setMdfinally("");
					obj_b.setMdufinally("");
					obj_b.setOrder(obj_h);

					obj_b.setSysnote(obj.getSysnote());
					obj_b.setSysstatus(obj.getSysstatus());
					obj_b.setSyscdate(obj.getSyscdate());
					obj_b.setSysmdate(obj.getSysmdate());
					obj_b.setSysmuser(obj.getSysmuser());
					obj_b.setSyscuser(obj.getSyscuser());
					obj_b.setSysheader(false);
					obj_b.setRegister(register);
					detailDao.save(obj_b);

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
			JSONArray list_check = new JSONArray();
			// 群組 統一資料用
			Long md_mu_id = 0L;// 指派單位
			String mo_id = "";
			MaintenanceOrder obj_h = new MaintenanceOrder();// 維修單資料
			MaintenanceOrder obj = new MaintenanceOrder();// 維修單資料-共用
			// 先行檢核
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				if (data.getBoolean("sys_header")) {
					list_check.put(data);
				} else {
					// Step1.帶入 產品資訊
					List<ProductionBody> bodys = bodyDao.findAllByPbbsn(data.getString("md_mr_sn"));
					if (!data.getString("md_mr_sn").equals("") && bodys.size() == 1) {
						// Step2.帶入 製令資訊
						List<ProductionHeader> headers = headerDao.findAllByPhpbgid(bodys.get(0).getPbgid());
						if (headers.size() == 1) {
							data.put("mr_pr_id", headers.get(0).getProductionRecords().getPrid());
							data.put("mr_pr_p_qty", headers.get(0).getProductionRecords().getPrpquantity());
							data.put("mr_pr_w_years", headers.get(0).getProductionRecords().getPrwyears());
							data.put("mr_pr_p_model", headers.get(0).getProductionRecords().getPrpmodel());
							data.put("mr_pb_sys_m_date", Fm_Time.to_yMd_Hms(bodys.get(0).getSysmdate()));
							// 產品製造日期 = 產品最後修改時間
						} else {
							return false;
						}
						list_check.put(data);
					} else {
						return false;
					}
				}
			}
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				MaintenanceDetail obj_b = new MaintenanceDetail();// 維修問題清單
				// MaintenanceRegister register = new MaintenanceRegister();// 品件登記

				// 共用
				obj.setSysnote("");
				obj.setSysstatus(data.getInt("sys_status"));
				obj.setSyscdate(new Date());
				obj.setSysmdate(new Date());
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());

				// 維修單-父類別
				if (data.getBoolean("sys_header")) {
					// 存入資料(間戳記)
					mo_id = data.getString("mo_id");
					obj_h = new MaintenanceOrder();
					ArrayList<MaintenanceOrder> obj_hs = orderDao.findAllByMoid(mo_id);
					// 如果 維修單禁止重複
					if (obj_hs.size() == 1) {
						// 維修單資料
						obj_h = obj_hs.get(0);
						obj_h.setMocid(data.getLong("mo_c_id"));
						obj_h.setMocheck(data.getInt("mo_check"));
						obj_h.setMoramdate(new Date());
						obj_h.setSysnote(obj.getSysnote());
						obj_h.setSysstatus(obj.getSysstatus());
						obj_h.setSysmdate(obj.getSysmdate());
						obj_h.setSysmuser(obj.getSysmuser());
						obj_h.setSysheader(true);
						orderDao.save(obj_h);
						md_mu_id = (data.has("md_mu_id") && !data.get("md_mu_id").equals("")) ? data.getLong("md_mu_id") : 0L;
					}

				} else {
					// 新建 單據內容+登記產品 登記子類別
					// 產品登記?
					ArrayList<MaintenanceRegister> registers = new ArrayList<MaintenanceRegister>();
					registers = registerDao.findAllByMrsn(data.getString("md_mr_sn"));
					if (registers.size() >= 1) {
						MaintenanceRegister register = registers.get(0);
						register.setSysmdate(obj.getSysmdate());
						register.setSysmuser(obj.getSysmuser());
						registerDao.save(register);
					} else {
						MaintenanceRegister register = new MaintenanceRegister();// 品件登記
						register.setMrsn(data.getString("md_mr_sn"));
						register.setMrcsn(data.getString("mr_c_sn"));
						register.setMrprid(data.getString("mr_pr_id"));
						register.setMrprpqty(data.getInt("mr_pr_p_qty"));
						register.setMrprpmodel(data.getString("mr_pr_p_model"));
						register.setMrexpired(true);// mr_pr_w_years
						register.setMrprwyears(data.getInt("mr_pr_w_years"));
						register.setMrpbsysmdate(Fm_Time.toDateTime(data.getString("mr_pb_sys_m_date")));
						register.setMrpbtype(data.getString("mr_pb_type"));
						register.setMrv(data.getString("mr_v"));
						register.setMrfok(data.getBoolean("mr_f_ok"));
						register.setSysnote(obj.getSysnote());
						register.setSysstatus(obj.getSysstatus());
						register.setSysmdate(obj.getSysmdate());
						register.setSysmuser(obj.getSysmuser());
						registerDao.save(register);
					}

					// 維修單細節
					String md_id = data.getString("md_id");
					ArrayList<MaintenanceDetail> obj_bs = detailDao.findAllByMdid(mo_id + '-' + md_id);
					registers = registerDao.findAllByMrsn(data.getString("md_mr_sn"));
					if (obj_bs.size() >= 1) {
						obj_b = obj_bs.get(0);
						obj_b.setMdstatement(data.getString("md_statement"));
						obj_b.setMdmuid(md_mu_id == 0L ? data.getLong("md_mu_id") : md_mu_id);
						obj_b.setMduqty(data.getInt("md_u_qty"));
						obj_b.setMdtrue("");
						obj_b.setMdexperience("");
						obj_b.setMdsvg(data.getString("md_svg"));
						obj_b.setMdfinally("");
						obj_b.setMdufinally("");
						obj_b.setOrder(obj_h);
					}

					obj_b.setSysnote(obj.getSysnote());
					obj_b.setSysstatus(obj.getSysstatus());
					obj_b.setSysmdate(obj.getSysmdate());
					obj_b.setSysmuser(obj.getSysmuser());
					obj_b.setSysheader(false);
					obj_b.setRegister(registers.get(0));
					detailDao.save(obj_b);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			check = false;
		}
		check = true;
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
				JSONObject data = (JSONObject) one;
				if (data.getBoolean("sys_header")) {
					// 父類別 關聯全清除
					orderDao.deleteByMoid(data.getString("mo_id"));
				} else {
					detailDao.deleteByMdid(data.getString("mo_id") + "-" + data.getString("md_id"));
					// registerDao.deleteByDetails(data.getString("md_mr_sn"));//不能刪除
				}
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// ==============客製化==============

	// 更新/新增 資料清單
	@Transactional
	public boolean updateDataCustomized(PackageBean reBean, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONObject list = body.getJSONObject("modify");
			JSONArray details = new JSONArray(list.getJSONArray("detail"));
			JSONObject order = list.getJSONObject("order");
			List<String> moids = new ArrayList<String>();
			// 客戶資料
			String c_c_name = order.getString("c_c_name").equals("") ? null : order.getString("c_c_name");
			String c_name = order.getString("c_name").equals("") ? null : order.getString("c_name");
			String c_address = order.getString("c_address");
			String c_tex = order.getString("c_tex").equals("") ? null : order.getString("c_tex");
			String c_fax = order.getString("c_fax");
			// 維修單
			String mo_id = order.getString("mo_id");
			order.put("mo_check", order.has("mo_check") ? order.getString("mo_check") : "0");
			Integer mo_check = order.getString("mo_check").equals("") ? 0 : order.getInt("mo_check");
			MaintenanceOrder mO_one = new MaintenanceOrder();
			Customer c_one = new Customer();

			// ====[資料檢核]====
			// 維修單單頭-資料
			// Step0.資料矯正
			if (!mo_id.equals("")) {
				moids.add(mo_id);
			} else {
				moids.add("");
			}

			// 維修單細節-資料
			for (int ch_d = 0; ch_d < details.length(); ch_d++) {
				JSONObject data = (JSONObject) details.getJSONObject(ch_d);

				// Step1.帶入 產品資訊
				List<ProductionBody> bodys = bodyDao.findAllByPbbsn(data.getString("mr_sn"));
				if (!data.getString("mr_sn").equals("") && bodys.size() == 1) {
					// Step2.帶入 製令資訊
					List<ProductionHeader> headers = headerDao.findAllByPhpbgid(bodys.get(0).getPbgid());
					if (headers.size() == 1) {
						data.put("mr_pr_id", headers.get(0).getProductionRecords().getPrid());
						data.put("mr_pr_p_qty", headers.get(0).getProductionRecords().getPrpquantity());
						data.put("mr_pr_w_years", headers.get(0).getProductionRecords().getPrwyears());
						data.put("mr_pr_p_model", headers.get(0).getProductionRecords().getPrpmodel());
						data.put("mr_pb_sys_m_date", Fm_Time.to_yMd_Hms(bodys.get(0).getSysmdate()));// 產品製造日期 = 產品最後修改時間
					} else {
						reBean.autoMsssage("WK004");
						check = false;
						return check;
					}
					// Step3.檢查資訊正確性
					data.put("mr_v", data.has("mr_v") ? data.getString("mr_v") : "");
					data.put("md_check", data.getString("md_check").equals("") ? 0 : data.getInt("md_check"));
					data.put("md_mu_id", data.getString("md_mu_id").equals("") ? 0L : data.getLong("md_mu_id"));
					data.put("mr_pb_type", data.getString("mr_pb_type").equals("") ? "產品" : data.getString("mr_pb_type"));
					data.put("md_statement", data.getString("md_statement").equals("") ? "天啊!我忘了打問題敘述" : data.getString("md_statement"));
					details.put(ch_d, data);
				} else {
					reBean.autoMsssage("WK004");
					check = false;
					return check;
				}
			}
			// ====[資料更新]====
			// 客戶
			ArrayList<Customer> customers = customerDao.findAllByCustomer(0L, c_c_name, null, 0, null);
			// 維修單
			ArrayList<MaintenanceOrder> orders = orderDao.findAllByMaintenanceOrder(moids);
			if (!mo_id.equals("") && orders.size() == 0) {
				reBean.autoMsssage("MT003");
				check = false;
				return check;
			}
			// 共用
			mO_one.setSysnote("");
			mO_one.setSysstatus(0);
			mO_one.setSyscdate(new Date());
			mO_one.setSysmdate(new Date());
			mO_one.setSysmuser(user.getSuaccount());
			mO_one.setSyscuser(user.getSuaccount());

			// Step4.客戶 新增/修改?
			if (customers.size() > 0) {// 修改
				c_one = customers.get(0);
				c_one.setCaddress(c_address);
				c_one.setCname(c_name);
				c_one.setCfax(c_fax);
				c_one.setCtex(c_tex);
				c_one.setSysmdate(mO_one.getSysmdate());
				c_one.setSysmuser(mO_one.getSysmuser());
				customerDao.save(c_one);
			} else {// 新增
				c_one.setCaddress(c_address);
				c_one.setCname(c_name);
				c_one.setCcname(c_c_name);
				c_one.setCfax(c_fax);
				c_one.setCtex(c_tex);
				c_one.setSyscdate(mO_one.getSyscdate());
				c_one.setSyscuser(mO_one.getSyscuser());
				c_one.setSysmdate(mO_one.getSysmdate());
				c_one.setSysmuser(mO_one.getSysmuser());
				c_one.setSysstatus(mO_one.getSysstatus());
				customerDao.save(c_one);
			}
			// Step5.維修單 新增/修改?
			if (orders.size() > 0) {
				// 5-1.維修單-修改
				mO_one = orders.get(0);
				// 5-2.不可改已結單
				if (mO_one.getMocheck() < 1) {
					mO_one.setMocheck(mo_check);// 0=未結單 1= 結單
					mO_one.setSysmdate(new Date());
					mO_one.setSysmuser(user.getSuaccount());
					customers = customerDao.findAllByCustomer(0L, c_c_name, null, 0, null);
					mO_one.setMocid(customers.get(0).getCid());
					if (mo_check == 1) {
						mO_one.setMosdate(new Date());
					}

					int md_id_nb = details.length();
					// 新資料 比對 舊資料
					for (int d_new = 0; d_new < details.length(); d_new++) {
						JSONObject new_deatil = (JSONObject) details.get(d_new);
						Boolean check_same = true;
						for (int d_old = 0; d_old < mO_one.getDetails().size(); d_old++) {
							MaintenanceDetail old_detail = mO_one.getDetails().get(d_old);
							// 5-3-1.同資料(更新)Ex :DTR1658733889337-D001
							if (old_detail.getMdid().equals(mo_id + "-" + new_deatil.get("md_id"))) {
								// 5-3-2. 尚未[檢核] 前可修改錯誤敘述
								MaintenanceDetail detail = mO_one.getDetails().get(d_old);
								if (mO_one.getDetails().get(d_old).getMdcheck() < 1) {
									detail.getRegister().setMrcsn(new_deatil.getString("mr_c_sn"));
									detail.getRegister().setMrexpired(new_deatil.getBoolean("mr_expired"));
									detail.getRegister().setMrpbtype(new_deatil.getString("mr_pb_type"));
									detail.setMduqty(new_deatil.getInt("md_u_qty"));
									detail.setMdcheck(new_deatil.getInt("md_check"));
									detail.setMdstatement(new_deatil.getString("md_statement"));
									detail.setMdmuid(new_deatil.getLong("md_mu_id"));
									detailDao.save(detail);
									if (new_deatil.getInt("md_check") == 1) {
										mO_one.setMogdate(new Date());
									}
									// 已經進入維修階段 不可異動
								} else if (mO_one.getDetails().get(d_old).getMdcheck() > 2) {
									// 不可修改
									reBean.autoMsssage("MT002");
									check = false;
									return check;
								}
								check_same = false;
								break;
							}
						}
						// 5-4.維修單-不同資料(添加)
						if (check_same) {
							// 新建 單據內容+登記產品 登記子類別
							// 產品登記
							MaintenanceRegister register = new MaintenanceRegister();
							register.setMrsn(new_deatil.getString("mr_sn"));
							register.setMrcsn(new_deatil.getString("mr_c_sn"));
							register.setMrprid(new_deatil.getString("mr_pr_id"));
							register.setMrprpqty(new_deatil.getInt("mr_pr_p_qty"));
							register.setMrprpmodel(new_deatil.getString("mr_pr_p_model"));
							register.setMrexpired(true);// mr_pr_w_years
							register.setMrprwyears(new_deatil.getInt("mr_pr_w_years"));
							register.setMrpbsysmdate(Fm_Time.toDateTime(new_deatil.getString("mr_pb_sys_m_date")));
							register.setMrpbtype(new_deatil.getString("mr_pb_type"));
							register.setMrv(new_deatil.getString("mr_v"));
							register.setMrfok(true);
							register.setSysnote("");
							register.setSysstatus(0);
							register.setSyscdate(mO_one.getSyscdate());
							register.setSysmdate(mO_one.getSysmdate());
							register.setSysmuser(mO_one.getSysmuser());
							register.setSyscuser(mO_one.getSyscuser());
							registerDao.save(register);

							MaintenanceDetail add_detail = new MaintenanceDetail();
							String md_id = "D" + String.format("%03d", md_id_nb++);
							// 檢查重複?->重複則->下一筆新序號
							Boolean check_rep = true;
							while (check_rep) {
								if (detailDao.findAllByMdid(mo_id + '-' + md_id).size() > 0) {
									md_id = "D" + String.format("%03d", md_id_nb++);
								} else {
									check_rep = false;
								}
							}
							add_detail.setMdid(mo_id + '-' + md_id);
							add_detail.setMdstatement(new_deatil.getString("md_statement"));
							add_detail.setMdmuid(new_deatil.getLong("md_mu_id"));
							add_detail.setMduqty(new_deatil.getInt("md_u_qty"));
							add_detail.setMdtrue("");
							add_detail.setMdexperience("");
							add_detail.setMdsvg(null);
							add_detail.setMdcheck(new_deatil.getInt("md_check"));
							add_detail.setMdfinally("");
							add_detail.setMdufinally("");
							add_detail.setOrder(mO_one);
							add_detail.setRegister(register);
							detailDao.save(add_detail);
						}
					}
					orderDao.save(mO_one);
				} else {
					reBean.autoMsssage("MT001");
					check = false;
					return check;
				}
			} else {
				// 5-3.維修單-新增
				int md_id_nb = 0;
				customers = customerDao.findAllByCustomer(0L, c_c_name, null, 0, null);
				mO_one.setMocid(customers.get(0).getCid());
				MaintenanceOrder obj_h = new MaintenanceOrder();// 維修單資料
				MaintenanceDetail obj_b = new MaintenanceDetail();// 維修問題清單
				MaintenanceOrder obj = new MaintenanceOrder();// 維修單資料-共用
				// 共用
				obj.setSysnote("");
				obj.setSysstatus(0);
				obj.setSyscdate(new Date());
				obj.setSysmdate(new Date());
				obj.setSysmuser(user.getSuaccount());
				obj.setSyscuser(user.getSuaccount());

				// Order 維修單頭- 存入資料(間戳記)
				mo_id = "DTR" + new Date().getTime();
				obj_h = new MaintenanceOrder();
				// 維修單資料
				obj_h.setMoid(mo_id);
				obj_h.setMocid(customers.get(0).getCid());
				obj_h.setMocheck(order.get("mo_check").equals("") ? 0 : order.getInt("mo_check"));
				obj_h.setMofrom("DTR");
				obj_h.setMoramdate(new Date());
				obj_h.setDetails(null);

				obj_h.setSysnote(obj.getSysnote());
				obj_h.setSysstatus(obj.getSysstatus());
				obj_h.setSyscdate(obj.getSyscdate());
				obj_h.setSysmdate(obj.getSysmdate());
				obj_h.setSysmuser(obj.getSysmuser());
				obj_h.setSyscuser(obj.getSyscuser());
				obj_h.setSysheader(true);
				orderDao.save(obj_h);
				// 新建 單據內容+登記產品 登記子類別
				// 產品登記
				for (Object obj_one : details) {
					JSONObject data = (JSONObject) obj_one;
					MaintenanceRegister register = new MaintenanceRegister();
					register.setMrsn(data.getString("mr_sn"));
					register.setMrcsn(data.getString("mr_c_sn"));
					register.setMrprid(data.getString("mr_pr_id"));
					register.setMrprpqty(data.getInt("mr_pr_p_qty"));
					register.setMrprpmodel(data.getString("mr_pr_p_model"));
					register.setMrexpired(true);// mr_pr_w_years
					register.setMrprwyears(data.getInt("mr_pr_w_years"));
					register.setMrpbsysmdate(Fm_Time.toDateTime(data.getString("mr_pb_sys_m_date")));
					register.setMrpbtype(data.getString("mr_pb_type"));
					register.setMrv(data.getString("mr_v"));
					register.setMrfok(true);
					register.setSysnote(obj.getSysnote());
					register.setSysstatus(obj.getSysstatus());
					register.setSyscdate(obj.getSyscdate());
					register.setSysmdate(obj.getSysmdate());
					register.setSysmuser(obj.getSysmuser());
					register.setSyscuser(obj.getSyscuser());
					registerDao.save(register);
					// 維修單細節
					String md_id = "D" + String.format("%03d", md_id_nb++);
					obj_b.setMdid(mo_id + '-' + md_id);
					obj_b.setMdstatement(data.getString("md_statement"));
					obj_b.setMdmuid(data.getLong("md_mu_id"));
					obj_b.setMduqty(data.getInt("md_u_qty"));
					obj_b.setMdtrue("");
					obj_b.setMdexperience("");
					obj_b.setMdcheck(data.getInt("md_check"));
					obj_b.setMdfinally("");
					obj_b.setMdufinally("");
					obj_b.setOrder(obj_h);

					obj_b.setSysnote(obj.getSysnote());
					obj_b.setSysstatus(obj.getSysstatus());
					obj_b.setSyscdate(obj.getSyscdate());
					obj_b.setSysmdate(obj.getSysmdate());
					obj_b.setSysmuser(obj.getSysmuser());
					obj_b.setSyscuser(obj.getSyscuser());
					obj_b.setSysheader(false);
					obj_b.setRegister(register);
					detailDao.save(obj_b);
				}

				reBean.setCall_bk_vals(reBean.getCall_bk_vals().put("mo_id", mo_id));
			}

			check = true;
		} catch (Exception e) {
			System.out.println(e);
			check = false;
		}
		return check;
	}

	// 過站登記故障代碼 資料清單
	@Transactional
	public boolean setDataCustomized(MaintenanceOrder order, SystemUser user) {
		// 檢查

		// order.

		return false;
	}

	// 取得 - Customized mode當前表單式查詢資料
	public boolean getDataCustomized(PackageBean bean, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		if (bean == null) {
			bean = new PackageBean();
		}
		// 查詢
		String search_c_c_name = "定誼科技";
		String search_c_name = null;
		String search_c_tex = null;
		String search_mo_id = null;
		Long mocid = 0L;

		// 維修單據
		String md_id = "No.", mr_pr_p_model = "Model", mr_sn = "P/N(DTR)", mr_c_sn = "P/N(client)", mr_pb_type = "Type", //
				md_statement = "Failure Description", md_u_qty = "Qty", mr_expired = "Warranty?", md_mu_id = "To whom", md_check = "Status";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = bean.getHeader();
			JSONObject customized_header = new JSONObject();
			int ord = 0;
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_id", FFS.h_t(md_id, "80px", FFM.Wri.W_N));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pr_p_model", FFS.h_t(mr_pr_p_model, "100px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_sn", FFS.h_t(mr_sn, "150px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_c_sn", FFS.h_t(mr_c_sn, "150px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_pb_type", FFS.h_t(mr_pb_type, "90px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_statement", FFS.h_t(md_statement, "350px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_u_qty", FFS.h_t(md_u_qty, "70px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "mr_expired", FFS.h_t(mr_expired, "120px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_mu_id", FFS.h_t(md_mu_id, "120px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "md_check", FFS.h_t(md_check, "130px", FFM.Wri.W_N));
			object_header.put("customized_header", customized_header);

			// 維修單-訊息
			JSONArray s_val = new JSONArray();
			JSONObject object_documents = new JSONObject();
			List<MaintenanceUnit> mUnits = unitDao.findAllByMaintenanceUnit(0L, null, null, true, null);
			for (MaintenanceUnit oneUnit : mUnits) {
				String oneUnit_one = oneUnit.getMugname();
				s_val.put((new JSONObject()).put("value", oneUnit_one).put("key", oneUnit.getMuid()));
			}
			s_val.put((new JSONObject()).put("value", "全單位").put("key", 0));
			object_documents.put("md_mu_id", s_val);
			// 單據狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "未結單").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已結單").put("key", 1));
			object_documents.put("mo_check", s_val);
			// 類型
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_documents.put("mr_pb_type", s_val);
			// 處理狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", 4));
			s_val.put((new JSONObject()).put("value", "已寄出(結單)").put("key", 5));
			object_documents.put("md_check", s_val);
			// 處理狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			object_documents.put("md_check_only", s_val);

			// 處理狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "保固期內").put("key", true));
			s_val.put((new JSONObject()).put("value", "保固過期").put("key", false));
			object_documents.put("mr_expired", s_val);

			// 數字格式
			object_documents.put("md_u_qty", "number");

			object_header.put("customized_documents", object_documents);

			bean.setHeader(object_header);
		} else {
			// 進行-特定查詢
			search_c_c_name = body.getJSONObject("search").getString("c_c_name");
			search_c_c_name = search_c_c_name.equals("") ? null : search_c_c_name;
			search_c_name = body.getJSONObject("search").getString("c_name");
			search_c_name = search_c_name.equals("") ? null : search_c_name;
			search_c_tex = body.getJSONObject("search").getString("c_tex");
			search_c_tex = search_c_tex.equals("") ? null : search_c_tex;
			search_mo_id = body.getJSONObject("search").getString("mo_id");
			search_mo_id = search_mo_id.equals("") ? null : search_mo_id;
			// 尚未指定客戶
			if (search_c_c_name == null && search_c_name == null && search_c_tex == null) {
				ArrayList<MaintenanceOrder> orders = orderDao.findAllByMoid(search_mo_id);
				if (orders.size() >= 1) {
					mocid = orders.get(0).getMocid();
				} else {
					search_c_c_name = "定誼科技";
				}
			}
		}

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		// 維修單-問題清單+產品
		JSONObject object_body = bean.getBody();
		if (object_body == null)
			object_body = new JSONObject();

		// 維修單-客戶
		JSONObject object_customer = new JSONObject();
		JSONObject object_order = new JSONObject();
		JSONArray object_detail = new JSONArray();
		ArrayList<Customer> customers = customerDao.findAllByCustomer(mocid, search_c_c_name, search_c_name, 0, null);
		if (customers.size() >= 1) {
			object_customer.put("c_c_name", customers.get(0).getCcname());// 公司名稱
			object_customer.put("c_name", customers.get(0).getCname());// 客戶名稱
			object_customer.put("c_address", customers.get(0).getCaddress());// 地址
			object_customer.put("c_tex", customers.get(0).getCtex());// 電話
			object_customer.put("c_fax", customers.get(0).getCfax());// 傳真
		}
		object_body.put("customized_customer", object_customer);
		// 維修單
		ArrayList<MaintenanceOrder> orders = orderDao.findAllByMoid(search_mo_id);
		if (orders.size() >= 1) {
			object_order.put("mo_id", orders.get(0).getMoid());
			object_order.put("mo_check", orders.get(0).getMocheck());
			orders.get(0).getDetails().forEach(details -> {
				int ord = 0;
				JSONObject obj = new JSONObject();
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_id", details.getMdid().split("-")[1]);
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_pr_p_model", details.getRegister().getMrprpmodel());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_sn", details.getRegister().getMrsn());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_c_sn", details.getRegister().getMrcsn());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_pb_type", details.getRegister().getMrpbtype());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_statement", details.getMdstatement());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_u_qty", details.getMduqty());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "mr_expired", details.getRegister().getMrexpired());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_mu_id", details.getMdmuid());
				obj.put(FFS.ord((ord += 1), FFM.Hmb.B) + "md_check", details.getMdcheck());
				object_detail.put(obj);
			});
		}

		object_body.put("customized_orders", object_order);
		object_body.put("customized_detail", object_detail);
		bean.setBody(object_body);
		return true;
	}
}
