package dtri.com.tw.controller;

import org.json.JSONArray;
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
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AD" }, method = { RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
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
		check =  repairRmaListBatService.deleteData(resp, req, user);
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
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
		check =  repairRmaListBatService.getData(resp, req, user);		
		
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
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.AU" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
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
		check =  repairRmaListBatService.updateData(resp, req, user);
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
	 * (目前沒用)寄信 
	 */
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.S2" }, method = { RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	public String sendMail(@RequestBody String json_object) {
	    showSYS_CM("search");
	    show(json_object);
	    PackageBean req = new PackageBean();  // 建立 req 為 PackageBean 類別
	    PackageBean resp = new PackageBean();
	    boolean check = false;

	    // Step0. 當前用戶資料-UI 權限
	    SystemUser user = loginUser().getSystemUser();
	    // Step1. 包裝解析
	    req = packageService.jsonToObj(new JSONObject(json_object));
	    // Step2. 進行查詢	    
	    check =  repairRmaListBatService.getData(resp, req, user);
	    // Step3. 進行判定
	    if (check) {
	        // Step4. 包裝回傳		
	        resp = packageService.setObjResp(resp, req, null);
	        System.out.println("resp 設定後的 JSON：" + packageService.objToJson(resp));

	        // 取得 "search" 內的資料
	        JSONObject body = resp.getBody();
	        JSONArray searchResults = body.optJSONArray("search");
	        String mailList=body.optString("rmamail"); //取mail帳號 字串
//	      使用 optString("rmamail")：避免 NullPointerException，如果 rmamail 不存在，mailList 會是 ""（空字串）。
	        
	        String rmano = req.getBody().getJSONObject("search").getString("rma_sn"); //取出RMA號碼	        
	        String state = searchResults.getJSONObject(0).getString("state");
	   
//	        String[] toUser = mailList.split(";"); // 用 ";" 分割成 String 陣列
	        String[] toUser = {"johnny_chuang@dtri.com","ansolder@gmail.com"};
	        String[] toCcUser = {""};
	        String subject = "RMA 通知"+rmano + state+"TEST 寄信測試 ";

	        // 構建郵件內容
	        StringBuilder httpstr = new StringBuilder();
	        httpstr.append("Dear All, <br><br>")
	               .append("通知 ").append(rmano).append(state); //.append(" 請提領<br><br>");

	        if (searchResults != null && searchResults.length() > 0) {
	            httpstr.append("<table border='1'><tr>")
	                   .append("<th>State</th><th>RMA Number</th><th>Customer</th><th>Model</th><th>Serial Number</th><th>MB Number</th><th>Issue</th></tr>");

	            for (int i = 0; i < searchResults.length(); i++) {
	                JSONObject item = searchResults.getJSONObject(i);
	                httpstr.append("<tr>")
	                	   .append("<td>").append(item.getString("state")).append("</td>")
	                       .append("<td>").append(item.getString("rma_number")).append("</td>")
	                       .append("<td>").append(item.getString("customer")).append("</td>")
	                       .append("<td>").append(item.getString("model")).append("</td>")
	                       .append("<td>").append(item.getString("serial_number")).append("</td>")
	                       .append("<td>").append(item.getString("mb_number")).append("</td>")
	                       .append("<td>").append(item.getString("issue")).append("</td>")	                      
	                       .append("</tr>");
	            }
	            httpstr.append("</table>");
	        } else {
	         //   httpstr.append("目前無符合條件的 RMA 資料。");
	        }

	        // 發送郵件
	        mailService.sendEmail(toUser, toCcUser, subject, httpstr.toString(), null, null);
	    } else {
	        // Step4. 包裝 Err 回傳
	        packageService.setObjErrResp(resp, req);
	        resp = packageService.setObjResp(resp, req, null);
	    }

	    // 回傳 JSON
	    return packageService.objToJson(resp);
	}
	
	
	/**
	 * 上傳Excel
	 */	
	@ResponseBody
	@RequestMapping(value = { "/ajax/repair_rma_list_bat.basil.S1" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
			check =  repairRmaListBatService.excelToSave(resp, req, user);
		    	
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