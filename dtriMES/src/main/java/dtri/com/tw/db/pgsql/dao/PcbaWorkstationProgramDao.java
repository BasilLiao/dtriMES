package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.PcbaWorkstationProgram;

public interface PcbaWorkstationProgramDao extends JpaRepository<PcbaWorkstationProgram, Long> {

	// 查詢全部
	ArrayList<PcbaWorkstationProgram> findAll();

//	// 查詢全部 流程組+ 流程內的工作站
//	ArrayList<PcbaWorkstationProgram> findAllByWpgidAndWpwgid(Long Wpgid, Long Wpwgid);
//
	// 查詢全部 pwp_name or pwp_c_name
	ArrayList<PcbaWorkstationProgram> findAllByPwpcnameOrPwpname(String pwp_c_name, String pwp_name);
//
	// 查詢全部 pwp_c_name + 排除自己
	ArrayList<PcbaWorkstationProgram> findAllByPwpcnameAndPwpcnameNot(String pwp_c_name, String pwp_c_name2);

	// 查詢全部 wp_name + 排除自己
	ArrayList<PcbaWorkstationProgram> findAllByPwpnameAndPwpnameNot(String pwp_name, String pwp_name2);

	// 查詢全部 By Group
	ArrayList<PcbaWorkstationProgram> findAllByPwpgidOrderBySyssortAsc(Long pwp_g_id);
//
	// 查詢全部 By Group 排除代表
	ArrayList<PcbaWorkstationProgram> findAllByPwpgidAndSysheaderOrderBySyssortAsc(Long pwp_g_id, Boolean sysheader);
//
//	// 查詢全部 By Group + 特定工作站
//	ArrayList<PcbaWorkstationProgram> findAllByWpgidAndWpwgidAndSysheaderOrderBySyssortAsc(Long wp_g_id, Long wp_w_g_id, Boolean sysheader);
//
	// 查詢全部 By Group 代表
	ArrayList<PcbaWorkstationProgram> findAllBySysheader(Boolean sysheader);

	// 查詢全部 By pwp_w_g_id 關聯工作站
	ArrayList<PcbaWorkstationProgram> findAllByPwpwgid(Long Pwpwgid);

	// 查詢一部分
	@Query("SELECT c FROM PcbaWorkstationProgram c "//
			+ "WHERE (:pwpname is null or c.pwpname LIKE %:pwpname% ) and "//
			+ "(:pwpcname is null or c.pwpcname LIKE %:pwpcname% ) and "//
			+ "( c.sysstatus = :sysstatus ) and "//
			+ "( c.sysheader = :sysheader ) "//
			+ "order by c.pwpgid asc,c.sysheader desc,c.syssort asc")
	ArrayList<PcbaWorkstationProgram> findAllByProgram( //
			@Param("pwpname") String pwp_name, //
			@Param("pwpcname") String pwp_c_name, //
			@Param("sysstatus") Integer sysstatus, //
			@Param("sysheader") boolean sysheader, //
			Pageable pageable);

	// 查詢一部分-son
	@Query("SELECT c FROM PcbaWorkstationProgram c "//
			+ "WHERE (:pwpname is null or c.pwpname LIKE %:pwpname% ) and "//
			+ "(coalesce(:pwpgid, null) is null or c.pwpgid IN :pwpgid ) and "// coalesce 回傳非NULL值
			+ "(:pwpcname is null or c.pwpcname LIKE %:pwpcname% ) and "//
			+ "( c.sysheader = :sysheader ) "//
			+ "order by c.pwpgid asc,c.sysheader desc,c.syssort asc")
	ArrayList<PcbaWorkstationProgram> findAllByProgram( //
			@Param("pwpname") String pwp_name, //
			@Param("pwpcname") String pwp_c_name, //
			@Param("pwpgid") List<Long> pw_pg_id, //
			@Param("sysheader") boolean sysheader);

	// 取得G_ID PCBA_WORKSTATION_PROGRAM_G_SEQ
	@Query(value = "SELECT NEXTVAL('pcba_workstation_program_g_seq')", nativeQuery = true)
	Long getPcba_workstation_program_g_seq();

	// 取得ID
	@Query(value = "SELECT CURRVAL('pcba_workstation_program_seq')", nativeQuery = true)
	Long getPcbaworkstation_program_seq();

	// delete
	Long deleteByPwpidAndSysheader(Long id, Boolean sysheader);

	// delete group
	Long deleteByPwpgid(Long id);
}