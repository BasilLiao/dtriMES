package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.PcbaRuleSn;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.PcbaRuleSnDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class PcbaRuleSnService {
	@Autowired
	private PcbaRuleSnDao prsDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<PcbaRuleSn> pcbaRuleSns = new ArrayList<PcbaRuleSn>();
		ArrayList<PcbaRuleSn> pcbaRuleSns_son = new ArrayList<PcbaRuleSn>();

//		JSONArray a_vals = new JSONArray();
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("prsid").descending());
		String prs_g_name = null;
		String prs_c_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_header", FFS.h_t("群組", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "ui_group_id", FFS.h_t("UI_Group_ID", "100px", FFM.Wri.W_N));// 群組專用-必須放前面
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_g_id", FFS.h_t("群組[ID]", "100px", FFM.Wri.W_N));		//j 群組id
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_g_name", FFS.h_t("流程序[名稱]", "250px", FFM.Wri.W_Y));  //j 群組名稱 pcb /macd
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_c_name", FFS.h_t("流程序[代碼]", "150px", FFM.Wri.W_Y)); 
			

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_name", FFS.h_t("規則名稱", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_type", FFS.h_t("規則定義", "150px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "prs_length", FFS.h_t("規則碼數", "150px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "280px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N)); // j 欄位順序			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));

			bean.setHeader(object_header);

			//************************************************ 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "prs_id", "ID")); //主key

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "prs_g_id", "群組ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "prs_g_name", "序號組[名稱]"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", true, n_val, "prs_c_name", "序號[代碼]"));			
//			workstations = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, PageRequest.of(0, 999));
//			workstations.forEach(w -> {
//				if (w.getWgid() != 0)
//					a_vals.put((new JSONObject()).put("value", w.getWpbname()).put("key", w.getWgid()));
//			});
			//j 規則種類 Ex:年月日/機種別/跟隨...
			a_val.put((new JSONObject()).put("value", "廠商代號").put("key", "Factory"));
			a_val.put((new JSONObject()).put("value", "機種代號").put("key", "Model"));
			a_val.put((new JSONObject()).put("value", "年").put("key", "Year"));
			a_val.put((new JSONObject()).put("value", "月").put("key", "Month"));
			a_val.put((new JSONObject()).put("value", "ECN").put("key", "ECN"));
			a_val.put((new JSONObject()).put("value", "流水號").put("key", "Serial"));
			a_val.put((new JSONObject()).put("value", "固定值").put("key", "Fixed"));			
			//obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_Y, "col-md-2", true, a_vals, "wp_w_g_id", "工作站[ID]")); 
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "Factory", FFM.Wri.W_Y, "col-md-2", true, a_val, "prs_name", "規則名稱"));
			
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "8進制").put("key", "8進制"));
			a_val.put((new JSONObject()).put("value", "10進制").put("key", "10進制"));
			a_val.put((new JSONObject()).put("value", "12進制").put("key", "12進制"));
			a_val.put((new JSONObject()).put("value", "16進制").put("key", "16進制"));			
			a_val.put((new JSONObject()).put("value", "36進制").put("key", "36進制"));
			a_val.put((new JSONObject()).put("value", "跟隨").put("key", "Flow"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "8進制", FFM.Wri.W_Y, "col-md-2", true, a_val, "prs_type", "規則定義")); 			
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", false, n_val, "prs_length", "規則碼數")); //J 碼數
			
//			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, " ", " ", FFM.Wri.W_Y, "col-md-2", false, n_val, "w_c_name", "工作站[代碼]"));			
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val, "sys_note", "備註"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-1", true, n_val, "sys_sort", "排序")); // j 群組內欄位順序
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", "版本"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_N, "col-md-1", true, a_val, "sys_status", "狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "false", "false", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_header", "群組"));
			bean.setCell_modify(obj_m);

			// **********************放入群主指定 [(key)](modify/Create/Delete) 格式*************    ****群組頭 設定不顯示的欄位覆蓋***********  
			JSONArray obj_g_m = new JSONArray();
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_S, "col-md-1", "sys_sort", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-2", "prs_g_id", ""));

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-2", "prs_g_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "prs_c_name", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "prs_name", "")); //規則名稱
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "prs_type", "")); //規則定義			
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "prs_length", "")); //規則碼數	
			

			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "sys_sort", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_N, "col-md-1", "sys_ver", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_Y, FFM.Dno.D_S, "col-md-1", "sys_status", ""));
			obj_g_m.put(FFS.h_g(FFM.Wri.W_N, FFM.Dno.D_N, "col-md-1", "sys_header", "true"));

			bean.setCell_g_modify(obj_g_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "prs_g_name", "序號組[名稱]", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "prs_c_name", "序號[代碼]", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			prs_g_name = body.getJSONObject("search").getString("prs_g_name");
			prs_g_name = prs_g_name.equals("") ? null : prs_g_name;
			prs_c_name = body.getJSONObject("search").getString("prs_c_name");
			prs_c_name = prs_c_name.equals("") ? null : prs_c_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		
		pcbaRuleSns = prsDao.findAllByProgram(prs_g_name, prs_c_name, Integer.parseInt(status), true, page_r);
		List<Long> prsgid = new ArrayList<Long>();
		
		for (PcbaRuleSn obj : pcbaRuleSns) {
			String one = obj.getPrsgid().toString();
			prsgid.add(Long.parseLong(one));
		}
		pcbaRuleSns_son = prsDao.findAllByProgram(prs_g_name, prs_c_name, prsgid, false); //用ArrayList<PcbaRuleSn>接所有符合資料

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		JSONObject object_bodys_son = new JSONObject();
		pcbaRuleSns.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPrsgid());// 群組專用-必須放前面
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_id", one.getPrsid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_g_id", one.getPrsgid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_g_name", one.getPrsgname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_c_name", one.getPrscname());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wp_c_n_yield", one.getWpcnyield() == null ? "" : one.getWpcnyield());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wp_w_g_id", one.getWpwgid());
//			Workstation work = workstationDao.findAllByWgidOrderBySyssortAsc(one.getWpwgid()).get(0);
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_name", work.getWpbname());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_c_name", work.getWcname());
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_name", one.getPrsname()); //規則名稱Ex:年月日/機種別/跟隨....
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_type", one.getPrstype()); //規則定義 ex:10進制... 36進制或跟隨
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_length", one.getPrslength()); //規則碼數			
			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());

			object_bodys_son.put(one.getPrsgid() + "", new JSONArray());  //+""是轉字串 建立一個 "ID → 子資料陣列" 的 JSON Map 結構，方便之後把子資料分類存入。
			object_bodys.put(object_body);
			// 準備子類別容器
		});
		bean.setBody(new JSONObject().put("search", object_bodys));
		// son   **********************************************************j  RUD執行的顯示網頁內容
		pcbaRuleSns_son.forEach(one -> {
			JSONObject object_son = new JSONObject();
			int ord = 0;
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_header", one.getSysheader());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "ui_group_id", one.getPrsgid());// 群組專用-必須放前面
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_id", one.getPrsid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_g_id", one.getPrsgid());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_g_name", one.getPrsgname());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_c_name", one.getPrscname());
			
//			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wp_c_n_yield", one.getWpcnyield() == null ? "" : one.getWpcnyield());
//			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wp_w_g_id", one.getWpwgid());
//			Workstation work = workstationDao.findAllByWgidOrderBySyssortAsc(one.getWpwgid()).get(0);
//			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_pb_name", work.getWpbname());
//			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "w_c_name", work.getWcname());
			
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_name", one.getPrsname()); //規則名稱Ex:年月日/機種別/跟隨....
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_type", one.getPrstype()); //規則定義 ex:10進制... 36進制或跟隨
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "prs_length", one.getPrslength());	 //規則碼數		

			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_son.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_bodys_son.getJSONArray(one.getPrsgid() + "").put(object_son);
		});
		bean.setBody(bean.getBody().put("search_son", object_bodys_son));

		// 是否為群組模式? type:[group/general] || 新增時群組? createOnly:[all/general]
		bean.setBody_type(new JSONObject("{'type':'group','createOnly':'all'}"));  //J MODIFY 裡面的開頭群組
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		JSONArray list = body.getJSONArray("create");
		ArrayList<PcbaRuleSn> pcbaRuleSnS = new ArrayList<PcbaRuleSn>();
		PcbaRuleSn sys_wp_f = new PcbaRuleSn();
		PcbaRuleSn sys_wp = new PcbaRuleSn();
		try {
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;

				// Step2.檢查
				// 檢查名稱重複? 序號組[名稱] 序號組[名稱]
				pcbaRuleSnS = prsDao.findAllByPrsgnameOrPrscname(data.getString("prs_g_name"), data.getString("prs_c_name"));
				if (pcbaRuleSnS.size() > 0 && data.getBoolean("sys_header")) {
					return false;
				}
				
			//	 Step3.資料分類建置
				if (data.getBoolean("sys_header")) {
					// 父
					sys_wp_f = new PcbaRuleSn();
					Long prsgid = prsDao.getPcba_rule_sn_g_seq();
					sys_wp_f.setPrsgid(prsgid); //prs_g_id

					sys_wp_f.setPrsgname(data.getString("prs_g_name"));
					sys_wp_f.setPrscname(data.getString("prs_c_name"));				
					sys_wp_f.setPrsname(data.getString("prs_name"));
					sys_wp_f.setPrstype(data.getString("prs_type"));
					sys_wp_f.setPrslength(data.getLong("prs_length"));
					
					sys_wp_f.setSyssort(0);
					sys_wp_f.setSysnote("");
					sys_wp_f.setSysstatus(0);
					sys_wp_f.setSysheader(true);
					sys_wp_f.setSysmuser(user.getSuaccount());
					sys_wp_f.setSyscuser(user.getSuaccount());
					prsDao.save(sys_wp_f);				
				} else {
					// 如果為子目錄 非群組
					if (!sys_wp_f.getSysheader()) {
						sys_wp_f = prsDao.findAllByPrsgidAndSysheaderOrderBySyssortAsc(data.getLong("prs_g_id"), true).get(0);
					}
					// 子
					sys_wp = new PcbaRuleSn();
					sys_wp.setPrsgname(sys_wp_f.getPrsgname()); //序號組[名稱]
					sys_wp.setPrscname(sys_wp_f.getPrscname()); //序號[代碼]					
					sys_wp.setPrsgid(sys_wp_f.getPrsgid());  //群組ID			
					
					sys_wp.setPrsname(data.getString("prs_name"));
					sys_wp.setPrstype(data.getString("prs_type"));
					sys_wp.setPrslength(data.getLong("prs_length"));
					
					sys_wp.setSysnote("");
					sys_wp.setSyssort(data.getInt("sys_sort"));
					sys_wp.setSysstatus(data.getInt("sys_status"));
					//sys_wp.setWpwgid(data.getLong("wp_w_g_id"));
					sys_wp.setSysheader(false);
					sys_wp.setSysmuser(user.getSuaccount());
					sys_wp.setSyscuser(user.getSuaccount());
					prsDao.save(sys_wp);
				}
			}
			check = true;
		} catch (Exception e) {
		    e.printStackTrace();
		    throw e;
		}
		return check;
	}

	// 存檔 資料清單
	@Transactional
	public boolean save_asData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		JSONArray list = body.getJSONArray("save_as");
		ArrayList<PcbaRuleSn> pcbaRuleSnS = new ArrayList<PcbaRuleSn>();
		PcbaRuleSn sys_wp_f = new PcbaRuleSn();
		PcbaRuleSn sys_wp = new PcbaRuleSn();
		try {
			for (Object one : list) {
				// Step1. 物件_轉換
				JSONObject data = (JSONObject) one;
				// Step2.檢查
				// 檢查名稱重複?
				pcbaRuleSnS = prsDao.findAllByPrsgnameOrPrscname(data.getString("prs_g_name"), data.getString("prs_c_name"));
				if (pcbaRuleSnS.size() > 0 && data.getBoolean("sys_header")) {
					return false;
				}
				// Step3.資料分類建置
				if (data.getBoolean("sys_header")) {
					// 父
					sys_wp_f = new PcbaRuleSn();
					Long prsgid = prsDao.getPcba_rule_sn_g_seq();	
					
					sys_wp_f.setPrsgid(prsgid);					
					sys_wp_f.setPrsgname(data.getString("prs_g_name"));
					sys_wp_f.setPrscname(data.getString("prs_c_name"));				
					sys_wp_f.setPrsname(data.getString("prs_name"));
					sys_wp_f.setPrstype(data.getString("prs_type"));
					sys_wp_f.setPrslength(data.getLong("prs_length"));
					
					sys_wp_f.setSyssort(0);
					sys_wp_f.setSysnote("");
					sys_wp_f.setSysstatus(0);
					sys_wp_f.setSysheader(true);
					sys_wp_f.setSysmuser(user.getSuaccount());
					sys_wp_f.setSyscuser(user.getSuaccount());
					prsDao.save(sys_wp_f);
				} else {
					// 子
					sys_wp = new PcbaRuleSn();
					sys_wp.setPrsgname(sys_wp_f.getPrsgname()); //序號組[名稱]
					sys_wp.setPrscname(sys_wp_f.getPrscname()); //序號[代碼]					
					sys_wp.setPrsgid(sys_wp_f.getPrsgid());  //群組ID					
					sys_wp.setPrsname(data.getString("prs_name"));
					sys_wp.setPrstype(data.getString("prs_type"));
					sys_wp.setPrslength(data.getLong("prs_length"));
					
					sys_wp.setSysnote("");
					sys_wp.setSyssort(data.getInt("sys_sort"));
					sys_wp.setSysstatus(data.getInt("sys_status"));
//					sys_wp.setWpwgid(data.getLong("wp_w_g_id"));
					sys_wp.setSysheader(false);
					sys_wp.setSysmuser(user.getSuaccount());
					sys_wp.setSyscuser(user.getSuaccount());
					prsDao.save(sys_wp);
				}
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
			PcbaRuleSn sys_p_f = new PcbaRuleSn();
			PcbaRuleSn sys_p = new PcbaRuleSn();

			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				
				// 父類別 				
				if (data.getBoolean("sys_header")) {
					// 檢查名稱重複?  排除本身檢查有無重複名稱
					//prsgname = 指定名稱 prsgname = 指定名稱 id != 指定ID AND sysheader = true / false
					//SELECT * FROM pcba_rule_sn WHERE prsgname = ? AND id <> ? AND sysheader = ?
					if (prsDao.findAllByPrsgnameAndPrsidNotAndSysheader(data.getString("prs_g_name"), data.getLong("prs_id"),true).size() > 0 ||
						prsDao.findAllByPrscnameAndPrsidNotAndSysheader(data.getString("prs_c_name"), data.getLong("prs_id"),true).size() > 0) {
						return false;
					}		
					sys_p_f=prsDao.findAllByPrsgidAndSysheaderOrderBySyssortAsc(data.getLong("prs_g_id"),true).get(0);
			
					sys_p_f.setPrsgid(data.getLong("prs_g_id"));						
					sys_p_f.setPrsgname(data.getString("prs_g_name"));
					sys_p_f.setPrscname(data.getString("prs_c_name"));				
					sys_p_f.setPrsname("");
					sys_p_f.setPrstype("");
					sys_p_f.setPrslength(0L);
			
					sys_p_f.setSysnote("");
					sys_p_f.setSyssort(0);
					sys_p_f.setSysstatus(data.getInt("sys_status"));
					sys_p_f.setSysmuser(user.getSuaccount());
					sys_p_f.setSysmdate(new Date());
					sys_p_f.setSysheader(true);
					prsDao.save(sys_p_f);

					// 更新子類別 查詢全部 By Group 
					ArrayList<PcbaRuleSn> sys_p_s = prsDao.findAllByPrsgidOrderBySyssortAsc(data.getLong("prs_g_id"));
					sys_p_s.forEach(wp -> {
						wp.setPrsgname(data.getString("prs_g_name"));
						wp.setPrscname(data.getString("prs_c_name"));				
					});
					prsDao.saveAll(sys_p_s);
				} else {					
			
					sys_p=prsDao.findByPrsid(data.getLong("prs_id")).get(0);
			
					sys_p.setPrsname(data.getString("prs_name"));
					sys_p.setPrstype(data.getString("prs_type"));
					sys_p.setPrslength(data.getLong("prs_length"));
					
					sys_p.setSysnote("");
					sys_p.setSyssort(data.getInt("sys_sort"));
					sys_p.setSysstatus(data.getInt("sys_status"));
					sys_p.setSysmuser(user.getSuaccount());
					sys_p.setSysmdate(new Date());
					prsDao.save(sys_p);
				}
			}
			// 有更新才正確
			if (list.length() > 0) {
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
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
				
				if (data.getBoolean("sys_header")) {
					prsDao.deleteByPrsgid(data.getLong("prs_g_id"));
					continue;
				}			

				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
