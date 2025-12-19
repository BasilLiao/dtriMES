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
 *      
 *      ---使用者帳戶---<br>
 *      su_id : 主key <br>
 *      su_name : 使用者名稱<br>
 *      su_e_name : 使用者英文名稱<br>
 *      su_position : 使用者[單位]名稱<br>
 *      su_email : 使用者E mail<br>
 *      su_received : 收到貨通知(是否) <br>
 *     	su_repairdone : 維修完成通知(是否) <br>
 *      su_dailyreport :測試日報通知(是否) <br>
 *      su_oqcdone : OQC結單通知(是否) <br>
 * 
 * @apiNote 標籤使用 @GeneratedValue<br>
 *          JPA提供的四種標準用法為TABLE，SEQUENCE，IDENTITY，AUTO。 <br>
 *          a，TABLE：使用一個特定的數據庫表格來保存主鍵。<br>
 *          b，SEQUENCE：根據底層數據庫的序列來生成主鍵，條件是數據庫支持序列。 <br>
 *          c，IDENTITY：主鍵由數據庫自動生成（主要是自動增長型）<br>
 *          d，AUTO：主鍵由程序控制。
 * 
 * @apiNote 標籤使用 @Column<br>
 *          varchar(50)<br>
 *          default ''<br>
 * 
 * @apiNote 標籤使用2<br>
 *          cascade CascadeType.PERSIST 在儲存時一併儲存 被參考的物件。 <br>
 *          CascadeType.MERGE 在合併修改時一併 合併修改被參考的物件。<br>
 *          CascadeType.REMOVE 在移除時一併移除 被參考的物件。 <br>
 *          CascadeType.REFRESH 在更新時一併更新 被參考的物件。<br>
 *          CascadeType.ALL 無論儲存、合併、 更新或移除，一併對被參考物件作出對應動作。<br>
 * 
 *          FetchType.LAZY時，
 *          除非真正要使用到該屬性的值，否則不會真正將資料從表格中載入物件，所以EntityManager後，才要載入該屬性值，就會發生例外錯誤，解決的方式
 *          之一是在EntityManager關閉前取得資料，另一個方式則是標示為FetchType.EARGE， 表示立即從表格取得資料
 * 
 * @Basic FetchType.EARGE <br>
 * @OneToOne FetchType.EARGE<br>
 * @ManyToOne FetchType.EARGE<br>
 * @OneToMany FetchType.LAZY<br>
 * @ManyToMany FetchType.LAZY<br>
 * 
 * 
 *             joinColumns：中間表的外來鍵欄位關聯當前實體類所對應表的主鍵欄位
 *             inverseJoinColumn：中間表的外來鍵欄位關聯對方表的主鍵欄位
 **/

@Entity
@Table(name = "system_mail")
@EntityListeners(AuditingEntityListener.class)
public class SystemMail {

	public SystemMail() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysgheader = false;
		this.sureceived="N";
		this.surepairdone="N";
		this.sudailyreport="N";
		this.suoqcdone="N";
	}

	// 共用型
	@Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()") //修改時間
	private Date syscdate;

	@Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'") //建立者
	private String syscuser;

	@Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()") //修改時間
	private Date sysmdate;

	@Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'") //修改者
	private String sysmuser;

	@Column(name = "sys_ver", columnDefinition = "int default 0")
	private Integer sysver;

	@Column(name = "sys_note", columnDefinition = "text default ''")
	private String sysnote;

	@Column(name = "sys_status", columnDefinition = "int default 0")
	private Integer sysstatus;

	@Column(name = "sys_sort", columnDefinition = "int default 0")
	private Integer syssort;

	@Column(name = "sys_g_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysgheader;

	// 主體型
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_mail_seq")
	@SequenceGenerator(name = "system_mail_seq", sequenceName = "system_mail_seq", allocationSize = 1)
	@Column(name = "su_id")
	private Long suid;

	//@Column(name = "su_sg_g_id")
	//private Long susggid;

	@Column(name = "su_name", nullable = false, columnDefinition = "varchar(50)")
	private String suname;

	@Column(name = "su_e_name", columnDefinition = "varchar(50)")
	private String suename;

	@Column(name = "su_position", columnDefinition = "varchar(50)")
	private String suposition;

	@Column(name = "su_email", columnDefinition = "varchar(200) default ''")
	private String suemail;
	
	@Column(name = "su_received", columnDefinition = "varchar(10)") //收到通知
	private String sureceived;
	
	@Column(name = "su_repairdone", columnDefinition = "varchar(10)")//完成通知
	private String surepairdone;
	
	@Column(name = "su_dailyreport", columnDefinition = "varchar(10)")//測試日報通知
	private String sudailyreport;	
	
	@Column(name = "su_oqcdone", columnDefinition = "varchar(10)")//oqc結單通知
	private String suoqcdone;	
	
	
	public Boolean getSysgheader() {
		return sysgheader;
	}

	public void setSysgheader(Boolean sysgheader) {
		this.sysgheader = sysgheader;
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

	public Long getSuid() {
		return suid;
	}

	public void setSuid(Long suid) {
		this.suid = suid;
	}

	public String getSuname() {
		return suname;
	}

	public void setSuname(String suname) {
		this.suname = suname;
	}

	public String getSuename() {
		return suename;
	}

	public void setSuename(String suename) {
		this.suename = suename;
	}

	public String getSuposition() {
		return suposition;
	}

	public void setSuposition(String suposition) {
		this.suposition = suposition;
	}

	public String getSuemail() {
		return suemail;
	}

	public void setSuemail(String suemail) {
		this.suemail = suemail;
	}

	public String getSureceived() {
		return sureceived;
	}

	public void setSureceived(String sureceived) {
		this.sureceived = sureceived;
	}

	public String getSurepairdone() {
		return surepairdone;
	}

	public void setSurepairdone(String surepairdone) {
		this.surepairdone = surepairdone;
	}

	public String getSudailyreport() {
		return sudailyreport;
	}

	public void setSudailyreport(String sudailyreport) {
		this.sudailyreport = sudailyreport;
	}

	public String getSuoqcdone() {
		return suoqcdone;
	}

	public void setSuoqcdone(String suoqcdone) {
		this.suoqcdone = suoqcdone;
	}

	
	
}
