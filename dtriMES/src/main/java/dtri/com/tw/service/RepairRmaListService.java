package dtri.com.tw.service;

import java.nio.charset.StandardCharsets;
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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.RepairDetail;
//import dtri.com.tw.db.entity.RepairRegister;
import dtri.com.tw.db.entity.RepairRmaDetail;
import dtri.com.tw.db.entity.RmaList;
import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.RepairDetailDao;
import dtri.com.tw.db.pgsql.dao.RepairRmaDetailDao;
import dtri.com.tw.db.pgsql.dao.RmaListDao;
import dtri.com.tw.db.pgsql.dao.SystemMailDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class RepairRmaListService {

	@Autowired // RmaList RMA清單
	RmaListDao rmaListDao;
	@Autowired // 產品細節
	ProductionBodyDao bodyDao;
	@Autowired // RMA維修紀錄
	RepairRmaDetailDao rmaDetailDao;
	@Autowired // 通用-製令內容
	ProductionHeaderDao headerDao;
	@Autowired // 廠內維修紀錄
	RepairDetailDao detailDao;
	@Autowired // 信寄清單
	private SystemMailDao rmaMailListDao;
	@Autowired // 系統寄信
	BasicNotificationMailService mailService;
	@Autowired
	EntityManager em;

	// 取得當前 資料清單 search 欄位
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("id").descending());


		String search_rma = null; // RMA號碼
		String search_rd_guest = null; //客戶
		String search_rd_model = null; //產品型號
		String search_rma_b_sn = null; // 機台號碼
		String search_rd_rr_mb = null; // MB號碼"
		String search_rma_result = null; // 維修結果
		String search_rma_statement = null; // 客戶問題敘述
		String search_rd_true = null; // 實際問題
		String search_rd_solve = null; // 解決問題
		String search_rd_experience = null; // 維修備註
		String search_rma_part_sn = null; // MB料號
		String search_packing_list = null; // 單據號碼
		String search_sys_c_user = null; //建立人
		// 功能-名稱編譯
		// 維修細節
		String rd_id = "ID", /* rd_ro_id = "維修單", */ //
				rd_rma = "RMA號碼", rd_guest = "客戶", rd_model = "產品型號", rd_part_no = "Part no", rd_rr_sn = "產品序號",
				rd_rr_mb = "MB號碼", rma_statement = "客戶問題敘述",
				// rd_u_qty = "數量", //
				rd_true = "實際問題", rd_solve = "解決問題", rd_experience = "維修備註", rma_part_sn = "MB料號",
				packing_list = "單據號碼", rma_result = "維修結果";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", /* sys_sort = "排序", sys_ver = "版本", */ sys_status = "狀態"; //
//				sys_header = "群組"/* , ui_group_id = "UI_Group_ID" */;

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]********************** table表頭設置
			// ******************************
			JSONObject object_header = new JSONObject();
			int ord = 0;
			// 維修細節
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t(sys_header, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_id", FFS.h_t(rd_id, "50px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_rma", FFS.h_t(rd_rma, "200px", FFM.Wri.W_Y)); // RMA
																													// 號碼
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_guest", FFS.h_t(rd_guest, "150px", FFM.Wri.W_Y)); // 客戶
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_model", FFS.h_t(rd_model, "150px", FFM.Wri.W_Y));// Model
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_part_no", FFS.h_t(rd_part_no, "150px", FFM.Wri.W_Y)); // Part
																															// NO
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_rr_sn", FFS.h_t(rd_rr_sn, "200px", FFM.Wri.W_Y)); // 產品序號
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_rr_mb", FFS.h_t(rd_rr_mb, "150px", FFM.Wri.W_Y)); // MB號碼
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rma_statement",	FFS.h_t(rma_statement, "250px", FFM.Wri.W_Y)); // 客戶問題敘述
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_true", FFS.h_t(rd_true, "250px", FFM.Wri.W_Y)); // 復判不良原因
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_solve", FFS.h_t(rd_solve, "250px", FFM.Wri.W_Y)); // 修復過程
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_experience",	FFS.h_t(rd_experience, "250px", FFM.Wri.W_Y)); // 維修備註
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rma_part_sn",FFS.h_t(rma_part_sn, "200px", FFM.Wri.W_Y)); // MB料號
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "packing_list",FFS.h_t(packing_list, "150px", FFM.Wri.W_Y)); // 單據號碼
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rma_result", FFS.h_t(rma_result, "100px", FFM.Wri.W_Y)); // 維修結果

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "50px", FFM.Wri.W_Y));
			bean.setHeader(new JSONObject().put("search_header", object_header));
//			bean.setHeader(object_header);

			// 放入報告 [m__(key)](Analysis report) 格式-是否需要顯示
			JSONObject object_analysis = new JSONObject();
//			object_analysis.put("ro_id", FFM.Wri.W_N);
//			object_analysis.put("ro_c_id", FFM.Wri.W_N);
//			object_analysis.put("ro_check", FFM.Wri.W_N);
//			object_analysis.put("ro_e_date", FFM.Wri.W_N);
//			object_analysis.put("ro_s_date", FFM.Wri.W_N);
//			object_analysis.put("ro_g_date", FFM.Wri.W_N);
//			object_analysis.put("rd_ru_id", FFM.Wri.W_N);
//			object_analysis.put("rd_svg", FFM.Wri.W_N);
			bean.setAnalysis(object_analysis);
			
			// ************************************** 放入修改 [m__(key)](modify/Create/Delete)
			// 格式 **************************
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();

			// 單據細節
			obj_m = new JSONArray();
//			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "rd_id", rd_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val,"rd_id", rd_id)); // ID
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"rd_rma", rd_rma)); // RMA號碼
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"rd_guest", rd_guest)); // 客戶
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val,"rd_model", rd_model)); // model
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"rd_part_no", rd_part_no)); // part no
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"rd_rr_sn", rd_rr_sn)); // 產品序號
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"rd_rr_mb", rd_rr_mb)); // MB號碼
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val,"rma_statement", rma_statement)); // 客戶問題敘述

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val,"rd_true", rd_true)); // 實際不良原因
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val,"rd_solve", rd_solve)); // 修復過程
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val,"rd_experience", rd_experience)); // 維修備註

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val,"rma_part_sn", rma_part_sn)); // MB料號
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val,"packing_list", packing_list)); // 單據號碼
			// obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_Y, "col-md-2", false, n_val, "rma_result", rma_result)); //維修結果

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已修復").put("key", "已修復"));
			s_val.put((new JSONObject()).put("value", "不修退回").put("key", "不修退回"));
			s_val.put((new JSONObject()).put("value", "報廢").put("key", "報廢"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, s_val,"rma_result", rma_result)); // 維修結果

//			s_val = new JSONArray();
//			s_val.put((new JSONObject()).put("value", "開啟(正常)").put("key", "0"));
//			s_val.put((new JSONObject()).put("value", "關閉(作廢)").put("key", "1"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val,"sys_status", sys_status));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val,"sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val,"sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val,"sys_m_user", sys_m_user));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val,"sys_c_date", sys_c_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val,"sys_c_user", sys_c_user));
			bean.setCell_modify(obj_m);

			// ************************************************* 放入包裝(search) Search欄位的設置
			// ***************************
			JSONArray object_searchs = new JSONArray();
			// 維修單細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_rma", rd_rma, n_val)); // RMA 號碼
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_guest", rd_guest, n_val)); // 客戶
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_model", rd_model, n_val)); // 產品型號				
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_rr_sn", rd_rr_sn, n_val)); // 產品序號
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_rr_mb", rd_rr_mb, n_val)); // MB號碼
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rma_statement", rma_statement, n_val)); // 客戶問題敘述
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_true", rd_true, n_val)); // 實際不良原因
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_solve", rd_solve, n_val)); // 修復過程

			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "rd_experience", rd_experience, n_val)); // 維修備註
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "rma_part_sn", rma_part_sn, n_val)); // MB料號
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "packing_list", packing_list, n_val)); // 單據號碼

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "已修復").put("key", "已修復"));
			s_val.put((new JSONObject()).put("value", "不修退回").put("key", "不修退回"));
			s_val.put((new JSONObject()).put("value", "報廢").put("key", "報廢"));
//			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "rma_result", rma_result, s_val)); // 維修結果			
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "sys_c_user", sys_c_user, n_val)); //建立人員		
			
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			search_rma = body.getJSONObject("search").getString("rd_rma"); // RMA 號碼
			search_rma = search_rma.equals("") ? null : search_rma;			
			search_rd_guest = body.getJSONObject("search").getString("rd_guest"); // 客戶
			search_rd_guest = search_rd_guest.equals("") ? null : search_rd_guest;			
			search_rd_model = body.getJSONObject("search").getString("rd_model"); // 產品型號	
			search_rd_model = search_rd_model.equals("") ? null : search_rd_model;			
			
			search_rma_b_sn = body.getJSONObject("search").getString("rd_rr_sn"); // 產品序號
			search_rma_b_sn = search_rma_b_sn.equals("") ? null : search_rma_b_sn;
			search_rd_rr_mb = body.getJSONObject("search").getString("rd_rr_mb"); // MB號碼
			search_rd_rr_mb = search_rd_rr_mb.equals("") ? null : search_rd_rr_mb;

			search_rma_statement = body.getJSONObject("search").getString("rma_statement"); // 客戶問題敘述
			search_rma_statement = search_rma_statement.equals("") ? null : search_rma_statement;

			search_rd_true = body.getJSONObject("search").getString("rd_true"); // 實際不良原因
			search_rd_true = search_rd_true.equals("") ? null : search_rd_true;

			search_rd_solve = body.getJSONObject("search").getString("rd_solve"); // 修復過程
			search_rd_solve = search_rd_solve.equals("") ? null : search_rd_solve;

			search_rd_experience = body.getJSONObject("search").getString("rd_experience"); // 維修備註
			search_rd_experience = search_rd_experience.equals("") ? null : search_rd_experience;

			search_rma_part_sn = body.getJSONObject("search").getString("rma_part_sn"); // MB料號
			search_rma_part_sn = search_rma_part_sn.equals("") ? null : search_rma_part_sn;

			search_packing_list = body.getJSONObject("search").getString("packing_list"); // 單據號碼
			search_packing_list = search_packing_list.equals("") ? null : search_packing_list;
			
			search_rma_result = body.getJSONObject("search").getString("rma_result"); // 維修結果
			search_rma_result = search_rma_result.equals("") ? null : search_rma_result;			
			
			
			search_sys_c_user = body.getJSONObject("search").getString("sys_c_user"); // 建立人員
			search_sys_c_user = search_sys_c_user.equals("") ? null : search_sys_c_user;
		}
		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		// 物件
//		Long rdruid = 0L;
//		List<RepairUnit> units = unitDao.findAllByRepairUnit(null, user.getSuid(), null, null, null, false, null);
//		rdruid = units.size() >= 1 ? units.get(0).getRugid() : 0L;

		ArrayList<RepairRmaDetail> rds = rmaDetailDao.findAllByRepairRmaDetail(null, search_rma,search_rd_guest,search_rd_model, search_rma_b_sn,
				search_rd_rr_mb, search_rma_statement, search_rd_true, search_rd_solve, search_rd_experience,
				search_rma_part_sn, search_packing_list, search_rma_result,search_sys_c_user, page_r);

		// 有沒有資料?
		if (rds.size() > 0) {
			rds.forEach(rd -> {
				JSONObject object_body = new JSONObject();
				int ord = 0;
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", rd.getSysheader());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", rd.getId());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rma", rd.getRmasn()); // RMA 號碼
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_guest", rd.getRmaguest()); // 客戶
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_model", rd.getRmamodel()); // Model
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_part_no", rd.getRmaPartNo()); // part no
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_sn", rd.getRmabsn()); // 產品序號
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_mb", rd.getRmambsn()); // MB號碼
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_statement", rd.getRmastatement()); // 客戶問題敘述
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", rd.getRdtrue()); // 復判不良原因
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve",rd.getRdsolve() == null ? "" : rd.getRdsolve()); // 修復過程
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", rd.getRdexperience()); // 維修備註
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_part_sn", rd.getRmapartsn()); // MB料號
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "packing_list", rd.getPackinglist()); // 單據號碼
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_result", rd.getRmaresult()); // 維修結果

//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_p_qty", rr.getRrphpqty());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_p_model", rr.getRrprpmodel());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_expired", rr.getRrexpired());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_w_years", rr.getRrphwyears());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_sys_m_date", rr.getRrpbsysmdate() == null ? "" : rr.getRrpbsysmdate());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_type", rr.getRrpbtype());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_v", rr.getRrv());
//				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_f_ok", rr.getRrfok());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(rd.getSyscdate()));
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", rd.getSyscuser());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(rd.getSysmdate()));
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", rd.getSysmuser());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", rd.getSysstatus());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", rd.getSysnote());

				object_bodys.put(object_body);
			});
		}
		bean.setBody(new JSONObject().put("search", object_bodys));
		return true;

	}

	// 存檔 資料清單123 Detail / Create / Modify / Delete
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("create");
			// JSONArray list_chok = new JSONArray();
			// check = true;
			System.out.println("存檔 資料清單123");
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 另存檔 資料清單456
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
//		JSONObject body = req.getBody();
//		JSONArray list = body.getJSONArray("modify");
		boolean check = false;

		try {
//			for (Object object : list) {
//				JSONObject one = (JSONObject) object;
//			}

		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		System.out.println(" 更新 資料清單");
		JSONArray list = body.getJSONArray("modify");
		Long rmaid = null;
		String rd_true = null, rd_solve = null, rd_experience = null, rma_part_sn = null, packing_list = null;
		String rma_result = null;
		try {

			for (Object object : list) {
				JSONObject one = (JSONObject) object;

				rmaid = (long) one.optInt("rd_id");
				rmaid = (rmaid == -1) ? 0 : rmaid;
				rd_true = one.getString("rd_true"); // 實際問題情況
				rd_true = rd_true.equals("") ? "" : rd_true;
				rd_solve = one.getString("rd_solve"); // 解決問題
				rd_solve = rd_solve.equals("") ? "" : rd_solve;
				rd_experience = one.getString("rd_experience"); // 維修備註
				rd_experience = rd_experience.equals("") ? "" : rd_experience;
				rma_part_sn = one.getString("rma_part_sn"); // MB料號
				rma_part_sn = rma_part_sn.equals("") ? "" : rma_part_sn;
				packing_list = one.getString("packing_list"); // 單據號碼
				packing_list = packing_list.equals("") ? "" : packing_list;
				rma_result = one.getString("rma_result"); // 維修結果
				rma_result = rma_result.equals("") ? "" : rma_result;
				
				ArrayList<RepairRmaDetail> rds = rmaDetailDao.findByid(rmaid);

				if (rds.size() > 0) {
					RepairRmaDetail rd = rds.get(0);
					rd.setRdtrue(rd_true); // 實際問題情況
					rd.setRdsolve(rd_solve); // 解決問題
					rd.setRdexperience(rd_experience); // 維修備註
					rd.setRmapartsn(rma_part_sn); // MB料號
					rd.setPackinglist(packing_list); // 單據號碼
					rd.setRmaresult(rma_result); // 維修結果
					rd.setSysmuser(user.getSuaccount()); // 修改人員
					rd.setSysmdate(new Date()); // 修改時間
					rmaDetailDao.save(rd);
					check = true;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		JSONArray list = body.getJSONArray("delete");
		boolean check = false;
		Long rmaid = null;
		try {
			for (Object object : list) {
				JSONObject one = (JSONObject) object;
				rmaid = (long) one.optInt("rd_id");
				rmaid = (rmaid == -1) ? 0 : rmaid;
				rmaDetailDao.deleteByid(rmaid);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// ==============客製化==============

	// 更新/新增 資料清單 Customized mode
	@Transactional // 存RMA維修資料johnny
	public boolean updateDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();

		JSONObject list = body.getJSONObject("modify").getJSONObject("repair");

		// 強制轉型為 long（因為 optInt 回傳 int，但 id 是 Long）
		// 嘗試取得 rma_list_id，如果 rma_list_id 不存在或不是整數，則回傳 -1（預設值）。
		// 如果 id == -1（代表 t_id 不存在或無效），則將 id 設為 null，否則保持原值。
		Long rmaListId = (long) list.optInt("rma_list_id", -1); // RMA LIS ID
		rmaListId = (rmaListId == -1) ? null : rmaListId;

		Long rmaRepairId = (long) list.optInt("rma_repair_id", -1); // RMA 維修紀錄ID
		rmaRepairId = (rmaRepairId == -1) ? null : rmaRepairId;

		String rmasn = list.getString("rma_sn");
		rmasn = rmasn.equals("") ? null : rmasn;

		String rmaBsn = list.getString("rma_b_sn");
		rmaBsn = rmaBsn.equals("") ? null : rmaBsn;

		String rmaMbsn = list.getString("rma_mb_sn");
		rmaMbsn = rmaMbsn.equals("") ? null : rmaMbsn;

		String rmaStatement = list.getString("rma_statement"); // //客戶問題敘述
		rmaStatement = rmaStatement.equals("") ? null : rmaStatement;

		String rdTrue = list.getString("rd_true");// 實際問題情況
		rdTrue = rdTrue.equals("") ? null : rdTrue;

		String rdSolve = list.getString("rd_solve");// 維修處理事項
		rdSolve = rdSolve.equals("") ? null : rdSolve;

		// 機台序號 OR MB序號都為空值 AND( 實際問題情況 or 維修處理事項 ) 回傳false
		if ((rmaBsn == null || rmaMbsn == null) && (rdTrue == null || rdSolve == null)) {
			return false;
		}

		List<RepairRmaDetail> rrds = null;
		// rmaRepairId 為null 表示未寫到資料庫
		if (rmaRepairId != null) {
			// 查詢維修紀錄repair_rma_detail資料庫有無資料,判斷是要新增還是更新
			rrds = rmaDetailDao.findAllByRmasnAndRmasn(rmaRepairId, rmasn, null, null);
		}
		try {
			RepairRmaDetail rrd;// 宣告 rrd 為 RepairRmaDetail 型別的物件變數，但此時 rrd 尚未被賦值，它只是一個變數的定義，還沒有指向任何具體的物件。

			// 如果搜尋資料庫後 rrds不為null 和 確保 rrds 內部只有一筆資料，才會執行內部邏輯
			if (rrds != null && rrds.size() == 1) {
				rrd = rrds.get(0); // 取出 rrds 的第一筆資料（索引 0）用來後續存取各個符合RepairRmaDetail物件格式的資料
				// RepairRmaDetail rrd = rrds.get(0); 這表示 rrds 中已經有一筆資料，我們只需要取得該物件並修改內容。
				// 不需要重新建立新物件，因為我們要更新已有的資料。
				// RMA號碼不能被更正 rrd.setRmasn(rmasn); // Rma號碼
				rrd.setRmaguest(list.getString("rma_guest")); // 客戶名稱
				rrd.setRmamodel(list.getString("rma_model")); // 機種型號
				rrd.setRmaPartNo(list.getString("rma_part_no"));
				rrd.setRmabsn(rmaBsn); // 機台序號
				rrd.setRmambsn(rmaMbsn); // MB序號

				rrd.setRmastatement(list.getString("rma_statement")); // 客戶問題敘述
				rrd.setRdtrue(list.getString("rd_true"));// 實際問題情況
				rrd.setRdsolve(list.getString("rd_solve"));// 維修處理事項
				rrd.setRdexperience(list.getString("rd_experience"));// 備註心得
				rrd.setRmapartsn(list.getString("rma_part_sn"));// MB料號
				rrd.setPackinglist(list.getString("packing_list"));// 單據
				rrd.setRmaresult(list.getString("rma_result"));// 維修結果
				rrd.setRmauser(list.getString("rma_user"));// 修復人

				rrd.setSysmuser(user.getSuaccount()); // 修改人員
				rrd.setSysmdate(new Date()); // 修改時間
//				RmaDetailDao.save(rrd);
			} else { // 如果搜尋資料庫後 rrds為null 就是新增資料 // 找不到資料，則建立新物件
				rrd = new RepairRmaDetail();
				// 建立RepairRmaDetail格式 的 rrd 物件 用來存取資料。
				// 若無查無資料這表示資料庫沒有該筆資料，因此我們需要建立一個新物件來存取新資料。然後再將新物件存入資料庫

				rrd.setRmasn(rmasn); // Rma號碼
				rrd.setRmaguest(list.getString("rma_guest")); // 客戶名稱
				rrd.setRmamodel(list.getString("rma_model")); // 機種型號
				rrd.setRmaPartNo(list.getString("rma_part_no"));
				rrd.setRmabsn(rmaBsn); // 機台序號
				rrd.setRmambsn(rmaMbsn); // MB序號

				rrd.setRmastatement(list.getString("rma_statement")); // 客戶問題敘述
				rrd.setRdtrue(list.getString("rd_true"));// 實際問題情況
				rrd.setRdsolve(list.getString("rd_solve"));// 維修處理事項
				rrd.setRdexperience(list.getString("rd_experience"));// 備註心得
				rrd.setRmapartsn(list.getString("rma_part_sn"));// MB料號
				rrd.setPackinglist(list.getString("packing_list"));// 單據
				rrd.setRmaresult(list.getString("rma_result"));// 維修結果 (已修復 /無法修復/不修退回/報廢)
				rrd.setRmauser(list.getString("rma_user"));// 修復人
				rrd.setRdcheck(1); // 改1 為已完工

				rrd.setSyscuser(user.getSuaccount()); // 建立人員
				rrd.setSyscdate(new Date()); // 建立時間
				rrd.setSysmuser(user.getSuaccount()); // 修改人員
				rrd.setSysmdate(new Date()); // 修改時間
			}
			rmaDetailDao.save(rrd);

			// 回寫資料到RMALIS清單上
			List<RmaList> rma_lis_details = rmaListDao.findAllBysnAndmb(rmaListId, rmasn, null, null, null); // 確認有無資料

			if (rma_lis_details.size() > 0) {
				RmaList r1 = rma_lis_details.get(0);
				// r1.setRmaNumber(rmasn);
				r1.setSerialNumber(rmaBsn);
				r1.setMbNumber(rmaMbsn); //
				r1.setModel(list.getString("rma_model"));
				r1.setStateCheck(2); // 改2 為已完工
				r1.setState("處理完畢");
				r1.setRrd_RmaResult(list.getString("rma_result")); // 把維修結果寫回RMALIST

				r1.setSysmuser(user.getSuaccount()); // 修改人員
				r1.setSysmdate(new Date()); // 修改時間

				rmaListDao.save(r1); // 回傳修改欄位資料到RMA清單
				// 0:未收到 1:已收到 2:處理完畢 3:已寄出
				// RmaDetailDao.save(rrd);
			}
//********************************** 自動寄信************************************************
			ArrayList<RmaList> yy = rmaListDao.findAllByrmaNumber(rmasn);
			// 再次檢查 此筆RMA單據是否完工?? 完工後寄信
			ArrayList<RepairRmaDetail> xx = rmaDetailDao.findAllByRmasn(rmasn);

			int x = xx.size();
			int y = yy.size();
			if (x == y) {
				System.out.println(rmasn + "單據已完工");
				// 執行寄送維修報表
				// ************************** 取得 MAIL 清單 ***********************
				// rmlds 就是一個 ArrayList<RmaMail> 型別的變數，存放查詢出來的所有RmaMail 物件。
				//搜尋"完成通知"的人員信箱資料
			//	ArrayList<SystemMail> rmlds = rmaMailListDao.findByRmaMail(null,"Y",null);
				ArrayList<SystemMail> rmlds = rmaMailListDao.findAll();
				StringBuilder rmaMailList = new StringBuilder(); // 使用 StringBuilder 來累加字串
				StringBuilder cMailList = new StringBuilder(); // 使用 StringBuilder 來累加字串
				// 符合收到貨 條件 取得需要寄信人員名單
				if (!rmlds.isEmpty()) { // 用 `isEmpty()` 取代 `size() > 0`
					rmlds.forEach(rl -> {
						if ("Y".equals(rl.getSurepairdone())) {// 如果 Surepairdone(處理好) 是 "Y"
							rmaMailList.append(rl.getSuemail()).append(";"); // 加入 email，並在後面加 ";"
						}
						//副本炒送
						if ("C".equals(rl.getSurepairdone())) {   //
							cMailList.append(rl.getSuemail()).append(";"); // 加入 email，並在後面加 ";"						}		
						}
					});					
					if (rmaMailList.isEmpty()) {
						resp.autoMsssage("MT007"); //[MT007] 此 (RMA單) 已全部處理完畢但無收件人資訊,無法寄信通知 請通知[管理員]
						return false;
					}
					
					// ************************** 寄信 ********************
					String mailList = rmaMailList.toString(); // 轉換為 String
					String c_mailList =cMailList.toString();
					String[] toUser = mailList.split(";"); // 用 ";" 分割成 String 陣列
					// String[] toUser = { "johnny_chuang@dtri.com"};
					String[] toCcUser = c_mailList.split(";"); // 用 ";" 分割成 String 陣列
					//String[] toCcUser = { "" };
					String subject = "RMA通知 " + rmasn + " "  + list.getString("rma_guest") + "  " + "維修報告 ";
					//把TABLE表雙層格線變成單線
					StringBuilder httpstr = new StringBuilder();
					httpstr.append("<style>")
				       .append("table { border-collapse: collapse; }")
				       .append("table, th, td { border: 1px solid black; padding: 5px; }")
				       .append("</style>");
					// 構建郵件內容
					httpstr.append("Dear All, <br><br>").append("通知 ").append(rmasn).append("處理完畢"); // .append("請提領<br><br>");
					httpstr.append("<br>").append("維修紀錄如下");
					httpstr.append("<table border='1'><tr>").append("<th>State</th>" //
							+ "<th>RMA Number</th>" // RMA號碼
							+ "<th>Customer</th>" // RMA客戶
							+ "<th>Model</th>" // model
							+ "<th>Part No</th>" // Oracle part no
							+ "<th>Serial Number</th>" // Serial Number
							+ "<th>MB Number</th>" // MB Number
							+ "<th>客戶問題敘述</th>" + "<th>復判不良原因</th>" + "<th>修復過程</th>" + "<th>備註</th>" + "</tr>");
					// 取得維修資料
					for (RepairRmaDetail rrd1 : xx) {
						httpstr.append("<tr>").append("<td>").append(rrd1.getRmaresult()).append("</td>") // 維修結果 :
								.append("<td>").append(rrd1.getRmasn()).append("</td>") // RMA號碼
								.append("<td>").append(rrd1.getRmaguest()).append("</td>") // RMA客戶
								.append("<td>").append(rrd1.getRmamodel()).append("</td>") // model
								.append("<td>").append(rrd1.getRmaPartNo()).append("</td>") // Oracle part no
								.append("<td>").append(rrd1.getRmabsn()).append("</td>") // Serial Number
								.append("<td>").append(rrd1.getRmambsn()).append("</td>") // MB Number
								.append("<td>").append(rrd1.getRmastatement()).append("</td>") // 客戶問題敘述
								.append("<td>").append(rrd1.getRdtrue()).append("</td>") // 復判不良原因
								.append("<td>").append(rrd1.getRdsolve()).append("</td>") // 維修處理事項
								.append("<td>").append(rrd1.getRdexperience()).append("</td>") // 備註
								.append("</tr>");
					}
					httpstr.append("</table> <br>");			
					httpstr.append("<span style='color:red; font-weight:bold;'>※ 本信件由 MES 系統自動發送，請勿直接回覆。如需協助，請洽資訊部。※</span><br>");

					mailService.sendEmail(toUser, toCcUser, subject, httpstr.toString(), null, null);
					resp.autoMsssage("MT006"); // [MT006] 此 (RMA單) 已全部處理完畢並系統通知信件寄出 [Successfully]!!"
				}else {
					resp.autoMsssage("MT007"); //[MT007] 此 (RMA單) 已全部處理完畢但無收件人資訊,無法寄信通知 請通知[管理員]
				}

			}

			return true;
		} catch (Exception e) {

			System.err.println("資料庫存取錯誤：" + e.getMessage());
			return false;
		}
	}

	// 取得 - Customized mode當前表單式查詢資料
	@Transactional
	public boolean getDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		String rmauser = user.getSuname();
		boolean check = false;

		System.out.println(body);

		// 建立空的JSONObject 的object_detail物件 用來存放資料
		JSONObject object_detail = new JSONObject();

	//	if (resp == null) {
	//		resp = new PackageBean();
	//	}

		if (body == null || body.isEmpty()) {
			System.out.println("body 為空或 null");
			return true; // 或者拋出自定義異常
		}

		String input_sn = body.getJSONObject("search").getString("input_sn").trim(); // 輸入號碼
		input_sn = input_sn.equals("") ? null : input_sn;

		// 建立 object_body 物件 為最上層, 用於存放各個資訊
		JSONObject object_body = new JSONObject();

		String search_rma_b_sn = null; // 機台號碼
		String search_rma_mb_sn = null; // M/B 號碼
		String search_rma = null; // RMA 號碼

		// 確保 input_sn 不為 null，否則直接返回
		if (input_sn == null) {
			return check; // 或者拋出自定義異常
		}

		// 先嘗試以 input_sn 作為機台序號 (b_sn) 搜尋 已收到的 RMA stateCheck=1 的資料

		// 排除0與3和4的紀錄,才不會搜尋到 未收到 或 已寄出 的資料 ,在機台還不寄出前的維修紀錄都可以更改寄出後就無法搜尋到資料
		List<RmaList> rma_lis_details = rmaListDao.findAllBysnAndmb(null, null, input_sn, null, Arrays.asList(0, 3,4));
		search_rma_b_sn = input_sn;
		search_rma_mb_sn = null;

		// 如果沒有查到資料，改用 MB 序號搜尋 搜尋 已收到的rma
		if (rma_lis_details == null || rma_lis_details.isEmpty()) {
			search_rma_b_sn = null;
			search_rma_mb_sn = input_sn;
			rma_lis_details = rmaListDao.findAllBysnAndmb(null, null, null, input_sn, Arrays.asList(0, 3,4)); 
			// 如果MB沒有查到資料，改用RMA 搜尋 搜尋 已收到的rma
			if (rma_lis_details == null || rma_lis_details.isEmpty()) {
				search_rma_b_sn = null;
				search_rma_mb_sn = null;
				search_rma = input_sn;
				rma_lis_details = rmaListDao.findAllBysnAndmb(null, input_sn, null, null, Arrays.asList(0, 3,4)); 
				// 若仍未查到資料，回傳錯誤訊息並結束執行
				if (rma_lis_details == null || rma_lis_details.isEmpty()) {
					System.out.println("RmaList查無資料，結束執行。");
					resp.autoMsssage("102"); // 回傳錯誤訊息
					return check;
				}
			}
		}
		// 確認有RmaList資料 才能取得資料 否則rma_lis_details.get(0) 會造成錯誤 "Index 0 out of bounds for
		// length 0，這代表 程式試圖存取一個空的
		RmaList rl = rma_lis_details.get(0); // 取得Rmalist 資料

		// 主要是若是序號有多加_0x,給切除_0x 給 查詢產品細節資料 查詢資料
		// 確保 input_sn 不是空字串
		if (input_sn != null && !input_sn.isEmpty()) {
			// 為了把"_01 / _02 ...尾數切割 去搜尋資料庫
			String[] input_sns = input_sn.split("_");
			// input_sns.length 來確認 String[] 陣列有幾個元素
			if (input_sns.length > 1) {
				// 有兩個以上的項目
				search_rma_b_sn = input_sns[0];
			}
		}

		// 查詢產品細節資料 Table(name="production_body") 故意使用 search_rma(sysmuser) 來對應RMA號碼
		// 來造成查無資料
		List<ProductionBody> check_Bodys = bodyDao.findAllByPbsnAndpbvalue16(search_rma, search_rma_b_sn,
				search_rma_mb_sn);
		// 確認check_Bodys
		if (check_Bodys == null || check_Bodys.isEmpty()) {

			System.out.println("MES查無資料，結束執行。");

			// r1取的RMA清單 顯示在頁面欄位上
			object_detail.put("rma_list_id", rl.getId()); // RMA_id

			object_detail.put("rma_sn", rl.getRmaNumber()); // RMA號碼
			object_detail.put("rma_guest", rl.getCustomer()); // 客戶
			object_detail.put("rma_model", rl.getModel()); // 產品型號
			object_detail.put("rma_part_no", rl.getPartNo()); // Oracle part no
			object_detail.put("rma_b_sn", rl.getSerialNumber()); // 機台號碼
			object_detail.put("rma_mb_sn", rl.getMbNumber()); // MB號碼
			object_detail.put("rma_statement", rl.getIssue()); // 客戶問題敘述

			object_detail.put("rma_pb_b_item", "MES查無資料");
			// return false; // 或者拋出自定義異常
		} else {

			ProductionBody cb = check_Bodys.get(0);// 取得產品節資料列
			System.out.println(check_Bodys);
			// 查詢工單// 用號碼查詢細節 ,PB細節表取的 關聯ID (cb.getPbgid() ) 再用cb.getPbgid() 查詢
			// production_header的資料 關連到 production_records資料
			List<ProductionHeader> check_headers = headerDao.findAllByPhpbgid(cb.getPbgid());
			ProductionHeader ch = check_headers.get(0);
			ProductionRecords pr = ch.getProductionRecords();
			// r1取的RMA清單 顯示在頁面欄位上
			object_detail.put("rma_list_id", rl.getId()); // RMA_id

			object_detail.put("rma_sn", rl.getRmaNumber()); // RMA號碼
			object_detail.put("rma_guest", rl.getCustomer()); // 客戶
			object_detail.put("rma_model", rl.getModel()); // 產品型號
			object_detail.put("rma_part_no", rl.getPartNo()); // Oracle part no
			object_detail.put("rma_b_sn", rl.getSerialNumber()); // 機台號碼
			object_detail.put("rma_mb_sn", rl.getMbNumber()); // MB號碼
			object_detail.put("rma_statement", rl.getIssue()); // 客戶問題敘述

			// 顯示頁面 在 產品細節
			object_detail.put("rma_pb_b_item",
					"工單: " + pr.getPrid() + "\n預定出貨日: " + ch.getPhesdate() + "\n保固年: " + cb.getPbwyears() + "\n產品型號: "
							+ pr.getPrpmodel() + "\n機台序號: " + cb.getPbbsn() + "\nMB序號: " + cb.getPbvalue16()
							+ "\nMB(UUID): " + cb.getPbvalue01() + "\nBios: " + cb.getPbvalue09() + "\nEC: "
							+ cb.getPbvalue08() + "\nLAN1: " + cb.getPbvalue02() + "\nLAN2: " + cb.getPbvalue03()
							+ "\nWifi: " + cb.getPbvalue04() + "\nNVRAM: " + cb.getPbvalue05() + "\nIMEI : "
							+ cb.getPbvalue06() + "\nECN: " + cb.getPbvalue07());

			object_detail.put("rma_user", rmauser); // 維修人員

		}

		JSONArray rma_lists = new JSONArray(); // 建立 rma_lists 就是一個 JSONArray，裡面存放了多個結構相同的 JSONObject。
		// 取得RMA List清單 放在畫面上的RMA清單
		ArrayList<RmaList> rmalists = rmaListDao.findAllByrmaNumber(rl.getRmaNumber());
		if (rmalists.size() > 0) {
			for (RmaList rmalist : rmalists) {
				JSONObject list = new JSONObject();
				list.put("rma_sn", rmalist.getRmaNumber()); // RMA號碼
				list.put("rma_guest", rmalist.getCustomer()); // 客戶
				list.put("rma_model", rmalist.getModel()); // 產品型號

				list.put("rma_b_sn", rmalist.getSerialNumber()); // 機台號碼
				list.put("rma_mb_sn", rmalist.getMbNumber()); // MB號碼
				list.put("rma_statement", rmalist.getIssue()); // 客戶問題敘述
				list.put("state", rmalist.getState()); // 目前進度
				rma_lists.put(list);
			}
			object_detail.put("rma_lists", rma_lists); // 問題敘述
		}

		// 廠內維修紀錄 : 因為維修欄位 沒有MB欄位可以搜尋 ,所以若是產品(序號) search_rma_b_sn 為NULL 就 跳過
		if (search_rma_b_sn != null) {
			// 廠內維修紀錄 : 查詢 ＂通用-維修紀錄＂
			ArrayList<RepairDetail> rds = detailDao.findAllByRdidAndRdruidBat1(search_rma_b_sn);
			String repairhistory = "";
			int item = 1;
			if (rds.size() > 0) {
				for (RepairDetail rd : rds) {
//					RepairRegister rr = rd.getRegister();
//					String rrsn = rr.getRrsn(); // 產品(序號)
					String rdtrue = rd.getRdtrue();// 故障原因
					String rdsolve = rd.getRdsolve(); // 解決問題

					repairhistory = repairhistory
//						    + "\n產品序號: " + rrsn 
							+ "項次: " + item + "\n故障原因 :" + rdtrue + "\n解決問題 :" + rdsolve + "\n";
					item = item + 1;
				}
				object_detail.put("pb_h_item", repairhistory);
				System.out.println(repairhistory);
			} else {
				object_detail.put("pb_h_item", "MES查無資料");
			}
		}

		// 取得RMA維修紀錄 維修的資料 若有先前維修資料要秀出在頁面上
		String rmasn = rl.getRmaNumber();
//		rmasn = rmasn.equals("") ? null : rmasn;
		String rmaBsn = rl.getSerialNumber();
//		rmaBsn = rmaBsn.equals("") ? null : rmaBsn;
		String rmaMbsn = rl.getMbNumber();
//		rmaMbsn = rmaMbsn.equals("") ? null : rmaMbsn;
		// 查詢維修紀錄repair_rma_detail資料庫有無資料,判斷是否顯示在維修頁面上
		List<RepairRmaDetail> rrds = rmaDetailDao.findAllByRmasnAndRmasn(null, rmasn, rmaBsn, rmaMbsn);

		JSONObject Repair_Rma_Detail = new JSONObject(); // 見哩一個物件放維修資料
		if (rrds.size() > 0) {
			RepairRmaDetail rrd = rrds.get(0);
			Repair_Rma_Detail.put("rma_repair_id", rrd.getId()); // //rnaid

			Repair_Rma_Detail.put("rd_true", rrd.getRdtrue()); // //實際故障原因
			Repair_Rma_Detail.put("rd_solve", rrd.getRdsolve()); // 維修處理事項 :
			Repair_Rma_Detail.put("rd_experience", rrd.getRdexperience()); // 維修備註 :
			Repair_Rma_Detail.put("rma_part_sn", rrd.getRmapartsn()); // MB料號 :
			Repair_Rma_Detail.put("packing_list", rrd.getPackinglist()); // 單據號碼 :
			Repair_Rma_Detail.put("rma_result", rrd.getRmaresult());// 維修結果 :

			object_detail.put("Repair_Rma_Detail", Repair_Rma_Detail);
		}

		object_body.put("Customized_detail", object_detail);
		resp.setBody(object_body);
		return true;
	}

	// 報表 查詢 資料清單
	@SuppressWarnings("unchecked")
	public boolean getReportData(PackageBean bean, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		List<RepairRmaDetail> details = new ArrayList<RepairRmaDetail>();
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

		str = str.replace("rd_rma", "rd.rma_sn ");
		str = str.replace("rd_rr_sn", "rd.rma_b_sn ");
		str = str.replace("rd_rr_mb", "rd.rma_mb_sn ");

//		str = str.replace("rd_guest", "rd.rma_guest");
		str = str.replace("rd_", "rd.rma_");
		str = str.replace("rd.rma_true", "rd.rd_true ");
		str = str.replace("rd.rma_solve", "rd.rd_solve ");
		str = str.replace("rd.rma_experience", "rd.rd_experience ");
//		str = str.replace("rr_", "rr.rr_");
//		str = str.replace("ro_", "ro.ro_");

		// Step2.=======Analysis report 查詢SN欄位+產品型號+製令單號 =======
		String nativeQuery = "SELECT rd.* FROM repair_rma_detail rd WHERE ";

		nativeQuery += str;
		nativeQuery += " order by rd.id desc ";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		System.out.println(nativeQuery);
		try {
			Query query = em.createNativeQuery(nativeQuery, RepairRmaDetail.class);
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
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", one.getId());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rma", one.getRmasn()); // RMA 號碼
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_guest", one.getRmaguest()); // 客戶
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_model", one.getRmamodel()); // Model
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_part_no", one.getRmaPartNo()); // part no
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_sn", one.getRmabsn()); // 產品序號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_mb", one.getRmambsn()); // MB號碼
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_statement", one.getRmastatement()); // 客戶問題敘述
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", one.getRdtrue()); // 復判不良原因
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve",one.getRdsolve() == null ? "" : one.getRdsolve()); // 修復過程
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", one.getRdexperience()); // 維修備註
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_part_sn", one.getRmaPartNo()); // MB料號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "packing_list", one.getPackinglist()); // 單據號碼
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rma_result", one.getRmaresult()); // 維修結果

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
}
