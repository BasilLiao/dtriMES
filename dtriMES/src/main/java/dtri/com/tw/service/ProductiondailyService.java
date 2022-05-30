package dtri.com.tw.service;

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
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductiondailyDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductiondailyService {
	private static final Logger log = LoggerFactory.getLogger(ProductiondailyService.class);
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

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size) {
		PackageBean bean = new PackageBean();
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
				pd_pr_p_model = "產品型號", pd_progress = "進度", pd_t_qty = "完成量";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			// 總數量_header_dp
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_wc_line", FFS.h_t(pd_wc_line, "80px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_wc_class", FFS.h_t(pd_wc_class, "80px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_pr_id", FFS.h_t(pd_pr_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t(pr_bom_id, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_pr_p_model", FFS.h_t(pd_pr_p_model, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_pr_total", FFS.h_t(pd_progress, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pd_t_qty", FFS.h_t(pd_t_qty, "80px", FFM.Wri.W_Y));
			for (Workstation w_one : workstations) {
				if (w_one.getWgid() != 0)
					object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + w_one.getWcname(), FFS.h_t(w_one.getWpbname(), "80px", FFM.Wri.W_Y));
			}

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
		if (sc_s_date == null || sc_e_date == null) {
			s_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(new Date()) + " 00:00:00");
			e_Date = Fm_Time.toDateTime(Fm_Time.to_y_M_d(new Date()) + " 23:59:00");
		} else {
			s_Date = Fm_Time.toDateTime(sc_s_date);
			e_Date = Fm_Time.toDateTime(sc_e_date);
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
				pbwNewObj.put("qty", 0);
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
							pbOne.put("qty", pbOne.getInt("qty") + pdOne.getPdtqty());
							pbwArr.put(index, pbOne);

							// 如果是最後一站(再累加)
							if (pbwcnameLast.equals(pdOne.getPdwcname())) {
								int tqty = Integer.parseInt(dailyBean.getPdtqty()) + pdOne.getPdtqty();
								dailyBean.setPdprogress(tqty + "/" + pdOne.getPdprtotal());
								dailyBean.setPdtqty(tqty + "");
							}
							break;
						}
					}
				} else {
					// 如果不同[新建]
					dailyBean.setId(pdOne.getPdid());
					dailyBean.setSysmdate(Fm_Time.to_y_M_d(pdOne.getSysmdate()));
					dailyBean.setPdprbomid(pdOne.getPdprbomid());
					dailyBean.setPdprid(pdOne.getPdprid());
					dailyBean.setPdprpmodel(pdOne.getPdprpmodel());
					dailyBean.setPdwcline(pdOne.getPdwcline());
					dailyBean.setPdwcclass(pdOne.getPdwcclass());
					dailyBean.setPdwpbname(new JSONArray(pbwNewArr.toString()));
					// 如果是最後一站
					int tqty = 0;
					if (pbwcnameLast.equals(pdOne.getPdwcname())) {
						tqty = pdOne.getPdtqty();
					}
					dailyBean.setPdprogress(tqty + "/" + pdOne.getPdprtotal());
					dailyBean.setPdtqty(tqty + "");

					// 工作站[統計]
					pbwArr = dailyBean.getPdwpbname();
					for (int index = 0; index < pbwArr.length(); index++) {
						JSONObject pbOne = pbwArr.getJSONObject(index);
						// 如果工作站[同一個]:累加數量
						if (pbOne.getString("wcname").equals(pdOne.getPdwcname())) {
							pbOne.put("qty", pdOne.getPdtqty());
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
		dailybeans.forEach((key, pdb_val) -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			// object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_id", pdb_val.getId());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", pdb_val.getSysmdate());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_wc_line", pdb_val.getPdwcline());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_wc_class", pdb_val.getPdwcclass());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_pr_id", pdb_val.getPdprid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_pr_bomid", pdb_val.getPdprbomid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_pr_p_model", pdb_val.getPdprpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_progress", pdb_val.getPdprogress());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pd_t_qty", pdb_val.getPdtqty());

			JSONArray pbwArr = pdb_val.getPdwpbname();
			for (int index = 0; index < pbwArr.length(); index++) {
				JSONObject pbOne = pbwArr.getJSONObject(index);
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + pbOne.getString("wcname"), pbOne.getInt("qty"));
			}

			object_dp_bodys.put(object_body);
		});
		// 共有4張表 同時回傳
		JSONObject all_json = new JSONObject();
		all_json.put("bodys_dp", object_dp_bodys);
		all_json.put("bodys_dwh", object_dp_bodys);

		bean.setBody(new JSONObject().put("search", all_json));

		updateData();
		return bean;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(JSONObject body, SystemUser user) {
		boolean check = false;
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(JSONObject body, SystemUser user) {
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
		oldDailys = dailyDao.findAllByProductionDailyCheck(null, null, null, null, null, 0);
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
						e_Date = Fm_Time.toDateTime(Fm_Time.to_yMd_Hm(productionDaily.getSysmdate()) + ":00");
						s_Date = Fm_Time.toDateTime(Fm_Time.to_yMd_Hm(productionDaily.getSyscdate()) + ":00");
						productionDaily.setPdetime(e_Date);
						total_time = (double) (e_Date.getTime() - s_Date.getTime()) / (60 * 60 * 1000);
						total_time = Math.round(total_time * 100.0) / 100.0;
					} else {
						productionDaily.setPdetime(e_Date);
						total_time = (double) (e_Date.getTime() - s_Date.getTime()) / (60 * 60 * 1000);
						total_time = Math.round(total_time * 100.0) / 100.0;
					}
					JSONObject wnames = new JSONObject(productionDaily.getPdwnames());
					JSONObject pbsn = new JSONObject(productionDaily.getPdprpbsn());

					productionDaily.setSysstatus(1);// 結算- 今日班別
					productionDaily.setPdtsu(wnames.getJSONArray("list").length());// 結算-人數
					productionDaily.setPdttime(Double.toString(total_time));// 結算-工時
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
		if (newDaily.getPdprpbsn() != null && !newDaily.getPdprpbsn().equals("") && // 產品SN號
				newDaily.getPdprid() != null && !newDaily.getPdprid().equals("") && // 製令單號
				newDaily.getPdprpmodel() != null && !newDaily.getPdprpmodel().equals("") && // 產品型號
				newDaily.getPdprbomid() != null && !newDaily.getPdprbomid().equals("") && // 產品BOM
				newDaily.getPdprtotal() != null && newDaily.getPdprtotal() != 0 && // 製令單 生產總數
				newDaily.getPdwcline() != null && !newDaily.getPdwcline().equals("") && // 生產產線
				newDaily.getPdwcname() != null && !newDaily.getPdwcname().equals("") && // 工作站代號
				newDaily.getPdwpbname() != null && !newDaily.getPdwpbname().equals("") && // 工作站名稱
				newDaily.getPdwaccounts() != null && !newDaily.getPdwaccounts().equals("") // 工作站人員

		) {

			String n_wcclass = null;
			String n_wcpline = newDaily.getPdwcline();
			String n_wcwcname = newDaily.getPdwcname();
			String n_wpbname = newDaily.getPdwpbname();
			String n_pdprid = newDaily.getPdprid();
			String n_pdprbomid = newDaily.getPdprbomid();
			String n_pdprpbsn = newDaily.getPdprpbsn();
			String n_pdwaccount = newDaily.getPdwaccounts();
			List<String> n_pdwaccounts = Arrays.asList(newDaily.getPdwaccounts().split("_"));
			String n_time = (Fm_Time.to_yMd_Hm(new Date()).split(" "))[1];
			Boolean n_wcg = null;
			ArrayList<WorkstationClass> classes = classDao.findAllBySameClass(null, n_wcpline, (n_wcwcname + "(" + n_wpbname + ")"), n_time, null);
			ArrayList<ProductionDaily> oldDailys = new ArrayList<ProductionDaily>();
			ProductionDaily oldDaily = new ProductionDaily();

			// Step1. 檢查 設置內是否有此工作站資料
			if (classes != null && classes.size() > 0) {

				// Step2. 取出 設定工作模式? [群組/個人]
				WorkstationClass wClass = classes.get(0);
				n_wcclass = wClass.getWcclass();
				n_wcg = wClass.getWcgroup();// true = 群組/ false = 單人

				// Step3. 是否有 今日登記過的 工單+產品+產線+工作站 未結單 工單資訊?
				oldDailys = dailyDao.findAllByProductionDailyCheck(n_pdprid, n_pdprbomid, n_wcpline, n_wcclass, n_wcwcname, 0);
				if (oldDailys != null && oldDailys.size() > 0) {
					oldDaily = oldDailys.get(0);

					// Step3-1. 登記過 不進行登記
					if (!oldDaily.getPdprpbsn().contains(n_pdprpbsn)) {

						// Step3-1-1. True群組? False單人?
						if (n_wcg) {
							// Step3-1-2. 同張工單+同一批人+時段一致->[更新] && 添加新 產品SN
							if (oldDaily.getPdwaccounts().equals(n_pdwaccount)) {
								JSONObject o_pdprpbsn = new JSONObject(oldDaily.getPdprpbsn());
								JSONArray o_pdprpbsns = (o_pdprpbsn.getJSONArray("list")).put(n_pdprpbsn);
								o_pdprpbsn.put("list", o_pdprpbsns);
								oldDaily.setPdprpbsn(o_pdprpbsn + "");
								oldDaily.setPdtqty(oldDaily.getPdtqty() + 1);
								oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								oldDaily.setSysmuser(user.getSuaccount());
								dailyDao.save(oldDaily);
							} else {
								// Step3-1-3. 同張工單+[不同一批人]+時段一致->[新建] && 添加新 產品SN
								ArrayList<String> su_name = userDao.readAccounts(n_pdwaccounts);
								JSONObject wnames = new JSONObject();
								wnames.put("list", new JSONArray(su_name));
								newDaily.setPdwnames(wnames.toString());
								newDaily.setPdtsu(n_pdwaccount.length());
								newDaily.setPdwcclass(n_wcclass);

								newDaily.setPdtqty(1);
								newDaily.setSyscdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								newDaily.setSyscuser(user.getSuaccount());
								newDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								newDaily.setSysmuser(user.getSuaccount());
								newDaily.setSysstatus(0);

								newDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								newDaily.setPdstime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
								newDaily.setPdlsuid(wClass.getWclsuid());
								newDaily.setPdlname(userDao.findAllBySuid(wClass.getWclsuid()).get(0).getSuname());
								newDaily.setPdprpbsn(new JSONObject().put("list", new JSONArray().put(n_pdwaccount)).toString());

								dailyDao.save(newDaily);
							}
						} else {
							// Step3-1-4 單人
							String n_acc = n_pdwaccounts.get(0);
							ArrayList<String> n_accs = new ArrayList<String>();
							n_accs.add(n_acc);
							ArrayList<String> su_name = userDao.readAccounts(n_accs);// 得使用者
							// 取得使用者
							JSONObject wname_list = new JSONObject(oldDaily.getPdwnames());
							JSONArray wnames = wname_list.getJSONArray("list");
							if (su_name.size() > 0 && !oldDaily.getPdwnames().contains(su_name.get(0))) {
								wnames.put(su_name.get(0));
								oldDaily.setPdwnames(new JSONObject().put("list", wnames).toString());
							} else {
								return bean;
							}

							JSONObject o_pdprpbsn = new JSONObject(oldDaily.getPdprpbsn());
							JSONArray o_pdprpbsns = (o_pdprpbsn.getJSONArray("list")).put(n_pdprpbsn);
							o_pdprpbsn.put("list", o_pdprpbsns);
							oldDaily.setPdprpbsn(o_pdprpbsn + "");
							oldDaily.setPdtqty(o_pdprpbsns.length());
							oldDaily.setPdtsu(wnames.length());// 人數
							oldDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							oldDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
							oldDaily.setSysmuser(user.getSuaccount());
							dailyDao.save(oldDaily);
						}
					}
				} else {
					// Step4 添加新工單 每日生產紀錄->新建立 && 添加新 產品SN
					ArrayList<String> su_name = userDao.readAccounts(n_pdwaccounts);
					JSONObject wnames = new JSONObject();
					wnames.put("list", new JSONArray(su_name));
					newDaily.setPdwnames(wnames.toString());
					newDaily.setPdtsu(n_pdwaccount.length());
					newDaily.setPdwcclass(n_wcclass);

					newDaily.setPdtqty(1);
					newDaily.setSyscdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setSyscuser(user.getSuaccount());
					newDaily.setSysmdate(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setSysmuser(user.getSuaccount());
					newDaily.setSysstatus(0);

					newDaily.setPdtsu(n_pdwaccounts.size());// 人數
					newDaily.setPdstime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setPdetime(Fm_Time.toDateTime(Fm_Time.to_yMd_Hms(new Date())));
					newDaily.setPdlsuid(wClass.getWclsuid());
					newDaily.setPdlname(userDao.findAllBySuid(wClass.getWclsuid()).get(0).getSuname());

					JSONArray o_pdprpbsns = new JSONArray().put(n_pdprpbsn);
					newDaily.setPdprpbsn(new JSONObject().put("list", o_pdprpbsns) + "");

					dailyDao.save(newDaily);
				}
			}

		}
		return bean;
	}
}
