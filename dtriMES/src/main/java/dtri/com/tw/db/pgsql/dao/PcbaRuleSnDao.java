package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.PcbaRuleSn;
//import dtri.com.tw.db.entity.PcbaWorkstationProgram;

public interface PcbaRuleSnDao extends JpaRepository<PcbaRuleSn, Long> {

	// 查詢全部
	ArrayList<PcbaRuleSn> findAll();
	
	//j查詢id
	ArrayList<PcbaRuleSn> findByPrsid(Long prs_id); 
	
	//j查詢
	//檢查 序號組[名稱]是否重複，但排除自己ID
	ArrayList<PcbaRuleSn> findAllByPrsgnameAndPrsidNotAndSysheader(String prs_g_name, Long prs_id,Boolean sysheader);
	//檢查 序號[代碼]是否重複，但排除自己ID
	ArrayList<PcbaRuleSn> findAllByPrscnameAndPrsidNotAndSysheader(String prs_c_name, Long prs_id,Boolean sysheader);

//	// 查詢全部 流程組+ 流程內的工作站
//	ArrayList<PcbaWorkstationProgram> findAllByWpgidAndWpwgid(Long Wpgid, Long Wpwgid);

	// 查詢全部 prs_g_name or prs_c_name
	ArrayList<PcbaRuleSn> findAllByPrsgnameOrPrscname(String prs_g_name, String prs_c_name);

	// 查詢全部 prs_g_name + 排除自己
	ArrayList<PcbaRuleSn> findAllByPrsgnameAndPrsgnameNot(String prs_g_name, String prs_g_name2);

	// 查詢全部 prs_c_name + 排除自己
//	ArrayList<PcbaRuleSn> findAllByPrscnameAndPrscnameNot(String prs_c_name, String prs_c_name2);

	// 查詢全部 By Group 
	ArrayList<PcbaRuleSn> findAllByPrsgidOrderBySyssortAsc(Long prs_g_id);

	// 查詢全部 By Group 排除代表 如果為子目錄 非群組
	ArrayList<PcbaRuleSn> findAllByPrsgidAndSysheaderOrderBySyssortAsc(Long prs_g_id, Boolean sysheader);
//
//	// 查詢全部 By Group + 特定工作站
//	ArrayList<PcbaWorkstationProgram> findAllByWpgidAndWpwgidAndSysheaderOrderBySyssortAsc(Long wp_g_id, Long wp_w_g_id, Boolean sysheader);
//
//	// 查詢全部 By Group 代表
//	ArrayList<PcbaWorkstationProgram> findAllBySysheader(Boolean sysheader);
//
//	// 查詢全部 By pwp_w_g_id 關聯工作站
//	ArrayList<PcbaWorkstationProgram> findAllByPwpwgid(Long Pwpwgid);
//
	// 查詢一部分
	@Query("SELECT c FROM PcbaRuleSn c "//
			+ "WHERE (:prsgname is null or c.prsgname LIKE %:prsgname% ) and "//
			+ "(:prscname is null or c.prscname LIKE %:prscname% ) and "//
			+ "( c.sysstatus = :sysstatus ) and "//
			+ "( c.sysheader = :sysheader ) "//
			+ "order by c.prsgid asc,c.sysheader desc,c.syssort asc")
	ArrayList<PcbaRuleSn> findAllByProgram( //
			@Param("prsgname") String prs_g_name, //
			@Param("prscname") String prs_c_name, //
			@Param("sysstatus") Integer sysstatus, //
			@Param("sysheader") boolean sysheader, //
			Pageable pageable);
//
	// 查詢一部分-son
	@Query("SELECT c FROM PcbaRuleSn c "//
			+ "WHERE (:prsgname is null or c.prsgname LIKE %:prsgname% ) and "//
			+ "(coalesce(:prsgid, null) is null or c.prsgid IN :prsgid ) and "// coalesce 回傳非NULL值
			+ "(:prscname is null or c.prscname LIKE %:prscname% ) and "//
			+ "( c.sysheader = :sysheader ) "//
			+ "order by c.prsgid asc,c.sysheader desc,c.syssort asc")
	ArrayList<PcbaRuleSn> findAllByProgram( //
			@Param("prsgname") String prs_g_name, //
			@Param("prscname") String prs_c_name, //
			@Param("prsgid") List<Long> prs_g_id, //
			@Param("sysheader") boolean sysheader);

	// 取得G_ID pcba_rule_sn_g_seq  父
	@Query(value = "SELECT NEXTVAL('pcba_rule_sn_g_seq')", nativeQuery = true)
	Long getPcba_rule_sn_g_seq();
//
//	// 取得ID
//	@Query(value = "SELECT CURRVAL('pcba_workstation_program_seq')", nativeQuery = true)
//	Long getPcbaworkstation_program_seq();

	// delete
	Long deleteByPrsidAndSysheader(Long id, Boolean sysheader);

	Long deleteByPrsgid(Long prs_g_id);
}