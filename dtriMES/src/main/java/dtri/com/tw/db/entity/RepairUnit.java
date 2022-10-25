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
 *      ru_id : 單位_ID<br>
 *      ru_name : 單位_名稱<br>
 *      ru_su_id : 帳號關聯_ID<br>
 *      ru_su_account : 帳號關聯_帳號<br>
 *      ru_su_name : 帳號關聯_名稱<br>
 *      ru_content : 可處理內容敘述<br>
 *      ru_cell_mail : 自動通知Mail<br>
 * 
 */
@Entity
@Table(name = "repair_unit")
@EntityListeners(AuditingEntityListener.class)
public class RepairUnit {
	public RepairUnit() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "repair_unit_seq")
	@SequenceGenerator(name = "repair_unit_seq", sequenceName = "repair_unit_seq", allocationSize = 1)
	@Column(name = "ru_id")
	private Long ruid;

	@Column(name = "ru_g_id", nullable = false)
	private Long rugid;

	@Column(name = "ru_g_name", nullable = false, columnDefinition = "varchar(50)")
	private String rugname;

	@Column(name = "ru_su_id", nullable = false)
	private Long rusuid;

	@Column(name = "ru_su_account", columnDefinition = "varchar(50)")
	private String rusuaccount;

	@Column(name = "ru_su_name", nullable = false, columnDefinition = "varchar(50)")
	private String rusuname;

	@Column(name = "ru_content", nullable = false, columnDefinition = "varchar(255)")
	private String rucontent;

	@Column(name = "ru_cell_mail", nullable = false, columnDefinition = "boolean default false")
	private Boolean rucellmail;

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

	public Long getRuid() {
		return ruid;
	}

	public void setRuid(Long ruid) {
		this.ruid = ruid;
	}

	public Long getRugid() {
		return rugid;
	}

	public void setRugid(Long rugid) {
		this.rugid = rugid;
	}

	public String getRugname() {
		return rugname;
	}

	public void setRugname(String rugname) {
		this.rugname = rugname;
	}

	public Long getRusuid() {
		return rusuid;
	}

	public void setRusuid(Long rusuid) {
		this.rusuid = rusuid;
	}

	public String getRusuname() {
		return rusuname;
	}

	public void setRusuname(String rusuname) {
		this.rusuname = rusuname;
	}

	public String getRucontent() {
		return rucontent;
	}

	public void setRucontent(String rucontent) {
		this.rucontent = rucontent;
	}

	public Boolean getRucellmail() {
		return rucellmail;
	}

	public void setRucellmail(Boolean rucellmail) {
		this.rucellmail = rucellmail;
	}

	public String getRusuaccount() {
		return rusuaccount;
	}

	public void setRusuaccount(String rusuaccount) {
		this.rusuaccount = rusuaccount;
	}

}