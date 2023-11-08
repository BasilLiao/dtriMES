package dtri.com.tw.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.LabelListBean;
import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.LabelList;
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.LabelListDao;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.tools.Fm_Time;
import dtri.com.tw.tools.Fm_ZPLCodeConveterImg;

@Service
public class LabelListService {
	@Autowired
	private LabelListDao labelsDao;
	@Autowired
	private ProductionBodyDao bodyDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}

		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("llid").descending());
		String search_ll_name = null;
		String search_ll_g_name = null;

		// 功能-名稱編譯
		// 維修細節
		String ll_id = "標籤ID", ll_name = "標籤名稱", ll_g_name = "標籤群名稱", //
				ll_xa = "語法ZPL", ll_ci = "編碼(固定)", //
				ll_ll = "紙張-長度", ll_pw = "紙張-寬度", //
				ll_lh = "起始座標(x,y)", ll_md = "暗度(+-30)", //
				ll_pr = "速度(1-7)", ll_fo_s = "所有區塊ZPL", ll_a_json = "設計內容Json";

		// 固定-名稱編譯
		String sys_c_date = "建立時間", sys_c_user = "建立人", //
				sys_m_date = "修改時間", sys_m_user = "修改人", //
				sys_note = "備註", sys_status = "狀態", sys_header = "群組";

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			// 標籤資料
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t(sys_header, "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_id", FFS.h_t(ll_id, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_name", FFS.h_t(ll_name, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_g_name", FFS.h_t(ll_g_name, "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_xa", FFS.h_t(ll_xa, "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_ci", FFS.h_t(ll_ci, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_ll", FFS.h_t(ll_ll, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_pw", FFS.h_t(ll_pw, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_lh", FFS.h_t(ll_lh, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_md", FFS.h_t(ll_md, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_pr", FFS.h_t(ll_pr, "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_fo_s", FFS.h_t(ll_fo_s, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ll_a_json", FFS.h_t(ll_a_json, "100px", FFM.Wri.W_N));

			// 系統固定
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t(sys_c_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t(sys_c_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t(sys_m_date, "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t(sys_m_user, "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t(sys_status, "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t(sys_note, "100px", FFM.Wri.W_N));
			bean.setHeader(new JSONObject().put("search_header", object_header));

			// 放入修改 [m__(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray s_val = new JSONArray();
			JSONArray n_val = new JSONArray();

			// 標籤細節
			obj_m = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_header", sys_header));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "ll_id", ll_id));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "ll_name", ll_name));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "製令單連動", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "ll_g_name", ll_g_name));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "UTF-8", "UTF-8", FFM.Wri.W_N, "col-md-1", true, n_val, "ll_ci", ll_ci));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1cm:200.", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "ll_ll", ll_ll));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1cm:200.", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "ll_pw", ll_pw));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, n_val, "ll_lh", ll_lh));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "-30~+30", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "ll_md", ll_md));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "1~7", "", FFM.Wri.W_Y, "col-md-1", true, n_val, "ll_pr", ll_pr));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-12", false, n_val, "ll_xa", ll_xa));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "{}", "{}", FFM.Wri.W_N, "col-md-6", false, n_val, "ll_fo_s", ll_fo_s));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.TTA, FFM.Type.TEXT, "{}", "{}", FFM.Wri.W_N, "col-md-6", false, n_val, "ll_a_json", ll_a_json));

			s_val = new JSONArray();
			s_val.put((new JSONObject()).put("value", "開啟(正常)").put("key", "0"));
			s_val.put((new JSONObject()).put("value", "關閉(作廢)").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, s_val, "sys_status", sys_status));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-4", false, n_val, "sys_note", sys_note));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_date", sys_m_date));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "sys_m_user", sys_m_user));

			bean.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			// 標籤細節
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ll_name", ll_name, n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "ll_g_name", ll_g_name, n_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			search_ll_name = body.getJSONObject("search").getString("ll_name");
			search_ll_name = search_ll_name.equals("") ? null : search_ll_name;
			search_ll_g_name = body.getJSONObject("search").getString("ll_g_name");
			search_ll_g_name = search_ll_g_name.equals("") ? null : search_ll_g_name;
		}

		// 查詢子類別?全查?
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();

		ArrayList<LabelList> llist = labelsDao.findAllByLlgnameAndLlname(search_ll_name, search_ll_g_name, page_r);
		// 有沒有資料?
		if (llist.size() > 0) {
			llist.forEach(ll -> {
				JSONObject object_body = new JSONObject();
				int ord = 0;

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", ll.getSysheader());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_id", ll.getLlid());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_name", ll.getLlname());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_g_name", ll.getLlgname());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_xa", ll.getLlxa());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_ci", ll.getLlci());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_ll", ll.getLlll());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_pw", ll.getLlpw());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_lh", ll.getLllh());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_md", ll.getLlmd() == null ? "" : ll.getLlmd());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_pr", ll.getLlpr());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_fo_s", ll.getLlfos());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ll_a_json", ll.getLlajson());

				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(ll.getSyscdate()));
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", ll.getSyscuser());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(ll.getSysmdate()));
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", ll.getSysmuser());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", ll.getSysstatus());
				object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", ll.getSysnote());

				object_bodys.put(object_body);
			});
		}
		bean.setBody(new JSONObject().put("search", object_bodys));
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("create");
			// JSONArray list_chok = new JSONArray();
			// check = true;
		} catch (Exception e) {
			System.out.println(e);
			return check;
		}
		return check;
	}

	// 另存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		// JSONObject body = req.getBody();
		boolean check = false;
		try {
			// JSONArray list = body.getJSONArray("save_as");
			// JSONArray list_check = new JSONArray();
			// check = true;
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
				// 物件轉換
				JSONObject data = (JSONObject) one;
				LabelList obj_b = new LabelList();
				// 維修單細節
				ArrayList<LabelList> obj_bs = labelsDao.findAllByLlid(data.getLong("ll_id"));
				if (obj_bs.size() == 1) {
					obj_b = obj_bs.get(0);
					// ================需補充================

					obj_b.setSysmdate(new Date());
					obj_b.setSysmuser(user.getSuaccount());
					labelsDao.save(obj_b);
					check = true;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			check = false;
		}
		check = true;
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
				labelsDao.deleteByLlid(data.getLong("ll_id"));
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// ==============客製化==============

	// 更新/新增 資料清單
	@Transactional
	public boolean updateOrAddDataCustomized(PackageBean resp, PackageBean req, SystemUser user, boolean dubCheck) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			JSONObject ll_one = body.getJSONObject("modify").getJSONObject("detail");
			JSONObject label_set = ll_one.getJSONObject("label_set");
			JSONObject label_package = ll_one.getJSONObject("label_package");
			JSONArray label_blocks = ll_one.getJSONArray("label_block");
			LabelList save_label = new LabelList();
			// ====[資料檢核]====
			// Step1. 標籤-標籤機與標籤紙
			if (label_set.getString("ll_name").equals("") || //
					label_set.getString("ll_g_name").equals("") || //
					label_set.getString("ll_ll").equals("") || //
					label_set.getString("ll_pw").equals("") || //
					label_set.getString("ll_lh_y").equals("") || //
					label_set.getString("ll_lh_x").equals("") || //
					label_set.getString("ll_md").equals("") || //
					label_set.getString("ll_pr").equals("")) {
				resp.autoMsssage("LB005");
				return check;
			}
			// Step2. 標籤-分裝設定
			if (label_package.getString("ll_o_p_type").equals("multiple")) {
				if (label_package.getString("ll_o_top").equals("") || //
						label_package.getString("ll_o_s_b_name").equals("") || //
						label_package.getString("ll_o_p_qty").equals("") || //
						label_package.getString("ll_o_h_b_name").equals("") || //
						label_package.getString("ll_o_l_qty").equals("")) {
					resp.autoMsssage("LB005");
					return check;
				}
			}
			// Step3. 標籤-區塊設定
			for (Object label_block_O : label_blocks) {
				JSONObject label_block = (JSONObject) label_block_O;
				if (label_block.getString("ll_fo_name").equals("") || //
						label_block.getString("ll_fo_y").equals("") || //
						label_block.getString("ll_fo_x").equals("") || //
						label_block.getJSONObject("ll_fo_content").toString().equals("{}")) {
					resp.autoMsssage("LB005");
					return check;
				}
			}
			// Step4-0. 可能是新增?(.特殊新增(必須而外再存一次))
			if (label_set.getString("ll_id").equals("") && dubCheck) {
				ArrayList<LabelList> save2_label_old = labelsDao.findAllByLlgnameAndLlname(label_set.getString("ll_name"), null, null);
				if (save2_label_old.size() > 0) {
					label_set.put("ll_id", save2_label_old.get(0).getLlid() + "");
				}
			}
			// Step4. 可能是新增?
			if (label_set.getString("ll_id").equals("")) {
				ArrayList<LabelList> save_label_old = labelsDao.findAllByLlgnameAndLlname(label_set.getString("ll_name"), null, null);
				// 如果跟舊的名稱一樣不可新增
				if (save_label_old.size() > 0) {
					resp.autoMsssage("LB006");
					return check;
				}
				// label_set
				save_label.setLlname(label_set.getString("ll_name"));
				save_label.setLlgname(label_set.getString("ll_g_name"));
				save_label.setLlci("UTF-8");
				save_label.setLlpw(label_set.getString("ll_pw"));
				save_label.setLlmd(label_set.getString("ll_md"));
				save_label.setLlpr(label_set.getString("ll_pr"));
				save_label.setLlll(label_set.getString("ll_ll"));
				save_label.setLllh(label_set.getString("ll_lh_x") + "," + label_set.getString("ll_lh_y"));// (X,Y)

				// label_package
				String lloptype = label_package.getString("ll_o_p_type").equals("multiple") ? "1" : "0";
				save_label.setLloptype(lloptype);
				save_label.setLlotop(Integer.parseInt(label_package.getString("ll_o_top")));
				save_label.setLlosbname(label_package.getString("ll_o_s_b_name"));
				save_label.setLlohbname(label_package.getString("ll_o_h_b_name"));
				save_label.setLlopqty(Integer.parseInt(label_package.getString("ll_o_p_qty")));
				save_label.setLlolqty(Integer.parseInt(label_package.getString("ll_o_l_qty")));

				// label_block
				save_label.setLlfos(label_blocks.toString());

				// all
				save_label.setLlajson(ll_one.toString());
				save_label.setSyscuser(user.getSuaccount());
				save_label.setSysmuser(user.getSuaccount());
				labelsDao.save(save_label);
				check = true;
			} else {
				// 修改?
				ArrayList<LabelList> labels = labelsDao.findAllByLlid(Long.parseLong(label_set.getString("ll_id")));
				if (labels.size() == 1) {
					save_label = labels.get(0);
					// label_set
					save_label.setLlname(label_set.getString("ll_name"));
					save_label.setLlgname(label_set.getString("ll_g_name"));
					save_label.setLlci("UTF-8");
					save_label.setLlpw(label_set.getString("ll_pw"));
					save_label.setLlmd(label_set.getString("ll_md"));
					save_label.setLlpr(label_set.getString("ll_pr"));
					save_label.setLlll(label_set.getString("ll_ll"));
					save_label.setLllh(label_set.getString("ll_lh_x") + "," + label_set.getString("ll_lh_y"));// (X,Y)

					// label_package
					String lloptype = label_package.getString("ll_o_p_type").equals("multiple") ? "1" : "0";
					save_label.setLloptype(lloptype);
					save_label.setLlotop(Integer.parseInt(label_package.getString("ll_o_top")));
					save_label.setLlosbname(label_package.getString("ll_o_s_b_name"));
					save_label.setLlohbname(label_package.getString("ll_o_h_b_name"));
					save_label.setLlopqty(Integer.parseInt(label_package.getString("ll_o_p_qty")));
					save_label.setLlolqty(Integer.parseInt(label_package.getString("ll_o_l_qty")));

					// label_block
					save_label.setLlfos(label_blocks.toString());

					// all
					save_label.setLlajson(ll_one.toString());
					save_label.setSysmdate(new Date());
					save_label.setSysmuser(user.getSuaccount());
					labelsDao.save(save_label);
					check = true;
				}
			}

			// Step5. 物件轉換入Entity

			// ====[資料更新]====
			// Step1. 維修單自動生成

			// 如果有維修清單List
			if (ll_one.length() > 0) {

			}

			// 回傳準備
			JSONObject cb = req.getCall_bk_vals();
			cb.put("search", true);
			req.setCall_bk_vals(cb);

		} catch (Exception e) {
			System.out.println(e);
			check = false;
		}
		return check;
	}

	// 取得 - Customized mode當前表單式查詢資料
	@Transactional
	public boolean getDataCustomized(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		if (resp == null) {
			resp = new PackageBean();
		}
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = resp.getHeader();
			JSONObject customized_header = new JSONObject();
			object_header.put("customized_header", customized_header);
			resp.setHeader(object_header);
		}

		JSONObject object_body = resp.getBody();
		if (object_body == null) {
			object_body = new JSONObject();
		}

		JSONArray objects = new JSONArray();

		// 標籤資料
		ArrayList<LabelList> labels = new ArrayList<LabelList>();
		labels = (ArrayList<LabelList>) labelsDao.findAllByOrderByLlgnameAscLlnameAsc();
		if (labels.size() >= 1) {
			labels.forEach(lls -> {
				JSONObject ll = new JSONObject();
				ll.put("ll_id", lls.getLlid());
				ll.put("ll_name", lls.getLlname());
				ll.put("ll_gname", lls.getLlgname());
				ll.put("ll_a_json", lls.getLlajson());
				objects.put(ll);
			});
		}
		object_body.put("customized_detail", objects);

		// 標籤跟隨清單
		JSONArray object_follow = new JSONArray();
		// 自訂義
		object_follow.put("na.na.====工作站(自訂義)====");//
		object_follow.put("lc.front_from_lc1.工作站-自訂義:LC1");// 自訂義:LC1
		object_follow.put("lc.front_from_lc2.工作站-自訂義:LC2");// 自訂義:LC2
		object_follow.put("lc.front_from_lc3.工作站-自訂義:LC3");// 自訂義:LC3
		object_follow.put("lc.front_from_lc4.工作站-自訂義:LC4");// 自訂義:LC4
		// 前端
		object_follow.put("na.na.====工作站(外箱裝用)====");//
		object_follow.put("fn.front_from_fixed.工作站-固定名稱");// 來自工作站前端
		object_follow.put("fn.front_from_sn.工作站-產品序號");// 來自工作站前端
		object_follow.put("fn.front_from_qty.工作站-產品序號數量");// 產品序號數量
		// 產品細節
		object_follow.put("na.na.====產品細節====");//
		object_follow.put("pb.getPbbsn.產品身分號碼");// 產品燒錄號碼
		object_follow.put("pb.getPbshippingdate.實際出貨日");// 實際出貨日
		object_follow.put("pb.getPbrecyclingdate.回收日");// 回收日
		object_follow.put("pb.getPbposition.最後位置");// 最後位置
		object_follow.put("pb.getPbwyears.保固年分");// 保固年分
		try {
			// 有效設定的欄位
			ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
			for (int k = 0; k < 50; k++) {
				String in_name = "getPbvalue" + String.format("%02d", k + 1);
				Method in_method = body_one.getClass().getMethod(in_name);
				String value = (String) in_method.invoke(body_one);
				// 欄位有定義的顯示
				if (value != null && !value.equals("")) {
					object_follow.put("pb." + in_name + "." + value);// 動態欄位
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 製令單
		object_follow.put("na.na.====製令單====");//
		object_follow.put("ph.getPhmfgpno.驗證碼");// 驗證碼
		object_follow.put("ph.getPhpsno.組件號");// 組件號
		object_follow.put("ph.getPhpname.產品名稱(號)");// 產品名稱(號)
		object_follow.put("ph.getPhesdate.預計出貨日");// 預計出貨日
		object_follow.put("ph.getPhorderid.訂單號(號)");// 訂單號(號)
		object_follow.put("ph.getPhwcline.產線");// 產線
		object_follow.put("ph.getSysnote.備註");// 備註
		object_follow.put("pr.getPrid.製令單號");// 製令單號
		// 製令規格
		object_follow.put("na.na.====製令規格====");//
		object_follow.put("pr.getPrbomid.BOM料號(公司)");// BOM料號(公司)
		object_follow.put("pr.getPrbomcid.BOM料號(客戶)");// BOM料號(客戶)
		object_follow.put("pr.getPrname.產品品名");// 產品品名
		object_follow.put("pr.getPrspecification.規格敘述");// 規格敘述
		object_follow.put("pr.getPrbitem.規格內容(指定項目)");// 規格內容(指定項目)
		object_follow.put("pr.getPrpmodel.產品型號");// 產品型號
		object_follow.put("pr.getPrpv.產品版本");// 產品版本

		object_body.put("customized_follow", object_follow);
		resp.setBody(object_body);
		return true;
	}

	// 測試打印 資料清單
	/**
	 * @param resp
	 * @param req
	 * @param user
	 * @param testPrint 是否測試打印
	 * 
	 */
	@Transactional
	public boolean printerCustomized(PackageBean resp, PackageBean req, SystemUser user, Boolean testPrint, LabelList truePrint) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			if (body != null && !body.isNull("print")) {
				System.out.println(body.getJSONObject("print"));
				String printName = body.getJSONObject("print").getString("print_code");// 標籤機代號
				int printQty = Integer.parseInt(body.getJSONObject("print").getString("print_qty"));// 幾張
				ArrayList<LabelList> labels = new ArrayList<LabelList>();
				// Y=測試用?N=正是用?
				if (testPrint) {
					Long ll_id = Long.parseLong(body.getJSONObject("print").getString("label_choose"));
					labels = labelsDao.findAllByLlid(ll_id);
				} else {
					labels.add(truePrint);
				}
				// Step1. 取出打印對象
				if (labels.size() == 1) {
					LabelList label = labels.get(0);
					// Step2. 分析打印內容
					JSONObject label_json = new JSONObject(label.getLlajson());
					LabelListBean label_bean = new LabelListBean();
					// label_set
					JSONObject label_set = label_json.getJSONObject("label_set");

					label_bean.setLlpw(label_bean.getLlpw().replace("{標籤寬度(點)}", label_set.getString("ll_pw")));
					label_bean.setLlll(label_bean.getLlll().replace("{標籤長度(點)}", label_set.getString("ll_ll")));
					label_bean.setLlmd(label_bean.getLlmd().replace("{打印暗度}", label_set.getString("ll_md")));
					label_bean.setLlpr(label_bean.getLlpr().replace("{打印速度}", label_set.getString("ll_pr")));
					label_bean.setLllh(
							label_bean.getLllh().replace("{x,y起始打印座標(點)}", label_set.getString("ll_lh_x") + "," + label_set.getString("ll_lh_y")));
					// label_package
					JSONObject label_package = label_json.getJSONObject("label_package");
					label_bean.setLl_o_p_type(label_package.getString("ll_o_p_type").equals("multiple"));
					if (label_package.getString("ll_o_p_type").equals("multiple")) {
						label_bean.setLl_o_p_qty(Integer.parseInt(label_package.getString("ll_o_p_qty")));// 每箱多少台
						label_bean.setLl_o_l_qty(Integer.parseInt(label_package.getString("ll_o_l_qty")));// 每箱多少台
						label_bean.setLl_l_qty(label_bean.getLl_o_p_qty() / label_bean.getLl_o_l_qty());// 每次 幾張標籤(無條件進位)
						label_bean.setLl_l_now(1);
						label_bean.setLl_o_h_b_name(label_package.getString("ll_o_h_b_name"));// 指定隱藏
						label_bean.setLl_o_s_b_name(label_package.getString("ll_o_s_b_name"));// 指定重複
						label_bean.setLl_o_top(Integer.parseInt(label_package.getString("ll_o_top")));
						// 測試用
						if (testPrint) {
							JSONArray label_t_blocks = label_json.getJSONArray("label_block");
							for (int i = 0; i < label_t_blocks.length(); i++) {
								JSONObject label_block = label_t_blocks.getJSONObject(i);
								JSONObject ll_fo_c = label_block.getJSONObject("ll_fo_content");
								String ll_fo_name = label_block.getString("ll_fo_name");
								switch (ll_fo_c.getString("label_block_type")) {
								case "char_type":
									String ll_fds = "";
									// 需要重複的
									if (label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										for (int a = 1; a <= label_bean.getLl_o_p_qty(); a++) {
											ll_fds += ll_fo_c.getString("ll_fd") + " ";
										}
										ll_fo_c.put("ll_fd", ll_fds);
										label_block.put("ll_fo_content", ll_fo_c);
										label_t_blocks.put(i, label_block);
									}
									break;
								case "img_type":
									// 圖片
									String ll_gfas = "";
									// 需要重複的
									if (label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										for (int a = 1; a <= label_bean.getLl_o_p_qty(); a++) {
											ll_gfas += ll_fo_c.getString("ll_gfa") + " ";
										}
										ll_fo_c.put("ll_gfa", ll_gfas);
										label_block.put("ll_fo_content", ll_fo_c);
										label_t_blocks.put(i, label_block);
									}
									break;
								case "barcode_type":
									// 一維碼
									String ll_bfds = "";
									// 需要重複的
									if (label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										for (int a = 1; a <= label_bean.getLl_o_p_qty(); a++) {
											ll_bfds += ll_fo_c.getString("ll_bfd") + " ";
										}
										ll_fo_c.put("ll_bfd", ll_bfds);
										label_block.put("ll_fo_content", ll_fo_c);
										label_t_blocks.put(i, label_block);
									}

									break;
								case "qr_code_type":
									// 二維碼
									String ll_bqfds = "";
									// 需要重複的
									if (label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										for (int a = 1; a <= label_bean.getLl_o_p_qty(); a++) {
											ll_bqfds += ll_fo_c.getString("ll_bqfd") + " ";
										}
										ll_fo_c.put("ll_bqfd", ll_bqfds);
										label_block.put("ll_fo_content", ll_fo_c);
										label_t_blocks.put(i, label_block);
									}
									break;
								}
							}
							label_json.put("label_block", label_t_blocks);
						}
					}

					// label_block
					JSONArray label_blocks = label_json.getJSONArray("label_block");

					// label_all
					String llxa = "";
					// 共要跑幾張
					for (int y = 1; y <= label_bean.getLl_l_qty(); y++) {
						label_bean.setLl_l_now(y);// 目前第幾張
						label_bean.newFolist();

						label_blocks.forEach(x -> {
							JSONObject label_block = (JSONObject) x;
							JSONObject ll_fo_c = label_block.getJSONObject("ll_fo_content");
							String ll_fo_name = label_block.getString("ll_fo_name");
							String llfo = "";
							// 隱藏 = 頁數大於1 且被指定隱藏 = false
							Boolean hidden = true;
							if (label_bean.getLl_l_now() > 1) {
								hidden = label_bean.getLl_o_h_b_name().indexOf(ll_fo_name) < 0;

							}

							if (hidden) {
								switch (ll_fo_c.getString("label_block_type")) {
								case "char_type":
									llfo = "";
									// 文字
									if (label_bean.isLl_o_p_type() && label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										// 一張標籤->多項->固定 or 跟隨?
										String ll_fd_s[] = ll_fo_c.getString("ll_fd").split(" ");

										// 複數區間
										int i_start = (label_bean.getLl_l_now() - 1) * label_bean.getLl_o_l_qty();
										int i_end = label_bean.getLl_l_now() * label_bean.getLl_o_l_qty();
										int l_o_top = label_bean.getLl_o_top();
										int ll_fo_y = Integer.parseInt(label_block.getString("ll_fo_y"));
										String llfos = "";// 該區塊所有內容
										for (int i = i_start; i < i_end; i++) {
											// 可能沒有資料
											if (i >= ll_fd_s.length) {
												continue;
											}
											String ll_fd = ll_fd_s[i];
											// 避免是空值
											if (ll_fd.length() != 0) {
												llfo = label_bean.getLlfd().replace("{一般文字}", ll_fd);
												// 字型?
												llfo += label_bean.getLla().replace("{字型與角度,高,寬}", //
														ll_fo_c.getString("ll_a_t") + ll_fo_c.getString("ll_a_c") + ","//
																+ ll_fo_c.getString("ll_a_h") //
																+ (ll_fo_c.getString("ll_a_w").equals("") ? "" : "," + ll_fo_c.getString("ll_a_w")));
												// 區塊屬性?
												if (!ll_fo_c.getString("ll_fb_d").equals("")) {
													llfo += label_bean.getLlfb().replace("{寬度(點),行數,行間高度,靠左右中,縮排}", //
															ll_fo_c.getString("ll_fb_a") + "," //
																	+ ll_fo_c.getString("ll_fb_b") + ","//
																	+ ll_fo_c.getString("ll_fb_c") + "," //
																	+ ll_fo_c.getString("ll_fb_d") + ",0");
												}
												// 位置
												llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
														label_block.getString("ll_fo_x") + "," + ll_fo_y + llfo);
												llfos += llfo;
												ll_fo_y = ll_fo_y + l_o_top;
											}
										}
										llfo = llfos;// 放回區塊內
										label_bean.setFolist(llfo);
									} else {
										// 一張標籤->單項->固定 or 跟隨?
										String ll_fd = ll_fo_c.getString("ll_fd");
										// 避免是空值
										if (ll_fd.length() != 0) {
											llfo = label_bean.getLlfd().replace("{一般文字}", ll_fd);

											// 字型?
											llfo += label_bean.getLla().replace("{字型與角度,高,寬}", //
													ll_fo_c.getString("ll_a_t") + ll_fo_c.getString("ll_a_c") + ","//
															+ ll_fo_c.getString("ll_a_h") //
															+ (ll_fo_c.getString("ll_a_w").equals("") ? "" : "," + ll_fo_c.getString("ll_a_w")));
											// 區塊屬性?
											if (!ll_fo_c.getString("ll_fb_d").equals("")) {
												llfo += label_bean.getLlfb().replace("{寬度(點),行數,行間高度,靠左右中,縮排}", //
														ll_fo_c.getString("ll_fb_a") + "," //
																+ ll_fo_c.getString("ll_fb_b") + ","//
																+ ll_fo_c.getString("ll_fb_c") + "," //
																+ ll_fo_c.getString("ll_fb_d") + ",0");

											}
											// 位置
											llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
													label_block.getString("ll_fo_x") + "," + label_block.getString("ll_fo_y") + llfo);
										}
										label_bean.setFolist(llfo);
									}
									break;
								case "img_type":
									// 圖片
									llfo = "";
									if (label_bean.isLl_o_p_type() && label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										// 一張標籤->多項->固定
										String ll_gfa_s[] = ll_fo_c.getString("ll_gfa").split(" ");
										// 複數區間
										int i_start = (label_bean.getLl_l_now() - 1) * label_bean.getLl_o_l_qty();
										int i_end = label_bean.getLl_l_now() * label_bean.getLl_o_l_qty();
										int l_o_top = label_bean.getLl_o_top();
										int ll_fo_y = Integer.parseInt(label_block.getString("ll_fo_y"));
										String llfos = "";// 該區塊所有內容
										for (int i = i_start; i < i_end; i++) {
											String ll_gfa = ll_gfa_s[i];
											String imageString = ll_gfa.split(",")[1];
											// create a buffered image
											byte[] decodedBytes = Base64.decodeBase64(imageString);
											InputStream inputImg = new ByteArrayInputStream(decodedBytes);
											try {
												imageString = Fm_ZPLCodeConveterImg.getFont2ImgZPL(inputImg, true, 90,
														Double.parseDouble(ll_fo_c.getString("ll_gfa_p")));
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											// 位置
											llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}^FS", //
													label_block.getString("ll_fo_x") + "," + ll_fo_y + imageString);
											llfos += llfo;
											ll_fo_y = ll_fo_y + l_o_top;
										}
										llfo = llfos;
									} else {
										String imageString = ll_fo_c.getString("ll_gfa").split(",")[1];
										// create a buffered image
										byte[] decodedBytes = Base64.decodeBase64(imageString);
										InputStream inputImg = new ByteArrayInputStream(decodedBytes);
										try {
											imageString = Fm_ZPLCodeConveterImg.getFont2ImgZPL(inputImg, true, 90,
													Double.parseDouble(ll_fo_c.getString("ll_gfa_p")));
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}^FS", //
												label_block.getString("ll_fo_x") + "," + label_block.getString("ll_fo_y") + imageString);
									}
									label_bean.setFolist(llfo);
									break;
								case "barcode_type":
									// 一維條碼
									llfo = "";
									if (label_bean.isLl_o_p_type() && label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										// 一張標籤->多項->固定 or 跟隨?
										String ll_bfd_s[] = ll_fo_c.getString("ll_bfd").split(" ");

										// 複數區間
										int i_start = (label_bean.getLl_l_now() - 1) * label_bean.getLl_o_l_qty();
										int i_end = label_bean.getLl_l_now() * label_bean.getLl_o_l_qty();
										int l_o_top = label_bean.getLl_o_top();
										int ll_fo_y = Integer.parseInt(label_block.getString("ll_fo_y"));
										String llfos = "";// 該區塊所有內容
										for (int i = i_start; i < i_end; i++) {
											// 可能沒有資料
											if (i >= ll_bfd_s.length) {
												continue;
											}
											String ll_bfd = ll_bfd_s[i];
											// 避免是空值
											if (ll_bfd.length() != 0) {
												// 如果只有一格字(code39 or 11)
												if (!ll_fo_c.getString("ll_b_x").equals("C") && ll_bfd.length() == 1) {
													ll_bfd += " ";
												}
												llfo = label_bean.getLlbfd().replace("{條碼文字}", ll_bfd);
												// 條碼?
												llfo += label_bean.getLlby().replace("{條碼窄線(點),條碼寬比}", //
														ll_fo_c.getString("ll_by_m") + "," + ll_fo_c.getString("ll_by_w"));
												// 類型?
												String codeType = "";
												if (ll_fo_c.getString("ll_b_x").equals("C")) {
													// code128
													codeType = ll_fo_c.getString("ll_b_x") + ll_fo_c.getString("ll_b_c") + ",";
												} else {
													// code39 or 11
													codeType = ll_fo_c.getString("ll_b_x") + ll_fo_c.getString("ll_b_c") + ",N,";
												}
												llfo += label_bean.getLlb().replace("{類型與角度,N,條碼高度(點),N,N}", //
														codeType + ll_fo_c.getString("ll_b_h") + ",N,N");

												// 位置
												llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
														label_block.getString("ll_fo_x") + "," + ll_fo_y + llfo);
												llfos += llfo;
												ll_fo_y = ll_fo_y + l_o_top;
											}
										}
										llfo = llfos;// 放回區塊內
										label_bean.setFolist(llfo);
									} else {
										// 固定 or 跟隨?
										String ll_bfd = ll_fo_c.getString("ll_bfd");
										// 避免是空值
										if (ll_bfd.length() != 0) {
											// 如果只有一格字(code39 or 11)
											if (!ll_fo_c.getString("ll_b_x").equals("C") && ll_bfd.length() == 1) {
												ll_bfd += " ";
											}

											llfo = label_bean.getLlbfd().replace("{條碼文字}", ll_bfd);

											// 條碼?
											llfo += label_bean.getLlby().replace("{條碼窄線(點),條碼寬比}", //
													ll_fo_c.getString("ll_by_m") + "," + ll_fo_c.getString("ll_by_w"));
											// 類型?
											String codeType = "";
											if (ll_fo_c.getString("ll_b_x").equals("C")) {
												// code128
												codeType = ll_fo_c.getString("ll_b_x") + ll_fo_c.getString("ll_b_c") + ",";
											} else {
												// code39 or 11
												codeType = ll_fo_c.getString("ll_b_x") + ll_fo_c.getString("ll_b_c") + ",N,";
											}
											llfo += label_bean.getLlb().replace("{類型與角度,N,條碼高度(點),N,N}", //
													codeType + ll_fo_c.getString("ll_b_h") + ",N,N");
											// 位置
											llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
													label_block.getString("ll_fo_x") + "," + label_block.getString("ll_fo_y") + llfo);
											label_bean.setFolist(llfo);
										}
									}
									break;
								case "qr_code_type":
									// 二維條碼
									llfo = "";
									if (label_bean.isLl_o_p_type() && label_bean.getLl_o_s_b_name().indexOf(ll_fo_name) >= 0) {
										// 一張標籤->多項->固定 or 跟隨?
										String ll_bqfd_s[] = ll_fo_c.getString("ll_bqfd").split(" ");

										// 複數區間
										int i_start = (label_bean.getLl_l_now() - 1) * label_bean.getLl_o_l_qty();
										int i_end = label_bean.getLl_l_now() * label_bean.getLl_o_l_qty();
										int l_o_top = label_bean.getLl_o_top();
										int ll_fo_y = Integer.parseInt(label_block.getString("ll_fo_y"));
										String llfos = "";// 該區塊所有內容
										for (int i = i_start; i < i_end; i++) {
											// 可能沒有資料
											if (i >= ll_bqfd_s.length) {
												continue;
											}
											String ll_bqfd = ll_bqfd_s[i];
											// 避免是空值
											if (ll_bqfd.length() != 0) {
												llfo = label_bean.getLlbfd().replace("{條碼文字}", ll_bqfd);

												// 條碼?
												llfo += label_bean.getLlbq().replace("{角度,2,大小}", //
														ll_fo_c.getString("ll_bq_c") + ",2," + ll_fo_c.getString("ll_bq_e"));

												// 位置
												llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
														label_block.getString("ll_fo_x") + "," + ll_fo_y + llfo);
												llfos += llfo;
												ll_fo_y = ll_fo_y + l_o_top;
											}
										}
										llfo = llfos;// 放回區塊內
										label_bean.setFolist(llfo);
									} else {
										// 固定 or 跟隨?
										String ll_bqfd = ll_fo_c.getString("ll_bqfd");
										// 避免是空值
										if (ll_bqfd.length() != 0) {
											llfo = label_bean.getLlbqfd().replace("{條碼文字}", ll_bqfd);
											// 條碼?
											llfo += label_bean.getLlbq().replace("{角度,2,大小}", //
													ll_fo_c.getString("ll_bq_c") + ",2," + ll_fo_c.getString("ll_bq_e"));

											llfo = label_bean.getLlfo().replace("{x,y區域位置座標(點)}", //
													label_block.getString("ll_fo_x") + "," + label_block.getString("ll_fo_y") + llfo);
											label_bean.setFolist(llfo);
										}
									}
									break;
								}
							}
						});
						// Step4.編譯成ZPL
						llxa += label_bean.getLlxa().replace("{ZPL打印內容}", label_bean.getLlheader() + label_bean.getLlbody()) + "\n";

					}
					// Step5.辨識標籤機
					PrintService pService = getPrinterService(printName);
					// Step5.送出
					System.out.println(llxa);
					if (pService != null && printQty >= 1) {
						// 印幾次
						String pxa = "";
						for (int i = 0; i < printQty; i++) {
							pxa += llxa;
						}
						sendPrinter(pxa, pService);
						check = true;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 工作站-資訊轉換標籤
	/**
	 * 格式: { <br>
	 * "label_package": { "ll_o_top": "0", "ll_o_p_type": "", "ll_o_s_b_name": "",
	 * "ll_o_p_qty": "0", "ll_o_h_b_name": "", "ll_o_l_qty": "0" }, <br>
	 * "label_set": { "ll_name": "3X1單張", "sys_note": "Test_Note", "ll_ci": "UTF-8",
	 * "ll_pw": "600", "ll_md": "20", "ll_pr": "5", "ll_ll": "200", "ll_lh_y": "0",
	 * "ll_g_name": "Test_Group", "ll_lh_x": "0", "ll_id": "6" }, <br>
	 * "label_block": [ { "ll_fo_y": "50", "ll_fo_x": "50", "ll_fo_content": {
	 * "label_block_type": "char_type", "ll_fd_f": "pb.getPbvalue08", "ll_a_h":
	 * "20", "ll_a_w": "", "ll_fb_d": "", "ll_fb_b": "", "ll_fd": "Text_FD",
	 * "ll_fb_c": "", "ll_fb_a": "", "ll_a_t": "GS", "ll_fd_test": "Test_FD",
	 * "ll_a_c": "N" }, "ll_fo_name": "ex_4" } ] <br>
	 * }<br>
	 * 輸入格式:<br>
	 * {"f_f_l":["A7777A0002"],"printer_c":"123","barcode_pe_q":0,"barcode_pt_q":"1","barcode_ll_q":0,"barcode_id":"6"}
	 * 
	 **/
	public LabelList workstationToLabel(JSONObject label_json, JSONArray front_from_sn, int front_from_qty, ProductionHeader ph, ProductionRecords pr,
			ProductionBody pb) {
		LabelList labelList = new LabelList();
		String label_id = label_json.getString("barcode_id");// ID

		JSONArray llajson = new JSONArray(ph.getPhllajson());
		for (Object object : llajson) {
			JSONObject ll = (JSONObject) object;
			// 如果有找到->封裝->跳出->回傳
			if (ll.getJSONObject("label_set").getString("ll_id").equals(label_id)) {
				// JSONObject label_set = ll.getJSONObject("label_set");
				JSONObject label_package = ll.getJSONObject("label_package");
				JSONArray label_blocks = ll.getJSONArray("label_block");
				// 包裝設置
				label_package.put("ll_o_p_qty", label_json.getString("barcode_pe_q"));
				label_package.put("ll_o_l_qty", label_json.getString("barcode_ll_q"));

				// 檢查區塊
				for (int a = 0; a < label_blocks.length(); a++) {
					JSONObject label_block = label_blocks.getJSONObject(a);
					JSONObject block = label_block.getJSONObject("ll_fo_content");
					String table = "";
					String cell = "";
					Method in_method;
					String putName = "";
					String putValue = "";
					String putSpace = "";
					switch (block.getString("label_block_type")) {
					case "char_type":
						// 文字模式
						// 跟隨機制?
						putName = "ll_fd";
						putValue = block.getString(putName);
						if (!block.getString("ll_fd_f").equals("")) {
							table = block.getString("ll_fd_f").split("\\.")[0];
							cell = block.getString("ll_fd_f").split("\\.")[1];
						}
						break;
					case "barcode_type":
						// 一維條碼
						// 跟隨機制?
						putName = "ll_bfd";
						putValue = block.getString(putName);
						if (!block.getString("ll_bfd_f").equals("")) {
							table = block.getString("ll_bfd_f").split("\\.")[0];
							cell = block.getString("ll_bfd_f").split("\\.")[1];
						}
						break;
					case "qr_code_type":
						// 二維條碼
						// 跟隨機制?
						putName = "ll_bqfd";
						putValue = block.getString(putName);
						if (!block.getString("ll_bqfd_f").equals("")) {
							table = block.getString("ll_bqfd_f").split("\\.")[0];
							cell = block.getString("ll_bqfd_f").split("\\.")[1];
						}
						break;
					default:
						break;
					}
					// 分析跟隨?
					if (!table.equals("") && !cell.equals("")) {
						try {
							switch (table) {
							case "ph":
								in_method = ph.getClass().getMethod(cell);
								putValue = (String) in_method.invoke(ph);
								break;
							case "pr":

								// 特殊-產品規格核對
								if (cell.equals("getPrbitem")) {
									try {
										/*
										 * {"週邊-RS232":{"Qty":1,"Is":"Yes"}, "CPU":{"Qty":1,"Is":"intel J5040"},
										 * "Adapter":{"Qty":1,"Is":"NｏrmalDC-in19V/65W"}, "FCCLabel":{"Qty":1,"Is":"有"},
										 */
										in_method = pr.getClass().getMethod(cell);
										String putValueSpecification = (String) in_method.invoke(pr);
										JSONObject specification = new JSONObject(putValueSpecification).getJSONObject(putValue);
										String spVal = specification.getString("Is");
										Integer spQty = specification.getInt("Qty");
										if (spVal != null) {
											putValue = putValue + " : " + spVal + (spQty > 1 ? "(x" + spQty + ")" : "");
										} else if (spVal == null || spVal.equals("")) {
											putValue = "";
										}
									} catch (JSONException e) {
										// 不做任何事情
									}
								} else {
									// 其他
									in_method = pr.getClass().getMethod(cell);
									putValue = (String) in_method.invoke(pr);
								}
								break;
							case "pb":
								in_method = pb.getClass().getMethod(cell);
								putValue = (String) in_method.invoke(pb);
								break;
							case "fn":
								// 有特殊-前端跟隨 設定?
								if (cell.equals("front_from_sn") && front_from_sn.length() > 0) {
									putValue = "";
									// 是否需要空格(如果只有1筆資料)
									putSpace = (front_from_sn.length() == 1 ? "" : " ");
									for (Object from_sn : front_from_sn) {
										putValue += (String) from_sn + putSpace;
									}
								}
								// 有特殊-前端跟隨(固定) 設定?
								if (cell.equals("front_from_fixed") && front_from_sn.length() > 0) {
									String putValues = "";
									for (int i = 0; i < front_from_qty; i++) {
										putValues += putValue + " ";
									}
									putValue = putValues;
								}
								// 有特殊-前端跟隨(數量) 設定?
								if (cell.equals("front_from_qty") && front_from_sn.length() > 0) {
									putValue = front_from_qty + "";
								}
								break;
							case "lc":
								// 工作站-自訂義-跟隨
								if (cell.equals("front_from_lc1")) {
									System.out.println(label_json.getString("front_from_lc1"));
									putValue = label_json.getString("front_from_lc1");// 自訂義-跟隨(1)
								} else if (cell.equals("front_from_lc2")) {
									putValue = label_json.getString("front_from_lc2");// 自訂義-跟隨(2)
								} else if (cell.equals("front_from_lc3")) {
									putValue = label_json.getString("front_from_lc3");// 自訂義-跟隨(3)
								} else if (cell.equals("front_from_lc4")) {
									putValue = label_json.getString("front_from_lc4");// 自訂義-跟隨(4)
								}
								break;
							default:
								break;
							}
							block.put(putName, putValue);
						} catch (Exception e) {
							System.out.println(e);
						}
						label_block.put("ll_fo_content", block);
						label_blocks.put(a, label_block);
					}
				}
				// 放回區塊
				ll.put("label_block", label_blocks);
				labelList.setLlajson(ll.toString());
				break;
			}
		}
		return labelList;
	}

	/**
	 * 取得相對應->標籤機代碼
	 * 
	 * @param printName 服務名稱
	 **/
	private PrintService getPrinterService(String printName) {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (final PrintService service : services) {
			if (service.getName().equals(printName)) {
				System.out.println(service.getName());
				return service;
			}
		}
		return null;
	}

	/**
	 * 完成傳送指令
	 **/
	private boolean sendPrinter(String zpl, PrintService service) {
		// 用網路串流可能用到
		// byte[] buf = new byte[1024];
		// Socket socket = new Socket("127.0.0.1", 9100);
		// OutputStream out = socket.getOutputStream();

		// 紙張大小
		// DocFlavor flavor = INPUT_STREAM.AUTOSENSE;
		Thread thread = new Thread() {
			public void run() {
				try {
					byte[] zpl_by = zpl.getBytes();
					DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
					Doc doc = new SimpleDoc(zpl_by, flavor, null);
					DocPrintJob printJob = service.createPrintJob();
					printJob.print(doc, null);

				} catch (PrintException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		return true;
	}
}
