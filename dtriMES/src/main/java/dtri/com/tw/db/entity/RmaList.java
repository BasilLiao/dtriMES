package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      
 *      id : 主key <br>     
 *      rma_number : rma號碼<br>
 *      model : 產品型號<br>
 *      part_no : Oracle part no<br>
 *      customer : 產品型號<br>
 *      serial_number : 機器序號<br>
 *      mb_number : MB序號<br>
 *      rrd_rma_result : RMA維修結果<br>
 *      issue : 客戶所述不良原因<br>
 *      state_check : 狀態確認<br>
 *      state : 目前狀態<br>
 *      send_track_num : 寄貨追蹤號碼 <br>
 *      send_date : 寄出日 <br>
 *      recd_track_num :到貨追蹤號碼<br>
 *      recd_date : 收到日 <br>
 */

@Entity
@Table(name = "rma_list")
@EntityListeners(AuditingEntityListener.class)
public class RmaList {
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
	// nullable = false代表 這個欄位不能為 NULL
	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rma_number", columnDefinition = "varchar(20)")
	private String rmaNumber;

	@Column(name = "model", columnDefinition = "varchar(20) DEFAULT 'UNKNOWN'")
	private String model;

	// Oracle part no
	@Column(name = "part_no", columnDefinition = "varchar(20) DEFAULT 'UNKNOWN'")
	private String partNo;

	// Oracle 保固
	@Column(name = "wty_status", columnDefinition = "varchar(20) DEFAULT 'UNKNOWN'")
	private String wtyStatus;

	@Column(name = "customer", columnDefinition = "varchar(20)")
	private String customer;

	@Column(name = "serial_number", columnDefinition = "varchar(255) DEFAULT 'UNKNOWN'")
	private String serialNumber;

	@Column(name = "mb_number", columnDefinition = "varchar(255) DEFAULT 'UNKNOWN'")
	private String mbNumber;

	@Column(name = "issue", columnDefinition = "varchar(500) DEFAULT 'UNKNOWN'")
	private String issue;

	@Column(name = "rrd_rma_result", columnDefinition = "varchar(10)")
	private String rrd_RmaResult;

	@Column(name = "state_check", columnDefinition = "int default 0")
	private Integer stateCheck;

	@Column(name = "state", columnDefinition = "varchar(10)")
	private String state;

	@Column(name = "send_track_num", columnDefinition = "varchar(25)")
	private String sendtracknum;
	
	@Column(name = "send_date", columnDefinition = "varchar(25)")
	private String senddate;
	
	@Column(name = "recd_track_num", columnDefinition = "varchar(25)")
	private String recdtracknum;
	
	@Column(name = "recd_date", columnDefinition = "varchar(25)")
	private String recddate;
	
	// 如果進來的值是null 或 空 的轉預設為N/A
	@PrePersist
	@PreUpdate
	public void setDefaultValues() {
		if (this.partNo == null || this.partNo.trim().isEmpty()) {
			this.partNo = "N/A";
		}
		if (this.wtyStatus == null || this.wtyStatus.trim().isEmpty()) {
			this.wtyStatus = "N/A";
		}
		if (this.serialNumber == null || this.serialNumber.trim().isEmpty()) {
			this.serialNumber = "N/A";
		}
		if (this.mbNumber == null || this.mbNumber.trim().isEmpty()) {
			this.mbNumber = "N/A";
		}
		if (this.model == null || this.model.trim().isEmpty()) {
			this.model = "N/A";
		}
	}
	// Constructors, Getters, and Setters

	public RmaList() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;		
		this.rmaNumber = "";
		this.model = "";
		this.customer = "";
		this.serialNumber = "";
		this.mbNumber = "";
		this.issue = "";
		this.wtyStatus = "";
		this.partNo = "";
		this.rrd_RmaResult = "";
		this.stateCheck = 0; // 預設 0
		this.state = "未收到";
		this.sendtracknum="";
		this.senddate="";
		this.recdtracknum="";
		this.recddate="";
		
	}
	//上傳EXCEL資料的方法
	public RmaList(String rmaNumber, String model, String customer, String serialNumber, String mbNumber, String issue,
			String partNo, String wtyStatus,String syscuser, String sysmuser) {
		this.syscdate = new Date();
		this.syscuser = syscuser;
		this.sysmdate = new Date();
		this.sysmuser = sysmuser;
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;

		this.rmaNumber = rmaNumber;
		this.model = model;
		this.customer = customer;
		this.serialNumber = serialNumber;
		this.mbNumber = mbNumber;
		this.issue = issue;
		this.wtyStatus = wtyStatus;
		this.partNo = partNo;

		this.rrd_RmaResult = "";
		this.stateCheck = 0; // 預設 0
		this.state = "未收到";
		this.sendtracknum="";
		this.senddate="";
		this.recdtracknum="";
		this.recddate="";
	}

	// 共用
	public String getSyscuser() {
		return syscuser;
	}

	public void setSyscuser(String syscuser) {
		this.syscuser = syscuser;
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

	public Date getSyscdate() {
		return syscdate;
	}

	public void setSyscdate(Date syscdate) {
		this.syscdate = syscdate;
	}

	public Date getSysmdate() {
		return sysmdate;
	}

	public void setSysmdate(Date sysmdate) {
		this.sysmdate = sysmdate;
	}

	// ****
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRmaNumber() {
		return rmaNumber;
	}

	public void setRmaNumber(String rmaNumber) {
		this.rmaNumber = rmaNumber;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getMbNumber() {
		return mbNumber;
	}

	public void setMbNumber(String mbNumber) {
		this.mbNumber = mbNumber;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getWtyStatus() {
		return wtyStatus;
	}

	public void setWtyStatus(String wtyStatus) {
		this.wtyStatus = wtyStatus;
	}

	public String getRrd_RmaResult() {
		return rrd_RmaResult;
	}

	public void setRrd_RmaResult(String rrd_RmaResult) {
		this.rrd_RmaResult = rrd_RmaResult;
	}

	public Integer getStateCheck() {
		return stateCheck;
	}

	public void setStateCheck(Integer stateCheck) {
		this.stateCheck = stateCheck;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSendtracknum() {
		return sendtracknum;
	}

	public void setSendtracknum(String sendtracknum) {
		this.sendtracknum = sendtracknum;
	}

	public String getSenddate() {
		return senddate;
	}

	public void setSenddate(String senddate) {
		this.senddate = senddate;
	}

	public String getRecdtracknum() {
		return recdtracknum;
	}

	public void setRecdtracknum(String recdtracknum) {
		this.recdtracknum = recdtracknum;
	}

	public String getRecddate() {
		return recddate;
	}

	public void setRecddate(String recddate) {
		this.recddate = recddate;
	}

}
