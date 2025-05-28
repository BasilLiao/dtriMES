package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.bean.ProductionDailyBean;
import dtri.com.tw.db.entity.ProductionDaily;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.RepairDetail;
import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductiondailyDao;
import dtri.com.tw.db.pgsql.dao.SystemMailDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductionDailyYieldService {
//	private static final Logger log = LoggerFactory.getLogger(ProductionDailyYieldService.class);
	@Autowired
	private ProductiondailyDao dailyDao;

	@Autowired
	private WorkstationDao workstationDao;

	@Autowired
	private WorkstationProgramDao programDao;

	@Autowired
	private ProductionHeaderDao headerDao;

	@Autowired
	private ProductionBodyDao bodyDao;

	@Autowired // 信寄清單
	private SystemMailDao sysMailListDao;
	@Autowired // 系統寄信
	BasicNotificationMailService mailService;

	@Autowired
	EntityManager em;

	// 取得當前 資料清單
	@SuppressWarnings("unchecked")
	public boolean getData() {

		// ************************** 取得 MAIL 清單 ***********************
		// rmlds 就是一個 ArrayList<RmaMail> 型別的變數，存放查詢出來的所有RmaMail 物件。
		// 搜尋"完成通知"的人員信箱資料
		ArrayList<SystemMail> sysmlds = sysMailListDao.findAll();
		StringBuilder MailList = new StringBuilder(); // 使用 StringBuilder 來累加字串
		
		// 符合收到貨 條件 取得需要寄信人員名單
		if (!sysmlds.isEmpty()) { // 用 `isEmpty()` 取代 `size() > 0`
			sysmlds.forEach(rl -> {
				if ("Y".equals(rl.getSudailyreport())) {// 如果 Surepairdone(處理好) 是 "Y"
					MailList.append(rl.getSuemail()).append(";"); // 加入 email，並在後面加 ";"
				}				
			});

		} else {
			System.out.println("SystemMail空的");
			return false;
		}

		if (MailList.isEmpty()) {
			System.out.println("無寄件者名單");
			return false;
		}		
		String mmdd = null;
		mmdd = Fm_Time.to_y_M_d(new Date());

		//String[] toUser = { "johnny_chuang@dtri.com" };
		String mailList = MailList.toString(); // 轉換為 String
		String[] toUser = mailList.split(";"); // 用 ";" 分割成 String 陣列
		String[] toCcUser = { "" };
		String subject = "Wugu Test result "+"(" + mmdd +")";  //Wugu Test result (2025-05-26)
	
//******************************  日報表  ********************
		ArrayList<ProductionDaily> productionDailys = new ArrayList<ProductionDaily>();
		ArrayList<Workstation> workstations = new ArrayList<Workstation>();
		LinkedHashMap<String, ProductionDailyBean> dailybeans = new LinkedHashMap<String, ProductionDailyBean>();

		// 查詢
//		String sc_s_date = null;
//		String sc_e_date = null;
		String sc_bom_id = null;
		String sc_model = null;
		String sc_class = null;
		String sc_line = null;
		String sc_id = null;
		Date s_Date = new Date();
		Date e_Date = new Date();

		s_Date = Fm_Time.toDateTime(mmdd + " 08:30:00");
		e_Date = Fm_Time.toDateTime(mmdd + " 19:30:00");

		System.out.println(s_Date);
		
		// [準備] 把每個工作站查詢出過站的資料 dailybeans
		// Step1. 工作站[{"wcname":"D0001","wpbname":"加工站","qty":"50"},{},{}]
		workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null); // 查詢工作站代表
		JSONArray pbwNewArr = new JSONArray();
		for (Workstation w_one : workstations) {
			if (w_one.getWgid() != 0) {
				JSONObject pbwNewObj = new JSONObject();
				pbwNewObj.put("wcname", w_one.getWcname()); //"D0001""D0002""D0003""D0004""D0005""D0006"
				pbwNewObj.put("wpbname", w_one.getWpbname()); //加工站""組裝站""測試站" "整理站""包裝站""拆解站"
				pbwNewObj.put("tsulist", new JSONArray()); //[]
				pbwNewObj.put("qtsu", 0);// 當天 使用者數       //0
				pbwNewObj.put("qty", 0);// 當天 台數          //0
				pbwNewObj.put("qttime", 0.0);// 當天 工時數   //0
				pbwNewArr.put(pbwNewObj);
			}
		}

		// Step2. [每日生產數量]把每個工作站查詢出過站的資料() dailybeans
		productionDailys = dailyDao.findAllByProductionDaily(sc_line, sc_class, sc_id, sc_model, sc_bom_id, s_Date,	e_Date);
		if (productionDailys.size() > 0) {
			// Step3. [準備]每工單的 最後一站的數量 今天的產出數量
			Map<String, String> last_wcname = new HashMap<String, String>();
			Map<String, Integer> wait_fix = new HashMap<String, Integer>();
			// 被途中移除的清單(排除)
			Map<String, Boolean> remove_prid = new HashMap<String, Boolean>();

			for (ProductionDaily object : productionDailys) {
				String prid = object.getPdprid();   //取得"工單號"
				if (!last_wcname.containsKey(prid)) {  //如果last_wcname 容器裡面沒有 此"工單號"
					ProductionRecords prs = new ProductionRecords(); //生產紀錄
					prs.setPrid(prid);
					List<ProductionHeader> headers = headerDao.findAllByProductionRecords(prs);  //主產品製程表頭
					// 沒資料?可能被臨時移除
					if (headers.size() == 0) {
						remove_prid.put(prid, false);
						continue;
					}
					Long wpid = headers.get(0).getPhwpid();// 取得 工作程序 ID 
					ArrayList<WorkstationProgram> pbwLast = programDao.findAllByWpgidOrderBySyssortAsc(wpid); //工作程序
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
				// 被途中移除的清單(排除)
				if (remove_prid.containsKey(pdOne.getPdprid())) {
					continue;
				}

				// 同一天+同一條產線+同一班別+同一張工單
				key = Fm_Time.to_y_M_d(pdOne.getSysmdate()) + "_" + pdOne.getPdwcline() + "_" + pdOne.getPdwcclass()+ "_" + pdOne.getPdprid();
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
							pdOne_old.put("qttime",	pdOne_old.getDouble("qttime") + Double.parseDouble(pdOne.getPdttime()));// 工時數
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
						dailyBean.setPdttqty(pdOne.getPdttqty() + "");/// 每日 產量良率 j 測試次數 // 工單 [當日]測試數 好數量
						dailyBean.setPdttbadqty(pdOne.getPdttbadqty() + "");/// 每日 產量良率 j 故障次數 // 工單 [當日]測試數 壞掉數量
						dailyBean.setPdttyield(pdOne.getPdttyield()); // 每日 產量良率 j 測試次數(不良率)
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
					dailyBean.setPdttqty(pdOne.getPdttqty() + ""); // j測試次數 // 工單 [當日]測試數 好數量
					dailyBean.setPdttbadqty(pdOne.getPdttbadqty() + ""); // j故障次數 // 工單 [當日]測試數 壞掉數量
					dailyBean.setPdttyield(pdOne.getPdttyield());// j測試次數(不良率) // 工單 [當日]測試數 良率比
					// Step6-4.[每日 產品不良] 同一日 最後的完成數量
					dailyBean.setPdprttokqty(pdOne.getPdprttokqty() + ""); // j測試完成總數 // 工單 [累計]產品測試數 好數量
					dailyBean.setPdprbadqty(pdOne.getPdprbadqty() + ""); // j 測試故障總數 //工單 [當日]測試數 壞掉數量
					dailyBean.setPdpryield(pdOne.getPdpryield()); // j測試總數(不良率) // 工單 [當日]測試數 良率比

					dailybeans.put(key, dailyBean);
					// System.out.println("123");
				}
			}
		}else {		
			System.out.println("dailybeans無資料");
			return false;			
		}
		
		//************************* 構建郵件內容  **************************
		StringBuilder httpstr = new StringBuilder();
		//把TABLE表雙層格線變成單線
		httpstr.append("<style>")
	       .append("table { border-collapse: collapse; }")
	       .append("table, th, td { border: 1px solid black; padding: 5px; }")
	       .append("</style>");
		//構建寄件內容
		httpstr.append("Dear All, <br><br>").append("通知 五股產線").append(mmdd).append("測試狀況如下<br><br>")

		.append("<table border='1'><tr>").append("<th>時間</th>" //
				+ "<th>產線</th>" // 產線
				+ "<th>工單號</th>" // 工單號
				+ "<th>BOM號</th>" // BOM號
				+ "<th>產品型號</th>" // 產品型號
				+ "<th>工單總數</th>" // 工單總數
				+ "<th>測試次數</th>" // 測試次數
				+ "<th>故障次數</th>" // 測試故障(次數)
				+ "<th>測試次數(不良率)</th>" // 測試(次數)良率
				+ "</tr>");
		
		//======= 建立TABLE 內容 =======
		dailybeans.forEach((key, pdb_val) -> {
			if (!pdb_val.getPdttqty().trim().equals("0")) {
				// 若故障次數不是0，顯示紅色
				String badQty = pdb_val.getPdttbadqty();
				if (!"0".equals(badQty)) {
					httpstr.append("<tr>")
							.append("<td style='color:red;'>").append(pdb_val.getSysmdate()).append("</td>") // 修改時間
							.append("<td style='color:red;'>").append(pdb_val.getPdwcline()).append("</td>") // 產線(現別)
							.append("<td style='color:red;'>").append(pdb_val.getPdprid()).append("</td>") // 工單號
							.append("<td style='color:red;'>").append(pdb_val.getPdprbomid()).append("</td>") // 產品BOM
							.append("<td style='color:red;'>").append(pdb_val.getPdprpmodel()).append("</td>")// 產品型號
							.append("<td style='color:red;'>").append(pdb_val.getPdprtotal()).append("</td>") // 工單總數
							.append("<td style='color:red;'>").append(pdb_val.getPdttqty()).append("</td>") // 測試(次數)
							.append("<td style='color:red;'>").append(pdb_val.getPdttbadqty()).append("</td>") // 測試故障(次數)
							.append("<td style='color:red;'>").append(pdb_val.getPdttyield()).append("</td>") // 測試(次數)良率
							.append("</tr>");
				} else {
					httpstr.append("<tr>")
							.append("<td>").append(pdb_val.getSysmdate()).append("</td>") // 修改時間
							.append("<td>").append(pdb_val.getPdwcline()).append("</td>") // 產線(現別)
							.append("<td>").append(pdb_val.getPdprid()).append("</td>") // 工單號
							.append("<td>").append(pdb_val.getPdprbomid()).append("</td>") // 產品BOM
							.append("<td>").append(pdb_val.getPdprpmodel()).append("</td>")// 產品型號
							.append("<td>").append(pdb_val.getPdprtotal()).append("</td>") // 工單總數
							.append("<td>").append(pdb_val.getPdttqty()).append("</td>") // 測試(次數)
							.append("<td>").append(pdb_val.getPdttbadqty()).append("</td>") // 測試故障(次數)
							.append("<td>").append(pdb_val.getPdttyield()).append("</td>") // 測試(次數)良率
							.append("</tr>");
				}
			}
		});

		httpstr.append("</table> <br>");
		System.out.println("測試狀況不良率");
				
//********************************* 維修紀錄 程式碼		*************************
		List<RepairDetail> details = new ArrayList<RepairDetail>();		
		String str = "";
		// Step1.=======  日期轉換 =======
		
//		str=" rd.sys_c_date > '2024-09-20 08:30:00'  AND rd.sys_c_date < '2024-09-20 19:30:00'  ";
		str = " rd.sys_c_date > '" + s_Date + "' AND " + " rd.sys_c_date < '" + e_Date + "'";

		// Step2.=======Analysis report 查詢SN欄位+產品型號+製令單號 =======
		String nativeQuery = "SELECT rd.* FROM repair_detail rd " + //
				"join repair_register rr on rd.rd_rr_sn = rr.rr_sn " + //
				"join repair_order ro on rd.rd_ro_id = ro.ro_id WHERE ";
		nativeQuery += str;
		nativeQuery += " order by rr.rr_pr_id desc ";
//		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		System.out.println(nativeQuery);
		try {
			Query query = em.createNativeQuery(nativeQuery, RepairDetail.class);
			details = query.getResultList();
//			if (details.size() > 25000) {
//				System.out.println("資料大於25000");
//				return false;
//			}
		} catch (Exception e) {
	
			return false;
		}
		
	//************************* 構建郵件內容  **************************
		httpstr.append("<table border='2'><tr>")
				.append("<th>製令單</th>" + "<th>製令數量</th>" // 製令數量
				+ "<th>產品型號</th>" // 產品型號
				+ "<th>產品(序號)</th>" // 產品(序號)
				+ "<th>故障代號</th>" // 故障代號
				+ "<th>描述問題</th>" // 描述問題
				+ "<th>故障原因</th>" // 故障原因
				+ "<th>解決問題</th>" // 解決問題
				+ "<th>產品類型</th>" // 產品類型
				+ "</tr>");
//		if (details.size() <= 0) {
//			bean.autoMsssage("102");
//			return false;
//		}
		//======= 建立TABLE 內容 =======
		details.forEach(one -> {

			httpstr.append("<tr>")
					.append("<td>").append(one.getRegister().getRrprid() == null ? "" : one.getRegister().getRrprid()).append("</td>") // 製令單
					.append("<td>").append(one.getRegister().getRrphpqty()).append("</td>") // 製令數量
					.append("<td>").append(one.getRegister().getRrprpmodel()).append("</td>") // 產品型號
					.append("<td>").append(one.getRegister().getRrsn()).append("</td>") // 產品(序號)
					.append("<td>").append(one.getRdrcvalue() == null ? "" : one.getRdrcvalue()).append("</td>") // 故障代號
					.append("<td>").append(one.getRdstatement() == null ? "" : one.getRdstatement().replaceAll("\\n", "")).append("</td>") // 描述問題
					.append("<td>").append(one.getRdtrue() == null ? "" : one.getRdtrue().replaceAll("\\n", "")).append("</td>") // 故障原因
					.append("<td>").append(one.getRdsolve() == null ? "" : one.getRdsolve().replaceAll("\\n", "")).append("</td>") // 解決問題
					.append("<td>").append(one.getRegister().getRrpbtype()).append("</td>") // 產品類型
					.append("</tr>");
		});

		httpstr.append("</table> <br>");
		System.out.println("維修紀錄");

		//************* 日報表有資料才會寄送 *************
		if (productionDailys.size() > 0) {
			// 發送郵件
			mailService.sendEmail(toUser, toCcUser, subject, httpstr.toString(), null, null);
			System.out.println("發送郵件");
		}		
		return true;
	}
}
