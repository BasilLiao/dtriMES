package dtri.com.tw.service;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.OqcInspectionForm;
import dtri.com.tw.db.entity.OqcInspectionItems;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
//import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.OqcInspectionFormDao;
import dtri.com.tw.db.pgsql.dao.OqcInspectionItemsDao;
import dtri.com.tw.db.pgsql.dao.OqcResultListDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.ProductionRecordsDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class OqcInspectionFormService {
	@Autowired
	private OqcInspectionFormDao oifDao;	
	@Autowired
	private OqcResultListDao orlDao;
	@Autowired
	private ProductionRecordsDao prDao;
	@Autowired
	private OqcInspectionItemsDao oIIDao;
	// 主產品製程表頭 (通用-製令內容)
	@Autowired
	private ProductionHeaderDao headerDao;
	@Autowired  // 產品細節
	private ProductionBodyDao bodyDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<OqcInspectionForm> OqcInspectionForms = new ArrayList<OqcInspectionForm>();
		List<OqcInspectionItems> Oiis = new ArrayList<OqcInspectionItems>();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			// page = 0;
			p_size = 100;
		}

		String oif_c_name = null;
		String oif_ow = null;
		String oif_o_nb = null;
		int sys_status = -1;
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
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_name", FFS.h_t("產品名", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_model", FFS.h_t("產品型號", "150px", FFM.Wri.W_Y));
			// 先取消 object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) +
			// "oif_p_specification",FFS.h_t("產品規格", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_sn", FFS.h_t("產品序號區間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_qty", FFS.h_t("出貨數", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_t_qty", FFS.h_t("抽樣數", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_p_ver", FFS.h_t("版本資訊", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_title", FFS.h_t("標題值", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_c_date", FFS.h_t("製表日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_c_user", FFS.h_t("製表人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_e_date", FFS.h_t("最後檢驗日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_e_user", FFS.h_t("最後檢驗人", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_f_date", FFS.h_t("審核日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_f_user", FFS.h_t("審核人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oif_oii_data", FFS.h_t("配置的檢驗項目", "150px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("資料狀態", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "150px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			// *************** 放入修改 [(key)](modify/Create/Delete) 格式**********進入後
			// ******************************** 在 modify/Create/Delete) 顯示的畫面*****
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();
//			OqcInspectionItemss = oIIDao.findAll();
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "oif_id", "id")); // ID
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_title", "標題值"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_ow", "工單"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_c_name", "客戶名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_o_nb", "訂單號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_p_nb", "產品號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_p_name", "產品名"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_p_model", "產品型號"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_p_sn", "產品序號區間"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "oif_p_qty", "出貨數"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "oif_t_qty", "抽樣數"));

			// "版本資訊", "150px", FFM.Wri.W_Y));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_c_date", "製表日"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "oif_c_user", "製表人"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_e_date", "最後檢驗日"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "oif_e_user", "最後檢驗人"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", true, n_val, "oif_p_ver", "版本資訊"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-8", true, n_val, "oif_oii_data", "配置的檢驗項目"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-8", true, n_val,"sys_note", "備註"));			

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "已結單").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "已審核").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "作廢").put("key", "3"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", "資料狀態"));			
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "oif_f_date", "審核日"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "oif_f_user", "審核人"));

			bean.setCell_modify(obj_m);

			//////////////////// 給Customized mode 用
			//////////////////// 抓出以記錄的標題各一個給select///////////////////////////////

			JSONArray obj_t = new JSONArray();
			JSONArray t_val = new JSONArray();
			// t_val = new JSONArray();
			Oiis = oIIDao.findMinIdPerTitle();
			if (Oiis != null && Oiis.size() > 0) { // 如果資料筆數大於 0 才進行處理。
				for (OqcInspectionItems Oii : Oiis) {
					JSONObject obj = new JSONObject();
					obj.put("value", Oii.getOiititleval());
					obj.put("key", Oii.getOiititleval());
					t_val.put(obj);
				}
			}
			obj_t.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, t_val,	"oif_title", "標題值"));

			bean.setCell_g_modify(obj_t);
			
			// *********************************************************** 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// "oii_check_name", "檢查項目名稱")
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_ow", "工單", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_c_name", "客戶名稱", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oif_o_nb", "訂單號", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "已結單").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "已審核").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "作廢").put("key", "3"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "sys_status", "資料狀態", a_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// *****************************進行-特定查詢 ***********TABLE表單 上方的叟巡輸入框****
			oif_ow = body.getJSONObject("search").getString("oif_ow"); // "工單號"
			oif_ow = oif_ow.equals("") ? null : oif_ow;

			oif_c_name = body.getJSONObject("search").getString("oif_c_name"); // "客戶名稱
			oif_c_name = oif_c_name.equals("") ? null : oif_c_name;

			oif_o_nb = body.getJSONObject("search").getString("oif_o_nb"); // "訂單號"
			oif_o_nb = oif_o_nb.equals("") ? null : oif_o_nb;

			sys_status = body.getJSONObject("search").optInt("sys_status", -1); // "資料狀態

		}

		// ******* 放入包裝(body) [01
		// 是排序][_b__***********資料顯示在SEARCH頁面下的TABLE表單資料***********
		// 是分割直][資料庫欄位名稱]
		OqcInspectionForms = oifDao.findByoifowAndoifcnameAndoifonb(oif_ow, oif_c_name, oif_o_nb, sys_status);
		JSONArray object_bodys = new JSONArray();
		OqcInspectionForms.forEach(one -> {
			int ord = 0;
			JSONObject object_body = new JSONObject();
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_id", one.getOifid()); // ID
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_ow", one.getOifow()); // 工單
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_name", one.getOifcname()); // 客戶名稱
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_o_nb", one.getOifonb()); // 訂單號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_nb", one.getOifpnb());// 產品號
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_name", one.getOifpname());// 產品名
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_model", one.getOifpmodel()); // 產品型號
			// 暫時取消 object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_specification",
			// one.getOifpspecification()); // 產品規格

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_sn", one.getOifpsn()); // 產品序號區間
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_qty", one.getOifpqty()); // 出貨數
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_t_qty", one.getOiftqty()); // 抽樣數
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_p_ver", one.getOifpver()); // 版本資訊
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_title", one.getOiftitle()); // 標題值

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_date", one.getOifcdate());// 製表日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_c_user", one.getOifcuser());// 製表人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_e_date", one.getOifedate()== null ? "" :one.getOifedate());// 最後檢驗日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_e_user", one.getOifeuser()== null ? "" :one.getOifeuser());// 最後檢驗人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_f_date", one.getOiffdate() == null ? "" :one.getOiffdate());// 審核日
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_f_user", one.getOiffuser() == null ? "" :one.getOiffuser());// 審核人

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oif_oii_data", one.getOifoiidata());// "配置的檢驗項目

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
		check = true;
		try {
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
			}

			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
			
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單 ************* 給modify使用**** 但 權限一般QC人員目前被設定無法使用 ************
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		try {
			JSONArray list = body.getJSONArray("modify");
			OqcInspectionForm oIF = new OqcInspectionForm();
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
//				oIF = oifDao.findAllByOifid(data.getLong("oif_id")).get(0);	
//				oIF.setSysstatus(data.getInt("sys_status")); // 資料狀態
//				oIF.setSysmdate(new Date());// 修改時間
//				oIF.setSysmuser(user.getSuaccount());// 修改者(帳號)
//
//				oifDao.save(oIF);
//				check = true;
				
			}
			resp.setError_ms("未開放此功能");
			resp.autoMsssage("109"); // 回傳錯誤訊息
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 移除 資料清單****************不能刪除已審核的單據 *********
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
					
				//再增加判斷是否已審核 有審核不能刪除
				Long status= data.optLong("sys_status");
				if(status==1) {
					resp.setError_ms("已結單無法刪除");
					resp.autoMsssage("109"); // 回傳錯誤訊息
					check = false ;					
					return check;
				}else if(status==2) {
					resp.setError_ms("已審核無法刪除");
					resp.autoMsssage("109"); // 回傳錯誤訊息
					check = false ;
					return check;
				}else if(status==3) {
					resp.setError_ms("標示作廢無法刪除");
					resp.autoMsssage("109"); // 回傳錯誤訊息
					check = false ;
					return check;
				}
				
				Long dataID = data.getLong("oif_id");
			
				//*********要清除產敏細節的 OQC檢驗的內容 的資訊*********
				// 用工單取出<ProductionRecords>訂單規格的資料 
				String oif_ow=data.optString("oif_ow");
				List<ProductionRecords> prs = prDao.findAllByPrid(oif_ow, null);
				ProductionRecords pr=prs.get(0); //取出第一筆table表
				List<ProductionHeader> phs = headerDao.findAllByProductionRecords(pr);//用table表 取出製令內容
				ProductionHeader ph = phs.get(0); // 取出第一筆製令內容
				Long xx=ph.getPhpbgid(); //工單代號id
				List<ProductionBody>pbs=bodyDao.findAllByOldAndPbgid(null,xx);	
				//**清除這張工單在產品細節的欄位 OQC檢驗的內容 的資料**
				for(ProductionBody pb : pbs) {
					pb.setPblnoteoqc("");
					bodyDao.save(pb);
				}	

				if(orlDao.existsById(dataID)) { //刪前先確認資料存在
					orlDao.deleteByOrloifid(dataID); //刪除登記機台資料
				}
				if(oifDao.existsById(dataID)) { //刪前先確認資料存在
					oifDao.deleteById(dataID);		//刪除from表單	
				}
						
			}
			check = true;
		//	resp.setError_ms("未開放此功能");
		//	resp.autoMsssage("109"); // 回傳錯誤訊息
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}

	// ==============客製化==============

	// 取得 - Customized 當前表單式查詢資料 (資料庫有資料存在-取出資料 或 開始建立 新檢測單據)
	@Transactional
	public boolean getDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
	//	String rmauser = user.getSuname();
		boolean check = false;
		List<OqcInspectionForm> oifs;
		List<ProductionRecords> prs;
		List<OqcInspectionItems> oiis;
		String oifow = null;
		// String ow = null;
		String title = null;

		// 建立 object_body 物件 為最上層, 用於存放各個資訊
		JSONObject object_body = new JSONObject();

		// 建立空的JSONObject 的object_detail物件 用來存放資料
		JSONObject object_details = new JSONObject();

		// 建立空的JSONObject 的object_detail物件 用來存放資料
		JSONObject object_detail = new JSONObject();
		try {
			if (body.getJSONObject("search").has("input_oifow")) {
				// 取的資料庫已建立的OQC檢驗資料
				oifow = body.getJSONObject("search").getString("input_oifow");// 取得工單號碼
				oifs = oifDao.findByOifow(oifow);
				// 避免空指針與陣列越界的通用方法 這種寫法在一些大專案中可以讓你寫得更安全。
//			if (Optional.ofNullable(oifs).orElse(Collections.emptyList()).isEmpty()) {
//			    System.out.println("no data");
//			    resp.autoMsssage("102"); // 回傳錯誤訊息
//				return check;
//			}		
				if (oifs == null || oifs.isEmpty()) {			
					resp.autoMsssage("102"); // 回傳錯誤訊息("no data")
					return check;
				} else {
					if (oifs.get(0) != null) {						
						// 正常處理資料 *************資料庫有資料存在-取出資料
						OqcInspectionForm oif = oifs.get(0);

						object_detail.put("oif_id", oif.getOifid()); // key
						object_detail.put("pr_id", oif.getOifow()); // 工單
						object_detail.put("ph_c_name", oif.getOifcname());// 客戶名稱
						object_detail.put("ph_order_id", oif.getOifonb());// 訂單號碼
						object_detail.put("pr_bom_id", oif.getOifpnb());// 產品料號 BOM料號(公司)
						object_detail.put("pr_p_model", oif.getOifpmodel());// 產品型號						
						object_detail.put("pr_p_name", oif.getOifpname());// 產品規格 取至 MES製令內容的產品品名(規格)						
						object_detail.put("oif_p_sn", oif.getOifpsn());// 產品序號區間
						object_detail.put("ph_p_qty", oif.getOifpqty());// 出貨數 預計生產數
						object_detail.put("oif_t_qty", oif.getOiftqty());// 抽樣數
						object_detail.put("oif_oii_data", oif.getOifoiidata());// 配置的檢驗項目 JSON
						object_detail.put("oif_oii_form", oif.getOifoiiform());// 配置的HTML form項目
						object_detail.put("oif_p_ver", oif.getOifpver()); // 版本資訊
						object_detail.put("oif_title", oif.getOiftitle()); // 標題值
						title = oif.getOiftitle();

						object_detail.put("oif_c_user", oif.getOifcuser());// 製表人
						object_detail.put("oif_c_date", Fm_Time.to_yMd_Hms(oif.getOifcdate()));// 製表日
						
						object_detail.put("oif_e_user", oif.getOifeuser());// 最後鑑驗人						
						if(oif.getOifedate()!=null) { //防止空值引發錯誤
							object_detail.put("oif_e_date", Fm_Time.to_yMd_Hms(oif.getOifedate()));// 最後鑑驗日
						}	
						
						object_detail.put("oif_f_user", oif.getOiffuser());// 審核人						
						if (oif.getOiffdate()!=null) {
							object_detail.put("oif_f_date", Fm_Time.to_yMd_Hms(oif.getOiffdate()));// 審核日
						}						
					
						object_detail.put("sys_note", oif.getSysnote());//備註						
						object_detail.put("sys_status", oif.getSysstatus());// 資料狀態
						long x= oif.getSysstatus();
						if(x==2) {
							resp.setError_ms("已審核過，無法編輯 製表檢驗單 ");
							resp.autoMsssage("109"); // 回傳錯誤訊息
						}else if(x==3){
							resp.setError_ms("被標示作廢");
							resp.autoMsssage("109"); // 回傳錯誤訊息
						}

						System.out.println("往前端送資料");
						object_details.put("detail", object_detail);
						check = true;
					}
					/////////////// **********************************///////////////////////////////
					// 把基本檢測項目丟到前端 檢測項目編輯的table供後續OQC編輯檢驗
					JSONArray object_oiiitems = new JSONArray();
					// if (prs.size() > 0) {
					oiis = oIIDao.findAllByOiititleval(title, Sort.by(Sort.Direction.ASC, "syssort"));

					oiis.forEach(oii -> {
					//	int ord = 0;
						JSONObject object_oiiitem = new JSONObject();
						
						object_oiiitem.put("oii_check_name", oii.getOiicheckname());// 檢查項目名稱
						object_oiiitem.put("oii_check_val", oii.getOiicheckval());// 檢查內容值
						object_oiiitem.put("oii_check_type", oii.getOiichecktype());// 檢查輸入類型 0.空白 1.一般入 2.下拉式選單 3.勾選式
						object_oiiitem.put("oill_check_options", oii.getOiicheckoptions());// 可自訂值如果是下拉式/勾選 請用,區隔 Ex:[key_val,key_val]
						object_oiiitems.put(object_oiiitem);
				
					});
					object_details.put("oiiitems", object_oiiitems);
				}

			} else {
				//      **********************   開始建立 新檢測單據   *********************
				// 第1步驟 先確認是否重複 ,取出 工單基本資料	

				oifow = body.getJSONObject("search").getString("input_ow");// 取得工單號碼
				title = body.getJSONObject("search").getString("input_title");// 取得標題

				oifs = oifDao.findByOifow(oifow); // 要先確認Form表單資庫有無資料
				if (oifs.size() > 0) {
			
					resp.setError_ms("此工單號[" + oifow + "] 已經被使用-無法重複建立OQC製表檢驗單");
					resp.autoMsssage("107"); // 回傳錯誤訊息
					return check;
				}

				// 用工單取出<ProductionRecords>訂單規格的資料
				prs = prDao.findAllByPrid(oifow, null);
				// 判斷有無此工單號碼存在
				if (prs.isEmpty()) {
					resp.autoMsssage("102"); // 回傳錯誤訊息("無資料存在");
				}
				// prs.get(0);
				if (prs.size() > 0) {
					ProductionRecords pr = prs.get(0); // 取出第一筆訂單規格的資料
					List<ProductionHeader> phs = headerDao.findAllByProductionRecords(pr);// 取出製令內容
					ProductionHeader ph = phs.get(0); // 取出第一筆製令內容

					Long phOrder = ph.getPhpbgid(); // 取出工單對應的SN關聯表
					// 查詢工單裡面的轉單old機台數量(查詢SN重複+群組)
					List<ProductionBody> pbs = bodyDao.findAllByOldAndPbgid("old", phOrder);
					object_detail.put("oif_q_old", pbs.size());// 轉單old機台數量
					
					object_detail.put("oif_title", title);// 標題
					object_detail.put("pr_id", pr.getPrid());// 工單
					object_detail.put("ph_c_name", ph.getPhcname());// 客戶名稱
					object_detail.put("ph_order_id", ph.getPhorderid());// 訂單號碼
					object_detail.put("pr_bom_id", pr.getPrbomid());// 產品料號 BOM料號(公司)
					object_detail.put("pr_p_model", pr.getPrpmodel());// 產品型號
					object_detail.put("pr_p_name", pr.getPrname());// 產品規格 MES製令內容的產品品名(規格)
					// 產品規格 先不寫

					object_detail.put("oif_p_sn", pr.getPrssn() + "~" + pr.getPresn());// 產品序號區間
					object_detail.put("ph_p_qty", ph.getPhpqty());// 出貨數 預計生產數

					String prsitem = pr.getPrsitem(); // 1. 假設回傳型別是 String
					String prbitem = pr.getPrbitem(); // 1.假設回傳型別是 String
					// 將字串包成 JSONArray (模擬前端 ["..."] 的效果)
					JSONArray prsArr = new JSONArray("[" + prsitem + "]");
					JSONArray prbArr = new JSONArray("[" + prbitem + "]");

					List<String> lines = new ArrayList<>();
					// *************************** 版本資訊 **************************************
					lines.add("\n" + "*************************  軟體定義  *************************");
					// 處理 prsitem
					for (int i = 0; i < prsArr.length(); i++) {
						JSONObject obj = prsArr.getJSONObject(i);
						for (String key : obj.keySet()) {
							JSONObject inner = obj.getJSONObject(key);
							String isVal = (inner.has("Is") ? inner.getString("Is") : "N/A").trim();
							isVal = isVal.isEmpty() ? "N/A" : isVal;
							lines.add("\n" + key + " : " + isVal);
						}
					}
					lines.add("\n" + "*************************  規格定義  *************************");
					// 處理 prbitem
					for (int i = 0; i < prbArr.length(); i++) {
						JSONObject obj = prbArr.getJSONObject(i);
						for (String key : obj.keySet()) {
							JSONObject inner = obj.getJSONObject(key);
							String isVal = (inner.has("Is") ? inner.getString("Is") : "N/A").trim();
							isVal = isVal.isEmpty() ? "N/A" : isVal;
							lines.add("\n" + key + " : " + isVal); 
						}
					}
					// 將結果放入 object_detail
					object_detail.put("oif_p_ver", lines);	//版本資訊			
					object_detail.put("oif_c_user", user.getSuename());/// 製表人
				
					object_details.put("detail", object_detail);
					
				}

				// 第2步驟 把基本檢測項目丟到前端 檢測項目編輯的table供後續OQC編輯檢驗
				JSONArray object_oiiitems = new JSONArray();				
				oiis = oIIDao.findAllByOiititleval(title, Sort.by(Sort.Direction.ASC, "syssort"));

				oiis.forEach(oii -> {				
					JSONObject object_oiiitem = new JSONObject();
					object_oiiitem.put("oii_check_name", oii.getOiicheckname());// 檢查項目名稱
					object_oiiitem.put("oii_check_val", oii.getOiicheckval());// 檢查內容值
					object_oiiitem.put("oii_check_type", oii.getOiichecktype());// 檢查輸入類型 0.空白 1.一般入 2.下拉式選單 3.勾選式
					object_oiiitem.put("oill_check_options", oii.getOiicheckoptions());// 可自訂值如果是下拉式/勾選 請用,區隔 Ex:[key_val,key_val]

					object_oiiitems.put(object_oiiitem);
				
				});
				object_details.put("oiiitems", object_oiiitems);
			
			}
			check = true;
			object_body.put("Customized_detail", object_details);
			resp.setBody(object_body);
			
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// ==============客製化==============

	// 更新/新增 資料清單 Customized mode
	@Transactional // 存RMA維修資料johnny
	public boolean updateDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		String oifow = body.getJSONObject("oqc").getString("oif_ow");// 取得工單號碼
		List<OqcInspectionForm> oifs = oifDao.findByOifow(oifow); // 要先確認Form表單資庫有無資料
		OqcInspectionForm oIF = new OqcInspectionForm();
		JSONObject title = body.getJSONObject("oqc");
		String oif_oii_data = body.getJSONObject("oqc").getJSONArray("oif_oii_data").toString();

		try {
			if (oifs.size() > 0) {
				System.out.println("資料已存在進行更新");
				// ******************* 更新資料
				oIF = oifs.get(0);

				int status = oIF.getSysstatus();// 抓取資料庫狀態值
				if (status ==2 ) { // 大於0不可更改
					resp.setError_ms("此工單號[" + oifow + "] 已經審核後鎖定,如需修正請洽QC主管");
					resp.autoMsssage("108"); // 回傳錯誤訊息 資料已鎖定或作廢
					return check;
				}else if(status ==3) {
					resp.setError_ms("此工單號[" + oifow + "] 已經作廢,如需修正請洽QC主管");
					resp.autoMsssage("108"); // 回傳錯誤訊息 資料已鎖定或作廢
					return check;
				}	// 資料狀態
	
				// oIF.setOifow(title.getString("oif_ow")); // 工單
				oIF.setOifcname(title.getString("oif_c_name")); // 客戶名稱
				oIF.setOifonb(title.getString("oif_o_nb")); // 訂單號
				oIF.setOifpnb(title.getString("oif_p_nb")); // 產品料號
				oIF.setOifpname(title.getString("oif_p_name")); // 產品名稱
				oIF.setOifpmodel(title.getString("oif_p_model")); // 產品品名(產品型號)
				// oIF.setOifpspecification(title.getString("oif_p_specification").toString());
				// //產品規格 (暫時取消)
				oIF.setOifpsn(title.getString("oif_p_sn")); // 產品序號區間
				oIF.setOifpqty(title.optInt("oif_p_qty")); // 出貨數
				oIF.setOiftqty(title.optInt("oif_t_qty")); // 抽樣數
				oIF.setOifpver(title.getString("oif_p_ver")); // 版本資訊 JSON 格式:
				// 誰修改就換是誰為制表人 ,(但最先創建製表人的資料是不會變) ( 採用)
				oIF.setOifcdate(new Date()); // 製表日期
				oIF.setOifcuser(user.getSuaccount()); // 製表人
				
				//後鑑驗人只用在 誰按 結單按鈕 就是 最後鑑驗人
			//	oIF.setOifedate(new Date()); // 最後鑑驗日
			//	oIF.setOifeuser(user.getSuaccount()); // 最後鑑驗人
				oIF.setSysnote(title.getString("sys_note"));//備註

				oIF.setOifoiidata(oif_oii_data); // 配置的檢驗項目 //配置的檢驗項目 JSON

				// "="被吃掉，是因為「沒有進行編碼 (encoding) 就把 HTML 放進 JSON 傳送」。
				// 後端：收到後要 decode
				String htmlEncoded = title.getString("oif_oii_form");
				String html = URLDecoder.decode(htmlEncoded, "UTF-8"); // 還原回 HTML
				
				oIF.setOifoiiform(html); // oif_oii_form:原始的HTML項目

				oIF.setSysmdate(new Date());// 修改時間
				oIF.setSysmuser(user.getSuaccount());// 修改者(帳號)		

			} else {
				// *************************新增資料************************

				oIF.setOifow(title.getString("oif_ow")); // 工單
				oIF.setOifcname(title.getString("oif_c_name")); // 客戶名稱
				oIF.setOifonb(title.getString("oif_o_nb")); // 訂單號
				oIF.setOifpnb(title.getString("oif_p_nb")); // 產品料號
				oIF.setOifpname(title.getString("oif_p_name")); // 產品名稱
				oIF.setOifpmodel(title.getString("oif_p_model")); // 產品品名(產品型號)

				oIF.setOifpsn(title.getString("oif_p_sn")); // 產品序號區間
				oIF.setOifpqty(title.optInt("oif_p_qty")); // 出貨數
				oIF.setOiftqty(title.optInt("oif_t_qty")); // 抽樣數
				oIF.setOifpver(title.getString("oif_p_ver")); // 版本資訊 JSON 格式:

				oIF.setOiftitle(title.getString("oif_title")); // 標題值
				
				oIF.setSysnote(title.getString("sys_note"));//備註
				
				oIF.setOifcdate(new Date()); // 製表日期
				oIF.setOifcuser(user.getSuaccount()); // 製表人
				
				//後鑑驗人只用在 誰按 結單按鈕 就是 最後鑑驗人
				//oIF.setOifedate(new Date()); // 最後鑑驗日	
				//oIF.setOifeuser(user.getSuaccount()); // 最後鑑驗人

				oIF.setOifoiidata(oif_oii_data); // 配置的檢驗項目 //配置的檢驗項目 JSON

				// "="被吃掉，是因為「沒有進行編碼 (encoding) 就把 HTML 放進 JSON 傳送」。
				// 後端：收到後要 decode
				String htmlEncoded = title.getString("oif_oii_form");
				String html = URLDecoder.decode(htmlEncoded, "UTF-8"); // 還原回 HTML
		
				oIF.setOifoiiform(html); // oif_oii_form:原始的HTML項目

				oIF.setSyscdate(new Date());// 創建時間
				oIF.setSyscuser(user.getSuaccount());// 創建者(帳號)
				oIF.setSysmdate(new Date());// 修改時間
				oIF.setSysmuser(user.getSuaccount());// 修改者(帳號)
			}
			oifDao.save(oIF);

			check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// ************S3 結案按鈕 (對 "工單" 作結單)(更改 "通用-製令內容"裡的狀態為"已完成"***** ******************** Customized
	// mode
	@Transactional // 
	public boolean reviewCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		String oifow = body.getJSONObject("oqc").getString("oif_ow");// 取得工單號碼

		List<OqcInspectionForm> oifs = oifDao.findByOifow(oifow); // 要先確認Form表單資庫有無資料
		OqcInspectionForm oIF = new OqcInspectionForm();	

		JSONObject title = body.getJSONObject("oqc");
	//	String oif_oii_data = body.getJSONObject("oqc").getJSONArray("oif_oii_data").toString();

		try {
			if (oifs.size() > 0) {
				System.out.println("資料已存在進行更新");
				// ******************* 更新資料
				oIF = oifs.get(0);

				int status = oIF.getSysstatus();// 抓取資料庫狀態值
				if (status >= 2) { // 大於1不可更改
					resp.setError_ms("此工單號[" + oifow + "] 已經審核後鎖定,如需修正請洽QC主管");
					resp.autoMsssage("108"); // 回傳錯誤訊息 資料已鎖定或作廢
					return check;
				} // 資料狀態

				//*********************** 計算指定工單號碼下，每一個測試項目 每個SN的最後一筆檢查結果為 PASS 的數量。 ****************************************			
				//long count = orlDao.countLastPassByOrlow(oifow);
				String orltitem="功能(測試OS)";
				long count1 = orlDao.countLastPassByOrlowAndOrltitem(oifow,orltitem);
				orltitem="功能(T2 OS)";
				long count2 = orlDao.countLastPassByOrlowAndOrltitem(oifow,orltitem);
				long count =count1+count2;
				orltitem="外觀/包裝檢驗";
				long count3 = orlDao.countLastPassByOrlowAndOrltitem(oifow,orltitem);	
							
				long oty=title.optInt("oif_t_qty");
				
				if (oty > count) {
					resp.setError_ms("此工單『功能』檢驗數未達抽樣數量,不能結單");
					resp.autoMsssage("109"); // 回傳錯誤訊息 資料已鎖定或作廢
					return check;
				}else if(oty > count3) {
					resp.setError_ms("此工單『外觀/包裝檢驗』檢驗數未達抽樣數量,不能結單");
					resp.autoMsssage("109"); // 回傳錯誤訊息 資料已鎖定或作廢
					return check;
				}
				
				//*********************************************************************		
			//	oIF.setOifcname(title.getString("oif_c_name")); // 客戶名稱
			//	oIF.setOifonb(title.getString("oif_o_nb")); // 訂單號
			//	oIF.setOifpnb(title.getString("oif_p_nb")); // 產品料號
			//	oIF.setOifpname(title.getString("oif_p_name")); // 產品名稱
			//	oIF.setOifpmodel(title.getString("oif_p_model")); // 產品品名(產品型號)
			//	oIF.setOifpsn(title.getString("oif_p_sn")); // 產品序號區間
			//	oIF.setOifpqty(title.optInt("oif_p_qty")); // 出貨數
			//	oIF.setOiftqty(title.optInt("oif_t_qty")); // 抽樣數
			//	oIF.setOifpver(title.getString("oif_p_ver")); // 版本資訊 JSON 格式:
				oIF.setSysstatus(1); //0:正常 1:已結單
				oIF.setOifedate(new Date()); // 最後鑑驗日	
				oIF.setOifeuser(user.getSuaccount()); // 最後鑑驗人
			//	oIF.setSysnote(title.getString("sys_note"));//備註				
			//	oIF.setOifoiidata(oif_oii_data); // 配置的檢驗項目 //配置的檢驗項目 JSON
				// "="被吃掉，是因為「沒有進行編碼 (encoding) 就把 HTML 放進 JSON 傳送」。
				// 後端：收到後要 decode
			//	String htmlEncoded = title.getString("oif_oii_form");
			//	String html = URLDecoder.decode(htmlEncoded, "UTF-8"); // 還原回 HTML
			//	System.out.println("接收到的 HTML: " + html);
			//	oIF.setOifoiiform(html); // oif_oii_form:原始的HTML項目
				oIF.setSysmdate(new Date());// 修改時間
				oIF.setSysmuser(user.getSuaccount());// 修改者(帳號)
			}else {
				resp.setError_ms("此工單號[" + oifow + "]製表鑑驗單尚未建立在資料庫中");
				resp.autoMsssage("108"); // 回傳錯誤訊息 資料已鎖定或作廢
				return check;
			}
			oifDao.save(oIF);
			
			//****************************************對 工單制令作結單動作 *************************
			// 用工單取出<ProductionRecords>訂單規格的資料 
			String oif_ow=title.optString("oif_ow");
			List<ProductionRecords> prs = prDao.findAllByPrid(oif_ow, null);
			ProductionRecords pr=prs.get(0); //取出第一筆table表
			List<ProductionHeader> phs = headerDao.findAllByProductionRecords(pr);//用table表 取出製令內容
			ProductionHeader ph = phs.get(0); // 取出第一筆製令內容
			ph.setSysstatus(2);  //2:已完成( 為結單)
			//對製令內容 修改人與時間做更正
			ph.setSysmdate(new Date());
			ph.setSysmuser(user.getSuaccount()+"("+user.getSuname()+")");
			headerDao.save(ph);		
			
			check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

}
