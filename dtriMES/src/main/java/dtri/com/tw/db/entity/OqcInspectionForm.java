package dtri.com.tw.db.entity;

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
 * @see 製表檢驗單 <br>
 *      oif_id : ID <br>
 *      oif_ow : 工單 <br>
 *      oif_c_name : 客戶名稱<br>
 *      oif_o_nb :訂單號<br>
 *      oif_p_nb:產品號<br>
 *      oif_p_name:產品名<br>
 *      oif_p_model:產品型號<br>
 * 		oif_p_sn:產品序號區間<br>
 * 		oif_p_qty:出貨數 <br>
 * 		oif_t_qty:抽樣數<br>
 * 		oif_p_ver:版本資訊 <br>
 * 		oif_title:標題值 <br>
 * 		oif_c_date:製表日 <br>
 * 		oif_c_user:製表人 <br>
 * 		oif_e_date:最後檢驗日 <br>
 * 		oif_e_user:最後檢驗人 <br>
 * 		oif_f_date:審核日 <br>
 * 		oif_f_user:審核人 <br>
 * 		oif_oii_data:配置的檢驗項目 <br>
 * 		oif_oii_form:原始的HTML項目 <br>
 */
@Entity
@Table(name = "oqc_inspection_form")
@EntityListeners(AuditingEntityListener.class)
public class OqcInspectionForm  {


	public OqcInspectionForm() {
		this.sysheader = false;
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysnote = "";
	
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oqc_inspection_form_seq")
	@SequenceGenerator(name = "oqc_inspection_form_seq", sequenceName = "oqc_inspection_form_seq", allocationSize = 1)
	@Column(name = "oif_id")
	private Long oifid;

	@Column(name = "oif_ow", nullable = false, columnDefinition = "varchar(50)") //訂單
	private String oifow;
	
	@Column(name = "oif_c_name", nullable = false, columnDefinition = "varchar(50)") //客戶名稱
	private String oifcname;

	@Column(name = "oif_o_nb", nullable = false, columnDefinition = "varchar(50)") //訂單號
	private String oifonb;
	
	@Column(name = "oif_p_nb", nullable = false, columnDefinition = "varchar(50)") //產品料號
	private String oifpnb;
	
	@Column(name = "oif_p_name", nullable = false, columnDefinition = "varchar(200)") //產品品名
	private String oifpname;
	
	@Column(name = "oif_p_model", nullable = false, columnDefinition = "varchar(50)") //產品型號
	private String oifpmodel;
	
	@Column(name = "oif_p_sn", nullable = false, columnDefinition = "varchar(50)") //產品序號區間 Ex:0001_0009
	private String oifpsn;
	
	@Column(name = "oif_p_qty", nullable = false, columnDefinition = "int default 0") //出貨數
	private Integer oifpqty;
	
	@Column(name = "oif_t_qty", nullable = false, columnDefinition = "int default 0") //抽樣數
	private Integer oiftqty;
	
	@Column(name = "oif_p_ver", nullable = false, columnDefinition = "text default ''") //版本資訊 JSON 格式:
	private String oifpver;
	
	@Column(name = "oif_title", nullable = false, columnDefinition ="text default ''") //標題值
	private String oiftitle;

	@Column(name = "oif_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()") //製表日
	private Date oifcdate;
	
	@Column(name = "oif_c_user", nullable = false, columnDefinition = "varchar(50)") //製表人
	private String oifcuser;
	
	@Column(name = "oif_e_date", columnDefinition = "TIMESTAMP default now()") //最後檢日
	private Date oifedate;
	
	@Column(name = "oif_e_user", columnDefinition = "varchar(50)") //最後鑑驗人
	private String oifeuser;
	
	@Column(name = "oif_f_date", columnDefinition = "TIMESTAMP default now()") //審核日
	private Date oiffdate;
	
	@Column(name = "oif_f_user",  columnDefinition = "varchar(50)") //審核人
	private String oiffuser;
		
	@Column(name = "oif_oii_data", nullable = false, columnDefinition = "text default ''") //配置的檢驗項目 JSON
	private String oifoiidata;

	@Column(name = "oif_oii_form", columnDefinition = "text default ''") //原始的HTML項目
	private String oifoiiform;

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

	public Long getOifid() {
		return oifid;
	}

	public void setOifid(Long oifid) {
		this.oifid = oifid;
	}

	public String getOifow() {
		return oifow;
	}

	public void setOifow(String oifow) {
		this.oifow = oifow;
	}

	public String getOifcname() {
		return oifcname;
	}

	public void setOifcname(String oifcname) {
		this.oifcname = oifcname;
	}

	public String getOifonb() {
		return oifonb;
	}

	public void setOifonb(String oifonb) {
		this.oifonb = oifonb;
	}

	public String getOifpnb() {
		return oifpnb;
	}

	public void setOifpnb(String oifpnb) {
		this.oifpnb = oifpnb;
	}

	public String getOifpname() {
		return oifpname;
	}

	public void setOifpname(String oifpname) {
		this.oifpname = oifpname;
	}

	public String getOifpmodel() {
		return oifpmodel;
	}

	public void setOifpmodel(String oifpmodel) {
		this.oifpmodel = oifpmodel;
	}

	public String getOifpsn() {
		return oifpsn;
	}

	public void setOifpsn(String oifpsn) {
		this.oifpsn = oifpsn;
	}

	public Integer getOifpqty() {
		return oifpqty;
	}

	public void setOifpqty(Integer oifpqty) {
		this.oifpqty = oifpqty;
	}

	public Integer getOiftqty() {
		return oiftqty;
	}

	public void setOiftqty(Integer oiftqty) {
		this.oiftqty = oiftqty;
	}

	public String getOifpver() {
		return oifpver;
	}

	public void setOifpver(String oifpver) {
		this.oifpver = oifpver;
	}

	public String getOiftitle() {
		return oiftitle;
	}

	public void setOiftitle(String oiftitle) {
		this.oiftitle = oiftitle;
	}

	public Date getOifcdate() {
		return oifcdate;
	}

	public void setOifcdate(Date oifcdate) {
		this.oifcdate = oifcdate;
	}

	public String getOifcuser() {
		return oifcuser;
	}

	public void setOifcuser(String oifcuser) {
		this.oifcuser = oifcuser;
	}

	public Date getOifedate() {
		return oifedate;
	}

	public void setOifedate(Date oifedate) {
		this.oifedate = oifedate;
	}

	public String getOifeuser() {
		return oifeuser;
	}

	public void setOifeuser(String oifeuser) {
		this.oifeuser = oifeuser;
	}

	public Date getOiffdate() {
		return oiffdate;
	}

	public void setOiffdate(Date oiffdate) {
		this.oiffdate = oiffdate;
	}

	public String getOiffuser() {
		return oiffuser;
	}

	public void setOiffuser(String oiffuser) {
		this.oiffuser = oiffuser;
	}

	public String getOifoiidata() {
		return oifoiidata;
	}

	public void setOifoiidata(String oifoiidata) {
		this.oifoiidata = oifoiidata;
	}

	public String getOifoiiform() {
		return oifoiiform;
	}

	public void setOifoiiform(String oifoiiform) {
		this.oifoiiform = oifoiiform;
	}



}
