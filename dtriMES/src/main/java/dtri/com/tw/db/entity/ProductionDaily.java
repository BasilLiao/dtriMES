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
 * @see 每日生產進度<br>
 *      pdid<br>
 *      pdstime = 工單 開始時間<br>
 *      pdetime = 工單 結束時間<br>
 * 
 *      pdttime = 當天 總共使用多少時間(小時)<br>
 *      pdtqty =當天 總共數量 pdtsu = 當天 使用人數<br>
 * 
 *      pdwcline = 生產線別<br>
 *      pdwcclass = 班別<br>
 *      pdwnames = 使用人名單<br>
 *      pdwaccounts = 使用人帳號單<br>
 *      pdlsuid = 工作站管理者 ID<br>
 *      pdlname = 工作站 管理者名稱<br>
 *      pdwcname = 工作站code 條碼<br>
 *      pdwpbname = 工作站 迷稱<br>
 *      pdprid = 工單號碼<br>
 *      pdprpmodel = 產品型號<br>
 *      pdprbomid = 產品BOM<br>
 *      pdpgs = 進度<br>
 *      pdprpbsn = 登記產品SN (json)<br>
 */
@Entity
@Table(name = "production_daily")
@EntityListeners(AuditingEntityListener.class)
public class ProductionDaily {
	public ProductionDaily() {
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_daily_seq")
	@SequenceGenerator(name = "production_daily_seq", sequenceName = "production_daily_seq", allocationSize = 1)
	@Column(name = "pd_id")
	private Long pdid;

	@Column(name = "pd_s_time", columnDefinition = "TIMESTAMP")
	private Date pdstime;

	@Column(name = "pd_e_time", columnDefinition = "TIMESTAMP")
	private Date pdetime;

	@Column(name = "pd_t_time", columnDefinition = "varchar(50)")
	private String pdttime;

	@Column(name = "pd_t_su", columnDefinition = "int default 0")
	private Integer pdtsu;

	@Column(name = "pd_t_qty", columnDefinition = "int default 0")
	private Integer pdtqty;

	@Column(name = "pd_wc_line", nullable = false, columnDefinition = "varchar(50)")
	private String pdwcline;

	@Column(name = "pd_wc_class", nullable = false, columnDefinition = "varchar(50)")
	private String pdwcclass;

	@Column(name = "pd_w_names", nullable = false, columnDefinition = "varchar(250)")
	private String pdwnames;

	@Column(name = "pd_w_accounts", nullable = false, columnDefinition = "varchar(250)")
	private String pdwaccounts;

	@Column(name = "pd_l_su_id", nullable = false)
	private Long pdlsuid;

	@Column(name = "pd_l_name", nullable = false, columnDefinition = "varchar(50)")
	private String pdlname;

	@Column(name = "pd_w_c_name", nullable = false, columnDefinition = "varchar(50)")
	private String pdwcname;

	@Column(name = "pd_w_pb_name", nullable = false, columnDefinition = "varchar(50)")
	private String pdwpbname;

	@Column(name = "pd_pr_id", nullable = false, columnDefinition = "varchar(50)")
	private String pdprid;

	@Column(name = "pd_pr_p_model", nullable = false, columnDefinition = "varchar(50)")
	private String pdprpmodel;

	@Column(name = "pd_pr_bom_id", nullable = false, columnDefinition = "varchar(50)")
	private String pdprbomid;

	@Column(name = "pd_pr_p_b_sn", columnDefinition = "text default ''")
	private String pdprpbsn;

	@Column(name = "pd_pr_total", columnDefinition = "int default 0")
	private Integer pdprtotal;

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

	public Long getPdid() {
		return pdid;
	}

	public void setPdid(Long pdid) {
		this.pdid = pdid;
	}

	public Date getPdstime() {
		return pdstime;
	}

	public void setPdstime(Date pdstime) {
		this.pdstime = pdstime;
	}

	public Date getPdetime() {
		return pdetime;
	}

	public void setPdetime(Date pdetime) {
		this.pdetime = pdetime;
	}

	public String getPdwcline() {
		return pdwcline;
	}

	public void setPdwcline(String pdwcline) {
		this.pdwcline = pdwcline;
	}

	public String getPdwcclass() {
		return pdwcclass;
	}

	public void setPdwcclass(String pdwcclass) {
		this.pdwcclass = pdwcclass;
	}

	public String getPdwnames() {
		return pdwnames;
	}

	public void setPdwnames(String pdwnames) {
		this.pdwnames = pdwnames;
	}

	public String getPdwaccounts() {
		return pdwaccounts;
	}

	public void setPdwaccounts(String pdwaccounts) {
		this.pdwaccounts = pdwaccounts;
	}

	public Integer getPdprtotal() {
		return pdprtotal;
	}

	public void setPdprtotal(Integer pdprtotal) {
		this.pdprtotal = pdprtotal;
	}

	public Long getPdlsuid() {
		return pdlsuid;
	}

	public void setPdlsuid(Long pdlsuid) {
		this.pdlsuid = pdlsuid;
	}

	public String getPdlname() {
		return pdlname;
	}

	public void setPdlname(String pdlname) {
		this.pdlname = pdlname;
	}

	public String getPdwcname() {
		return pdwcname;
	}

	public void setPdwcname(String pdwcname) {
		this.pdwcname = pdwcname;
	}

	public String getPdwpbname() {
		return pdwpbname;
	}

	public void setPdwpbname(String pdwpbname) {
		this.pdwpbname = pdwpbname;
	}

	public String getPdprid() {
		return pdprid;
	}

	public void setPdprid(String pdprid) {
		this.pdprid = pdprid;
	}

	public String getPdprpmodel() {
		return pdprpmodel;
	}

	public void setPdprpmodel(String pdprpmodel) {
		this.pdprpmodel = pdprpmodel;
	}

	public String getPdprbomid() {
		return pdprbomid;
	}

	public void setPdprbomid(String pdprbomid) {
		this.pdprbomid = pdprbomid;
	}

	public String getPdprpbsn() {
		return pdprpbsn;
	}

	public void setPdprpbsn(String pdprpbsn) {
		this.pdprpbsn = pdprpbsn;
	}

	public String getPdttime() {
		return pdttime;
	}

	public void setPdttime(String pdttime) {
		this.pdttime = pdttime;
	}

	public Integer getPdtsu() {
		return pdtsu;
	}

	public void setPdtsu(Integer pdtsu) {
		this.pdtsu = pdtsu;
	}

	public Integer getPdtqty() {
		return pdtqty;
	}

	public void setPdtqty(Integer pdtqty) {
		this.pdtqty = pdtqty;
	}

}
