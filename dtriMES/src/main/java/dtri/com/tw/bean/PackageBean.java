package dtri.com.tw.bean;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Basil 包裝 回傳 or 傳遞 物件
 * 
 **/
public class PackageBean {
	// 常用代號
	// danger--warning--success
	public static final String info_color_success = "success";
	public static final String info_color_warning = "warning";
	public static final String info_color_danger = "danger";
	public static final String info_success = "[000] The command was executed [Successfully/成功/Thành công]!!";
	public static final String info_success_save = "[001] The command was executed (Save/存檔/Lưu trữ) [Successfully/成功/Thành công]!!";
	public static final String info_success_check = "[002] The command was executed (Check/檢查/kiểm tra) [Successfully/成功/Thành công]!!";

	public static final String info_warning = "[100] The command was executed [Warning/警告/Cảnh cáo]!!";
	public static final String info_warning_save = "[105] The command was executed (Save/存檔/Lưu trữ) [Warning/警告/Cảnh cáo]!!";
	public static final String info_warning_check = "[106] The command was executed (Check/檢查/kiểm tra) [Warning/警告/Cảnh cáo]!!";
	public static final String info_warning_same_data = "[107] The data is duplicated (資料重複)";

	public static final String info_warning1_NotFindUser = "[101] Unable to get user information [Warning]!!";
	public static final String info_warning2_NotFind = "[102] Did not find any results (查無資料)[Warning]!!";
	public static final String info_warning3_SQLNotRight = "[103] SQL not Right [Warning]!!";
	public static final String info_warning3 = "[103] The command was executed [Warning]!!";
	public static final String info_warning4 = "[104] The command was executed [Warning]!!";

	public static final String info_work_p_warning1 = "[WP001] 此工作站被使用中 , 請檢查(工作站-流程管理)! [Warning]!!";

	public static final String info_work_warning0 = "[WK000] 無此 (工作站 在此 作業程序) , 請檢查(工作站)! [Warning]!!";
	public static final String info_work_warning1 = "[WK001] 無此 (工作站) , 請檢查(工作站)! [Warning]!!";
	public static final String info_work_warning2 = "[WK002] 無此 (製令單號) , 請檢查(製令單號)! [Warning]!!";
	public static final String info_work_warning3 = "[WK003] 無此 (產品/燒錄 序號) 序號 , 請檢查 (產品/燒錄 序號)! [Warning]!!";
	public static final String info_work_warning4 = "[WK004] 無此 (產品/燒錄 序號) 與 (製令單號) 關聯資料 , 請檢查 (產品/燒錄 序號) 與 (製令單號)! [Warning]!!";
	public static final String info_work_warning4_1 = "[WK004_1] 無此 ([舊]產品/燒錄 序號) 關聯資料 , 請檢查 ([舊]產品/燒錄 序號)! [Warning]!!";
	public static final String info_work_warning4_2 = "[WK004_2] 此 ([舊]產品/燒錄 製令單號) 為同一關聯資料 , 請檢查 ([舊]產品/燒錄 序號)! [Warning]!!";
	public static final String info_work_warning5 = "[WK005] 前站 (產品/燒錄 序號) 未過站 , 請遞交給 (前站人員)! [Warning]!!";
	public static final String info_work_warning6 = "[WK006] 無此 (標籤機 代號) , 請檢查(標籤機 代號)! [Warning]!!";
	public static final String info_work_warning7 = "[WK007] 無此 (PLT檔案/格式/大小) , 請檢查 (是否上傳 內容)! [Warning]!!";
	public static final String info_work_warning8 = "[WK008] 無此 ([舊]產品/燒錄 序號) , 請檢查 ([舊]產品/燒錄 序號)! [Warning]!!";
	public static final String info_work_warning9 = "[WK009] 無此 ([舊]產品/燒錄 序號) 已被使用, 請檢查 ([舊]產品/燒錄 序號)! [Warning]!!";
	public static final String info_work_warning10 = "[WK010] 特定(SN序號) 只能輸入在 (產品/燒錄 序號) 內使用 請檢查! [Warning]!!";
	public static final String info_work_warning11 = "[WK011] 特定(SN序號) 重複 請檢查";
	public static final String info_work_warning12 = "[WK012] 無法(LOG File) 無法備份資料 至 /PMS_LOG_BACKUP 下,請通知 [檔案管理員]!!";
	public static final String info_work_warning13 = "[WK013] FTP(LOG File) 無法 正確連線 or 帳密錯誤 ,請通知 [檔案管理員]!!";
	public static final String info_work_warning14 = "[WK014] 特定(SN序號) 請檢查";
	public static final String info_work_warning15 = "[WK015] FTP(LOG File) 檔案內容 資訊含有 亂碼 ,請通知 [檔案管理員]!!";
	public static final String info_work_warning16 = "[WK016] 此 (製令單號) 已結單/終止/暫停! [Warning]!!";
	public static final String info_work_warning17 = "[WK017] 特定([SN序號]與[產品規格]) 不相合! ";
	public static final String info_work_warning18 = "[WK018] 特定(PLT [SN序號]與[產品規格]) 不相合! ";
	public static final String info_work_warning19 = "[WK019] 請先清除 [故障代碼] 後進行過站! [Warning]!!";
	public static final String info_work_success20 = "[WK020] The command was executed [Successfully/成功/Thành công]!!";
	public static final String info_work_success21 = "[WK021] 存檔 此(產品/燒錄 序號) 已 重複過站!!";
	public static final String info_work_success22 = "[WK022] 檢查 此(產品/燒錄 序號) 已 重複過站!!";
	public static final String info_work_warning23 = "[WK023] 檢查 此(產品) 規格異常! [Warning]!!";

	public static final String info_Maint_warning01 = "[MT001] 此 (維修單) 已結單/終止/暫停! [Warning]!!";
	public static final String info_Maint_warning02 = "[MT002] 此 (維修單) [維修中] 不可異動 分配單位! [Warning]!!";
	public static final String info_Maint_warning03 = "[MT003] 無此 (維修單) 請再次檢查! [Warning]!!";
	public static final String info_Maint_warning04 = "[MT004] 您非[高級維修單位]成員, 請向 管理者 申請! [Warning]!!";

	public static final String info_search_warning0 = "[SH000] 查詢 過多筆資料 ,請更正條件! [Warning]!!";

	public static final String info_danger = "[502] The command was executed [ERROR]!!";
	public static final String info_administrator = " Please contact the system administrator #321";

	// "resp_content" or "req_content"
	private String type_content;// 請求 or 回傳
	private Date date;// 時間
	private String action;// 請求 動作行為
	private JSONObject header;// title 名稱表
	private JSONObject analysis;// anlysis report 報告設定
	private JSONObject body;// 資料 內 容物
	private JSONObject body_type;// 資料 內 [容物類型群組][新增需不需要:群組/只有一般] (type:group,createOnly:(all/general))
	private Integer page_total;// 每次 總頁數 ex :10
	private Integer page_batch;// 第幾批
	private Integer page_now_nb;// 第幾分頁
	private String info; // 回傳資訊
	private String info_color; // 回傳資訊 顏色
	private String call_bk_fn;// 回傳呼叫方法
	private JSONObject call_bk_vals;// 回傳呼叫傳遞值
	private String html_body;// 切換頁面
	private JSONObject html_permission;// 權限頁面限制
	private JSONObject info_user;// 使用者資訊
	private JSONArray cell_searchs;// 查詢欄位
	private JSONArray cell_modify;// 修改欄位
	private JSONArray cell_refresh;// 修改欄位更新

	private JSONArray cell_g_modify;// 修改群組欄位
	private String error_ms;// 特定錯誤訊息
	private String type;// 錯誤代號

	public PackageBean() {
		this.info = info_success;
		this.info_color = info_color_success;
		this.body_type = new JSONObject("{'type':'general','createOnly':'all'}");//
		this.date = new Date();
		this.error_ms = "";
		this.type = "";
		this.call_bk_vals = new JSONObject();
	}

	/**
	 * @param type 100 : 一般錯誤<br>
	 *             101 : 沒使用者資訊<br>
	 *             102 : 沒找到東西<br>
	 * 
	 **/
	public void autoMsssage(String type) {
		this.setType(type);
		this.info_color = info_color_warning;
		switch (type) {
		case "001":// 一般指令 存檔
			this.info_color = info_color_success;
			this.info = info_success_save;
			break;
		case "002":// 一般指令 檢查
			this.info_color = info_color_success;
			this.info = info_success_check;
			break;
		case "100":// 一般指令錯誤
			this.info = info_warning;
			break;
		case "101":// 沒使用者資訊
			this.info = info_warning1_NotFindUser;
			break;
		case "102":// 沒找到東西
			this.info = info_warning2_NotFind;
			break;
		case "103":// SQL查詢格式異常
			this.info = info_warning3_SQLNotRight;
			break;
		case "107":// 資料重複
			this.info = info_warning_same_data + error_ms + " [Warning]!!";
			break;
		case "WP001":// [WP001] 此工作站被使用中 , 請檢查(工作站-流程管理)! [Warning]!!"
			this.info = info_work_p_warning1;
			break;
		case "WK001":// 無此[工作站],請檢查[工作站]! [Warning]!!"
			this.info = info_work_warning1;
			break;
		case "WK002":// 無此[製令單],請檢查[製令單號]! [Warning]!!"
			this.info = info_work_warning2;
			break;
		case "WK003":// 無此[產品/燒錄] 序號,請檢查[產品/燒錄]! [Warning]
			this.info = info_work_warning3;
			break;
		case "WK004":// "[WK004] 無此[(產品/燒錄) 與 (製令單號)]比對 ,請檢查[(產品/燒錄) 與 (製令單號)]! [Warning]!!"
			this.info = info_work_warning4;
			break;
		case "WK004_1":// "[WK004-1] 無此[[舊](產品/燒錄)]比對 ,請檢查[[舊](產品/燒錄)]! [Warning]!!"
			this.info = info_work_warning4_1;
			break;
		case "WK004_2":// "[WK004-1] 無此[[舊](產品/燒錄)]比對 ,請檢查[[舊](產品/燒錄)]! [Warning]!!"
			this.info = info_work_warning4_2;
			break;
		case "WK005":// "[WK005] 前站[產品/燒錄]未過站 ,請檢遞交給[前站人員]!
			this.info = info_work_warning5;
			break;
		case "WK006":// [WK006] 無此[標籤機代號] ,請檢查[標籤機代號]!
			this.info = info_work_warning6;
			break;
		case "WK007":// "[WK007] 無此 (PLT檔案) , 請檢查 (是否上傳正確)! [Warning]!!"
			this.info = info_work_warning7;
			break;
		case "WK008":// "[WK008] 無此 ([舊]產品/燒錄 序號) , 請檢查 ([舊]產品/燒錄 序號)!
			this.info = info_work_warning8;
			break;
		case "WK009":// "[WK009] 不可使用自己(工單號) 登入 ([舊]產品/燒錄 序號), 請檢查 ([舊]產品/燒錄 序號)!
			this.info = info_work_warning9;
			break;
		case "WK010":// "[WK010] 無此(此序號SN) 只能輸入在 (SN燒錄序號) 內使用 請檢查! [Warning]!!
			this.info = info_work_warning10;
			break;
		case "WK011":// [WK011] 特定(SN序號) 重複 請檢查!
			this.info = info_work_warning11 + error_ms + " [Warning]!!";
			break;
		case "WK012":// [WK012] FTP(LOG File) 無法備份資料 至 /PMS_LOG_BACKUP 下,請通知 [檔案管理員]!!
			this.info = info_work_warning12;
			break;
		case "WK013":// [WK013] FTP(LOG File) 無法 正確連線 ,請通知 [檔案管理員]!!
			this.info = info_work_warning13;
			break;
		case "WK014":// [WK014] 特定(SN序號) 重複 請檢查!
			this.info = info_work_warning14 + error_ms + " [Warning]!!";
			break;
		case "WK015":// [WK015] FTP(LOG File) 檔案內容 資訊含有 亂碼 ,請通知 [檔案管理員]!!
			this.info = info_work_warning15;
			break;
		case "WK016":// [WK016] 此 (製令單號) 已結單/終止/暫停! [Warning]!!
			this.info = info_work_warning16;
			break;
		case "WK017":// [WK017] 特定(SN序號) 與規格數量不相合!
			this.info = info_work_warning17 + error_ms + " [Warning]!!";
			break;
		case "WK018":// [WK018] FTP(LOG File) 與規格數量不相合!
			this.info = info_work_warning18 + error_ms + " [Warning]!!";
			break;
		case "WK019":// [WK019] 請先清除 [故障代碼] 後進行過站! [Warning]!!
			this.info = info_work_warning19;
			break;
		case "WK020":// [WK020] The command was executed [Successfully/成功/Thành công]!!
			this.info = info_work_success20;
			this.info_color = info_color_success;
			break;
		case "WK021":// [WK021] 存檔 此(產品/燒錄 序號) 已 重複過站!!
			this.info = info_work_success21;
			this.info_color = info_color_success;
			break;
		case "WK022":// [WK022] 檢查 此(產品/燒錄 序號) 已 重複過站!!
			this.info = info_work_success22;
			this.info_color = info_color_success;
			break;
		case "WK023":// [WK023] 檢查 此(產品) 規格異常! [Warning]!!
			this.info = info_work_warning23;
			break;

		case "WK000":// 無此[工作站],請檢查[工作站]! [Warning]!!"
			this.info = info_work_warning0;
			break;
		case "SH000":// [SH000] 查詢資料多餘5000 筆資料 ,請更正條件! [Warning]!!
			this.info = info_search_warning0;
			break;
		case "MT001":// [MT000] 此 (維修單) 已結單/終止/暫停! [Warning]!!
			this.info = info_Maint_warning01;
			break;
		case "MT002":// [MT002] 此 (維修單) [維修中] 不可異動! [Warning]!!";
			this.info = info_Maint_warning02;
			break;
		case "MT003":// [MT003] 無此 (維修單) 請再次檢查! [Warning]!!";
			this.info = info_Maint_warning03;
			break;
		case "MT004":// [MT004] 您非 高級維修單位成員, 請向 管理者 申請! [Warning]!!";
			this.info = info_Maint_warning04;
			break;
			
			
		default:// 不明錯誤
			this.info = info_danger + info_administrator;
			this.info_color = info_color_danger;
			break;
		}

	}

	public String getType_content() {
		return type_content;
	}

	public void setType_content(String type_content) {
		this.type_content = type_content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getaction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public JSONObject getBody() {
		return body;
	}

	public void setBody(JSONObject body) {
		this.body = body;
	}

	public Integer getPage_total() {
		return page_total;
	}

	public void setPage_total(Integer page_total) {
		this.page_total = page_total;
	}

	public Integer getPage_batch() {
		return page_batch;
	}

	public void setPage_batch(Integer page_batch) {
		this.page_batch = page_batch;
	}

	public Integer getPage_now_nb() {
		return page_now_nb;
	}

	public void setPage_now_nb(Integer page_now_nb) {
		this.page_now_nb = page_now_nb;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public JSONObject getHeader() {
		return header;
	}

	public void setHeader(JSONObject header) {
		this.header = header;
	}

	public String getInfo_color() {
		return info_color;
	}

	public void setInfo_color(String info_color) {
		this.info_color = info_color;
	}

	public String getHtml_body() {
		return html_body;
	}

	public void setHtml_body(String html_body) {
		this.html_body = html_body;
	}

	public JSONObject getInfo_user() {
		return info_user;
	}

	public void setInfo_user(JSONObject info_user) {
		this.info_user = info_user;
	}

	public JSONArray getCell_searchs() {
		return cell_searchs;
	}

	public void setCell_searchs(JSONArray cell_searchs) {
		this.cell_searchs = cell_searchs;
	}

	public JSONArray getCell_modify() {
		return cell_modify;
	}

	public void setCell_modify(JSONArray cell_modify) {
		this.cell_modify = cell_modify;
	}

	public String getCall_bk_fn() {
		return call_bk_fn;
	}

	public void setCall_bk_fn(String call_bk_fn) {
		this.call_bk_fn = call_bk_fn;
	}

	public JSONObject getCall_bk_vals() {
		return call_bk_vals;
	}

	public void setCall_bk_vals(JSONObject call_bk_vals) {
		this.call_bk_vals = call_bk_vals;
	}

	public JSONObject getBody_type() {
		return body_type;
	}

	public void setBody_type(JSONObject body_type) {
		this.body_type = body_type;
	}

	public JSONObject getHtml_permission() {
		return html_permission;
	}

	public void setHtml_permission(JSONObject html_permission) {
		this.html_permission = html_permission;
	}

	public JSONArray getCell_g_modify() {
		return cell_g_modify;
	}

	public void setCell_g_modify(JSONArray cell_g_modify) {
		this.cell_g_modify = cell_g_modify;
	}

	public JSONArray getCell_refresh() {
		return cell_refresh;
	}

	public void setCell_refresh(JSONArray cell_refresh) {
		this.cell_refresh = cell_refresh;
	}

	public JSONObject getAnalysis() {
		return analysis;
	}

	public void setAnalysis(JSONObject analysis) {
		this.analysis = analysis;
	}

	public String getError_ms() {
		return error_ms;
	}

	public void setError_ms(String error_ms) {
		this.error_ms = error_ms;
	}

	public JSONObject permissionToJson(String[] ps) {
		JSONObject psJson = new JSONObject();
		psJson.put("s3", ps[0]);
		psJson.put("s2", ps[1]);
		psJson.put("s1", ps[2]);
		psJson.put("se", ps[3]);
		psJson.put("do", ps[4]);
		psJson.put("up", ps[5]);
		psJson.put("cr", ps[6]);
		psJson.put("mo", ps[7]);
		psJson.put("de", ps[8]);
		psJson.put("se", ps[9]);
		return psJson;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
