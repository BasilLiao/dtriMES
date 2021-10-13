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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.print.PrintService;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
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
import dtri.com.tw.db.pgsql.dao.SystemConfigDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class WorkstationWorkService {
	@Autowired
	private ProductionHeaderDao phDao;
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

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject(new LinkedHashMap<>());
			// 維修代碼fix
			List<Long> mc_g_id = new ArrayList<Long>();
			ArrayList<MaintainCode> codes = codeDao.findAllByMaintainCode(null, null, mc_g_id, 0);
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

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "sys_status", "狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_size", "PLT_Log_Size"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pb_l_path", "PLT_Log位置"));
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

							// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
							// doc
							ProductionBody pb_one = pb_all.get(0);
							JSONArray object_doc = new JSONArray();
							JSONArray object_sn = new JSONArray();
							JSONObject object_body_all = new JSONObject();
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

								// 計算 此工作站完成數
								List<ProductionBody> wk_schedules = pbDao.findAllByPbgidAndPbscheduleLikeOrderByPbsnAsc(pb_one.getPbgid(),
										"%" + wpcname + "_Y%");
								int all_nb = wk_schedules.size();

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "wk_quantity", all_nb);

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "sys_status", one.getSysstatus());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_size", pb_one.getPblsize());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_path", pb_one.getPblpath());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pb_l_text", pb_one.getPbltext());

								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_b_item", one.getProductionRecords().getPrbitem());
								object_body.put(FFM.choose(FFM.Hmb.M.toString()) + "pr_s_item", one.getProductionRecords().getPrsitem());
								object_doc.put(object_body);
							});
							object_body_all.put("search", object_doc);

							// SN_list
							ArrayList<Workstation> w_for_sn = wkDao.findAllByWgidAndSysheaderOrderBySyssortAsc(w_one.get(0).getWgid(), false);
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
				bean.setBody(new JSONObject());
				bean.autoMsssage("WK002");
				return bean;
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
			System.out.println(list);

			// Step0.查詢SN關聯
			if (!list.get("pb_b_sn").equals("")) {
				List<ProductionBody> body_s = new ArrayList<ProductionBody>();
				body_s = pbDao.findAllByPbbsn(list.getString("pb_b_sn"));

				// 更新[ProductionBody]
				if (body_s.size() == 1) {
					ProductionBody body_one = body_s.get(0);
					List<ProductionBody> check_sn = pbDao.findAllByPbgidOrderByPbsnAsc(body_one.getPbgid());
					if (check_sn.size() == 0) {
						// 製令單有問題
						bean.autoMsssage("WK003");
						return bean;
					}
					try {
						// ========Step1.工作站->工作站欄位名稱========
						JSONObject pbschedule = new JSONObject(body_s.get(0).getPbschedule());
						// 沒有此工作站
						if (!pbschedule.has(list.getString("w_c_name"))) {
							bean.autoMsssage("WK001");
							return bean;
						}

						// Step1-1.工作站進度
						Iterator<String> keys = pbschedule.keys();
						int sort_check = pbschedule.getJSONObject(list.getString("w_c_name")).getInt("sort");
						boolean check_fn = true;
						while (keys.hasNext()) {
							// 檢查前站別
							String key = keys.next();
							if (pbschedule.get(key) instanceof JSONObject) {
								String type = pbschedule.getJSONObject(key).getString("type");
								int sort = pbschedule.getJSONObject(key).getInt("sort");
								// 前置作業站別沒刷(除了自己)
								if (!key.equals(list.getString("w_c_name"))) {
									if (type.equals(key + "_N") && sort_check > sort) {
										bean.autoMsssage("WK005");
										return bean;
									}
									// 每一站都刷完了
									if (type.equals(key + "_N")) {
										check_fn = false;
									}
								}
							}
						}
						ProductionBody title_body = new ProductionBody();
						title_body = pbDao.findAllByPbid(0l).get(0);
						// 可能的SN範圍
						for (int k = 0; k < 50; k++) {
							// 有欄位?
							if (list.has("pb_value" + String.format("%02d", k + 1))) {
								String body_value = list.getString("pb_value" + String.format("%02d", k + 1));
								String set_name = "setPbvalue" + String.format("%02d", k + 1);
								String get_name = "getPbvalue" + String.format("%02d", k + 1);
								String cell_name = "pb_value" + String.format("%02d", k + 1);

								Method get_method = title_body.getClass().getMethod(get_name);
								String title_value = (String) get_method.invoke(title_body);
								Method in_method = body_one.getClass().getMethod(set_name, String.class);
								// 欄位有值
								if (body_value != null && !body_value.equals("")) {
									// 檢查 避免小卡 輸入主SN序號
									for (ProductionBody one_sn : check_sn) {
										if (one_sn.getPbbsn().equals(body_value)) {
											bean.autoMsssage("WK010");
											return bean;
										}
									}
									
									ArrayList<Workstation> check_only = wkDao.findAllByWorkstation_item(list.getString("w_c_name"), cell_name);
									//唯讀-不存檔
									if(check_only.size() > 0 && check_only.get(0).getWoption() == 2) {
										System.out.println(title_value+" / "+body_value +" pass");
										continue;
									}
									// 檢查是否有需要 檢查重複
									if (check_only != null && check_only.size() > 0 && check_only.get(0).getWonly() == 1) {
										String nativeQuery = "SELECT b.pb_b_sn FROM production_body b ";
										nativeQuery += "where ";
										nativeQuery += "b." + cell_name + " = :pb_value ";
										nativeQuery += " and b." + cell_name + " != '' ";
										nativeQuery += " and b.pb_b_sn not like '%old%'"; // 排除已經被替代的
										if (!list.getString("pb_b_sn").equals("")) {// 排除自己 新的SN
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
										List<String> pbid_obj = query.getResultList();
										if (pbid_obj.size() > 0) {// 有重複
											bean.setError_ms("此[" + title_value + " SN]: " + body_value + " , "//
													+ "已經被[產品/燒錄 SN]: " + pbid_obj.get(0) + " 使用中 ");
											bean.autoMsssage("WK011");
											return bean;
										}
									}
								
									in_method.invoke(body_one, body_value);
								}
							}
						}
						// ========Step2.錯誤代碼 ->有則不通過 但可存檔========
						boolean f_code_check = true;
						String w_c_name = "";
						String f_code = "";
						if (!list.has("pb_f_value") || list.getString("pb_f_value").equals("")) {
							w_c_name = list.getString("w_c_name") + "_Y";
						} else {
							w_c_name = list.getString("w_c_name") + "_N";
							f_code = w_c_name + "_" + user.getSuaccount() + "_" + list.getString("pb_f_value");
							f_code_check = false;
						}
						pbschedule.put(list.getString("w_c_name"), pbschedule.getJSONObject(list.getString("w_c_name")).put("type", w_c_name));
						body_one.setPbschedule(pbschedule.toString());
						body_one.setPbfvalue(f_code);
						body_one.setPbposition(list.getString("pb_position"));
						body_one.setPbcheck(check_fn);

						// ========Step3.過站簽名 ->有維修代碼 則不存人========
						String w_pb_cell = pbschedule.getJSONObject(list.getString("w_c_name")).getString("w_pb_cell");
						String user_acc = user.getSuaccount();
						// 可能的工作站範圍
						for (int k = 0; k < 20; k++) {
							// 有欄位?
							if (w_pb_cell.equals("pb_w_name" + String.format("%02d", k + 1))) {

								String in_name = "setPbwname" + String.format("%02d", k + 1);
								Method in_method = body_one.getClass().getMethod(in_name, String.class);

								// 有維修代碼
								if (f_code_check) {
									in_method.invoke(body_one, user_acc);
								} else {
									in_method.invoke(body_one, "");
								}

							}
						}
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
						bean.autoMsssage("1111");
						return bean;
					} catch (SecurityException e) {
						e.printStackTrace();
						bean.autoMsssage("1111");
						return bean;
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						bean.autoMsssage("1111");
						return bean;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						bean.autoMsssage("1111");
						return bean;
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						bean.autoMsssage("1111");
						return bean;
					}
					body_one.setSysmdate(new Date());
					body_one.setSysmuser(user.getSuaccount());

					// ========Step4.A521 登記舊 的產品SN ========
					if (!list.get("pb_old_sn").equals("")) {
						// 是否需要登記舊的SN(不能找已經登記過的)
						List<ProductionBody> body_old = pbDao.findAllByPbbsnAndPbbsnNotLike(list.getString("pb_old_sn"), "%old%");
						if (body_old.size() == 1) {
							List<ProductionHeader> prList_old = phDao.findAllByPhpbgid(body_old.get(0).getPbgid());
							// 不是自己
							if (prList_old.size() == 1 && !prList_old.get(0).getProductionRecords().getPrid().equals(list.getString("ph_pr_id"))) {
								ProductionBody body_old_one = new ProductionBody();
								JSONArray old_sn = new JSONArray();
								body_old_one = body_old.get(0);
								// 已經有舊資料的話
								if (body_old_one.getPboldsn() != null && !body_old_one.getPboldsn().equals("")) {
									old_sn = new JSONArray(body_old_one.getPboldsn());
									old_sn.put(list.getString("pb_old_sn") + "_old_" + old_sn.length());
									body_one.setPboldsn(old_sn.toString());
								} else {
									body_old_one.setPbbsn(list.getString("pb_old_sn") + "_old_" + old_sn.length());
									body_old_one.setPbsn(list.getString("pb_old_sn") + "_old_" + old_sn.length());
									pbDao.save(body_old_one);

									old_sn.put(list.getString("pb_old_sn") + "_old_" + old_sn.length());
									body_one.setPboldsn(old_sn.toString());
								}
							} else {
								bean.autoMsssage("WK009");
							}
						} else {
							bean.autoMsssage("WK008");
							return bean;
						}
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
								localPath = "";//
						int ftpPort = c_json.getInt("PORT");
						String[] searchName = { list.getString("ph_pr_id"), "", list.getString("pb_b_sn") };
						FtpUtilBean ftp = new FtpUtilBean(ftpHost, ftpUserName, ftpPassword, ftpPort);
						ftp.setRemotePath(remotePath);
						ftp.setRemotePathBackup(remotePathBackup);
						ftp.setLocalPath(localPath);
						FTPClient ftpClient = new FTPClient();
						list_log = ftpService.getLogPLT(ftpClient, ftp, user.getSuaccount(), searchName, plt_file_classify);
						// PLT檢查
						if (list_log.length() < 1 || !list_log.has("pb_l_size") || list_log.getInt("pb_l_size") < 10) {
							bean.autoMsssage("WK007");
							return bean;
						}

						// ======== Step6. 需要Log 更新資料?? ========
						// (檢查是否有LOG+內容是否正確)
						if (list_log != null && list_log.length() > 0 && list_log.has("pb_l_size")) {

							// 檢查所有可能對應的欄位
							Iterator<String> keys = list_log.keys();
							ProductionBody title_body = new ProductionBody();
							title_body = pbDao.findAllByPbid(0l).get(0);
							// 是否存檔
							if (plt_save) {
								while (keys.hasNext()) {
									String cell_key = keys.next();
									// sn關聯表
									int j = 0;
									for (j = 0; j < 50; j++) {
										String get_name = "getPbvalue" + String.format("%02d", j + 1);
										String set_name = "setPbvalue" + String.format("%02d", j + 1);
										String cell_name = "pb_value" + String.format("%02d", j + 1);
										try {
											// 取出欄位名稱 ->存入body_title資料
											Method set_method = body_one.getClass().getMethod(set_name, String.class);
											Method get_method = title_body.getClass().getMethod(get_name);
											String title_value = (String) get_method.invoke(title_body);

											if (title_value != null && title_value.equals(cell_key)) {
												String body_value = list_log.getString(cell_key);
												// 檢查 避免小卡 輸入主SN序號
												for (ProductionBody one_sn : check_sn) {
													if (one_sn.getPbbsn().equals(body_value)) {
														bean.autoMsssage("WK010");
														return bean;
													}
												}
												// 檢查是否有需要 檢查重複
												ArrayList<Workstation> check_only = wkDao.findAllByWorkstation_item(list.getString("w_c_name"), cell_name);
												if (check_only != null && check_only.size() > 0 && check_only.get(0).getWonly() == 1) {
													String nativeQuery = "SELECT b.pb_b_sn FROM production_body b ";
													nativeQuery += "where ";
													nativeQuery += "b." + cell_name + " = :pb_value ";
													nativeQuery += " and b." + cell_name + " != '' ";
													nativeQuery += " and b.pb_b_sn not like '%old%'"; // 排除已經被替代的
													if (!list.getString("pb_b_sn").equals("")) {// 排除自己 新的SN
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
													List<String> pbid_obj = query.getResultList();
													if (pbid_obj.size() > 0) {// 有重複
														bean.setError_ms("此 PLT_LOG [" + title_value + " SN]: " + body_value + " , "//
																+ "已經被[產品/燒錄 SN]: " + pbid_obj.get(0) + " 使用中 ");
														bean.autoMsssage("WK011");
														return bean;
													}
												}

												set_method.invoke(body_one, body_value);
												break;
											} else if (title_value == null) {
												break;
											}

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
									}
								}

								body_one.setPbltext(list_log.getString("pb_l_text"));
								body_one.setPblpath(list_log.getString("pb_l_path"));
								body_one.setPbldt(Fm_Time.toDateTime(list_log.getString("pb_l_dt")));
								body_one.setPblsize(list_log.getInt("pb_l_size") + "");
							}
						}

						// ======== Step7. 檔案歸檔?========
						ftpService.logPLT_Archive(ftpClient, ftp, list_log.getJSONArray("pb_l_files"));
					}

					pbDao.save(body_one);

					// ======== Step7. 製令單+規格更新[ProductionRecords]========
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
					// 此製令已完成
					if (check_end && p_records.getPrpokquantity() == p_records.getPrpquantity()) {
						p_header.setSysstatus(2);
						p_header.setPhedate(new Date());
					}
					phDao.save(p_header);

				} else {
					bean.autoMsssage("WK003");
					return bean;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			bean.autoMsssage("1111");
			return bean;
		}
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
