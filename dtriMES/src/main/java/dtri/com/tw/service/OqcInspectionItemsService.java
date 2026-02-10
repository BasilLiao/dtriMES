package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.OqcInspectionItems;
//import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.OqcInspectionItemsDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class OqcInspectionItemsService {
	@Autowired
	private OqcInspectionItemsDao oIIDao;
	@Autowired
	private EntityManager entityManager;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		 int page = req.getPage_batch();
		int p_size = req.getPage_total();
		List<OqcInspectionItems> OqcInspectionItems = new ArrayList<OqcInspectionItems>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			// page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("id").descending());

		String oii_check_name = null;
		String oii_check_val = null;
		String oii_check_type = null;
		String oii_check_options = null;
		String oii_title_val = null;
		String sys_note = null;

		// 把資料庫轉的資料轉為JSON物件
		List<OqcInspectionItems> list = oIIDao.findAll();
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(list); // 每筆是物件，而不是字串
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_id", FFS.h_t("ID", "50px", FFM.Wri.W_N));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_title_val", FFS.h_t("標題值", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_check_name",FFS.h_t("檢查項目名稱", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_check_val", FFS.h_t("檢查內容值", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_check_type",FFS.h_t("檢查輸入類型", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "oii_check_options",	FFS.h_t("可自訂值", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val,"oii_id", "id")); // ID
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "Ex:Tablet/整合型液晶電腦/Cradle", "", FFM.Wri.W_Y,"col-md-2", true, n_val, "oii_title_val", "標題值"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "Ex:外觀/功能/配件/包裝/3C產品..", "", FFM.Wri.W_Y,	"col-md-2", true, n_val, "oii_check_name", "檢查項目名稱"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "Ex:空白(不填)/系統主機/CPU規格/其他選配", "", FFM.Wri.W_Y,"col-md-2", false, n_val, "oii_check_val", "檢查內容值"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "空白(不填)").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "一般(輸入)").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "下拉式(擇一)").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "勾選(多選)").put("key", "3"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, a_val,"oii_check_type", "檢查輸入類型"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "空白(不填)或下拉式/勾選 請用,區隔Ex:key_val,key_val", "",	FFM.Wri.W_Y, "col-md-3", false, n_val, "oii_check_options", "可自訂值"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.NUMB, "Ex:1/2/3..", "", FFM.Wri.W_Y, "col-md-1", true,	n_val, "sys_sort", "排序"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "鎖定").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "作廢").put("key", "2"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, a_val,"sys_status", "資料狀態"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12", false, n_val,"sys_note", "備註"));

			bean.setCell_modify(obj_m);

			// ****************************************** 放入包裝(search) 搜尋頁面
			// 抓出已在資料庫的"標題值"名單,不重複紀錄
			JSONArray object_searchs = new JSONArray();
			a_val = new JSONArray();
			OqcInspectionItems = oIIDao.findMinIdPerTitle();
			if (OqcInspectionItems != null && OqcInspectionItems.size() > 0) {
				for (OqcInspectionItems Oii : OqcInspectionItems) {
					JSONObject obj = new JSONObject();
					obj.put("value", Oii.getOiititleval());
					obj.put("key", Oii.getOiititleval());
					a_val.put(obj);
					System.out.println(obj);
				}
			}
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-2", "oii_title_val", "標題值", a_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oii_check_name", "檢查項目名稱", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oii_check_val", "檢查內容值", n_val));
			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "空白").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "一般").put("key", "1"));
			a_val.put((new JSONObject()).put("value", "下拉式").put("key", "2"));
			a_val.put((new JSONObject()).put("value", "勾選").put("key", "3"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "", "col-md-1", "oii_check_type", "檢查輸入類型", a_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "oii_check_options", "可自訂值", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "sys_note", "備註", n_val));

			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			oii_check_name = body.getJSONObject("search").getString("oii_check_name"); // "檢查項目名稱"
			oii_check_name = oii_check_name.equals("") ? null : oii_check_name;

			oii_check_val = body.getJSONObject("search").getString("oii_check_val").trim(); // "檢查內容值"
			oii_check_val = oii_check_val.equals("") ? null : oii_check_val;

			oii_check_type = body.getJSONObject("search").getString("oii_check_type").trim(); // "檢查輸入類型"
			oii_check_type = oii_check_type.equals("") ? null : oii_check_type;

			oii_check_options = body.getJSONObject("search").getString("oii_check_options").trim(); // "可自訂值"
			oii_check_options = oii_check_options.equals("") ? null : oii_check_options;

			oii_title_val = body.getJSONObject("search").getString("oii_title_val").trim(); // "標題值"
			oii_title_val = oii_title_val.equals("") ? null : oii_title_val;

			sys_note = body.getJSONObject("search").getString("sys_note").trim(); // "備註"
			sys_note = sys_note.equals("") ? null : sys_note;
		}

		OqcInspectionItems = oIIDao.findAllByOqcItems(oii_check_name, oii_check_val, oii_check_type, oii_check_options,
				oii_title_val, sys_note,page_r);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		OqcInspectionItems.forEach(one -> {

			int ord = 0;
			JSONObject object_body = new JSONObject();

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_id", one.getOiiid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_title_val", one.getOiititleval()); // 標題值
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_check_name", one.getOiicheckname()); // 檢查項目名稱
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_check_val", one.getOiicheckval()); // 檢查內容值 (如果是下拉式/勾選 Ex:[key_val,key_val]
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_check_type", one.getOiichecktype()); // 檢查輸入類型 0.空白 1.一般入 2.下拉式選單
			//String oiiCheckOptions = one.getOiicheckoptions().replaceAll("[\\{\\}\\[\\]\"]", "").trim();
			String oiiCheckOptions = one.getOiicheckoptions().replaceAll("[\\{\\}\\[\\]\"]|:true", "").trim();
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "oii_check_options", oiiCheckOptions); // 可自訂值 (如果是下拉式/勾選請用,區隔 Ex:[key_val,key_val]
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			// object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_bodys.put(object_body);

		});

		bean.setBody(new JSONObject().put("search", object_bodys));
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		try {
			JSONArray list = body.getJSONArray("create");
			// 建立 Set 去除重複標題值
			Set<String> titilevalSet = new HashSet<String>();

			// 相同的資料 只寫入一筆資料到 titilevalSet 清單列
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				titilevalSet.add(data.optString("oii_title_val", "").trim());
			}

			for (String titileval : titilevalSet) {
				// 1.先找尋資料庫有無相同的標題
				List<OqcInspectionItems> items = oIIDao.findAllByOiititleval(titileval);
				Set<Integer> sortSet = new HashSet<>();

				// 先把資料庫的排序放入Set集合
				for (OqcInspectionItems item : items) {
					sortSet.add(item.getSyssort());
					System.out.println("排序為" + sortSet);
				}
				// 再把前端同標題的排序號碼 加入set 做辨別
				for (Object one : list) {
					JSONObject data = (JSONObject) one;
					if (titileval.equals(data.optString("oii_title_val", "").trim())) {
						System.out.println("相同標題");

						int num = data.optInt("sys_sort", 0); // 排序
						// 如有重複跳出,並顯示重複的號碼資料
						if (!sortSet.add(num)) {
							check = false;
							resp.setError_ms(titileval + "標題內的排序" + num + "號碼重複了");
							resp.autoMsssage("109"); // 回傳錯誤訊息
							return check;
						}
					}
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				OqcInspectionItems oII = new OqcInspectionItems();
				// System.out.println(data.get(null));
				String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
				String oii_check_name = data.optString("oii_check_name", "").trim(); // 檢查項目名稱
				String oii_check_val = data.optString("oii_check_val", "").trim(); // 檢查內容值
				String oii_check_type = data.optString("oii_check_type", "").trim(); // 檢查輸入類型
				String oii_check_options = data.optString("oii_check_options", "").trim(); // 可自訂值
				int sys_sort = data.optInt("sys_sort", 0); // 排序
				int sys_status = data.optInt("sys_status", 0); // 資料狀態
				String sys_note = data.optString("sys_note", "").trim(); // 備註

				oII.setOiititleval(oii_title_val); // 標題值
				oII.setOiicheckname(oii_check_name); // 檢查項目名稱
				oII.setOiicheckval(oii_check_val); // 檢查內容值
				oII.setOiichecktype(oii_check_type); // 檢查輸入類型

				// 可自訂值 轉成 Object或Array
				String jsonOptions = null;
				if ("3".equals(data.getString("oii_check_type"))) { // 空白
					String[] array = oii_check_options.split(","); //on,off,na
					jsonOptions = mapper.writeValueAsString(array); //Java → JSON
					oII.setOiicheckoptions(jsonOptions); // 可自訂值

				} else if ("2".equals(data.getString("oii_check_type"))) {
					String[] array = oii_check_options.split("\\s*,\\s*");
					Map<String, Boolean> map = new LinkedHashMap<>();
					for (String item : array) {
						map.put(item, true); // 或預設為 false、空字串等
					}
					jsonOptions = mapper.writeValueAsString(map); //Java → JSON
					oII.setOiicheckoptions(jsonOptions); // 可自訂值

				} else {
					// 0,1 維持原樣
					oII.setOiicheckoptions(oii_check_options); // 可自訂值
				}

				oII.setSyssort(sys_sort); // 排序
				oII.setSysstatus(sys_status); // 資料狀態
				oII.setSysnote(sys_note); // 備註
				oII.setSyscuser(user.getSuaccount()); // 建立
				oII.setSysmuser(user.getSuaccount()); // 修改

				oIIDao.save(oII);
			}
			check = true;
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
			// 建立 Set 去除重複標題值
			Set<String> titilevalSet = new HashSet<String>();

			// 相同的資料 只寫入一筆資料到 titilevalSet 清單列
			for (Object one : list) {
				JSONObject data = (JSONObject) one;
				titilevalSet.add(data.optString("oii_title_val", "").trim());
			}

			for (String titileval : titilevalSet) {
				// 1.先找尋資料庫有無相同的標題
				List<OqcInspectionItems> items = oIIDao.findAllByOiititleval(titileval);
				Set<Integer> sortSet = new HashSet<>();

				// 先把資料庫的排序放入Set集合
				for (OqcInspectionItems item : items) {
					sortSet.add(item.getSyssort());
					System.out.println("排序為" + sortSet);
				}
				// 再把前端同標題的排序號碼 加入set 做辨別
				for (Object one : list) {
					JSONObject data = (JSONObject) one;
					if (titileval.equals(data.optString("oii_title_val", "").trim())) {
						System.out.println("相同標題");

						int num = data.optInt("sys_sort", 0); // 排序
						// 如有重複跳出,並顯示重複的號碼資料
						if (!sortSet.add(num)) {
							check = false;
							resp.setError_ms(titileval + "標題內的排序" + num + "號碼重複了");
							resp.autoMsssage("109"); // 回傳錯誤訊息
							return check;
						}
					}
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				OqcInspectionItems sys_c = new OqcInspectionItems();
				// System.out.println(data.get(null));
				String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
				String oii_check_name = data.optString("oii_check_name", "").trim(); // 檢查項目名稱
				String oii_check_val = data.optString("oii_check_val", "").trim(); // 檢查內容值
				String oii_check_type = data.optString("oii_check_type", "").trim(); // 檢查輸入類型
				String oii_check_options = data.optString("oii_check_options", "").trim(); // 可自訂值
				int sys_sort = data.optInt("sys_sort", 0); // 排序
				int sys_status = data.optInt("sys_status", 0); // 資料狀態
				String sys_note = data.optString("sys_note", "").trim(); // 備註

				sys_c.setOiititleval(oii_title_val); // 標題值
				sys_c.setOiicheckname(oii_check_name); // 檢查項目名稱
				sys_c.setOiicheckval(oii_check_val); // 檢查內容值
				sys_c.setOiichecktype(oii_check_type); // 檢查輸入類型

				// 可自訂值 轉成 Object或Array
				String jsonOptions = null;
				if ("3".equals(data.getString("oii_check_type"))) { // 空白
					String[] array = oii_check_options.split(",");
					jsonOptions = mapper.writeValueAsString(array);
					sys_c.setOiicheckoptions(jsonOptions); // 可自訂值

				} else if ("2".equals(data.getString("oii_check_type"))) {
					String[] array = oii_check_options.split("\\s*,\\s*");
					Map<String, Boolean> map = new LinkedHashMap<>();
					for (String item : array) {
						map.put(item, true); // 或預設為 false、空字串等
					}
					jsonOptions = mapper.writeValueAsString(map);
					sys_c.setOiicheckoptions(jsonOptions); // 可自訂值

				} else {
					// 0,1 維持原樣
					sys_c.setOiicheckoptions(oii_check_options); // 可自訂值
				}

				sys_c.setSyssort(sys_sort); // 排序
				sys_c.setSysstatus(sys_status); // 資料狀態
				sys_c.setSysnote(sys_note); // 備註
				sys_c.setSyscuser(user.getSuaccount()); // 建立
				sys_c.setSysmuser(user.getSuaccount()); // 修改

				oIIDao.save(sys_c);
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
			System.out.println(list);
			// 建立 Set 去除重複標題值 和 建立 Map 儲存 前端JSON資料
			Set<String> titilevalSet = new HashSet<String>();
			Map<Long, JSONObject> dataMap = new HashMap<>(); // oii_id -> JSONObject
			ObjectMapper mapper = new ObjectMapper();

			// 1.先整理前端過來的標題去除重複 及 儲存JSON資料到 dataMap
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				titilevalSet.add(data.optString("oii_title_val", "").trim());
				dataMap.put(data.optLong("oii_id"), data);
			}

			// 2.依"標題值"取 依序取出每張表單
			for (String titileval : titilevalSet) {
				System.out.println(titileval);
				// 3.取出符合標題值讀表單
				List<OqcInspectionItems> items = oIIDao.findAllByOiititleval(titileval);
					//把相同標題的 sort 號碼全部放入SET辨別有無重複
				Set<Integer> sortSet = new HashSet<>();
				// 4.依序取出表單的每一列資料
				for (OqcInspectionItems item : items) {
					// 只要：
					// 1.方法有 @Transactional
					// 2.該物件是 findById() 查出來的（被 Hibernate 管理）
					// 3.改完屬性後沒有 rollback
					// → 就會自動更新！
					// （Entity）已經是「持久化狀態」（Persistent / Managed Entity）。在這種狀況下，只要你對這個 entity 呼叫
					// setXXX() 改變屬性值，
					entityManager.detach(item); // 使用分離狀態（detach）,避免被直接修改資料庫資料
					// 5.表單的每一列ID 與 前端進來的 ID資料 依序比對有無符合 ,有相同進行更新資料
				
					JSONObject data = dataMap.get(item.getOiiid()); // 取的table表的一列ID去對應到MAP索引ID後得到的JSON物件資料
					if (data == null)
						continue; // 無對應資料 跳過這次迴圈
					// 取出欄位資料
					String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
					String oii_check_name = data.optString("oii_check_name", "").trim();// 檢查項目名稱
					String oii_check_val = data.optString("oii_check_val", "").trim(); // 檢查內容值
					String oii_check_type = data.optString("oii_check_type", "").trim(); // 檢查輸入類型
					String oii_check_options = data.optString("oii_check_options", "").trim(); // 可自訂值
					int sys_sort = data.optInt("sys_sort", 0); // 排序
					String sys_note = data.optString("sys_note", "").trim(); // 備註
					int sys_status = data.getInt("sys_status"); // 資料狀態
					item.setOiititleval(oii_title_val); // 標題值
					item.setOiicheckname(oii_check_name); // 檢查項目名稱
					item.setOiicheckval(oii_check_val); // 檢查內容值
					item.setOiichecktype(oii_check_type); // 檢查輸入類型
					// 轉成 Object或Array					
					String jsonOptions = null;
					if ("3".equals(data.getString("oii_check_type"))) { // 空白
						String[] array = oii_check_options.split(",");
						jsonOptions = mapper.writeValueAsString(array);  //Java → JSON 
						item.setOiicheckoptions(jsonOptions); // 可自訂值

					} else if ("2".equals(data.getString("oii_check_type"))) {
						String[] array = oii_check_options.split("\\s*,\\s*");
						Map<String, Boolean> map = new LinkedHashMap<>();
						for (String oiiitem : array) {
							map.put(oiiitem, true); // 或預設為 false、空字串等
						}
						jsonOptions = mapper.writeValueAsString(map);
						item.setOiicheckoptions(jsonOptions);// 可自訂值

					} else {
						// 0,1 維持原樣
						item.setOiicheckoptions(oii_check_options); // 可自訂值
					}
					item.setSyssort(sys_sort); // 排序
					item.setSysstatus(sys_status); // 資料狀態
					item.setSysnote(sys_note); // 備註
					item.setSysmuser(user.getSuaccount()); // 修改人
					item.setSysmdate(new Date()); // 修改時間
				}
			
				// 6.待原資料庫表單資料全部填入更新後 依序檢查sort有無重複			
				for (OqcInspectionItems item : items) {
					int num = item.getSyssort();
					// 如有重複跳出,並顯示重複的號碼資料
					if (!sortSet.add(num)) {
						check = false;
						item.getOiicheckval();
						resp.setError_ms(titileval + "標題內的檢查內容值的"+item.getOiicheckval()+"排序" + num + "號碼重複了");
						resp.autoMsssage("109"); // 回傳錯誤訊息
						return check;
					}				
				}
				
				//7.有更改標題名稱的資料(不在上面的items表單) 辦判SORT號碼有無重複
				OqcInspectionItems oII = new OqcInspectionItems();
				for (Object one : list) {
					// 物件轉換
					JSONObject data = (JSONObject) one;
					String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
					//用前端送來的ID去找資料庫 原始的 標題名稱
					oII = oIIDao.findAllByOiiid(data.getLong("oii_id")).get(0);					
					String titile=oII.getOiititleval();
					//JSON標題名稱 與 資料庫名稱不一樣時 (表示為有更改標題名稱的資料) 且 此JSON的標題名稱 與 外圈 titilevalSet標題 相同
					if(!titile.equals(titileval) && titileval.equals(oii_title_val)) {
						int num = data.optInt("sys_sort", 0); // 排序
						if (!sortSet.add(num)) {
							check = false;
							resp.setError_ms(titileval + "標題內的排序" + num + "號碼重複了");
							resp.autoMsssage("109"); // 回傳錯誤訊息
							return check;						
						}
					}				
				}
				// 8. 在7遍歷SORT沒問題後才進行更新
				for(Object one : list) {
					JSONObject data = (JSONObject) one;	
					oII = oIIDao.findAllByOiiid(data.getLong("oii_id")).get(0);	
					entityManager.detach(oII); // 使用分離狀態（detach）,避免被直接修改資料庫資料
					String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
					String titile=oII.getOiititleval();
					//當此迴圈的JSON名稱 與 資料庫名稱不一樣時 表示為有更改標題名稱的資料
					if(!titile.equals(titileval) && titileval.equals(oii_title_val)) {
						// 取出欄位資料
						//String oii_title_val = data.optString("oii_title_val", "").trim(); // 標題值
						String oii_check_name = data.optString("oii_check_name", "").trim();// 檢查項目名稱
						String oii_check_val = data.optString("oii_check_val", "").trim(); // 檢查內容值
						String oii_check_type = data.optString("oii_check_type", "").trim(); // 檢查輸入類型
						String oii_check_options = data.optString("oii_check_options", "").trim(); // 可自訂值
						int sys_sort = data.optInt("sys_sort", 0); // 排序
						String sys_note = data.optString("sys_note", "").trim(); // 備註
						int sys_status = data.getInt("sys_status"); // 資料狀態
						oII.setOiititleval(oii_title_val); // 標題值
						oII.setOiicheckname(oii_check_name); // 檢查項目名稱
						oII.setOiicheckval(oii_check_val); // 檢查內容值
						oII.setOiichecktype(oii_check_type); // 檢查輸入類型
						// 轉成 Object或Array					
						String jsonOptions = null;
						if ("3".equals(data.getString("oii_check_type"))) { // 空白
							String[] array = oii_check_options.split(",");
							jsonOptions = mapper.writeValueAsString(array);
							oII.setOiicheckoptions(jsonOptions); // 可自訂值
	
						} else if ("2".equals(data.getString("oii_check_type"))) {
							String[] array = oii_check_options.split("\\s*,\\s*");
							Map<String, Boolean> map = new LinkedHashMap<>();
							for (String oiiitem : array) {
								map.put(oiiitem, true); // 或預設為 false、空字串等
							}
							jsonOptions = mapper.writeValueAsString(map);
							oII.setOiicheckoptions(jsonOptions);// 可自訂值
	
						} else {
							// 0,1 維持原樣
							oII.setOiicheckoptions(oii_check_options); // 可自訂值
						}
						oII.setSyssort(sys_sort); // 排序
						oII.setSysstatus(sys_status); // 資料狀態
						oII.setSysnote(sys_note); // 備註
						oII.setSysmuser(user.getSuaccount()); // 修改人
						oII.setSysmdate(new Date()); // 修改時間							
						oIIDao.save(oII);
					}
				}			
				
				// 更新
				oIIDao.saveAll(items);
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
				Long x = data.getLong("oii_id");
				oIIDao.deleteById(x);
			}
			check = true;

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
