package dtri.com.tw.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.print.PrintService;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.FtpUtilBean;
import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.MaintainCode;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemConfig;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.MaintainCodeDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductionRecordsDao;
import dtri.com.tw.db.pgsql.dao.SystemConfigDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class WorkstationWorkService {
	@Autowired
	private ProductionHeaderDao phDao;
	@Autowired
	private ProductionRecordsDao prDao;
	@Autowired
	private ProductionBodyDao pbDao;
	@Autowired
	private WorkstationDao wkDao;
	@Autowired
	private WorkstationProgramDao wkpDao;
	@Autowired
	private MaintainCodeDao codeDao;

	@Autowired
	private FtpService ftpService;

	@Autowired
	private ForPrinterLabelService labelService;

	@Autowired
	private SystemConfigDao sysDao;

	@Autowired
	EntityManager em;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size, SystemUser user) {
		PackageBean bean = new PackageBean();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		String w_c_name = null;
		String ph_pr_id = null;
		String pb_b_sn = null;
		String pb_b_sn_old = null;
		Boolean set_replace = true;
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject(new LinkedHashMap<>());
			// 維修代碼fix
			ArrayList<MaintainCode> codes = codeDao.findAllByOrderByMcgidAscSysheaderDescMcvalueAsc();
			JSONObject fix_obj = new JSONObject();
			JSONObject fix_type = new JSONObject();
			// JSONObject fix_item = new JSONObject();
			Map<String, String> fix_item = new LinkedHashMap<String, String>();// 因為排序(一般JSONObject 是不排序)
			String mc_g_code = "", mc_g_name = "";

			for (MaintainCode one : codes) {
				// TYPE
				if (one.getSysheader()) {
					if (fix_item.size() > 0) {
						fix_type.put("name", mc_g_name);
						fix_type.put("item", new JSONObject(fix_item));
						fix_obj.put(mc_g_code, fix_type);
						fix_item = new LinkedHashMap<String, String>();
						fix_type = new JSONObject();
					}
					mc_g_name = one.getMcgname();
					mc_g_code = one.getMcvalue();
				} else {
					// ITEM
					fix_item.put(one.getMcvalue(), one.getMcname());
				}
			}
			// 補上最後一圈
			if (fix_item.size() > 0) {
				fix_type.put("name", mc_g_name);
				fix_type.put("item", new JSONObject(fix_item));
				fix_obj.put(mc_g_code, fix_type);
				fix_item = new LinkedHashMap<String, String>();
				fix_type = new JSONObject();
			}
			object_header.put("fix_list", fix_obj);
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			// doc
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "ph_s_date", "投線日"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "ph_type", "製令類"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "ph_pr_id", "製令單號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "ph_p_number", "Part No: "));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_p_name", "產品名(號)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_p_model", "產品型號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_bom_id", "BOM料號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_order_id", "訂單編號"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_c_name", "訂購客戶"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pr_p_quantity", "全部/完成(整體數量)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "wk_quantity", "本工作站(通過數量)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_workstation", "工作站(狀態)"));
			
			
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_dt", "PLT_Log上傳時間"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_size", "PLT_Log_Size"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pb_l_path", "PLT_Log位置"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pb_l_text", "PLT_Log內容"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pr_b_item", "規格定義"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pr_s_item", "軟體定義"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "sys_note", "備註"));
			object_header.put("doc_list", obj_m);
			// bean.setCell_modify(obj_m);

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

			// 進行-特定檢查
			w_c_name = body.getJSONObject("search").getString("w_c_name");
			w_c_name = w_c_name.equals("") ? null : w_c_name;
			ph_pr_id = body.getJSONObject("search").getString("ph_pr_id");
			ph_pr_id = ph_pr_id.equals("") ? null : ph_pr_id;
			pb_b_sn = body.getJSONObject("search").getString("pb_b_sn");
			pb_b_sn = pb_b_sn.equals("") ? null : pb_b_sn;
			pb_b_sn_old = body.getJSONObject("search").getString("pb_b_sn_old");
			pb_b_sn_old = pb_b_sn_old.equals("") ? null : pb_b_sn_old;

			// 工作站
			ArrayList<Workstation> w_one = wkDao.findAllByWcname(w_c_name, PageRequest.of(0, 1));
			ArrayList<WorkstationProgram> wp_all = new ArrayList<WorkstationProgram>();
			List<ProductionHeader> ph_all = new ArrayList<ProductionHeader>();
			List<ProductionBody> pb_all = new ArrayList<ProductionBody>();
			
			
			pb_all = pbDao.findAllByPbbsn(pb_b_sn);
			// 檢查資料是否存在
			if (pb_all.size() != 1) {
				bean.setBody(new JSONObject());
				bean.autoMsssage("WK003");
				return bean;
			}

			// Step2. 製令+工作站+SN關聯+Doc 檢查
			ProductionRecords records = new ProductionRecords();
			records.setPrid(ph_pr_id);
			List<Integer> sysstatus = new ArrayList<Integer>();
			// 狀態非(暫停/終止/完成)的資料
			sysstatus.add(2);
			sysstatus.add(8);
			sysstatus.add(9);
			ph_all = phDao.findAllByProductionRecordsAndSysstatusNotIn(records, sysstatus);
			// 比對-檢查製令
			if (ph_all.size() == 1) {
				// 比對-工作站
				if (w_one.size() == 1) {
					wp_all = wkpDao.findAllByWpgidAndWpwgidAndSysheaderOrderBySyssortAsc(ph_all.get(0).getPhwpid(), w_one.get(0).getWgid(), false);
					// 比對-檢查程序
					if (wp_all.size() == 1) {
						String wpcname = w_one.get(0).getWcname();
						// 比對-檢查 燒錄 SN關聯
						pb_all = pbDao.findAllByPbbsnAndPbgid(pb_b_sn, ph_all.get(0).getPhpbgid());
						if (pb_all.size() == 1) {
							// 計算 此工作站完成數
							List<ProductionBody> wk_schedules = pbDao.findAllByPbgidAndPbscheduleLikeOrderByPbsnAsc(pb_all.get(0).getPbgid(),
									"%" + wpcname + "_Y%");
							int all_nb = wk_schedules.size();
							String pb_old_sn = pb_all.get(0).getPboldsn() == null ? "" : pb_all.get(0).getPboldsn();
							// 如果是A521 有舊的SN (要排除已經繼承)
							if (pb_b_sn_old != null && pb_old_sn.indexOf(pb_b_sn_old) < 0) {
								pb_all = pbDao.findAllByPbbsnAndPbbsnNotLike(pb_b_sn_old, "_old");
								if (pb_all.size() != 1) {
									bean.setBody(new JSONObject());
									bean.autoMsssage("WK004_1");
									return bean;
								}
							}

							// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
							// doc
							ProductionBody pb_one = pb_all.get(0);
							JSONArray object_doc = new JSONArray();
							JSONArray object_sn = new JSONArray();
							JSONObject object_body_all = new JSONObject();
							//過站狀態
							JSONObject pb_workstation = new JSONObject(pb_one.getPbschedule()).getJSONObject(""+wpcname);
							String pb_w_pass = "<<尚未過站>>";
							if(pb_workstation.get("type").equals(wpcname+"_Y")) {
								pb_w_pass = ">>已經過站<<";
								set_replace = false;
							}
							String pb_w = pb_w_pass;
							ph_all.forEach(one -> {
								JSONObject object_body = new JSONObject();
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "ph_s_date",
										one.getPhsdate() == null ? "" : Fm_Time.to_yMd_Hms(one.getPhsdate()));

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "ph_type", one.getPhtype());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "ph_pr_id", one.getProductionRecords().getPrid());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "ph_p_number", one.getPhpnumber());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_p_name", one.getPhpname());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_p_model", one.getProductionRecords().getPrpmodel());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_bom_id", one.getProductionRecords().getPrbomid());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_order_id", one.getProductionRecords().getProrderid());

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_c_name", one.getProductionRecords().getPrcname());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_p_quantity",
										one.getProductionRecords().getPrpquantity() + "/" + one.getProductionRecords().getPrpokquantity());

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "wk_quantity", all_nb);
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_workstation", pb_w);

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_dt",
										pb_one.getPbldt() == null ? "" : Fm_Time.to_yMd_Hms(pb_one.getPbldt()));
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_size", pb_one.getPblsize());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_path", pb_one.getPblpath());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_text", pb_one.getPbltext());

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_b_item", one.getProductionRecords().getPrbitem());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_s_item", one.getProductionRecords().getPrsitem());
								object_doc.put(object_body);
							});
							object_body_all.put("search", object_doc);

							// sn_list
							ArrayList<Workstation> w_for_sn = new ArrayList<Workstation>();
							w_for_sn = wkDao.findAllByWgidAndSysheaderOrderBySyssortAsc(w_one.get(0).getWgid(), false);
							w_for_sn.forEach(w -> {
								// 是否顯示
								if (w.getWoption() == 0 || w.getWoption() == 2) {
									JSONObject object_body = new JSONObject();
									JSONObject object_work = new JSONObject();
									// 此工作站 項目
									object_work.put("name", w.getWorkstationItem().getWipbvalue());
									// 貸出已存入的 內容值
									String get_name = w.getWorkstationItem().getWipbcell().replace("pb_value", "getPbvalue");
									try {
										// 取出欄位名稱 ->存入body_title資料
										Method get_method = pb_one.getClass().getMethod(get_name);
										String value = (String) get_method.invoke(pb_one);

										object_work.put("value", value);
										object_work.put("woption", w.getWoption());
									} catch (NoSuchMethodException e) {
										e.printStackTrace();
									} catch (SecurityException e) {
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									} catch (InvocationTargetException e) {
										e.printStackTrace();
									}
									object_body.put(w.getWorkstationItem().getWipbcell(), object_work);
									object_sn.put(object_body);
								}
							});
							// [body]

							object_body_all.put("pb_fvalue", pb_all.get(0).getPbfvalue());
							object_body_all.put("sn_list", object_sn);
							object_body_all.put("workdatation_program_name", wp_all.get(0).getWpname());
							object_body_all.put("workdatation_sort", wp_all.get(0).getSyssort());
							object_body_all.put("workdatation_name", w_one.get(0).getWpbname());
							bean.setBody(object_body_all);

						} else {
							bean.setBody(new JSONObject());
							bean.autoMsssage("WK004");
							return bean;
						}

					} else {
						bean.setBody(new JSONObject());
						bean.autoMsssage("WK000");
						return bean;
					}
				} else {
					bean.setBody(new JSONObject());
					bean.autoMsssage("WK001");
					return bean;
				}

			} else {
				ph_all = phDao.findAllByProductionRecordsAndSysstatusIn(records, sysstatus);
				if (ph_all.size() == 1) {
					// 有此公單 但已結案/暫停/終止
					bean.setBody(new JSONObject());
					bean.autoMsssage("WK016");
					return bean;
				}
				// 查無公單
				bean.setBody(new JSONObject());
				bean.autoMsssage("WK002");
				return bean;
			}
			
			if (set_replace) {
				bean.autoMsssage("WK020");
			} else {
				bean.autoMsssage("WK021");
			}
		}
		return bean;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
//			JSONArray list = body.getJSONArray("create");
//			for (Object one : list) {
//				// 物件轉換
//				SystemConfig sys_c = new SystemConfig();
//				JSONObject data = (JSONObject) one;
//
//			}
//			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("save_as");
			// for (Object one : list) {
			// 物件轉換
			// SystemConfig sys_c = new SystemConfig();
			// JSONObject data = (JSONObject) one;
			// }
			// check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單
	@Transactional
	public PackageBean updateData(JSONObject body, SystemUser user) {
		PackageBean bean = new PackageBean();
		try {
			JSONObject list = body.getJSONObject("modify");
			boolean check_end = body.getBoolean("check_end");
			// PLT 設定
			boolean plt_check = body.getJSONObject("modify").has("plt_check")//
					? body.getJSONObject("modify").getBoolean("plt_check")//
					: false;
			boolean plt_save = body.getJSONObject("modify").has("plt_save")//
					? body.getJSONObject("modify").getBoolean("plt_save")//
					: false;
			boolean plt_file_classify = body.getJSONObject("modify").has("plt_file_classify")//
					? body.getJSONObject("modify").getBoolean("plt_file_classify")//
					: false;
			// System.out.println(list);

			// [檢核階段-初步] SN 燒錄必須要
			if (!list.get("pb_b_sn").equals("")) {
				List<ProductionBody> body_s = new ArrayList<ProductionBody>();
				List<ProductionBody> body_s_old = new ArrayList<ProductionBody>();
				body_s = pbDao.findAllByPbbsn(list.getString("pb_b_sn"));
				body_s_old = pbDao.findAllByPbbsnAndPbbsnNotLike(list.getString("pb_old_sn"), "%old%");

				// 更新 [ProductionBody] 開始
				if (body_s.size() == 1) {
					ProductionBody body_one_now = body_s.get(0);// 目前產品資料
					ProductionBody body_one_old = body_s_old.size() > 0 ? body_s_old.get(0) : null;// 舊產品資料
					ProductionBody title_body = pbDao.findAllByPbid(0l).get(0);// 目前產品 自訂義SN欄位
					JSONObject pbschedule = new JSONObject(body_s.get(0).getPbschedule());// 目前工作程序
					Map<String, JSONObject> body_map_now = new HashedMap<String, JSONObject>();// 自訂義 SN範圍+工作站+過站時間+需要更新資料
					Map<String, JSONObject> body_map_old = new HashedMap<String, JSONObject>();// 自訂義 SN範圍+工作站+過站時間+需要更新資料
					List<ProductionBody> check_sn = pbDao.findAllByPbgidOrderByPbsnAsc(body_one_now.getPbgid());// 與此 燒錄SN的產品 製令單 相關清單
					ArrayList<ProductionRecords> wpicheck_pr = prDao.findAllByPrid(list.getString("ph_pr_id"), PageRequest.of(0, 10));// 產品規格內容(檢驗用)
					Map<String, JSONObject> wpi_pr_map = new HashedMap<String, JSONObject>();// 轉換成檢查參數
					Map<String, JSONObject> wpi_pr_map_auto = new HashedMap<String, JSONObject>();// [PLT]轉換成檢查參數
					Map<String, JSONObject> wpi_pr_map_def = new HashedMap<String, JSONObject>();// [PLT]轉換成檢查參數(預設值)
					// Method set_method = null;
					Method get_method = null;
					boolean check_fn = true;// 是否完成此 品製成
					boolean f_code_check = true;// 是否需要 維修
					String f_code = "";// 維修代號
					String w_c_name = "";// 過站人
					Boolean set_replace = true;// 重複過站?true=重複過站/ false = 沒重複過站

					// ========Step0. 是否原先有故障代碼 ========
					if (!list.getString("pb_f_value").equals("") && body_one_now.getPbfvalue() != null && !body_one_now.getPbfvalue().equals("")) {
						bean.autoMsssage("WK019");
						return bean;
					}

					// ========Step1.A521 是否新舊(SN)繼承? ========
					if (list.getString("pb_old_sn") != null && !list.getString("pb_old_sn").equals("")) {
						// Step1-1.[檢核階段-初階] 是否需要登記舊的SN(不能找已經登記過的)
						if (body_s_old.size() == 1) {
							JSONArray old_sn = new JSONArray();
							List<ProductionHeader> ph_List_old = phDao.findAllByPhpbgid(body_one_old.getPbgid());
							// Step1-2.[檢核階段-進階] 有此產品SN + 不是自己
							if (ph_List_old.size() == 1 && !ph_List_old.get(0).getProductionRecords().getPrid().equals(list.getString("ph_pr_id"))) {
								// 已經有舊資料的話
								if (body_one_old.getPboldsn() != null && !body_one_old.getPboldsn().equals("")) {
									old_sn = new JSONArray(body_one_old.getPboldsn());
								} else {
									// 沒有自己建立(body_map_now)
									JSONArray body_map_old_sn = new JSONArray().put(list.getString("pb_old_sn") + "_old_beginning");
									body_map_old.put("setPboldsn", new JSONObject().put("value", body_map_old_sn.toString()).put("type", String.class));

								}
								// SN繼承修改 被繼承SN值
								JSONObject old_sn_list = new JSONObject().put("value", list.getString("pb_old_sn") + "_old_" + old_sn.length());
								body_map_old.put("setPbbsn", old_sn_list.put("type", String.class));
								body_map_old.put("setPbsn", old_sn_list.put("type", String.class));

								old_sn.put(list.getString("pb_old_sn") + "_old_" + old_sn.length());
								body_map_now.put("setPboldsn", new JSONObject().put("value", old_sn.toString()).put("type", String.class));
							} else {
								bean.autoMsssage("WK009");
								return bean;
							}

							// Step1-3.[登記-SN] 自訂義的SN範圍+需要繼承內容
							try {
								for (int k = 0; k < 50; k++) {
									String set_name = "setPbvalue" + String.format("%02d", k + 1);
									String get_name = "getPbvalue" + String.format("%02d", k + 1);
									// 取出欄位名稱 ->存入body_title資料
									get_method = body_one_old.getClass().getMethod(get_name);
									String body_value = (String) get_method.invoke(body_one_old);
									if (body_value == null) {
										body_value = "";
									}
									// set_method = body_one.getClass().getMethod(set_name, String.class);
									// set_method.invoke(body_one, body_value);
									body_map_now.put(set_name, new JSONObject().put("value", body_value).put("type", String.class));
								}
								body_map_now.put("setPblpath", new JSONObject().put("value", body_one_old.getPblpath()).put("type", String.class));
								body_map_now.put("setPblsize", new JSONObject().put("value", body_one_old.getPblsize()).put("type", String.class));
								body_map_now.put("setPbltext", new JSONObject().put("value", body_one_old.getPbltext()).put("type", String.class));
								body_map_now.put("setPbldt", new JSONObject().put(//
										"value", body_one_old.getPbldt() == null ? "isNull" : body_one_old.getPbldt()).put("type", Date.class));

								// body_one.setPblpath(body_one_old.getPblpath());
								// body_one.setPblsize(body_one_old.getPblsize());
								// body_one.setPbltext(body_one_old.getPbltext());
								// body_one.setPbldt(body_one_old.getPbldt());
							} catch (Exception e) {
								e.printStackTrace();
								bean.autoMsssage("1111");
								return bean;
							}
						} else {
							bean.autoMsssage("WK008");
							return bean;
						}
					}

					// ========Step2.[檢核階段-初步] ========
					// Step2-1.[檢核階段] 製令單有問題?
					if (check_sn.size() == 0) {
						bean.autoMsssage("WK003");
						return bean;
					}
					// Step2-2.[檢核階段-初步] 沒有此工作站?
					if (!pbschedule.has(list.getString("w_c_name"))) {
						bean.autoMsssage("WK001");
						return bean;
					}
					// Step2-3.[檢核階段-初步] 前置工作站 有沒有過站?
					Iterator<String> keys_schedule = pbschedule.keys();// 取得每一站 代號
					int sort_check = pbschedule.getJSONObject(list.getString("w_c_name")).getInt("sort"); // 取得此站 排序
					while (keys_schedule.hasNext()) {
						// 檢查前站別
						String key_schedule = keys_schedule.next();
						if (pbschedule.get(key_schedule) instanceof JSONObject) {
							String type = pbschedule.getJSONObject(key_schedule).getString("type");
							int sort = pbschedule.getJSONObject(key_schedule).getInt("sort");
							// 如果有 [前置站] 沒完成,跳錯誤 還別沒刷(除了自己)
							if (!key_schedule.equals(list.getString("w_c_name"))) {
								if (type.equals(key_schedule + "_N") && sort_check > sort) {
									bean.autoMsssage("WK005");
									return bean;
								}
								// 其中有一站 沒完成 =產品未生產完成
								if (type.equals(key_schedule + "_N")) {
									check_fn = false;
								}
							}
						}
					}

					// ========Step3.錯誤代碼 ->有則不通過 但可存檔========
					if (!list.has("pb_f_value") || list.getString("pb_f_value").equals("")) {
						w_c_name = list.getString("w_c_name") + "_Y";
					} else {
						w_c_name = list.getString("w_c_name") + "_N";
						f_code = new JSONObject().//
								put("workstation", w_c_name).//
								put("user", user.getSuaccount()).//
								put("fix_code", list.getString("pb_f_value")).toString();

						f_code_check = false;
					}
					// 重複過站[標記]
					if (pbschedule.getJSONObject(list.getString("w_c_name")).getString("type").equals(list.getString("w_c_name") + "_Y")) {
						set_replace = false;
					}

					pbschedule.put(list.getString("w_c_name"), pbschedule.getJSONObject(list.getString("w_c_name")).put("type", w_c_name));

					body_map_now.put("setPbschedule", new JSONObject().put("value", pbschedule.toString()).put("type", String.class));
					body_map_now.put("setPbfvalue", new JSONObject().put("value", f_code).put("type", String.class));
					body_map_now.put("setPbcheck", new JSONObject().put("value", check_fn).put("type", Boolean.class));

					// ========Step3-1.過站簽名 ->有維修代碼 則不存人========
					String w_pb_cell = pbschedule.getJSONObject(list.getString("w_c_name")).getString("w_pb_cell");
					String user_acc = user.getSuaccount();

					// 可能的工作站範圍
					String setPbwname = w_pb_cell.replace("pb_w_name", "setPbwname");
					String setPbwpdate = w_pb_cell.replace("pb_w_name", "setPbwpdate");

					// 有維修代碼? [true = 正常過站]/[false = 不過站 & 只存入部分資訊]
					if (f_code_check) {
						body_map_now.put(setPbwname, new JSONObject().put("value", user_acc).put("type", String.class));
						body_map_now.put(setPbwpdate, new JSONObject().put("value", new Date()).put("type", Date.class));
					} else {
						body_map_now.put(setPbwname, new JSONObject().put("value", "").put("type", String.class));
						body_map_now.put(setPbwpdate, new JSONObject().put("value", "isNull").put("type", Date.class));
					}

					// 有維修代碼則不進行其他行為
					// ========Step4.[檢核階段-進階]========
					if (f_code_check) {
						try {
							// Step4-1.[檢核階段-進階] 規格轉換
							if (wpicheck_pr.size() == 1) {
								String prbitem = wpicheck_pr.get(0).getPrbitem();
								// [檢核階段-進階] 避免規格異常
								if (prbitem != null && !prbitem.equals("")) {
									try {
										new JSONObject(prbitem);
									} catch (JSONException ex) {
										prbitem = new JSONObject().toString();
									}
								} else {
									prbitem = new JSONObject().toString();
								}
								JSONObject wpic_pr = new JSONObject(prbitem.replaceAll(" ", ""));
								Iterator<String> keys = wpic_pr.keys();
								while (keys.hasNext()) {
									String key = keys.next();
									if (wpic_pr.get(key) instanceof JSONObject) {
										// 格式 {數量:XX,是否需要檢查,true/false}
										wpi_pr_map.put(key, new JSONObject().put("Qty", wpic_pr.getJSONObject(key).getInt("Qty")).put("Check", false));
										wpi_pr_map_auto.put(key, new JSONObject().put("Qty", wpic_pr.getJSONObject(key).getInt("Qty")).put("Check", false));
										wpi_pr_map_def.put(key, new JSONObject().put("Qty", wpic_pr.getJSONObject(key).getInt("Qty")).put("Check", false));
									}
								}
							}

							// Step4-2.[檢核階段-初步] 有對應欄位?
							for (String cell_name : JSONObject.getNames(list)) {
								// 自訂義SN
								if (cell_name.indexOf("pb_value") != -1) {
									String body_value = list.getString(cell_name);
									String set_name = cell_name.replace("pb_value", "setPbvalue");
									String get_name = cell_name.replace("pb_value", "getPbvalue");
									get_method = title_body.getClass().getMethod(get_name);
									// set_method = body_one.getClass().getMethod(set_name, String.class);
									// 取得方法名稱->取出自定義 欄位名稱
									String title_name = (String) get_method.invoke(title_body);

									// Step4-3.[檢核階段-進階] 檢查 避免小卡 輸入主SN序號
									for (ProductionBody one_sn : check_sn) {
										if (one_sn.getPbbsn().equals(body_value)) {
											bean.autoMsssage("WK010");
											return bean;
										}
									}
									// 取得對應欄位[檢核條件]資料設定
									ArrayList<Workstation> check_only = wkDao.findAllByWorkstation_item(list.getString("w_c_name"), cell_name);
									if (check_only != null && check_only.size() > 0) {
										Workstation wk = check_only.get(0);

										// Step4-4.[檢核階段-進階] 唯讀-不存檔?
										if (wk.getWoption() == 2) {
											// System.out.println(title_name + " / " + body_value + " pass");
											continue;
										}

										// Step4-5.[檢核階段-進階] 需要檢查規格?
										if (wk.getWpicheck() == 1 || wk.getWpicheck() == 3) {
											// 需要檢查的數量
											if (wpi_pr_map.containsKey(wk.getWpiname())) {
												// 如果有值:則計算+標記 / 沒值:則標記
												int nb = wpi_pr_map.get(wk.getWpiname()).getInt("Qty");
												if (!body_value.equals("")) {
													nb = nb - 1;
												}
												wpi_pr_map.put(wk.getWpiname(), new JSONObject().put("Qty", nb).put("Check", true));
											}
										}

										// Step4-6.[檢核階段-進階] 必填欄位?
										if (wk.getWmust() == 1 && body_value.equals("")) {
											bean.setError_ms("此[" + title_name + "] SN: " + body_value + " 為[必填欄位] ");
											bean.autoMsssage("WK014");
											return bean;
										}

										// Step4-7.[檢核階段-進階] 不必要檢核空值
										if (!body_value.equals("")) {

											// Step4-8.[檢核階段-進階] 是否有要檢查 長度
											if (wk.getWlength() > 0 && body_value.length() != wk.getWlength()) {
												bean.setError_ms("此[" + title_name + "]SN: " + body_value + " 長度不正確,指定:[" + wk.getWlength() + "] 位數");
												bean.autoMsssage("WK014");
												return bean;
											}
											// Step4-9.[檢核階段-進階] 是否有要檢查 格式
											if (wk.getWformat() > 0) {
												String error = "";
												boolean check = false;
												switch (check_only.get(0).getWformat()) {
												case 1:
													check = body_value.matches("^[A-Z0-9]*$");
													error = "只能輸入[A-Z,0-9]";
													break;
												case 2:
													check = body_value.matches("^[A-Z]*$");
													error = "只能輸入[A-Z]";
													break;
												case 3:
													check = body_value.matches("^[0-9]*$");
													error = "只能輸入[0-9]";
													break;

												}
												if (!check) {
													bean.setError_ms("此[" + title_name + "] SN: [" + body_value + "] 格式錯誤, " + error + " ");
													bean.autoMsssage("WK014");
													return bean;
												}
											}
											// Step4-10.[檢核階段-進階] 檢查是否有需要 檢查重複
											if (wk.getWonly() == 1) {
												// 排除已經被替代的 + 排除自己 新的SN
												String nativeQuery = "SELECT b.pb_b_sn FROM production_body b where ";
												nativeQuery += "b." + cell_name + " = :pb_value and ";
												nativeQuery += "b." + cell_name + " != '' and ";
												nativeQuery += "b.pb_b_sn not like '%old%'";

												if (!list.getString("pb_b_sn").equals("")) {// 排除 自己
													nativeQuery += " and (b.pb_b_sn != :pb_b_sn) ";
												}

												if (!list.getString("pb_old_sn").equals("")) {// 排除 舊的SN
													nativeQuery += "and (b.pb_b_sn != :pb_old_sn) ";
												}
												Query query = em.createNativeQuery(nativeQuery);
												// 條件
												query.setParameter("pb_value", body_value);
												if (!list.getString("pb_b_sn").equals("")) {
													query.setParameter("pb_b_sn", list.getString("pb_b_sn"));
												}
												if (!list.getString("pb_old_sn").equals("")) {
													query.setParameter("pb_old_sn", list.getString("pb_old_sn"));
												}

												@SuppressWarnings("unchecked")
												List<String> pbid_obj = query.getResultList();
												if (pbid_obj.size() > 0) {// 有重複
													bean.setError_ms(
															"此[" + title_name + " SN]: " + body_value + " , 已經被[產品/燒錄 SN]: " + pbid_obj.get(0) + " 使用中 ");
													bean.autoMsssage("WK011");
													return bean;
												}
											}
										}
									}
									body_map_now.put(set_name, new JSONObject().put("value", body_value).put("type", String.class));
									// set_method.invoke(body_one, body_value);
								} else {

								}
							}

							// Step4-11.[檢核階段-進階] 檢查規格結果
							System.out.println(wpi_pr_map);
							boolean wpi_check_fail = false;
							for (Entry<String, JSONObject> entry : wpi_pr_map.entrySet()) {
								if (entry.getValue().getBoolean("Check") && entry.getValue().getInt("Qty") != 0) {
									// 相差值
									if (entry.getValue().getInt("Qty") > 0) {
										bean.setError_ms("此規格[" + entry.getKey() + "]數量少登記: " + Math.abs(entry.getValue().getInt("Qty")) + " 請檢查!");
									} else {
										bean.setError_ms("此規格[" + entry.getKey() + "]數量多登記: " + Math.abs(entry.getValue().getInt("Qty")) + " 請檢查!");
									}
									wpi_check_fail = true;
									break;
								}
							}
							if (wpi_check_fail) {
								bean.autoMsssage("WK017");
								return bean;
							}

						} catch (NoSuchMethodException e) {
							e.printStackTrace();
							bean.autoMsssage("1111");
							return bean;
						}

						// ======== Step5. FTP檢查[] ========
						JSONObject list_log = new JSONObject();
						if (plt_check) {
							ArrayList<SystemConfig> config = sysDao.findAllByConfig(null, "FTP_PLT", 0, PageRequest.of(0, 99));
							JSONObject c_json = new JSONObject();
							config.forEach(c -> {
								c_json.put(c.getScname(), c.getScvalue());
							});
							Integer year = Year.now().getValue();
							String ftpHost = c_json.getString("IP"), //
									ftpUserName = c_json.getString("ACCOUNT"), //
									ftpPassword = c_json.getString("PASSWORD"), //
									remotePath = c_json.getString("PATH") + year, //
									remotePathBackup = c_json.getString("PATH_BACKUP"), //
									localPath = c_json.getString("LOCAL_PATH");//
							int ftpPort = c_json.getInt("PORT");
							String[] searchName = { list.getString("ph_pr_id"), "", list.getString("pb_b_sn") };
							FtpUtilBean ftp = new FtpUtilBean(ftpHost, ftpUserName, ftpPassword, ftpPort);
							ftp.setRemotePath(remotePath);
							ftp.setRemotePathBackup(remotePathBackup);
							ftp.setLocalPath(localPath);
							FTPClient ftpClient = new FTPClient();
							list_log = ftpService.getLogPLT(ftpClient, ftp, user.getSuaccount(), searchName, plt_file_classify);
							// Step5-1. FTP檢查[] SIZE
							if (list_log.length() < 1 || !list_log.has("pb_l_size") || list_log.getInt("pb_l_size") < 10) {
								bean.autoMsssage("WK007");
								return bean;
							}

							// ======== Step6. 需要Log 更新資料?? ========
							// (檢查是否有LOG+內容是否正確)
							if (list_log != null && list_log.length() > 0 && list_log.has("pb_l_size")) {
								try {
									// 是否存檔
									if (plt_save) {
										// 檢查所有可能對應的欄位
										Iterator<String> keys_log = list_log.keys();
										while (keys_log.hasNext()) {
											String cell_key = keys_log.next();
											// sn關聯表
											int j = 0;
											for (j = 0; j < 50; j++) {
												String get_name = "getPbvalue" + String.format("%02d", j + 1);
												String set_name = "setPbvalue" + String.format("%02d", j + 1);
												String cell_name = "pb_value" + String.format("%02d", j + 1);
												// Step6-1. 取得方法名稱->取出自定義 欄位名稱
												// set_method = body_one.getClass().getMethod(set_name, String.class);
												get_method = title_body.getClass().getMethod(get_name);
												String title_name = (String) get_method.invoke(title_body);

												// Step6-2. [PLT欄位名稱] 對應 [MES自定義SN欄位名稱]
												if (title_name != null && title_name.equals(cell_key)) {
													String body_value = list_log.getString(cell_key);

													ArrayList<Workstation> check_only = wkDao.findAllByWorkstation_item(list.getString("w_c_name"), cell_name);
													// Step6-3. [檢核階段-進階] 自定義檢核 是否需要檢核
													if (check_only != null && check_only.size() > 0) {
														Workstation wk = check_only.get(0);

														// Step6-4.[檢核階段-進階] 需要檢查產品?
														if (wk.getWpicheck() == 2 || wk.getWpicheck() == 3) {
															// 需要檢查的數量
															if (wpi_pr_map_auto.containsKey(wk.getWpiname())) {
																// 如果有值:則計算+標記 / 沒值:則標記
																int nb = wpi_pr_map_auto.get(wk.getWpiname()).getInt("Qty");
																if (!body_value.equals("")) {
																	nb = nb - 1;
																}
																wpi_pr_map_auto.put(wk.getWpiname(), new JSONObject().put("Qty", nb).put("Check", true));
															}
														}

														// Step6-5.[檢核階段-進階] 必填欄位?
														if (wk.getWmust() == 1 && body_value.equals("")) {
															bean.setError_ms("此 PLT_LOG[" + title_name + "] SN: " + body_value + " 為[必填欄位] ");
															bean.autoMsssage("WK014");
															return bean;
														}

														// Step6-6.[檢核階段-進階] 不必要檢核空值
														if (!body_value.equals("")) {

															// Step6-7.[檢核階段-進階] 是否有要檢查 長度
															if (wk.getWlength() > 0 && body_value.length() != wk.getWlength()) {
																bean.setError_ms("此 PLT_LOG[" + title_name + "]SN: " + body_value + " 長度不正確,指定:["
																		+ wk.getWlength() + "] 位數");
																bean.autoMsssage("WK014");
																return bean;
															}
															// Step6-8.[檢核階段-進階] 是否有要檢查 格式
															if (wk.getWformat() > 0) {
																String error = "";
																boolean check = false;
																switch (check_only.get(0).getWformat()) {
																case 1:
																	check = body_value.matches("^[A-Z0-9]*$");
																	error = "只能輸入[A-Z,0-9]";
																	break;
																case 2:
																	check = body_value.matches("^[A-Z]*$");
																	error = "只能輸入[A-Z]";
																	break;
																case 3:
																	check = body_value.matches("^[0-9]*$");
																	error = "只能輸入[0-9]";
																	break;
																}
																if (!check) {
																	bean.setError_ms(
																			"此 PLT_LOG[" + title_name + "] SN: [" + body_value + "] 格式錯誤, " + error + " ");
																	bean.autoMsssage("WK014");
																	return bean;
																}
															}
															// Step6-9.[檢核階段-進階] 檢查是否有需要 檢查重複
															if (wk.getWonly() == 1) {
																// 排除已經被替代的 + 排除自己 新的SN
																String nativeQuery = "SELECT b.pb_b_sn FROM production_body b where ";
																nativeQuery += "b." + cell_name + " = :pb_value and ";
																nativeQuery += "b." + cell_name + " != '' and ";
																nativeQuery += "b.pb_b_sn not like '%old%'";

																if (!list.getString("pb_b_sn").equals("")) {// 排除 自己
																	nativeQuery += " and (b.pb_b_sn != :pb_b_sn) ";
																}

																if (!list.getString("pb_old_sn").equals("")) {// 排除 舊的SN
																	nativeQuery += "and (b.pb_b_sn != :pb_old_sn) ";
																}
																Query query = em.createNativeQuery(nativeQuery);
																// 條件
																query.setParameter("pb_value", body_value);
																if (!list.getString("pb_b_sn").equals("")) {
																	query.setParameter("pb_b_sn", list.getString("pb_b_sn"));
																}
																if (!list.getString("pb_old_sn").equals("")) {
																	query.setParameter("pb_old_sn", list.getString("pb_old_sn"));
																}

																@SuppressWarnings("unchecked")
																List<String> pbid_obj = query.getResultList();
																if (pbid_obj.size() > 0) {// 有重複
																	bean.setError_ms("此 PLT_LOG [" + title_name + " SN]: " + body_value + " , 已經被[產品/燒錄 SN]: "
																			+ pbid_obj.get(0) + " 使用中 ");
																	bean.autoMsssage("WK011");
																	return bean;
																}
															}
														}
													}

													// set_method.invoke(body_one, body_value);
													body_map_now.put(set_name, new JSONObject().put("value", body_value).put("type", String.class));
												}
											}
										}

										// Step6-11.[檢核階段-進階] 檢查規格結果
										// System.out.println(wpi_pr_map);
										boolean wpi_check_fail = false;
										for (Entry<String, JSONObject> entry : wpi_pr_map_auto.entrySet()) {
											if (entry.getValue().getBoolean("Check") && entry.getValue().getInt("Qty") != 0) {
												// 相差值
												if (entry.getValue().getInt("Qty") > 0) {
													bean.setError_ms("此規格[" + entry.getKey() + "]數量少登記: " + Math.abs(entry.getValue().getInt("Qty")) + " 請檢查!");
												} else {
													bean.setError_ms("此規格[" + entry.getKey() + "]數量多登記: " + Math.abs(entry.getValue().getInt("Qty")) + " 請檢查!");
												}
												wpi_check_fail = true;
												break;
											}
										}
										if (wpi_check_fail) {
											bean.autoMsssage("WK018");
											return bean;
										}
										// Step6-12.[檢核階段-進階] PLT 亂碼檢核
										// System.out.print(list_log.getString("pb_l_text").indexOf('\u0000'));
										if (list_log.getString("pb_l_text").indexOf('\u0000') != -1) {
											bean.autoMsssage("WK015");
											return bean;
										}
										body_map_now.put("setPblpath",
												new JSONObject().put("value", list_log.getString("pb_l_path")).put("type", String.class));
										body_map_now.put("setPblsize",
												new JSONObject().put("value", list_log.getInt("pb_l_size") + "").put("type", String.class));
										body_map_now.put("setPbltext",
												new JSONObject().put("value", list_log.getString("pb_l_text")).put("type", String.class));
										body_map_now.put("setPbldt",
												new JSONObject().put("value", Fm_Time.toDateTime(list_log.getString("pb_l_dt"))).put("type", Date.class));

//								body_one.setPbltext(list_log.getString("pb_l_text"));
//								body_one.setPblpath(list_log.getString("pb_l_path"));
//								body_one.setPbldt(Fm_Time.toDateTime(list_log.getString("pb_l_dt")));
//								body_one.setPblsize(list_log.getInt("pb_l_size") + "");
									}
								} catch (Exception e) {
									e.printStackTrace();
									bean.autoMsssage("1111");
									return bean;
								}
							}

							// ======== Step7. 檔案歸檔?========
							if (list_log.getJSONArray("pb_l_files").length() > 0) {
								Boolean check_move = ftpService.logPLT_Archive(ftpClient, ftp, list_log.getJSONArray("pb_l_files"));
								if (!check_move) {
									bean.autoMsssage("WK012");
									return bean;
								}
							}
						}
					}

					// ======== Step8.其他參數紀錄 ========
					body_map_now.put("setSysmdate", new JSONObject().put("value", new Date()).put("type", Date.class));
					body_map_now.put("setSysmuser", new JSONObject().put("value", user.getSuaccount()).put("type", String.class));
					body_map_now.put("setPbposition", new JSONObject().put("value", list.getString("pb_position")).put("type", String.class));

					// 更新 [ProductionBody_old] 結束-> 登記入 body_one forEach
					for (Entry<String, JSONObject> entry : body_map_old.entrySet()) {
						System.out.println("key:" + entry.getKey());
						System.out.println("value:" + entry.getValue());
						JSONObject json_v = entry.getValue();
						try {
							Method set_method = body_one_old.getClass().getMethod(entry.getKey(), (Class<?>) json_v.get("type"));
							// 如果有Null
							if (json_v.get("value") instanceof String && json_v.getString("value").equals("isNull")) {
								System.out.println("value:" + entry.getValue());
								set_method.invoke(body_one_old, new Object[] { null });
							} else {
								set_method.invoke(body_one_old, json_v.get("value"));
							}

						} catch (Exception e) {
							e.printStackTrace();
							bean.autoMsssage("1111");
							return bean;
						}
					}

					// 更新 [ProductionBody_now] 結束-> 登記入 body_one forEach
					for (Entry<String, JSONObject> entry : body_map_now.entrySet()) {
						System.out.println("key:" + entry.getKey());
						System.out.println("value:" + entry.getValue());
						JSONObject json_v = entry.getValue();

						try {
							Method set_method = body_one_now.getClass().getMethod(entry.getKey(), (Class<?>) json_v.get("type"));
							// 如果有Null
							if (json_v.get("value") instanceof String && json_v.getString("value").equals("isNull")) {
								System.out.println("value:" + entry.getValue());
								set_method.invoke(body_one_now, new Object[] { null });
							} else {
								set_method.invoke(body_one_now, json_v.get("value"));
							}

						} catch (Exception e) {
							e.printStackTrace();
							bean.autoMsssage("1111");
							return bean;
						}
					}

					// ======== Step9. 製令單+規格更新[ProductionRecords]========
					ProductionRecords phprid = new ProductionRecords();
					phprid.setPrid(list.getString("ph_pr_id"));
					ProductionHeader p_header = phDao.findAllByProductionRecords(phprid).get(0);
					// 啟動時間
					if (p_header.getSysstatus() == 0) {
						p_header.setPhsdate(new Date());
					}
					// 關聯SN 計算完成數量
					List<ProductionBody> p_body = pbDao.findAllByPbgidOrderByPbsnAsc(p_header.getPhpbgid());
					int finish = 0;
					// 完成?
					for (ProductionBody productionBody : p_body) {
						if (productionBody.getPbcheck()) {
							finish += 1;
						}
					}
					// 規格
					ProductionRecords p_records = p_header.getProductionRecords();
					p_records.setPrpokquantity(finish);
					p_header.setSysstatus(1);
					p_header.setPhschedule(p_records.getPrpokquantity() + "／" + p_records.getPrpquantity());
					p_header.setProductionRecords(p_records);
					p_header.setSysmdate(new Date());
					// 更新工作站 (更新數量)/(避免先前[舊版本]有沒過站內容)
					ArrayList<WorkstationProgram> programs = wkpDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(p_header.getPhwpid(), false);
					List<ProductionBody> wk_schedules = pbDao.findAllByPbgidAndPbscheduleLikeOrderByPbsnAsc(p_header.getPhpbgid(),
							"%" + list.getString("w_c_name") + "_Y%");

					if (p_header.getPhpbschedule() != null && !p_header.getPhpbschedule().equals("")) {
						JSONObject phpbs = new JSONObject(p_header.getPhpbschedule());
						phpbs.put(list.getString("w_c_name"), wk_schedules.size());
						p_header.setPhpbschedule(phpbs.toString());
					} else {
						// 補充建立
						JSONObject json_ph_pb_schedule = new JSONObject();
						// 建立結構
						for (WorkstationProgram p_one : programs) {
							ArrayList<Workstation> works = wkDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
							// 計算 此工作站完成數
							wk_schedules = pbDao.findAllByPbgidAndPbscheduleLikeOrderByPbsnAsc(p_header.getPhpbgid(), "%" + works.get(0).getWcname() + "_Y%");
							json_ph_pb_schedule.put(works.get(0).getWcname(), wk_schedules.size());
						}
						p_header.setPhpbschedule(json_ph_pb_schedule.toString());
					}

					// 此製令已完成(完成數/目標數)量
					int f_nb = p_records.getPrpokquantity();
					int e_nb = p_records.getPrpquantity();
					if (check_end && (f_nb == e_nb)) {
						p_header.setSysstatus(2);
						p_header.setPhedate(new Date());
					}
					phDao.save(p_header);
					pbDao.save(body_one_now);
					pbDao.save(body_one_old);
					// true = 一般過站/false = 重複過站 = ?
					if (set_replace) {
						bean.autoMsssage("WK020");
					} else {
						bean.autoMsssage("WK021");
					}
				} else {
					bean.autoMsssage("WK003");
					return bean;
				}
			}
		} catch (NullPointerException e) {
			System.out.println(e);
			bean.autoMsssage("WK013");
			return bean;
		} catch (Exception e) {
			System.out.println(e);
			bean.autoMsssage("1111");
			return bean;
		}

		// ========Step0. 是否有過站更新成功 ========

		return bean;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(JSONObject body) {

		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("delete");
//			for (Object one : list) {
//				// 物件轉換
//				SystemConfig sys_p = new SystemConfig();
//				JSONObject data = (JSONObject) one;
//				sys_p.setScid(data.getInt("sc_id"));
//
//				// configDao.deleteByScidAndSysheader(sys_p.getScid(), false);
//				check = true;
//			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 傳送列印指令 資料清單
	@Transactional
	public boolean printerData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			JSONObject printer = body.getJSONObject("printer");
			if (printer.has("printer_3X1_code")) {
				PrintService ps = labelService.getPrinterService(printer.getString("printer_3X1_code"));
				if (ps != null) {
					String zpl = labelService.label_3X1(printer.getJSONArray("label_3X1_list"), printer.getInt("label_3X1_nb"));
					labelService.sendPrinter(zpl, ps);
					check = true;
				}
			}
			if (printer.has("printer_3X2_code")) {
				PrintService ps = labelService.getPrinterService(printer.getString("printer_3X2_code"));
				if (ps != null) {
					String zpl = labelService.label_3X2(printer.getJSONObject("label_3X2_list"), printer.getInt("label_3X2_nb"));
					labelService.sendPrinter(zpl, ps);
					check = true;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}
}
