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
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---工作站項目---<br>
 *      pwp_id : 主key<br>
 *      pwp_g_id : 工作站程序(群組)<br>
 *      pwp_name : 工作程序名稱<br>
 *      pwp_c_name :2維代號(選擇工作流程)<br>   
 *      pwp_w_g_id : 工作站GID(群組)<br> 
 *      pwp_c_n_yield : 工作程序指定統計良率<br>
 * 
 * 
 **/
@Entity
@Table(name = "pcba_workstation_program")
@EntityListeners(AuditingEntityListener.class)
public class PcbaWorkstationProgram {

	public PcbaWorkstationProgram() {
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
	@Column(name = "pwp_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "pcba_workstation_program_seq")
	@SequenceGenerator(name = "pcba_workstation_program_seq", sequenceName = "pcba_workstation_program_seq", allocationSize = 1)
	private Long pwpid;

	@Column(name = "pwp_g_id", nullable = false)
	private Long pwpgid;

	@Column(name = "pwp_name", nullable = false, columnDefinition = "varchar(50)")
	private String pwpname;

	@Column(name = "pwp_w_g_id", nullable = false)
	private Long pwpwgid;

	@Column(name = "pwp_c_name", nullable = false, columnDefinition = "varchar(50)")
	private String pwpcname;
	
	@Column(name = "pwp_c_n_yield", columnDefinition = "varchar(50)")
	private String pwpcnyield;
	

	public Boolean getSysheader() {
		return sysheader;
	}

	public void setSysheader(Boolean sysgheader) {
		this.sysheader = sysgheader;
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

	public Long getPwpid() {
		return pwpid;
	}

	public void setPwpid(Long pwpid) {
		this.pwpid = pwpid;
	}

	public Long getPwpgid() {
		return pwpgid;
	}

	public void setPwpgid(Long pwpgid) {
		this.pwpgid = pwpgid;
	}

	public String getPwpname() {
		return pwpname;
	}

	public void setPwpname(String pwpname) {
		this.pwpname = pwpname;
	}

	public Long getPwpwgid() {
		return pwpwgid;
	}

	public void setPwpwgid(Long pwpwgid) {
		this.pwpwgid = pwpwgid;
	}

	public String getPwpcname() {
		return pwpcname;
	}

	public void setPwpcname(String pwpcname) {
		this.pwpcname = pwpcname;
	}

	public String getPwpcnyield() {
		return pwpcnyield;
	}

	public void setPwpcnyield(String pwpcnyield) {
		this.pwpcnyield = pwpcnyield;
	}


}
