package dtri.com.tw.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	// @PersistenceUnit(unitName = "default")
	// private EntityManagerFactory emf;

	// 取得當前 資料清單
	public PackageBean getData(JSONObject body, int page, int p_size) {
		PackageBean bean = new PackageBean();
		List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();

		// 進行-特定查詢(拆解工單)
		String now_order = body.getJSONObject("search").getString("m_now_order");
		ProductionRecords phprid = new ProductionRecords();
		phprid.setPrid(now_order);
		prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A431_disassemble");

		// u資料
		if (prArrayList.size() == 1) {
			// 查詢多少台
			Long phpbgid = prArrayList.get(0).getPhpbgid();
			List<ProductionBody> bodies = bodyDao.findAllByPbgidAndPbbsnNotOrderByPbsnAsc(phpbgid, "no_sn");
			bean.setBody(new JSONObject().put("search", new JSONObject().//
					put("phpnumber_total", prArrayList.get(0).getProductionRecords().getPrpquantity()).//
					put("phpnumber_register", bodies.size()).//
					put("check", true)));//
		} else {
			bean.autoMsssage("102");
		}
		return bean;
	}

	// 存檔 資料清單
	@Transactional
	public boolean createData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			// 新建的資料
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			String now_order = body.getJSONObject("create").getString("m_now_order");
			String m_now_sn = body.getJSONObject("create").getString("m_now_sn");
			ProductionRecords phprid = new ProductionRecords();

			// 進行-特定查詢(重工工單)
			phprid.setPrid(now_order);
			prArrayList = headerDao.findAllByProductionRecordsAndPhtype(phprid, "A431_disassemble");
			// 檢查 u 製令單資料
			if (prArrayList.size() == 1) {
				ProductionHeader pro_h = prArrayList.get(0);
				Long phpbgid = prArrayList.get(0).getPhpbgid();
				List<ProductionBody> bodies = bodyDao.findAllByPbsnAndPbgid(m_now_sn, phpbgid);
				// 檢查 此工單+SN 是否重複
				if (bodies.size() > 0) {
					return false;
				}
				// 查詢 指定的SN
				bodies = new ArrayList<ProductionBody>();
				bodies = bodyDao.findAllByPbsn(m_now_sn);
				// 檢查 SN 是否u效 (有效不可覆蓋 -> 排除)
				if (bodies.size() >= 1) {
					return false;
				} else {
					// 工作站資訊
					JSONObject json_work = new JSONObject();
					ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(pro_h.getPhwpid(), false);
					for (WorkstationProgram p_one : programs) {
						ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
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
					pro_b.setPbsn(m_now_sn);
					pro_b.setPbbsn(m_now_sn);

					pro_b.setPbcheck(false);
					pro_b.setPbusefulsn(0);
					pro_b.setPbwyears(pro_h.getProductionRecords().getPrwyears());
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
	@Transactional
	public boolean updateData(JSONObject body, SystemUser user) {
		boolean check = false;
		try {
			// 預備資料
			List<ProductionHeader> prArrayList = new ArrayList<ProductionHeader>();
			List<ProductionHeader> prArrayList_old = new ArrayList<ProductionHeader>();
			String action = body.getJSONObject("modify").getString("action");

			// 取舊的-> 新建的資料
			if (action.equals("order_btn")) {
				String now_order = body.getJSONObject("modify").getString("m_now_order");
				String m_now_sn = body.getJSONObject("modify").getString("m_now_sn");
				String m_old_order = body.getJSONObject("modify").getString("m_old_order");
				ProductionRecords phprid = new ProductionRecords();

				// Step1. 進行-特定查詢(重工工單)
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
				List<ProductionBody> bodies = bodyDao.findAllByPbsnAndPbgid(m_now_sn, prArrayList.get(0).getPhpbgid());
				// 檢查 SN 是否u效 (有效不可覆蓋 -> 排除)
				if (bodies.size() >= 1) {
					return false;
				}

				// Step4. 進行-特定查詢(曾經工單)-> 指定的SN
				List<ProductionBody> bodies_old = bodyDao.findAllByPbsnAndPbgid(m_now_sn, prArrayList_old.get(0).getPhpbgid());
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
					// 註冊到 重工 工單號
					pro_h.setPhpbgid(id_b_g);
					headerDao.save(pro_h);
				}

				// 修正舊SN ->已更換
				JSONArray pboldsns = new JSONArray();
				if (pro_b_one_old.getPboldsn() != null && !pro_b_one_old.getPboldsn().equals("")) {
					pboldsns = new JSONArray(pro_b_one_old.getPboldsn());
				}
				// 更換成舊的SN
				String sn_new = pro_b_one_old.getPbbsn();
				String sn_old = pro_b_one_old.getPbbsn() + "_old_" + pboldsns.length();
				pboldsns.put(sn_old);

				// 舊
				pro_b_one_old.setPbbsn(sn_old);
				pro_b_one_old.setPbsn(sn_old);
				pro_b_one_old.setSysmuser(user.getSuaccount());
				pro_b_one_old.setSysmdate(new Date());
				bodyDao.save(pro_b_one_old);

				// 新
				JSONObject json_work = new JSONObject();
				ArrayList<WorkstationProgram> programs = programDao.findAllByWpgidAndSysheaderOrderBySyssortAsc(pro_h.getPhwpid(), false);
				for (WorkstationProgram p_one : programs) {
					ArrayList<Workstation> works = workDao.findAllByWgidAndSysheaderOrderBySyssortAsc(p_one.getWpwgid(), true);
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
				pro_b_one.setPbwyears(pro_h.getProductionRecords().getPrwyears());
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

				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					return check;
				} catch (SecurityException e) {
					e.printStackTrace();
					return check;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return check;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return check;
				} catch (InvocationTargetException e) {
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
								bodyDao.save(p_old);
								// 移除新資料
								bodyDao.delete(p_now);
								check = true;
							}
						}
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
