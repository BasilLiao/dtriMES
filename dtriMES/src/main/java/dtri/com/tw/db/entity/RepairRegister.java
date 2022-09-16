package dtri.com.tw.db.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see <br>
 *      rr_sn : ID(產品SN號)[產品/主板/有序號的]1:多[維修細節]<br>
 *      rr_c_sn : 客戶產品SN號<br>
 *      rr_pr_id : 製令單號<br>
 *      rr_ph_p_qty : 製令單 數量<br>
 *      rr_pr_p_model : 產品型號<br>
 *      rr_ph_w_years : 產品保故<br>
 *      rr_pb_sys_m_date : 產品製造日<br>
 *      rr_pb_type : 產品類型<br>
 *      rr_expired : 產品過保?<br>
 *      rr_v : 產品/主板/小板 版本<br>
 *      rr_f_ok 修好?1 = 待修中/2 = 以處已/3 = 已報廢<br>
 */
@Entity
@Table(name = "repair_register")
@EntityListeners(AuditingEntityListener.class)
public class RepairRegister {
	public RepairRegister() {
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
	@Column(name = "rr_sn")
	private String rrsn;

	@Column(name = "rr_c_sn", columnDefinition = "varchar(50)")
	private String rrcsn;

	@Column(name = "rr_pr_id", columnDefinition = "varchar(50)")
	private String rrprid;

	@Column(name = "rr_ph_p_qty")
	private Integer rrphpqty;

	@Column(name = "rr_pr_p_model")
	private String rrprpmodel;

	@Column(name = "rr_ph_w_years", columnDefinition = "int default 0")
	private Integer rrphwyears;

	@Column(name = "rr_expired", nullable = false, columnDefinition = "boolean default false")
	private Boolean rrexpired;

	@Column(name = "rr_pb_sys_m_date")
	private Date rrpbsysmdate;

	@Column(name = "rr_pb_type", nullable = false)
	private String rrpbtype;

	@Column(name = "rr_v", columnDefinition = "varchar(50)")
	private String rrv;

	@Column(name = "rr_f_ok", nullable = false, columnDefinition = "int default 0")
	private Integer rrfok;

	@OneToMany(mappedBy = "register", cascade = { CascadeType.ALL })
	private List<RepairDetail> details;

	public String getRrsn() {
		return rrsn;
	}

	public void setRrsn(String rrsn) {
		this.rrsn = rrsn;
	}

	public String getRrcsn() {
		return rrcsn;
	}

	public void setRrcsn(String rrcsn) {
		this.rrcsn = rrcsn;
	}

	public String getRrprid() {
		return rrprid;
	}

	public void setRrprid(String rrprid) {
		this.rrprid = rrprid;
	}

	public Integer getRrphpqty() {
		return rrphpqty;
	}

	public void setRrphpqty(Integer rrphpqty) {
		this.rrphpqty = rrphpqty;
	}

	public String getRrprpmodel() {
		return rrprpmodel;
	}

	public void setRrprpmodel(String rrprpmodel) {
		this.rrprpmodel = rrprpmodel;
	}

	public Integer getRrphwyears() {
		return rrphwyears;
	}

	public void setRrphwyears(Integer rrphwyears) {
		this.rrphwyears = rrphwyears;
	}

	public Boolean getRrexpired() {
		return rrexpired;
	}

	public void setRrexpired(Boolean rrexpired) {
		this.rrexpired = rrexpired;
	}

	public Date getRrpbsysmdate() {
		return rrpbsysmdate;
	}

	public void setRrpbsysmdate(Date rrpbsysmdate) {
		this.rrpbsysmdate = rrpbsysmdate;
	}

	public String getRrpbtype() {
		return rrpbtype;
	}

	public void setRrpbtype(String rrpbtype) {
		this.rrpbtype = rrpbtype;
	}

	public String getRrv() {
		return rrv;
	}

	public void setRrv(String rrv) {
		this.rrv = rrv;
	}

	public Integer getRrfok() {
		return rrfok;
	}

	public void setRrfok(Integer rrfok) {
		this.rrfok = rrfok;
	}

	public List<RepairDetail> getDetails() {
		return details;
	}

	public void setDetails(List<RepairDetail> details) {
		this.details = details;
	}

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
}
