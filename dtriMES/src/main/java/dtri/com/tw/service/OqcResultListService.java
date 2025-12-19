package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.OqcInspectionForm;
import dtri.com.tw.db.entity.OqcResultList;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
//import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.OqcInspectionFormDao;
import dtri.com.tw.db.pgsql.dao.OqcResultListDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class OqcResultListService {
	@Autowired // 通用-製令內容
	private ProductionHeaderDao headerDao;

	@Autowired // 產品細節
	private ProductionBodyDao bodyDao;

	@Autowired
	private OqcInspectionFormDao oifDao;

	@Autowired
	private OqcResultListDao orlDao;

	@Autowired
	private EntityManager em;
	// 取得當前 資料清單
	@SuppressWarnings("unchecked")
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		int p_size = req.getPage_total();
	//	List<OqcInspectionForm> OqcInspectionForms = new ArrayList<OqcInspectionForm>();
		List<OqcResultList> OqcResultLists = new ArrayList<OqcResultList>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			// page = 0;
			p_size = 100;
		}

		String orl_ow = null;
		String orl_p_sn = null;
		String orl_t_user=null;
		String orl_t_date_s=null;
		String orl_t_date_e=null;
		String orl_t_item=null;
		String orl_t_results=null;
		int sys_status = -1;

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// ****************** 放入包裝(header) [01
			// 是排序][_h_******************資料顯示在SEARCH頁面下的TABLE 表頭 資料********
			// 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_id", FFS.h_t("ID", "50px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_oif_id", FFS.h_t("配對檢驗表的Key", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_ow", FFS.h_t("工單號", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_p_sn", FFS.h_t("產品SN號", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_p_nb", FFS.h_t("產品料號", "150px", FFM.Wri.W_Y));
			// object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_n", FFS.h_t("項次",
			// "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_t_item", FFS.h_t("測試項目", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_t_results", FFS.h_t("測試結果", "150px", FFM.Wri.W_Y)); // [PASS/暫停 / NG]

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_t_date", FFS.h_t("檢驗日期", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_t_user", FFS.h_t("檢驗人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("資料狀態", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "150px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			// *************** 放入修改 [(key)](modify/Create/Delete) 格式**********進入後
			// ******************************** 在 modify/Create/Delete) 顯示的畫面*****
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "orl_id", "ID")); // ID;
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "orl_oif_id", "配對檢驗表的Key"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "orl_ow", "工單號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "orl_p_nb", "產品料號"));

			// obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val,"orl_n", "項次"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "orl_p_sn", "產品SN號"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "功能(測試OS)").put("key", "功能(測試OS)"));
			a_val.put((new JSONObject()).put("value", "功能(T2 OS)").put("key", "功能(T2 OS)"));
			a_val.put((new JSONObject()).put("value", "Check T2 OS").put("key", "Check T2 OS"));
			a_val.put((new JSONObject()).put("value", "外觀/包裝檢驗").put("key", "外觀/包裝檢驗"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "orl_t_item", "測試項目"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, a_val, "orl_t_item_m", "測試項目(修)"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "PASS").put("key", "PASS"));
			a_val.put((new JSONObject()).put("value", "暫停").put("key", "暫停"));
			a_val.put((new JSONObject()).put("value", "NG").put("key", "NG"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "orl_t_results", "測測試結果"));// [PASS / 暫停 / NG]
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, a_val, "orl_t_results_m", "測測試結果(修)"));// [PASS / 暫停 / NG]

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "sys_note", "備註"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "orl_t_date", "檢驗日"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "orl_t_user", "最後檢驗人"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "鎖定").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "作廢").put("key", "2"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", "資料狀態"));  //不給改
			
			bean.setCell_modify(obj_m);
			
//**********************在 客製化頁面設置	*********************************
			
			JSONArray obj_t = new JSONArray();				
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "功能(測試OS)").put("key", "功能(測試OS)"));
			a_val.put((new JSONObject()).put("value", "功能(T2 OS)").put("key", "功能(T2 OS)"));
			a_val.put((new JSONObject()).put("value", "Check T2 OS").put("key", "Check T2 OS"));
			a_val.put((new JSONObject()).put("value", "外觀/包裝檢驗").put("key", "外觀/包裝檢驗"));		
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, a_val, "orl_t_item", "測試項目"));
			
			//*********************** 設置 產品細節的輸入欄位名稱 資訊到前端 ********************************** 
			ProductionBody pb = bodyDao.findAllByPbid(0l).get(0); //取得pbid=0的所有資料 然後再取締一筆資料來用
			int j = 0;		
			Method method;
			
			// sn關聯表
			for (j = 0; j < 50; j++) {
				String m_name = "getPbvalue" + String.format("%02d", j + 1);
				try {
					method = pb.getClass().getMethod(m_name);
					String value = (String) method.invoke(pb);
					String name = "pb_value" + String.format("%02d", j + 1);
					if (value != null && !value.equals("")) {
						obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, name, value));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pb_f_value", "故障項目"));
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", true, n_val, "pb_f_note", "故障說明"));
			
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_path", "檢測Log位置"));
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_size", "檢測Log大小"));
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-6", false, n_val, "pb_l_dt", "上傳Log時間"));
			//obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "pb_position", "最後位置"));
			//obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", "備註"));		
		
			bean.setCell_g_modify(obj_t);
	
			// *********************************************************** 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// "oii_check_name", "檢查項目名稱")
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "orl_ow", "工單號碼", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "orl_p_sn", "產品SN號", n_val));
			
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-1", "orl_t_user", "最後檢驗人", n_val));		
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "orl_t_date_s", "檢驗日(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "orl_t_date_e", "檢驗日(終)", n_val));
			
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "功能(測試OS)").put("key", "功能(測試OS)"));
			a_val.put((new JSONObject()).put("value", "功能(T2 OS)").put("key", "功能(T2 OS)"));
			a_val.put((new JSONObject()).put("value", "Check T2 OS").put("key", "Check T2 OS"));
			a_val.put((new JSONObject()).put("value", "外觀/包裝檢驗").put("key", "外觀/包裝檢驗"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-2", "orl_t_item", "測試項目", a_val));
			
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "PASS").put("key", "PASS"));
			a_val.put((new JSONObject()).put("value", "暫停").put("key", "暫停"));
			a_val.put((new JSONObject()).put("value", "NG").put("key", "NG"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "orl_t_results", "測測試結果", a_val));
		//	*************** 資料狀態 先不用顯示 *********
		//	a_val = new JSONArray();
		//	a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
		//	a_val.put((new JSONObject()).put("value", "鎖定").put("key", "1"));
		//	a_val.put((new JSONObject()).put("value", "作廢").put("key", "2"));
		//	object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "sys_status", "資料狀態", a_val));
			bean.setCell_searchs(object_searchs);

		} else {
			// *****************************進行-特定查詢 ***********TABLE表單 上方的叟巡輸入框****
			orl_ow = body.getJSONObject("search").getString("orl_ow"); // "工單號"
			orl_ow = (orl_ow== null) ? "" : orl_ow;

			orl_p_sn = body.getJSONObject("search").getString("orl_p_sn"); // "產品SN號"
			orl_p_sn = (orl_p_sn== null) ? "" : orl_p_sn;
			
			orl_t_user = body.getJSONObject("search").getString("orl_t_user"); // "最後檢驗人"
			orl_t_user = (orl_t_user== null) ? "" : orl_t_user;// "最後檢驗人"

			orl_t_date_s = body.getJSONObject("search").getString("orl_t_date_s"); // "檢驗日(起)"
			//orl_t_date_s = (orl_t_date_s== null) ? "": orl_t_date_s;			
			orl_t_date_e = body.getJSONObject("search").getString("orl_t_date_e"); // "檢驗日(終)"
			//orl_t_date_e = (orl_t_date_e== null) ? "" : orl_t_date_e;
			
			orl_t_item = body.getJSONObject("search").getString("orl_t_item"); // "測試項目"
			orl_t_results = body.getJSONObject("search").getString("orl_t_results"); // "測測試結果"

			sys_status = body.getJSONObject("search").optInt("sys_status", -1); // "資料狀態

		}
		// ******* 放入包裝(body) [01
		// 是排序][_b__***********資料顯示在SEARCH頁面下的TABLE表單資料***********
		// 是分割直][資料庫欄位名稱]
	//	OqcResultLists = orlDao.findByLikeOrlowAndOrlpsn(orl_ow, orl_p_sn, sys_status);
		
	//***************************************************************************	
		// 查詢SN欄位+產品型號+製令單號
		// --- SQL ---
		String nativeQuery = "SELECT d.* FROM oqc_result_list d "
		                   + "WHERE (:orlow='' OR d.orl_ow LIKE :orlow) AND " ;//工單
					
		orl_t_date_s = (orl_t_date_s== null) ? "" : orl_t_date_s; 	// "檢驗日(起)" 
		orl_t_date_e = (orl_t_date_e== null) ? "" : orl_t_date_e; 	//"檢驗日(終)"
		//有日期才添加
		if(!orl_t_date_s.equals("") && !orl_t_date_e.equals("")) {
			 nativeQuery  += " (  d.orl_t_date   BETWEEN  :orltdates  and :orltdatee ) AND ";		
		}
		
		nativeQuery+=  " (:orltuser='' OR d.orl_t_user LIKE :orltuser) AND ";// "最後檢驗人"			
		nativeQuery+=  " (:orlpsn='' OR d.orl_p_sn LIKE :orlpsn) AND "; // "產品SN號"
		nativeQuery+=  " (:orltitem='' OR d.orl_t_item LIKE :orltitem) AND "; // "測試項目"
		nativeQuery+=  " (:orltresults='' OR d.orl_t_results LIKE :orltresults) AND "; // "測測試結果"
		nativeQuery+=  " (:sysstatus = -1 or d.sys_status =:sysstatus )  "; // "資料狀態"

		Query query = em.createNativeQuery(nativeQuery, OqcResultList.class);
		
		orl_ow = (orl_ow== null) ? "" : orl_ow;		// 工單
		query.setParameter("orlow", "%" + orl_ow + "%");

		// "檢驗日(起)" "檢驗日(終)"
		if(!orl_t_date_s.equals("") && !orl_t_date_e.equals("")) { 
			query.setParameter("orltdates",  Fm_Time.toDateTime(orl_t_date_s));
			query.setParameter("orltdatee",  Fm_Time.toDateTime(orl_t_date_e));				
		}
				
		orl_p_sn = (orl_p_sn== null) ? "" : orl_p_sn;  // "產品SN號"
		query.setParameter("orlpsn", "%" + orl_p_sn + "%");		
		
		orl_t_user = (orl_t_user== null) ? "" : orl_t_user;  // "最後檢驗人"
		query.setParameter("orltuser", "%" + orl_t_user + "%");
		
		orl_t_item = (orl_t_item== null) ? "" : orl_t_item; //"測試項目"
		query.setParameter("orltitem", "%" + orl_t_item + "%");
		
		orl_t_results = (orl_t_results== null) ? "" : orl_t_results; //測測試結果
		query.setParameter("orltresults", "%" + orl_t_results + "%");
		
		query.setParameter("sysstatus", sys_status); // "資料狀態"

		OqcResultLists = query.getResultList();

	//***********************************	
		
		JSONArray object_bodys = new JSONArray();
		OqcResultLists.forEach(one -> {
			int ord = 0;
			JSONObject object_body = new JSONObject();
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_id", one.getOrlid()); // ID
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_oif_id", one.getOrloifid()); // 配對檢驗表的Key
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_ow", one.getOrlow()); // 工單
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_p_sn", one.getOrlpsn());// 產品SN號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_p_nb", one.getOrlpnb());// 產品料號

			// object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_n", one.getOrln());//// 項次			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_t_item", one.getOrltitem());// 測試項目
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_t_results", one.getOrltresults());//測試結果[PASS/暫停/NG]

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_t_date", one.getOrltdate());// 最後檢驗日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_t_user", one.getOrltuser());// 最後檢驗人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
		
			object_bodys.put(object_body);

		});

		bean.setBody(new JSONObject().put("search", object_bodys));
		return true;
	}

	// 新建資料 後 存檔 資料清單 到資料庫
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
			}
		//	check = true;
			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單 ************* ************
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		try {
			JSONArray list = body.getJSONArray("modify");
			OqcResultList oRL = new OqcResultList();
			String orl_p_sn=null;
			
			for (Object one : list) {
//				// 物件轉換
				JSONObject data = (JSONObject) one;
				oRL = orlDao.findAllByOrlid(data.getLong("orl_id")).get(0);
				orl_p_sn=data.getString("orl_p_sn");
				//PASS 不能修改
				if (!data.getString("orl_t_results").equals("PASS") && data.getInt("sys_status")<2) {
					oRL.setOrltitem(data.getString("orl_t_item_m"));
					oRL.setOrltresults(data.getString("orl_t_results_m"));
					oRL.setSysnote(data.getString("sys_note")); // 備註				
					oRL.setSysstatus(data.getInt("sys_status")); // 資料狀態
					oRL.setSysmdate(new Date());// 修改時間
					oRL.setSysmuser(user.getSuaccount());// 修改者(帳號)
					orlDao.save(oRL);			
										
					// STEP1. 先確認SN是否屬與此工單
					// A.用機台號碼搜尋有無資料
					List<ProductionBody> check_Bodys = bodyDao.findAllByPbsnAndpbvalue16(null, orl_p_sn, null);
					// B. 在 ProductionBody 資料表取得 
					ProductionBody cb = check_Bodys.get(0);// 取得產品細節資料
					//******************** 登記到產品細節 "OQC檢驗的內容"欄位 **********************************************************			
					cb.setPblnoteoqc(data.getString("orl_t_item")+" : "+data.getString("orl_t_results")+" 備註 : "+data.getString("sys_note"));			
					bodyDao.save(cb);					
					check = true;
				}else if(data.getString("orl_t_results").equals("PASS")) {
					resp.setError_ms("已經PASS的資料不能修改");
					resp.autoMsssage("109"); // 回傳錯誤訊息
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 移除 資料清單****************不給刪除*********
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				// * Long x = data.getLong("oif_id");
				// * System.out.println(x);

				// * oifDao.deleteById(x);
			}
			// *check = true;
			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
			
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// ==============客製化==============

	// 取得 - Customized mode當前表單式查詢資料 "S1"
	@Transactional
	public boolean getDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		List<OqcInspectionForm> OqcInspectionForms = new ArrayList<OqcInspectionForm>();
		List<OqcResultList> OqcResultLists = new ArrayList<OqcResultList>();
		String orl_ow = null; // "工單號"
		String orl_p_sn = null; // "機台號碼"
		int sys_status = 0;  //"資料狀態"
		// 建立 object_body 物件 為最上層, 用於存放各個資訊
		JSONObject object_body = new JSONObject();
		// 建立空的JSONObject 的object_details物件 用來存放資料
		JSONObject object_details = new JSONObject();
		// 建立空的JSONObject 的object_detail物件 用來存放資料
		JSONObject object_detail = new JSONObject();
		JSONArray object_results = new JSONArray();
		try {
				//**************  取出 檢核表單 及 檢驗的資料清單 *********************
			if (body.getJSONObject("search").has("input_orlow")) {
				
				orl_ow = body.getJSONObject("search").getString("input_orlow"); // "工單號"
				orl_ow = orl_ow.equals("") ? null : orl_ow;
			
				// step1 先用工單搜尋有無建立檢核表單存在
				OqcInspectionForms = oifDao.findByOifow(orl_ow);
				if (OqcInspectionForms.isEmpty()) {				
					resp.setError_ms("無此(工單號)檢核表單" + orl_ow);
					resp.autoMsssage("102"); // 回傳錯誤訊息
					return check;
				}
				OqcInspectionForm oif = OqcInspectionForms.get(0);
				long x= oif.getSysstatus();
				if(x==2) {
					resp.setError_ms("已審核過，無法再檢驗登記");
					resp.autoMsssage("109"); // 回傳錯誤訊息
				}
				if(x==3) {
					resp.setError_ms("此單已作廢，無法再檢驗登記");
					resp.autoMsssage("109"); // 回傳錯誤訊息
				}				
				// step2.用工單搜尋有資料庫有無已經有檢驗的資料清單 存在 (完全比對) ,資料狀態=0 , 只搜尋 "資料狀態sys_status"為"正常"或已結單的資料
				OqcResultLists = orlDao.findByOrlowAndOrlpsn(orl_ow, null);

				if (OqcResultLists == null || OqcResultLists.isEmpty() || OqcResultLists.get(0) == null) {
					System.out.println("no data,就只會 OqcInspectionForms 資料庫取出 檢查資料表");
					object_detail.put("oif_id", oif.getOifid()); // id
					object_detail.put("oif_ow", oif.getOifow()); // 工單號
					object_detail.put("oif_p_nb", oif.getOifpnb()); // 產品料號
					object_detail.put("oif_oii_data", oif.getOifoiidata());// 配置的檢驗項目 JSON
					object_detail.put("oif_oii_form", oif.getOifoiiform());// 配置的HTML form項目
					object_detail.put("oif_sys_status", oif.getSysstatus());
					object_details.put("detail", object_detail);
					object_body.put("Customized_detail", object_details);
					System.out.println("往前端送資料");					
				} else {
					System.out.println("有資料,就從step1 OqcInspectionForms資料庫取出檢查資料表 step2. OqcResultLists資料庫取出檢驗登記清單");
					// step 1.
					//OqcInspectionForm oif = OqcInspectionForms.get(0);
					object_detail.put("oif_id", oif.getOifid()); // id
					object_detail.put("oif_ow", oif.getOifow()); // 工單號
					object_detail.put("oif_p_nb", oif.getOifpnb()); // 產品料號
					object_detail.put("oif_oii_data", oif.getOifoiidata());// 配置的檢驗項目 JSON
					object_detail.put("oif_oii_form", oif.getOifoiiform());// 配置的HTML form項目
					object_detail.put("oif_sys_status", oif.getSysstatus()); //狀態 (若是2) 為審核後鎖定 給前端判定要不要鎖 產品序號欄位
					object_details.put("detail", object_detail);
					object_body.put("Customized_detail", object_details);
					
					//*********************** 計算指定工單號碼下，每個測試項目 SN 的最後一筆檢查結果為 PASS 的數量。 ****************************************	
					long count = orlDao.countLastPassByOrlow(orl_ow);					
					object_body.put("OqcPassQty", count);		
					
					String orltitem="功能(測試OS)";
					count = orlDao.countLastPassByOrlowAndOrltitem(orl_ow,orltitem);									
					object_body.put("OqcFTOs", count);
					
					orltitem="功能(T2 OS)";
					count = orlDao.countLastPassByOrlowAndOrltitem(orl_ow,orltitem);									
					object_body.put("OqcFT2Os", count);
					
					orltitem="Check T2 OS";
					count = orlDao.countLastPassByOrlowAndOrltitem(orl_ow,orltitem);									
					object_body.put("OqcCT2Os", count);
					
					orltitem="外觀/包裝檢驗";
					count = orlDao.countLastPassByOrlowAndOrltitem(orl_ow,orltitem);										
					object_body.put("OqcAPI", count);					
					
					//*********************************************************************	
					OqcResultLists.forEach(orl -> {
						JSONObject object_orl = new JSONObject();			
						object_orl.put("orl_p_sn", orl.getOrlpsn()); // 產品SN號
						object_orl.put("orl_t_item", orl.getOrltitem()); // 測試項目
						object_orl.put("orl_t_results", orl.getOrltresults()); // 測試結果
						object_orl.put("orl_t_date", Fm_Time.to_yMd_Hms(orl.getOrltdate())); // 檢驗日期
						object_orl.put("orl_t_user", orl.getOrltuser()); // 檢驗人
						object_orl.put("sys_note", orl.getSysnote()); // 備註
						object_results.put(object_orl);
					});
				}
				check = true;
				object_body.put("OqcResultList", object_results);				
				resp.setBody(object_body);
			
			}
			//**************  取出產品細節資料 *********************
			if (body.getJSONObject("search").has("orl_p_sn")) {
				
				orl_p_sn = body.getJSONObject("search").getString("orl_p_sn"); // "產品序號"
				orl_p_sn = orl_p_sn.equals("") ? null : orl_p_sn;				
				orl_ow = body.getJSONObject("search").getString("orl_ow"); // "工單號"
				orl_ow = orl_ow.equals("") ? null : orl_ow;
				
				// STEP1. 先確認SN是否屬與此工單
				// A.用機台號碼搜尋有無資料
				List<ProductionBody> check_Bodys = bodyDao.findAllByPbsnAndpbvalue16(null, orl_p_sn, null);
				if (check_Bodys.isEmpty()) {
		
					resp.autoMsssage("102"); // 回傳錯誤訊息(無資料存在)
					return check;
				}
				// B. 在 ProductionBody 資料表取得 "群組對應製令ID"
				ProductionBody cb = check_Bodys.get(0);// 取得產品細節資料
			
				// C. 用" 群組對應製令ID" 取得 對應的 表頭資料
				List<ProductionHeader> phs = headerDao.findAllByPhpbgid(cb.getPbgid());
				ProductionHeader ph = phs.get(0);// 取得 產品表頭資料

				// D. 在表頭資料取得 "ProductionRecords"關聯表資料
				ProductionRecords pr = ph.getProductionRecords();
				// E. 在ProductionRecords 取的工單號碼
				String ow = pr.getPrid().trim();		
				//F. 比對資料庫取得的工單號碼 與 輸入工單號碼是否相同
				if (ow == null || !ow.equals(orl_ow)) {
					resp.setError_ms("機台號碼不在此工單中");
					resp.autoMsssage("102"); // 回傳錯誤訊息
					return check;
				}				
				if(pr.getSysstatus() ==2 ) {
					resp.setError_ms("已審核無法儲存");
					resp.autoMsssage("102"); // 回傳錯誤訊息
					return check;
				}
				
	
				//***************** 產品細節 *** sn關聯表
				
				ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0); //取得pbid=0的所有資料 然後再取第一筆資料來用做為資料的方法名稱
				int ord = 0;			
				JSONObject productionbodyvaule = new JSONObject();
				try {
					// 有效設定的欄位
					for (int k = 0; k < 50; k++) {
						String in_name = "getPbvalue" + String.format("%02d", k + 1); //組合getter 方法名稱
						Method in_method = body_one.getClass().getMethod(in_name); //透過反射取得ProductionBody類別中明為getPbvalueXX()的方法
						String value = (String) in_method.invoke(body_one);	// 執行這個方法 等於body_one.getPbvalueXX()的方法
						// 欄位有定義的顯示
						if (value != null && !value.equals("")) { //執行這個body_one.getPbvalueXX()的方法 不為null和 空 才能執行內容
							// sn關聯表
							String name_b = "getPbvalue" + String.format("%02d", k + 1);//組合getter 方法名稱
							Method method_b = cb.getClass().getMethod(name_b);//透過反射取得ProductionBody類別中明為getPbvalueXX()的方法
							String value_b = (String) method_b.invoke(cb); // 執行這個方法 等於cb.getPbvalueXX()的方法
							String key_b = "pb_value" + String.format("%02d", k + 1);  //組合出名稱
							productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + key_b, (value_b == null ? "" : value_b));
						}
					}						
					productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_f_value", cb.getPbfvalue() == null ? "" : cb.getPbfvalue()); //故障項目
					productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_f_note", cb.getPbfnote() == null ? "" : cb.getPbfnote());	//故障說明
					productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_path", cb.getPblpath() == null ? "" : cb.getPblpath());	//檢測Log位置
					productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_size", cb.getPblsize() == null ? "" : cb.getPblsize());  //檢測Log大小
					productionbodyvaule.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_dt", cb.getPbldt() == null ? "" : Fm_Time.to_yMd_Hms(cb.getPbldt())); //上傳Log時間
							
				} catch (Exception e) {
					e.printStackTrace();
				}				
				
				object_body.put("productionbodyvaule", productionbodyvaule);
				resp.setBody(object_body);									
				check = true;
			}			
			
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// ==============客製化==============

	// 更新/新增 資料清單 Customized mode "S2"  
	@Transactional  
	public boolean updateDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		JSONObject list = body.getJSONObject("oqc");
		boolean check = false;
		// STEP1. 先確認SN是否屬與此工單
		// Step2. 在確認sn是否已經存在result list 資料庫中,來判斷是否要"新增"或"更新"
		OqcResultList oRL = new OqcResultList();

		String orl_ow = list.getString("orl_ow").trim();
		String orl_p_sn = list.getString("orl_p_sn").trim();

		try {
			// STEP1. 先確認SN是否屬與此工單
			// A.用機台號碼搜尋有無資料
			List<ProductionBody> check_Bodys = bodyDao.findAllByPbsnAndpbvalue16(null, orl_p_sn, null);
			if (check_Bodys.isEmpty()) {			
				resp.autoMsssage("102"); // 回傳錯誤訊息("無資料存在")
				return check;
			}
			// B. 在 ProductionBody 資料表取得 "群組對應製令ID"
			ProductionBody cb = check_Bodys.get(0);// 取得產品細節資料
	
			// C. 用" 群組對應製令ID" 取得 對應的 表頭資料
			List<ProductionHeader> phs = headerDao.findAllByPhpbgid(cb.getPbgid());
			ProductionHeader ph = phs.get(0);// 取得 產品表頭資料
			
			// D. 在表頭資料取得 "ProductionRecords"關聯表資料
			ProductionRecords pr = ph.getProductionRecords();
			
			// E. 在ProductionRecords 取的工單號碼
			String ow = pr.getPrid().trim();		

			if (ow == null || !ow.equals(orl_ow)) {
				resp.setError_ms("機台號碼不在此工單中");
				resp.autoMsssage("102"); // 回傳錯誤訊息
				return check;
			}

			oRL.setOrloifid(list.getLong("orl_oif_id")); // 配對檢驗表的Key
			oRL.setOrlow(orl_ow); // 工單號
			oRL.setOrlpnb(list.getString("orl_p_nb")); // 產品料號
			oRL.setOrlpsn(orl_p_sn); // 產品SN號
			oRL.setOrltitem(list.getString("orl_t_item")); // 測試項目
			oRL.setOrltresults(list.getString("orl_t_results")); // 測試結果
			oRL.setOrltdate(new Date()); // 檢驗日期
			oRL.setOrltuser(user.getSuaccount()); // 檢驗人
			oRL.setSysnote(list.getString("sys_note")); // 備註
			oRL.setSyscuser(user.getSuaccount());// 創建者(帳號)
			oRL.setSysmdate(new Date());// 修改時間
			oRL.setSysmuser(user.getSuaccount());// 修改者(帳號)

			orlDao.save(oRL);
			
			//******************** 登記到產品細節 "OQC檢驗的內容"欄位 **********************************************************			
			cb.setPblnoteoqc(list.getString("orl_t_item")+" : "+list.getString("orl_t_results")+" 備註 : "+list.getString("sys_note"));			
			bodyDao.save(cb);			
			check = true;		

		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}
}
