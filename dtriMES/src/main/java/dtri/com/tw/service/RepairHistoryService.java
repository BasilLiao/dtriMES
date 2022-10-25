package dtri.com.tw.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import dtri.com.tw.db.entity.Customer;
import dtri.com.tw.db.entity.RepairDetail;
import dtri.com.tw.db.entity.RepairOrder;
import dtri.com.tw.db.entity.RepairRegister;
import dtri.com.tw.db.entity.RepairUnit;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.CustomerDao;
import dtri.com.tw.db.pgsql.dao.RepairDetailDao;
import dtri.com.tw.db.pgsql.dao.RepairUnitDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class RepairHistoryService {
	@Autowired
	private RepairUnitDao unitDao;
	@Autowired
	private RepairDetailDao detailDao;
	@Autowired
	private CustomerDao customerDao;
	@Autowired
	EntityManager em;

	// 取得當前 資料清單
	public boolean getData(PackageBean resp, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<RepairUnit> mUnits = new ArrayList<RepairUnit>();
		List<RepairDetail> rdids = new ArrayList<RepairDetail>();// 有相關的資料
		List<Customer> customers = new ArrayList<Customer>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		//排序
		List<Sort.Order> orders = new ArrayList<>();
		orders.add(new Sort.Order(Sort.Direction.DESC, "register.rrprid"));
		orders.add(new Sort.Order(Sort.Direction.ASC, "rdstatement"));
		orders.add(new Sort.Order(Sort.Direction.DESC, "rdid"));	
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by(orders));
		String search_ro_id = null;
		String search_rr_sn = null;
		String search_rr_pr_id = null;
		String search_rd_statement = null;
		String search_rd_u_finally = null;

		String search_rd_check = "-1";
		String search_rr_pb_type = null;

		Date rosramdate = null;
		Date roeramdate = null;
		Date rrspbsysmdate = null;
		Date rrepbsysmdate = null;
		String search_ro_s_ram_date = null;
		String search_ro_e_ram_date = null;
		String search_rr_s_pb_sys_m_date = null;
		String search_rr_e_pb_sys_m_date = null;

		// 功能-名稱編譯
		// 維修單據
		String ro_id = "維修單(序號)", ro_c_id = "客戶ID", //
				ro_check = "單據狀態", ro_from = "來源", //
				ro_e_date = "完成日", ro_s_date = "寄出日", ro_g_date = "收到日", ro_ram_date = "申請日";
		// 維修細節
		String rd_id = "維修項目(序號)", /* rd_ro_id = "維修單", */ //
				/* rd_rr_sn = "產品序號", */ rd_u_qty = "產品數量", rd_rc_value = "故障代號", //
				rd_ru_id = "分配單位ID", rd_statement = "描述問題", //
				rd_true = "故障原因", rd_solve = "解決問題", rd_experience = "備註事項", rd_check = "檢核狀態", //
				rd_svg = "圖片", rd_u_finally = "修復員", rd_f_analyst = "優先判斷單位", rd_type = "故障類型";
		// 維修登記(物件)
		String rr_sn = "產品(序號)", rr_c_sn = "客戶產品(序號)", //
				rr_pr_id = "製令單", rr_ph_p_qty = "製令數量", //
				rr_pr_p_model = "產品型號", rr_ph_w_years = "保固年份", //
				rr_pb_sys_m_date = "生產日期", rr_pb_type = "產品類型", //
				rr_v = "版本號", rr_f_ok = "產品狀態", rr_expired = "保固內?";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", /* sys_sort = "排序", sys_ver = "版本", */ sys_status = "狀態";
		//
		/* sys_header = "群組", ui_group_id = "UI_Group_ID" */;

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			// 維修單據
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_id", FFS.h_t(ro_id, "210px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_c_id", FFS.h_t(ro_c_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_check", FFS.h_t(ro_check, "100px", FFM.Wri.W_N));
			// 產品資料
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_id", FFS.h_t(rr_pr_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_p_qty", FFS.h_t(rr_ph_p_qty, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_p_model", FFS.h_t(rr_pr_p_model, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_sn", FFS.h_t(rr_sn, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_c_sn", FFS.h_t(rr_c_sn, "200px", FFM.Wri.W_Y));

			// 維修細節
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_rc_value", FFS.h_t(rd_rc_value, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_statement", FFS.h_t(rd_statement, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_true", FFS.h_t(rd_true, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_solve", FFS.h_t(rd_solve, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_experience", FFS.h_t(rd_experience, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_f_analyst", FFS.h_t(rd_f_analyst, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_u_finally", FFS.h_t(rd_u_finally, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_svg", FFS.h_t(rd_svg, "150px", FFM.Wri.W_N));

			// 維修單據
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_from", FFS.h_t(ro_from, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_e_date", FFS.h_t(ro_e_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_s_date", FFS.h_t(ro_s_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_g_date", FFS.h_t(ro_g_date, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ro_ram_date", FFS.h_t(ro_ram_date, "150px", FFM.Wri.W_Y));
			// 維修細節
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_id", FFS.h_t(rd_id, "230px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_u_qty", FFS.h_t(rd_u_qty, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_ru_id", FFS.h_t(rd_ru_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_type", FFS.h_t(rd_type, "120px", FFM.Wri.W_Y));

			// 產品資料
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_expired", FFS.h_t(rr_expired, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_w_years", FFS.h_t(rr_ph_w_years, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_sys_m_date", FFS.h_t(rr_pb_sys_m_date, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_type", FFS.h_t(rr_pb_type, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_v", FFS.h_t(rr_v, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_f_ok", FFS.h_t(rr_f_ok, "150px", FFM.Wri.W_Y));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "100px", FFM.Wri.W_Y));
			resp.setHeader(object_header);

			// 放入報告 [m__(key)](Analysis report) 格式-是否需要顯示
			JSONObject object_analysis = new JSONObject();
			object_analysis.put("ro_id", FFM.Wri.W_N);
			object_analysis.put("ro_c_id", FFM.Wri.W_N);
			object_analysis.put("ro_check", FFM.Wri.W_N);
			object_analysis.put("ro_e_date", FFM.Wri.W_N);
			object_analysis.put("ro_s_date", FFM.Wri.W_N);
			object_analysis.put("ro_g_date", FFM.Wri.W_N);
			object_analysis.put("rd_ru_id", FFM.Wri.W_N);
			object_analysis.put("rd_svg", FFM.Wri.W_N);
			resp.setAnalysis(object_analysis);

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();

			// 維修單據
			obj_m = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-12", false, n_val, "ro_id", ro_id));
			customers = customerDao.findAll();
			for (Customer one : customers) {
				s_val.put((new JSONObject()).put("value", one.getCcname()).put("key", one.getCid()));
			}
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "3", FFM.Wri.W_Y, "col-md-2", true, s_val, "ro_c_id", ro_c_id));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "未結單").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已結單").put("key", 1));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, s_val, "ro_check", ro_check));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "RMA售後").put("key", "RMA"));
			s_val.put((new JSONObject()).put("value", "DTR廠內").put("key", "DTR"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "DTR", FFM.Wri.W_N, "col-md-1", true, s_val, "ro_from", ro_from));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-2", false, n_val, "ro_e_date", ro_e_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-2", false, n_val, "ro_s_date", ro_s_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-2", false, n_val, "ro_g_date", ro_g_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-2", false, n_val, "ro_ram_date", ro_ram_date));

			// 維修細節
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-12", true, n_val, "rd_id", rd_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "rd_type", rd_type));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1", "1", FFM.Wri.W_Y, "col-md-1", true, n_val, "rd_u_qty", rd_u_qty));
			s_val = new JSONArray();
			mUnits = unitDao.findAllByRepairUnit(0L, 0L, null, null, null, true, null);
			for (RepairUnit oneUnit : mUnits) {
				s_val.put((new JSONObject()).put("value", oneUnit.getRugname()).put("key", oneUnit.getRuid()));
			}
			s_val.put((new JSONObject()).put("value", "全單位").put("key", 0));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, s_val, "rd_ru_id", rd_ru_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "rd_u_finally", rd_u_finally));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", 4));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-2", true, s_val, "rd_check", rd_check));

			// obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_Y, "col-md-6", true, n_val, "rd_rc_value", rd_rc_value));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val, "rd_statement", rd_statement));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.IMG, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_svg", rd_svg));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_true", rd_true));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_solve", rd_solve));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_experience", rd_experience));

			// 產品(品件)
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", true, n_val, "rr_sn", rr_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "rr_c_sn", rr_c_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "rr_pr_id", rr_pr_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "rr_ph_p_qty", rr_ph_p_qty));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "rr_pr_p_model", rr_pr_p_model));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "保固期內").put("key", true));
			s_val.put((new JSONObject()).put("value", "保固過期").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_Y, "col-md-1", true, s_val, "rr_expired", rr_expired));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "rr_ph_w_years", rr_ph_w_years));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.DATE, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "rr_pb_sys_m_date", rr_pb_sys_m_date));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "產品", FFM.Wri.W_Y, "col-md-1", true, s_val, "rr_pb_type", rr_pb_type));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "rr_v", rr_v));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "待修中").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已修復").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已報廢").put("key", 2));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "false", FFM.Wri.W_Y, "col-md-1", true, s_val, "rr_f_ok", rr_f_ok));

			// 系統
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "開啟(正常)").put("key", "0"));
			s_val.put((new JSONObject()).put("value", "關閉(作廢)").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-2", true, s_val, "sys_status", sys_status));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));
			resp.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// 維修單
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ro_id", ro_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rr_sn", rr_sn, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rr_pr_id", rr_pr_id, n_val));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", 4));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "rd_check", rd_check, s_val));

			// 維修品登記
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "rr_pb_type", rr_pb_type, s_val));

			// 維修單細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_statement", rd_statement, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_u_finally", rd_u_finally, n_val));
			// 時間區間
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "ro_s_ram_date", ro_ram_date + "(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "ro_e_ram_date", ro_ram_date + "(終)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "rr_s_pb_sys_m_date", rr_pb_sys_m_date + "(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "rr_e_pb_sys_m_date", rr_pb_sys_m_date + "(終)", n_val));
			resp.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			search_ro_id = body.getJSONObject("search").getString("ro_id");
			search_ro_id = search_ro_id.equals("") ? null : search_ro_id;
			search_rr_pr_id = body.getJSONObject("search").getString("rr_pr_id");
			search_rr_pr_id = search_rr_pr_id.equals("") ? null : search_rr_pr_id;
			search_rd_check = body.getJSONObject("search").getString("rd_check");
			search_rd_check = search_rd_check.equals("") ? "-1" : search_rd_check;
			search_rr_sn = body.getJSONObject("search").getString("rr_sn");
			search_rr_sn = search_rr_sn.equals("") ? null : search_rr_sn;
			search_rr_pb_type = body.getJSONObject("search").getString("rr_pb_type");
			search_rr_pb_type = search_rr_pb_type.equals("") ? null : search_rr_pb_type;
			search_rd_statement = body.getJSONObject("search").getString("rd_statement");
			search_rd_statement = search_rd_statement.equals("") ? null : search_rd_statement;
			search_rd_u_finally = body.getJSONObject("search").getString("rd_u_finally");
			search_rd_u_finally = search_rd_u_finally.equals("") ? null : search_rd_u_finally;

			search_ro_s_ram_date = body.getJSONObject("search").getString("ro_s_ram_date");
			search_ro_e_ram_date = body.getJSONObject("search").getString("ro_e_ram_date");
			rosramdate = search_ro_s_ram_date.equals("") ? null : Fm_Time.toDateTime(search_ro_s_ram_date);
			roeramdate = search_ro_e_ram_date.equals("") ? null : Fm_Time.toDateTime(search_ro_e_ram_date);

			search_rr_s_pb_sys_m_date = body.getJSONObject("search").getString("rr_s_pb_sys_m_date");
			search_rr_e_pb_sys_m_date = body.getJSONObject("search").getString("rr_e_pb_sys_m_date");
			rrspbsysmdate = search_rr_s_pb_sys_m_date.equals("") ? null : Fm_Time.toDateTime(search_rr_s_pb_sys_m_date);
			rrepbsysmdate = search_rr_e_pb_sys_m_date.equals("") ? null : Fm_Time.toDateTime(search_rr_e_pb_sys_m_date);
		}

		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		// 是否是維修成員 or 管理員?
		Long suid = user.getSuid();
		if (unitDao.findByRusuidOrderBySyssortAsc(suid).size() < 1 && suid != 1L) {
			resp.autoMsssage("MT004");
			return false;
		}

		// 查詢
		rdids = detailDao.findAllByRepairDetail(//
				search_ro_id, search_rr_sn, search_rr_pr_id, Integer.parseInt(search_rd_check), search_rr_pb_type, //
				search_rd_statement, search_rd_u_finally, rosramdate, roeramdate, //
				rrspbsysmdate, rrepbsysmdate, null, page_r);

		rdids.forEach(rd -> {
			// 問題細節
			RepairOrder ro = rd.getOrder();
			RepairRegister rr = rd.getRegister();
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_id", ro.getRoid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_c_id", ro.getRocid() == null ? "" : ro.getRocid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_check", ro.getRocheck());
			//
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_id", rr.getRrprid() == null ? "" : rr.getRrprid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_p_qty", rr.getRrphpqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_p_model", rr.getRrprpmodel() == null ? "" : rr.getRrprpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_sn", rr.getRrsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_c_sn", rr.getRrcsn() == null ? "" : rr.getRrcsn());
			//
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rc_value", rd.getRdrcvalue() == null ? "" : rd.getRdrcvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_statement", rd.getRdstatement());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", rd.getRdtrue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve", rd.getRdsolve() == null ? "" : rd.getRdsolve());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", rd.getRdexperience() == null ? "" : rd.getRdexperience());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_f_analyst", rd.getRdfanalyst() == null ? "" : rd.getRdfanalyst());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_finally", rd.getRdufinally() == null ? "" : rd.getRdufinally());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_svg", rd.getRdsvg() == null ? "[]" : rd.getRdsvg());
			//
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_from", ro.getRofrom() == null ? "" : ro.getRofrom());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_e_date", ro.getRoedate() == null ? "" : Fm_Time.to_y_M_d(ro.getRoedate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_s_date", ro.getRosdate() == null ? "" : Fm_Time.to_y_M_d(ro.getRosdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_g_date", ro.getRogdate() == null ? "" : Fm_Time.to_y_M_d(ro.getRogdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_ram_date", ro.getRoramdate() == null ? "" : Fm_Time.to_y_M_d(ro.getRoramdate()));

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", rd.getRdid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_qty", rd.getRduqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_ru_id", rd.getRdruid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_type", rd.getRdtype() == null ? "" : rd.getRdtype());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_expired", rr.getRrexpired() == null ? "" : rr.getRrexpired());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_w_years", rr.getRrphwyears());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_sys_m_date", rr.getRrpbsysmdate() == null ? "" : Fm_Time.to_y_M_d(rr.getRrpbsysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_type", rr.getRrpbtype());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_v", rr.getRrv());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_f_ok", rr.getRrfok() == null ? 0 : rr.getRrfok());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(rd.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", rd.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(rd.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", rd.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", rd.getSysstatus());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", rd.getSysnote() == null ? "" : rd.getSysnote());
			object_bodys.put(object_body);

		});
		resp.setBody(new JSONObject().put("search", object_bodys));
		return true;
	}

	// 報表 查詢 資料清單
	public boolean getReportData(PackageBean bean, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		List<RepairDetail> details = new ArrayList<RepairDetail>();
		JSONArray list = body.getJSONArray("search");
		String str = "";
		boolean first = true;
		// Step1.======= 轉換內容 bytes to string =======
		for (Object object : list) {
			String[] list_s = ((String) object).split(",");
			byte[] bytes = new byte[list_s.length];
			for (int i = 0; i < list_s.length; i++) {
				bytes[i] = (byte) (Integer.parseInt(list_s[i]) & 0xFF);
			}
			if (first) {
				first = false;
				String one = new String(bytes, StandardCharsets.UTF_8);
				if (one.indexOf("!= ''") > 0) {
					one = "(" + one + " AND " + one.replace("!= ''", "is not null ") + ")";
				} else if (one.indexOf("= ''") > 0) {
					one = "(" + one + " OR " + one.replace("= ''", " is null") + ")";
				}
				str += one;
			} else {
				String one = new String(bytes, StandardCharsets.UTF_8);
				if (one.indexOf("!= ''") > 0) {
					one = "(" + one + " AND " + one.replace("!= ''", "is not null ") + ")";
				} else if (one.indexOf("= ''") > 0) {
					one = "(" + one + " OR " + one.replace("= ''", " is null") + ")";
				}
				str += " AND " + one;
			}
		}
		// 取代共用參數
		str = str.replace("sys_", "rd.sys_");
		str = str.replace("rd_", "rd.rd_");
		str = str.replace("rr_", "rr.rr_");
		str = str.replace("ro_", "ro.ro_");

		// Step2.=======Analysis report 查詢SN欄位+產品型號+製令單號 =======
		String nativeQuery = "SELECT rd.* FROM repair_detail rd " + //
				"join repair_register rr on rd.rd_rr_sn = rr.rr_sn " + //
				"join repair_order ro on rd.rd_ro_id = ro.ro_id WHERE ";
		nativeQuery += str;
		nativeQuery += " order by rr.rr_pr_id desc ";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		System.out.println(nativeQuery);
		try {
			Query query = em.createNativeQuery(nativeQuery, RepairDetail.class);
			details = query.getResultList();
			if (details.size() <= 0) {
				bean.autoMsssage("102");
				return false;
			}
			if (details.size() > 25000) {
				bean.autoMsssage("SH000");
				return false;
			}
		} catch (Exception e) {
			bean.autoMsssage("103");
			return false;
		}
		// Step3.======= 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱] =======
		JSONArray object_bodys = new JSONArray();
		details.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_id", one.getRegister().getRrprid() == null ? "" : one.getRegister().getRrprid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_p_qty", one.getRegister().getRrphpqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_p_model", one.getRegister().getRrprpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_sn", one.getRegister().getRrsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_c_sn", one.getRegister().getRrcsn());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rc_value", one.getRdrcvalue() == null ? "" : one.getRdrcvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_statement", one.getRdstatement());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", one.getRdtrue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve", one.getRdsolve() == null ? "" : one.getRdsolve());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", one.getRdexperience() == null ? "" : one.getRdexperience());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_finally", one.getRdufinally());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_from", one.getOrder().getRofrom());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ro_ram_date", one.getOrder().getRoramdate());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", one.getRdid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_qty", one.getRduqty());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_type", one.getRdtype() == null ? "" : one.getRdtype());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_expired", one.getRegister().getRrexpired() == null ? "" : one.getRegister().getRrexpired());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_w_years", one.getRegister().getRrphwyears());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_sys_m_date",
					one.getRegister().getRrpbsysmdate() == null ? "" : one.getRegister().getRrpbsysmdate());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_type", one.getRegister().getRrpbtype());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_v", one.getRegister().getRrv());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_f_ok", one.getRegister().getRrfok());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys.put(object_body);
		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		check = true;
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("create");
			check = true;
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
			// check = true;
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
				JSONObject data = (JSONObject) one;
				// Step1. 找尋此 維修項目(序號)
				ArrayList<RepairDetail> rds = detailDao.findAllByRdid(data.getString("rd_id"));
				if (rds.size() == 1) {
					RepairDetail rd = rds.get(0);
					// 維修單
					rd.getOrder().setRocid(data.getLong("ro_c_id"));
					rd.getOrder().setRocheck(data.getInt("ro_check"));
					rd.getOrder().setSysmdate(new Date());
					rd.getOrder().setSysmuser(user.getSuaccount());
					// 維修單項目
					rd.setRdruid(data.getLong("rd_ru_id"));
					rd.setRdufinally(data.getString("rd_u_finally"));
					rd.setRdcheck(data.getInt("rd_check"));
					rd.setRdstatement(data.getString("rd_statement"));
					rd.setRdsvg(data.getString("rd_svg"));
					rd.setRdtrue(data.getString("rd_true"));
					rd.setRdexperience(data.getString("rd_solve"));
					rd.setRdexperience(data.getString("rd_experience"));
					rd.setSysmdate(new Date());
					rd.setSysmuser(user.getSuaccount());
					// 品件登記
					rd.getRegister().setRrcsn(data.getString("rr_c_sn"));
					rd.getRegister().setRrprpmodel(data.getString("rr_pr_p_model"));
					rd.getRegister().setRrexpired(data.getBoolean("rr_expired"));
					rd.getRegister().setRrphwyears(data.getInt("rr_ph_w_years"));
					rd.getRegister().setRrpbtype(data.getString("rr_pb_type"));
					rd.getRegister().setRrv(data.getString("rr_v"));
					rd.getRegister().setRrfok(data.getInt("rr_f_ok"));
					rd.getRegister().setSysmdate(new Date());
					rd.getRegister().setSysmuser(user.getSuaccount());
					detailDao.save(rd);
				} else {
					resp.autoMsssage("102");
					return false;
				}
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
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
				RepairDetail entity = new RepairDetail();
				JSONObject data = (JSONObject) one;
				entity.setRdid(data.getString("rd_id"));
				detailDao.delete(entity);
				check = true;
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}