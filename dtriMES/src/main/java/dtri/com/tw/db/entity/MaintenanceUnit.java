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
 * @see 單位設定<br>
 *      mu_id : 單位_ID<br>
 *      mu_name : 單位_名稱<br>
 *      mu_su_id : 帳號關聯_ID<br>
 *      mu_su_name : 帳號關聯_名稱<br>
 *      mu_content : 可處理內容敘述<br>
 */
@Entity
@Table(name = "maintenance_unit")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceUnit {
	public MaintenanceUnit() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maintenance_unit_seq")
	@SequenceGenerator(name = "maintenance_unit_seq", sequenceName = "maintenance_unit_seq", allocationSize = 1)
	@Column(name = "mu_id")
	private Long muid;

	@Column(name = "mu_g_id", nullable = false)
	private Long mugid;

	@Column(name = "mu_g_name", nullable = false, columnDefinition = "varchar(50)")
	private String mugname;

	@Column(name = "mu_su_id", nullable = false)
	private Long musuid;

	@Column(name = "mu_su_name", nullable = false, columnDefinition = "varchar(50)")
	private String musuname;

	@Column(name = "mu_content", nullable = false, columnDefinition = "varchar(255)")
	private String mucontent;

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

	public Long getMuid() {
		return muid;
	}

	public void setMuid(Long muid) {
		this.muid = muid;
	}

	public Long getMugid() {
		return mugid;
	}

	public void setMugid(Long mugid) {
		this.mugid = mugid;
	}

	public String getMugname() {
		return mugname;
	}

	public void setMugname(String mugname) {
		this.mugname = mugname;
	}

	public Long getMusuid() {
		return musuid;
	}

	public void setMusuid(Long musuid) {
		this.musuid = musuid;
	}

	public String getMusuname() {
		return musuname;
	}

	public void setMusuname(String musuname) {
		this.musuname = musuname;
	}

	public String getMucontent() {
		return mucontent;
	}

	public void setMucontent(String mucontent) {
		this.mucontent = mucontent;
	}

}
