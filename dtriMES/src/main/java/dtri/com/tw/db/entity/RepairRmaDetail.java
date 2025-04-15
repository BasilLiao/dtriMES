package dtri.com.tw.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see <br>
 *      
 *      rma_sn : 單據編號 ex:RAM0123456789<br> 
 *      rma_guest : 客戶  <br>
 *      rma_model: 產品型號  <br>
 *      rma_part_no : Part No <br>
 *      rma_b_sn: 機台 SN<br>     
 *      rma_mb_sn: MB SN<br>
 *      rma_statement:問題描述<br>   
 *      
 *      rd_true: 實際問題情況<br>  
 *      rd_solve: 維修處理事項<br>
 *      rd_experience: 備註 心得<br>
 *      rma_part_sn: 料號 <br>
 *      packing_list : 領料單據 <br>
 *      rd_result :維修結果 <br>    
 *      rma_user:修復人<br>
 *      rd_u_qty:單位 數量 "自動為1"<br>       
 *            
 *            
 *    
 *       
 *      rd_check:檢核狀態(0=已申請(尚未收到) 1=已檢核(收到) 2=已處理(完成修復) 3=轉處理 4=修不好(丟棄報廢)
 *      5=已寄回(結單)<br>
 *      rd_finally:true =已解決 /false =尚未解決<br>


 * 
 */
@Entity
@Table(name = "repair_rma_detail")
@EntityListeners(AuditingEntityListener.class)
public class RepairRmaDetail {
	public RepairRmaDetail() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;
		this.rduqty=1;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
        
	@Column(name = "rma_sn", nullable = false, columnDefinition = "varchar(20)")
	private String rmasn;

	@Column(name = "rma_guest", columnDefinition = "varchar(30) DEFAULT 'UNKNOWN'")
	private String rmaguest;

	@Column(name = "rma_model", columnDefinition = "varchar(30) DEFAULT 'UNKNOWN'")
	private String rmamodel;


	@Column(name = "rma_part_no", columnDefinition = "varchar(30) DEFAULT 'UNKNOWN' ")
	private String rmaPartNo;
	
	@Column(name = "rma_b_sn", columnDefinition = "varchar(20) DEFAULT 'UNKNOWN' ")
	private String rmabsn;
	
	@Column(name = "rma_mb_sn", columnDefinition = "varchar(20) DEFAULT 'UNKNOWN' ")
	private String rmambsn;	
	
	@Column(name = "rma_statement", columnDefinition = "varchar(500)")
	private String rmastatement;

	@Column(name = "rd_true", columnDefinition = "varchar(500)")
	private String rdtrue;
	
	@Column(name = "rd_solve", columnDefinition = "varchar(500)")
	private String rdsolve;

	@Column(name = "rd_experience", columnDefinition = "varchar(500)")
	private String rdexperience;
	
	//自行輸入MB料號
	@Column(name = "rma_part_sn", columnDefinition = "varchar(50)")
	private String rmapartsn;
	
	//自行輸入單據號碼
	@Column(name = "packing_list", columnDefinition = "varchar(50)")
	private String packinglist;
	
	// 維修結果
	@Column(name = "rma_result", columnDefinition = "varchar(10)")
	private String rmaresult;
	
	//維修人員
	@Column(name = "rma_user", columnDefinition = "varchar(20)")
	private String rmauser;
	

	//以下尚未使用
	@Column(name = "rd_u_qty",columnDefinition = "int default 0")
	private Integer rduqty;
	
	
	@Column(name = "rd_check", columnDefinition = "int default 0")
	private Integer rdcheck;



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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRmasn() {
		return rmasn;
	}

	public void setRmasn(String rmasn) {
		this.rmasn = rmasn;
	}

	public String getRmaguest() {
		return rmaguest;
	}

	public void setRmaguest(String rmaguest) {
		this.rmaguest = rmaguest;
	}

	public String getRmamodel() {
		return rmamodel;
	}

	public void setRmamodel(String rmamodel) {
		this.rmamodel = rmamodel;
	}

	
	public String getRmaPartNo() {
		return rmaPartNo;
	}

	public void setRmaPartNo(String rmaPartNo) {
		this.rmaPartNo = rmaPartNo;
	}

	public String getRmabsn() {
		return rmabsn;
	}

	public void setRmabsn(String rmabsn) {
		this.rmabsn = rmabsn;
	}

	public String getRmambsn() {
		return rmambsn;
	}

	public void setRmambsn(String rmambsn) {
		this.rmambsn = rmambsn;
	}

	public String getRmastatement() {
		return rmastatement;
	}

	public void setRmastatement(String rmastatement) {
		this.rmastatement = rmastatement;
	}

	public String getRdtrue() {
		return rdtrue;
	}

	public void setRdtrue(String rdtrue) {
		this.rdtrue = rdtrue;
	}

	public String getRdsolve() {
		return rdsolve;
	}

	public void setRdsolve(String rdsolve) {
		this.rdsolve = rdsolve;
	}

	public String getRdexperience() {
		return rdexperience;
	}

	public void setRdexperience(String rdexperience) {
		this.rdexperience = rdexperience;
	}

	public String getRmapartsn() {
		return rmapartsn;
	}

	public void setRmapartsn(String rmapartsn) {
		this.rmapartsn = rmapartsn;
	}
	

	public String getPackinglist() {
		return packinglist;
	}

	public void setPackinglist(String packinglist) {
		this.packinglist = packinglist;
	}

	public String getRmaresult() {
		return rmaresult;
	}

	public void setRmaresult(String rmaresult) {
		this.rmaresult = rmaresult;
	}

	public String getRmauser() {
		return rmauser;
	}

	public void setRmauser(String rmauser) {
		this.rmauser = rmauser;
	}

	public Integer getRduqty() {
		return rduqty;
	}

	public void setRduqty(Integer rduqty) {
		this.rduqty = rduqty;
	}

	public Integer getRdcheck() {
		return rdcheck;
	}

	public void setRdcheck(Integer rdcheck) {
		this.rdcheck = rdcheck;
	}

	
}
