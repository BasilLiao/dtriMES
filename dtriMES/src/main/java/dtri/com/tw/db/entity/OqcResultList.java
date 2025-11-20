package dtri.com.tw.db.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
//import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Basil
 * @see OQC檢驗登記清單<br>
 *      orl_id : ID <br>
 *      orl_oif_id :配對檢驗表的Key <br>
 *      orl_ow : 工單號 <br>
 *      orl_o_nb : 訂單號碼 <br>
 *      orl_p_nb: 產品品號 <br>
 *      orl_n: 項次 <br>
 *      orl_p_sn: 產品SN號 <br>
 * 		orl_t_item: 測試項目 <br>
 * 		orl_t_results: 測試結果 [PASS / 暫停 / NG] <br>
 * 		orl_t_date: 檢驗日期 <br>
 * 		orl_t_user: 檢驗人 <br>
 */
@Entity
@Table(name = "oqc_result_list")
@EntityListeners(AuditingEntityListener.class)
public class OqcResultList implements Serializable {

	private static final long serialVersionUID = 1L;

	public OqcResultList() {
		this.sysheader = false;
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysnote = "";
	}

	// 共用型
	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;

	@Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date syscdate;

	@Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String syscuser;

	@Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysmdate;

	@Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysmuser;

	@Column(name = "sys_sort", columnDefinition = "int default 0")
	private Integer syssort;

	@Column(name = "sys_status", columnDefinition = "int default 0")
	private Integer sysstatus;
	
	@Column(name = "sys_note", columnDefinition = "text default ''")
	private String sysnote;

	// 功能項目
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oqc_result_list_seq")
	@SequenceGenerator(name = "oqc_result_list_seq", sequenceName = "oqc_result_list_seq", allocationSize = 1)
	@Column(name = "orl_id")
	private Long orlid;
	
	@Column(name = "orl_oif_id") //配對檢驗表的Key
	private Long orloifid;

	@Column(name = "orl_ow", nullable = false, columnDefinition = "varchar(50)") //工單號
	private String orlow;

//	@Column(name = "orl_o_nb", nullable = false, columnDefinition = "varchar(50)") //訂單號碼
//	private String orlonb;
	
	@Column(name = "orl_p_nb", nullable = false, columnDefinition = "varchar(50)") //產品料號
	private String orlpnb;
	
//	@Column(name = "orl_n", nullable = false, columnDefinition = "varchar(50)") //項次
//	private String orln;
	
	@Column(name = "orl_p_sn", nullable = false, columnDefinition = "varchar(50)") //產品SN號
	private String orlpsn;	
	
	@Column(name = "orl_t_item", nullable = false, columnDefinition = "varchar(50)") //測試項目
	private String orltitem;
	
	@Column(name = "orl_t_results", nullable = false, columnDefinition = "varchar(50)") //測試結果
	private String orltresults;
	
	@Column(name = "orl_t_date", nullable = false, columnDefinition = "TIMESTAMP default now()") // 檢驗日期
	private Date orltdate;
	
	@Column(name = "orl_t_user", nullable = false, columnDefinition = "varchar(50)") //檢驗人
	private String orltuser;

	public Boolean getSysheader() {
		return sysheader;
	}

	public void setSysheader(Boolean sysheader) {
		this.sysheader = sysheader;
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

	public String getSysnote() {
		return sysnote;
	}

	public void setSysnote(String sysnote) {
		this.sysnote = sysnote;
	}

	public Long getOrlid() {
		return orlid;
	}

	public void setOrlid(Long orlid) {
		this.orlid = orlid;
	}

	public Long getOrloifid() {
		return orloifid;
	}

	public void setOrloifid(Long orloifid) {
		this.orloifid = orloifid;
	}

	public String getOrlow() {
		return orlow;
	}

	public void setOrlow(String orlow) {
		this.orlow = orlow;
	}


	public String getOrlpnb() {
		return orlpnb;
	}

	public void setOrlpnb(String orlpnb) {
		this.orlpnb = orlpnb;
	}

	public String getOrlpsn() {
		return orlpsn;
	}

	public void setOrlpsn(String orlpsn) {
		this.orlpsn = orlpsn;
	}

	public String getOrltitem() {
		return orltitem;
	}

	public void setOrltitem(String orltitem) {
		this.orltitem = orltitem;
	}

	public String getOrltresults() {
		return orltresults;
	}

	public void setOrltresults(String orltresults) {
		this.orltresults = orltresults;
	}

	public Date getOrltdate() {
		return orltdate;
	}

	public void setOrltdate(Date orltdate) {
		this.orltdate = orltdate;
	}

	public String getOrltuser() {
		return orltuser;
	}

	public void setOrltuser(String orltuser) {
		this.orltuser = orltuser;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
