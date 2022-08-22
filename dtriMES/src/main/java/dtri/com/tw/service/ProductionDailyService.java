package dtri.com.tw.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

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
				pd_pr_p_model = "產品型號", pd_pr_total = "工單總數", pd_pr_bad_qty = "待修數", //
				pd_pr_ok_qty = "累計完成", pd_t_qty = "日完成數", pd_w_names = "各站作業員工(清單)", sys_note = "備註";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			JSONObject object_dp = new JSONObject();
			JSONObject object_dp_all = new JSONObject();
			JSONObject object_dwh = new JSONObject();
			JSONObject object_dnoe = new JSONObject();

			// 總數量_header_dp
			int ord_dp = 0;
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_p_model", FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_total", FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_ok_qty", FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_pr_bad_qty", FFS.h_t(pd_pr_bad_qty, "110px", FFM.Wri.W_Y));

			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));
			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + w_one.getWcname(), FFS.h_t(w_one.getWpbname(), "80px", FFM.Wri.W_Y));
			}
			object_dp.put(FFS.ord((ord_dp += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總數量累計_header_dp
			int ord_dp_all = 0;
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_p_model", FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_total", FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_pr_ok_qty", FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));
			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + w_one.getWcname(), FFS.h_t(w_one.getWpbname(), "80px", FFM.Wri.W_Y));
			}
			object_dp_all.put(FFS.ord((ord_dp_all += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總工時
			int ord_dwh = 0;
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_p_model", FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_total", FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_pr_ok_qty", FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));

			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + w_one.getWcname(), FFS.h_t(w_one.getWpbname(), "80px", FFM.Wri.W_Y));
			}
			object_dwh.put(FFS.ord((ord_dwh += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "80px", FFM.Wri.W_Y));

			// 總人數
			int ord_dnoe = 0;
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "120px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_p_model", FFS.h_t(pd_pr_p_model, "120px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_total", FFS.h_t(pd_pr_total, "110px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_pr_ok_qty", FFS.h_t(pd_pr_ok_qty, "110px", FFM.Wri.W_Y));
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "110px", FFM.Wri.W_Y));

			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + w_one.getWcname(), FFS.h_t(w_one.getWpbname(), "80px", FFM.Wri.W_Y));
			}
			object_dnoe.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.H) + "pd_w_names", FFS.h_t(pd_w_names, "550px", FFM.Wri.W_Y));

			object_header.put("header_dp", object_dp);
			object_header.put("header_dp_all", object_dp_all);
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
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, Fm_Time.to_y_M_d(new Date()) + " 00:00:00", "col-md-2", "sys_s_date", "時間(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, Fm_Time.to_y_M_d(new Date()) + " 23:59:00", "col-md-2", "sys_e_date", "時間(終)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pd_wc_class", pd_wc_class, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "pd_wc_line", pd_wc_line, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pd_pr_id", pd_pr_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_bom_id", pr_bom_id, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pd_pr_p_model", pd_pr_p_model, n_val));
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

		// 把每個工作站查詢出過站的資料 dailybeans(準備好)
		// 工作站[{"wcname":"D0001","wpbmane":"加工站","qty":"50"},{},{}]
		workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);
		JSONArray pbwNewArr = new JSONArray();
		for (Workstation w_one : workstations) {
			if (w_one.getWgid() != 0) {
				JSONObject pbwNewObj = new JSONObject();
				pbwNewObj.put("wcname", w_one.getWcname());
				pbwNewObj.put("wpbmane", w_one.getWpbname());
				pbwNewObj.put("tsulist", new JSONArray());
				pbwNewObj.put("qtsu", 0);
				pbwNewObj.put("qty", 0);
				pbwNewObj.put("qttime", 0.0);
				pbwNewArr.put(pbwNewObj);

			}
		}

		// [每日生產數量]把每個工作站查詢出過站的資料() dailybeans
		productionDailys = dailyDao.findAllByProductionDaily(sc_line, sc_class, sc_id, sc_model, sc_bom_id, s_Date, e_Date);
		if (productionDailys.size() > 0) {

			// 此工單的 最後一站的數量 今天的產出數量
			String prid = productionDailys.get(0).getPdprid();
			String pbwcnameLast = "";
			ProductionRecords prs = new ProductionRecords();
			prs.setPrid(prid);
			List<ProductionHeader> headers = headerDao.findAllByProductionRecords(prs);
			Long wpid = headers.get(0).getPhwpid();// 取得 工作程序 ID
			ArrayList<WorkstationProgram> pbwLast = programDao.findAllByWpgidOrderBySyssortAsc(wpid);
			Long wpwgid = pbwLast.get(pbwLast.size() - 1).getWpwgid();
			pbwcnameLast = workstationDao.findAllByWgidOrderBySyssortAsc(wpwgid).get(0).getWcname();

			// 把同一個工作站 的 倒出來
			int pdprokqty = 0;// 取最大值
			for (ProductionDaily pdOne : productionDailys) {
				// 同一天+同一條產線+同一班別+同一張工單
				String key = Fm_Time.to_y_M_d(pdOne.getSysmdate()) + pdOne.getPdwcline() + pdOne.getPdwcclass() + pdOne.getPdprid();
				ProductionDailyBean dailyBean = new ProductionDailyBean();//
				JSONArray pbwArr = new JSONArray();
				// 如果[同一天+同一條產線+同一班別+同一張工單]
				if (dailybeans.containsKey(key)) {
					dailyBean = dailybeans.get(key);

					// 工作站[統計]
					pbwArr = dailyBean.getPdwpbname();
					for (int index = 0; index < pbwArr.length(); index++) {
						JSONObject pbOne = pbwArr.getJSONObject(index);
						// 如果工作站[同一個]:累加數量
						if (pbOne.getString("wcname").equals(pdOne.getPdwcname())) {
							// 使用者清單
							JSONArray pdwnames_old = pbOne.getJSONArray("tsulist");
							JSONArray pdwnames_new = new JSONObject(pdOne.getPdwnames()).getJSONArray("list");
							for (Object one : pdwnames_new) {
								// 排除重複
								if (!pdwnames_old.toString().contains((String) one)) {
									pdwnames_old.put(one);
								}
							}
							pbOne.put("tsulist", pdwnames_old);
							pbOne.put("qty", pbOne.getInt("qty") + pdOne.getPdtqty());// 台數
							pbOne.put("qtsu", pdwnames_old.length());// 人數
							pbOne.put("qttime", pbOne.getDouble("qttime") + Double.parseDouble(pdOne.getPdttime()));// 工時
							pbwArr.put(index, pbOne);

							// 如果是最後一站(再累加)
							if (pbwcnameLast.equals(pdOne.getPdwcname())) {
								int tqty = Integer.parseInt(dailyBean.getPdtqty()) + pdOne.getPdtqty();
								dailyBean.setPdtqty(tqty + "");
							}
							// 同一日的最大完成數量
							if (pdOne.getPdprokqty() > pdprokqty) {
								pdprokqty = pdOne.getPdprokqty();
							}
							dailyBean.setPdprokqty(pdprokqty + "");

							break;
						}
					}
				} else {
					// 如果不同[新建]
					pdprokqty = 0;
					dailyBean.setId(pdOne.getPdid());
					dailyBean.setSysmdate(Fm_Time.to_y_M_d(pdOne.getSysmdate()));
					dailyBean.setPdprbomid(pdOne.getPdprbomid());
					dailyBean.setPdprid(pdOne.getPdprid());
					dailyBean.setPdprpmodel(pdOne.getPdprpmodel());
					dailyBean.setPdwcline(pdOne.getPdwcline());
					dailyBean.setPdwcclass(pdOne.getPdwcclass());
					dailyBean.setPdwpbname(new JSONArray(pbwNewArr.toString()));
					dailyBean.setPdprtotal(pdOne.getPdprtotal() + "");
					// 待修數量
					ProductionRecords rds = new ProductionRecords();
					rds.setPrid(pdOne.getPdprid());
					List<ProductionHeader> hds = headerDao.findAllByProductionRecords(rds);

					int fixNb = bodyDao.findPbbsnPbscheduleFixList(hds.get(0).getPhpbgid(), "_N").size();
					dailyBean.setPdprbadqty(fixNb + "");
					// 如果是最後一站
					int tqty = 0;
					if (pbwcnameLast.equals(pdOne.getPdwcname())) {
						tqty = pdOne.getPdtqty();
					}
					// 同一日的最大完成數量
					if (pdOne.getPdprokqty() > pdprokqty) {
						pdprokqty = pdOne.getPdprokqty();
					}

					dailyBean.setPdprokqty(pdprokqty + "");
					dailyBean.setPdtqty(tqty + "");

					// 工作站[統計]
					pbwArr = dailyBean.getPdwpbname();
					for (int index = 0; index < pbwArr.length(); index++) {
						JSONObject pbOne = pbwArr.getJSONObject(index);
						// 如果工作站[同一個]:累加數量
						if (pbOne.getString("wcname").equals(pdOne.getPdwcname())) {
							// 防止欄位 沒資料
							JSONArray tsulist = new JSONArray();
							if (pdOne.getPdwnames() == null || pdOne.getPdwnames().equals("")) {
								tsulist = new JSONArray();
							} else {
								tsulist = new JSONObject(pdOne.getPdwnames()).getJSONArray("list");
							}
							pbOne.put("qty", pdOne.getPdtqty());
							pbOne.put("qtsu", pdOne.getPdtsu());
							pbOne.put("qttime", pdOne.getPdttime());

							pbOne.put("tsulist", tsulist);
							pbOne.put("wcname", pdOne.getPdwcname());
							pbOne.put("wpbmane", pdOne.getPdwpbname());

							System.out.println("" + pbOne);
							pbwArr.put(index, pbOne);
							break;
						}
					}
					dailyBean.setPdwpbname(pbwArr);
					dailybeans.put(key, dailyBean);
				}
			}
		}

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_dp_bodys = new JSONArray();
		JSONArray object_dwh_bodys = new JSONArray();
		JSONArray object_dnoe_bodys = new JSONArray();
		dailybeans.forEach((key, pdb_val) -> {
			JSONObject object_dp_one = new JSONObject();
			JSONObject object_dwh_one = new JSONObject();
			JSONObject object_dnoe_one = new JSONObject();
			int ord_dp = 0, ord_dwh = 0, ord_dnoe = 0;
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
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_pr_bad_qty", pdb_val.getPdprbadqty());
			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());

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

			JSONArray pbwArr = pdb_val.getPdwpbname();
			String tsulist = "";
			for (int index = 0; index < pbwArr.length(); index++) {
				JSONObject pbOne = pbwArr.getJSONObject(index);
				object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + pbOne.getString("wcname"), pbOne.getInt("qty"));
				object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + pbOne.getString("wcname"), pbOne.getDouble("qttime"));
				object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + pbOne.getString("wcname"), pbOne.getInt("qtsu"));
				if (pbOne.getJSONArray("tsulist").length() > 0) {
					tsulist += pbOne.getString("wpbmane") + ":" + pbOne.getJSONArray("tsulist") + "\n";
				}
			}

			object_dp_one.put(FFS.ord((ord_dp += 1), FFM.Hmb.B) + "sys_note", "");
			object_dwh_one.put(FFS.ord((ord_dwh += 1), FFM.Hmb.B) + "sys_note", "");
			object_dnoe_one.put(FFS.ord((ord_dnoe += 1), FFM.Hmb.B) + "pd_w_names", tsulist);

			object_dp_bodys.put(object_dp_one);
			object_dwh_bodys.put(object_dwh_one);
			object_dnoe_bodys.put(object_dnoe_one);

		});
		// 共有4張表 同時回傳
		JSONObject all_json = new JSONObject();
		all_json.put("bodys_dp", object_dp_bodys);
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
				Date s_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(productionDaily.getSyscdate()) + " " + oneClass.getWcstime() + ":00");
				Date e_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(productionDaily.getSyscdate()) + " " + oneClass.getWcetime() + ":00");

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
	public PackageBean setData(ProductionDaily newDaily, SystemUser user) {
		// Boolean check = false;
		PackageBean bean = new PackageBean();

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
			String n_wcclass = null;
			String n_time = (Fm_Time.to_yMd_Hm(new Date()).split(" "))[1];
			Boolean n_wcg = null;
			List<String> n_pdwaccounts = new ArrayList<String>();
			ArrayList<WorkstationClass> classes = new ArrayList<WorkstationClass>();
			ArrayList<ProductionDaily> oldDailySn = new ArrayList<ProductionDaily>();
			n_pdwaccounts = Arrays.asList(newDaily.getPdwaccounts().split("_"));
			classes = classDao.findAllBySameClass(//
					null, newDaily.getPdwcline(), //
					(newDaily.getPdwcname() + "(" + newDaily.getPdwpbname() + ")"), n_time, null);
			oldDailySn = dailyDao.findAllByPdpridAndPdpbbsnLikeAndPdwcname(newDaily.getPdprid(), "%" + newDaily.getPdpbbsn() + "%", newDaily.getPdwcname());

			ArrayList<ProductionDaily> oldDailys = new ArrayList<ProductionDaily>();
			ProductionDaily oldDaily = new ProductionDaily();

			// Step1. 檢查 設置內是否有此工作站資料 && 不重複SN && (工單+SN是配對的)
			if (classes != null && classes.size() > 0 && oldDailySn.size() == 0) {

				// Step2. 取出 設定工作模式 [群組/個人]
				WorkstationClass wClass = classes.get(0);
				n_wcclass = wClass.getWcclass();
				n_wcg = wClass.getWcgroup();// true = 群組/ false = 單人

				// Step3. 今日登記過的 工單+產品+產線+工作站 未結單 工單資訊
				oldDailys = dailyDao.findAllByProductionDailyCheck(//
						newDaily.getPdprid(), newDaily.getPdprbomid(), newDaily.getPdwcline(), n_wcclass, newDaily.getPdwcname(), null, //
						Fm_Time.to_y_M_d(new Date()), 0);

				Boolean need_create = false;
				// Step4 如果 有舊的[今日資料]?
				if (oldDailys != null && oldDailys.size() > 0) {
					need_create = true;
					oldDaily = oldDailys.get(0);
					// Step4-1. 如果 [群組模式] + [不同批人]?
					if (n_wcg && !oldDaily.getPdwaccounts().equals(newDaily.getPdwaccounts())) {
						need_create = false;
					}
				}

				// Step5.新建 or 更新
				if (need_create) {
					// Step5-1. True群組? False單人?
					if (n_wcg) {
						// 同張工單 + 同一批人 + 時段一致->[更新] && 添加新 產品SN
						JSONObject o_pdpbbsn = new JSONObject(oldDaily.getPdpbbsn());
						JSONArray o_pdprpbsns = (o_pdpbbsn.getJSONArray("list")).put(newDaily.getPdpbbsn());
						log.info("登記入每日報表[ProductionDaily]模式?:" + n_wcg + " 登記入的 Pdpbbsn :" + newDaily.getPdpbbsn());
						o_pdpbbsn.put("list", o_pdprpbsns);
						oldDaily.setPdpbbsn(o_pdpbbsn + "");
						oldDaily.setPdtqty(o_pdprpbsns.length());
						oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						oldDaily.setSysmuser(user.getSuaccount());
						oldDaily.setPdprokqty(newDaily.getPdprokqty());
						log.info("登記入每日報表[ProductionDaily] 模式?:" + n_wcg + " 更新?:" + oldDaily.toString());
						dailyDao.save(oldDaily);
					} else {
						// Step5-2 單人
						// 只取第一人[代號]
						String n_one = n_pdwaccounts.get(0);
						ArrayList<String> n_accs = new ArrayList<String>();
						n_accs.add(n_one);
						// 得使用者[中文]
						ArrayList<String> su_name = userDao.readAccounts(n_accs);
						// 取得使用者
						JSONObject wname_list = new JSONObject(oldDaily.getPdwnames());
						JSONArray wnames = wname_list.getJSONArray("list");
						// 有沒有新使用者
						if (!oldDaily.getPdwaccounts().contains(n_one)) {
							// 有可能沒有中文
							wnames.put(su_name.size() == 1 ? su_name.get(0) : n_one);
							oldDaily.setPdwnames(new JSONObject().put("list", wnames).toString());
							oldDaily.setPdwaccounts(oldDaily.getPdwaccounts() + "_" + n_one);
						}
						JSONObject o_pdprpbsn = new JSONObject(oldDaily.getPdpbbsn());
						JSONArray o_pdprpbsns = (o_pdprpbsn.getJSONArray("list")).put(newDaily.getPdpbbsn());
						log.info("登記入每日報表[ProductionDaily]模式?:" + n_wcg + " 登記入的 Pdpbbsn :" + newDaily.getPdpbbsn());
						o_pdprpbsn.put("list", o_pdprpbsns);
						oldDaily.setPdpbbsn(o_pdprpbsn + "");
						oldDaily.setPdtqty(o_pdprpbsns.length());
						oldDaily.setPdtsu(wnames.length());// 人數
						oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
						oldDaily.setSysmuser(user.getSuaccount());
						oldDaily.setPdprokqty(newDaily.getPdprokqty());
						log.info("登記入每日報表[ProductionDaily] 模式?:" + n_wcg + " 更新?:" + oldDaily.toString());
						dailyDao.save(oldDaily);
					}
				} else {
					// Step4 添加新工單 每日生產紀錄->新建立 && 添加新 產品SN
					ArrayList<String> su_name = userDao.readAccounts(n_pdwaccounts);
					JSONObject wnames = new JSONObject();
					wnames.put("list", new JSONArray(su_name));
					newDaily.setPdwnames(wnames.toString());
					newDaily.setPdtsu(n_pdwaccounts.size());// 人數
					newDaily.setPdwcclass(n_wcclass);// 班別
					newDaily.setPdttime("0.0");// 工時
					newDaily.setPdtqty(1);// 初始數量
					newDaily.setPdwaccounts(newDaily.getPdwaccounts());// 登記的作業員帳號(s)
					newDaily.setSyscdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setSyscuser(user.getSuaccount());
					newDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setSysmuser(user.getSuaccount());
					newDaily.setSysstatus(0);

					newDaily.setPdstime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setPdlsuid(wClass.getWclsuid());
					newDaily.setPdlname(userDao.findAllBySuid(wClass.getWclsuid()).get(0).getSuname());

					JSONArray o_pdprpbsns = new JSONArray().put(newDaily.getPdpbbsn());// 產品登記
					newDaily.setPdpbbsn(new JSONObject().put("list", o_pdprpbsns) + "");
					log.info("登記入每日報表[ProductionDaily] 新建?:" + newDaily.toString());
					dailyDao.save(newDaily);
				}
			}
		}
		return bean;
	}
}
