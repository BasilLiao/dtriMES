package dtri.com.tw.service;

import org.springframework.stereotype.Service;

@Service
public class WorkstationItemService {
	/*
	 * 遺棄功能模組
	 */
//	@Autowired
//	private WorkstationItemDao itemDao;
//	@Autowired
//	private ProductionBodyDao bodyDao;
//
//	// 取得當前 資料清單
//	public PackageBean getData(JSONObject body, int page, int p_size) {
//		PackageBean bean = new PackageBean();
//		ArrayList<WorkstationItem> workstationItems = new ArrayList<WorkstationItem>();
//
//		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
//		if (p_size < 1) {
//			page = 0;
//			p_size = 100;
//		}
//		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("wiid").descending());
//		String wi_pb_cell = null;
//		String wi_pb_value = null;
//		String status = "0";
//		// 初次載入需要標頭 / 之後就不用
//		if (body == null || body.isNull("search")) {
//			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
//			JSONObject object_header = new JSONObject();
//			int ord = 0;
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wi_id", FFS.h_t("ID", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wi_pb_cell", FFS.h_t("項目欄位", "150px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "wi_pb_value", FFS.h_t("項目名稱", "150px", FFM.Wri.W_Y));
//
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "150px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "150px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "100px", FFM.Wri.W_Y));
//
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
//			bean.setHeader(object_header);
//
//			// 放入修改 [(key)](modify/Create/Delete) 格式
//			JSONArray obj_m = new JSONArray();
//			JSONArray n_val = new JSONArray();
//			JSONArray a_val = new JSONArray();
//
//			obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, n_val, "wi_id", "ID"));
//
//			// sn關聯表
//			int j = 0;
//			Method method;
//			ProductionBody body_one = bodyDao.findAllByPbid(0).get(0);
//			for (j = 0; j < 50; j++) {
//				String m_name = "getPbvalue" + String.format("%02d", j + 1);
//				try {
//					method = body_one.getClass().getMethod(m_name);
//					String value = (String) method.invoke(body_one);
//					String name = "pb_value" + String.format("%02d", j + 1);
//					if (value != null && !value.equals("")) {
//						a_val.put((new JSONObject()).put("value", value).put("key", name));
//					}
//				} catch (NoSuchMethodException e) {
//					e.printStackTrace();
//				} catch (SecurityException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
//
//			obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, a_val, "wi_pb_cell", "項目欄位"));
//			obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, n_val, "wi_pb_value", "項目名稱"));
//
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N,
//			// "col-md-2", false,
//			// n_val, "sys_c_date", "建立時間"));
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N,
//			// "col-md-2", false,
//			// n_val, "sys_c_user", "建立人"));
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N,
//			// "col-md-2", false,
//			// n_val, "sys_m_date", "修改時間"));
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N,
//			// "col-md-2", false,
//			// n_val, "sys_m_user", "修改人"));
//
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFS.TTA, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-12",
//			// false,
//			// n_val, "sys_note", "備註"));
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFS.NUMB, "0", "0", FFM.Wri.W_Y, "col-md-2",
//			// true,
//			// n_val, "sys_sort", "排序"));
//			// obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.INP, FFS.NUMB, "", "", FFM.Wri.W_N, "col-md-2",
//			// false,
//			// n_val, "sys_ver", "版本"));
//
//			a_val = new JSONArray();
//			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
//			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
//			obj_m.put(FFS.h_m(FFM.Dno.D_S,FFM.Tag.SEL, FFM.Type.TEXT, "", "0", FFM.Wri.W_Y, "col-md-2", true, a_val, "sys_status", "狀態"));
//			bean.setCell_modify(obj_m);
//
//			// 放入包裝(search)
//			JSONArray object_searchs = new JSONArray();
//			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "wi_pb_cell", "項目欄位", n_val));
//			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "wi_pb_value", "項目名稱", n_val));
//
//			a_val = new JSONArray();
//			a_val.put((new JSONObject()).put("value", "正常").put("key", "0"));
//			a_val.put((new JSONObject()).put("value", "異常").put("key", "1"));
//			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-2", "sys_status", "狀態", a_val));
//			bean.setCell_searchs(object_searchs);
//		} else {
//
//			// 進行-特定查詢
//			wi_pb_cell = body.getJSONObject("search").getString("wi_pb_cell");
//			wi_pb_cell = wi_pb_cell.equals("") ? null : wi_pb_cell;
//			wi_pb_value = body.getJSONObject("search").getString("wi_pb_value");
//			wi_pb_value = wi_pb_value.equals("") ? null : wi_pb_value;
//			status = body.getJSONObject("search").getString("sys_status");
//			status = status.equals("") ? "0" : status;
//		}
//		workstationItems = itemDao.findAllByWorkstationItem(wi_pb_cell, wi_pb_value, Integer.parseInt(status), page_r);
//
//		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
//		JSONArray object_bodys = new JSONArray();
//		workstationItems.forEach(one -> {
//			JSONObject object_body = new JSONObject();
//			int ord = 0;
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wi_id", one.getWiid());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wi_pb_cell", one.getWipbcell());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "wi_pb_value", one.getWipbvalue());
//
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate()));
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
//			object_bodys.put(object_body);
//		});
//		bean.setBody(new JSONObject().put("search", object_bodys));
//		return bean;
//	}
//
//	// 存檔 資料清單
//	@Transactional
//	public boolean createData(JSONObject body, SystemUser user) {
//		boolean check = false;
//		try {
//			JSONArray list = body.getJSONArray("create");
//			for (Object one : list) {
//				// 物件轉換
//				WorkstationItem sys_c = new WorkstationItem();
//				JSONObject data = (JSONObject) one;
//
//				// sn關聯表
//				String wi_pb_cell = "";
//				int j = 0;
//				Method method;
//				ProductionBody body_one = bodyDao.findAllByPbid(0).get(0);
//				for (j = 0; j < 50; j++) {
//					String m_name = "getPbvalue" + String.format("%02d", j + 1);
//					try {
//						method = body_one.getClass().getMethod(m_name);
//						String value = (String) method.invoke(body_one);
//						String name = "pb_value" + String.format("%02d", j + 1);
//						if (value != null && !value.equals("") && data.getString("wi_pb_cell").equals(name)) {
//							wi_pb_cell = value;
//						}
//					} catch (NoSuchMethodException e) {
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						e.printStackTrace();
//					}
//				}
//
//				sys_c.setWipbcell(data.getString("wi_pb_cell"));
//				sys_c.setWipbvalue(wi_pb_cell);
//				sys_c.setSysnote("");
//				sys_c.setSyssort(0);
//				sys_c.setSysstatus(data.getInt("sys_status"));
//				sys_c.setSysmuser(user.getSuaccount());
//				sys_c.setSyscuser(user.getSuaccount());
//
//				// 檢查名稱重複
//				ArrayList<WorkstationItem> sys_p_g = itemDao.findAllByWorkstationItem(sys_c.getWipbcell(), sys_c.getWipbvalue(), 1,
//						PageRequest.of(0, 1));
//				if (sys_p_g.size() == 0) {
//					itemDao.save(sys_c);
//				} else {
//					return false;
//				}
//			}
//			check = true;
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		return check;
//	}
//
//	// 存檔 資料清單
//	@Transactional
//	public boolean save_asData(JSONObject body, SystemUser user) {
//		boolean check = false;
//		try {
//			JSONArray list = body.getJSONArray("save_as");
//			for (Object one : list) {
//				// 物件轉換
//				WorkstationItem sys_c = new WorkstationItem();
//				JSONObject data = (JSONObject) one;
//
//				// sn關聯表
//				String wi_pb_cell = "";
//				int j = 0;
//				Method method;
//				ProductionBody body_one = bodyDao.findAllByPbid(0).get(0);
//				for (j = 0; j < 50; j++) {
//					String m_name = "getPbvalue" + String.format("%02d", j + 1);
//					try {
//						method = body_one.getClass().getMethod(m_name);
//						String value = (String) method.invoke(body_one);
//						String name = "pb_value" + String.format("%02d", j + 1);
//						if (value != null && !value.equals("") && data.getString("wi_pb_cell").equals(name)) {
//							wi_pb_cell = value;
//						}
//					} catch (NoSuchMethodException e) {
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						e.printStackTrace();
//					}
//				}
//
//				sys_c.setWipbcell(data.getString("wi_pb_cell"));
//				sys_c.setWipbvalue(wi_pb_cell);
//				sys_c.setSysnote("");
//				sys_c.setSyssort(0);
//				sys_c.setSysstatus(data.getInt("sys_status"));
//				sys_c.setSysmuser(user.getSuaccount());
//				sys_c.setSyscuser(user.getSuaccount());
//
//				// 檢查名稱重複
//				ArrayList<WorkstationItem> sys_p_g = itemDao.findAllByWorkstationItem(sys_c.getWipbcell(), sys_c.getWipbvalue(), 1,
//						PageRequest.of(0, 1));
//				if (sys_p_g.size() == 0) {
//					itemDao.save(sys_c);
//				} else {
//					return false;
//				}
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		return check;
//	}
//
//	// 更新 資料清單
//	@Transactional
//	public boolean updateData(JSONObject body, SystemUser user) {
//		boolean check = false;
//		try {
//			JSONArray list = body.getJSONArray("modify");
//			for (Object one : list) {
//				// 物件轉換
//				WorkstationItem sys_p = new WorkstationItem();
//				JSONObject data = (JSONObject) one;
//				// sn關聯表
//				String wi_pb_cell = "";
//				int j = 0;
//				Method method;
//				ProductionBody body_one = bodyDao.findAllByPbid(0).get(0);
//				for (j = 0; j < 50; j++) {
//					String m_name = "getPbvalue" + String.format("%02d", j + 1);
//					try {
//						method = body_one.getClass().getMethod(m_name);
//						String value = (String) method.invoke(body_one);
//						String name = "pb_value" + String.format("%02d", j + 1);
//						if (value != null && !value.equals("") && data.getString("wi_pb_cell").equals(name)) {
//							wi_pb_cell = value;
//						}
//					} catch (NoSuchMethodException e) {
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						e.printStackTrace();
//					}
//				}
//				sys_p.setWiid(data.getInt("wi_id"));
//				sys_p.setWipbcell(data.getString("wi_pb_cell"));
//				sys_p.setWipbvalue(wi_pb_cell);
//				sys_p.setSysnote("");
//				sys_p.setSyssort(0);
//				sys_p.setSysstatus(data.getInt("sys_status"));
//				sys_p.setSysmuser(user.getSuaccount());
//				sys_p.setSysmdate(new Date());
//
//				// 檢查名稱重複
//				ArrayList<WorkstationItem> sys_p_g = itemDao.findAllByWorkstationItem(sys_p.getWipbcell(), sys_p.getWipbvalue(), 1,
//						PageRequest.of(0, 1));
//				if (sys_p_g.size() == 0) {
//					itemDao.save(sys_p);
//				} else {
//					return false;
//				}
//			}
//			// 有更新才正確
//			if (list.length() > 0) {
//				check = true;
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//			return false;
//		}
//		return check;
//	}
//
//	// 移除 資料清單
//	@Transactional
//	public boolean deleteData(JSONObject body) {
//
//		boolean check = false;
//		try {
//			JSONArray list = body.getJSONArray("delete");
//			for (Object one : list) {
//				// 物件轉換
//				WorkstationItem sys_p = new WorkstationItem();
//				JSONObject data = (JSONObject) one;
//				sys_p.setWiid(data.getInt("wi_id"));
//
//				itemDao.deleteByWiidAndSysheader(sys_p.getWiid(), false);
//				check = true;
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//			return false;
//		}
//		return check;
//	}
}
