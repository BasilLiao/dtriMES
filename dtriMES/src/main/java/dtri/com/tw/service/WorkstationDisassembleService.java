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
import dtri.com.tw.db.entity.ProductionBody;
import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.ProductionBodyDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;

@Service
public class WorkstationDisassembleService {
	@Autowired
	private ProductionHeaderDao headerDao;
	@Autowired
	private ProductionBodyDao bodyDao;
	@Autowired
	private WorkstationProgramDao programDao;
	@Autowired
	private WorkstationDao workDao;
	@Autowired
	private EntityManager em;

	// 取得當前 產品項目資料清單
	public boolean getProductionBodyData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		// JSONObject body = req.getBody();

		// 取得清單
		JSONObject secrch = new JSONObject();
		ProductionBody body_one = bodyDao.findAllByPbid(0l).get(0);
		Method method;
		for (int j = 0; j < 50; j++) {
			String m_name = "getPbvalue" + String.format("%02d", j + 1);
			try {
				method = body_one.getClass().getMethod(m_name);
				String value = (String) method.invoke(body_one);
				String name = "setPbvalue" + String.format("%02d", j + 1);
				if (value != null && !value.equals("")) {
					secrch.put(name, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (secrch.length() != 0) {
			bean.setBody(new JSONObject().put("search", secrch));
		} else {
			bean.autoMsssage("102");
			return false;
		}
		return true;
	}

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		// int page = req.getPage_batch();
		// int p_size = req.getPage_total();
		List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
		List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();
		// 進行-特定查詢(拆解工單)
		String now_order = body.getJSONObject("search").getString("m_now_order");
		String m_old_sn = body.getJSONObject("search").getString("m_old_sn");

		ProductionRecords phprid = new ProductionRecords();
		phprid.setPrid(now_order);
		prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A431_disassemble");

		// u資料
		if (prArrayList.size() == 1) {
			// 查詢多少台
			Long phpbgid = prArrayList.get(0).getPhpbgid();
			List<ProductionBody> bodies = bodyDao.findAllByPbgidAndPbbsnNotOrderByPbsnAsc(phpbgid, "no_sn");
			List<ProductionBody> bodies_old = bodyDao.findAllByPbbsn(m_old_sn);

			// 如果有-> 回傳原先製令單
			String m_old_order = "";
			if (bodies_old.size() == 1) {
				prArrayList_old = headerDao.findAllByPhpbgid(bodies_old.get(0).getPbgid());
				m_old_order = prArrayList_old.size() > 0 ? prArrayList_old.get(0).getProductionRecords().getPrid() : "";
			}

			bean.setBody(new JSONObject().put("search", new JSONObject().//
					put("phpnumber_total", prArrayList.get(0).getPhpqty()).//
					put("phpnumber_register", bodies.size()).//
					put("m_old_order", m_old_order).//
					put("check", true)));//
		} else {
			bean.autoMsssage("102");
		}
		return true;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 新建的資料
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			String now_order = body.getJSONObject("create").getString("m_now_order");
			String m_old_sn = body.getJSONObject("create").getString("m_old_sn");
			ProductionRecords phprid = new ProductionRecords();

			// 進行-特定查詢(重工工單)
			phprid.setPrid(now_order);
			prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A431_disassemble");
			// 檢查 u 製令單資料
			if (prArrayList.size() == 1) {
				ProductionHeader pro_h = prArrayList.get(0);
				Long phpbgid = prArrayList.get(0).getPhpbgid();
				List<ProductionBody> bodies = bodyDao.findAllByPbsnAndPbgid(m_old_sn, phpbgid);
				// 檢查 此工單+SN 是否重複
				if (bodies.size() > 0) {
					return false;
				}
				// 查詢 指定的SN
				bodies = new ArrayList<ProductionBody>();
				bodies = bodyDao.findAllByPbsn(m_old_sn);
				// 檢查 SN 是否u效 (有效不可覆蓋 -> 排除)
				if (bodies.size() >= 1) {
					return false;
				} else {
					// 工作站資訊
					JSONObject json_work = new JSONObject();
					ArrayList<WorkstationProgram> programs = programDao
							.findAllByWpgidAndSysheaderOrderBySyssortAsc(pro_h.getPhwpid(), false);
					for (WorkstationProgram p_one : programs) {
						ArrayList<Workstation> works = workDao
								.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
						JSONObject json_one = new JSONObject();
						json_one.put("name", works.get(0).getWpbname());
						json_one.put("type", works.get(0).getWcname() + "_N");
						json_one.put("id", works.get(0).getWid());
						json_one.put("w_pb_cell", works.get(0).getWpbcell());
						json_one.put("sort", p_one.getSyssort());
						json_work.put(works.get(0).getWcname(), json_one);
					}

					// 是否已經建過 群組
					Long id_b_g = pro_h.getPhpbgid();
					if (id_b_g == 1) {
						id_b_g = bodyDao.getProductionBodyGSeq();
						// 註冊到 重工 工單號
						pro_h.setPhpbgid(id_b_g);
						headerDao.save(pro_h);
					}

					// 登記 SN
					ProductionBody pro_b = new ProductionBody();
					pro_b.setSysver(0);
					pro_b.setPbgid(id_b_g);
					pro_b.setSysheader(false);
					pro_b.setPbsn(m_old_sn);
					pro_b.setPbbsn(m_old_sn);

					pro_b.setPbcheck(false);
					pro_b.setPbusefulsn(0);
					pro_b.setPbwyears(pro_h.getPhwyears());
					pro_b.setSysstatus(0);
					pro_b.setSyssort(0);
					pro_b.setPblpath("");
					pro_b.setPblsize("");
					pro_b.setPbltext("");
					pro_b.setPbschedule(json_work.toString());
					pro_b.setSysmuser(user.getSuaccount());
					pro_b.setSysmdate(new Date());

					bodyDao.save(pro_b);

				}
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單
	@SuppressWarnings("unchecked")
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		try {
			// 預備資料
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();
			String action = body.getJSONObject("modify").getString("action");

			// 取舊的-> 新建的資料
			if (action.equals("order_btn")) {
				String now_order = body.getJSONObject("modify").getString("m_now_order");
				String m_old_sn = body.getJSONObject("modify").getString("m_old_sn");
				String m_old_order = body.getJSONObject("modify").getString("m_old_order");
				ProductionRecords phprid = new ProductionRecords();

				// Step1. 進行-特定查詢(拆解工單)
				phprid.setPrid(now_order);
				prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A431_disassemble");
				// 檢查 u 新的 製令單資料
				if (prArrayList.size() != 1) {
					return false;
				}

				// Step2. 進行-特定查詢(曾經工單)
				phprid = new ProductionRecords();
				phprid.setPrid(m_old_order);
				prArrayList_old = headerDao.findAllByProductionRecords(phprid);
				// 檢查 u 舊有更換的 製令單資料
				if (prArrayList_old.size() != 1) {
					return false;
				}

				// Step3. 進行-特定查詢(重工工單)-> 指定的SN
				List<ProductionBody> bodies = bodyDao.findAllByPbbsnAndPbgid(m_old_sn, prArrayList.get(0).getPhpbgid());
				// 檢查 SN 是否u效 (有效不可覆蓋 -> 排除)
				if (bodies.size() >= 1) {
					return false;
				}

				// Step4. 進行-特定查詢(曾經工單)-> 指定的SN
				List<ProductionBody> bodies_old = bodyDao.findAllByPbbsnAndPbgid(m_old_sn,
						prArrayList_old.get(0).getPhpbgid());
				// 檢查 SN 是否u效 (有效資料)
				if (bodies_old.size() != 1) {
					return false;
				}
				// 取得工單與SN交換
				ProductionHeader pro_h = prArrayList.get(0);
				// ProductionHeader pro_h_old = prArrayList_old.get(0);
				ProductionBody pro_b_one = new ProductionBody();
				ProductionBody pro_b_one_old = bodies_old.get(0);

				// 是否已經建過 群組
				Long id_b_g = pro_h.getPhpbgid();
				if (id_b_g == 1) {
					id_b_g = bodyDao.getProductionBodyGSeq();
					// 註冊到 拆解 工單號
					pro_h.setPhpbgid(id_b_g);
					headerDao.save(pro_h);
				}

				// 修正舊SN ->已更換
				JSONArray pboldsns = new JSONArray();
				if (pro_b_one_old.getPboldsn() != null && !pro_b_one_old.getPboldsn().equals("")) {
					pboldsns = new JSONArray(pro_b_one_old.getPboldsn());
				}
				// 更換成舊的SN
				String sn_new = pro_b_one_old.getPbbsn() + "_old_Delete";
				String sn_old = pro_b_one_old.getPbbsn() + "_old_" + pboldsns.length();
				pboldsns.put(sn_old);

				// 舊
				if (pboldsns.length() == 1) {// 開頭資訊(剛開始繼承的話)
					pro_b_one_old.setPboldsn("" + new JSONArray().put(pro_b_one_old.getPbbsn() + "_old_beginning"));
					// sn_old = pro_b_one_old.getPbbsn()+ "_old_beginning";
				}
				pro_b_one_old.setPbbsn(sn_old);
				pro_b_one_old.setPbsn(sn_old);
				// pro_b_one_old.setSysmuser(user.getSuaccount());
				// pro_b_one_old.setSysmdate(new Date());
				bodyDao.save(pro_b_one_old);

				// 新
				JSONObject json_work = new JSONObject();
				ArrayList<WorkstationProgram> programs = programDao
						.findAllByWpgidAndSysheaderOrderBySyssortAsc(pro_h.getPhwpid(), false);
				for (WorkstationProgram p_one : programs) {
					ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(),
							true);
					JSONObject json_one = new JSONObject();
					json_one.put("name", works.get(0).getWpbname());
					json_one.put("type", works.get(0).getWcname() + "_N");
					json_one.put("id", works.get(0).getWid());
					json_one.put("w_pb_cell", works.get(0).getWpbcell());
					json_one.put("sort", p_one.getSyssort());
					json_work.put(works.get(0).getWcname(), json_one);
				}

				// pro_b_one.setPbid(bodyDao.getProductionBodySeqNext());
				pro_b_one.setPbgid(id_b_g);
				pro_b_one.setPbbsn(sn_new);
				pro_b_one.setPbsn(sn_new);
				pro_b_one.setPboldsn(pboldsns.toString());

				pro_b_one.setPbfnote(pro_b_one_old.getPbfnote());
				pro_b_one.setPbfvalue(pro_b_one_old.getPbfvalue());

				pro_b_one.setPbcheck(false);
				pro_b_one.setPbposition(pro_b_one_old.getPbposition());
				pro_b_one.setPbwyears(pro_h.getPhwyears());
				pro_b_one.setPbschedule(json_work.toString());

				pro_b_one.setPblpath(pro_b_one_old.getPblpath());
				pro_b_one.setPblpathoqc(pro_b_one_old.getPblpathoqc());
				pro_b_one.setPblsize(pro_b_one_old.getPblsize());
				pro_b_one.setPbltext(pro_b_one_old.getPbltext());
				pro_b_one.setPbusefulsn(pro_b_one_old.getPbusefulsn());

				pro_b_one.setSysstatus(0);
				pro_b_one.setSyssort(0);
				pro_b_one.setSyscuser(user.getSuaccount());
				pro_b_one.setSyscdate(new Date());
				pro_b_one.setSysmuser(user.getSuaccount());
				pro_b_one.setSysmdate(new Date());
				pro_b_one.setSysnote(pro_b_one_old.getSysnote());
				pro_b_one.setSysheader(false);

				// SN類別
				try {
					for (int k = 0; k < 50; k++) {
						// 有欄位?
						String get_name = "getPbvalue" + String.format("%02d", k + 1);
						Method get_method = pro_b_one_old.getClass().getMethod(get_name);
						String value = (String) get_method.invoke(pro_b_one_old);

						String set_name = "setPbvalue" + String.format("%02d", k + 1);
						Method set_method = pro_b_one.getClass().getMethod(set_name, String.class);
						set_method.invoke(pro_b_one, value);
						// 欄位有值
					}
					for (int k = 0; k < 20; k++) {
						// 有欄位?
						String get_name = "getPbwpdate" + String.format("%02d", k + 1);
						Method get_method = pro_b_one_old.getClass().getMethod(get_name);
						Date value = (Date) get_method.invoke(pro_b_one_old);

						String set_name = "setPbwpdate" + String.format("%02d", k + 1);
						Method set_method = pro_b_one.getClass().getMethod(set_name, Date.class);
						set_method.invoke(pro_b_one, value);
						// 欄位有值
					}
					for (int k = 0; k < 20; k++) {
						// 有欄位?
						String get_name = "getPbwname" + String.format("%02d", k + 1);
						Method get_method = pro_b_one_old.getClass().getMethod(get_name);
						String value = (String) get_method.invoke(pro_b_one_old);

						String set_name = "setPbwname" + String.format("%02d", k + 1);
						Method set_method = pro_b_one.getClass().getMethod(set_name, String.class);
						set_method.invoke(pro_b_one, value);
						// 欄位有值
					}

				} catch (Exception e) {
					e.printStackTrace();
					return check;
				}

				bodyDao.save(pro_b_one);

				check = true;
			} else if (action.equals("return_order_btn")) {
				// 歸還資料
				String return_sn = body.getJSONObject("modify").getString("m_return_sn");
				List<ProductionBody> now_sns = bodyDao.findAllByPbbsnAndPbbsnNotLike(return_sn, "%old%");

				// 確定有歸還 對象
				if (now_sns.size() == 1) {
					ProductionBody p_now = now_sns.get(0);
					// 檢查製令單
					prArrayList = headerDao.findAllByPhpbgid(p_now.getPbgid());
					if (prArrayList.size() == 1 //
							&& prArrayList.get(0).getPhtype().equals("A431_disassemble")) {
						// 是否有舊紀錄
						if (p_now.getPboldsn() != null && !p_now.getPboldsn().equals("")) {
							JSONArray re_old_sn = new JSONArray(p_now.getPboldsn());
							String old_sn = re_old_sn.getString(re_old_sn.length() - 1);
							// 核對
							List<ProductionBody> old_sns = bodyDao.findAllByPbbsn(old_sn);
							if (old_sns.size() == 1) {
								// 還原舊資料
								ProductionBody p_old = old_sns.get(0);
								p_old.setPbbsn(old_sn.split("_")[0]);
								p_old.setPbsn(old_sn.split("_")[0]);
								// 只有單一繼承,清除源頭紀錄
								if (re_old_sn.length() == 1) {
									p_old.setPboldsn("");
								}
								bodyDao.save(p_old);
								// 移除新資料
								bodyDao.delete(p_now);
								check = true;
							}
						} else {
							// 移除新資料
							bodyDao.delete(p_now);
							check = true;
						}
					}
				}
			} else if (action.equals("sn_part_unlock")) {
				// 產品SN解綁 [料件]
				String pbbsn = body.getJSONObject("modify").getString("m_sn_part_unlock");
				JSONArray part_vals = body.getJSONObject("modify").getJSONArray("part_val");
				Boolean all = false;

				for (Object x : part_vals) {
					if (x.toString().equals("part_all")) {
						all = true;
					}
				}

				// Step1. 避免->不存在
				List<ProductionBody> pbbs = bodyDao.findAllByPbbsn(pbbsn);
				if (pbbs.size() == 1) {
					ProductionBody pbb = pbbs.get(0);
					Method method_get;
					Method method_set;
					try {
						// Step2. 可能是(全部/指定)->
						if (all) {
							for (int j = 0; j < 50; j++) {
								String name_get = "getPbvalue" + String.format("%02d", j + 1);
								String name_set = "setPbvalue" + String.format("%02d", j + 1);
								method_get = pbb.getClass().getMethod(name_get);
								method_set = pbb.getClass().getMethod(name_set, String.class);
								// 取值
								String value = (String) method_get.invoke(pbb);
								// 填入(不能是空的+沒有_old)
								if (value != null && !value.equals("") && value.indexOf("_old") < 0) {
									method_set.invoke(pbb, value + "_old");
								}
							}
						} else {
							for (Object x : part_vals) {
								String name_get = x.toString().replace("set", "get");
								String name_set = x.toString();
								method_get = pbb.getClass().getMethod(name_get);
								method_set = pbb.getClass().getMethod(name_set, String.class);
								// 取值
								String value = (String) method_get.invoke(pbb);
								// 填入(不能是空的+沒有_old)
								if (value != null && !value.equals("") && value.indexOf("_old") < 0) {
									method_set.invoke(pbb, value + "_old");
								}
							}
						}
						check = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 存檔
					pbb.setSysmdate(new Date());
					pbb.setSysmuser(user.getSuaccount());
					bodyDao.save(pbb);
				}

			} else if (action.equals("order_part_unlock")) {
				// 製令單解綁 [料件]
				String prid = body.getJSONObject("modify").getString("m_order_part_unlock");
				JSONArray part_vals = body.getJSONObject("modify").getJSONArray("part_val");
				Boolean all = false;

				for (Object x : part_vals) {
					if (x.toString().equals("part_all")) {
						all = true;
					}
				}

				ProductionRecords pr = new ProductionRecords();
				pr.setPrid(prid);
				prArrayList = headerDao.findAllByProductionRecords(pr);
				// Step1. 避免->不存在
				if (prArrayList.size() == 1) {
					List<ProductionBody> pbbs = bodyDao.findAllByPbgidOrderByPbsnAsc(prArrayList.get(0).getPhpbgid());
					if (pbbs.size() >= 1) {
						for (ProductionBody pbb : pbbs) {
							Method method_get;
							Method method_set;

							// Step2. 可能是(全部/指定)->
							if (all) {
								for (int j = 0; j < 50; j++) {
									String name_get = "getPbvalue" + String.format("%02d", j + 1);
									String name_set = "setPbvalue" + String.format("%02d", j + 1);
									method_get = pbb.getClass().getMethod(name_get);
									method_set = pbb.getClass().getMethod(name_set, String.class);
									// 取值
									String value = (String) method_get.invoke(pbb);
									// 填入(不能是空的+沒有_old)
									if (value != null && !value.equals("") && value.indexOf("_old") < 0) {
										method_set.invoke(pbb, value + "_old");
									}
								}
							} else {
								for (Object x : part_vals) {
									String name_get = x.toString().replace("set", "get");
									String name_set = x.toString();
									method_get = pbb.getClass().getMethod(name_get);
									method_set = pbb.getClass().getMethod(name_set, String.class);
									// 取值
									String value = (String) method_get.invoke(pbb);
									// 填入(不能是空的+沒有_old)
									if (value != null && !value.equals("") && value.indexOf("_old") < 0) {
										method_set.invoke(pbb, value + "_old");
									}
								}
							}
							check = true;

							// 存檔
							pbb.setSysmdate(new Date());
							pbb.setSysmuser(user.getSuaccount());
							bodyDao.save(pbb);
						}
					}
				}
			} else if (action.equals("key_part_unlock")) {
				// key_part綁定 [料件]
				String pb_value = body.getJSONObject("modify").getString("m_key_part_unlock");
				JSONArray part_vals = body.getJSONObject("modify").getJSONArray("part_val");

				Boolean all = false;
				// 判斷 All
				for (Object x : part_vals) {
					if (x.toString().equals("part_all")) {
						all = true;
					}
				}
				// 必須 不是ALL+選擇一項料件項
				if (!all && part_vals.length() == 1) {
					String name_get = part_vals.get(0).toString().replace("set", "get");
					String name_set = part_vals.get(0).toString();
					String pb_value_name = part_vals.get(0).toString().replace("set", "").replace("Pbvalue",
							"pb_value");
					// 查詢SN欄位
					List<ProductionBody> productionBodies = new ArrayList<ProductionBody>();
					String nativeQuery = "SELECT b.* FROM production_body b WHERE"; //
					nativeQuery += " (b." + pb_value_name + " LIKE :pb_value)";
					nativeQuery += "and (b.pb_b_sn NOT LIKE '%old%')";
					Query query = em.createNativeQuery(nativeQuery, ProductionBody.class);
					query.setParameter("pb_value", "%" + pb_value + "%");
					productionBodies = query.getResultList();
					// 如果有取得產品
					if (productionBodies.size() == 1) {
						Method method_get;
						Method method_set;
						ProductionBody pbb = new ProductionBody();
						pbb = productionBodies.get(0);
						method_get = pbb.getClass().getMethod(name_get);
						method_set = pbb.getClass().getMethod(name_set, String.class);
						// 取值
						String value = (String) method_get.invoke(pbb);
						// 填入(不能是空的+沒有_old)
						if (value != null && !value.equals("") && value.indexOf("_old") < 0) {
							method_set.invoke(pbb, value + "_old");
							pbb.setSysmdate(new Date());
							pbb.setSysmuser(user.getSuaccount());
							bodyDao.save(pbb);
							check = true;
						} else {
							resp.autoMsssage("102");
						}
					}
				}
			} else if (action.equals("return_sn_part_lock")) {
				// 產品SN綁定 [料件]
				String pbbsn = body.getJSONObject("modify").getString("m_return_sn_part_lock");
				List<ProductionBody> pbbs = bodyDao.findAllByPbbsn(pbbsn);
				// Step1. 避免->不存在
				if (pbbs.size() == 1) {
					ProductionBody pbb = pbbs.get(0);
					Method method_get;
					Method method_set;

					for (int j = 0; j < 50; j++) {
						String name_get = "getPbvalue" + String.format("%02d", j + 1);
						String name_set = "setPbvalue" + String.format("%02d", j + 1);
						method_get = pbb.getClass().getMethod(name_get);
						method_set = pbb.getClass().getMethod(name_set, String.class);
						// 取值
						String value = (String) method_get.invoke(pbb);
						// 填入(不能是空的+沒有_old)
						if (value != null && !value.equals("") && value.indexOf("_old") > 0) {
							method_set.invoke(pbb, value.replace("_old", ""));
						}
					}
					pbb.setSysmdate(new Date());
					pbb.setSysmuser(user.getSuaccount());
					bodyDao.save(pbb);
					check = true;
				}
			} else if (action.equals("return_order_part_lock")) {
				// 製令單綁定 [料件]
				String prid = body.getJSONObject("modify").getString("m_return_order_part_lock");
				ProductionRecords pr = new ProductionRecords();
				pr.setPrid(prid);
				prArrayList = headerDao.findAllByProductionRecords(pr);
				// Step1. 避免->不存在
				if (prArrayList.size() == 1) {
					List<ProductionBody> pbbs = bodyDao.findAllByPbgidOrderByPbsnAsc(prArrayList.get(0).getPhpbgid());
					for (ProductionBody pbb : pbbs) {
						Method method_get;
						Method method_set;
						for (int j = 0; j < 50; j++) {
							String name_get = "getPbvalue" + String.format("%02d", j + 1);
							String name_set = "setPbvalue" + String.format("%02d", j + 1);
							method_get = pbb.getClass().getMethod(name_get);
							method_set = pbb.getClass().getMethod(name_set, String.class);
							// 取值
							String value = (String) method_get.invoke(pbb);
							// 填入(不能是空的+沒有_old)
							if (value != null && !value.equals("") && value.indexOf("_old") > 0) {
								method_set.invoke(pbb, value.replace("_old", ""));
							}
						}
						pbb.setSysmdate(new Date());
						pbb.setSysmuser(user.getSuaccount());
						bodyDao.save(pbb);
						check = true;
					}
				}
			} else if (action.equals("return_key_part_lock")) {
				// key_part綁定 [料件]
				String pb_value = body.getJSONObject("modify").getString("m_return_key_part_lock");
				JSONArray part_vals = body.getJSONObject("modify").getJSONArray("part_val");

				Boolean all = false;
				// 判斷 All
				for (Object x : part_vals) {
					if (x.toString().equals("part_all")) {
						all = true;
					}
				}
				// 必須 不是ALL+選擇一項料件項
				if (!all && part_vals.length() == 1) {
					String name_get = part_vals.get(0).toString().replace("set", "get");
					String name_set = part_vals.get(0).toString();
					String pb_value_name = part_vals.get(0).toString().replace("set", "").replace("Pbvalue",
							"pb_value");
					// 查詢SN欄位
					List<ProductionBody> productionBodies = new ArrayList<ProductionBody>();
					String nativeQuery = "SELECT b.* FROM production_body b WHERE"; //
					nativeQuery += " (b." + pb_value_name + " LIKE :pb_value)";
					nativeQuery += "and (b.pb_b_sn NOT LIKE '%old%')";
					Query query = em.createNativeQuery(nativeQuery, ProductionBody.class);
					query.setParameter("pb_value", "%" + pb_value + "%");
					productionBodies = query.getResultList();
					// 如果有取得產品
					if (productionBodies.size() == 1) {
						Method method_get;
						Method method_set;
						ProductionBody pbb = new ProductionBody();
						pbb = productionBodies.get(0);
						method_get = pbb.getClass().getMethod(name_get);
						method_set = pbb.getClass().getMethod(name_set, String.class);
						// 取值
						String value = (String) method_get.invoke(pbb);
						// 填入(不能是空的+沒有_old)
						if (value != null && !value.equals("")) {
							method_set.invoke(pbb, value.replace("_old", ""));
							pbb.setSysmdate(new Date());
							pbb.setSysmuser(user.getSuaccount());
							bodyDao.save(pbb);
							check = true;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			return false;
		}
		return check;
	}

	// 移除(還原) 資料清單
	@Transactional
	public boolean deleteData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;
		// 預備資料
		List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
		// List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();
		String action = body.getJSONObject("modify").getString("action");
		String return_sn = body.getJSONObject("modify").getString("m_return_sn");
		try {
			if (action.equals("return_order_btn")) {
				// 歸還資料
				List<ProductionBody> now_sns = bodyDao.findAllByPbbsnAndPbbsnNotLike(return_sn + "_old_Delete", "% %");

				// Step0. 確定有歸還 對象
				if (now_sns.size() == 1) {
					ProductionBody p_now = now_sns.get(0);
					// Step1. 檢查製令單
					prArrayList = headerDao.findAllByPhpbgid(p_now.getPbgid());
					if (prArrayList.size() == 1 //
							&& (prArrayList.get(0).getPhtype().equals("A431_disassemble"))) {
						// Step2. 是否有舊紀錄
						if (p_now.getPboldsn() != null && !p_now.getPboldsn().equals("")) {
							JSONArray re_old_sn = new JSONArray(p_now.getPboldsn());
							String old_sn = re_old_sn.getString(re_old_sn.length() - 1);
							// 核對
							List<ProductionBody> old_sns = bodyDao.findAllByPbbsn(old_sn);
							if (old_sns.size() == 1) {
								// Step3. 還原舊資料
								ProductionBody p_old = old_sns.get(0);
								p_old.setPbbsn(old_sn.split("_")[0]);
								p_old.setPbsn(old_sn.split("_")[0]);
								// 只有單一繼承,清除源頭紀錄
								if (re_old_sn.length() == 1) {
									p_old.setPboldsn("");
								}
								bodyDao.save(p_old);
								bodyDao.delete(p_now);
								check = true;
							}
						} else {
							// 移除新資料
							bodyDao.delete(p_now);
							check = true;
						}
					}
				} else {
					// 移除舊資料(不是 old )
					List<ProductionBody> p_nows = bodyDao.findAllByPbbsnAndPbbsnNotLike(return_sn, "%old%");
					if (p_nows != null && p_nows.size() == 1) {
						bodyDao.delete(p_nows.get(0));
						check = true;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
}