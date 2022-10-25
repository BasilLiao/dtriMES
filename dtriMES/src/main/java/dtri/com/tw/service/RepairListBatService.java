package dtri.com.tw.service;

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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.RepairDetail;
import dtri.com.tw.db.entity.RepairOrder;
import dtri.com.tw.db.entity.RepairRegister;
import dtri.com.tw.db.entity.RepairUnit;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.RepairDetailDao;
import dtri.com.tw.db.pgsql.dao.RepairOrderDao;
import dtri.com.tw.db.pgsql.dao.RepairUnitDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class RepairListBatService {
	@Autowired
	private RepairUnitDao unitDao;
	@Autowired
	private RepairDetailDao detailDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private RepairOrderDao orderDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<RepairUnit> mUnits = new ArrayList<RepairUnit>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		page = 0;
		p_size = 99999;

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("rdid").descending());
		String search_rd_id = null; // 維修序號
		String search_rr_sn = null; // 產品序號
		String search_rr_pr_id = null; // 製令單
		String search_rd_statement = null; // 問題-現象敘述
		// 功能-名稱編譯
		// 維修細節
		String rd_id = "維修項目(序號)", /* rd_ro_id = "維修單", */ //
				rd_rr_sn = "產品序號", /* rd_u_qty = "數量", */ //
				rd_ru_id = "分配單位ID", rd_f_analyst = "(優先)故障分析人員", rd_statement = "描述問題", //
				rd_true = "實際問題", rd_solve = "解決問題", rd_experience = "維修備註", rd_check = "檢核狀態", //
				rd_svg = "圖片", rd_u_finally = "修復員", rd_type = "故障類型";
		// 維修登記(物件)
		String rr_sn = "產品序號", rr_c_sn = "客戶產品(序號)", //
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
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_id", FFS.h_t(rr_pr_id, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_check", FFS.h_t(rd_check, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_type", FFS.h_t(rd_type, "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_statement", FFS.h_t(rd_statement, "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_svg", FFS.h_t(rd_svg, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_true", FFS.h_t(rd_true, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_solve", FFS.h_t(rd_solve, "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_experience", FFS.h_t(rd_experience, "300px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_ru_id", FFS.h_t(rd_ru_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_u_finally", FFS.h_t(rd_u_finally, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rd_f_analyst", FFS.h_t(rd_f_analyst, "180px", FFM.Wri.W_Y));

			// 產品資料
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_c_sn", FFS.h_t(rr_c_sn, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_p_qty", FFS.h_t(rr_ph_p_qty, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pr_p_model", FFS.h_t(rr_pr_p_model, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_expired", FFS.h_t(rr_expired, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_ph_w_years", FFS.h_t(rr_ph_w_years, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_sys_m_date", FFS.h_t(rr_pb_sys_m_date, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_pb_type", FFS.h_t(rr_pb_type, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_v", FFS.h_t(rr_v, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "rr_f_ok", FFS.h_t(rr_f_ok, "100px", FFM.Wri.W_N));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "100px", FFM.Wri.W_N));

			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray n_val = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray obj_m = new JSONArray();
			obj_m.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:38GW12239X547", "col-md-2", "rd_rr_sn", rd_rr_sn, n_val));
			obj_m.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:螺絲沒拴緊", "col-md-3", "rd_true", rd_true, n_val));
			obj_m.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:拴緊就好了", "col-md-2", "rd_solve", rd_solve, n_val));
			obj_m.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:提醒組裝人員", "col-md-2", "rd_experience", rd_experience, n_val));
			s_val = new JSONArray();
			mUnits = unitDao.findAllByRepairUnit(0L, 0L, null, null, null, false, null);
			for (RepairUnit oneUnit : mUnits) {
				s_val.put((new JSONObject()).put("value", oneUnit.getRusuname()).put("key", oneUnit.getRusuaccount()));
			}
			obj_m.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-2", "rd_u_finally", rd_u_finally, s_val));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "無法判定").put("key", "無法判定"));
			s_val.put((new JSONObject()).put("value", "材料").put("key", "材料"));
			s_val.put((new JSONObject()).put("value", "組裝").put("key", "組裝"));
			s_val.put((new JSONObject()).put("value", "外包").put("key", "外包"));
			s_val.put((new JSONObject()).put("value", "主板").put("key", "主板"));
			obj_m.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "無法判定", "col-md-1", "rd_type", rd_type, s_val));

			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// 維修單細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:A511-123456", "col-md-2", "rr_pr_id", rr_pr_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rd_id", rd_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "rr_sn", rr_sn, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "Ex:亮度不足", "col-md-2", "rd_statement", rd_statement, n_val));
			bean.setCell_searchs(object_searchs);

		} else {
			// 進行-特定查詢
			search_rr_sn = body.getJSONObject("search").getString("rr_sn");
			search_rr_sn = search_rr_sn.equals("") ? null : search_rr_sn;

			search_rd_id = body.getJSONObject("search").getString("rd_id");
			search_rd_id = search_rd_id.equals("") ? null : search_rd_id;

			search_rd_statement = body.getJSONObject("search").getString("rd_statement");
			search_rd_statement = search_rd_statement.equals("") ? null : search_rd_statement;

			search_rr_pr_id = body.getJSONObject("search").getString("rr_pr_id");
			search_rr_pr_id = search_rr_pr_id.equals("") ? null : search_rr_pr_id;
		}
		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		// 物件
		ArrayList<RepairDetail> rds = detailDao.findAllByRdidAndRdruidBat(search_rd_id, search_rr_sn, search_rr_pr_id, search_rd_statement, 1, page_r);
		// 有沒有資料?
		if (rds.size() > 0) {
			rds.forEach(rd -> {
				JSONObject object_body = new JSONObject();
				int ord = 0;
				RepairRegister rr = rd.getRegister();

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", rd.getSysheader());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_id", rd.getRdid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_rr_sn", rr.getRrsn());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_pr_id", rr.getRrprid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_check", rd.getRdcheck());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_type", rd.getRdtype() == null ? "" : rd.getRdtype());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_statement", rd.getRdstatement());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_svg", rd.getRdsvg());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_true", rd.getRdtrue());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_solve", rd.getRdsolve() == null ? "" : rd.getRdsolve());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_experience", rd.getRdexperience());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_ru_id", rd.getRdruid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_u_finally", rd.getRdufinally());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rd_f_analyst", rd.getRdfanalyst() == null ? "" : rd.getRdfanalyst());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "rr_c_sn", rr.getRrcsn() == null ? "" : rr.getRrcsn());
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

	// 更新 資料清單
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("modify");
			RepairDetail rd = new RepairDetail();
			System.out.println(list);
			for (Object object : list) {
				JSONObject one = (JSONObject) object;
				ArrayList<RepairDetail> rds = detailDao.findAllByRdid(one.getString("rd_id"));
				if (rds != null && rds.size() == 1) {
					rd = rds.get(0);
					rd.getRegister().setRrfok(1);
					rd.getRegister().setSysmdate(new Date());
					rd.getRegister().setSysmuser(user.getSuaccount());

					rd.setRdtrue(one.getString("rd_true"));
					rd.setRdsolve(one.getString("rd_solve"));
					rd.setRdexperience(one.getString("rd_experience"));
					rd.setRdufinally(one.getString("rd_u_finally"));
					rd.setRdtype(one.getString("rd_type"));
					rd.setRdcheck(2);

					rd.setSysmdate(new Date());
					rd.setSysmuser(user.getSuaccount());
					detailDao.save(rd);
					// 如果 工作站 程序上 有故障代碼?
					List<ProductionBody> bodies = bodyDao.findAllByPbbsn(rd.getRegister().getRrsn());
					if (bodies.size() == 1) {
						bodies.get(0).setPbfnote("");
						bodies.get(0).setPbfvalue("");
						bodyDao.save(bodies.get(0));
					}
				}
			}
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
}
