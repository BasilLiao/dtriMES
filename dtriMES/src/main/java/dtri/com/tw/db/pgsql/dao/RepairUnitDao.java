package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.RepairUnit;

public interface RepairUnitDao extends JpaRepository<RepairUnit, Long> {

	// 查詢ID
	List<RepairUnit> findByRuidOrderBySyssortAsc(Long ruid);

	// 查詢user ID
	List<RepairUnit> findByRusuidOrderBySyssortAsc(Long rusuid);

	// 查詢G_ID
	List<RepairUnit> findByRugidOrderBySyssortAsc(Long rugid);

	// 查詢群組名稱()
	@Query("SELECT r FROM RepairUnit r "//
			+ "WHERE "//
			+ "(:rugid = 0L or r.rugid = rugid) and "//
			+ "(:rugname is null or r.rugname LIKE %:rugname%) and "//
			+ "(:rusuname is null or r.rusuname LIKE %:rusuname%) and "//
			+ "( r.sysheader = :sysheader )  "//
			+ "order by r.rugid asc, r.ruid asc")
	List<RepairUnit> findAllByRepairUnit(Long rugid, String rugname, String rusuname, boolean sysheader, Pageable p);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('repair_unit_g_seq')", nativeQuery = true)
	Long getRepairUnit_g_seq();

	// 查詢是否重複 群組
	@Query("SELECT c FROM RepairUnit c WHERE  (c.rugname = :rugname) order by c.ruid desc")
	ArrayList<RepairUnit> findAllByGroupTop1(String rugname, Pageable pageable);

	// 移除
	Long deleteByRugid(Long rugid);
}