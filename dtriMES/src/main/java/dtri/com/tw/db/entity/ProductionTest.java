package dtri.com.tw.db.entity;

import java.io.Serializable;
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
 * @see 產品製程-測試<br>
 *      pt_id : Key<br>
 *      pt_pb_b_sn : 燒錄序號<br>
 *      pt_pr_id : 工單號<br>
 *      pt_pr_bom_id : 產品BOM<br>
 *      pt_pr_b_item : 規範定義<br>
 *      pt_pr_s_item : 軟體定義<br>
 *      pt_l_size : PLT_Log 大小 <br>
 *      pt_l_dt : PLT_Log 時間 <br>
 *      pt_l_text : PLT_Log 內容資訊 <br>
 *      pt_l_path : PLT_Log 位置資訊 <br>
 * 
 */
@Entity
@Table(name = "production_test")
@EntityListeners(AuditingEntityListener.class)
public class ProductionTest implements Serializable {

	private static final long serialVersionUID = 1L;

	public ProductionTest() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_test_seq")
	@SequenceGenerator(name = "production_test_seq", sequenceName = "production_test_seq", allocationSize = 1)
	@Column(name = "pt_id")
	private Long ptid;
	
	@Column(name = "pt_pb_g_id")
	private Long ptpbgid;

	@Column(name = "pt_pb_b_sn", nullable = false, columnDefinition = "varchar(50)")
	private String ptpbbsn;

	@Column(name = "pt_pr_id", columnDefinition = "varchar(50)")
	private String ptprid;

	@Column(name = "pt_pr_model", columnDefinition = "varchar(50)")
	private String ptprmodel;

	@Column(name = "pt_pr_bom_id", columnDefinition = "varchar(50)")
	private String ptprbomid;

	@Column(name = "pt_pr_b_item", columnDefinition = "text default ''")
	private String ptprbitem;

	@Column(name = "pt_pr_s_item", columnDefinition = "text default ''")
	private String ptprsitem;

	@Column(name = "pt_l_text", columnDefinition = "text default ''")
	private String ptltext;

	@Column(name = "pt_l_path", columnDefinition = "varchar(255) default ''")
	private String ptlpath;

	@Column(name = "pt_l_size", columnDefinition = "varchar(50) default ''")
	private String ptlsize;

	@Column(name = "pt_l_dt", columnDefinition = "TIMESTAMP ")
	private Date ptldt;

	public String getPtltext() {
		return ptltext;
	}

	public void setPtltext(String ptltext) {
		this.ptltext = ptltext;
	}

	public String getPtlpath() {
		return ptlpath;
	}

	public void setPtlpath(String ptlpath) {
		this.ptlpath = ptlpath;
	}

	public String getPtlsize() {
		return ptlsize;
	}

	public void setPtlsize(String ptlsize) {
		this.ptlsize = ptlsize;
	}

	public Date getPtldt() {
		return ptldt;
	}

	public void setPtldt(Date ptldt) {
		this.ptldt = ptldt;
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

	public Long getPtid() {
		return ptid;
	}

	public void setPtid(Long ptid) {
		this.ptid = ptid;
	}

	public String getPtpbbsn() {
		return ptpbbsn;
	}

	public void setPtpbbsn(String ptpbbsn) {
		this.ptpbbsn = ptpbbsn;
	}

	public String getPtprid() {
		return ptprid;
	}

	public void setPtprid(String ptprid) {
		this.ptprid = ptprid;
	}

	public String getPtprmodel() {
		return ptprmodel;
	}

	public void setPtprmodel(String ptprmodel) {
		this.ptprmodel = ptprmodel;
	}

	public String getPtprbomid() {
		return ptprbomid;
	}

	public void setPtprbomid(String ptprbomid) {
		this.ptprbomid = ptprbomid;
	}

	public String getPtprbitem() {
		return ptprbitem;
	}

	public void setPtprbitem(String ptprbitem) {
		this.ptprbitem = ptprbitem;
	}

	public String getPtprsitem() {
		return ptprsitem;
	}

	public void setPtprsitem(String ptprsitem) {
		this.ptprsitem = ptprsitem;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getPtpbgid() {
		return ptpbgid;
	}

	public void setPtpbgid(Long ptpbgid) {
		this.ptpbgid = ptpbgid;
	}
}
