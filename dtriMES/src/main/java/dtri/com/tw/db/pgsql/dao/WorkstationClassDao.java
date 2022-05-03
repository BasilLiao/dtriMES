package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.WorkstationClass;

public interface WorkstationClassDao extends JpaRepository<WorkstationClass, Long> {

	// 查詢全部
	ArrayList<WorkstationClass> findAll();

	// 查詢一部分
	@Query("SELECT c FROM WorkstationClass c " //
			+ "WHERE (:wcclass is null or c.wcclass LIKE %:wcclass% ) and "//
			+ "(:wcpline is null or c.wcpline LIKE %:wcpline% ) and "//
			+ "( c.sysstatus = :sysstatus ) "//
			+ "order by c.wcpline asc , c.wcstime asc, c.wcwcname asc")
	ArrayList<WorkstationClass> findAllByClass(String wcclass, String wcpline, Integer sysstatus, Pageable pageable);

	// 查詢是否重複 同一線別+班別+工作站
	@Query("SELECT c FROM WorkstationClass c " + //
			"WHERE (:wcclass is null or c.wcclass = :wcclass) and " + //
			"(:wcpline is null or c.wcpline = :wcpline) and " + //
			"(:wcwcname is null or c.wcwcname LIKE %:wcwcname%) and " + //
			"(:wcstime is null or :wcstime BETWEEN c.wcstime AND c.wcetime)" + //
			"order by c.wcpline asc ,c.wcclass asc, c.wcwcname")
	ArrayList<WorkstationClass> findAllBySameClass(String wcclass, String wcpline, String wcwcname, String wcstime, Pageable pageable);

	// 取得不同班別
	@Query(value = "SELECT DISTINCT w.wcpline FROM WorkstationClass w")
	ArrayList<String> getWcLineDistinct();

	// delete
	Long deleteByWcid(Long id);
}