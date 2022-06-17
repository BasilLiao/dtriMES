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
 * @see <br>
 *      mo_id : 產品維修單_ID<br>
 *      mo_g_sn : 單據序號_(群組)<br>
 *      mo_i_sn : 單據項目_(單位)RAM類型:R001-R999/DTR類型:D001-D999/Self類型:S001-S999<br>
 *      mo_mr_id : 單據項目 對應 維修料號ID<br>
 *      mo_c_id : 客戶( 單據 多對一 客戶)<br>
 *      mo_check : 檢核狀態(0=已申請(尚未收到) 1=已檢核(收到) 2=廢止 3=處理中 4=結單)<br>
 *      mo_from : 產線:DTR/場外維修:RAM/如果是DTR單 則每日自動建立<br>
 *      mo_e_date : 完成日<br>
 *      mo_s_date : 寄出日<br>
 *      mo_g_date : 收到日<br>
 *      mo_ram_date : 申請RAM日期<br>
 * 
 */
@Entity
@Table(name = "maintenance_order")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceOrder {
	public MaintenanceOrder() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maintenance_order_seq")
	@SequenceGenerator(name = "maintenance_order_seq", sequenceName = "maintenance_order_seq", allocationSize = 1)
	@Column(name = "mo_id")
	private Long moid;

	@Column(name = "mo_g_sn", nullable = false, columnDefinition = "varchar(50)")
	private String mogsn;

	@Column(name = "mo_i_sn", nullable = false, columnDefinition = "varchar(50)")
	private String moisn;

	@Column(name = "mo_mr_id", nullable = false)
	private Long momrid;

	@Column(name = "mo_c_id", nullable = false)
	private Long mocid;

	@Column(name = "mo_check", nullable = false)
	private Integer mocheck;

	@Column(name = "mo_from", nullable = false, columnDefinition = "varchar(50)")
	private String mofrom;

	@Column(name = "mo_e_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date moedate;

	@Column(name = "mo_g_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date mogdate;

	@Column(name = "mo_s_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date mosdate;

	@Column(name = "mo_ram_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date moramdate;

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

	public Long getMoid() {
		return moid;
	}

	public void setMoid(Long moid) {
		this.moid = moid;
	}

	public String getMogsn() {
		return mogsn;
	}

	public void setMogsn(String mogsn) {
		this.mogsn = mogsn;
	}

	public String getMoisn() {
		return moisn;
	}

	public void setMoisn(String moisn) {
		this.moisn = moisn;
	}

	public Long getMomrid() {
		return momrid;
	}

	public void setMomrid(Long momrid) {
		this.momrid = momrid;
	}

	public Long getMocid() {
		return mocid;
	}

	public void setMocid(Long mocid) {
		this.mocid = mocid;
	}

	public Integer getMocheck() {
		return mocheck;
	}

	public void setMocheck(Integer mocheck) {
		this.mocheck = mocheck;
	}

	public String getMofrom() {
		return mofrom;
	}

	public void setMofrom(String mofrom) {
		this.mofrom = mofrom;
	}

	public Date getMoedate() {
		return moedate;
	}

	public void setMoedate(Date moedate) {
		this.moedate = moedate;
	}

	public Date getMogdate() {
		return mogdate;
	}

	public void setMogdate(Date mogdate) {
		this.mogdate = mogdate;
	}

	public Date getMosdate() {
		return mosdate;
	}

	public void setMosdate(Date mosdate) {
		this.mosdate = mosdate;
	}

	public Date getMoramdate() {
		return moramdate;
	}

	public void setMoramdate(Date moramdate) {
		this.moramdate = moramdate;
	}
}
