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
 * @see 系統設定<br>
 *      rc_id : ID<br>
 *      rc_name : 名稱<br>
 *      rc_g_id : 群組ID<br>
 *      rc_g_name : 群組名稱<br>
 *      rc_value : 設定參數<br>
 *      rc_f_analyst:(優先)故障分對象<br>
 */
@Entity
@Table(name = "repair_code")
@EntityListeners(AuditingEntityListener.class)
public class RepairCode {
	public RepairCode() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "repair_code_seq")
	@SequenceGenerator(name = "repair_code_seq", sequenceName = "repair_code_seq", allocationSize = 1)
	@Column(name = "rc_id")
	private Long rcid;

	@Column(name = "rc_g_id", nullable = false)
	private Long rcgid;

	@Column(name = "rc_name", nullable = false, columnDefinition = "varchar(50)")
	private String rcname;

	@Column(name = "rc_g_name", nullable = false, columnDefinition = "varchar(50)")
	private String rcgname;

	@Column(name = "rc_value", nullable = false, columnDefinition = "varchar(50)")
	private String rcvalue;
	
	@Column(name = "rc_f_analyst", columnDefinition = "varchar(50) default ''")
	private String rcfanalyst;
	

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

	public Long getRcid() {
		return rcid;
	}

	public void setRcid(Long rcid) {
		this.rcid = rcid;
	}

	public Long getRcgid() {
		return rcgid;
	}

	public void setRcgid(Long rcgid) {
		this.rcgid = rcgid;
	}

	public String getRcname() {
		return rcname;
	}

	public void setRcname(String rcname) {
		this.rcname = rcname;
	}

	public String getRcgname() {
		return rcgname;
	}

	public void setRcgname(String rcgname) {
		this.rcgname = rcgname;
	}

	public String getRcvalue() {
		return rcvalue;
	}

	public void setRcvalue(String rcvalue) {
		this.rcvalue = rcvalue;
	}

	public String getRcfanalyst() {
		return rcfanalyst;
	}

	public void setRcfanalyst(String rcfanalyst) {
		this.rcfanalyst = rcfanalyst;
	}
}
