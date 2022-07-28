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
 *      mr_sn : ID(產品SN號)[產品/主板/有序號的]1:多[維修細節]<br>
 *      mr_c_sn : 客戶產品SN號<br>
 *      mr_pr_id : 製令單號<br>
 *      mr_pr_p_qty : 製令單 數量<br>
 *      mr_pr_p_model : 產品型號<br>
 *      mr_pr_w_years : 產品保故<br>
 *      mr_pb_sys_m_date : 產品製造日<br>
 *      mr_pb_type : 產品類型<br>
 *      mr_expired : 產品過保?<br>
 *      mr_v : 產品/主板/小板 版本<br>
 *      mr_f_ok 修好?false = 壞了/true = 正在處理<br>
 */
@Entity
@Table(name = "maintenance_register")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceRegister {
	public MaintenanceRegister() {
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
	@Column(name = "mr_sn")
	private String mrsn;

	@Column(name = "mr_c_sn", columnDefinition = "varchar(50)")
	private String mrcsn;

	@Column(name = "mr_pr_id", columnDefinition = "varchar(50)")
	private String mrprid;

	@Column(name = "mr_pr_p_qty")
	private Integer mrprpqty;

	@Column(name = "mr_pr_p_model")
	private String mrprpmodel;

	@Column(name = "mr_pr_w_years", columnDefinition = "int default 0")
	private Integer mrprwyears;
	
	@Column(name = "mr_expired", nullable = false, columnDefinition = "boolean default false")
	private Boolean mrexpired;
	
	@Column(name = "mr_pb_sys_m_date")
	private Date mrpbsysmdate;

	@Column(name = "mr_pb_type", nullable = false)
	private String mrpbtype;

	@Column(name = "mr_v", columnDefinition = "varchar(50)")
	private String mrv;

	@Column(name = "mr_f_ok", nullable = false, columnDefinition = "boolean default false")
	private Boolean mrfok;

	@OneToMany(mappedBy = "register",cascade = {CascadeType.ALL})
	private List<MaintenanceDetail> details;

	public Boolean getMrexpired() {
		return mrexpired;
	}

	public void setMrexpired(Boolean mrexpired) {
		this.mrexpired = mrexpired;
	}

	public List<MaintenanceDetail> getDetails() {
		return details;
	}

	public void setDetails(List<MaintenanceDetail> details) {
		this.details = details;
	}

	public String getMrsn() {
		return mrsn;
	}

	public void setMrsn(String mrsn) {
		this.mrsn = mrsn;
	}

	public String getMrcsn() {
		return mrcsn;
	}

	public void setMrcsn(String mrcsn) {
		this.mrcsn = mrcsn;
	}

	public String getMrprid() {
		return mrprid;
	}

	public void setMrprid(String mrprid) {
		this.mrprid = mrprid;
	}

	public Integer getMrprpqty() {
		return mrprpqty;
	}

	public void setMrprpqty(Integer mrprpqty) {
		this.mrprpqty = mrprpqty;
	}

	public String getMrprpmodel() {
		return mrprpmodel;
	}

	public void setMrprpmodel(String mrprpmodel) {
		this.mrprpmodel = mrprpmodel;
	}

	public Integer getMrprwyears() {
		return mrprwyears;
	}

	public void setMrprwyears(Integer mrprwyears) {
		this.mrprwyears = mrprwyears;
	}

	public Date getMrpbsysmdate() {
		return mrpbsysmdate;
	}

	public void setMrpbsysmdate(Date mrpbsysmdate) {
		this.mrpbsysmdate = mrpbsysmdate;
	}

	public String getMrpbtype() {
		return mrpbtype;
	}

	public void setMrpbtype(String mrpbtype) {
		this.mrpbtype = mrpbtype;
	}

	public String getMrv() {
		return mrv;
	}

	public void setMrv(String mrv) {
		this.mrv = mrv;
	}

	public Boolean getMrfok() {
		return mrfok;
	}

	public void setMrfok(Boolean mrfok) {
		this.mrfok = mrfok;
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
