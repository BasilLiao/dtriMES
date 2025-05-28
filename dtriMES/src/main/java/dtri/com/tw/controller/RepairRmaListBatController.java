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
import dtri.com.tw.service.BasicNotificationMailService;
import dtri.com.tw.service.PackageService;
import dtri.com.tw.service.RepairRmaListBatService;

@Controller
public class RepairRmaListBatController extends AbstractController {
	public RepairRmaListBatController() {
		super("repair_rma_list_bat.basil");// 功能
	}

	@Autowired
	PackageService packageService;
	@Autowired
	RepairRmaListBatService repairRmaListBatService;

	@Autowired
	BasicNotificationMailService mailService;

	/**
	 * 訪問
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check = repairRmaListBatService.getData(resp, req, user);
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
	 * delete
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AD" }, method = {
			RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
	public String delete(@RequestBody String json_object) {
		showSYS_CM("delete");
		show(json_object);

		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		SystemUser user = loginUser().getSystemUser();
		// Step1.包裝解析
		req = packageService.jsonToObj(new JSONObject(json_object));
		// Step2.進行data
		check = repairRmaListBatService.deleteData(resp, req, user);
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
	 * 查詢
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AR" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check = repairRmaListBatService.getData(resp, req, user);

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
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AU" }, method = {
			RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
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
		check = repairRmaListBatService.updateData(resp, req, user);
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
	 * 上傳Excel
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.S1" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	public String uploadExcel(@RequestBody String json_object) {
		System.out.println("uploadExcel");

		// Step 1: 初始化
		PackageBean req = new PackageBean();
		PackageBean resp = new PackageBean();
		boolean check = false;

		// Step 2: 當前用戶資料
		SystemUser user = loginUser().getSystemUser();

		// Step 3: JSON 解析到 PackageBean
		req = packageService.jsonToObj(new JSONObject(json_object));
		try {
			// Step 4: 處理文件邏輯 (例如解析 Excel 或其他操作)
			check = repairRmaListBatService.excelToSave(resp, req, user);

			System.out.println("Step 4: 處理文件邏輯 (例如解析 Excel 或其他操作)");

		} catch (Exception e) {
			e.printStackTrace();
			packageService.setObjErrResp(resp, req); // 包裝錯誤回應
		}

		// Step 5: 包裝回應數據
		if (check) {
			resp = packageService.setObjResp(resp, req, null);
		} else {
			packageService.setObjErrResp(resp, req); // 確保有錯誤回應格式
			resp = packageService.setObjResp(resp, req, null);
		}
		// Step 6: 回傳 JSON 字串
		return packageService.objToJson(resp);
	}

}