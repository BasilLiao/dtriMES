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
 *      pdtime = 共使用多少時間(小時)<br>
 *      pdline = 生產線別<br>
 *      pdclass = 班別<br>
 *      pdworkernb = 工單 使用人數<br>
 *      pdwnames = 使用人名單<br>
 *      pdlsuid = 工作站管理者 ID<br>
 *      pdlname = 工作站 管理者名稱<br>
 *      pdwcname = 工作站code 條碼<br>
 *      pdwpbname = 工作站 迷稱<br>
 *      pdprid = 工單號碼<br>
 *      pdprpmodel = 產品型號<br>
 *      pdprpsn = 登記產品SN (json)<br>
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

	@Column(name = "pd_time", columnDefinition = "varchar(50)")
	private String pdtime;

	@Column(name = "pd_wc_line", nullable = false, columnDefinition = "varchar(50)")
	private String pdline;

	@Column(name = "pd_wc_class", nullable = false, columnDefinition = "varchar(50)")
	private String pdclass;

	@Column(name = "pd_worker_nb", columnDefinition = "int default 0")
	private Integer pdworkernb;

	@Column(name = "pd_w_names", nullable = false, columnDefinition = "varchar(250)")
	private String pdwnames;

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

	@Column(name = "pd_pr_p_sn", columnDefinition = "text default ''")
	private String pdprpsn;

}
