package dtri.com.tw.service;

//import java.net.URLDecoder;
//import java.text.DateFormat;
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
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.OqcResultListDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class OqcFailureRateService {

	@Autowired
	private OqcResultListDao orlDao;	
	@Autowired
	private EntityManager em;

	// 取得當前 資料清單
	@SuppressWarnings("unchecked")    // @SuppressWarnings("unchecked")標記是for OqcInspectionForms = query.getResultList();
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<OqcInspectionForm> OqcInspectionForms = new ArrayList<OqcInspectionForm>();
//		List<OqcInspectionItems> Oiis = new ArrayList<OqcInspectionItems>();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			// page = 0;
			p_size = 100;
		}

		String oif_c_name = null;
		String oif_ow = null;
		String oif_o_nb = null;
		String oif_e_date_s=null;
		String oif_e_date_e=null;
	//	int sys_status = 2;
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// ****************** 放入包裝(header) [01
			// 是排序][_h_******************資料顯示在SEARCH頁面下的TABLE 表頭 資料********
			// 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();		
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_id", FFS.h_t("ID", "50px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_ow", FFS.h_t("工單", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_c_name", FFS.h_t("客戶名稱", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_o_nb", FFS.h_t("訂單號", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_nb", FFS.h_t("產品號", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_model", FFS.h_t("產品型號", "150px", FFM.Wri.W_Y));
			// 先取消 object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) +
			// "oif_p_specification",FFS.h_t("產品規格", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_sn", FFS.h_t("產品序號區間", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_qty", FFS.h_t("出貨數", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_t_qty", FFS.h_t("抽樣數", "100px", FFM.Wri.W_Y));
			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_n_qty", FFS.h_t("NG數", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "orl_f_rate", FFS.h_t("不良率", "100px", FFM.Wri.W_Y));
			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_name", FFS.h_t("產品名", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_c_date", FFS.h_t("製表日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_c_user", FFS.h_t("製表人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_e_date", FFS.h_t("最後檢驗日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_e_user", FFS.h_t("最後檢驗人", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_f_date", FFS.h_t("審核日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_f_user", FFS.h_t("審核人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("資料狀態", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "150px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			// ****************************************************** 放入包裝(search) 
			JSONArray n_val = new JSONArray();
			JSONArray object_searchs = new JSONArray();
			// "oii_check_name", "檢查項目名稱")
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_ow", "工單", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_c_name", "客戶名稱", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_o_nb", "訂單號", n_val));
			
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "oif_e_date_s", "檢驗日(起)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "oif_e_date_e", "檢驗日(終)", n_val));


			bean.setCell_searchs(object_searchs);
		} else {
			// *****************************進行-特定查詢 ***********TABLE表單 上方的叟巡輸入框****
			oif_ow = body.getJSONObject("search").getString("oif_ow"); // "工單號"
			oif_ow = oif_ow.equals("") ? null : oif_ow;

			oif_c_name = body.getJSONObject("search").getString("oif_c_name"); // "客戶名稱
			oif_c_name = oif_c_name.equals("") ? null : oif_c_name;

			oif_o_nb = body.getJSONObject("search").getString("oif_o_nb"); // "訂單號"
			oif_o_nb = oif_o_nb.equals("") ? null : oif_o_nb;
			
			oif_e_date_s = body.getJSONObject("search").getString("oif_e_date_s"); // "檢驗日(起)"				
			oif_e_date_e = body.getJSONObject("search").getString("oif_e_date_e"); // "檢驗日(終)"

		//	sys_status = body.getJSONObject("search").optInt("sys_status", -1); // "資料狀態

		}

		// ******* 放入包裝(body) [01
		// 是排序][_b__***********資料顯示在SEARCH頁面下的TABLE表單資料***********
		// 是分割直][資料庫欄位名稱]
		// 查詢SN欄位+產品型號+製令單號
		// --- SQL ---
		String nativeQuery = "SELECT d.* FROM oqc_inspection_form d "
		                   + "WHERE (:oifow='' OR d.oif_ow LIKE :oifow) AND " ;//工單
		
		nativeQuery +="(:oifcname='' OR d.oif_c_name LIKE :oifcname) AND"; // "客戶名稱
		nativeQuery +="(:oifonb='' OR d.oif_o_nb LIKE :oifonb) AND"; // "訂單號
	// **************** oif_f_date == null	 預設30日  做搜尋 **********
		Date mmdds =Fm_Time.to_count(-20, new Date());
		Date mmdde =Fm_Time.to_count(0, new Date());		
		String s_Date = Fm_Time.to_y_M_d(mmdds)+" 08:30:00";		
		String e_Date = Fm_Time.to_y_M_d(mmdde) + " 17:30:00";
		System.out.println(s_Date);
		System.out.println(e_Date);
	// *************************************************	
		oif_e_date_s = (oif_e_date_s== null) ? s_Date : oif_e_date_s; 	// "檢驗日(起)" 
		oif_e_date_e = (oif_e_date_e== null) ? e_Date : oif_e_date_e; 	//"檢驗日(終)"
		
		//有日期才添加
		if(!oif_e_date_s.equals("") && !oif_e_date_e.equals("")) {
			 nativeQuery  += " (  d.oif_e_date   BETWEEN  :oifedates  and :oifedatee ) AND ";		
		}
		
		nativeQuery +=  " (d.sys_status = 2 )  "; // "資料狀態" 2為已審核

		Query query = em.createNativeQuery(nativeQuery, OqcInspectionForm.class);
	
		oif_ow = (oif_ow == null) ? "" : oif_ow;		// 工單
		query.setParameter("oifow", "%" + oif_ow + "%");
		
		oif_c_name = (oif_c_name == null) ? "" : oif_c_name;	// 客戶名稱
		query.setParameter("oifcname", "%" + oif_c_name + "%");
		
		oif_o_nb = (oif_o_nb == null) ? "" : oif_o_nb;	// 訂單號
		query.setParameter("oifonb", "%" + oif_o_nb + "%");

		// "檢驗日(起)" "檢驗日(終)"
		if(!oif_e_date_s.equals("") && !oif_e_date_e.equals("")) { 
			query.setParameter("oifedates",  Fm_Time.toDateTime(oif_e_date_s));
			query.setParameter("oifedatee",  Fm_Time.toDateTime(oif_e_date_e));				
		}
			
		OqcInspectionForms = query.getResultList();		
		//OqcInspectionForms = oifDao.findByoifowAndoifcnameAndoifonb(oif_ow, oif_c_name, oif_o_nb, 2);
		JSONArray object_bodys = new JSONArray();
		OqcInspectionForms.forEach(one -> {
			int ord = 0;
			JSONObject object_body = new JSONObject();
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_id", one.getOifid()); // ID
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_ow", one.getOifow()); // 工單
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_name", one.getOifcname()); // 客戶名稱
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_o_nb", one.getOifonb()); // 訂單號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_nb", one.getOifpnb());// 產品號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_model", one.getOifpmodel()); // 產品型號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_sn", one.getOifpsn()); // 產品序號區間
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_qty", one.getOifpqty()); // 出貨數
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_t_qty", one.getOiftqty()); // 抽樣數			
			
			//******************* 顯示 每筆工單的不良NG數  同SN不管壞幾次,都算一次不良  ********************		
			String ow=one.getOifow();
			long count = orlDao.countLastNgByOrlow(ow);			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_n_qty", count); //NG數
			
			long x= one.getOifpqty();
			Double failureRate= (double)count/x ;
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "orl_f_rate", failureRate); //不良率
	
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_name", one.getOifpname());// 產品名

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_date", one.getOifcdate());// 製表日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_user", one.getOifcuser());// 製表人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_e_date", one.getOifedate()== null ? "" :one.getOifedate());// 最後檢驗日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_e_user", one.getOifeuser()== null ? "" :one.getOifeuser());// 最後檢驗人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_f_date", one.getOiffdate() == null ? "" :one.getOiffdate());// 審核日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_f_user", one.getOiffuser() == null ? "" :one.getOiffuser());// 審核人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
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
//		JSONObject body = req.getBody();
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
//		JSONObject body = req.getBody();
		boolean check = false;
		check = true;
		try {
//			JSONArray list = body.getJSONArray("save_as");
//			for (Object one : list) {
				// 物件轉換
//				JSONObject data = (JSONObject) one;
//			}
			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
			
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單 ************* 給modify使用* 與 審核按鈕 使用 ************
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
//		JSONObject body = req.getBody();
		boolean check = false;
		
		try {			

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
//		JSONObject body = req.getBody();
		boolean check = false;
		try {
//			JSONArray list = body.getJSONArray("delete");
			check = true;
		//	resp.setError_ms("未開放此功能");
		//	resp.autoMsssage("109"); // 回傳錯誤訊息
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}


}
