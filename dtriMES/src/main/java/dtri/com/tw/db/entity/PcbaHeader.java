package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see 產品製程<br>
 *      ph_id : ID<br>
 *      ph_model : 產品型號 固定為PCBA <br>
 *   	ph_pb_g_id : 關聯群組-SN清單<br>
 *      ph_pr_id : 製令工單<br>
 *      ph_type : 類型-製令工單<br>
 *      ph_wp_id : 關聯-工作站(程序ID)<br>
 *      ph_s_date : 開始-製成時間 <br>
 *      ph_e_date : 結束-製成時間 <br>    
 *      ph_schedule : 進度(X／X) <br>
 *      ph_pb_schedule : 各站_過站程序狀態(JSON)<br>    
 *      ph_p_name  : 產品號(Product Name) <br>      
 *      ph_p_number  :  產品批次驗整(part no) <br> 
 *      ph_order_id : "訂單編號"<br>    
 *      ph_c_name : "客戶名稱"<br>     
 *      ph_c_from : (單據來源)<br>    
 *      ph_p_qty :  "工單-需求數量"<br>
 *      ph_p_ok_qty : "工單-目前完成數量"<br>     
 *      ph_p_a_ok_qty : "工單-目前加工完成數量"<br>      
 *      ph_e_s_date : "預計出貨時間"<br>   
 *      ph_w_years : 保固年份<br>
 *      ph_api_data : 傳遞API<br>
 */
@Entity
@Table(name = "pcba_header")
@EntityListeners(AuditingEntityListener.class)
public class PcbaHeader {
	public PcbaHeader() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;
		this.phapidata = "";
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

	@Column(name = "sys_ver", columnDefinition = "int default 0")
	private Integer sysver;

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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pcba_header_seq")
	@SequenceGenerator(name = "pcba_header_seq", sequenceName = "pcba_header_seq", allocationSize = 1)
	@Column(name = "ph_id")
	private Long phid;

	@Column(name = "ph_model", columnDefinition = "varchar(30)")  //產品型號 固定為PCBA
	private String phmodel;

	@Column(name = "ph_pb_g_id")  //SN關聯表id
	private Long phpbgid;

	@Column(name = "ph_pr_id", columnDefinition = "varchar(50)") //製令單號
	private String phprid;

	@Column(name = "ph_type", columnDefinition = "varchar(50)") //製令單類型
	private String phtype;

	
	 @Column(name = "ph_wp_id", nullable = false, columnDefinition = "varchar(50)")
	 private Long phwpid;
	 
    //  工作站(程序ID)	
//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "ph_wp_id", referencedColumnName = "") //外鍵FOREIGN KEY
//	private ProductionRecords productionRecords;
	 
	
	@Column(name = "ph_s_date", columnDefinition = "TIMESTAMP")
	private Date phsdate;

	@Column(name = "ph_e_date", columnDefinition = "TIMESTAMP")
	private Date phedate;

	@Column(name = "ph_schedule", columnDefinition = "varchar(30)")
	private String phschedule;
	
	@Column(name = "ph_pb_schedule", columnDefinition = "varchar(200)")
	private String phpbschedule;	
	
	@Column(name = "ph_p_name", columnDefinition = "varchar(50)") //客戶的產品名稱/序號
	private String phpname;
	
	@Column(name = "ph_p_number", nullable = false, columnDefinition = "varchar(50) default ''")
	private String phpnumber;

	@Column(name = "ph_order_id", nullable = false, columnDefinition = "varchar(50) default ''")
	private String phorderid;
	
	@Column(name = "ph_c_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String phcname;
	
	@Column(name = "ph_c_from", nullable = false, columnDefinition = "varchar(50) default ''") //來源
	private String phcfrom;

	@Column(name = "ph_p_qty", nullable = false, columnDefinition = "int default 0")
	private Integer phpqty;
	
	@Column(name = "ph_p_ok_qty", columnDefinition = "int default 0")
	private Integer phpokqty;
	
	@Column(name = "ph_p_a_ok_qty", columnDefinition = "int default 0")
	private Integer phpaokqty;
	
	@Column(name = "ph_e_s_date", columnDefinition = "varchar(50)  default ''")
	private String phesdate;
	
	@Column(name = "ph_w_years", columnDefinition = "int default 0")
	private Integer phwyears;
	
	@Column(name = "ph_api_data", columnDefinition = "text default ''")
	private String phapidata;

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

	public Integer getSysver() {
		return sysver;
	}

	public void setSysver(Integer sysver) {
		this.sysver = sysver;
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

	public Long getPhid() {
		return phid;
	}

	public void setPhid(Long phid) {
		this.phid = phid;
	}

	public String getPhmodel() {
		return phmodel;
	}

	public void setPhmodel(String phmodel) {
		this.phmodel = phmodel;
	}

	public Long getPhpbgid() {
		return phpbgid;
	}

	public void setPhpbgid(Long phpbgid) {
		this.phpbgid = phpbgid;
	}

	public String getPhprid() {
		return phprid;
	}

	public void setPhprid(String phprid) {
		this.phprid = phprid;
	}

	public String getPhtype() {
		return phtype;
	}

	public void setPhtype(String phtype) {
		this.phtype = phtype;
	}

	public Long getPhwpid() {
		return phwpid;
	}

	public void setPhwpid(Long phwpid) {
		this.phwpid = phwpid;
	}

	public Date getPhsdate() {
		return phsdate;
	}

	public void setPhsdate(Date phsdate) {
		this.phsdate = phsdate;
	}

	public Date getPhedate() {
		return phedate;
	}

	public void setPhedate(Date phedate) {
		this.phedate = phedate;
	}

	public String getPhschedule() {
		return phschedule;
	}

	public void setPhschedule(String phschedule) {
		this.phschedule = phschedule;
	}

	public String getPhpbschedule() {
		return phpbschedule;
	}

	public void setPhpbschedule(String phpbschedule) {
		this.phpbschedule = phpbschedule;
	}

	public String getPhpname() {
		return phpname;
	}

	public void setPhpname(String phpname) {
		this.phpname = phpname;
	}

	public String getPhpnumber() {
		return phpnumber;
	}

	public void setPhpnumber(String phpnumber) {
		this.phpnumber = phpnumber;
	}

	public String getPhorderid() {
		return phorderid;
	}

	public void setPhorderid(String phorderid) {
		this.phorderid = phorderid;
	}

	public String getPhcname() {
		return phcname;
	}

	public void setPhcname(String phcname) {
		this.phcname = phcname;
	}

	public String getPhcfrom() {
		return phcfrom;
	}

	public void setPhcfrom(String phcfrom) {
		this.phcfrom = phcfrom;
	}

	public Integer getPhpqty() {
		return phpqty;
	}

	public void setPhpqty(Integer phpqty) {
		this.phpqty = phpqty;
	}

	public Integer getPhpokqty() {
		return phpokqty;
	}

	public void setPhpokqty(Integer phpokqty) {
		this.phpokqty = phpokqty;
	}

	public Integer getPhpaokqty() {
		return phpaokqty;
	}

	public void setPhpaokqty(Integer phpaokqty) {
		this.phpaokqty = phpaokqty;
	}

	public String getPhesdate() {
		return phesdate;
	}

	public void setPhesdate(String phesdate) {
		this.phesdate = phesdate;
	}

	public Integer getPhwyears() {
		return phwyears;
	}

	public void setPhwyears(Integer phwyears) {
		this.phwyears = phwyears;
	}

	public String getPhapidata() {
		return phapidata;
	}

	public void setPhapidata(String phapidata) {
		this.phapidata = phapidata;
	}


}
