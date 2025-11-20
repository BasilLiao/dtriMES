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
import dtri.com.tw.service.OqcInspectionFormService;
import dtri.com.tw.service.PackageService;


@Controller
public class OqcInspectionFormController extends AbstractController {
	public OqcInspectionFormController() {
		super("oqc_inspection_form.basil");
	}

	@Autowired
	PackageService packageService;
	@Autowired
	OqcInspectionFormService inspectionFormService;

	/**
	 * 訪問
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/oqc_inspection_form.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check = inspectionFormService.getData(resp, req, user);
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
	@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check = inspectionFormService.getData(resp, req, user);
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
	@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.AC" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check = inspectionFormService.createData(resp, req, user);
		if (check) {
			check = inspectionFormService.save_asData(resp, req, user);
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
	@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.AU" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
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
		// Step2.進行修改
		check = inspectionFormService.updateData(resp, req, user);
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
	@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.AD" }, method = { RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
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
		// Step2.進行移除
		check = inspectionFormService.deleteData(resp, req, user);
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
		@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.S1" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
			check = inspectionFormService.getDataCustomized(resp, req, user);
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
		 * 修改/新增
		 */
		@ResponseBody
		@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.S2" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
		public String createCustomized(@RequestBody String json_object) {
			showSYS_CM("createCustomized");
			show(json_object);
			PackageBean req = new PackageBean();
			PackageBean resp = new PackageBean();
			boolean check = false;

			// Step0.當前用戶資料-UI權限
			SystemUser user = loginUser().getSystemUser();
			// Step1.包裝解析
			req = packageService.jsonToObj(new JSONObject(json_object));
			
			// Step2.進行查詢
			check = inspectionFormService.updateDataCustomized(resp, req, user);
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
		
		///***********************先不用 S3******************
		@ResponseBody
		@RequestMapping(value = { "/ajax/oqc_inspection_form.basil.S3" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
		public String reviewCustomized(@RequestBody String json_object) {
			showSYS_CM("reviewCustomized");
			show(json_object);
			PackageBean req = new PackageBean();
			PackageBean resp = new PackageBean();
			boolean check = false;

			// Step0.當前用戶資料-UI權限
			SystemUser user = loginUser().getSystemUser();
			// Step1.包裝解析
			req = packageService.jsonToObj(new JSONObject(json_object));
			
			// Step2.進行查詢
			check = inspectionFormService.reviewCustomized(resp, req, user);
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
