package dtri.com.tw.bean;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 呈現報告
 */

public class ProductionDailyBean {
	public ProductionDailyBean() {
		
	}

	private Long id;// ID
	private String sysmdate;// 時間
	private String pdwcline;// 產線
	private String pdwcclass;// 班別
	private String pdprid;// 工單
	private JSONArray pdwpbname;// 工作站[{"wcname":"D0001","wpbname":"加工站","qty":"50"},{},{}]
	private JSONObject pdphpbschedule;// 工作站累計數
	private String pdprbomid;// BOM
	private String pdprpmodel;// 產品型號
	private String pdprtotal;// 工單 總數量
	private String pdbadqty;// 工單 目前壞掉數量

	private String pdprokqty;// 工單 [累計]產品數 好數量
	private String pdprttokqty;// 工單 [累計]產品測試數 好數量
	private String pdprbadqty;// 工單 [累計]產品數 壞掉數量
	private String pdpryield;// 工單 [累計]產品數 良率比

	private String pdttqty;// 工單 [當日]測試數 好數量
	private String pdttbadqty;// 工單 [當日]測試數 壞掉數量
	private String pdttyield;// 工單 [當日]測試數 良率比

	private String pdtqty;
	private Double pdttime;// 總時間
	private Date sysmdatemsort;// 最後修改時間

	public String getPdttqty() {
		return pdttqty;
	}

	public void setPdttqty(String pdttqty) {
		this.pdttqty = pdttqty;
	}

	public String getPdttbadqty() {
		return pdttbadqty;
	}

	public void setPdttbadqty(String pdttbadqty) {
		this.pdttbadqty = pdttbadqty;
	}

	public String getPdttyield() {
		return pdttyield;
	}

	public void setPdttyield(String pdttyield) {
		this.pdttyield = pdttyield;
	}

	public String getPdprbadqty() {
		return pdprbadqty;
	}

	public void setPdprbadqty(String pdprbadqty) {
		this.pdprbadqty = pdprbadqty;
	}

	public String getPdpryield() {
		return pdpryield;
	}

	public void setPdpryield(String pdpryield) {
		this.pdpryield = pdpryield;
	}

	public String getSysmdate() {
		return sysmdate;
	}

	public void setSysmdate(String sysmdate) {
		this.sysmdate = sysmdate;
	}

	public String getPdwcline() {
		return pdwcline;
	}

	public void setPdwcline(String pdwcline) {
		this.pdwcline = pdwcline;
	}

	public String getPdwcclass() {
		return pdwcclass;
	}

	public void setPdwcclass(String pdwcclass) {
		this.pdwcclass = pdwcclass;
	}

	public String getPdprid() {
		return pdprid;
	}

	public void setPdprid(String pdprid) {
		this.pdprid = pdprid;
	}

	public JSONArray getPdwpbname() {
		return pdwpbname;
	}

	public void setPdwpbname(JSONArray pdwpbname) {
		this.pdwpbname = pdwpbname;
	}

	public String getPdprbomid() {
		return pdprbomid;
	}

	public void setPdprbomid(String pdprbomid) {
		this.pdprbomid = pdprbomid;
	}

	public String getPdprpmodel() {
		return pdprpmodel;
	}

	public void setPdprpmodel(String pdprpmodel) {
		this.pdprpmodel = pdprpmodel;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPdtqty() {
		return pdtqty;
	}

	public void setPdtqty(String pdtqty) {
		this.pdtqty = pdtqty;
	}

	public Double getPdttime() {
		return pdttime;
	}

	public void setPdttime(Double pdttime) {
		this.pdttime = pdttime;
	}

	public String getPdprtotal() {
		return pdprtotal;
	}

	public void setPdprtotal(String pdprtotal) {
		this.pdprtotal = pdprtotal;
	}

	public String getPdprokqty() {
		return pdprokqty;
	}

	public void setPdprokqty(String pdprokqty) {
		this.pdprokqty = pdprokqty;
	}

	public String getPdbadqty() {
		return pdbadqty;
	}

	public void setPdbadqty(String pdbadqty) {
		this.pdbadqty = pdbadqty;
	}

	public JSONObject getPdphpbschedule() {
		return pdphpbschedule;
	}

	public void setPdphpbschedule(JSONObject pdphpbschedule) {
		this.pdphpbschedule = pdphpbschedule;
	}

	public String getPdprttokqty() {
		return pdprttokqty;
	}

	public void setPdprttokqty(String pdprttokqty) {
		this.pdprttokqty = pdprttokqty;
	}

	public Date getSysmdatemsort() {
		return sysmdatemsort;
	}

	public void setSysmdatemsort(Date sysmdatemsort) {
		this.sysmdatemsort = sysmdatemsort;
	}

}
