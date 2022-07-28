package dtri.com.tw.controller;

import java.awt.print.PrinterJob;
import java.util.List;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import dtri.com.tw.bean.ConfigBean;
import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.login.LoginUserDetails;
import dtri.com.tw.service.IndexService;
import dtri.com.tw.service.PackageService;

@Controller
public class IndexController extends AbstractController {
	public IndexController() {
		super("index.basil");
	}

	// 功能
	final static String SYS_F = "index.basil";

	@Autowired
	PackageService packageService;
	@Autowired
	IndexService indexService;

	@Autowired
	ConfigBean configBean;
	private static final Logger log = LoggerFactory.getLogger(IndexController.class);

	/**
	 * 登入 and 登出-畫面
	 */
	@RequestMapping(value = { "/", "/login.basil", "/index.basil", "/logout.basil" }, method = { RequestMethod.GET })
	public ModelAndView loginCheck(HttpServletRequest request) {
		showSYS_CM("loginCheck");

		// 可能有錯誤碼
		String error = request.getParameter("status");
		log.warn(error);
		// 測試網路列印
		String printerName = "";
		PrintService service = null;
		// Get array of all print services - sort order NOT GUARANTEED!
		PrintService[] services = PrinterJob.lookupPrintServices();
		// Retrieve specified print service from the array
		for (int index = 0; service == null && index < services.length; index++) {
			System.out.println(services[index].getName());
			if (services[index].getName().equalsIgnoreCase(printerName)) {
				service = services[index];
			}
		}
		configBean.init();
		// 回傳-模板
		return new ModelAndView("./html/login.html", "status", error);
	}

	/**
	 * (初始化)主頁
	 * 
	 */
	@RequestMapping(value = { "/index.basil" }, method = { RequestMethod.POST })
	public ModelAndView indexCheck() {
		showSYS_CM("index(init)");
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step1.進行查詢-取得-當前用戶資料
		LoginUserDetails loginUser = loginUser();
		SystemUser user = loginUser.getSystemUser();
		List<SystemGroup> nav = loginUser.getSystemGroup();
		// Step3.進行判定
		check = (loginUser.getSystemUser().getSuaccount() != null) ? true : false;
		if (check) {
			// Step4.包裝回傳
			resp = indexService.getNav(nav);
			resp.setInfo_user(indexService.getUserInfo(user));
		} else {
			// Step4.包裝Err回傳-取得用戶失敗
			resp.setInfo(PackageBean.info_warning1_NotFindUser);
			resp.setInfo_color(PackageBean.info_color_warning);
		}

		// Step2.包裝回傳
		resp = packageService.setObjResp(resp, req, null);

		// 回傳-模板
		return new ModelAndView("./html/main.html", "initMain", packageService.objToJson(resp));
	}

	/**
	 * (再次讀取)主頁
	 */
	@ResponseBody
	@RequestMapping(value = { "ajax/index.basil" }, method = { RequestMethod.POST })
	public String index(@RequestBody String json_object) {
		showSYS_CM("index(again)");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行查詢-取得-當前用戶資料
		LoginUserDetails loginUser = loginUser();
		SystemUser user = loginUser.getSystemUser();
		List<SystemGroup> nav = loginUser.getSystemGroup();

		// Step3.進行判定
		check = (loginUser.getSystemUser().getSuaccount() != null) ? true : false;
		if (check) {
			// Step4.包裝回傳
			resp = indexService.getNav(nav);
			resp.setInfo_user(indexService.getUserInfo(user));
		} else {
			// Step4.包裝Err回傳-取得用戶失敗
			resp.setInfo(PackageBean.info_warning1_NotFindUser);
			resp.setInfo_color(PackageBean.info_color_warning);
		}
		resp = packageService.setObjResp(resp, req, null);

		// 回傳-模板
		return packageService.objToJson(resp);
	}

	// =========無用到=========
	@Override
	String access(String json_object) {
		return null;
	}

	@Override
	String search(String json_object) {
		return null;
	}

	@Override
	String modify(String json_object) {
		return null;
	}

	@Override
	String delete(String json_object) {
		return null;
	}
}
