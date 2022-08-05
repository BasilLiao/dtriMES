package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

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

	// 查詢 負責對象+維修單項目ID
	@Query("SELECT d FROM RepairDetail d WHERE "//
			+ "(:rdid is null or d.rdid LIKE %:rdid%) and "//
			+ "(:rdruid = 0L or d.rdruid  = :rdruid or d.rdruid=0L) "//
			+ "order by d.rdid asc")
	ArrayList<RepairDetail> findAllByRdidAndRdruid(String rdid, Long rdruid);

	// 查詢 負責對象+SN產品號
	@Query("SELECT d FROM RepairDetail d join d.register r WHERE "//
			+ "(:rrsn is null or r.rrsn LIKE %:rrsn%) and "//
			+ "(:rdruid = 0L or d.rdruid  = :rdruid or d.rdruid=0L) "//
			+ "order by d.rdid asc")
	ArrayList<RepairDetail> findAllByRrsnAndRdruid(String rrsn, Long rdruid);
}