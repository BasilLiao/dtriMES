package dtri.com.tw.db.pgsql.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.MaintainCode;

public interface MaintainCodeDao extends JpaRepository<MaintainCode, Long> {

	// 查詢全部
	ArrayList<MaintainCode> findAll();

	// 查詢一部分
	@Query("SELECT c FROM MaintainCode c " //
			+ "WHERE (:mcvalue is null or c.mcvalue LIKE %:mcvalue% ) and "//
			+ "(:mcgname is null or c.mcgname LIKE %:mcgname% ) and " //
			+ "(coalesce(:mcgid, null) is null or c.mcgid IN :mcgid ) and "// coalesce 回傳非NULL值
			+ "( c.sysstatus = :sysstatus )  " //
			+ "order by c.mcgid asc,c.sysheader desc,c.mcvalue asc")
	ArrayList<MaintainCode> findAllByMaintainCode(@Param("mcvalue") String mc_value, @Param("mcgname") String mcgname,
			 @Param("mcgid") List<Long> mc_g_id,@Param("sysstatus") Integer sysstatus);

	// 查詢不含群組代表
	ArrayList<MaintainCode> findAllBySysheaderOrderByMcgidAsc(boolean sysheader);
	
	// 查詢不含群組代表 and 限制
	@Query("SELECT c.mcgid FROM MaintainCode c " //
			+ "WHERE "//
			+ "( c.sysheader = :sysheader )  " //
			+ "order by c.mcgid asc,c.sysheader desc,c.mcvalue asc")
	ArrayList<BigInteger> findAllBySysheaderOrderByMcgidAsc(boolean sysheader,Pageable pageable);

	// 查詢是否重複 群組
	ArrayList<MaintainCode> findAllBySysheaderAndMcgname(boolean sysheader, String gname);

	// 查詢是否重複 及代碼
	ArrayList<MaintainCode> findAllByMcvalue(String vlaue);

	// 查詢是否重複 及代碼
	ArrayList<MaintainCode> findAllByMcvalueAndSysheader(String vlaue, boolean sysheader);

	// 取得最新G_ID
	@Query(value = "SELECT NEXTVAL('maintain_code_g_seq')", nativeQuery = true)
	Long getMaintain_code_g_seq();

	// 取得ID
	@Query(value = "SELECT CURRVAL('maintain_code_seq')", nativeQuery = true)
	Long getMaintain_code_seq();

	// delete
	Long deleteByMcidAndSysheader(Long id, Boolean sysheader);

	// delete 群組一除
	Long deleteByMcgid(Long id);
}