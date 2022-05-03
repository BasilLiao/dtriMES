package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.ProductionDaily;
import dtri.com.tw.db.entity.SystemConfig;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.WorkstationClass;
import dtri.com.tw.db.pgsql.dao.ProductiondailyDao;
import dtri.com.tw.db.pgsql.dao.SystemConfigDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductiondailyService {

	@Autowired
	private SystemConfigDao configDao;

	@Autowired
	private ProductiondailyDao dailyDao;

	@Autowired
	private WorkstationClassDao classDao;

	@Autowired
	private SystemUserDao userDao;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size) {
		PackageBean bean = new PackageBean();
		ArrayList<SystemConfig> systemConfigs = new ArrayList<SystemConfig>();
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("scgid").descending());
		String sc_name = null;
		String sc_g_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sc_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sc_g_id", FFS.h_t("群組ID", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sc_g_name", FFS.h_t("群組名稱", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sc_name", FFS.h_t("名稱", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sc_value", FFS.h_t("參數", "400px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "300px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_N));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "sc_id", "ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "sc_g_id", "群組ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "sc_g_name", "群組名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "sc_name", "名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-3", true, n_val, "sc_value", "參數"));

			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_N,
			// "col-md-2", false, n_val, "sys_c_date", "建立時間"));
			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_N,
			// "col-md-2", false, n_val, "sys_c_user", "建立人"));
			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_N,
			// "col-md-2", false, n_val, "sys_m_date", "修改時間"));
			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_N,
			// "col-md-2", false, n_val, "sys_m_user", "修改人"));

			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFS.TTA, FFM.Type.TEXT, "", "",
			// FFM.Wri.W_Y, "col-md-12",
			// false, n_val, "sys_note", "備註"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", "版本"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "sys_status", "狀態"));
			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "sc_g_name", "群組名稱", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "sc_name", "名稱", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			sc_name = body.getJSONObject("search").getString("sc_name");
			sc_name = sc_name.equals("") ? null : sc_name;
			sc_g_name = body.getJSONObject("search").getString("sc_g_name");
			sc_g_name = sc_g_name.equals("") ? null : sc_g_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		systemConfigs = configDao.findAllByConfig(sc_name, sc_g_name, Integer.parseInt(status), page_r);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		systemConfigs.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sc_id", one.getScid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sc_g_id", one.getScgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sc_g_name", one.getScgname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sc_name", one.getScname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sc_value", one.getScvalue());

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
	public boolean updateData(JSONObject body, SystemUser user) {
		boolean check = false;
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
		Boolean check = false;
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
			Boolean wcg = null;
			ArrayList<WorkstationClass> classes = classDao.findAllBySameClass(null, n_wcpline, (n_wcwcname + "(" + n_wpbname + ")"), n_time, null);
			ArrayList<ProductionDaily> oldDailys = new ArrayList<ProductionDaily>();
			ProductionDaily oldDaily = new ProductionDaily();

			// Step1. 檢查 設置內是否有此工作站資料
			if (classes != null && classes.size() > 0) {

				// Step2. 取出 設定工作模式? [群組/個人]
				WorkstationClass wClass = classes.get(0);
				n_wcclass = wClass.getWcclass();
				wcg = wClass.getWcgroup();// true = 群組/ false = 單人

				// Step3. 是否有 今日登記過的 工單+產品+產線+工作站 未結單 工單資訊?
				oldDailys = dailyDao.findAllByProductionDaily(n_pdprbomid, null, null, null, null, 0, n_wcclass, n_wcpline, n_pdprid, null, null);
				if (oldDailys != null && oldDailys.size() > 0) {
					oldDaily = oldDailys.get(0);

					// Step3-1. 登記過 不進行登記
					if (!oldDaily.getPdprpbsn().contains(n_pdprpbsn)) {

						// Step3-1-1. True群組? False單人?
						if (wcg) {
							// Step3-1-2. 同張工單+同一批人+時段一致->[更新] && 添加新 產品SN
							if (oldDaily.getPdwaccounts().equals(n_pdwaccount)) {
								JSONObject o_pdprpbsn = new JSONObject(oldDaily.getPdprpbsn());
								JSONArray o_pdprpbsns = (o_pdprpbsn.getJSONArray("list")).put(n_pdprpbsn);
								o_pdprpbsn.put("list", o_pdprpbsns);
								oldDaily.setPdprpbsn(o_pdprpbsn + "");
								oldDaily.setPdtqty(oldDaily.getPdtqty() + 1);
								oldDaily.setPdetime(new Date());
								oldDaily.setSysmdate(new Date());
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
								newDaily.setSyscdate(new Date());
								newDaily.setSyscuser(user.getSuaccount());
								newDaily.setSysmdate(new Date());
								newDaily.setSysmuser(user.getSuaccount());
								newDaily.setSysstatus(0);

								newDaily.setPdstime(new Date());
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
							if (!oldDaily.getPdwnames().contains(su_name.get(0))) {
								wnames.put(su_name.get(0));
								oldDaily.setPdwnames(new JSONObject().put("list", wnames).toString());
							}

							JSONObject o_pdprpbsn = new JSONObject(oldDaily.getPdprpbsn());
							JSONArray o_pdprpbsns = (o_pdprpbsn.getJSONArray("list")).put(n_pdprpbsn);
							o_pdprpbsn.put("list", o_pdprpbsns);
							oldDaily.setPdprpbsn(o_pdprpbsn + "");
							oldDaily.setPdtqty(o_pdprpbsns.length());
							oldDaily.setPdtsu(wnames.length());// 人數
							oldDaily.setPdetime(new Date());
							oldDaily.setSysmdate(new Date());
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
					newDaily.setSyscdate(new Date());
					newDaily.setSyscuser(user.getSuaccount());
					newDaily.setSysmdate(new Date());
					newDaily.setSysmuser(user.getSuaccount());
					newDaily.setSysstatus(0);

					newDaily.setPdtsu(n_pdwaccounts.size());// 人數
					newDaily.setPdstime(new Date());
					newDaily.setPdetime(new Date());
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
