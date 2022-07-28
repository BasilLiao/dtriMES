package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.RepairCode;

public interface RepairCodeDao extends JpaRepository<RepairCode, Long> {

	// 查詢全部
	ArrayList<RepairCode> findAll();

	// 查詢全部+排序
	ArrayList<RepairCode> findAllByOrderByRcgidAscSysheaderDescRcvalueAsc();

	// 查詢(不含/只含)群組代表 and 限制
	@Query("SELECT c FROM RepairCode c " //
			+ "WHERE "//
			+ "(:rcgname is null or c.rcgname LIKE %:rcgname% ) and " //
			+ "( c.sysheader = :sysheader )  " //
			+ "order by c.rcgid asc,c.sysheader desc,c.rcvalue asc")
	ArrayList<RepairCode> findAllBySysheaderOrderByRcgidAsc(//
			boolean sysheader, String rcgname, Pageable pageable);

	// 查詢一部分
	@Query("SELECT c FROM RepairCode c " //
			+ "WHERE (:rcvalue is null or c.rcvalue LIKE %:rcvalue% ) and "//
			+ "(coalesce(:rcgid, null) is null or c.rcgid IN :rcgid ) and "// coalesce 回傳非NULL值
			+ "( c.sysheader = :sysheader ) and " //
			+ "( c.sysstatus = :sysstatus ) " //
			+ "order by c.rcgid asc,c.sysheader desc,c.rcvalue asc")
	ArrayList<RepairCode> findAllByRepairCode(String rcvalue, List<Long> rcgid, //
			boolean sysheader, Integer sysstatus);

	// 查詢不含群組代表
	ArrayList<RepairCode> findAllBySysheaderOrderByRcgidAsc(boolean sysheader);

	// 查詢是否重複 群組
	ArrayList<RepairCode> findAllBySysheaderAndRcgname(boolean sysheader, String rcgname);

	// 查詢是否重複 及代碼
	ArrayList<RepairCode> findAllByRcvalue(String rcvlaue);

	// 查詢是否重複 及代碼
	ArrayList<RepairCode> findAllByRcvalueAndSysheader(String rcvlaue, boolean sysheader);

	// 取得最新G_ID
	@Query(value = "SELECT NEXTVAL('repair_code_g_seq')", nativeQuery = true)
	Long getRepairCode_g_seq();

	// 取得ID
	@Query(value = "SELECT CURRVAL('repair_code_seq')", nativeQuery = true)
	Long getRepairCode_seq();

	// delete
	Long deleteByRcidAndSysheader(Long id, Boolean sysheader);

	// delete 群組一除
	Long deleteByRcgid(Long id);
}