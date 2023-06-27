package dtri.com.tw.db.entity;

import java.util.Date;
import java.util.List;

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
 *      ro_id : 產品維修單_ID ex:RAM0123456789-R001<br>
 *      ro_c_id : 客戶( 單據 多對一 客戶)<br>
 *      ro_check : 檢核狀態(0=未結單 1=已結單) <br>
 *      ro_from : 產線:DTR/場外維修:RAM/如果是DTR單 則每日自動建立<br>
 *      ro_e_date : 完成日<br>
 *      ro_s_date : 寄出日<br>
 *      ro_g_date : 收到日<br>
 *      ro_ram_date : 申請RAM日期<br>
 * 
 */
@Entity
@Table(name = "repair_order")
@EntityListeners(AuditingEntityListener.class)
public class RepairOrder {
	public RepairOrder() {
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
	@Column(name = "ro_id")
	private String roid;

	@Column(name = "ro_c_id", nullable = false)
	private Long rocid;

	@Column(name = "ro_check", nullable = false)
	private Integer rocheck;

	@Column(name = "ro_from", nullable = false, columnDefinition = "varchar(50)")
	private String rofrom;

	@Column(name = "ro_e_date", columnDefinition = "TIMESTAMP default now()")
	private Date roedate;

	@Column(name = "ro_g_date", columnDefinition = "TIMESTAMP default now()")
	private Date rogdate;

	@Column(name = "ro_s_date", columnDefinition = "TIMESTAMP default now()")
	private Date rosdate;

	@Column(name = "ro_ram_date", columnDefinition = "TIMESTAMP default now()")
	private Date roramdate;

	@OneToMany(mappedBy = "order", orphanRemoval = true)
	//@OrderBy("register asc , rdstatement asc")
	private List<RepairDetail> details;

	public List<RepairDetail> getDetails() {
		return details;
	}

	public void setDetails(List<RepairDetail> details) {
		this.details = details;
	}

	public String getRoid() {
		return roid;
	}

	public void setRoid(String roid) {
		this.roid = roid;
	}

	public Long getRocid() {
		return rocid;
	}

	public void setRocid(Long rocid) {
		this.rocid = rocid;
	}

	public Integer getRocheck() {
		return rocheck;
	}

	public void setRocheck(Integer rocheck) {
		this.rocheck = rocheck;
	}

	public String getRofrom() {
		return rofrom;
	}

	public void setRofrom(String rofrom) {
		this.rofrom = rofrom;
	}

	public Date getRoedate() {
		return roedate;
	}

	public void setRoedate(Date roedate) {
		this.roedate = roedate;
	}

	public Date getRogdate() {
		return rogdate;
	}

	public void setRogdate(Date rogdate) {
		this.rogdate = rogdate;
	}

	public Date getRosdate() {
		return rosdate;
	}

	public void setRosdate(Date rosdate) {
		this.rosdate = rosdate;
	}

	public Date getRoramdate() {
		return roramdate;
	}

	public void setRoramdate(Date roramdate) {
		this.roramdate = roramdate;
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
