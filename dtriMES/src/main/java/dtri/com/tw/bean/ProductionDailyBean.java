package dtri.com.tw.bean;

import org.json.JSONArray;

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
	private JSONArray pdwpbname;// 工作站[{"wcname":"D0001","wpbmane":"加工站","qty":"50"},{},{}]
	private String pdprbomid;// BOM
	private String pdprpmodel;// 產品型號
	private String pdprtotal;//工單 總數量
	private String pdprokqty;//工單 目前好數量
	private String pdprbadqty;//工單 目前壞掉數量
	
	private String pdtqty;
	private Double pdttime;// 總時間

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

	public String getPdprbadqty() {
		return pdprbadqty;
	}

	public void setPdprbadqty(String pdprbadqty) {
		this.pdprbadqty = pdprbadqty;
	}

}
