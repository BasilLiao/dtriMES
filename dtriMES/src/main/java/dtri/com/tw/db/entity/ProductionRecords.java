package dtri.com.tw.db.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see 系統設定<br>
 *      pr_id : 工單序號ID<br>
 *      pr_p_v : 產品版本<br>
 *      pr_p_model : 產品型號<br>
 *      pr_bom_id : BOM料號<br>
 *      pr_bom_c_id : 客戶BOM料號<br>
 *      pr_b_item : 規格定義{"名稱1":"內容1","名稱2":"內容2"}<br>
 *      pr_s_item : 軟體定義{"名稱1":"內容1","名稱2":"內容2"}<br>
 *      pr_s_sn : 產品序號 開始 EX:xxxxxx 01YW12042J044-<br>
 *      pr_e_sn : 產品序號 結束 EX: xxxxxx 01YW12042J050<br>
 *      pr_s_b_sn : 燒錄序號 開始 EX:xxxxxx 01YW12042J044-<br>
 *      pr_e_b_sn : 燒錄序號 結束 EX:xxxxxx 01YW12042J044-<br>
 * 
 */
@Entity
@Table(name = "production_records")
@EntityListeners(AuditingEntityListener.class)
public class ProductionRecords {
	public ProductionRecords() {
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
	// 因為是文字 故無用 自動累加
	@Column(name = "pr_id")
	private String prid;

	@Column(name = "pr_p_v", columnDefinition = "varchar(20) default ''")
	private String prpv;

	@Column(name = "pr_p_model", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prpmodel;

	@Column(name = "pr_bom_id", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prbomid;

	@Column(name = "pr_bom_c_id", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prbomcid;

	@Column(name = "pr_b_item", nullable = false, columnDefinition = "text default ''")
	private String prbitem;

	@Column(name = "pr_s_item", nullable = false, columnDefinition = "text default ''")
	private String prsitem;

	@Column(name = "pr_s_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prssn;

	@Column(name = "pr_e_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String presn;

	@Column(name = "pr_s_b_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prsbsn;

	@Column(name = "pr_e_b_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String prebsn;

	@OneToOne(mappedBy = "productionRecords")
	private ProductionHeader header;

	@OneToMany(mappedBy = "productionRecords")
	private List<WorkHours> workHours;

	public String getPrsbsn() {
		return prsbsn;
	}

	public void setPrsbsn(String prsbsn) {
		this.prsbsn = prsbsn;
	}

	public String getPrebsn() {
		return prebsn;
	}

	public void setPrebsn(String prebsn) {
		this.prebsn = prebsn;
	}

	public ProductionHeader getHeader() {
		return header;
	}

	public void setHeader(ProductionHeader header) {
		this.header = header;
	}

	public List<WorkHours> getWorkHours() {
		return workHours;
	}

	public void setWorkHours(List<WorkHours> workHours) {
		this.workHours = workHours;
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

	public String getPrid() {
		return prid;
	}

	public void setPrid(String prid) {
		this.prid = prid;
	}

	public String getPrpmodel() {
		return prpmodel;
	}

	public void setPrpmodel(String prpmodel) {
		this.prpmodel = prpmodel;
	}

	public String getPrbomid() {
		return prbomid;
	}

	public void setPrbomid(String prbomid) {
		this.prbomid = prbomid;
	}

	public String getPrbitem() {
		return prbitem;
	}

	public void setPrbitem(String prbitem) {
		this.prbitem = prbitem;
	}

	public String getPrsitem() {
		return prsitem;
	}

	public void setPrsitem(String prsitem) {
		this.prsitem = prsitem;
	}

	public String getPrssn() {
		return prssn;
	}

	public void setPrssn(String prssn) {
		this.prssn = prssn;
	}

	public String getPresn() {
		return presn;
	}

	public void setPresn(String presn) {
		this.presn = presn;
	}

	public String getPrbomcid() {
		return prbomcid;
	}

	public void setPrbomcid(String prbomcid) {
		this.prbomcid = prbomcid;
	}

	public String getPrpv() {
		return prpv;
	}

	public void setPrpv(String prpv) {
		this.prpv = prpv;
	}

}
