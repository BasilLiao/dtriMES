package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.Customer;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.CustomerDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class CustomerService {
	@Autowired
	private CustomerDao customerDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean resp, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<Customer> customers = new ArrayList<Customer>();

		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("ccname").descending());
		String c_name = null;
		String c_c_name = null;
		String status = "0";
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {
			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_c_name", FFS.h_t("客戶公司", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_name", FFS.h_t("客戶姓名", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_address", FFS.h_t("客戶地址", "350px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_tex", FFS.h_t("客戶電話", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "c_fax", FFS.h_t("客戶傳真", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "item_date", FFS.h_t("建立時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "item_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			resp.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			JSONArray n_val = new JSONArray();
			JSONArray a_val = new JSONArray();

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "c_id", "ID"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, n_val, "c_c_name", "客戶公司"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "c_name", "客戶姓名"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-3", false, n_val, "c_address", "客戶地址"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "c_tex", "客戶電話"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "c_fax", "客戶傳真"));

			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "0", "0", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_sort", "排序"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.NUMB, "", "", FFM.Wri.W_N, "col-md-1", false, n_val, "sys_ver", "版本"));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-1", true, a_val, "sys_status", "狀態"));
			resp.setCell_modify(obj_m);

			// 放入包裝(search)
			JSONArray object_searchs = new JSONArray();
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "c_c_name", "客戶公司", n_val));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "c_name", "客戶姓名", n_val));

			a_val = new JSONArray();
			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
			resp.setCell_searchs(object_searchs);
		} else {

			// 進行-特定查詢
			c_c_name = body.getJSONObject("search").getString("c_c_name");
			c_c_name = c_c_name.equals("") ? null : c_c_name;
			c_name = body.getJSONObject("search").getString("c_name");
			c_name = c_name.equals("") ? null : c_name;
			status = body.getJSONObject("search").getString("sys_status");
			status = status.equals("") ? "0" : status;
		}
		customers = customerDao.findAllByCustomer(0L, c_c_name, c_name, Integer.parseInt(status), page_r);

		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		customers.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_id", one.getCid());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_c_name", one.getCcname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_name", one.getCname()== null ? "" : one.getCname());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_address", one.getCaddress() == null ? "" : one.getCaddress());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_tex", one.getCtex() == null ? "" : one.getCtex());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "c_fax", one.getCfax() == null ? "" : one.getCfax());

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote() == null ? "" : one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "item_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "item_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
			object_bodys.put(object_body);
		});
		resp.setBody(new JSONObject().put("search", object_bodys));
		return true;
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
				Customer item = new Customer();
				JSONObject data = (JSONObject) one;
				item.setCname(data.getString("c_name"));
				item.setCcname(data.getString("c_c_name"));
				item.setCaddress(data.getString("c_address"));
				item.setCtex(data.getString("c_tex"));
				item.setCfax(data.getString("c_fax"));

				item.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
				item.setSyssort(data.getInt("sys_sort"));
				item.setSysstatus(data.getInt("sys_status"));
				item.setSysmuser(user.getSuaccount());
				item.setSyscuser(user.getSuaccount());

				// 檢查名稱重複
				ArrayList<Customer> items = customerDao.findAllByCustomer(0L, item.getCcname(), null, 0, PageRequest.of(0, 1));
				if (items != null && items.size() > 0) {
					return check;
				}
				customerDao.save(item);
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
			for (Object one : list) {
				// 物件轉換
				Customer item = new Customer();
				JSONObject data = (JSONObject) one;
				item.setCname(data.getString("c_name"));
				item.setCcname(data.getString("c_c_name"));
				item.setCaddress(data.getString("c_address"));
				item.setCtex(data.getString("c_tex"));
				item.setCfax(data.getString("c_fax"));

				item.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
				item.setSyssort(data.getInt("sys_sort"));
				item.setSysstatus(data.getInt("sys_status"));
				item.setSysmuser(user.getSuaccount());
				item.setSyscuser(user.getSuaccount());

				// 檢查名稱重複
				ArrayList<Customer> items = customerDao.findAllByCustomer(0L, item.getCcname(), null, 0, PageRequest.of(0, 1));
				if (items != null && items.size() > 0) {
					return check;
				}
				customerDao.save(item);
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
				// 物件轉換
				Customer item = new Customer();
				JSONObject data = (JSONObject) one;
				ArrayList<Customer> items = customerDao.findAllByCid(data.getLong("c_id"));
				if (items.size() == 1) {
					item = items.get(0);
					item.setCname(data.getString("c_name"));
					item.setCcname(data.getString("c_c_name"));
					item.setCaddress(data.getString("c_address"));
					item.setCtex(data.getString("c_tex"));
					item.setCfax(data.getString("c_fax"));

					item.setSysnote(data.has("sys_note") ? data.getString("sys_note") : "");
					item.setSyssort(data.getInt("sys_sort"));
					item.setSysstatus(data.getInt("sys_status"));
					item.setSysmuser(user.getSuaccount());
					item.setSysmdate(new Date());
					customerDao.save(item);
					check = true;
				}
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
				customerDao.deleteBycid(data.getLong("c_id"));
				check = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}
