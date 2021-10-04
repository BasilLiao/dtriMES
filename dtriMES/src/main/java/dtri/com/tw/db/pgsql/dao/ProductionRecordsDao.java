package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.ProductionRecords;

public interface ProductionRecordsDao extends JpaRepository<ProductionRecords, Long> {

	// 查詢全部
	ArrayList<ProductionRecords> findAll();

	// 查詢一部分
	@Query("SELECT c FROM ProductionRecords c "//
			+ "WHERE (:prcname is null or c.prcname LIKE %:prcname% ) and "//
			+ "(:prorderid is null or c.prid LIKE %:prorderid% ) and "//
			+ "(:prbomid is null or c.prbomid LIKE %:prbomid% ) and "//
			+ "(:prssn is null or :prssn BETWEEN c.prssn  AND c.presn) and "//
			+ "( c.sysstatus = :sysstatus ) "//
			+ "order by c.sysmdate desc")
	ArrayList<ProductionRecords> findAllByRecords(@Param("prcname") String prcname, @Param("prorderid") String prorderid,
			@Param("prbomid") String prbomid, @Param("prssn") String prssn, @Param("sysstatus") Integer sysstatus, Pageable pageable);

	// 查詢是否重複 製令
	ArrayList<ProductionRecords> findAllByPrid(String prid, Pageable pageable);

	// delete
	Long deleteByPridAndSysheader(String id, Boolean sysheader);
}