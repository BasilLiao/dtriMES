package dtri.com.tw.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.service.ProductionHeaderService;
import dtri.com.tw.service.SystemApiService;

@Controller
public class SystemApiController extends AbstractController {
	public SystemApiController() {
		super("api.basil");
	}

	@Autowired
	ProductionHeaderService headerService;
	@Autowired
	SystemApiService apiService;

	/**
	 * API-與延展系統串接
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/api.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String access(@RequestBody String json_object) {
		showSYS_CM("modify");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;
		JSONObject obj = new JSONObject(json_object);
		JSONObject obj_return = new JSONObject();

		// Step0.當前用戶資料-UI權限
		//SystemUser user = loginUser().getSystemUser();
		SystemUser user = new SystemUser();
		user.setSuaccount("system");
		// Step1.解析行為
		switch (obj.getString("action")) {
		case "production_create":
			// 創建-製令單
			req.setBody(obj);
			check = headerService.createData(resp, req, user);
			if (check) {
				obj_return.put("status", "ok");
			} else {
				obj_return.put("status", "fail");
			}
			break;
		case "get_work_program":
			// 取得-工作站程序
			obj_return.put("wProgram", apiService.getWorkstationProgramList().toString());
			obj_return.put("wLine", apiService.getWorkstationLineList().toString());
			obj_return.put("status", "ok");
			break;
		default:
			obj_return.put("status", "fail");
			break;
		}
		// 回傳-資料
		return obj_return.toString();
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
