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
 * 
 * @see ---共用型---<br>
 * 
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      prs_id : 主key<br>      
 *      prs_g_id : 群組ID <br>
 *      prs_g_name : 序號組[名稱] <br>
 *      prs_c_name : 序號[代碼] <br>
 *      prs_name : 規則名稱 Ex:年月日/機種別/跟隨.... <br>
 *      prs_type : 規則定義 ex:10進制... 36進制或跟隨<br> 
 *      prs_length :規則碼數<br>   
 *      
 * 
 **/
@Entity
@Table(name = "pcba_rule_sn")
@EntityListeners(AuditingEntityListener.class)
public class PcbaRuleSn {

	public PcbaRuleSn() {
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

	@Column(name = "sys_status", columnDefinition = "int default 0")
	private Integer sysstatus;

	@Column(name = "sys_sort", columnDefinition = "int default 0")
	private Integer syssort;

	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;
	// 工作站
	@Id
	@Column(name = "prs_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "pcba_rule_sn_seq")
	@SequenceGenerator(name = "pcba_rule_sn_seq", sequenceName = "pcba_rule_sn_seq", allocationSize = 1)
	private Long prsid;	
	
	@Column(name = "prs_g_id", nullable = false) //群組ID (所有在這群組資料都會有的共同號碼
	private Long prsgid;
	
	@Column(name = "prs_g_name", nullable = false, columnDefinition = "varchar(50)") //序號組(群組)[名稱]
	private String prsgname;
	
	@Column(name = "prs_c_name", nullable = false, columnDefinition = "varchar(50)") //序號組(群組)[代碼]
	private String prscname;
	
	@Column(name = "prs_name", nullable = false, columnDefinition = "varchar(50)") //規則名稱 年月日/機種別/跟隨....
	private String prsname;
	
	@Column(name = "prs_type", nullable = false, columnDefinition = "varchar(50)") //規則定義 ex:10進制... 36進制或跟隨
	private String prstype;
	
	@Column(name = "prs_length", nullable = false, columnDefinition = "varchar(50)") //規則碼數
	private Long prslength;

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

	public Integer getSysstatus() {
		return sysstatus;
	}

	public void setSysstatus(Integer sysstatus) {
		this.sysstatus = sysstatus;
	}

	public Integer getSyssort() {
		return syssort;
	}

	public void setSyssort(Integer syssort) {
		this.syssort = syssort;
	}

	public Boolean getSysheader() {
		return sysheader;
	}

	public void setSysheader(Boolean sysheader) {
		this.sysheader = sysheader;
	}

	public Long getPrsid() {
		return prsid;
	}

	public void setPrsid(Long prsid) {
		this.prsid = prsid;
	}

	public Long getPrsgid() {
		return prsgid;
	}

	public void setPrsgid(Long prsgid) {
		this.prsgid = prsgid;
	}

	public String getPrsgname() {
		return prsgname;
	}

	public void setPrsgname(String prsgname) {
		this.prsgname = prsgname;
	}

	public String getPrscname() {
		return prscname;
	}

	public void setPrscname(String prscname) {
		this.prscname = prscname;
	}

	public String getPrsname() {
		return prsname;
	}

	public void setPrsname(String prsname) {
		this.prsname = prsname;
	}

	public String getPrstype() {
		return prstype;
	}

	public void setPrstype(String prstype) {
		this.prstype = prstype;
	}

	public Long getPrslength() {
		return prslength;
	}

	public void setPrslength(Long prslength) {
		this.prslength = prslength;
	}

}
