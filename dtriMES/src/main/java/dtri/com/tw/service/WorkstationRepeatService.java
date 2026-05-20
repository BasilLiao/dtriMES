package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;

@Service
public class WorkstationRepeatService {
	private static final Logger log = LoggerFactory.getLogger(WorkstationRepeatService.class);
	@Autowired
	private ProductionHeaderDao headerDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private WorkstationProgramDao programDao;
	@Autowired
	private WorkstationDao workDao;

	// @PersistenceUnit(unitName = "default")
	// private EntityManagerFactory emf;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		// int p_size = req.getPage_total();
		List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
		List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();
		// 進行-特定查詢(重工工單)
		String now_order = body.getJSONObject("search").getString("m_now_order");
		String m_old_only_sn = body.getJSONObject("search").getString("m_old_only_sn");

		ProductionRecords phprid = new ProductionRecords();
		phprid.setPrid(now_order);
		prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A521_old_sn");

		// u資料
		if (prArrayList.size() == 1) {
			// 查詢多少台
			Long phpbgid = prArrayList.get(0).getPhpbgid();
			List<ProductionBody> bodies = bodyDao.findAllByPbgidAndPbbsnNotOrderByPbsnAsc(phpbgid, "no_sn");
			List<ProductionBody> bodies_old = bodyDao.findAllByPbbsn(m_old_only_sn);

			// 如果有-> 回傳原先製令單
			String m_old_order = "";
			if (bodies_old.size() == 1) {
				ProductionBody one_old = bodies_old.get(0);
				// 有舊資料 則顯示舊的工單
				if (one_old != null && one_old.getPboldsn() != null && !one_old.getPboldsn().equals("")) {
					List<ProductionBody> bodies_old_last = bodyDao.findAllByPbbsnLike("%" + m_old_only_sn + "_old%");
					if (bodies_old_last.size() == 1) {
						bodies_old = bodies_old_last;
					}
				}

				prArrayList_old = headerDao.findAllByPhpbgid(bodies_old.get(0).getPbgid());
				m_old_order = prArrayList_old.size() > 0 ? prArrayList_old.get(0).getProductionRecords().getPrid() : "";
			}

			bean.setBody(new JSONObject().put("search", new JSONObject().//
					put("phpnumber_total", prArrayList.get(0).getPhpqty()).//
					put("phpnumber_register", bodies.size()).//
					put("m_old_order", m_old_order).//
					put("check", true)));//
		} else {
			bean.autoMsssage("102");
			log.error(bean.getError_ms());
		}
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 新建的資料
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			String now_order = body.getJSONObject("create").getString("m_now_order");
			String m_old_only_sn = body.getJSONObject("create").getString("m_old_only_sn");
			ProductionRecords phprid = new ProductionRecords();

			// 進行-特定查詢(重工工單)
			phprid.setPrid(now_order);
			prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A521_old_sn");

			// 檢查 u 製令單資料
			if (prArrayList.size() == 1) {
				ProductionHeader pro_h = prArrayList.get(0);
				List<ProductionBody> bodies = bodyDao.findAllByPbbsn(m_old_only_sn);
				// 檢查 此工單+SN 是否重複
				if (bodies.size() > 0) {
					resp.setError_ms("此序號[" + m_old_only_sn + "] 已使被使用,不新建SN產品序號 請[取消勾選] ");
					resp.autoMsssage("107");
					return false;
				}
				// 查詢 指定的SN
				bodies = new ArrayList<ProductionBody>();
				bodies = bodyDao.findAllByPbsn(m_old_only_sn);
				// 檢查 SN 是否u效 (有效不可覆蓋 -> 排除)
				if (bodies.size() >= 1) {
					return false;
				} else {
					// 工作站資訊
					JSONObject json_work = new JSONObject();
					ArrayList<WorkstationProgram> programs = programDao
							.findAllByWpgidAndSysheaderOrderBySyssortAsc(pro_h.getPhwpid(), false);
					for (WorkstationProgram p_one : programs) {
						ArrayList<Workstation> works = workDao
								.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
						JSONObject json_one = new JSONObject();
						json_one.put("name", works.get(0).getWpbname());
						json_one.put("type", works.get(0).getWcname() + "_N");
						json_one.put("id", works.get(0).getWid());
						json_one.put("w_pb_cell", works.get(0).getWpbcell());
						json_one.put("sort", p_one.getSyssort());
						json_work.put(works.get(0).getWcname(), json_one);
					}

					// 是否已經建過 群組
					Long id_b_g = pro_h.getPhpbgid();
					if (id_b_g == 1) {
						id_b_g = bodyDao.getProductionBodyGSeq();
						// 註冊到 重工 工單號
						pro_h.setPhpbgid(id_b_g);
						headerDao.save(pro_h);
					}

					// 登記 SN
					ProductionBody pro_b = new ProductionBody();
					pro_b.setSysver(0);
					pro_b.setPbgid(id_b_g);
					pro_b.setSysheader(false);
					pro_b.setPbsn(m_old_only_sn);
					pro_b.setPbbsn(m_old_only_sn);

					pro_b.setPbcheck(false);
					pro_b.setPbusefulsn(0);
					pro_b.setPbwyears(pro_h.getPhwyears());
					pro_b.setSysstatus(0);
					pro_b.setSyssort(0);
					pro_b.setPblpath("");
					pro_b.setPblsize("");
					pro_b.setPbltext("");
					pro_b.setPbschedule(json_work.toString());
					pro_b.setSysmuser(user.getSuaccount());
					pro_b.setSysmdate(new Date());

					bodyDao.save(pro_b);
				}
			}
			check = true;
		} catch (Exception e) {
			log.error(e.toString());
			System.out.println(e);
		}
		return check;
	}

	/**
	 * 更新資料清單：處理重工工單（A521_old_sn）的序號繼承與變更機制
	 * 
	 * @param resp 回傳結果封裝物件
	 * @param req  前端請求封裝物件
	 * @param user 當前登入的系統操作人員
	 * @return boolean 執行成功或失敗
	 */
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 預備工單標頭資料暫存器
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();

			// 取得當前操作動作 (單筆重工: order_only_btn / 批次重工: order_bat_btn)
			String action = body.getJSONObject("modify").getString("action");

			// ==========================================
			// 流程一：單筆序號轉移 (order_only_btn)
			// ==========================================
			if (action.equals("order_only_btn")) {
				ProductionRecords phprid = new ProductionRecords();
				String m_old_only_sn = body.getJSONObject("modify").getString("m_old_only_sn");
				String now_order = body.getJSONObject("modify").getString("m_now_order");

				// Step 0. 檢查該燒錄序號（SN）是否存在，並排除已註記為舊資料(_old)的紀錄
				List<ProductionBody> currentBodies = bodyDao.findAllByPbbsnAndPbbsnNotLike(m_old_only_sn, "_old");
				if (currentBodies.size() != 1) {
					resp.autoMsssage("102"); // 序號不存在或資料異常
					return false;
				}

				// 透過群組 ID (Pbgid) 尋找對應的製令單標頭 (Header)
				List<ProductionHeader> headers = headerDao.findAllByPhpbgid(currentBodies.get(0).getPbgid());
				if (!(headers.size() >= 1)) {
					return false;
				}
				// 取得該產品目前所屬的舊工單 ID
				String m_old_order = headers.get(0).getProductionRecords().getPrid();

				// Step 1. 查詢目標重工工單，且類型必須為 "A521_old_sn"
				phprid.setPrid(now_order);
				prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A521_old_sn");
				if (prArrayList.size() != 1) {
					return false; // 新工單無效或類型不符
				}

				// Step 2. 查詢該產品過去曾待過的舊工單資料
				phprid = new ProductionRecords();
				phprid.setPrid(m_old_order);
				prArrayList_old = headerDao.findAllByProductionRecords(phprid);
				if (prArrayList_old.size() != 1) {
					return false;
				}

				// Step 3. 檢查目標重工工單中，是否已經存在此序號（避免重複重工覆蓋）
				List<ProductionBody> targetBodiesCheck = bodyDao.findAllByPbbsnAndPbgid(m_old_only_sn,
						prArrayList.get(0).getPhpbgid());
				if (targetBodiesCheck.size() >= 1) {
					resp.setError_ms("此序號[" + m_old_only_sn + "] 已使用此 ["
							+ prArrayList.get(0).getProductionRecords().getPrid() + "] 工單");
					resp.autoMsssage("107");
					return false;
				}

				// Step 4. 取得該序號在舊工單的實體資料，準備進行複製與繼承
				List<ProductionBody> bodies_old = bodyDao.findAllByPbbsnAndPbgid(m_old_only_sn,
						prArrayList_old.get(0).getPhpbgid());
				if (bodies_old.size() != 1) {
					return false;
				}

				ProductionHeader pro_h = prArrayList.get(0);
				ProductionBody pro_b_one_old = bodies_old.get(0);

				// Step 5. 處理重工工單的群組識別碼 (Pbgid)。若為新群組(1)，則動態產生新 Sequence
				Long id_b_g = pro_h.getPhpbgid();
				if (id_b_g == 1) {
					id_b_g = bodyDao.getProductionBodyGSeq();
					pro_h.setPhpbgid(id_b_g);
					headerDao.save(pro_h);
				}

				// Step 6. 處理舊序號備份：將原序號後綴加上 _old_序號，保留軌跡
				JSONArray pboldsns = new JSONArray();
				if (pro_b_one_old.getPboldsn() != null && !pro_b_one_old.getPboldsn().equals("")) {
					pboldsns = new JSONArray(pro_b_one_old.getPboldsn());
				}
				String sn_new = pro_b_one_old.getPbbsn();
				String sn_old = pro_b_one_old.getPbbsn() + "_old_" + pboldsns.length();
				pboldsns.put(sn_old);

				if (pboldsns.length() == 1) { // 寫入開頭初始結構資訊
					pro_b_one_old.setPboldsn("" + new JSONArray().put(pro_b_one_old.getPbbsn() + "_old_beginning"));
				}
				pro_b_one_old.setPbbsn(sn_old);
				pro_b_one_old.setPbsn(sn_old);
				pro_b_one_old.setSysmuser(user.getSuaccount());
				pro_b_one_old.setSysmdate(new Date());
				bodyDao.save(pro_b_one_old); // 更新舊序號狀態

				// Step 7. 產生新工單的製程工作站 JSON 站別路徑
				JSONObject json_work = generateWorkstationSchedule(pro_h.getPhwpid());

				// Step 8. 複製並建立全新的 ProductionBody 資料實體
				ProductionBody pro_b_one = createNewProductionBody(pro_b_one_old, id_b_g, sn_new, pboldsns, pro_h,
						json_work, user);
				if (pro_b_one == null) {
					return false; // 透過 Reflection 複製動態欄位失敗
				}

				bodyDao.save(pro_b_one);
				check = true;
			}

			// ==========================================
			// 流程二：整批序號轉移 (order_bat_btn)
			// ==========================================
			if (action.equals("order_bat_btn")) {

				String m_old_bat_order = body.getJSONObject("modify").getString("m_old_bat_order");
				String m_now_bat_order = body.getJSONObject("modify").getString("m_now_order");

				ProductionRecords productionRecords_old = new ProductionRecords();
				productionRecords_old.setPrid(m_old_bat_order);
				ProductionRecords productionRecords_new = new ProductionRecords();
				productionRecords_new.setPrid(m_now_bat_order);

				// 驗證新舊工單標頭是否存在
				List<ProductionHeader> headers_old = headerDao.findAllByProductionRecords(productionRecords_old);
				List<ProductionHeader> headers_now = headerDao.findAllByProductionRecords(productionRecords_new);

				// 驗證限制：新舊工單必須存在，且限制計畫生產數量（Phpqty）必須完全一致才允許整批轉移
				if (!(headers_old.size() == 1 && headers_now.size() == 1
						&& headers_now.get(0).getPhpqty().equals(headers_old.get(0).getPhpqty()))) {
					return false;
				}

				ProductionHeader headers_new_one = headers_now.get(0);
				ProductionHeader headers_old_one = headers_old.get(0);

				// 嚴格檢查：新接收工單的類型必須為重工類型的 "A521_old_sn"
				if (!headers_new_one.getPhtype().equals("A521_old_sn")) {
					return false;
				}

				// 撈出舊工單下所有「未被標記為 _old」的有效產品序號清單
				List<ProductionBody> productionBodyOlds = bodyDao
						.findAllByPbgidAndPbbsnNotLike(headers_old_one.getPhpbgid(), "_old");
				if (!(headers_old.size() >= 1)) {
					resp.autoMsssage("102");
					return false;
				}

				// 效能優化：將新工單工作站 JSON 排程提到迴圈外，避免重複查詢資料庫
				JSONObject json_work = generateWorkstationSchedule(headers_new_one.getPhwpid());

				List<ProductionBody> pro_b_one_save = new ArrayList<ProductionBody>();

				// 開始逐筆遞迴轉移工單序號
				for (ProductionBody productionBodyOld : productionBodyOlds) {

					ProductionHeader pro_h = headers_new_one;
					ProductionBody pro_b_one_old = productionBodyOld;

					// 處理重工群組 ID 的綁定或全新宣告
					Long id_b_g = pro_h.getPhpbgid();
					if (id_b_g == 1) {
						id_b_g = bodyDao.getProductionBodyGSeq();
						pro_h.setPhpbgid(id_b_g);
						headerDao.save(pro_h);
					}

					// 舊序號名稱置換備份流程 (_old_1, _old_2...)
					JSONArray pboldsns = new JSONArray();
					if (pro_b_one_old.getPboldsn() != null && !pro_b_one_old.getPboldsn().equals("")) {
						pboldsns = new JSONArray(pro_b_one_old.getPboldsn());
					}
					String sn_new = pro_b_one_old.getPbbsn();
					String sn_old = pro_b_one_old.getPbbsn() + "_old_" + pboldsns.length();
					pboldsns.put(sn_old);

					if (pboldsns.length() == 1) {
						pro_b_one_old.setPboldsn("" + new JSONArray().put(pro_b_one_old.getPbbsn() + "_old_beginning"));
					}
					pro_b_one_old.setPbbsn(sn_old);
					pro_b_one_old.setPbsn(sn_old);
					pro_b_one_old.setSysmuser(user.getSuaccount());
					pro_b_one_old.setSysmdate(new Date());
					bodyDao.save(pro_b_one_old);

					// 呼叫輔助方法建立新 ProductionBody 實體並複製資料
					ProductionBody pro_b_one = createNewProductionBody(pro_b_one_old, id_b_g, sn_new, pboldsns, pro_h,
							json_work, user);
					if (pro_b_one == null) {
						return false;
					}

					pro_b_one_save.add(pro_b_one);
					check = true;
				}
				// 使用批次儲存，大幅降低資料庫 Transaction 消耗
				bodyDao.saveAll(pro_b_one_save);
			}

		} catch (Exception e) {
			System.out.println(e);
			log.error(e.toString());
			return false;
		}
		return check;
	}

	/**
	 * 輔助方法：產生新工單對應的工作站流程排程 JSON 格式
	 */
	private JSONObject generateWorkstationSchedule(Long phwpid) {
		JSONObject json_work = new JSONObject();
		ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(phwpid, false);
		for (WorkstationProgram p_one : programs) {
			ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
			JSONObject json_one = new JSONObject();
			json_one.put("name", works.get(0).getWpbname());
			json_one.put("type", works.get(0).getWcname() + "_N");
			json_one.put("id", works.get(0).getWid());
			json_one.put("w_pb_cell", works.get(0).getWpbcell());
			json_one.put("sort", p_one.getSyssort());
			json_work.put(works.get(0).getWcname(), json_one);
		}
		return json_work;
	}

	/**
	 * 輔助方法：建構全新的 ProductionBody 並透過 Reflection 動態填入過往 90 個客製化測試數值欄位
	 */
	private ProductionBody createNewProductionBody(ProductionBody oldBody, Long groupId, String newSn,
			JSONArray pboldsns, ProductionHeader header, JSONObject scheduleJson, SystemUser user) {

		ProductionBody newBody = new ProductionBody();

		// 基礎關鍵欄位複製與對接
		newBody.setPbgid(groupId);
		newBody.setPbbsn(newSn);
		newBody.setPbsn(newSn);
		newBody.setPboldsn(pboldsns.toString());

		newBody.setPbfnote(oldBody.getPbfnote());
		newBody.setPbfvalue(oldBody.getPbfvalue());

		newBody.setPbcheck(false); // 新重工件預設尚未檢驗
		newBody.setPbposition(oldBody.getPbposition());
		newBody.setPbwyears(header.getPhwyears()); // 帶入新工單的保固年份
		newBody.setPbschedule(scheduleJson.toString()); // 載入新排定的工作站

		newBody.setPblpath(oldBody.getPblpath());
		newBody.setPblpathoqc(oldBody.getPblpathoqc());
		newBody.setPblsize(oldBody.getPblsize());
		newBody.setPbltext(oldBody.getPbltext());
		newBody.setPbusefulsn(oldBody.getPbusefulsn());

		// 寫入系統共用追蹤欄位
		newBody.setSysstatus(0);
		newBody.setSyssort(0);
		newBody.setSyscuser(user.getSuaccount());
		newBody.setSyscdate(new Date());
		newBody.setSysmuser(user.getSuaccount());
		newBody.setSysmdate(new Date());
		newBody.setSysnote(oldBody.getSysnote());
		newBody.setSysheader(false);

		// 使用 Java Reflection 機制，安全地批次複製巨量對應欄位 (Pbvalue01~50, Pbwpdate01~20,
		// Pbwname01~20)
		try {
			// 1. 複製 50 個自訂參數值 (Pbvalue01 ~ Pbvalue50)
			for (int k = 0; k < 50; k++) {
				String fieldSuffix = String.format("%02d", k + 1);
				Method get_method = oldBody.getClass().getMethod("getPbvalue" + fieldSuffix);
				String value = (String) get_method.invoke(oldBody);

				Method set_method = newBody.getClass().getMethod("setPbvalue" + fieldSuffix, String.class);
				set_method.invoke(newBody, value);
			}
			// 2. 複製 20 個製程日期節點 (Pbwpdate01 ~ Pbwpdate20)
			for (int k = 0; k < 20; k++) {
				String fieldSuffix = String.format("%02d", k + 1);
				Method get_method = oldBody.getClass().getMethod("getPbwpdate" + fieldSuffix);
				Date value = (Date) get_method.invoke(oldBody);

				Method set_method = newBody.getClass().getMethod("setPbwpdate" + fieldSuffix, Date.class);
				set_method.invoke(newBody, value);
			}
			// 3. 複製 20 個過站作業員名稱 (Pbwname01 ~ Pbwname20)
			for (int k = 0; k < 20; k++) {
				String fieldSuffix = String.format("%02d", k + 1);
				Method get_method = oldBody.getClass().getMethod("getPbwname" + fieldSuffix);
				String value = (String) get_method.invoke(oldBody);

				Method set_method = newBody.getClass().getMethod("setPbwname" + fieldSuffix, String.class);
				set_method.invoke(newBody, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Reflection 複製動態測試參數欄位時發生異常: " + e.toString());
			return null;
		}

		return newBody;
	}

	/**
	 * 還原資料清單：當作業員登記錯誤時，執行序號還原（Rollback）歸還機制
	 * 
	 * @param resp 回傳結果封裝物件
	 * @param req  前端請求封裝物件
	 * @param user 當前登入的系統操作人員
	 * @return boolean 執行成功或失敗
	 */
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		// 預備工單標頭資料暫存器
		List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();

		// 取得當前還原動作 (單筆歸還: return_order_only_btn / 批次歸還: return_order_bat_btn)
		String action = body.getJSONObject("modify").getString("action");

		try {
			// ==========================================
			// 流程一：單筆序號還原歸還 (return_order_only_btn)
			// ==========================================
			if (action.equals("return_order_only_btn")) {
				String return_sn = body.getJSONObject("modify").getString("m_return_sn");

				// 撈出目前畫面上指定的有效新序號（排除已被更換為 %old% 的過往歷史紀錄）
				List<ProductionBody> now_sns = bodyDao.findAllByPbbsnAndPbbsnNotLike(return_sn, "%old%");

				// Step 0. 確定有找到要歸還的對象
				if (now_sns.size() == 1) {
					ProductionBody p_now = now_sns.get(0);

					// Step 1. 檢查目前序號綁定的製令單標頭
					prArrayList = headerDao.findAllByPhpbgid(p_now.getPbgid());
					if (prArrayList.size() == 1) {
						ProductionHeader header_now = prArrayList.get(0);

						// 呼叫封裝好的單筆還原核心方法
						check = processSingleRowRestore(p_now, header_now, user);
					}
				} else {
					// 補救配套：如果在有效區域找不到，則嘗試依序號直接移除該張單據建立的資料（防呆，且不能是 old 紀錄）
					List<ProductionBody> p_nows = bodyDao.findAllByPbbsnAndPbbsnNotLike(return_sn, "%old%");
					if (p_nows != null && p_nows.size() == 1) {
						log.info("Step6. 移除舊資料(不能是 old ):" + p_nows.get(0).getPbbsn());
						bodyDao.delete(p_nows.get(0));
						check = true;
					}
				}

				// ==========================================
				// 流程二：整批工單序號還原歸還 (return_order_bat_btn)
				// ==========================================
			} else if (action.equals("return_order_bat_btn")) {
				String m_return_order = body.getJSONObject("modify").getString("m_return_order");

				ProductionRecords productionRecords_new = new ProductionRecords();
				productionRecords_new.setPrid(m_return_order);

				// 1. 查詢目前準備被歸還的新重工工單標頭
				List<ProductionHeader> headers_now = headerDao.findAllByProductionRecords(productionRecords_new);
				if (!(headers_now.size() >= 1)) {
					return false;
				}
				ProductionHeader header_now = headers_now.get(0);

				// 2. 撈出該新工單群組下，目前處於有效狀態的新序號清單
				List<ProductionBody> productionBodyNews = bodyDao.findAllByPbgidAndPbbsnNotLike(header_now.getPhpbgid(),
						"%old%");

				// Step 0. 確定新工單內有找到可歸還的序號對象
				if (productionBodyNews.size() >= 1) {

					// ==========================================================
					// 【新增檢查】追蹤並驗證原始舊工單與目前新工單的生產數量是否一致
					// ==========================================================
					try {
						ProductionBody sampleBody = productionBodyNews.get(0);
						// 檢查該序號是否有舊歷史紀錄軌跡
						if (sampleBody.getPboldsn() != null && !sampleBody.getPboldsn().equals("")) {
							org.json.JSONArray re_old_sn_array = new org.json.JSONArray(sampleBody.getPboldsn());
							// 取得前一次被改名的舊序號名稱 (例如: SN123_old_0)
							String last_old_sn = re_old_sn_array.getString(re_old_sn_array.length() - 1);

							// 尋找這筆舊序號在資料庫中的實體
							List<ProductionBody> oldSnsCheck = bodyDao.findAllByPbbsn(last_old_sn);
							if (oldSnsCheck.size() == 1) {
								// 透過舊序號的群組識別碼 (Pbgid) 撈出舊工單標頭
								List<ProductionHeader> headers_old = headerDao
										.findAllByPhpbgid(oldSnsCheck.get(0).getPbgid());
								if (headers_old.size() >= 1) {
									ProductionHeader header_old = headers_old.get(0);

									// 嚴格比對：新舊工單的計畫生產數量 (Phpqty) 必須完全一致
									if (!header_now.getPhpqty().equals(header_old.getPhpqty())) {
										resp.setError_ms("批次還原失敗：新工單數量 [" + header_now.getPhpqty() + "] 與原舊工單數量 ["
												+ header_old.getPhpqty() + "] 不一致！");
										resp.autoMsssage("105"); // 可依您的系統代碼定義調整
										return false; // 數量不匹配，觸發事務 Rollback 安全拒絕
									}
								}
							}
						}
					} catch (Exception ex) {
						log.error("批次還原防呆機制 - 解析舊工單數量時發生異常: " + ex.toString());
						return false;
					}
					// ==========================================================

					// 驗證通過，遞迴逐筆為批次中的序號進行 Rollback 還原
					for (ProductionBody productionBody : productionBodyNews) {
						boolean singleResult = processSingleRowRestore(productionBody, header_now, user);
						if (singleResult) {
							check = true; // 只要有一筆成功還原，即調整狀態為 true
						}
					}

				} else {
					// 補救配套：若找不到任何新資料，則直接將該群組 ID 底下所有非 old 的殘留紀錄整批清除
					List<ProductionBody> p_nows = bodyDao.findAllByPbgidAndPbbsnNotLike(header_now.getPhpbgid(),
							"%old%");
					if (p_nows != null && p_nows.size() >= 1) {
						log.info("Step6. 批次強制移除無舊紀錄之新資料:" + p_nows.get(0).getPbbsn());
						bodyDao.deleteAll(p_nows);
						check = true;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			log.error(e.toString());
			return false;
		}
		return check;
	}

	/**
	 * 輔助方法：核心單筆還原與清除邏輯
	 * 
	 * @param p_now      當前新建立的生產序號實體
	 * @param header_now 當前序號所綁定的工單標頭實體
	 * @param user       當前操作人員
	 * @return boolean 單筆還原是否成功
	 */
	private boolean processSingleRowRestore(ProductionBody p_now, ProductionHeader header_now, SystemUser user) {
		boolean isSuccess = false;

		// 限定特定允許重工/綁定序號的工單類型才執行還原
		if (header_now.getPhtype().equals("A521_no_and_has_sn") || header_now.getPhtype().equals("A521_has_sn")
				|| header_now.getPhtype().equals("A521_old_sn")) {

			// Step 2. 檢查該新資料是否帶有繼承舊紀錄（歷史 SN 軌跡陣列）
			if (p_now.getPboldsn() != null && !p_now.getPboldsn().equals("")) {
				JSONArray re_old_sn = new JSONArray(p_now.getPboldsn());

				// 取出前一次更換時被重新命名的舊 SN（通常位於陣列最後一個位置，如：SN001_old_0）
				String old_sn = re_old_sn.getString(re_old_sn.length() - 1);

				// 前往資料庫核對並撈出這筆被改名的舊實體
				List<ProductionBody> old_sns = bodyDao.findAllByPbbsn(old_sn);
				if (old_sns.size() == 1) {

					// Step 3. 還原舊資料：回復原名（將後綴 _old_X 割除），讓舊資料起死回生
					ProductionBody p_old = old_sns.get(0);
					String originalSn = old_sn.split("_")[0];
					p_old.setPbbsn(originalSn);
					p_old.setPbsn(originalSn);

					// 如果這是唯一的/第一次的繼承，還原後就沒有更早的歷史了，直接清空歷史欄位
					if (re_old_sn.length() == 1) {
						p_old.setPboldsn("");
					}

					// Step 4. 處置與移除新資料
					// 特殊機制：若是帶序號或混合製令（A521_has_sn / A521_no_and_has_sn），保留實體，僅將自訂與測試狀態欄位清空洗掉
					if (header_now.getPhtype().equals("A521_no_and_has_sn")
							|| header_now.getPhtype().equals("A521_has_sn")) {

						ProductionBody p_now_clear = new ProductionBody();
						p_now_clear.setPbid(p_now.getPbid());
						p_now_clear.setSysver(0);
						p_now_clear.setPbgid(p_now.getPbgid());
						p_now_clear.setSysheader(false);
						p_now_clear.setPbsn(p_now.getPbsn());
						p_now_clear.setPbbsn(p_now.getPbbsn());
						p_now_clear.setPbcheck(false);
						p_now_clear.setPbusefulsn(0);
						p_now_clear.setPbwyears(p_now.getPbwyears());
						p_now_clear.setSysstatus(0);
						p_now_clear.setSyssort(p_now.getSyssort());
						p_now_clear.setPblpath("");
						p_now_clear.setPblsize("");
						p_now_clear.setPbltext("");
						p_now_clear.setPbschedule(p_now.getPbschedule());
						p_now_clear.setSysmuser(user.getSuaccount());
						p_now_clear.setSyscuser(user.getSuaccount());

						bodyDao.save(p_now_clear);
						log.info("Step4. 已清空特定工單類型之新資料狀態");
					} else {
						// 若是單純重工工單（A521_old_sn），還原後直接將這筆錯誤建立的新資料實體直接物理刪除
						bodyDao.delete(p_now);
						log.info("Step4. 已實體移除新資料: " + p_now.getPbbsn());
					}

					// 儲存已回復原名與狀態的舊資料實體
					bodyDao.save(p_old);
					isSuccess = true;
				}
			} else {
				// 沒有舊紀錄軌跡時的配套 -> 必須是不曾被繼承過的新序號，且僅在「無SN繼承(A511)」或「原號重工(A521)」下，直接移除該筆新創資料
				List<ProductionBody> p_nows = bodyDao.findAllByPbbsnAndPbbsnNotLike(p_now.getPbbsn(), "%old%");
				if (p_nows != null && p_nows.size() == 1 && (header_now.getPhtype().equals("A511_no_sn")
						|| header_now.getPhtype().equals("A521_old_sn"))) {
					log.info("Step5. 移除全新建立無歷史之新資料: " + p_nows.get(0).getPbbsn());
					bodyDao.delete(p_nows.get(0));
					isSuccess = true;
				}
			}
		}

		return isSuccess;
	}
}
