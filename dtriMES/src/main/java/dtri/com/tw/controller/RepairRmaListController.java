package dtri.com.tw.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemPermission;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.service.PackageService;
import dtri.com.tw.service.RepairRmaListService;

@Controller
public class RepairRmaListController extends AbstractController {
	public RepairRmaListController() {
		super("repair_rma_list.basil");// 功能
	}

	@Autowired
	PackageService packageService;
	@Autowired
	RepairRmaListService repairRmaListService;

	/**
	 * 訪問
	 */
	@ResponseBody    //按下menu下的單元會先執行訪問
	@RequestMapping(value = { "/ajax/repair_rma_list.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String access(@RequestBody String json_object) {
		showSYS_CM("access");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		SystemPermission pern = permissionUI();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行查詢
		check = repairRmaListService.getData(resp, req, user);
		check = repairRmaListService.getDataCustomized(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, resp.permissionToJson(pern.getSppermission().split("")));
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	/**
	 * 查詢
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String search(@RequestBody String json_object) {
		showSYS_CM("search");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行查詢
		check = repairRmaListService.getData(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	/**
	 * 新增
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.AC" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String create(@RequestBody String json_object) {
		showSYS_CM("create");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行新增
		check = repairRmaListService.createData(resp, req, user);
		if (check) {
			check = repairRmaListService.save_asData(resp, req, user);
		}
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.AU" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	public String modify(@RequestBody String json_object) {
		showSYS_CM("modify");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行新增
		if (req.getCall_bk_fn().equals("customized_search")) {
			check = repairRmaListService.updateDataCustomized(resp, req, user);
		} else {
			check = repairRmaListService.updateData(resp, req, user);
		}
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	/**
	 * 移除
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.AD" }, method = { RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
	public String delete(@RequestBody String json_object) {
		showSYS_CM("delete");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行新增
		check = repairRmaListService.deleteData(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	// ============================客製化Customized============================
	/**
	 * 查詢
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.S1" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String searchCustomized(@RequestBody String json_object) {
		showSYS_CM("searchCustomized");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行查詢
		check = repairRmaListService.getDataCustomized(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.S2" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	public String modifyCustomized(@RequestBody String json_object) {
		showSYS_CM("modifyCustomized");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行新增
		check = repairRmaListService.updateDataCustomized(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}
	

	/**
	 * 查詢 Reprot
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list.basil.RT" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String report(@RequestBody String json_object) {
		showSYS_CM("report");
		show(json_object);
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step0.當前用戶資料-UI權限
		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行查詢
		check = repairRmaListService.getReportData(resp, req, user);
		// Step3.進行判定
		if (check) {
			// Step4.包裝回傳
			resp = packageService.setObjResp(resp, req, null);
		} else {
			// Step4.包裝Err回傳
			packageService.setObjErrResp(resp, req);
			resp = packageService.setObjResp(resp, req, null);
		}
		// 回傳-資料
		return packageService.objToJson(resp);
	}

}