package dtri.com.tw.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WorkstationItemController{
//	public WorkstationItemController() {
//		super("workstation_item.basil");
//	}
//
//	// 功能
//	final static String SYS_F = "workstation_item.basil";
//	// 遺棄功能獏組
//	@Autowired
//	PackageService packageService;
//	@Autowired
//	WorkstationItemService itemService;
//
//	/**
//	 * 訪問
//	 */
//	@ResponseBody
//	@RequestMapping(value = { "/ajax/workstation_item.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
//	public String access(@RequestBody String json_object) {
//		showSYS_CM("access");
//		show(json_object);
//		PackageBean req = new PackageBean();
//		PackageBean resp = new PackageBean();
//		boolean check = false;
//
//		// Step0.當前用戶資料-UI權限
//		SystemUser user = loginUser().getSystemUser();
//		SystemPermission pern = permissionUI();
//		// Step1.包裝解析
//		req = packageService.jsonToObj(new JSONObject(json_object));
//		// Step2.進行查詢
//		check = itemService.getData(resp, req, user);
//		// Step3.進行判定
//		if (check) {
//			// Step4.包裝回傳
//			resp = packageService.setObjResp(resp, req, resp.permissionToJson(pern.getSppermission().split("")));
//		} else {
//			// Step4.包裝Err回傳
//			packageService.setObjErrResp(resp, req);
//			resp = packageService.setObjResp(resp, req, null);
//		}
//		// 回傳-資料
//		return packageService.objToJson(resp);
//	}
//
//	/**
//	 * 查詢
//	 */
//	@ResponseBody
//	@RequestMapping(value = { "/ajax/workstation_item.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
//	public String search(@RequestBody String json_object) {
//		showSYS_CM("search");
//		show(json_object);
//		PackageBean req = new PackageBean();
//		PackageBean resp = new PackageBean();
//		boolean check = false;
//
//		// Step0.當前用戶資料-UI權限
//		SystemUser user = loginUser().getSystemUser();
//		// Step1.包裝解析
//		req = packageService.jsonToObj(new JSONObject(json_object));
//		// Step2.進行查詢
//		check = itemService.getData(resp, req, user);
//		// Step3.進行判定
//		if (check) {
//			// Step4.包裝回傳
//			resp = packageService.setObjResp(resp, req, null);
//		} else {
//			// Step4.包裝Err回傳
//			packageService.setObjErrResp(resp, req);
//			resp = packageService.setObjResp(resp, req, null);
//		}
//		// 回傳-資料
//		return packageService.objToJson(resp);
//	}
//
//	/**
//	 * 新增
//	 */
//	@ResponseBody
//	@RequestMapping(value = { "/ajax/workstation_item.basil.AC" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
//	public String create(@RequestBody String json_object) {
//		showSYS_CM("create");
//		show(json_object);
//		PackageBean req = new PackageBean();
//		PackageBean resp = new PackageBean();
//		boolean check = false;
//
//		// Step0.當前用戶資料-UI權限
//		SystemUser user = loginUser().getSystemUser();
//		// Step1.包裝解析
//		req = packageService.jsonToObj(new JSONObject(json_object));
//		// Step2.進行新增
//		check = itemService.createData(resp, req, user);
//		if (check) {
//			check = itemService.save_asData(resp, req, user);
//		}
//		// Step3.進行判定
//		if (check) {
//			// Step4.包裝回傳
//			resp = packageService.setObjResp(resp, req, null);
//		} else {
//			// Step4.包裝Err回傳
//			packageService.setObjErrResp(resp, req);
//			resp = packageService.setObjResp(resp, req, null);
//		}
//		// 回傳-資料
//		return packageService.objToJson(resp);
//	}
//
//	/**
//	 * 修改
//	 */
//	@ResponseBody
//	@RequestMapping(value = { "/ajax/workstation_item.basil.AU" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
//	public String modify(@RequestBody String json_object) {
//		showSYS_CM("modify");
//		show(json_object);
//		PackageBean req = new PackageBean();
//		PackageBean resp = new PackageBean();
//		boolean check = false;
//
//		// Step0.當前用戶資料-UI權限
//		SystemUser user = loginUser().getSystemUser();
//		// Step1.包裝解析
//		req = packageService.jsonToObj(new JSONObject(json_object));
//		// Step2.進行修改
//		check = itemService.updateData(resp, req, user);
//		// Step3.進行判定
//		if (check) {
//			// Step4.包裝回傳
//			resp = packageService.setObjResp(resp, req, null);
//		} else {
//			// Step4.包裝Err回傳
//			packageService.setObjErrResp(resp, req);
//			resp = packageService.setObjResp(resp, req, null);
//		}
//		// 回傳-資料
//		return packageService.objToJson(resp);
//	}
//
//	/**
//	 * 移除
//	 */
//	@ResponseBody
//	@RequestMapping(value = { "/ajax/workstation_item.basil.AD" }, method = { RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
//	public String delete(@RequestBody String json_object) {
//		showSYS_CM("delete");
//		show(json_object);
//		PackageBean req = new PackageBean();
//		PackageBean resp = new PackageBean();
//		boolean check = false;
//
//		// Step0.當前用戶資料-UI權限
//		SystemUser user = loginUser().getSystemUser();
//		// Step1.包裝解析
//		req = packageService.jsonToObj(new JSONObject(json_object));
//		// Step2.進行移除
//		check = itemService.deleteData(resp, req, user);
//		// Step3.進行判定
//		if (check) {
//			// Step4.包裝回傳
//			resp = packageService.setObjResp(resp, req, null);
//		} else {
//			// Step4.包裝Err回傳
//			packageService.setObjErrResp(resp, req);
//			resp = packageService.setObjResp(resp, req, null);
//		}
//		// 回傳-資料
//		return packageService.objToJson(resp);
//	}
}
