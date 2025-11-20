package dtri.com.tw.db.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see 產品製程-主體<br>
 *      oii_id : ID <br>
 *      oii_test_nb : 測試項條件(產品品號)<br>
 *      oii_test_model :測試項條件(產品型號)<br>
 *      oii_test_client:測試項條件(客戶名稱)<br>
 *      oii_test_country:測試項條件(國家名稱)<br>
 *      oii_check_name:檢查項目名稱<br>
 * 		oii_check_val:檢查內容值 如果不需要則不需要填寫 <br>
 * 		oii_check_type:檢查輸入類型 1.一般入 2.下拉式選單 3.勾選式<br>
 * 		oii_check_options:(如果是下拉式/勾選 請用,區隔 Ex:[key_val,key_val]<br>
 * 		oii_title_nb:標題條件(產品號) <br>
 * 		oii_title_model:標題條件(產品型號) <br>
 * 		oii_title_val:標題值 <br>
 */
@Entity
@Table(name = "oqc_inspection_items")
@EntityListeners(AuditingEntityListener.class)
public class OqcInspectionItems implements Serializable {

	private static final long serialVersionUID = 1L;

	public OqcInspectionItems() {
		this.sysheader = false;
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.syssort = 0;
		this.sysstatus = 0;
//		this.sysver = 0;
		this.sysnote = "";
		this.oiititleval="";
	}

	// 共用型
	@Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date syscdate;

	@Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String syscuser;

	@Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysmdate;

	@Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysmuser;
	
//	@Column(name = "sys_ver", columnDefinition = "int default 0")
//	private Integer sysver;

	@Column(name = "sys_note", columnDefinition = "text default ''")
	private String sysnote;

	@Column(name = "sys_sort", columnDefinition = "int default 0")
	private Integer syssort;

	@Column(name = "sys_status", columnDefinition = "int default 0")
	private Integer sysstatus;

	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;

	// 功能項目
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oqc_inspection_items_seq")
	@SequenceGenerator(name = "oqc_inspection_items_seq", sequenceName = "oqc_inspection_items_seq", allocationSize = 1)
	@Column(name = "oii_id")
	private Long oiiid;

//	@Column(name = "oii_test_nb", nullable = false, columnDefinition = "varchar(50)") //測試項條件(產品品號)
//	private String oiitestnb;

//	@Column(name = "oii_test_model", nullable = false, columnDefinition = "varchar(50)") //測試項條件(產品型號)
//	private String oiitestmodel;
	
//	@Column(name = "oii_test_client", nullable = false, columnDefinition = "varchar(50)") //測試項條件(客戶名稱)
//	private String oiitestclient;
	
//	@Column(name = "oii_test_country", nullable = false, columnDefinition = "varchar(50)") //測試項條件(國家名稱)
//	private String oiitestcountry;
	
	@Column(name = "oii_check_name", nullable = false, columnDefinition = "varchar(50)") //檢查項目名稱
	private String oiicheckname;	
	
	@Column(name = "oii_check_val", nullable = false, columnDefinition = "varchar(50)") //檢查內容值 如果不需要則不需填寫
	private String oiicheckval;
	
	@Column(name = "oii_check_type", nullable = false, columnDefinition = "varchar(50)") //檢查輸入類型 0.空白　1.一般入 2.下拉式選單 3.勾選式
	private String oiichecktype;
	
	@Column(name = "oii_check_options", nullable = false, columnDefinition = "varchar(50)") //檢可自訂值(如果是下拉式/勾選 請用,區隔 Ex:[key_val,key_val]
	private String oiicheckoptions;
	
//	@Column(name = "oii_title_nb", nullable = false, columnDefinition = "varchar(50)") //標題條件(產品號)
//	private String oiititlenb;
	
//	@Column(name = "oii_title_model", nullable = false, columnDefinition = "varchar(50)") //標題條件(產品型號)
//	private String oiititlemodel;

	@Column(name = "oii_title_val", nullable = false, columnDefinition = "text default ''") //標題值
	private String oiititleval;

	public Date getSyscdate() {
		return syscdate;
	}

	public void setSyscdate(Date syscdate) {
		this.syscdate = syscdate;
	}

	public String getSyscuser() {
		return syscuser;
	}

	public void setSyscuser(String syscuser) {
		this.syscuser = syscuser;
	}

	public Date getSysmdate() {
		return sysmdate;
	}

	public void setSysmdate(Date sysmdate) {
		this.sysmdate = sysmdate;
	}

	public String getSysmuser() {
		return sysmuser;
	}

	public void setSysmuser(String sysmuser) {
		this.sysmuser = sysmuser;
	}

	public String getSysnote() {
		return sysnote;
	}

	public void setSysnote(String sysnote) {
		this.sysnote = sysnote;
	}

	public Integer getSyssort() {
		return syssort;
	}

	public void setSyssort(Integer syssort) {
		this.syssort = syssort;
	}

	public Integer getSysstatus() {
		return sysstatus;
	}

	public void setSysstatus(Integer sysstatus) {
		this.sysstatus = sysstatus;
	}

	public Boolean getSysheader() {
		return sysheader;
	}

	public void setSysheader(Boolean sysheader) {
		this.sysheader = sysheader;
	}

	public Long getOiiid() {
		return oiiid;
	}

	public void setOiiid(Long oiiid) {
		this.oiiid = oiiid;
	}

//	public String getOiitestnb() {
//		return oiitestnb;
//	}

//	public void setOiitestnb(String oiitestnb) {
//		this.oiitestnb = oiitestnb;
//	}

//	public String getOiitestmodel() {
//		return oiitestmodel;
//	}

//	public void setOiitestmodel(String oiitestmodel) {
//		this.oiitestmodel = oiitestmodel;
//	}

//	public String getOiitestclient() {
//		return oiitestclient;
//	}

//	public void setOiitestclient(String oiitestclient) {
//		this.oiitestclient = oiitestclient;
//	}

//	public String getOiitestcountry() {
//		return oiitestcountry;
//	}

//	public void setOiitestcountry(String oiitestcountry) {
//		this.oiitestcountry = oiitestcountry;
//	}

	public String getOiicheckname() {
		return oiicheckname;
	}

	public void setOiicheckname(String oiicheckname) {
		this.oiicheckname = oiicheckname;
	}

	public String getOiicheckval() {
		return oiicheckval;
	}

	public void setOiicheckval(String oiicheckval) {
		this.oiicheckval = oiicheckval;
	}

	public String getOiichecktype() {
		return oiichecktype;
	}

	public void setOiichecktype(String oiichecktype) {
		this.oiichecktype = oiichecktype;
	}

	
	public String getOiicheckoptions() {
		return oiicheckoptions;
	}

	public void setOiicheckoptions(String oiicheckoptions) {
		this.oiicheckoptions = oiicheckoptions;
	}

//	public String getOiititlenb() {
//		return oiititlenb;
//	}

//	public void setOiititlenb(String oiititlenb) {
//		this.oiititlenb = oiititlenb;
//	}

//	public String getOiititlemodel() {
//		return oiititlemodel;
//	}

//	public void setOiititlemodel(String oiititlemodel) {
//		this.oiititlemodel = oiititlemodel;
//	}

	public String getOiititleval() {
		return oiititleval;
	}

	public void setOiititleval(String oiititleval) {
		this.oiititleval = oiititleval;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
