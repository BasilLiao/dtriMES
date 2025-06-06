package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemMail;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.SystemGroupDao;
import dtri.com.tw.db.pgsql.dao.SystemMailDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;
import dtri.com.tw.tools.Fm_Time;

@Service
public class SystemMailService {
	@Autowired
	private SystemUserDao userDao;
	@Autowired
	private SystemGroupDao groupDao;
	
	@Autowired
	private SystemMailDao rmaMailListDao;

	// 取得當前 資料清單
	public boolean getData(PackageBean bean, PackageBean req, SystemUser user) {
		// 傳入參數
		JSONObject body = req.getBody();
		int page = req.getPage_batch();
		int p_size = req.getPage_total();
		ArrayList<SystemMail> rmaMail = new ArrayList<SystemMail>();
		
		// 查詢的頁數，page=從0起算/size=查詢的每頁筆數
		if (p_size < 1) {
			page = 0;
			p_size = 100;
		}
//		String su_account = null;
		String su_e_name =null;
		String su_name = null;
		String su_position = null;
		String sys_note = null;
//		String status = "0";
//		Long susggid = 0L;
		PageRequest page_r = PageRequest.of(page, p_size, Sort.by("suid").descending());
		// 初次載入需要標頭 / 之後就不用
		if (body == null || body.isNull("search")) {

			// 放入包裝(header) [01 是排序][_h__ 是分割直][資料庫欄位名稱]
			JSONObject object_header = new JSONObject();
			int ord = 0;
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_id", FFS.h_t("ID", "100px", FFM.Wri.W_N));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_sg_g_id", FFS.h_t("群組ID", "100px", FFM.Wri.W_N)); //群組名稱 su_sg_g_id			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_name", FFS.h_t("姓名", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_e_name", FFS.h_t("英文姓名", "180px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_position", FFS.h_t("單位(部門)", "180px", FFM.Wri.W_Y));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_template", FFS.h_t("階級", "180px", FFM.Wri.W_N)); //"階級
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_email", FFS.h_t("Email", "250px", FFM.Wri.W_Y));
		
			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_received", FFS.h_t("收發貨", "100px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_repairdone", FFS.h_t("維修完成", "100px", FFM.Wri.W_Y));			
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "su_dailyreport", FFS.h_t("測試日報", "100px", FFM.Wri.W_Y));
	
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_date", FFS.h_t("建立時間", "190px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_c_user", FFS.h_t("建立人", "150px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_date", FFS.h_t("修改時間", "190px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_m_user", FFS.h_t("修改人", "150px", FFM.Wri.W_Y));

			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_note", FFS.h_t("備註", "200px", FFM.Wri.W_Y));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_sort", FFS.h_t("排序", "100px", FFM.Wri.W_N));
			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_ver", FFS.h_t("版本", "100px", FFM.Wri.W_N));
//			object_header.put(FFS.ord((ord += 1), FFM.Hmb.H) + "sys_status", FFS.h_t("狀態", "100px", FFM.Wri.W_Y));
			bean.setHeader(object_header);

			// 放入修改 [(key)](modify/Create/Delete) 格式
			JSONArray obj_m = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", false, new JSONArray(), "su_id", "ID"));

			JSONArray groups = new JSONArray();
			// (u些事admin專用)
//			if (user.getSusggid() == 1) {
				userDao.findAll( ).forEach(s -> {
					groups.put((new JSONObject()).put("value", s.getSuname()+" | "+s.getSuename()+" | "+s.getSuposition()+" | "+s.getSuemail())
							.put("key",  s.getSuname()+" | "+s.getSuename()+" | "+s.getSuposition()+" | "+s.getSuemail()));	
				});
//			} else {
//				groupDao.findAllBySysheaderAndSgidNot(true, 1l, PageRequest.of(0, 999)).forEach(s -> {
//					groups.put((new JSONObject()).put("value", s.getSgname()).put("key", s.getSggid()));
//				});
//			}
			JSONArray value = new JSONArray();
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, value, "su_name", "姓名"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, value, "su_e_name", "英文姓名"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, value, "su_account", "帳號"));
			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.INP, FFM.Type.PASS, "", "", FFM.Wri.W_N, "col-md-1", false, value, "su_password", "密碼"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-1", true, value, "su_position", "單位(部門)"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", true, value, "su_email", "Email"));			
			
//			obj_m.put(FFS.h_m(FFM.Dno.D_N, FFM.Tag.SEL, FFM.Type.TEXT, "", "", FFM.Wri.W_N, "col-md-2", true, groups, "su_sg_g_id", "群組名稱")); //群組名稱 su_sg_g_id
		
			JSONArray values = new JSONArray();
			values.put((new JSONObject()).put("value", "否").put("key", "N"));
			values.put((new JSONObject()).put("value", "主").put("key", "Y"));
			values.put((new JSONObject()).put("value", "副").put("key", "C"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "N", "N", FFM.Wri.W_Y, "col-md-1", true, values, "su_received", "收發貨"));
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "N", "N", FFM.Wri.W_Y, "col-md-1", true, values, "su_repairdone", "維修完成"));			
			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "N", "N", FFM.Wri.W_Y, "col-md-1", true, values, "su_dailyreport", "測試日報"));

//			JSONArray values = new JSONArray();
//			values.put((new JSONObject()).put("value", "一般職員").put("key", "一般職員"));;
//			values.put((new JSONObject()).put("value", "組長").put("key", "組長"));
//			values.put((new JSONObject()).put("value", "課長").put("key", "課長"));
//			values.put((new JSONObject()).put("value", "經理").put("key", "經理"));
//			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "一般職員", "一般職員", FFM.Wri.W_N, "col-md-1", true, values, "su_template", "階級"));

			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.INP, FFM.Type.TEXT, "", "", FFM.Wri.W_Y, "col-md-2", false, value, "sys_note", "備註"));

//			values = new JSONArray();
//			values.put((new JSONObject()).put("value", "正常").put("key", "0"));
//			values.put((new JSONObject()).put("value", "異常").put("key", "1"));
//			obj_m.put(FFS.h_m(FFM.Dno.D_S, FFM.Tag.SEL, FFM.Type.TEXT, "0", "0", FFM.Wri.W_N, "col-md-1", true, values, "sys_status", "狀態"));

			bean.setCell_modify(obj_m);

			// 放入包裝(search)   //顯示搜巡 輸入的頁面
			JSONArray object_searchs = new JSONArray();
//			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "su_account", "帳號", value));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "su_e_name", "英文名字", value));			
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "su_name", "姓名", value));
//			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TIME, "", "col-md-2", "su_sg_g_id", "群組名稱", groups)); //群組名稱 su_sg_g_id
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "su_position", "單位(部門)", value));
			object_searchs.put(FFS.h_s(FFM.Tag.INP, FFM.Type.TEXT, "", "col-md-2", "sys_note", "備註", value));
			
//			values = new JSONArray();
//			values.put((new JSONObject()).put("value", "否").put("key", "N"));
//			values.put((new JSONObject()).put("value", "主").put("key", "Y"));
//			values.put((new JSONObject()).put("value", "副").put("key", "C"));
//			
//			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "su_received", "收發貨", values));
//			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1", "su_repairdone", "維修完成", values));
//			object_searchs.put(FFS.h_s(FFM.Tag.SEL, FFM.Type.TEXT, "0", "col-md-1","su_dailyreport", "測試日報", values));
			bean.setCell_searchs(object_searchs);
		} else {
			// 進行-特定查詢
			su_e_name = body.getJSONObject("search").getString("su_e_name");
			su_e_name = su_e_name.equals("") ? null : su_e_name;
			
			su_name = body.getJSONObject("search").getString("su_name");
			su_name = su_name.equals("") ? null : su_name;
			su_position = body.getJSONObject("search").getString("su_position");
			su_position = su_position.equals("") ? null : su_position;
			sys_note = body.getJSONObject("search").getString("sys_note");
			sys_note = sys_note.equals("") ? "" : sys_note;
			
//			susggid = body.getJSONObject("search").getString("su_sg_g_id").equals("") ? //群組名稱 su_sg_g_id
//					0L : body.getJSONObject("search").getLong("su_sg_g_id");
		}
		// 全查
//		if (user.getSusggid() == 1) {
			rmaMail = rmaMailListDao.findAllBySystemMail(su_name, su_e_name,su_position, sys_note, page_r);
//		} else {
//			rmaMail = rmaMailListDao.findAllByRmaMailNotAdmin(susggid, su_name, su_e_name, su_position, Integer.parseInt(status), page_r);
//		}
			
		//顯示在TABLE列表上		
		// 放入包裝(body) [01 是排序][_b__ 是分割直][資料庫欄位名稱]
		JSONArray object_bodys = new JSONArray();
		rmaMail.forEach(one -> {
			JSONObject object_body = new JSONObject();
			int ord = 0;
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_id", one.getSuid());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_sg_g_id", one.getSusggid()); //群組名稱 su_sg_g_id			
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_name", one.getSuname());   //姓名 su_name
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_e_name", one.getSuename()); //英文姓名 su_e_name
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_position", one.getSuposition());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_template", one.getSutemplate());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_account", one.getSuaccount());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_email", one.getSuemail());
		
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_received", one.getSureceived());   // 收發貨通知
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_repairdone", one.getSurepairdone()); //維修完成通知
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "su_dailyreport", one.getSudailyreport()==null ? "" : one.getSudailyreport()); //測試日報

			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_date", Fm_Time.to_yMd_Hms(one.getSyscdate()));
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_c_user", one.getSyscuser());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_date", Fm_Time.to_yMd_Hms(one.getSysmdate())); //修改時間
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_m_user", one.getSysmuser()); //修改人
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_note", one.getSysnote());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_sort", one.getSyssort());
			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_ver", one.getSysver());
//			object_body.put(FFS.ord((ord += 1), FFM.Hmb.B) + "sys_status", one.getSysstatus());
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
//		PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		try {
			JSONArray list = body.getJSONArray("create");
			for (Object one : list) {
				// 物件轉換
//				SystemUser sys_c = new SystemUser();
//				ArrayList<RmaMail> rmaMail = new ArrayList<RmaMail>();
				SystemMail rmaMail=new SystemMail();
				JSONObject data = (JSONObject) one;
//				rmaMail.setSusggid(data.getLong("su_sg_g_id"));  //群組名稱 su_sg_g_id
				
				rmaMail.setSureceived(data.getString("su_received"));
				rmaMail.setSurepairdone(data.getString("su_repairdone"));
				rmaMail.setSudailyreport(data.getString("su_dailyreport"));
				
				rmaMail.setSuname(data.getString("su_name"));  //姓名 su_name
				rmaMail.setSuename(data.getString("su_e_name"));//英文姓名 su_e_name
				rmaMail.setSuposition(data.getString("su_position")); //單位(部門) su_position
//				rmaMail.setSutemplate(data.getString("su_template")); //階級 su_template
//				rmaMail.setSuaccount(data.getString("su_account"));  //帳號
//				// 密碼空不存
//				if (data.getString("su_password").equals("")) {
//					return false;
//				}
//				rmaMail.setSupassword(pwdEncoder.encode("123"));
				rmaMail.setSuemail(data.getString("su_email").trim()); //	Email su_email
				rmaMail.setSysnote(data.getString("sys_note"));
				rmaMail.setSyssort(0);
//				rmaMail.setSysstatus(data.getInt("sys_status"));
				rmaMail.setSysmuser(user.getSuaccount()); //修改人
				rmaMail.setSyscuser(user.getSuaccount()); //建立人
		
				// email帳號重複
				Set<SystemMail> suemail = rmaMailListDao.findBysuemailContaining(data.getString("su_email"));
				if (suemail.size()!=0) {  // 程式中可以用 isEmpty() 或 size() 來檢查是否為空。
					resp.setError_ms(" 之此帳號 : " + data.getString("su_email") + " ");
					resp.autoMsssage("107");
					check = false;
					return check;
				} 
				rmaMailListDao.save(rmaMail);						
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
//		PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		try {
			JSONArray list = body.getJSONArray("save_as");
			for (Object one : list) {
				// 物件轉換
			//	SystemUser sys_c = new SystemUser();
				SystemMail rmaMail=new SystemMail();
				JSONObject data = (JSONObject) one;
//				rmaMail.setSusggid(data.getLong("su_sg_g_id"));  //群組名稱 su_sg_g_id
				
				rmaMail.setSureceived(data.getString("su_received"));
				rmaMail.setSurepairdone(data.getString("su_repairdone"));
				rmaMail.setSudailyreport(data.getString("su_dailyreport"));
				
				rmaMail.setSuname(data.getString("su_name"));    //姓名 su_name
				rmaMail.setSuename(data.getString("su_e_name"));  //英文姓名 su_e_name
				rmaMail.setSuposition(data.getString("su_position")); //單位(部門) su_position

				rmaMail.setSuemail(data.getString("su_email").trim());  //	Email su_email
				rmaMail.setSysnote(data.getString("sys_note"));
				rmaMail.setSyssort(0);
//				rmaMail.setSysstatus(data.getInt("sys_status"));
				rmaMail.setSysmuser(user.getSuaccount());
				rmaMail.setSyscuser(user.getSuaccount());
				//e-mail 帳號重複
				if (rmaMailListDao.findBysuemailContaining(data.getString("su_email")).size()>0) {
					resp.setError_ms(" 之此E-mail帳號 : " + rmaMail.getSuemail() + " ");
					resp.autoMsssage("107");
					check = false;
					return check;
				} else {
					rmaMailListDao.save(rmaMail);
				}
				
//				rmaMailListDao.save(rmaMail);
			}
			check = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return check;
	}

	// 更新 資料清單  //更改成功
	@Transactional
	public boolean updateData(PackageBean resp, PackageBean req, SystemUser user) {
		JSONObject body = req.getBody();
		boolean check = false;

		try {
//			PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
			JSONArray list = body.getJSONArray("modify");
			for (Object one : list) {
				// 物件轉換
				JSONObject data = (JSONObject) one;
				SystemMail sys_c = rmaMailListDao.findAllBySuid(data.getLong("su_id")).get(0); 
				String sys_mail=sys_c.getSuemail();
				String data_mail=data.getString("su_email").trim();
				if(data_mail.length()==0) {
					resp.setError_ms(" 之此Email : " + "不得為空" + " ");
					resp.autoMsssage("108");
					check = false;
					return check;
				}
				
				if( !sys_mail.equals(data_mail) && rmaMailListDao.findBysuemailContaining(data_mail).size()!=0) {
					// email帳號重複
						resp.setError_ms(" 之此帳號 : " + data.getString("su_email") + " ");
						resp.autoMsssage("107");	
						check = false;
						return check; 					
				}
				// 如果帳號重覆
//				RmaMail oneUser = rmaMailListDao.findBySuaccount(data.getString("su_account")); //帳號
//				if (oneUser != null && data.getLong("su_id") != oneUser.getSuid()) {
//					resp.setError_ms(" 之此帳號 : " + oneUser.getSuaccount() + " ");
//					resp.autoMsssage("107");
//					check = false;
//					return check;
//				}
//				sys_c.setSuaccount(data.getString("su_account")); //帳號 su_account
				// sys_c.setSuid(data.getInt("su_id"));
				//sys_c.setSusggid(data.getLong("su_sg_g_id"));  //群組名稱 su_sg_g_id
				
				sys_c.setSureceived(data.getString("su_received"));
				sys_c.setSurepairdone(data.getString("su_repairdone"));
				sys_c.setSudailyreport(data.getString("su_dailyreport"));
				
				sys_c.setSuname(data.getString("su_name"));    //姓名 su_name
				sys_c.setSuename(data.getString("su_e_name"));  //英文姓名 su_e_name
				sys_c.setSuposition(data.getString("su_position")); //單位(部門) su_position
//				sys_c.setSutemplate(data.getString("su_template"));  //階級 su_template
//				// 密碼空不存
//				if (!data.getString("su_password").equals("")) {   //密碼 su_password
//					sys_c.setSupassword(pwdEncoder.encode(data.getString("su_password")));
//				}
				sys_c.setSuemail(data.getString("su_email"));   //	Email su_email

				sys_c.setSysnote(data.getString("sys_note"));
				sys_c.setSyssort(0);
//				sys_c.setSysstatus(data.getInt("sys_status"));
				sys_c.setSysmuser(user.getSuaccount());  //修改人
				sys_c.setSysmdate(new Date());  //修改時間

				rmaMailListDao.save(sys_c);
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
		//	RmaMail rmaMail=new RmaMail();
		try {
			JSONArray list = body.getJSONArray("delete");
			for (Object one : list) {
				// 物件轉換
				SystemMail sys_p = new SystemMail();
				JSONObject data = (JSONObject) one;
				sys_p.setSuid(data.getLong("su_id"));
				// 不得刪除自己
//				if (!data.getString("su_account").equals(user.getSuaccount())) {
					if (rmaMailListDao.deleteBySuid(sys_p.getSuid()) > 0) {
						check = true;
					}
//				} else {
//					return false;
//				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return check;
	}
	
	// 取得當前 資料清單
	public boolean getDataMail (PackageBean bean, PackageBean req, SystemUser user) {
			
		rmaMailListDao.findAllBySystemMail(null, null, null, null, null);
		
		return true;		
	}
	
}
