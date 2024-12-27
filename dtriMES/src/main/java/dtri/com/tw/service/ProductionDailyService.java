package dtri.com.tw.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.bean.ProductionDailyBean;
import dtri.com.tw.db.entity.ProductionDaily;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationClass;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductiondailyDao;
import dtri.com.tw.db.pgsql.dao.RepairRegisterDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductionDailyService {
	private static final Logger log = LoggerFactory.getLogger(ProductionDailyService.class);
	@Autowired
	private ProductiondailyDao dailyDao;

	@Autowired
	private RepairRegisterDao registerDao;

	@Autowired
	private WorkstationClassDao classDao;

	@Autowired
	private WorkstationDao workstationDao;

	@Autowired
	private WorkstationProgramDao programDao;

	@Autowired
	private SystemUserDao userDao;

	@Autowired
	private ProductionHeaderDao headerDao;

	@Autowired
	private ProductionBodyDao bodyDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		// int p_size = req.getPage_total();
		boolean checkFirst = false;
		ArrayList<ProductionDaily> productionDailys = new ArrayList<ProductionDaily>();
		ArrayList<Workstation> workstations = new ArrayList<Workstation>();
		LinkedHashMap<String, ProductionDailyBean> dailybeans = new LinkedHashMap<String, ProductionDailyBean>();

		// 查詢
		String sc_s_date = null;
		String sc_e_date = null;
		String sc_bom_id = null;
		String sc_model = null;
		String sc_class = null;
		String sc_line = null;
		String sc_id = null;

		// 功能-名稱編譯
		String pd_wc_class = "班別", sys_m_date = "時間", //
				pd_wc_line = "產線", pr_bom_id = "BOM號", pd_pr_id = "工單號", //
				pd_pr_p_model = "產品型號", pd_pr_total = "工單總數", pd_bad_qty = "待修數", //
				pd_tt_qty = "測試次數", pd_tt_bad_qty = "故障次數", pd_tt_yield = "測試次數(不良率)", //
				pd_pr_ok_qty = "完成總數", pd_pr_tt_ok_qty = "測試完成總數", pd_pr_bad_qty = "測試故障總數", pd_pr_yield = "測試總數(不良率)", //
				pd_t_qty = "日完成數", pd_w_names = "各站作業員工(清單)", sys_note = "備註";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			checkFirst = true;
			workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			JSONObject object_dp = new JSONObject();
			JSONObject object_dp_all = new JSONObject();
			JSONObject object_yield = new JSONObject();
			JSONObject object_dwh = new JSONObject();
			JSONObject object_dnoe = new JSONObject();

			// 總數量_header_dp
			int ord_dp = 0;
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_p_model",
					FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_total",
					FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_ok_qty",
					FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_bad_qty", FFS.h_t(pd_bad_qty, "110px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));
			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + w_one.getWcname(),
							FFS.h_t(w_one.getWpbname(), "110px", FFM.Wri.W_Y));
			}
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總數量累計_header_dp_all
			int ord_dp_all = 0;
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "sys_m_date",
					FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_wc_line",
					FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_wc_class",
					FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_id",
					FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pr_bom_id",
					FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_p_model",
					FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_total",
					FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_ok_qty",
					FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_t_qty",
					FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));
			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + w_one.getWcname(),
							FFS.h_t(w_one.getWpbname(), "110px", FFM.Wri.W_Y));
			}
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "sys_note",
					FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 良率_header_yield
			int ord_yield = 0;
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "sys_m_date",
					FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_wc_line",
					FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_wc_class",
					FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_id",
					FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pr_bom_id",
					FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_p_model",
					FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_total",
					FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));

			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_tt_qty",
					FFS.h_t(pd_tt_qty, "130px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_tt_bad_qty",
					FFS.h_t(pd_tt_bad_qty, "130px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_tt_yield",
					FFS.h_t(pd_tt_yield, "150px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_tt_ok_qty",
					FFS.h_t(pd_pr_tt_ok_qty, "130px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_bad_qty",
					FFS.h_t(pd_pr_bad_qty, "130px", FFM.Wri.W_Y));
			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "pd_pr_yield",
					FFS.h_t(pd_pr_yield, "150px", FFM.Wri.W_Y));

			object_yield.put(FFS.ord((ord_yield += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總工時
			int ord_dwh = 0;
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "sys_m_date",
					FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_wc_class",
					FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_p_model",
					FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_total",
					FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_ok_qty",
					FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));

			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + w_one.getWcname(),
							FFS.h_t(w_one.getWpbname(), "110px", FFM.Wri.W_Y));
			}
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總人數
			int ord_dnoe = 0;
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "sys_m_date",
					FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_wc_line",
					FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_wc_class",
					FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pr_bom_id",
					FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_p_model",
					FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_total",
					FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_ok_qty",
					FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));

			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + w_one.getWcname(),
							FFS.h_t(w_one.getWpbname(), "110px", FFM.Wri.W_Y));
			}
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_w_names",
					FFS.h_t(pd_w_names, "550px", FFM.Wri.W_Y));

			object_header.put("header_dp", object_dp);
			object_header.put("header_dp_all", object_dp_all);
			object_header.put("header_yield", object_yield);
			object_header.put("header_dwh", object_dwh);
			object_header.put("header_dnoe", object_dnoe);
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			// JSONArray a_val = new JSONArray();
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, Fm_Time.to_y_M_d(new Date()) + " 00:00:00",
					"col-md-2", "sys_s_date", "時間(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, Fm_Time.to_y_M_d(new Date()) + " 23:59:00",
					"col-md-2", "sys_e_date", "時間(終)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pd_wc_class", pd_wc_class, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pd_wc_line", pd_wc_line, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pd_pr_id", pd_pr_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_bom_id", pr_bom_id, n_val));
			object_searchs
					.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pd_pr_p_model", pd_pr_p_model, n_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			sc_s_date = body.getJSONObject("search").getString("sys_s_date");
			sc_s_date = sc_s_date.equals("") ? null : sc_s_date;

			sc_e_date = body.getJSONObject("search").getString("sys_e_date");
			sc_e_date = sc_e_date.equals("") ? null : sc_e_date;

			sc_bom_id = body.getJSONObject("search").getString("pr_bom_id");
			sc_bom_id = sc_bom_id.equals("") ? null : sc_bom_id;

			sc_model = body.getJSONObject("search").getString("pd_pr_p_model");
			sc_model = sc_model.equals("") ? null : sc_model;

			sc_class = body.getJSONObject("search").getString("pd_wc_class");
			sc_class = sc_class.equals("") ? null : sc_class;

			sc_line = body.getJSONObject("search").getString("pd_wc_line");
			sc_line = sc_line.equals("") ? null : sc_line;

			sc_id = body.getJSONObject("search").getString("pd_pr_id");
			sc_id = sc_id.equals("") ? null : sc_id;

		}
		Date s_Date = new Date();
		Date e_Date = new Date();
		if (sc_s_date == null && sc_e_date == null) {
			s_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(new Date()) + " 00:00:00");
			e_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(new Date()) + " 23:59:00");
		} else {
			s_Date = sc_s_date == null ? null : Fm_Time.toDateTime(sc_s_date);
			e_Date = sc_e_date == null ? null : Fm_Time.toDateTime(sc_e_date);
		}

		// [準備] 把每個工作站查詢出過站的資料 dailybeans
		// Step1. 工作站[{"wcname":"D0001","wpbname":"加工站","qty":"50"},{},{}]
		workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);
		JSONArray pbwNewArr = new JSONArray();
		for (Workstation w_one : workstations) {
			if (w_one.getWgid() != 0) {
				JSONObject pbwNewObj = new JSONObject();
				pbwNewObj.put("wcname", w_one.getWcname());
				pbwNewObj.put("wpbname", w_one.getWpbname());
				pbwNewObj.put("tsulist", new JSONArray());
				pbwNewObj.put("qtsu", 0);// 當天 使用者數
				pbwNewObj.put("qty", 0);// 當天 台數
				pbwNewObj.put("qttime", 0.0);// 當天 工時數
				pbwNewArr.put(pbwNewObj);

			}
		}

		// Step2. [每日生產數量]把每個工作站查詢出過站的資料() dailybeans
		productionDailys = dailyDao.findAllByProductionDaily(sc_line, sc_class, sc_id, sc_model, sc_bom_id, s_Date,
				e_Date);
		if (productionDailys.size() > 0) {
			// Step3. [準備]每工單的 最後一站的數量 今天的產出數量
			Map<String, String> last_wcname = new HashMap<String, String>();
			Map<String, Integer> wait_fix = new HashMap<String, Integer>();
			for (ProductionDaily object : productionDailys) {
				String prid = object.getPdprid();
				if (!last_wcname.containsKey(prid)) {
					ProductionRecords prs = new ProductionRecords();
					prs.setPrid(prid);
					List<ProductionHeader> headers = headerDao.findAllByProductionRecords(prs);
					Long wpid = headers.get(0).getPhwpid();// 取得 工作程序 ID
					ArrayList<WorkstationProgram> pbwLast = programDao.findAllByWpgidOrderBySyssortAsc(wpid);
					Long wpwgid = pbwLast.get(pbwLast.size() - 1).getWpwgid();
					String pbwcnameLast = workstationDao.findAllByWgidOrderBySyssortAsc(wpwgid).get(0).getWcname();
					last_wcname.put(prid, pbwcnameLast);// 最後工作站
					int fixNb = bodyDao.findPbbsnPbscheduleFixList(headers.get(0).getPhpbgid(), "_N").size();
					wait_fix.put(prid, fixNb);// 待修數量
				}
			}
			// Step4. [準備] 完成幾台/產品測試/產品測試次數
			String key = "";
			JSONArray pbwArr = new JSONArray();
			JSONObject pdphpbschedule = new JSONObject();
			ProductionDailyBean dailyBean = new ProductionDailyBean();//
			// Step4.每一筆日報表資料
			for (ProductionDaily pdOne : productionDailys) {

				// 同一天+同一條產線+同一班別+同一張工單
				key = Fm_Time.to_y_M_d(pdOne.getSysmdate()) + "_" + pdOne.getPdwcline() + "_" + pdOne.getPdwcclass()
						+ "_" + pdOne.getPdprid();
				pbwArr = new JSONArray();
				pdphpbschedule = new JSONObject();
				dailyBean = new ProductionDailyBean();//
				// Step5. 如果[同一天+同一條產線+同一班別+同一張工單]
				if (dailybeans.containsKey(key)) {
					dailyBean = dailybeans.get(key);
					// Step5-1. 工作站[統計]
					pbwArr = dailyBean.getPdwpbname();// [{"wcname":"D0001","wpbname":"加工站","qty":"50"},{},{}]
					for (int index = 0; index < pbwArr.length(); index++) {

						JSONObject pdOne_old = pbwArr.getJSONObject(index);
						// 如果工作站[同一個]:累加數量
						if (pdOne_old.getString("wcname").equals(pdOne.getPdwcname())) {
							// 使用者清單
							JSONArray pdwnames_old = pdOne_old.getJSONArray("tsulist");
							JSONArray pdwnames_new = new JSONObject(pdOne.getPdwnames()).getJSONArray("list");
							for (Object one : pdwnames_new) {
								// 排除重複
								if (!pdwnames_old.toString().contains((String) one)) {
									pdwnames_old.put(one);
								}
							}
							pdOne_old.put("tsulist", pdwnames_old);// 使用人清單
							pdOne_old.put("qty", pdOne_old.getInt("qty") + pdOne.getPdtqty());// [同一個]工作站-台數(累加數量)
							pdOne_old.put("qtsu", pdwnames_old.length());// 人數
							pdOne_old.put("qttime",
									pdOne_old.getDouble("qttime") + Double.parseDouble(pdOne.getPdttime()));// 工時數
							pbwArr.put(index, pdOne_old);

							// 如果 是最後一站(累加[當日完成數])
							if (last_wcname.get(pdOne.getPdprid()).equals(pdOne.getPdwcname())) {
								int tqty = Integer.parseInt(dailyBean.getPdtqty()) + pdOne.getPdtqty();
								dailyBean.setPdtqty(tqty + "");
							}
							break;
						}
					}
					// 同一日 最後的完成數量
					if (pdOne.getSysmdate().getTime() > dailyBean.getSysmdatemsort().getTime()) {
						dailyBean.setPdprokqty(pdOne.getPdprokqty() + "");// [固定]總完成數
						dailyBean.setPdprttokqty(pdOne.getPdprttokqty() + "");
						dailyBean.setPdprbadqty(pdOne.getPdprbadqty() + "");
						dailyBean.setPdpryield(pdOne.getPdpryield());//
						dailyBean.setSysmdatemsort(pdOne.getSysmdate());// [其他]最後更新時間
					}
					// 同一天測試次數最大
					if (pdOne.getPdttqty() > 0 && pdOne.getPdttqty() >= Integer.parseInt(dailyBean.getPdttqty())) {
						dailyBean.setPdttqty(pdOne.getPdttqty() + "");
						dailyBean.setPdttbadqty(pdOne.getPdttbadqty() + "");
						dailyBean.setPdttyield(pdOne.getPdttyield());
					}
					dailybeans.put(key, dailyBean);
				} else {
					// Step6. 如果不同[新建]
					// Step6-1. 建立物件(同一天+同一條產線+同一班別+同一張工單)
					dailyBean = new ProductionDailyBean();
					dailyBean.setId(pdOne.getPdid());
					dailyBean.setSysmdate(Fm_Time.to_y_M_d(pdOne.getSysmdate()));// [固定]時間
					dailyBean.setPdwcline(pdOne.getPdwcline());// [固定]產線
					dailyBean.setPdwcclass(pdOne.getPdwcclass());// [固定]班別
					dailyBean.setPdprid(pdOne.getPdprid());// [固定]工單
					dailyBean.setPdprbomid(pdOne.getPdprbomid());// [固定]BOM
					dailyBean.setPdprpmodel(pdOne.getPdprpmodel());// [固定]型號
					dailyBean.setPdprtotal(pdOne.getPdprtotal() + "");// [固定]工單總數
					dailyBean.setPdprokqty(pdOne.getPdprokqty() + "");// [固定]總完成數
					dailyBean.setPdbadqty(wait_fix.get(pdOne.getPdprid()) + "");// [固定]待修數量
					// 工作站[統計]
					int tqty = 0;
					pbwArr = new JSONArray(pbwNewArr.toString());
					for (int index = 0; index < pbwArr.length(); index++) {
						JSONObject pbOne = pbwArr.getJSONObject(index);
						// 如果工作站[同一個]:累加數量
						if (pbOne.getString("wcname").equals(pdOne.getPdwcname())) {
							// 防止欄位 沒資料
							JSONArray tsulist = new JSONArray();
							if (pdOne.getPdwnames() != null && !pdOne.getPdwnames().equals("")) {
								tsulist = new JSONObject(pdOne.getPdwnames()).getJSONArray("list");
							}
							pbOne.put("qty", pdOne.getPdtqty());// [該站]通過數量
							pbOne.put("qtsu", pdOne.getPdtsu());// [該站]人數
							pbOne.put("qttime", pdOne.getPdttime());// [該站]工時數

							pbOne.put("tsulist", tsulist);// [該站]人清單
							pbOne.put("wcname", pdOne.getPdwcname());// 工作站代號
							pbOne.put("wpbname", pdOne.getPdwpbname());// 工作站名稱
							pbwArr.put(index, pbOne);
							// 如果是最後一站
							if (last_wcname.get(pdOne.getPdprid()).equals(pdOne.getPdwcname())) {
								tqty = pdOne.getPdtqty();
							}
							System.out.println("" + pbOne);
							break;
						}
					}
					dailyBean.setPdwpbname(pbwArr);// [浮動]每一工作站
					dailyBean.setPdtqty(tqty + "");// [固定]日完成數

					// Step6-2.[其他] 資料
					if (pdOne.getPdphpbschedule() != null && !pdOne.getPdphpbschedule().equals("")) {
						pdphpbschedule = new JSONObject(pdOne.getPdphpbschedule());
					}
					dailyBean.setPdphpbschedule(pdphpbschedule);// 工作站 每一站過站數量
					dailyBean.setSysmdatemsort(pdOne.getSysmdate());// 每一筆 時間

					// Step6-3.[每日 測試不良] 同一日 最後的完成數量
					dailyBean.setPdttqty(pdOne.getPdttqty() + "");
					dailyBean.setPdttbadqty(pdOne.getPdttbadqty() + "");
					dailyBean.setPdttyield(pdOne.getPdttyield());
					// Step6-4.[每日 產品不良] 同一日 最後的完成數量
					dailyBean.setPdprttokqty(pdOne.getPdprttokqty() + "");
					dailyBean.setPdprbadqty(pdOne.getPdprbadqty() + "");
					dailyBean.setPdpryield(pdOne.getPdpryield());

					dailybeans.put(key, dailyBean);
				}
			}
		}

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_dp_bodys = new JSONArray();
		JSONArray object_dp_all_bodys = new JSONArray();
		JSONArray object_yield_bodys = new JSONArray();
		JSONArray object_dwh_bodys = new JSONArray();
		JSONArray object_dnoe_bodys = new JSONArray();
		boolean checkFirstif = checkFirst;
		dailybeans.forEach((key, pdb_val) -> {
			JSONObject object_dp_one = new JSONObject();
			JSONObject object_dp_all_one = new JSONObject();
			JSONObject object_yield_one = new JSONObject();
			JSONObject object_dwh_one = new JSONObject();
			JSONObject object_dnoe_one = new JSONObject();
			int ord_dp = 0, ord_dp_all = 0, ord_yield = 0, ord_dwh = 0, ord_dnoe = 0;
			// object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_id", pdb_val.getId());
			// 每日數量
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_total", pdb_val.getPdprtotal());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_ok_qty", pdb_val.getPdprokqty());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_bad_qty", pdb_val.getPdbadqty());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());

			// 第一次則不需要這些資料
			if (!checkFirstif) {
				// 每日 累計數量
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_pr_total", pdb_val.getPdprtotal());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_pr_ok_qty", pdb_val.getPdprokqty());
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());

				// 每日 良率
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_total", pdb_val.getPdprtotal());

				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_tt_qty", pdb_val.getPdttqty());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_tt_bad_qty", pdb_val.getPdttbadqty());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_tt_yield", pdb_val.getPdttyield());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_tt_ok_qty",
						pdb_val.getPdprttokqty());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_bad_qty", pdb_val.getPdprbadqty());
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "pd_pr_yield", pdb_val.getPdpryield());

				// 每日總工時
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_pr_total", pdb_val.getPdprtotal());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_pr_ok_qty", pdb_val.getPdprokqty());
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());

				// 每日總人數
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_pr_total", pdb_val.getPdprtotal());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_pr_ok_qty", pdb_val.getPdprokqty());
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());
			}
			JSONArray pbwArr = pdb_val.getPdwpbname();
			JSONObject pbphpbsArrAll = pdb_val.getPdphpbschedule();
			String tsulist = "";
			for (int index = 0; index < pbwArr.length(); index++) {
				JSONObject pbOne = pbwArr.getJSONObject(index);
				object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + pbOne.getString("wcname"), pbOne.getInt("qty"));
				// 第一次則不需要這些資料
				if (!checkFirstif) {
					object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + pbOne.getString("wcname"),
							pbOne.getDouble("qttime"));
					object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + pbOne.getString("wcname"),
							pbOne.getInt("qtsu"));
					if (pbOne.getJSONArray("tsulist").length() > 0) {
						tsulist += pbOne.getString("wpbname") + ":" + pbOne.getJSONArray("tsulist") + "\n";
					}
					// 工單-累計資訊
					if (pbphpbsArrAll.has(pbOne.getString("wcname"))) {
						object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + pbOne.getString("wcname"),
								pbphpbsArrAll.getInt(pbOne.getString("wcname")));
					} else {
						object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + pbOne.getString("wcname"), "無");
					}
				}
			}

			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "sys_note", "");
			// 第一次則不需要這些資料
			if (!checkFirstif) {
				object_dp_all_one.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.B) + "sys_note", "");
				object_yield_one.put(FFS.ord((ord_yield += 1), FFM.Hmb.B) + "sys_note", "");
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "sys_note", "");
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_w_names", tsulist);
			}

			object_dp_bodys.put(object_dp_one);
			// 第一次則不需要這些資料
			if (!checkFirstif) {
				object_dp_all_bodys.put(object_dp_all_one);
				object_yield_bodys.put(object_yield_one);
				object_dwh_bodys.put(object_dwh_one);
				object_dnoe_bodys.put(object_dnoe_one);
			}

		});
		// 共有4張表 同時回傳
		JSONObject all_json = new JSONObject();
		all_json.put("bodys_dp", object_dp_bodys);
		all_json.put("bodys_dp_all", object_dp_all_bodys);
		all_json.put("bodys_yield", object_yield_bodys);
		all_json.put("bodys_dwh", object_dwh_bodys);
		all_json.put("bodys_dnoe", object_dnoe_bodys);

		bean.setBody(new JSONObject().put("search", all_json));

		updateData();
		return true;

	}

	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		boolean check = false;
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		boolean check = false;
		return check;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData() {
		boolean check = false;
		log.info("updateData 更新每日生產報告");
		// 檢查[結算 每日 資料]
		ArrayList<ProductionDaily> oldDailys = new ArrayList<ProductionDaily>();
		oldDailys = dailyDao.findAllByProductionDailyCheck(null, null, null, null, null, null, null, 0);
		for (ProductionDaily productionDaily : oldDailys) {
			String wcc = productionDaily.getPdwcclass();
			String wcl = productionDaily.getPdwcline();
			String wcn = productionDaily.getPdwcname();

			// 取出 [所配班別]
			ArrayList<WorkstationClass> classes = classDao.findAllBySameClass(wcc, wcl, wcn, null, null);
			if (classes.size() > 0) {
				WorkstationClass oneClass = classes.get(0);
				// 如果[目前時間] 大於 [結算時間]
				Date n_Date = new Date();
				Date s_Date = Fm_Time.toDateTime(
						Fm_Time.to_y_M_d(productionDaily.getSyscdate()) + " " + oneClass.getWcstime() + ":00");
				Date e_Date = Fm_Time.toDateTime(
						Fm_Time.to_y_M_d(productionDaily.getSyscdate()) + " " + oneClass.getWcetime() + ":00");

				if (n_Date.after(e_Date)) {
					// (時*分*秒*毫秒)(24 * 60 * 60 * 1000)
					double total_time = 0.0;

					// 時間到[true]: 最後修改時間|| [false]:結算時間
					if (oneClass.getWceauto()) {
						s_Date = Fm_Time.toDateTime(Fm_Time.to_yMd_Hm(productionDaily.getSyscdate()) + ":00");
						e_Date = Fm_Time.toDateTime(Fm_Time.to_yMd_Hm(productionDaily.getSysmdate()) + ":00");
						productionDaily.setPdetime(e_Date);
						total_time = (double) (e_Date.getTime() - s_Date.getTime()) / (60 * 60 * 1000);
						total_time = Math.round(total_time * 100.0) / 100.0;
					} else {
						productionDaily.setPdetime(e_Date);
						total_time = (double) (e_Date.getTime() - s_Date.getTime()) / (60 * 60 * 1000);
						total_time = Math.round(total_time * 100.0) / 100.0;
					}
					JSONObject wnames = new JSONObject(productionDaily.getPdwnames());
					// 格式化
					DecimalFormat df = new DecimalFormat("###.##");
					productionDaily.setSysstatus(1);// 結算- 今日班別
					productionDaily.setPdtsu(wnames.getJSONArray("list").length());// 結算-人數
					productionDaily.setPdttime(df.format(total_time));// 結算-工時
					productionDaily.setSysmuser("system");
					dailyDao.save(productionDaily);
				}
			}
		}
		return check;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(JSONObject body) {
		boolean check = false;
		return check;
	}

	// 登記入每日報表
	@Transactional
	public PackageBean setData(ProductionDaily newDaily, SystemUser user, Boolean pd_test) {

		PackageBean bean = new PackageBean();
		try {
			// Step0.檢查必要值
			if (newDaily.getPdpbbsn() != null && !newDaily.getPdpbbsn().equals("") && // 產品SN號
					newDaily.getPdprid() != null && !newDaily.getPdprid().equals("") && // 製令單號
					newDaily.getPdprpmodel() != null && !newDaily.getPdprpmodel().equals("") && // 產品型號
					newDaily.getPdprbomid() != null && !newDaily.getPdprbomid().equals("") && // 產品BOM
					newDaily.getPdprtotal() != null && newDaily.getPdprtotal() != 0 && // 製令單 生產總數
					newDaily.getPdprokqty() != null && // 製令單 生產目前總數
					newDaily.getPdwcline() != null && !newDaily.getPdwcline().equals("") && // 生產產線
					newDaily.getPdwcname() != null && !newDaily.getPdwcname().equals("") && // 工作站代號
					newDaily.getPdwpbname() != null && !newDaily.getPdwpbname().equals("") && // 工作站名稱
					newDaily.getPdwaccounts() != null && !newDaily.getPdwaccounts().equals("") // 工作站人員
			) {
				log.info("Step1.登記入每日報表[ProductionDaily]" + newDaily.toString());
				String w_cclass = null;// 班別?
				Boolean w_cgroup = null;// 單人/多人模式?
				String n_time = (Fm_Time.to_yMd_Hm(new Date()).split(" "))[1];// 年月日
				Boolean need_create = false;// 是否新增?
				List<String> pd_accounts = new ArrayList<String>();// 需要登記的人
				ArrayList<WorkstationClass> classes = new ArrayList<WorkstationClass>();
				ArrayList<ProductionDaily> oldDailySn = new ArrayList<ProductionDaily>();
				ArrayList<ProductionDaily> oldDailys = new ArrayList<ProductionDaily>();
				ProductionDaily oldDaily = new ProductionDaily();
				ProductionDaily saveDaily = new ProductionDaily();
				DecimalFormat df_yield = new DecimalFormat("###.##");

				// Step0.[取出] 工作站的作業員 & 班別 & 以前有登記過的SN產品
				pd_accounts = Arrays.asList(newDaily.getPdwaccounts().split("_"));
				classes = classDao.findAllBySameClass(null, newDaily.getPdwcline(), //
						(newDaily.getPdwcname() + "(" + newDaily.getPdwpbname() + ")"), n_time, null);
				oldDailySn = dailyDao.findAllByPdpridAndPdpbbsnLikeAndPdwcname(newDaily.getPdprid(),
						"%" + newDaily.getPdpbbsn() + "%", newDaily.getPdwcname());

				// Step1.[檢查] 設置內是否有此工作站資料 && (工單+SN是配對的)
				if (classes != null && classes.size() > 0) {

					// Step2.[取出] 設定工作模式 [群組/個人]
					WorkstationClass w_class = classes.get(0);
					w_cclass = w_class.getWcclass();
					w_cgroup = w_class.getWcgroup();// true = 群組/ false = 單人

					// Step3.[取出] 此工單 是否有 維修資料
					int pr_bad_qty = registerDao.findAllByRrprid(newDaily.getPdprid()).size();

					// Step4.[取出] 今日登記過的 工單+產品+產線+工作站 未結單 工單資訊
					oldDailys = dailyDao.findAllByProductionDailyCheck(//
							newDaily.getPdprid(), newDaily.getPdprbomid(), newDaily.getPdwcline(), //
							w_cclass, newDaily.getPdwcname(), null, Fm_Time.to_y_M_d(new Date()), 0);

					// Step5 如果 有舊的[今日資料]?
					if (oldDailys != null && oldDailys.size() > 0) {
						need_create = true;
						oldDaily = oldDailys.get(0);
						// 如果 [群組模式] + ([不同批人]=新建)
						if (w_cgroup && !oldDaily.getPdwaccounts().equals(newDaily.getPdwaccounts())) {
							need_create = false;
						}
					}

					// Step6-1.[登記] [唯一SN] 每日工作站 登記數量
					if (oldDailySn.size() == 0) {
						// 新建 or 更新
						if (need_create) {
							// [修改]新工單 每日生產紀錄
							log.info("登記入每日報表[ProductionDaily]模式?:" + w_cgroup + " 登記入的 Pdpbbsn :"
									+ newDaily.getPdpbbsn());
							// 產品資訊登記
							JSONObject pd_pbbsn = new JSONObject(oldDaily.getPdpbbsn());
							JSONArray pd_pbbsns = (pd_pbbsn.getJSONArray("list")).put(newDaily.getPdpbbsn());
							pd_pbbsn.put("list", pd_pbbsns);
							// Step7.[模式]: True群組? False單人?
							if (w_cgroup) {
								// Step7-1.[多人群組] 同一批人
								oldDaily.setPdpbbsn(pd_pbbsn + "");
								oldDaily.setPdtqty(pd_pbbsns.length());
								oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmuser(user.getSuaccount());
								oldDaily.setPdprokqty(newDaily.getPdprokqty());// 指定-測試完成數
								oldDaily.setPdphpbschedule(newDaily.getPdphpbschedule());
							} else {
								// Step7-2.[單人]-只取第一人 [代號]
								// [取得] 新資料-使用者
								String pd_acc_one = pd_accounts.get(0);
								ArrayList<String> accounts = new ArrayList<String>();
								accounts.add(pd_acc_one);
								ArrayList<String> su_name = userDao.readAccounts(accounts);
								// [取得] 舊資料-使用者
								JSONObject wname_list = new JSONObject(oldDaily.getPdwnames());
								JSONArray wnames = wname_list.getJSONArray("list");
								// Step7-3.[判斷]有沒有新使用者
								if (!oldDaily.getPdwaccounts().contains(pd_acc_one)) {
									// 有可能沒有中文
									wnames.put(su_name.size() == 1 ? su_name.get(0) : pd_acc_one);
									oldDaily.setPdwnames(new JSONObject().put("list", wnames).toString());
									oldDaily.setPdwaccounts(oldDaily.getPdwaccounts() + "_" + pd_acc_one);
								}
								oldDaily.setPdpbbsn(pd_pbbsn + "");
								oldDaily.setPdtqty(pd_pbbsns.length());
								oldDaily.setPdtsu(wnames.length());// 人數
								oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmuser(user.getSuaccount());
								oldDaily.setPdprokqty(newDaily.getPdprokqty());
								oldDaily.setPdphpbschedule(newDaily.getPdphpbschedule());
							}
							saveDaily = oldDaily;
							log.info("登記入每日報表[ProductionDaily] 模式?:" + w_cgroup + " 更新?:" + oldDaily.toString());
						} else {
							// Step8. [新增]新工單 每日生產紀錄->新建立 && 添加新 產品SN
							ArrayList<String> su_name = userDao.readAccounts(pd_accounts);
							JSONObject wnames = new JSONObject();
							wnames.put("list", new JSONArray(su_name));
							newDaily.setPdwnames(wnames.toString());
							newDaily.setPdtsu(pd_accounts.size());// 人數
							newDaily.setPdwcclass(w_cclass);// 班別
							newDaily.setPdttime("0.0");// 工時
							newDaily.setPdtqty(1);// 日完成數 初始數量
							newDaily.setPdttqty(0);
							newDaily.setPdttyield("0%");
							newDaily.setPdpryield("0%");
							newDaily.setPdwaccounts(newDaily.getPdwaccounts());// 登記的作業員帳號(s)
							newDaily.setSyscdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							newDaily.setSyscuser(user.getSuaccount());
							newDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							newDaily.setSysmuser(user.getSuaccount());
							newDaily.setSysstatus(0);
							newDaily.setPdstime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							newDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							newDaily.setPdlsuid(w_class.getWclsuid());
							newDaily.setPdlname(userDao.findAllBySuid(w_class.getWclsuid()).get(0).getSuname());
							JSONArray o_pdprpbsns = new JSONArray().put(newDaily.getPdpbbsn());// 產品登記
							newDaily.setPdpbbsn(new JSONObject().put("list", o_pdprpbsns) + "");
							log.info("登記入每日報表[ProductionDaily] 新建?:" + newDaily.toString());
							saveDaily = newDaily;
						}

						// Step8.[登記] 不良率(產品數)
						double yield = 0;
						if (newDaily.getPdprttokqty() > 0 && pr_bad_qty > 0) {
							yield = ((double) pr_bad_qty * 100) / newDaily.getPdprttokqty();
							yield = yield > 100 ? yield = 100 : yield;// 不可超過100

						} else if (newDaily.getPdprttokqty() == 0 && pr_bad_qty > 0) {
							yield = 100;// 還沒生產就不良
							saveDaily.setPdprttokqty(pr_bad_qty);
						}
						saveDaily.setPdprttokqty(newDaily.getPdprttokqty());
						saveDaily.setPdpryield(df_yield.format(yield) + "%");
						saveDaily.setPdprbadqty(pr_bad_qty);

						// Step9.[登記] 不良率(測試數)
						yield = 0;
						if (pd_test && need_create) {
							// [有舊資料]
							saveDaily.setPdttqty(oldDaily.getPdttqty() + 1);
							saveDaily.setPdttbadqty(oldDaily.getPdttbadqty() + newDaily.getPdttbadqty());
							if (saveDaily.getPdttbadqty() > 0 && saveDaily.getPdttqty() > 0) {
								yield = ((double) saveDaily.getPdttbadqty() * 100) / saveDaily.getPdttqty();
								yield = yield > 100 ? yield = 100 : yield;// 不可超過100
							}
							saveDaily.setPdttyield(df_yield.format(yield) + "%");
						} else if (pd_test && !need_create) {
							// [新資資料]
							saveDaily.setPdttqty(1);
							saveDaily.setPdttbadqty(newDaily.getPdttbadqty());
							if (saveDaily.getPdttbadqty() > 0 && saveDaily.getPdttqty() > 0) {
								yield = ((double) saveDaily.getPdttbadqty() * 100) / saveDaily.getPdttqty();
								yield = yield > 100 ? yield = 100 : yield;// 不可超過100
							}
							saveDaily.setPdttyield(df_yield.format(yield) + "%");
						}
						dailyDao.save(saveDaily);

						// Step6-2. [登記] [唯一SN]+[測試數] 不良率(測試數)
					} else if (pd_test && oldDailys.size() == 0) {
						ArrayList<String> su_name = userDao.readAccounts(pd_accounts);
						JSONObject wnames = new JSONObject();
						wnames.put("list", new JSONArray(su_name));
						newDaily.setPdwnames(wnames.toString());
						newDaily.setPdtsu(pd_accounts.size());// 人數
						newDaily.setPdwcclass(w_cclass);// 班別
						newDaily.setPdttime("0.0");// 工時
						newDaily.setPdtqty(0);// 日完成數 初始數量
						newDaily.setPdwaccounts(newDaily.getPdwaccounts());// 登記的作業員帳號(s)
						newDaily.setSyscdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						newDaily.setSyscuser(user.getSuaccount());
						newDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						newDaily.setSysmuser(user.getSuaccount());
						newDaily.setSysstatus(0);
						newDaily.setPdstime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						newDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						newDaily.setPdlsuid(w_class.getWclsuid());
						newDaily.setPdlname(userDao.findAllBySuid(w_class.getWclsuid()).get(0).getSuname());
						newDaily.setPdpbbsn(new JSONObject().put("list", new JSONArray()) + "");// 產品登記
						log.info("登記入每日報表[ProductionDaily] 新建?:" + newDaily.toString());
						saveDaily = newDaily;
						double yield = 0;
						saveDaily.setPdttqty(1);
						saveDaily.setPdttbadqty(newDaily.getPdttbadqty());
						if (saveDaily.getPdttbadqty() > 0 && saveDaily.getPdttqty() > 0) {
							yield = ((double) saveDaily.getPdttbadqty() * 100) / saveDaily.getPdttqty();
							yield = yield > 100 ? yield = 100 : yield;// 不可超過100
						}
						saveDaily.setPdttyield(df_yield.format(yield) + "%");
						// Step8.[登記] 不良率(產品數)
						yield = 0;
						if (newDaily.getPdprttokqty() > 0 && pr_bad_qty > 0) {
							yield = ((double) pr_bad_qty * 100) / newDaily.getPdprttokqty();
							yield = yield > 100 ? yield = 100 : yield;// 不可超過100
						} else if (newDaily.getPdprttokqty() == 0 && pr_bad_qty > 0) {
							yield = 100;// 還沒生產就不良
							saveDaily.setPdprttokqty(pr_bad_qty);
						}
						saveDaily.setPdprttokqty(newDaily.getPdprttokqty());
						saveDaily.setPdpryield(df_yield.format(yield) + "%");
						saveDaily.setPdprbadqty(pr_bad_qty);
						dailyDao.save(saveDaily);

						// Step6-3. [登記] [重複SN]+[測試數] 不良率(測試數) && [有舊資料]
					} else if (pd_test && oldDailys.size() > 0) {
						double yield = 0;
						saveDaily = oldDaily;
						saveDaily.setPdttqty(oldDaily.getPdttqty() + 1);
						saveDaily.setPdttbadqty(oldDaily.getPdttbadqty() + newDaily.getPdttbadqty());
						if (saveDaily.getPdttbadqty() > 0 && saveDaily.getPdttqty() > 0) {
							yield = ((double) saveDaily.getPdttbadqty() * 100) / saveDaily.getPdttqty();
							yield = yield > 100 ? yield = 100 : yield;// 不可超過100
						}
						saveDaily.setPdttyield(df_yield.format(yield) + "%");
						dailyDao.save(saveDaily);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		return bean;
	}
}
