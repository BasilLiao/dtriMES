package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.RepairDetail;

public interface RepairDetailDao extends JpaRepository<RepairDetail, String> {

	// 查詢全部
	ArrayList<RepairDetail> findAll();

	// 移除資料
	Long deleteByRdid(String rdid);

	// 查詢 維修單項目ID
	ArrayList<RepairDetail> findAllByRdid(String rdid);

	// 查詢 負責對象+維修單項目ID+SN產品號
	@Query("SELECT d FROM RepairDetail d join d.register r join d.order o  WHERE "//
			+ "(:rdid is null or d.rdid LIKE %:rdid%) and "//
			+ "(:rrsn is null or r.rrsn LIKE %:rrsn%) and "//
			+ "(:rdcheck = 0 or d.rdcheck  = :rdcheck) and"//
			+ "(o.rocheck  = :rocheck) and"//
			+ "(:rdruid = 0L or (d.rdruid  = :rdruid or d.rdruid=0L)) "//
			+ "order by d.rdid asc")
	ArrayList<RepairDetail> findAllByRdidAndRdruid(String rdid, String rrsn, int rdcheck, int rocheck, Long rdruid, Pageable pageable);

	// 查詢 負責對象+SN產品號
	/*
	 * @Query("SELECT d FROM RepairDetail d join d.register r WHERE "// +
	 * "(:rrsn is null or r.rrsn LIKE %:rrsn%) and "// +
	 * "(:rdcheck = 0 or d.rdcheck  >= :rdcheck ) and"// +
	 * "(:rdruid = 0L or (d.rdruid  = :rdruid or d.rdruid=0L)) "// +
	 * "order by d.rdid asc") ArrayList<RepairDetail> findAllByRrsnAndRdruid(String
	 * rrsn, int rdcheck, Long rdruid);
	 */
}