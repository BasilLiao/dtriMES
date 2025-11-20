package dtri.com.tw.service;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class ProductionBodyService {
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private ProductionHeaderDao headerDao;

	@Autowired
	private EntityManager em;

	// 取得當前 資料清單
	@SuppressWarnings("unchecked")
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<ProductionBody> productionBodies = new ArrayList<ProductionBody>();
		ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0); //取得pbid=0的所有資料 然後再取締一筆資料來用
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		// 查詢預設
		String prid = "";
		String phmodel = "";
		String sysstatus = "0";
		String pb_sn_value = "";
		String pb_sn_name = "";
		String pb_w_value = "";
		String pb_w_name = "";
		String pb_w_p_date = "";
		String pb_sn = "";
		String pb_b_sn = "";
		String pb_old_sn = "";
		String pb_sn_check = null;
		String pb_sn_date_s = "";
		String pb_sn_date_e = "";
		String pb_w_p_date_s = "";
		String pb_w_p_date_e = "";
		// List<Long> pbid = new ArrayList<Long>();
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_id", FFS.h_t("SN_ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_g_id", FFS.h_t("SN_G_ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_id", FFS.h_t("TL_ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_id", FFS.h_t("工單號", "160px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_b_sn", FFS.h_t("SN_(燒錄/產品)", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_sn", FFS.h_t("SN_(身分/產品)", "160px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_old_sn", FFS.h_t("SN_(身分/產品)[舊]", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_p_model", FFS.h_t("產品型號", "160px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ph_order_id", FFS.h_t("訂單號", "160px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_bom_id", FFS.h_t("BOM(公司)", "160px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pr_bom_c_id", FFS.h_t("BOM(客戶)", "160px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_shipping_date", FFS.h_t("出貨日(預計)", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_recycling_date", FFS.h_t("回收日", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_position", FFS.h_t("最後位置", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_w_years", FFS.h_t("保固年", "120px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_f_value", FFS.h_t("故障項目", "250px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_f_note", FFS.h_t("故障說明", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_check", FFS.h_t("完成?", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_useful_sn", FFS.h_t("狀態", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_l_path", FFS.h_t("檢測Log位置", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_l_size", FFS.h_t("檢測Log大小", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_l_dt", FFS.h_t("上傳Log時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_l_note_oqc", FFS.h_t("OQC檢驗的內容", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "pb_schedule", FFS.h_t("過站設定", "150px", FFM.Wri.W_Y));

			// sn關聯表
			int j = 0;
			Method method;
			for (j = 0; j < 50; j++) {
				String m_name = "getPbvalue" + String.format("%02d", j + 1);
				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = "pb_value" + String.format("%02d", j + 1);
					if (value != null && !value.equals("")) {
						object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + name, FFS.h_t("SN_[" + value + "]", "250px", FFM.Wri.W_Y));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 過站簽名
			for (j = 0; j < 20; j++) {
				String m_name = "getPbwname" + String.format("%02d", j + 1);

				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = "pb_w_name" + String.format("%02d", j + 1);

					String value_date = (String) method.invoke(body_one);
					String name_date = "pb_w_p_date" + String.format("%02d", j + 1);
					if (value != null && !value.equals("")) {
						object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + name, FFS.h_t("過站簽名[" + value + "]", "270px", FFM.Wri.W_Y));
						object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + name_date, FFS.h_t("過站時間[" + value_date + "]", "270px", FFM.Wri.W_Y));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", "sys_header");

			bean.setHeader(object_header);

			// 放入報告 [m__(key)](Analysis report) 格式-是否需要顯示
			JSONObject object_analysis = new JSONObject();
			object_analysis.put("pb_id", FFM.Wri.W_N);
			object_analysis.put("pb_g_id", FFM.Wri.W_N);
			object_analysis.put("ph_id", FFM.Wri.W_N);
			object_analysis.put("sys_m_date", FFM.Wri.W_N);
			object_analysis.put("sys_c_date", FFM.Wri.W_N);
			object_analysis.put("pb_sn", FFM.Wri.W_N);

			object_analysis.put("pb_recycling_date", FFM.Wri.W_N);
			object_analysis.put("sys_m_user", FFM.Wri.W_N);
			object_analysis.put("sys_c_user", FFM.Wri.W_N);
			object_analysis.put("pb_schedule", FFM.Wri.W_N);
			object_analysis.put("pb_f_note", FFM.Wri.W_N);
			object_analysis.put("pb_l_size", FFM.Wri.W_N);
			object_analysis.put("pb_l_path", FFM.Wri.W_N);
			object_analysis.put("pb_useful_sn", FFM.Wri.W_N);
			// object_analysis.put("pb_position", FFM.Wri.W_N);
			object_analysis.put("sys_note", FFM.Wri.W_N);
			object_analysis.put("sys_sort", FFM.Wri.W_N);
			object_analysis.put("sys_ver", FFM.Wri.W_N);
			object_analysis.put("sys_status", FFM.Wri.W_N);
			object_analysis.put("sys_header", FFM.Wri.W_N);
			object_analysis.put("sys_sort", FFM.Wri.W_N);

			bean.setAnalysis(object_analysis);

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray a_val = new JSONArray();
			JSONArray n_val = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pb_id", "SN_ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "pb_g_id", "TL_S_ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "ph_id", "TL_ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pr_p_model", "產品型號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "pr_id", "工單號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "pb_sn", "SN_(身分/產品)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "pb_b_sn", "SN_(燒錄/產品)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "3", "3", FFM.Wri.W_N, "col-md-1", true, a_val, "pb_w_years", "保固年份"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "完成").put("key", "true"));
			a_val.put((new JSONObject()).put("value", "未完成").put("key", "false"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "false", "false", FFM.Wri.W_Y, "col-md-1", true, a_val, "pb_check", "工作流程"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "有效").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "已出貨").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "可拆解").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "已失效").put("key", "3"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "false", "false", FFM.Wri.W_Y, "col-md-1", true, a_val, "pb_useful_sn", "產品狀態"));
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "sys_status", "系統狀態"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "請小心填寫:格式Ex:[\"81TW12242C077_old_beginning\"]", "", FFM.Wri.W_Y, "col-md-12", false,
					a_val, "pb_old_sn", "SN_[舊](燒錄/產品)"));
			// sn關聯表
			for (j = 0; j < 50; j++) {
				String m_name = "getPbvalue" + String.format("%02d", j + 1);
				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = "pb_value" + String.format("%02d", j + 1);
					if (value != null && !value.equals("")) {
						obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, name, value));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", true, n_val, "pb_f_value", "故障項目"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", true, n_val, "pb_f_note", "故障說明"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pb_schedule", "過站狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "pb_l_path", "檢測Log位置"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pb_l_size", "檢測Log大小"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "pb_l_dt", "上傳Log時間"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "pb_l_note_oqc", "OQC檢驗的內容"));
			
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-8", false, n_val, "pb_position", "最後位置"));		
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", "備註"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", "版本"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_date", "建立時間"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_c_user", "建立人"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", "修改時間"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", "修改人"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序"));

			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_id", "製令單號", n_val));

			// 項目查詢(選單)
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pb_b_sn", "SN_(燒錄/產品)序號", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pb_old_sn", "SN_[舊](燒錄/產品)序號", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "sys_status", "系統狀態", a_val));
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "完成").put("key", "true"));
			a_val.put((new JSONObject()).put("value", "未完成").put("key", "false"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "pb_sn_check", "SN_完成?", a_val));
			// SN
			a_val = new JSONArray();
			for (j = 0; j < 50; j++) {
				String m_name = "getPbvalue" + String.format("%02d", j + 1);
				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = String.format("pb_value" + String.format("%02d", j + 1));
					if (value != null && !value.equals("")) {
						a_val.put((new JSONObject()).put("value", value).put("key", name));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-2", "pb_sn_name", "SN_類型", a_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pb_sn_value", "SN_值", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "pr_p_model", "產品型號", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "pb_sn_date_s", "修改時間(始)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "", "col-md-2", "pb_sn_date_e", "修改時間(終)", n_val));

			a_val = new JSONArray();
			for (j = 0; j < 20; j++) {
				String m_name = "getPbwname" + String.format("%02d", j + 1);

				try {
					method = body_one.getClass().getMethod(m_name);
					String value = (String) method.invoke(body_one);
					String name = String.format("pb_w_name" + String.format("%02d", j + 1));
					if (value != null && !value.equals("")) {
						a_val.put((new JSONObject()).put("value", value).put("key", name));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "pb_w_name", "工作站類型", a_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "過站人員", "col-md-1", "pb_w_value", "過站人", n_val));

			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "過站簽名 或 過站時間(選其一)", "col-md-2", "pb_w_p_date_s", "過站時間(始)", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.DATE, "時間區間 (始)與(終) 都要填入", "col-md-2", "pb_w_p_date_e", "過站時間(終)", n_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			phmodel = body.getJSONObject("search").getString("pr_p_model");
			phmodel = (phmodel == null) ? "" : phmodel;

			prid = body.getJSONObject("search").getString("pr_id");
			prid = (prid == null) ? "" : prid;

			sysstatus = body.getJSONObject("search").getString("sys_status");
			sysstatus = (sysstatus.equals("")) ? "0" : sysstatus;

			pb_sn = body.getJSONObject("search").has("pb_sn") ? body.getJSONObject("search").getString("pb_sn") : null;
			pb_sn = (pb_sn == null) ? "" : pb_sn;

			pb_b_sn = body.getJSONObject("search").getString("pb_b_sn");
			pb_b_sn = (pb_b_sn == null) ? "" : pb_b_sn;

			pb_old_sn = body.getJSONObject("search").getString("pb_old_sn");
			pb_old_sn = (pb_old_sn.equals("")) ? "" : "%" + pb_old_sn + "%";

			pb_sn_name = body.getJSONObject("search").getString("pb_sn_name");
			pb_sn_name = pb_sn_name == null ? "" : pb_sn_name;

			pb_sn_value = body.getJSONObject("search").getString("pb_sn_value");
			pb_sn_value = pb_sn_value == null ? "" : pb_sn_value;
			// 過站類型
			pb_w_name = body.getJSONObject("search").getString("pb_w_name");
			pb_w_name = pb_w_name == null ? "" : pb_w_name;

			// 過站人
			pb_w_value = body.getJSONObject("search").getString("pb_w_value");
			pb_w_value = pb_w_value == null ? "" : pb_w_value;
			// 過站時間
			pb_sn_date_s = body.getJSONObject("search").getString("pb_sn_date_s");
			pb_sn_date_e = body.getJSONObject("search").getString("pb_sn_date_e");

			pb_w_p_date_s = body.getJSONObject("search").getString("pb_w_p_date_s");
			pb_w_p_date_e = body.getJSONObject("search").getString("pb_w_p_date_e");

			if (!pb_w_name.equals("")) {
				pb_w_p_date = "pb_w_p_date" + pb_w_name.substring(Math.max(pb_w_name.length() - 2, 0));
			}

			if (pb_sn_name.equals("") || pb_sn_value.equals("")) {
				pb_sn_value = "";
				pb_sn_name = "";
			}
			// 工作站類型+人
			if (pb_w_name.equals("") || pb_w_value.equals("")) {
				pb_w_value = "";
			}
			// 工作站類型+時間
			if (pb_w_name.equals("") || pb_w_p_date_s.equals("") || pb_w_p_date_e.equals("")) {
				pb_w_p_date = "";
				pb_w_p_date_s = "";
				pb_w_p_date_e = "";
			}

			pb_sn_check = body.getJSONObject("search").getString("pb_sn_check");
			pb_sn_check = (pb_sn_check.equals("")) ? null : pb_sn_check;
		}

		// 查詢SN欄位+產品型號+製令單號
		String nativeQuery = "SELECT b.* FROM production_body b " + //
				"join production_header h on b.pb_g_id = h.ph_pb_g_id " + //
				"join production_records p on h.ph_pr_id = p.pr_id WHERE ";
		
		if (!pb_sn_value.equals("")) {
			nativeQuery += " (:pb_sn_value='' or " + pb_sn_name + " LIKE :pb_sn_value) and ";
		}
		// 過站類型+過站人
		if (!pb_w_value.equals("")) {
			nativeQuery += " (:pb_w_value='' or " + pb_w_name + " LIKE :pb_w_value) and ";
		}
		// 過站類型+過站時間
		if (!pb_w_p_date_e.equals("") && !pb_w_p_date_e.equals("") && //
				!pb_w_p_date_s.equals("") && !pb_w_p_date_s.equals("")) {
			nativeQuery += " (" + pb_w_p_date + " BETWEEN  :pb_w_p_date_s  and :pb_w_p_date_e ) and ";
		}
		if (pb_sn_check != null) {
			nativeQuery += " (b.pb_check = :pb_check) and ";
		}
		if (!pb_sn_date_s.equals("") && !pb_sn_date_e.equals("")) {
			nativeQuery += " (b.sys_m_date BETWEEN '" + pb_sn_date_s + "'  and '" + pb_sn_date_e + "' ) and ";
		}
		nativeQuery += " (:pb_sn='' or b.pb_sn LIKE :pb_sn) and ";
		nativeQuery += " (:pb_b_sn='' or b.pb_b_sn LIKE :pb_b_sn) and ";
		nativeQuery += " (:pb_old_sn='' or b.pb_old_sn LIKE :pb_old_sn) and ";
		nativeQuery += " ( b.sys_status = :sys_status) and ";

		nativeQuery += " (:pr_p_model='' or p.pr_p_model LIKE :pr_p_model) and ";
		nativeQuery += " (:pr_id='' or h.ph_pr_id LIKE :pr_id) and ";
		nativeQuery += " (b.pb_g_id != 1) and (b.pb_g_id != 0)  order by b.pb_id desc ";
		nativeQuery += " LIMIT :limit OFFSET :offset ";
		Query query = em.createNativeQuery(nativeQuery, ProductionBody.class);
		if (!pb_sn_value.equals("")) {
			query.setParameter("pb_sn_value", "%" + pb_sn_value + "%");
		}
		if (!pb_w_value.equals("")) {
			query.setParameter("pb_w_value", "%" + pb_w_value + "%");
		}
		if (!pb_w_p_date_e.equals("") && !pb_w_p_date_e.equals("") && //
				!pb_w_p_date_s.equals("") && !pb_w_p_date_s.equals("")) {
			query.setParameter("pb_w_p_date_s", Fm_Time.toDateTime(pb_w_p_date_s));
			query.setParameter("pb_w_p_date_e", Fm_Time.toDateTime(pb_w_p_date_e));
		}

		query.setParameter("pb_b_sn", "%" + pb_b_sn + "%");
		query.setParameter("pb_old_sn", pb_old_sn);
		query.setParameter("sys_status", Integer.parseInt(sysstatus));
		query.setParameter("pb_sn", "%" + pb_sn + "%");
		query.setParameter("pr_p_model", "%" + phmodel + "%");
		query.setParameter("pr_id", "%" + prid + "%");
		if (pb_sn_check != null) {
			query.setParameter("pb_check", Boolean.parseBoolean(pb_sn_check));
		}

		query.setParameter("limit", p_size);
		query.setParameter("offset", page * p_size);

		// 轉換LONG
		productionBodies = query.getResultList();
		if (productionBodies.size() == 0) {
			bean.autoMsssage("102");
			return false;
		}
		em.clear();
		em.close();

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		productionBodies.forEach(one -> {
			JSONObject object_body = new JSONObject();
			ProductionHeader productionHeader = new ProductionHeader();
			int ord = 0;
			// 查詢最新的製令單
			productionHeader = headerDao.findTopByPhpbgidOrderBySysmdateDesc(one.getPbgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_id", one.getPbid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_g_id", one.getPbgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_id", productionHeader.getPhid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_id", productionHeader.getProductionRecords().getPrid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_b_sn", one.getPbbsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_sn", one.getPbsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_old_sn", one.getPboldsn() == null ? "" : one.getPboldsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_p_model", productionHeader.getProductionRecords().getPrpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_order_id", productionHeader.getPhorderid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_id", productionHeader.getProductionRecords().getPrbomid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_c_id", productionHeader.getProductionRecords().getPrbomcid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_shipping_date", one.getPbshippingdate() == null ? "" : one.getPbshippingdate());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_recycling_date", one.getPbrecyclingdate() == null ? "" : one.getPbrecyclingdate());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_position", one.getPbposition() == null ? "" : one.getPbposition());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_w_years", one.getPbwyears() == null ? "" : one.getPbwyears());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_f_value", one.getPbfvalue() == null ? "" : one.getPbfvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_f_note", one.getPbfnote() == null ? "" : one.getPbfnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_check", one.getPbcheck());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_useful_sn", one.getPbusefulsn());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_path", one.getPblpath() == null ? "" : one.getPblpath());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_size", one.getPblsize() == null ? "" : one.getPblsize());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_dt", one.getPbldt() == null ? "" : Fm_Time.to_yMd_Hms(one.getPbldt()));

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_note_oqc", one.getPblnoteoqc() == null ? "" : one.getPblnoteoqc()); //OQC標記檢驗內容
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_schedule", one.getPbschedule() == null ? "" : one.getPbschedule());
			try {
				// 有效設定的欄位
				for (int k = 0; k < 50; k++) {
					String in_name = "getPbvalue" + String.format("%02d", k + 1);
					Method in_method = body_one.getClass().getMethod(in_name);
					String value = (String) in_method.invoke(body_one);
					// 欄位有定義的顯示
					if (value != null && !value.equals("")) {
						// sn關聯表
						String name_b = "getPbvalue" + String.format("%02d", k + 1);
						Method method_b = one.getClass().getMethod(name_b);
						String value_b = (String) method_b.invoke(one);
						String key_b = "pb_value" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + key_b, (value_b == null ? "" : value_b));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// 有效設定的欄位
				for (int k = 0; k < 20; k++) {
					String in_name = "getPbwname" + String.format("%02d", k + 1);
					Method in_name_method = body_one.getClass().getMethod(in_name);
					String in_name_value = (String) in_name_method.invoke(body_one);

					// 欄位有定義的顯示
					if (in_name_value != null && !in_name_value.equals("")) {
						// sn關聯表(過站人)
						String name_b = "getPbwname" + String.format("%02d", k + 1);
						Method name_method_b = one.getClass().getMethod(name_b);
						String name_value_b = (String) name_method_b.invoke(one);
						String name_key_b = "pb_w_name" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + name_key_b, (name_value_b == null ? "" : name_value_b));
						// sn關聯表(過站時間)
						String date_b = "getPbwpdate" + String.format("%02d", k + 1);
						Method date_method_b = one.getClass().getMethod(date_b);
						String date_value_b = "";
						if ((Date) date_method_b.invoke(one) != null) {
							date_value_b = Fm_Time.to_yMd_Hms((Date) date_method_b.invoke(one));
						}
						String date_key_b = "pb_w_p_date" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + date_key_b, (date_value_b == null ? "" : date_value_b));

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());

			object_bodys.put(object_body);

		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		return true;
	}

	// 報表 查詢 資料清單
	@SuppressWarnings("unchecked")
	public boolean getReportData(PackageBean bean, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		// int p_size = req.getPage_total();
		boolean check = false;
		List<ProductionBody> productionBodies = new ArrayList<ProductionBody>();
		ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);

		JSONArray list = body.getJSONArray("search");
		String str = "";
		boolean first = true;
		// Step1.======= 轉換內容 bytes to string =======
		for (Object object : list) {
			String[] list_s = ((String) object).split(",");
			byte[] bytes = new byte[list_s.length];
			for (int i = 0; i < list_s.length; i++) {
				bytes[i] = (byte) (Integer.parseInt(list_s[i]) & 0xFF);
			}
			if (first) {
				first = false;
				String one = new String(bytes, StandardCharsets.UTF_8);
				if (one.indexOf("!= ''") > 0) {
					one = "(" + one + " AND " + one.replace("!= ''", "is not null ") + ")";
				} else if (one.indexOf("= ''") > 0) {
					one = "(" + one + " OR " + one.replace("= ''", " is null") + ")";
				}
				str += one;
			} else {
				String one = new String(bytes, StandardCharsets.UTF_8);
				if (one.indexOf("!= ''") > 0) {
					one = "(" + one + " AND " + one.replace("!= ''", "is not null ") + ")";
				} else if (one.indexOf("= ''") > 0) {
					one = "(" + one + " OR " + one.replace("= ''", " is null") + ")";
				}
				str += " AND " + one;
			}
		}
		// 取代共用參數
		str = str.replace("sys_", "b.sys_");
		str = str.replace("pr_", "p.pr_");
		str = str.replace("ph_", "h.ph_");

		// Step2.=======Analysis report 查詢SN欄位+產品型號+製令單號 =======
		String nativeQuery = "SELECT b.* FROM production_body b " + //
				"join production_header h on b.pb_g_id = h.ph_pb_g_id " + //
				"join production_records p on h.ph_pr_id = p.pr_id WHERE ";
		nativeQuery += str;
		nativeQuery += " order by b.pb_b_sn desc ";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		System.out.println(nativeQuery);
		try {
			Query query = em.createNativeQuery(nativeQuery, ProductionBody.class);
			productionBodies = query.getResultList();
			if (productionBodies.size() <= 0) {
				bean.autoMsssage("102");
				return false;
			}
			if (productionBodies.size() > 25000) {
				bean.autoMsssage("SH000");
				return false;
			}
		} catch (Exception e) {
			bean.autoMsssage("103");
			return false;
		}
		// Step3.======= 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱] =======
		JSONArray object_bodys = new JSONArray();
		productionBodies.forEach(one -> {
			JSONObject object_body = new JSONObject();
			ProductionHeader productionHeader = new ProductionHeader();
			int ord = 0;
			// 查詢最新的製令單
			productionHeader = headerDao.findTopByPhpbgidOrderBySysmdateDesc(one.getPbgid());
			// productionHeader.getPhid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_id", productionHeader.getProductionRecords().getPrid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_b_sn", one.getPbbsn());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_old_sn", one.getPboldsn() == null ? "" : one.getPboldsn());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_p_model", productionHeader.getProductionRecords().getPrpmodel());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ph_order_id", productionHeader.getPhorderid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_id", productionHeader.getProductionRecords().getPrbomid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pr_bom_c_id", productionHeader.getProductionRecords().getPrbomcid());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_shipping_date", one.getPbshippingdate() == null ? "" : Fm_Time.to_yMd_Hms(one.getPbshippingdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_position", one.getPbposition() == null ? "" : one.getPbposition());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_w_years", one.getPbwyears() == null ? "" : one.getPbwyears());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_f_value", one.getPbfvalue() == null ? "" : one.getPbfvalue());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_check", one.getPbcheck());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_dt", one.getPbldt() == null ? "" : Fm_Time.to_yMd_Hms(one.getPbldt()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "pb_l_note_oqc", one.getPblnoteoqc()); //"OQC檢驗的內容"
			try {
				// 有效設定的欄位
				for (int k = 0; k < 50; k++) {
					String in_name = "getPbvalue" + String.format("%02d", k + 1);
					Method in_method = body_one.getClass().getMethod(in_name);
					String value = (String) in_method.invoke(body_one);
					// 欄位有定義的顯示
					if (value != null && !value.equals("")) {
						// sn關聯表
						String name_b = "getPbvalue" + String.format("%02d", k + 1);
						Method method_b = one.getClass().getMethod(name_b);
						String value_b = (String) method_b.invoke(one);
						String key_b = "pb_value" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + key_b, (value_b == null ? "" : value_b));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// 有效設定的欄位
				for (int k = 0; k < 20; k++) {
					String in_name = "getPbwname" + String.format("%02d", k + 1);
					Method in_method = body_one.getClass().getMethod(in_name);
					String value = (String) in_method.invoke(body_one);
					// 欄位有定義的顯示
					if (value != null && !value.equals("")) {
						// sn關聯表
						String name_b = "getPbwname" + String.format("%02d", k + 1);
						Method method_b = one.getClass().getMethod(name_b);
						String value_b = (String) method_b.invoke(one);
						String key_b = "pb_w_name" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + key_b, (value_b == null ? "" : value_b));
						// sn關聯表(過站時間)
						String date_b = "getPbwpdate" + String.format("%02d", k + 1);
						Method date_method_b = one.getClass().getMethod(date_b);
						String date_value_b = "";
						if ((Date) date_method_b.invoke(one) != null) {
							date_value_b = Fm_Time.to_yMd_Hms((Date) date_method_b.invoke(one));
						}
						String date_key_b = "pb_w_p_date" + String.format("%02d", k + 1);
						object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + date_key_b, (date_value_b == null ? "" : date_value_b));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
			object_bodys.put(object_body);
		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		check = true;
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("create");
			for (Object one : list) {
				// 物件轉換
				ProductionBody p_body = new ProductionBody();
				JSONObject data = (JSONObject) one;
				ProductionRecords search = new ProductionRecords();
				search.setPrid(data.getString("pr_id"));
				List<ProductionHeader> p_Headers = headerDao.findAllByProductionRecords(search);
				List<ProductionBody> p_Bodys = bodyDao.findAllByPbbsn(data.getString("pb_b_sn"));
				// 有資料
				if (p_Headers.size() < 1) {
					return check;
				}
				// 重複?
				if (p_Bodys.size() > 0) {
					return check;
				}
				// ProductionBody
				p_body.setPbusefulsn(data.getInt("pb_useful_sn"));
				p_body.setPbsn(data.getString("pb_b_sn"));
				p_body.setPbbsn(data.getString("pb_b_sn"));
				p_body.setPbgid(p_Headers.get(0).getPhpbgid());
				p_body.setPbcheck(data.getBoolean("pb_check"));
				p_body.setPboldsn(data.getString("pb_old_sn"));
				p_body.setPboldsn("");
				p_body.setSysnote(data.getString("sys_note"));
				p_body.setSyssort(data.getInt("sys_sort"));
				p_body.setSysstatus(data.getInt("sys_status"));
				p_body.setSysmuser(user.getSuaccount());
				p_body.setSyscuser(user.getSuaccount());
				// SN類別
				try {
					for (int k = 0; k < 50; k++) {
						// 有欄位?
						if (data.has("pb_value" + String.format("%02d", k + 1))) {
							String value = data.getString("pb_value" + String.format("%02d", k + 1));
							String in_name = "setPbvalue" + String.format("%02d", k + 1);
							Method in_method = p_body.getClass().getMethod(in_name, String.class);
							// 欄位有值
							if (value != null && !value.equals("")) {
								in_method.invoke(p_body, value);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return check;
				}
				bodyDao.save(p_body);
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
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
				ProductionBody p_body = new ProductionBody();
				JSONObject data = (JSONObject) one;
				ProductionRecords search = new ProductionRecords();
				search.setPrid(data.getString("pr_id"));
				List<ProductionHeader> p_Headers = headerDao.findAllByProductionRecords(search);
				List<ProductionBody> p_Bodys = bodyDao.findAllByPbbsn(data.getString("pb_b_sn"));
				// 有資料
				if (p_Headers.size() < 1) {
					return check;
				}
				// 重複?
				if (p_Bodys.size() > 0) {
					return check;
				}

				p_body.setPbusefulsn(data.getInt("pb_useful_sn"));
				p_body.setPbsn(data.getString("pb_b_sn"));
				p_body.setPbbsn(data.getString("pb_b_sn"));
				p_body.setPboldsn("");
				p_body.setPboldsn(data.getString("pb_old_sn"));
				p_body.setPbgid(p_Headers.get(0).getPhpbgid());
				p_body.setPbcheck(data.getBoolean("pb_check"));
				p_body.setSysnote(data.getString("sys_note"));
				p_body.setSyssort(data.getInt("sys_sort"));
				p_body.setSysstatus(data.getInt("sys_status"));
				p_body.setSysmuser(user.getSuaccount());
				p_body.setSyscuser(user.getSuaccount());
				// SN類別
				try {
					for (int k = 0; k < 50; k++) {
						// 有欄位?
						if (data.has("pb_value" + String.format("%02d", k + 1))) {
							String value = data.getString("pb_value" + String.format("%02d", k + 1));
							String in_name = "setPbvalue" + String.format("%02d", k + 1);
							Method in_method = p_body.getClass().getMethod(in_name, String.class);
							// 欄位有值
							if (value != null && !value.equals("")) {
								in_method.invoke(p_body, value);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				bodyDao.save(p_body);
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("modify");
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				List<ProductionBody> p_Bodys = bodyDao.findAllByPbid(data.getLong("pb_id"));
				List<ProductionBody> check_Bodys = bodyDao.findAllByPbbsn(data.getString("pb_b_sn"));
				ProductionRecords search = new ProductionRecords();
				search.setPrid(data.getString("pr_id"));
				List<ProductionHeader> p_Headers = headerDao.findAllByProductionRecords(search);
				// 有資料?
				if (p_Headers.size() < 1) {
					return check;
				}
				// 重複?
				if (check_Bodys.size() > 0) {
					// 1.不包含自己2.不是別人的SN
					if (!data.getString("pb_b_sn").equals(data.getString("pb_sn")) && //
							data.getString("pb_b_sn").equals(check_Bodys.get(0).getPbsn())) {
						return check;
					}
				}
				// 物件轉換
				ProductionBody pro_b = p_Bodys.get(0);
				pro_b.setPbid(data.getLong("pb_id"));
				pro_b.setPbusefulsn(data.getInt("pb_useful_sn"));
				pro_b.setPbsn(data.getString("pb_b_sn"));
				pro_b.setPbbsn(data.getString("pb_b_sn"));
				pro_b.setPboldsn(data.getString("pb_old_sn"));
				pro_b.setPbgid(p_Headers.get(0).getPhpbgid());
				pro_b.setPbcheck(data.getBoolean("pb_check"));
				pro_b.setPbfvalue(data.getString("pb_f_value"));
				// pro_b.setPbltext(data.getString("pb_l_text"));
				pro_b.setSysstatus(data.getInt("sys_status"));
				pro_b.setSysnote(data.getString("sys_note"));
				pro_b.setPbwyears(data.getInt("pb_w_years"));
				pro_b.setSyssort(data.getInt("sys_sort"));
				pro_b.setSysmuser(user.getSuaccount());
				pro_b.setSysmdate(new Date());
				try {
					for (int k = 0; k < 50; k++) {
						// 有欄位?
						if (data.has("pb_value" + String.format("%02d", k + 1))) {
							String value = data.getString("pb_value" + String.format("%02d", k + 1));
							String in_name = "setPbvalue" + String.format("%02d", k + 1);
							Method in_method = pro_b.getClass().getMethod(in_name, String.class);
							// 欄位有值
							if (value != null && !value.equals("")) {
								in_method.invoke(pro_b, value);
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				bodyDao.save(pro_b);
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 移除 資料清單
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				bodyDao.deleteByPbid(data.getLong("pb_id"));
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}
}
