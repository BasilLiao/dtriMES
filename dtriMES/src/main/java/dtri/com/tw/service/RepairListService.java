package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.RepairDetail;
import dtri.com.tw.db.entity.RepairOrder;
import dtri.com.tw.db.entity.RepairRegister;
import dtri.com.tw.db.entity.RepairUnit;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.CustomerDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductionRecordsDao;
import dtri.com.tw.db.pgsql.dao.RepairDetailDao;
import dtri.com.tw.db.pgsql.dao.RepairOrderDao;
import dtri.com.tw.db.pgsql.dao.RepairRegisterDao;
import dtri.com.tw.db.pgsql.dao.RepairUnitDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class RepairListService {
	@Autowired
	private RepairUnitDao unitDao;
	@Autowired
	private RepairOrderDao orderDao;
	@Autowired
	private RepairDetailDao detailDao;
	@Autowired
	private RepairRegisterDao registerDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private ProductionHeaderDao headerDao;
	@Autowired
	private ProductionRecordsDao recordsDao;

	@Autowired
	private CustomerDao customerDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<RepairUnit> mUnits = new ArrayList<RepairUnit>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("rdid").descending());
		String search_rd_id = null;
		String search_ro_id = null;
		String search_rd_rr_sn = null;
		String search_rr_pb_type = "產品";

		// 功能-名稱編譯
		// 維修細節
		String rd_id = "維修項目(序號)", /* rd_ro_id = "維修單", */ //
				rd_rr_sn = "產品序號", rd_u_qty = "數量", //
				rd_ru_id = "分配單位ID", rd_statement = "描述問題", //
				rd_true = "實際問題", rd_solve = "解決問題", rd_experience = "維修心得", rd_check = "檢核狀態", //
				rd_svg = "圖片", rd_u_finally = "修復員";
		// 維修登記(物件)
		String /* rr_sn = "產品序號", */ rr_c_sn = "客戶產品(序號)", //
				rr_pr_id = "製令單", rr_ph_p_qty = "製令數量", //
				rr_pr_p_model = "產品型號", rr_ph_w_years = "保固年份", //
				rr_pb_sys_m_date = "生產日期", rr_pb_type = "產品類型", //
				rr_v = "版本號", rr_f_ok = "產品狀態", rr_expired = "保固內?";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", /* sys_sort = "排序", sys_ver = "版本", */ sys_status = "狀態", //
				sys_header = "群組"/* , ui_group_id = "UI_Group_ID" */;

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			// 維修細節
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t(sys_header, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_id", FFS.h_t(rd_id, "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_rr_sn", FFS.h_t(rd_rr_sn, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_ru_id", FFS.h_t(rd_ru_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_u_finally", FFS.h_t(rd_u_finally, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_check", FFS.h_t(rd_check, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_statement", FFS.h_t(rd_statement, "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_svg", FFS.h_t(rd_svg, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_true", FFS.h_t(rd_true, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_solve", FFS.h_t(rd_solve, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_experience", FFS.h_t(rd_experience, "300px", FFM.Wri.W_N));

			// 產品資料
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_c_sn", FFS.h_t(rr_c_sn, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_id", FFS.h_t(rr_pr_id, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_p_qty", FFS.h_t(rr_ph_p_qty, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_p_model", FFS.h_t(rr_pr_p_model, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_expired", FFS.h_t(rr_expired, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_w_years", FFS.h_t(rr_ph_w_years, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_sys_m_date", FFS.h_t(rr_pb_sys_m_date, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_type", FFS.h_t(rr_pb_type, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_v", FFS.h_t(rr_v, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_f_ok", FFS.h_t(rr_f_ok, "100px", FFM.Wri.W_Y));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "100px", FFM.Wri.W_N));

			bean.setHeader(new JSONObject().put("search_header", object_header));

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();

			// 單據細節
			obj_m = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "系統建立", "", FFM.Wri.W_N, "col-md-12", false, n_val, "rd_id", rd_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "rr_pr_id", rr_pr_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "rr_ph_p_qty", rr_ph_p_qty));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "rd_rr_sn", rd_rr_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1", "1", FFM.Wri.W_N, "col-md-1", true, n_val, "rd_u_qty", rd_u_qty));

			s_val = new JSONArray();
			// s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已檢核(收到)").put("key", "1"));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", "2"));
			s_val.put((new JSONObject()).put("value", "轉處理").put("key", "3"));
			s_val.put((new JSONObject()).put("value", "修不好").put("key", "4"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, s_val, "rd_check", rd_check));

			s_val = new JSONArray();
			mUnits = unitDao.findAllByRepairUnit(0L, 0L, null, null, true, null);
			for (RepairUnit oneUnit : mUnits) {
				s_val.put((new JSONObject()).put("value", oneUnit.getRugname()).put("key", oneUnit.getRuid()));
			}
			s_val.put((new JSONObject()).put("value", "全單位").put("key", 0));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, s_val, "rd_ru_id", rd_ru_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "rd_u_finally", rd_u_finally));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "待修中").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已修復").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已報廢").put("key", 2));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "false", FFM.Wri.W_N, "col-md-1", true, s_val, "rr_f_ok", rr_f_ok));

			// 產品或是物件 登記資訊
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "rr_c_sn", rr_c_sn));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "rr_pr_p_model", rr_pr_p_model));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "保固期內").put("key", true));
			s_val.put((new JSONObject()).put("value", "保固過期").put("key", false));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "true", FFM.Wri.W_N, "col-md-1", true, s_val, "rr_expired", rr_expired));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "rr_ph_w_years", rr_ph_w_years));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.DATE, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "rr_pb_sys_m_date", rr_pb_sys_m_date));
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "產品", FFM.Wri.W_Y, "col-md-1", true, s_val, "rr_pb_type", rr_pb_type));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", false, n_val, "rr_v", rr_v));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "開啟(正常)").put("key", "0"));
			s_val.put((new JSONObject()).put("value", "關閉(作廢)").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val, "sys_status", sys_status));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "rd_statement", rd_statement));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val, "rd_true", rd_true));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", true, n_val, "rd_solve", rd_true));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.IMG, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_svg", rd_svg));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-6", false, n_val, "rd_experience", rd_experience));
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// 維修單細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_id", rd_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_rr_sn", rd_rr_sn, n_val));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-2", "rr_pb_type", rr_pb_type, s_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			search_rd_rr_sn = body.getJSONObject("search").getString("rd_rr_sn");
			search_rd_rr_sn = search_rd_rr_sn.equals("") ? null : search_rd_rr_sn;
			search_rd_id = body.getJSONObject("search").getString("rd_id");
			search_rd_id = search_rd_id.equals("") ? null : search_rd_id;
			search_rr_pb_type = body.getJSONObject("search").getString("rr_pb_type");
			search_rr_pb_type = search_rr_pb_type.equals("") ? null : search_rr_pb_type;
		}

		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();

		// 物件
		Long rdruid = 0L;
		List<RepairUnit> units = unitDao.findAllByRepairUnit(null, user.getSuid(), null, null, false, null);
		rdruid = units.size() >= 1 ? units.get(0).getRugid() : 0L;

		ArrayList<RepairDetail> rds = detailDao.findAllByRdidAndRdruid(search_ro_id, search_rd_id, search_rd_rr_sn, search_rr_pb_type, 1, 0, rdruid, page_r);
		// 有沒有資料?
		if (rds.size() > 0) {
			rds.forEach(rd -> {
				JSONObject object_body = new JSONObject();
				int ord = 0;
				RepairRegister rr = rd.getRegister();

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", rd.getSysheader());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", rd.getRdid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_sn", rr.getRrsn());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_ru_id", rd.getRdruid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_finally", rd.getRdufinally());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_check", rd.getRdcheck());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_statement", rd.getRdstatement());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_svg", rd.getRdsvg());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", rd.getRdtrue());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve", rd.getRdsolve() == null ? "" : rd.getRdsolve());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", rd.getRdexperience());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_c_sn", rr.getRrcsn() == null ? "" : rr.getRrcsn());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_id", rr.getRrprid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_p_qty", rr.getRrphpqty());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_p_model", rr.getRrprpmodel());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_expired", rr.getRrexpired());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_ph_w_years", rr.getRrphwyears());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_sys_m_date", rr.getRrpbsysmdate() == null ? "" : rr.getRrpbsysmdate());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pb_type", rr.getRrpbtype());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_v", rr.getRrv());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_f_ok", rr.getRrfok());

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

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("create");
			// JSONArray list_chok = new JSONArray();
			// check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 另存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("save_as");
			// JSONArray list_check = new JSONArray();
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
				// 物件轉換
				JSONObject data = (JSONObject) one;
				RepairDetail obj_b = new RepairDetail();
				// 維修單細節
				ArrayList<RepairDetail> obj_bs = detailDao.findAllByRdid(data.getString("rd_id"));
				if (obj_bs.size() == 1) {
					obj_b = obj_bs.get(0);
					obj_b.setRdexperience(data.getString("rd_experience"));
					obj_b.setRdstatement(data.getString("rd_statement"));
					obj_b.setRdtrue(data.getString("rd_true"));
					obj_b.setRdsolve(data.getString("rd_solve"));
					obj_b.setRdsvg(data.getString("rd_svg"));
					obj_b.setSysmdate(new Date());
					obj_b.setSysmuser(user.getSuaccount());
					detailDao.save(obj_b);
					check = true;
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
				detailDao.deleteByRdid(data.getString("rd_id"));
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
	public boolean updateDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONObject list = body.getJSONObject("modify");
			JSONObject rd_old_one = list.getJSONObject("repair");// 只改單一筆資料?
			JSONArray rd_new_list = new JSONArray(list.getJSONArray("detail"));// 要添加其他維修?

			// ====[資料檢核]====
			// 維修單-項目
			if (rd_old_one.getString("rd_type").equals("") || rd_old_one.getString("rd_check").equals("") || //
					rd_old_one.getString("rd_true").equals("") || rd_old_one.getString("rd_solve").equals("") || //
					!rd_old_one.has("rd_id") || rd_old_one.getString("rd_id").equals("")) {
				resp.autoMsssage("MT005");
				return check;
			}

			// 維修單細節-資料
			for (int ch_d = 0; ch_d < rd_new_list.length(); ch_d++) {
				JSONObject data = (JSONObject) rd_new_list.getJSONObject(ch_d);
				// Step1.帶入 產品資訊
				List<ProductionBody> bodys = bodyDao.findAllByPbbsn(data.getString("rr_sn"));
				if (!data.getString("rr_sn").equals("") && bodys.size() == 1) {
					// Step2.帶入 製令資訊
					List<ProductionHeader> headers = headerDao.findAllByPhpbgid(bodys.get(0).getPbgid());
					if (headers.size() == 1) {
						data.put("rr_pr_id", headers.get(0).getProductionRecords().getPrid());
						data.put("rr_ph_p_qty", headers.get(0).getPhpqty());
						data.put("rr_ph_w_years", headers.get(0).getPhwyears());
						data.put("rr_pr_p_model", headers.get(0).getProductionRecords().getPrpmodel());
						data.put("rr_pb_sys_m_date", Fm_Time.to_yMd_Hms(bodys.get(0).getSysmdate()));// 產品製造日期 = 產品最後修改時間
					} else {
						resp.autoMsssage("WK004");
						check = false;
						return check;
					}
					// Step3.檢查資訊正確性
					data.put("rr_v", data.has("rr_v") ? data.getString("rr_v") : "");
					data.put("rd_check", data.getString("rd_check").equals("") ? 1 : data.getInt("rd_check"));
					data.put("rd_ru_id", data.getString("rd_ru_id").equals("") ? 0L : data.getLong("rd_ru_id"));
					data.put("rr_pb_type", data.getString("rr_pb_type").equals("") ? "其他" : data.getString("rr_pb_type"));
					data.put("rd_statement", data.getString("rd_statement").equals("") ? "Something project wrong" : data.getString("rd_statement"));
					rd_new_list.put(ch_d, data);
				} else {
					data.put("rr_pr_id", "");
					data.put("rr_ph_p_qty", 0);
					data.put("rr_ph_w_years", 0);
					data.put("rr_pb_sys_m_date", Fm_Time.to_yMd_Hms(new Date()));

					data.put("rr_v", data.has("rr_v") ? data.getString("rr_v") : "");
					data.put("rd_check", data.getString("rd_check").equals("") ? 1 : data.getInt("rd_check"));
					data.put("rd_type", "無法判定");

					data.put("rd_ru_id", data.getString("rd_ru_id").equals("") ? 0L : data.getLong("rd_ru_id"));
					data.put("rr_pb_type", data.getString("rr_pb_type").equals("") ? "產品" : data.getString("rr_pb_type"));
					data.put("rd_statement", data.getString("rd_statement").equals("") ? "Something project wrong" : data.getString("rd_statement"));
					rd_new_list.put(ch_d, data);
				}
			}

			// ====[資料更新]====
			// Step1. 維修單自動生成
			Date today = new Date();
			String yyyy_MM_dd_HH_mm_ss = Fm_Time.to_y_M_d(today) + " 00:00:00";
			Date todayStr = Fm_Time.toDateTime(yyyy_MM_dd_HH_mm_ss);// 今日起始
			String ro_id = "DTR" + todayStr.getTime();
			// 客戶
			ArrayList<Customer> customers = customerDao.findAllByCustomer(0L, "美商定誼", null, 0, null);
			ArrayList<RepairOrder> orders = orderDao.findAllByRoid(ro_id);
			// 如果有維修清單List
			if (rd_new_list.length() > 0) {
				// 共用
				RepairOrder ro_one = new RepairOrder();// 維修單資料
				RepairDetail rd_one = new RepairDetail();// 維修單-問題清單
				ro_one.setSysnote("");
				ro_one.setSysstatus(0);
				ro_one.setSyscdate(new Date());
				ro_one.setSysmdate(new Date());
				ro_one.setSysmuser(user.getSuaccount());
				ro_one.setSyscuser(user.getSuaccount());

				// Step2. 今日[尚未]建立
				if (orders.size() == 0) {
					ro_one.setRoid(ro_id);
					ro_one.setRocid(customers.get(0).getCid());
					ro_one.setRocheck(0);
					ro_one.setRofrom("DTR");
					ro_one.setRoramdate(new Date());
					ro_one.setDetails(null);
					ro_one.setSysheader(true);
					orderDao.save(ro_one);
				} else {
					ro_one = orders.get(0);
					ro_one.setSysmdate(new Date());
					ro_one.setSysmuser(user.getSuaccount());
				}

				// Step3.維修單-項目 (新增-登記子類別+產品登記)
				if (rd_new_list.length() > 0) {
					for (Object rd_new_one : rd_new_list) {
						JSONObject data = (JSONObject) rd_new_one;
						RepairRegister rr_one = new RepairRegister();
						ArrayList<RepairRegister> rrs = registerDao.findAllByRrsn(data.getString("rr_sn"));
						if (rrs.size() == 1) {
							rr_one = rrs.get(0);
						} else {
							rr_one.setRrsn(data.getString("rr_sn"));
							rr_one.setRrcsn(data.getString("rr_c_sn"));
							rr_one.setRrprid(data.getString("rr_pr_id"));
							rr_one.setRrphpqty(data.getInt("rr_ph_p_qty"));
							rr_one.setRrprpmodel(data.getString("rr_pr_p_model"));
							rr_one.setRrexpired(true);// rr_ph_w_years
							rr_one.setRrphwyears(data.getInt("rr_ph_w_years"));
							rr_one.setRrpbsysmdate(Fm_Time.toDateTime(data.getString("rr_pb_sys_m_date")));
							rr_one.setRrpbtype(data.getString("rr_pb_type"));
							rr_one.setRrv(data.getString("rr_v"));
							rr_one.setRrfok(0);
							rr_one.setSysnote("");
							rr_one.setSysstatus(0);
							rr_one.setSyscdate(ro_one.getSyscdate());
							rr_one.setSyscuser(ro_one.getSyscuser());
						}
						rr_one.setSysmdate(ro_one.getSysmdate());
						rr_one.setSysmuser(ro_one.getSysmuser());
						registerDao.save(rr_one);
						// 維修單細節
						Boolean check_rep = true;
						int rd_id_nb = 1;
						String rd_id = "S" + String.format("%04d", rd_id_nb++);
						while (check_rep) {
							if (detailDao.findAllByRdid(ro_id + '-' + rd_id).size() > 0) {
								rd_id = "S" + String.format("%04d", rd_id_nb++);
							} else {
								check_rep = false;
							}
						}
						rd_one.setRdid(ro_id + '-' + rd_id);
						rd_one.setRdstatement(data.getString("rd_statement"));
						rd_one.setRdruid(data.getLong("rd_ru_id"));
						rd_one.setRduqty(data.getInt("rd_u_qty"));
						rd_one.setRdtrue("");
						rd_one.setRdexperience("");
						rd_one.setRdcheck(data.getInt("rd_check"));
						rd_one.setRdufinally("");
						rd_one.setOrder(ro_one);

						rd_one.setSysnote("");
						rd_one.setSysstatus(ro_one.getSysstatus());
						rd_one.setSyscdate(ro_one.getSyscdate());
						rd_one.setSysmdate(ro_one.getSysmdate());
						rd_one.setSysmuser(ro_one.getSysmuser());
						rd_one.setSyscuser(ro_one.getSyscuser());
						rd_one.setSysheader(false);
						rd_one.setRegister(rr_one);
						detailDao.save(rd_one);
						check = true;
					}
				}
			}
			// Step4.維修單-單一項目
			if (rd_old_one.has("rd_id")) {
				ArrayList<RepairDetail> rds = detailDao.findAllByRdid(rd_old_one.getString("rd_id"));
				if (rds.size() == 1) {
					RepairDetail rd = rds.get(0);
					RepairRegister rr = rd.getRegister();
					rr.setRrpbtype(rd_old_one.getString("rr_pb_type"));
					rr.setRrv(rd_old_one.getString("rr_v"));
					rr.setRrfok(1);

					rd.setRdufinally(user.getSuaccount());
					rd.setRdstatement(rd_old_one.getString("rd_statement"));
					rd.setRdtrue(rd_old_one.getString("rd_true"));
					rd.setRdsolve(rd_old_one.getString("rd_solve"));
					rd.setRdexperience(rd_old_one.getString("rd_experience"));
					rd.setRdcheck(rd_old_one.getInt("rd_check"));
					rd.setRdtype(rd_old_one.getString("rd_type"));
					rd.setRdsvg(rd_old_one.getString("rd_svg"));
					rd.setRegister(rr);
					rd.setSysmdate(new Date());
					rd.setSysmuser(user.getSuaccount());
					rd.setSysnote(rd_old_one.getString("sys_note"));

					// 已處理(尚未選)
					if (rd_old_one.getInt("rd_check") == 1) {
						resp.autoMsssage("MT005");
						check = false;
						return check;
					}
					// 已處理(修復)
					if (rd_old_one.getInt("rd_check") == 2) {
						int rr_f_ok = 1;
						// 檢查此產品是否已修復? 或是報廢
						for (RepairDetail repairDetail : rds) {
							if (repairDetail.getRdcheck() != 2) {
								// 修復
								rr_f_ok = 0;
							}
							if (repairDetail.getRdcheck() == 4) {
								// 報廢
								rr_f_ok = 2;
							}
						}
						rr.setRrfok(rr_f_ok);
						rd.setRegister(rr);
						detailDao.save(rd);
						// 檢查是否 此單據 全都修復完畢
						String rd_ro_id = rd.getRdid().split("-")[0];
						ArrayList<RepairOrder> ros = orderDao.findAllByRoid(rd_ro_id);
						List<RepairDetail> ro_rds = ros.get(0).getDetails();
						boolean rd_check = true;
						for (Iterator<RepairDetail> iterator = ro_rds.iterator(); iterator.hasNext();) {
							RepairDetail repairDetail = iterator.next();
							if (repairDetail.getRdcheck() < 2) {
								rd_check = false;
								break;
							}
						}
						// 如果都修好? 進行寫入 完成時間
						if (rd_check) {
							ros.get(0).setRoedate(new Date());
							ros.get(0).setSysmdate(new Date());
							ros.get(0).setSysmuser(user.getSuaccount());
							orderDao.save(ros.get(0));
						}
						// 如果 工作站 程序上 有故障代碼?
						List<ProductionBody> bodies = bodyDao.findAllByPbbsn(rr.getRrsn());
						if (bodies.size() == 1) {
							bodies.get(0).setPbfnote("");
							bodies.get(0).setPbfvalue("");
							bodyDao.save(bodies.get(0));
						}
					}
					// 轉處理
					if (rd_old_one.getInt("rd_check") == 3) {
						rd_old_one.getLong("rd_ru_id");
						// 新建-維修單細節
						RepairDetail rd_old = rds.get(0);
						RepairDetail rd_new = new RepairDetail();
						Boolean check_rep = true;
						int rd_id_nb = 1;
						String rd_id = "S" + String.format("%04d", rd_id_nb++);
						while (check_rep) {
							if (detailDao.findAllByRdid(ro_id + '-' + rd_id).size() > 0) {
								rd_id = "S" + String.format("%04d", rd_id_nb++);
							} else {
								check_rep = false;
							}
						}
						// 維修單資料
						// Step2. 今日[尚未]建立
						RepairOrder ro_one = new RepairOrder();// 維修單資料
						if (orders.size() == 0) {
							ro_one.setRoid(ro_id);
							ro_one.setRocid(customers.get(0).getCid());
							ro_one.setRocheck(0);
							ro_one.setRofrom("DTR");
							ro_one.setRoramdate(new Date());
							ro_one.setDetails(null);
							ro_one.setSysheader(true);
							orderDao.save(ro_one);
						} else {
							ro_one = orders.get(0);
							ro_one.setSysmdate(new Date());
							ro_one.setSysmuser(user.getSuaccount());
						}

						rd_new.setOrder(ro_one);
						rd_new.setRegister(rr);
						rd_new.setRdid(ro_id + '-' + rd_id);
						rd_new.setRdstatement(rd_old.getRdstatement());
						rd_new.setRduqty(rd_old.getRduqty());
						rd_new.setRdtrue(rd_old.getRdtrue());
						rd_new.setRdexperience(rd_old.getRdexperience());
						rd_new.setRdufinally("");
						// 轉處理
						rd_new.setRdruid(rd_old_one.getLong("rd_ru_id"));
						rd_new.setRdcheck(1);
						// 系統
						rd_new.setSysnote(rd_old_one.getString("sys_note"));
						rd_new.setSysstatus(0);
						rd_new.setSyscdate(new Date());
						rd_new.setSysmdate(new Date());
						rd_new.setSysmuser(user.getSuaccount());
						rd_new.setSyscuser(user.getSuaccount());
						rd_new.setSysheader(false);
						detailDao.save(rd_new);
						detailDao.save(rd);
						rd_old_one.put("rd_id", ro_id + '-' + rd_id);
					}
					// 報廢
					if (rd_old_one.getInt("rd_check") == 4) {
						rr.setRrfok(3);
						rd.setRegister(rr);
						detailDao.save(rd);
					}
				}
			}
			JSONObject cb = req.getCall_bk_vals();
			cb.put("search", false);
			cb.put("rr_sn", rd_old_one.getString("rr_sn"));
			cb.put("rd_id", rd_old_one.getString("rd_id"));
			req.setCall_bk_vals(cb);

			check = true;
		} catch (Exception e) {
			System.out.println(e);
			check = false;
		}
		return check;
	}

	// 取得 - Customized mode當前表單式查詢資料
	@Transactional
	public boolean getDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		if (resp == null) {
			resp = new PackageBean();
		}
		// 查詢
		String search_rr_sn = null;// 產品序號
		String search_rd_id = null;// 維修項目
		String search_ro_id = null;// 維修單據

		// 維修單據
		String rd_id = "No.", rr_pr_p_model = "Model", rr_sn = "P/N(DTR)", rr_c_sn = "P/N(client)", rr_pb_type = "Type", //
				rd_statement = "Failure Description", rd_u_qty = "Qty", rr_expired = "Warranty?", rd_ru_id = "To whom", rd_check = "Status";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = resp.getHeader();
			JSONObject customized_header = new JSONObject();
			int ord = 0;
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_id", FFS.h_t(rd_id, "80px", FFM.Wri.W_N));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_p_model", FFS.h_t(rr_pr_p_model, "100px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_sn", FFS.h_t(rr_sn, "150px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_c_sn", FFS.h_t(rr_c_sn, "150px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_type", FFS.h_t(rr_pb_type, "90px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_statement", FFS.h_t(rd_statement, "350px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_u_qty", FFS.h_t(rd_u_qty, "70px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_expired", FFS.h_t(rr_expired, "120px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_ru_id", FFS.h_t(rd_ru_id, "120px", FFM.Wri.W_Y));
			customized_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_check", FFS.h_t(rd_check, "130px", FFM.Wri.W_Y));
			object_header.put("customized_header", customized_header);

			// 維修單-訊息
			JSONArray s_val = new JSONArray();
			JSONObject object_documents = new JSONObject();
			// 維修單位
			List<RepairUnit> mUnits = unitDao.findAllByRepairUnit(0L, 0L, null, null, true, null);
			for (RepairUnit oneUnit : mUnits) {
				String oneUnit_one = oneUnit.getRugname();
				s_val.put((new JSONObject()).put("value", oneUnit_one).put("key", oneUnit.getRuid()));
			}
			s_val.put((new JSONObject()).put("value", "全單位").put("key", 0));
			object_documents.put("rd_ru_id", s_val);
			// 單據狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "未結單").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已結單").put("key", 1));
			object_documents.put("ro_check", s_val);
			// 類型
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "產品").put("key", "產品"));
			s_val.put((new JSONObject()).put("value", "配件").put("key", "配件"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			s_val.put((new JSONObject()).put("value", "小板").put("key", "小板"));
			s_val.put((new JSONObject()).put("value", "零件").put("key", "零件"));
			s_val.put((new JSONObject()).put("value", "軟體").put("key", "軟體"));
			s_val.put((new JSONObject()).put("value", "其他").put("key", "其他"));
			object_documents.put("rr_pb_type", s_val);
			// 處理狀態
			s_val = new JSONArray();
			// s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "待處理(未修)").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "轉處理(踢球)").put("key", 3));
			s_val.put((new JSONObject()).put("value", "修不好(報廢)").put("key", 4));
			// s_val.put((new JSONObject()).put("value", "已寄出(結單)").put("key", 5));
			object_documents.put("rd_check_2", s_val);

			// 處理狀態
			s_val = new JSONArray();
			// s_val.put((new JSONObject()).put("value", "已申請(未到)").put("key", 0));
			s_val.put((new JSONObject()).put("value", "待處理(未修)").put("key", 1));
			// s_val.put((new JSONObject()).put("value", "已處理(修復)").put("key", 2));
			s_val.put((new JSONObject()).put("value", "修不好(報廢)").put("key", 4));
			// s_val.put((new JSONObject()).put("value", "已寄出(結單)").put("key", 5));
			object_documents.put("rd_check", s_val);
			// 處理狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "無法判定").put("key", "無法判定"));
			s_val.put((new JSONObject()).put("value", "材料").put("key", "材料"));
			s_val.put((new JSONObject()).put("value", "組裝").put("key", "組裝"));
			s_val.put((new JSONObject()).put("value", "外包").put("key", "外包"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			object_documents.put("rd_type", s_val);

			// 產品狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "待修中").put("key", 0));
			s_val.put((new JSONObject()).put("value", "已修復").put("key", 1));
			s_val.put((new JSONObject()).put("value", "已修復").put("key", 2));
			object_documents.put("rr_f_ok", s_val);

			// 處理狀態
			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "保固期內").put("key", true));
			s_val.put((new JSONObject()).put("value", "保固過期").put("key", false));
			object_documents.put("rr_expired", s_val);

			// 數字格式
			object_documents.put("rd_u_qty", "number");
			object_header.put("customized_documents", object_documents);
			resp.setHeader(object_header);
		} else {
			// 進行-特定查詢
			search_rr_sn = body.getJSONObject("search").getString("rr_sn");
			search_rr_sn = search_rr_sn.equals("") ? null : search_rr_sn;
			search_rd_id = body.getJSONObject("search").getString("rd_id");
			search_rd_id = search_rd_id.equals("") ? null : search_rd_id;
		}

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		// 維修單-問題清單+產品
		JSONObject object_body = resp.getBody();
		if (object_body == null) {
			object_body = new JSONObject();
		}

		// 預設維修
		JSONObject object_detail = new JSONObject();
		object_detail.put("rd_ru_id", "");
		object_detail.put("rr_sn", "");

		// Step1.維修人員?
		Long rdruid = user.getSuid();
		List<RepairUnit> units = unitDao.findAllByRepairUnit(null, rdruid, null, null, false, null);
		if (units.size() == 0) {
			resp.autoMsssage("MT004");
			return false;
		} else {
			object_detail.put("rd_ru_id", units.get(0).getRugid());
		}

		// Step2.所屬的[維修人員]產品 or 維修單項目?
		Long rugid = units.get(0).getRugid();
		ArrayList<RepairDetail> details = new ArrayList<RepairDetail>();

		details = detailDao.findAllByRdidAndRdruid(search_ro_id, search_rd_id, search_rr_sn, null, 1, 0, rugid, null);
		if (details.size() >= 1 && (search_rr_sn != null || search_rd_id != null)) {
			// 有相關資料帶出第一筆資料
			RepairDetail rd = details.get(0);
			RepairRegister rr = rd.getRegister();
			object_detail.put("rd_id", rd.getRdid());
			object_detail.put("rr_sn", rd.getRegister().getRrsn());
			object_detail.put("rr_pb_type", rr.getRrpbtype());
			object_detail.put("rr_v", rr.getRrv());
			object_detail.put("rr_f_ok", rr.getRrfok() + "");
			object_detail.put("rr_pr_p_model", rr.getRrprpmodel());

			object_detail.put("rd_check", rd.getRdcheck());
			object_detail.put("rd_svg", rd.getRdsvg());
			object_detail.put("rd_statement", rd.getRdstatement());
			object_detail.put("rd_true", rd.getRdtrue());
			object_detail.put("rd_solve", rd.getRdsolve());
			object_detail.put("rd_experience", rd.getRdexperience());

			// 如果有:產品規格
			if (rr.getRrprid() != null) {
				ArrayList<ProductionRecords> pr = recordsDao.findAllByPrid(rr.getRrprid(), null);
				if (pr.size() == 1) {
					object_detail.put("pr_b_item", pr.get(0).getPrbitem().replaceAll("},", "}, \n"));// 硬體
					object_detail.put("pr_s_item", pr.get(0).getPrsitem().replaceAll("},", "}, \n"));// 軟體
				}
			}
			// 如果有:有零件
			List<ProductionBody> pbs = bodyDao.findAllByPbbsn(rd.getRegister().getRrsn());
			if (pbs.size() == 1) {
				// sn關聯表
				ProductionBody body_title = bodyDao.findAllByPbid(0l).get(0);
				ProductionBody body_context = pbs.get(0);
				JSONObject pr_i_item = new JSONObject();
				int j = 0;
				Method method_title;
				Method method_context;
				// sn關聯表
				for (j = 0; j < 50; j++) {
					String m_name = "getPbvalue" + String.format("%02d", j + 1);
					try {
						// 零件名稱
						method_title = body_title.getClass().getMethod(m_name);
						String name = (String) method_title.invoke(body_title);
						// 零件值
						method_context = body_context.getClass().getMethod(m_name);
						String value = (String) method_context.invoke(body_context);

						if (value != null && !value.equals("")) {
							pr_i_item.put(name, value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 有登記的硬體資訊
				object_detail.put("pb_i_item", pr_i_item.toString().replaceAll(",\"", ",\" \n"));
			}
		} else if (search_rr_sn != null || search_rd_id != null) {
			resp.autoMsssage("102");
			return false;
		}
		object_body.put("customized_detail", object_detail);
		resp.setBody(object_body);
		return true;
	}
}
