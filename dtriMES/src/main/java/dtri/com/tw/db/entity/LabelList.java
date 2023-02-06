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
 * @see 系統設定<br>
 *      Ex:<br>
 *      ^XA --開始<br>
 * 
 *      ^LH25,15 --紙張上->標籤位置座標<br>
 *      ^LL100 --標籤長度<br>
 *      ^PW300 --標籤寬度<br>
 *      ^CI28 --文字編碼UTF8<br>
 *      ^MD100 --暗度<br>
 *      ^PR3 --速度<br>
 * 
 *      ^FO0,2 --標籤上->打印位置座標-開始<br>
 *      ^ADN,20 --字型<br>
 *      ^FB240,1,1,C,0 --段落設定<br>
 *      ^FDPART NO:AA\& --文字內容<br>
 *      ^FS --標籤上->打印位置-結束<br>
 * 
 *      ^FO25,22 --標籤上->打印位置座標-開始<br>
 *      ^BY1,2,30 --條碼長寬設定<br>
 *      ^B3N,N,25,N,N --條碼類型設定<br>
 *      ^FDQQQWWWDDERRR --條碼-文字內容<br>
 *      ^FS<br>
 * 
 *      ^FO100,100<br>
 *      ^BQN,2,10<br>
 *      ^FDLA,http://0123456789ABCD___2D code<br>
 *      ^FS<br>
 * 
 *      ^XZ --結束<br>
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "label_list")
@EntityListeners(AuditingEntityListener.class)
public class LabelList {
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

	// ===基本(設定)============================
	// 功能項目
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "label_list_seq")
	@SequenceGenerator(name = "label_list_seq", sequenceName = "label_list_seq", allocationSize = 1)
	@Column(name = "ll_id")
	private Long llid;

	@Column(name = "ll_name", nullable = false, columnDefinition = "varchar(50)")
	private String llname;// 標籤名稱

	@Column(name = "ll_g_name", nullable = false, columnDefinition = "varchar(50)")
	private String llgname;// 標籤群名稱

	@Column(name = "ll_xa", nullable = false, columnDefinition = "text default ''")
	private String llxa;// 固定-開頭:^XA ... 固定-結尾:^XZ

	// ===紙張與標籤機(設定)============================
	@Column(name = "ll_ci", nullable = false, columnDefinition = "varchar(50)")
	private String llci;// 固定-設定-編碼: ^CI28=UTF8
	@Column(name = "ll_ll", nullable = false, columnDefinition = "varchar(50)")
	private String llll;// 動態-設定-標籤紙張-長度(1cm=200點) ^LL100
	@Column(name = "ll_pw", nullable = false, columnDefinition = "varchar(50)")
	private String llpw;// 動態-設定-標籤紙張-寬度(1cm=200點) ^PW300
	@Column(name = "ll_lh", nullable = false, columnDefinition = "varchar(50)")
	private String lllh;// 動態-設定-[標籤紙張]上->[標籤]位置座標(x,y) ^LH25,15
	@Column(name = "ll_md", nullable = false, columnDefinition = "varchar(50)")
	private String llmd;// 動態-設定-暗度(+-30) ^MD30
	@Column(name = "ll_pr", nullable = false, columnDefinition = "varchar(50)")
	private String llpr;// 動態-設定-速度(1-7) ^PR

	// ===紙張換張(設定)============================
	@Column(name = "ll_o_p_type", nullable = false, columnDefinition = "varchar(5)")
	private String lloptype;// 多張 = 1 單張=0
	@Column(name = "ll_o_p_qty", columnDefinition = "int default 0")
	private Integer llopqty;// 一箱 多少台
	@Column(name = "ll_o_l_qty", columnDefinition = "int default 0")
	private Integer llolqty;// 一張 多少台
	@Column(name = "ll_o_h_b_name", columnDefinition = "varchar(255)")
	private String llohbname;// 第二 張開始隱藏標籤
	@Column(name = "ll_o_s_b_name", columnDefinition = "varchar(255)")
	private String llosbname;// 指定 要重複的標籤
	@Column(name = "ll_o_top", columnDefinition = "int default 0")
	private Integer llotop;// 指定 間格高度

	

	// ===存檔設定(設計)============================
	@Column(name = "ll_fo_s", nullable = false, columnDefinition = "text default ''")
	private String llfos;// 動態-設計-[標籤]上->[打印]位置座標(x,y) ^FO0,2 ... [打印]位置結尾:^FS

	@Column(name = "ll_a_json", nullable = false, columnDefinition = "text default ''")
	private String llajson;// 前端設定內容

	public LabelList() {
		// DB
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;
		// 標籤紙
		this.llxa = "^XA{ZPL打印內容}^XZ";
		this.llci = "^CI28";
		this.lllh = "^LH{x,y起始打印座標(點)}";
		this.llll = "^LL{標籤長度(點)}";
		this.llpw = "^PW{標籤寬度(點)}";
		this.llmd = "^MD{打印暗度}";
		this.llpr = "^PR{打印速度}";

		// 全區域位置(通用)[如果有跟隨機制 則{table.cell}]
		this.llfos = "^FO{x,y全部區域位置座標(點)}^FS";

		// UI存檔(通用)
		this.llajson = "{}";
	}

	public String getLlxa() {
		return llxa;
	}

	public void setLlxa(String llxa) {
		this.llxa = llxa;
	}

	public String getLlci() {
		return llci;
	}

	public void setLlci(String llci) {
		this.llci = llci;
	}

	public String getLlll() {
		return llll;
	}

	public void setLlll(String llll) {
		this.llll = llll;
	}

	public String getLlpw() {
		return llpw;
	}

	public void setLlpw(String llpw) {
		this.llpw = llpw;
	}

	public String getLllh() {
		return lllh;
	}

	public void setLllh(String lllh) {
		this.lllh = lllh;
	}

	public String getLlmd() {
		return llmd;
	}

	public void setLlmd(String llmd) {
		this.llmd = llmd;
	}

	public String getLlpr() {
		return llpr;
	}

	public void setLlpr(String llpr) {
		this.llpr = llpr;
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

	public Long getLlid() {
		return llid;
	}

	public void setLlid(Long llid) {
		this.llid = llid;
	}

	public String getLlname() {
		return llname;
	}

	public void setLlname(String llname) {
		this.llname = llname;
	}

	public String getLlgname() {
		return llgname;
	}

	public void setLlgname(String llgname) {
		this.llgname = llgname;
	}

	public String getLlfos() {
		return llfos;
	}

	public void setLlfos(String llfos) {
		this.llfos = llfos;
	}

	public String getLlajson() {
		return llajson;
	}

	public void setLlajson(String llajson) {
		this.llajson = llajson;
	}

	public String getLloptype() {
		return lloptype;
	}

	public void setLloptype(String lloptype) {
		this.lloptype = lloptype;
	}

	public Integer getLlopqty() {
		return llopqty;
	}

	public void setLlopqty(Integer llopqty) {
		this.llopqty = llopqty;
	}

	public Integer getLlolqty() {
		return llolqty;
	}

	public void setLlolqty(Integer llolqty) {
		this.llolqty = llolqty;
	}

	public String getLlohbname() {
		return llohbname;
	}

	public void setLlohbname(String llohbname) {
		this.llohbname = llohbname;
	}

	public String getLlosbname() {
		return llosbname;
	}

	public void setLlosbname(String llosbname) {
		this.llosbname = llosbname;
	}

	public Integer getLlotop() {
		return llotop;
	}

	public void setLlotop(Integer llotop) {
		this.llotop = llotop;
	}

}