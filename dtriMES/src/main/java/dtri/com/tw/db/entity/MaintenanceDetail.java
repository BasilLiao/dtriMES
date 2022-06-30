package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see <br>
 *      md_id : 單據項目_(單位)RAM類型:R001-R999/DTR類型:D001-D999/Self類型:S001-S999<br>
 *      ex:RAM0123456789-R001<br>
 *      md_mr_sn:產品/主板/任何有序號唯一性 SN<br>
 *      md_mu_id:指派單位<br>
 *      md_statement:問題描述<br>
 *      md_u_qty:單位 數量<br>
 *      md_true: 實際問題情況<br>
 *      md_experience: 修理心得<br>
 *      md_svg:圖片資料<br>
 *      md_finally:修復好的結果 例如:換掉主板<br>
 *      md_u_finally:修復人
 * 
 */
@Entity
@Table(name = "maintenance_detail")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceDetail {
	public MaintenanceDetail() {
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
	@Column(name = "md_id")
	private String mdid;
	
	@ManyToOne(targetEntity = MaintenanceRegister.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "md_mr_sn")
	private MaintenanceRegister register;

	@Column(name = "md_mu_id", nullable = false, columnDefinition = "varchar(50)")
	private Long mdmuid;

	@Column(name = "md_statement", nullable = false, columnDefinition = "varchar(250)")
	private String mdstatement;

	@Column(name = "md_true", columnDefinition = "varchar(250)")
	private String mdtrue;

	@Column(name = "md_experience", columnDefinition = "varchar(250)")
	private String mdexperience;

	@Column(name = "md_svg", nullable = false, columnDefinition = "varchar(50)")
	private String mdsvg;

	@Column(name = "md_u_finally", columnDefinition = "varchar(30)")
	private String mdufinally;

	@Column(name = "mdfinally", columnDefinition = "varchar(250)")
	private String mdfinally;

	@OneToOne(mappedBy = "mdetail")
	private MaintenanceOrder mOrder;

	public MaintenanceOrder getmOrder() {
		return mOrder;
	}

	public void setmOrder(MaintenanceOrder mOrder) {
		this.mOrder = mOrder;
	}

	public String getMdid() {
		return mdid;
	}

	public void setMdid(String mdid) {
		this.mdid = mdid;
	}
	
	public Long getMdmuid() {
		return mdmuid;
	}

	public void setMdmuid(Long mdmuid) {
		this.mdmuid = mdmuid;
	}

	public String getMdstatement() {
		return mdstatement;
	}

	public void setMdstatement(String mdstatement) {
		this.mdstatement = mdstatement;
	}

	public String getMdtrue() {
		return mdtrue;
	}

	public void setMdtrue(String mdtrue) {
		this.mdtrue = mdtrue;
	}

	public String getMdexperience() {
		return mdexperience;
	}

	public void setMdexperience(String mdexperience) {
		this.mdexperience = mdexperience;
	}

	public String getMdsvg() {
		return mdsvg;
	}

	public void setMdsvg(String mdsvg) {
		this.mdsvg = mdsvg;
	}

	public String getMdufinally() {
		return mdufinally;
	}

	public void setMdufinally(String mdufinally) {
		this.mdufinally = mdufinally;
	}

	public String getMdfinally() {
		return mdfinally;
	}

	public void setMdfinally(String mdfinally) {
		this.mdfinally = mdfinally;
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
