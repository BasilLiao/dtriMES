package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.RmaList;
import dtri.com.tw.db.entity.RmaMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.RmaListDao;
import dtri.com.tw.db.pgsql.dao.RmaMailListDao;

@Service
public class RepairRmaListBatService {
//	@Autowired
//	private RepairUnitDao unitDao;

	@Autowired
	private RmaListDao rmaListDao;

	@Autowired
	private RmaMailListDao rmaMailListDao;

	@Autowired
	BasicNotificationMailService mailService;

	// delete 資料清單
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		try {
			JSONObject body = req.getBody();
			JSONObject list = body.getJSONObject("modifyDelete");
			System.out.println(list);

			String rma_number = list.getString("modify_rmano");
			rma_number = rma_number.equals("") ? null : rma_number;

			String serial_number = list.getString("modify_sn");
			serial_number = serial_number.equals("") ? null : serial_number;

			if (rma_number.length() > 0 && serial_number == null) {
				// 排除1與2和3的紀錄,才不會搜尋到 以收到 或 處理完畢 和 已寄出 的資料 , 要刪除 在機台還沒收到前
				List<RmaList> rls = rmaListDao.findAllBysnAndmb(null, rma_number, null, null, Arrays.asList(1, 2, 3));
				if (rls.size() > 0) {
					rmaListDao.deleteByrmaNumber(rma_number);
					return true;
				} else {
					resp.autoMsssage("102"); // 沒找到東西
					return false;
				}
			} else if (rma_number.length() > 0 && serial_number != null) {
				// 排除1與2和3的紀錄,才不會搜尋到 以收到 或 處理完畢 和 已寄出 的資料 , 要刪除 在機台還沒收到前
				List<RmaList> rls = rmaListDao.findAllBysnAndmb(null, rma_number, serial_number, null,
						Arrays.asList(1, 2, 3));
				if (rls.size() > 0) {
					rmaListDao.deleteByRmaNumberAndSerialNumber(rma_number, serial_number);
					return true;
				} else {
					resp.autoMsssage("102"); // 沒找到東西
					return false;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return false;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		JSONArray list = body.getJSONArray("modify");
//		String serialNumber = jsonObject.getString("System S/N").trim(); // 去除前後空格
//		serialNumber = serialNumber.equals("") ? "" : serialNumber;
		if (list.isEmpty()) {
			return check;
		}
		Long rmaid = null;
		String rmano = null;
		String rmaMachSn = null;
		String rmaMbsn = null;
		Integer StateCheck = null;
		String state = null;	
		try {
			for (Object object : list) {
				JSONObject one = (JSONObject) object;
				// rmasn必須要有資料
				rmano = one.getString("modify_rmano");
				// 先確認 one 是否包"rma_mb_sn 物件
				if (one.has("modify_id")) {
					rmaid = (long) one.optInt("modify_id");
					rmaid = (rmaid == -1) ? 0 : rmaid;
				}
				// 先確認 one 是否包"rma_mb_sn 物件
				if (one.has("modify_sn")) {
					rmaMachSn = one.getString("modify_sn");
					rmaMachSn = rmaMachSn.equals("") ? null : rmaMachSn;
				}
				// 先確認 one 是否包"rma_mb_sn 物件
				if (one.has("rma_mb_sn")) {
					rmaMbsn = one.getString("rma_mb_sn");
					rmaMbsn = rmaMbsn.equals("") ? null : rmaMbsn;
				}
				StateCheck = one.getInt("modify_state_select"); // 取至SELECT 目前進度 所以在回圈內都是一樣的值
				StateCheck = (StateCheck == -1) ? 0 : StateCheck;
				state = one.getString("modify_state"); // 取的中文表示的狀態
				state = state.equals("") ? null : state;
				ArrayList<RmaList> rls = rmaListDao.findAllByRdidAndRdruidBat1(rmaid, rmano, rmaMachSn, rmaMbsn, null,
						null, null, null, null);
				if (rls.size() > 0) {
					RmaList rl = rls.get(0);
					rl.setState(one.getString("modify_state"));
					rl.setStateCheck(one.getInt("modify_state_select"));
					rl.setSysmuser(user.getSuaccount()); //修改人員
					rl.setSysmdate(new Date());
					rmaListDao.save(rl);
					check = true;
				}
			}

			// *******************比較條件後(全部收到後)----> 準備寄信 ******************
			ArrayList<RmaList> rls = rmaListDao.findAllByRdidAndRdruidBat1(null, rmano, null, null, null, null, null,
					StateCheck, null);
			Long counts = rmaListDao.countByRmaNumber(rmano); // 取得這筆RMA裡的筆數
			int x = (int) (counts * StateCheck); // 筆數 * 狀態欄
			int c = 0;
			// 計算RMA裡的 StateCheck 數 的總和多少 在與 X 做比較
			for (int i = 0; i < rls.size(); i++) {
				c = c + rls.get(i).getStateCheck();
			}
			System.out.println(c);
			System.out.println(x);

			// 若符合1收到貨 或 3已寄出 及 x(筆數＊狀態) ＝＝ c(狀態累加)
			if ((StateCheck == 1 || StateCheck == 3) && (c == x)) {
				// ************************** 取得 MAIL 清單 ***********************
				// rmlds 就是一個 ArrayList<RmaMail> 型別的變數，存放查詢出來的所有RmaMail 物件。
				ArrayList<RmaMail> rmlds = rmaMailListDao.findAll();
				StringBuilder rmaMailList = new StringBuilder(); // 使用 StringBuilder 來累加字串

				// 符合收到貨 條件 取得需要寄信人員名單
				if (!rmlds.isEmpty()) { // 用 `isEmpty()` 取代 `size() > 0`
					rmlds.forEach(rl -> {
						if ("Y".equals(rl.getSureceived())) {// 如果 sureceived(收到貨) 是 "Y"
							rmaMailList.append(rl.getSuemail()).append(";"); // 加入 email，並在後面加 ";"
						}
					});
				}

				// ************************** 寄信 ********************
				String mailList = rmaMailList.toString(); // 轉換為 String
				String[] toUser = mailList.split(";"); // 用 ";" 分割成 String 陣列
				// String[] toUser = { "johnny_chuang@dtri.com", "ansolder@gmail.com" };
				String[] toCcUser = { "" };
				String subject = "RMA 通知" + rmano + state + "";
				// 構建郵件內容
				StringBuilder httpstr = new StringBuilder();
				httpstr.append("Dear All, <br><br>").append("通知 ").append(rmano).append(state); // .append("請提領<br><br>");

				mailService.sendEmail(toUser, toCcUser, subject, httpstr.toString(), null, null);
			}

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 上傳RMALISTExcel
	@Transactional
	public boolean excelToSave(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		System.out.println(body);
		String rmano = body.getJSONObject("excelSend").getString("filePart1");
		String guest = body.getJSONObject("excelSend").getString("filePart2");
		JSONArray lists = body.getJSONObject("excelSend").getJSONArray("data");
		if (lists.isEmpty()) {
			resp.autoMsssage("108"); // 回傳錯誤訊息
			return false;
		}
		List<RmaList> RmaList = new ArrayList<>();
//		List<RmaList> x = rmaListDao.findByRmaNumberContaining(rmano);
		// Set 集合的 contains() 方法 用來快速檢查是否已經有這個 rmano，效率比 List 高
		Set<RmaList> x = rmaListDao.findByRmaNoContaining(rmano);
		// 追蹤已出現的 serialNumber
		Set<String> existingSerialNumbers = new HashSet<>();
		// 追蹤已出現的 serialNumber
		Set<String> existingmbNumbers = new HashSet<>();

		// x=0表示資料庫無資料後 可存入資料庫
		if (x.size() == 0) { // 程式中可以用 isEmpty() 或 size() 來檢查是否為空。
			String serialNumber = "";
			String mbNumber = ""; // null 在
			String model = "";
			String issue = "";
			String partNo = "";
			String wtyStatus = "";
			for (Object listx : lists) {
				if (listx instanceof JSONObject) {
					JSONObject one = (JSONObject) listx;

					// 先確認 JSONObject 是否包含"rma_mb_sn 物件
					if (one.has("System S/N")) {
						serialNumber = one.getString("System S/N").trim(); // 去除前後空格
						serialNumber = serialNumber.equals("") ? "" : serialNumber;
					}
					if (one.has("Motherboard S/N")) {
						mbNumber = one.getString("Motherboard S/N").trim();
						mbNumber = mbNumber.equals("") ? "" : mbNumber;
					}
					if (one.has("Model Name")) {
						model = one.getString("Model Name").trim();
						model = model.equals("") ? "" : model;
					}
					if (one.has("Issue Description")) {
						issue = one.getString("Issue Description").trim();
						issue = issue.equals("") ? "" : issue;
					}
					// ****** Oracle****************
					if (one.has("Failure Description")) {
						issue = one.getString("Failure Description").trim();
						issue = issue.equals("") ? "" : issue;
					}
					if (one.has("S/N")) {
						serialNumber = one.getString("S/N").trim();
						serialNumber = serialNumber.equals("") ? "" : serialNumber;
					}
					if (one.has("Oracle P/N#")) {
						partNo = one.getString("Oracle P/N#").trim();
						partNo = partNo.equals("") ? "" : partNo;
					}
					if (one.has("Warranty status")) {
						wtyStatus = one.getString("Warranty status").trim();
						wtyStatus = wtyStatus.equals("") ? "" : wtyStatus;
					}

					// ********** 如果 serialNumber 已存在，則加上 _01, _02 ...
					String uniqueSerial = serialNumber;
					if (!serialNumber.isEmpty()) {
						int counter = 1;
						while (existingSerialNumbers.contains(uniqueSerial)) {
							uniqueSerial = serialNumber + "_" + String.format("%02d", counter);
							serialNumber = serialNumber + "_" + String.format("%02d", counter);
							counter++;
						}
						existingSerialNumbers.add(uniqueSerial);
					}
					// ********** 如果 mbNumber 已存在，則加上 _01, _02 ...
					String uniqueMb = mbNumber;
					// 在這裡執行當 mbNumber 不是 空值 時的邏輯
					if (!mbNumber.isEmpty()) {
						int counter = 1;
						while (existingmbNumbers.contains(uniqueMb)) {
							uniqueMb = mbNumber + "_" + String.format("%02d", counter);
							mbNumber = mbNumber + "_" + String.format("%02d", counter);
							counter++;
						}
						existingmbNumbers.add(uniqueMb);
					}
					String syscuser = user.getSuaccount();	//建立人員
					String sysmuser = user.getSuaccount();	//修改人員
					RmaList rma = new RmaList(rmano, model, guest, serialNumber, mbNumber, issue, partNo, wtyStatus,
							syscuser, sysmuser);
					RmaList.add(rma);
				}
			}
			try {
				rmaListDao.saveAll(RmaList);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		resp.autoMsssage("107"); // 回傳錯誤訊息
		return false;
	}

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {

		int page1 = req.getPage_batch(); // 取得請求中的當前頁數
		int p_size1 = req.getPage_total(); // 取得每頁的筆數
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		// 這裡直接覆蓋上面取得的值，讓分頁查詢變成 "不分頁"
		page1 = 0;
		p_size1 = 99999; // 設定為極大值，意味著一次查詢所有資料
		PageRequest page_r = PageRequest.of(page1, p_size1, Sort.by("rmaNumber").descending());
		boolean check = false;

		JSONObject body = req.getBody(); // 建立空的JSON格式

		if (body == null || body.isNull("search")) {
			System.out.println("搜尋資料body");
			return true;
		}
		String rmaNO = body.getJSONObject("search").getString("rma_sn").trim();
		rmaNO = rmaNO.equals("") ? null : rmaNO;
		String rma_b_sn = body.getJSONObject("search").getString("rma_b_sn").trim();
		rma_b_sn = rma_b_sn.equals("") ? null : rma_b_sn;
		String rma_mb_sn = body.getJSONObject("search").getString("rma_mb_sn").trim();
		rma_mb_sn = rma_mb_sn.equals("") ? null : rma_mb_sn;
		String state = body.getJSONObject("search").getString("state").trim();
		state = state.equals("") ? null : state;

		// ************************** 取得 MAIL 清單 ***********************
		ArrayList<RmaMail> rmlds = rmaMailListDao.findAll(); // rmlds 就是一個 ArrayList<RmaMail> 型別的變數，存放查詢出來的所有 RmaMail物件。

		StringBuilder rmaMailList = new StringBuilder(); // 使用 StringBuilder 來累加字串
		if (!rmlds.isEmpty()) { // 用 `isEmpty()` 取代 `size() > 0`
			rmlds.forEach(rl -> {
				if ("Y".equals(rl.getSureceived())) {// 如果 sureceived 是 "Y"
					rmaMailList.append(rl.getSuemail()).append(";"); // 加入 email，並在後面加 ";"
				}
			});
		}

		System.out.println(rmaMailList);
		// ************************** 取得 RMA 清單 ************************
		ArrayList<RmaList> rls = rmaListDao.findAllByRdidAndRdruidBat1(null, rmaNO, rma_b_sn, rma_mb_sn, null, null,
				state, null, page_r);
		JSONObject search = new JSONObject();
		JSONArray object_bodys = new JSONArray();

		if (rls.size() > 0) {
			rls.forEach(rl -> {
				JSONObject object_body = new JSONObject();

				object_body.put("id", rl.getId());
				object_body.put("rma_number", rl.getRmaNumber());
				object_body.put("customer", rl.getCustomer());
				object_body.put("model", rl.getModel());
				object_body.put("Part_No", rl.getPartNo());
				object_body.put("serial_number", rl.getSerialNumber());
				object_body.put("mb_number", rl.getMbNumber());
				object_body.put("issue", rl.getIssue());

				object_body.put("rma_result", rl.getRrd_RmaResult());
				object_body.put("stateCheck", rl.getStateCheck());
				object_body.put("state", rl.getState()); // 未收到
				
				object_body.put("syscdate", rl.getSyscdate()); 	// 建立時間
				object_body.put("syscuser", rl.getSyscuser());	//建立人
				object_body.put("sysmdate", rl.getSysmdate());	//修改時間
				object_body.put("sysmuser", rl.getSysmuser());	//修改人
				
				// 0:未收到 1:已收到 2:處理完畢 3:已寄出
				object_bodys.put(object_body);

			});
			// bean.setBody(new JSONObject().put("search", object_bodys));
			search.put("search", object_bodys);
			search.put("rmamail", rmaMailList);
			bean.setBody(search);

			return true;
		}

		System.out.println("RmaList查無資料，結束執行。");
		bean.autoMsssage("102"); // 回傳錯誤訊息
		return check;
	}
}
