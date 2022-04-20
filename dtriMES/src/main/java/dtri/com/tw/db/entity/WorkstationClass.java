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
 * @see 產線班別設定<br>
 *      wcid = ID<br>
 *      wcstime = 班別開始時間<br>
 *      wcetime = 班別結束時間<br>
 *      wclname = 工作組長名稱<br>
 *      wclsuid = 工作組長ID<br>
 *      wcmname = 工作領班名稱<br>
 *      wcmsuid = 工作領班ID<br>
 *      wcpline = 產線線別<br>
 *      wcclass = 班別(早中晚加班...)<br>
 *      wcwcname = 工作站code條碼<br>
 *      wcwpbname = 工作站名稱<br>
 *      wcwtime = 工時計算?<br>
 *      wcwquantity = 台數計算?<br>
 *      wcsauto = 工作站過站時 自動啟動?<br>
 *      wceauto = 工作站 自動抓取最後過站時間?<br>
 *      wcgroup = 群組模式/單人模式<br>
 */
@Entity
@Table(name = "workstation_class")
@EntityListeners(AuditingEntityListener.class)
public class WorkstationClass {
	public WorkstationClass() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workstation_class_seq")
	@SequenceGenerator(name = "workstation_class_seq", sequenceName = "workstation_class_seq", allocationSize = 1)
	@Column(name = "wc_id")
	private Long wcid;

	@Column(name = "wc_s_time", nullable = false, columnDefinition = "varchar(50)")
	private String wcstime;

	@Column(name = "wc_e_time", nullable = false, columnDefinition = "varchar(50)")
	private String wcetime;

	@Column(name = "wc_l_name", nullable = false, columnDefinition = "varchar(50)")
	private String wclname;

	@Column(name = "wc_l_su_id", nullable = false)
	private Long wclsuid;

	@Column(name = "wc_m_name", nullable = false, columnDefinition = "varchar(50)")
	private String wcmname;

	@Column(name = "wc_m_su_id", nullable = false)
	private Long wcmsuid;

	@Column(name = "wc_p_line", nullable = false, columnDefinition = "varchar(50)")
	private String wcpline;

	@Column(name = "wc_class", nullable = false, columnDefinition = "varchar(50)")
	private String wcclass;

	@Column(name = "wc_w_c_name", nullable = false, columnDefinition = "varchar(50)")
	private String wcwcname;

	@Column(name = "wc_w_pb_name", nullable = false, columnDefinition = "varchar(50)")
	private String wcwpbname;

	@Column(name = "wc_w_time", nullable = false, columnDefinition = "boolean default false")
	private String wcwtime;

	@Column(name = "wc_w_quantity", nullable = false, columnDefinition = "boolean default false")
	private String wcwquantity;

	@Column(name = "wc_s_auto", nullable = false, columnDefinition = "boolean default false")
	private String wcsauto;

	@Column(name = "wc_e_auto", nullable = false, columnDefinition = "boolean default false")
	private String wceauto;

	@Column(name = "wc_group", nullable = false, columnDefinition = "boolean default false")
	private String wcgroup;

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

	public Long getWcid() {
		return wcid;
	}

	public void setWcid(Long wcid) {
		this.wcid = wcid;
	}

	public String getWcstime() {
		return wcstime;
	}

	public void setWcstime(String wcstime) {
		this.wcstime = wcstime;
	}

	public String getWcetime() {
		return wcetime;
	}

	public void setWcetime(String wcetime) {
		this.wcetime = wcetime;
	}

	public String getWclname() {
		return wclname;
	}

	public void setWclname(String wclname) {
		this.wclname = wclname;
	}

	public Long getWclsuid() {
		return wclsuid;
	}

	public void setWclsuid(Long wclsuid) {
		this.wclsuid = wclsuid;
	}

	public String getWcmname() {
		return wcmname;
	}

	public void setWcmname(String wcmname) {
		this.wcmname = wcmname;
	}

	public Long getWcmsuid() {
		return wcmsuid;
	}

	public void setWcmsuid(Long wcmsuid) {
		this.wcmsuid = wcmsuid;
	}

	public String getWcpline() {
		return wcpline;
	}

	public void setWcpline(String wcpline) {
		this.wcpline = wcpline;
	}

	public String getWcclass() {
		return wcclass;
	}

	public void setWcclass(String wcclass) {
		this.wcclass = wcclass;
	}

	public String getWcwcname() {
		return wcwcname;
	}

	public void setWcwcname(String wcwcname) {
		this.wcwcname = wcwcname;
	}

	public String getWcwpbname() {
		return wcwpbname;
	}

	public void setWcwpbname(String wcwpbname) {
		this.wcwpbname = wcwpbname;
	}

	public String getWcwtime() {
		return wcwtime;
	}

	public void setWcwtime(String wcwtime) {
		this.wcwtime = wcwtime;
	}

	public String getWcwquantity() {
		return wcwquantity;
	}

	public void setWcwquantity(String wcwquantity) {
		this.wcwquantity = wcwquantity;
	}

	public String getWcsauto() {
		return wcsauto;
	}

	public void setWcsauto(String wcsauto) {
		this.wcsauto = wcsauto;
	}

	public String getWceauto() {
		return wceauto;
	}

	public void setWceauto(String wceauto) {
		this.wceauto = wceauto;
	}

	public String getWcgroup() {
		return wcgroup;
	}

	public void setWcgroup(String wcgroup) {
		this.wcgroup = wcgroup;
	}
}
