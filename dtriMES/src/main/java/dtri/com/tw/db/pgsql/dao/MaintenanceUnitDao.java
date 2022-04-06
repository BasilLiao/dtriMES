package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.MaintenanceUnit;

public interface MaintenanceUnitDao extends JpaRepository<MaintenanceUnit, Long> {

	// 查詢ID
	List<MaintenanceUnit> findByMuidOrderBySyssortAsc(Long muid);

	// 查詢G_ID
	List<MaintenanceUnit> findByMugidOrderBySyssortAsc(Long mugid);

	// 查詢群組名稱()
	@Query("SELECT m FROM MaintenanceUnit m "//
			+ "WHERE  "//
			+ "(:mugid = 0L or m.mugid = mugid) and "//
			+ "(:mugname is null or m.mugname LIKE %:mugname%) and "//
			+ "(:musuname is null or m.musuname LIKE %:musuname%) and "//
			+ "( m.sysheader = :sysheader )  "//
			+ "order by m.mugid asc, m.muid asc")
	List<MaintenanceUnit> findAllByMaintenanceUnit(Long mugid, String mugname, String musuname, boolean sysheader, Pageable p);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('maintenance_unit_g_seq')", nativeQuery = true)
	Long getMaintenance_unit_g_seq();

	// 查詢是否重複 群組
	@Query("SELECT c FROM MaintenanceUnit c WHERE  (c.mugname = :mugname) order by c.muid desc")
	ArrayList<MaintenanceUnit> findAllByGroupTop1(String mugname, Pageable pageable);

	// 移除
	Long deleteByMugid(Long mugid);
}