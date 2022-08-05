package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see <br>
 *      rd_id : 單據項目_(單位)RAM類型:R001-R999/DTR類型:D001-D999/Self類型:S001-S999<br>
 *      ex:RAM0123456789-R001<br>
 *      rd_ro_id : 單據編號 ex:RAM0123456789<br>
 *      rd_rr_sn:產品/主板/任何有序號唯一性 SN<br>
 *      rd_ru_id:指派單位<br>
 *      rd_statement:問題描述<br>
 *      rd_u_qty:單位 數量<br>
 *      rd_true: 實際問題情況<br>
 *      rd_experience: 修理心得<br>
 *      rd_svg:圖片資料<br>
 *      rd_check:檢核狀態(0=已申請(尚未收到) 1=已檢核(收到) 2=已處理(完成修復) 3=轉處理 4=修不好(丟棄報廢)
 *      5=已寄回(結單)<br>
 *      rd_finally:true =已解決 /false =尚未解決<br>
 *      rd_u_finally:修復人
 * 
 */
@Entity
@Table(name = "repair_detail")
@EntityListeners(AuditingEntityListener.class)
public class RepairDetail {
	public RepairDetail() {
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
	@Column(name = "rd_id")
	private String rdid;

	@ManyToOne(targetEntity = RepairRegister.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "rd_rr_sn")
	private RepairRegister register;

	@ManyToOne(targetEntity = RepairOrder.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "rd_ro_id")
	private RepairOrder order;

	@Column(name = "rd_statement", nullable = false, columnDefinition = "varchar(250)")
	private String rdstatement;

	@Column(name = "rd_true", columnDefinition = "varchar(250)")
	private String rdtrue;

	@Column(name = "rd_experience", columnDefinition = "varchar(250)")
	private String rdexperience;

	@Column(name = "rd_svg", columnDefinition = "text default ''")
	private String rdsvg;

	@Column(name = "mdcheck", columnDefinition = "int default 0")
	private Integer rdcheck;

	@Column(name = "rd_u_finally", columnDefinition = "varchar(30)")
	private String rdufinally;

	@Column(name = "rd_finally", columnDefinition = "default false")
	private Boolean rdfinally;

	@Column(name = "rd_u_qty")
	private Integer rduqty;

	@Column(name = "rd_ru_id")
	private Long rdruid;

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

	public String getRdid() {
		return rdid;
	}

	public void setRdid(String rdid) {
		this.rdid = rdid;
	}

	public RepairRegister getRegister() {
		return register;
	}

	public void setRegister(RepairRegister register) {
		this.register = register;
	}

	public RepairOrder getOrder() {
		return order;
	}

	public void setOrder(RepairOrder order) {
		this.order = order;
	}

	public String getRdstatement() {
		return rdstatement;
	}

	public void setRdstatement(String rdstatement) {
		this.rdstatement = rdstatement;
	}

	public String getRdtrue() {
		return rdtrue;
	}

	public void setRdtrue(String rdtrue) {
		this.rdtrue = rdtrue;
	}

	public String getRdexperience() {
		return rdexperience;
	}

	public void setRdexperience(String rdexperience) {
		this.rdexperience = rdexperience;
	}

	public String getRdsvg() {
		return rdsvg;
	}

	public void setRdsvg(String rdsvg) {
		this.rdsvg = rdsvg;
	}

	public Integer getRdcheck() {
		return rdcheck;
	}

	public void setRdcheck(Integer rdcheck) {
		this.rdcheck = rdcheck;
	}

	public String getRdufinally() {
		return rdufinally;
	}

	public void setRdufinally(String rdufinally) {
		this.rdufinally = rdufinally;
	}

	public Boolean getRdfinally() {
		return rdfinally;
	}

	public void setRdfinally(Boolean rdfinally) {
		this.rdfinally = rdfinally;
	}

	public Integer getRduqty() {
		return rduqty;
	}

	public void setRduqty(Integer rduqty) {
		this.rduqty = rduqty;
	}

	public Long getRdruid() {
		return rdruid;
	}

	public void setRdruid(Long rdruid) {
		this.rdruid = rdruid;
	}

}
