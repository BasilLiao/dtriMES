package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * 
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---工作站項目---<br>
 *      pw_id : 主key<br>
 *      pw_g_id : 群組<br>
 *      pw_i_id : 項目ID<br>
 *      pw_c_name : 登記工作站條碼用<br>
 *      pw_pb_name : 工作站名稱(來自於pb_w_name)<br>
 *      pw_pb_cell : 工作站名稱(來自於pb 欄位名稱)<br>
 *      pw_sg_id : 可使用此工作站群組[ID]<br>
 *      pw_sg_name : 可使用此工作站群組[名稱]<br>
 *      pw_replace : 是否可重複刷入資料<br>
 *      pw_option :顯示選項<br>
 *      pw_only : 唯一值<br>
 *      pw_length : 長度<br>
 *      pw_format : 格式<br>
 *      pw_must : 必填<br>
 *      pw_pi_check: 產品規格檢查的 設定<br>
 *      pw_pi_name: 產品規格的 名稱<br>
 * 
 * 
 * 
 **/
@Entity
@Table(name = "pcba_workstation")
@EntityListeners(AuditingEntityListener.class)
public class PcbaWorkstation {

	public PcbaWorkstation() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;
		this.pwpicheck = 0;
		this.pwpiname = "";

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
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "pcba_workstation_seq")
	@SequenceGenerator(name = "pcba_workstation_seq", sequenceName = "pcba_workstation_seq", allocationSize = 1)
	@Column(name = "pw_id")
	private Long pwid;

	@Column(name = "pw_g_id", nullable = false)
	private Long pwgid;

	@ManyToOne(targetEntity = PcbaWorkstationItem.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "pw_i_id")
	private PcbaWorkstationItem pcbaWorkstationItem;

	@Column(name = "pw_c_name", nullable = false, columnDefinition = "varchar(50)") //登記工作站條碼用
	private String pwcname;

	@Column(name = "pw_pb_name", nullable = false, columnDefinition = "varchar(50)")
	private String pwpbname;

	@Column(name = "pw_pb_cell", nullable = false, columnDefinition = "varchar(50)")
	private String pwpbcell;

	@Column(name = "pw_sg_id", columnDefinition = "int default 0")
	private Long pwsgid;

	@Column(name = "pw_sg_name", columnDefinition = "varchar(50) default ''")
	private String pwsgname;

	@Column(name = "pw_replace", columnDefinition = "boolean default true")
	private Boolean pwreplace;

	@Column(name = "pw_option", columnDefinition = "int default 0")
	private Integer pwoption;

	@Column(name = "pw_only", columnDefinition = "int default 0")
	private Integer pwonly;

	@Column(name = "pw_length", columnDefinition = "int default 0")
	private Integer pwlength;

	@Column(name = "pw_format", columnDefinition = "int default 0")
	private Integer pwformat;

	@Column(name = "pw_must", columnDefinition = "int default 0")
	private Integer pwmust;

	@Column(name = "pw_pi_check", columnDefinition = "int default 0")
	private Integer pwpicheck;

	@Column(name = "pw_pi_name", columnDefinition = "varchar(50) default ''")
	private String pwpiname;

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

	public Long getPwid() {
		return pwid;
	}

	public void setPwid(Long pwid) {
		this.pwid = pwid;
	}

	public Long getPwgid() {
		return pwgid;
	}

	public void setPwgid(Long pwgid) {
		this.pwgid = pwgid;
	}

	public PcbaWorkstationItem getPcbaWorkstationItem() {
		return pcbaWorkstationItem;
	}

	public void setPcbaWorkstationItem(PcbaWorkstationItem pcbaWorkstationItem) {
		this.pcbaWorkstationItem = pcbaWorkstationItem;
	}

	public String getPwcname() {
		return pwcname;
	}

	public void setPwcname(String pwcname) {
		this.pwcname = pwcname;
	}

	public String getPwpbname() {
		return pwpbname;
	}

	public void setPwpbname(String pwpbname) {
		this.pwpbname = pwpbname;
	}

	public String getPwpbcell() {
		return pwpbcell;
	}

	public void setPwpbcell(String pwpbcell) {
		this.pwpbcell = pwpbcell;
	}

	public Long getPwsgid() {
		return pwsgid;
	}

	public void setPwsgid(Long pwsgid) {
		this.pwsgid = pwsgid;
	}

	public String getPwsgname() {
		return pwsgname;
	}

	public void setPwsgname(String pwsgname) {
		this.pwsgname = pwsgname;
	}

	public Boolean getPwreplace() {
		return pwreplace;
	}

	public void setPwreplace(Boolean pwreplace) {
		this.pwreplace = pwreplace;
	}

	public Integer getPwoption() {
		return pwoption;
	}

	public void setPwoption(Integer pwoption) {
		this.pwoption = pwoption;
	}

	public Integer getPwonly() {
		return pwonly;
	}

	public void setPwonly(Integer pwonly) {
		this.pwonly = pwonly;
	}

	public Integer getPwlength() {
		return pwlength;
	}

	public void setPwlength(Integer pwlength) {
		this.pwlength = pwlength;
	}

	public Integer getPwformat() {
		return pwformat;
	}

	public void setPwformat(Integer pwformat) {
		this.pwformat = pwformat;
	}

	public Integer getPwmust() {
		return pwmust;
	}

	public void setPwmust(Integer pwmust) {
		this.pwmust = pwmust;
	}

	public Integer getPwpicheck() {
		return pwpicheck;
	}

	public void setPwpicheck(Integer pwpicheck) {
		this.pwpicheck = pwpicheck;
	}

	public String getPwpiname() {
		return pwpiname;
	}

	public void setPwpiname(String pwpiname) {
		this.pwpiname = pwpiname;
	}


}
