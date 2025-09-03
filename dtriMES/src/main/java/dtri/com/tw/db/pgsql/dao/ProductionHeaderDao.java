package dtri.com.tw.db.pgsql.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.ProductionRecords;

@Repository
public interface ProductionHeaderDao extends JpaRepository<ProductionHeader, Long> {

	// 查詢全部
	List<ProductionHeader> findAll();

	// 查詢最新一筆的製令單關聯
	ProductionHeader findTopByPhpbgidOrderBySysmdateDesc(Long phpbgid);

	// 查詢一部分_ProductionHeader+ProductionRecords
	@Query("SELECT h FROM ProductionHeader h join h.productionRecords r  WHERE "//
			+ "(:prpmodel is null or r.prpmodel LIKE %:prpmodel% ) and "//
			+ "(:phprid is null or r.prid LIKE %:phprid% ) and "//
			+ "(:phorderid is null or h.phorderid LIKE %:phorderid% ) and "//
			+ "(:phcname is null or h.phcname LIKE %:phcname% ) and "//
			+ "(:prbomid is null or r.prbomid LIKE %:prbomid% ) and "//
			+ "(:prbitem is null or r.prbitem LIKE %:prbitem% ) and "//
			+ "(:prsitem is null or r.prsitem LIKE %:prsitem% ) and "//
			+ "(:sysstatus = -1 or  h.sysstatus = :sysstatus ) and "//
			+ "(cast(:sysmdate as date) is null or h.sysmdate >= :sysmdate ) and "//
			+ "(cast(:phssdate as date) is null or h.phsdate >= :phssdate ) and "//
			+ "(cast(:phsedate as date) is null or h.phsdate <= :phsedate ) and "//
			+ "(coalesce(:phpbgid, null) is null or h.phpbgid IN :phpbgid ) and "// coalesce 回傳非NULL值
			+ "(h.phid != 0) "//
			+ " order by h.sysmdate desc ")
	List<ProductionHeader> findAllByProductionHeader(//
			@Param("prpmodel") String prpmodel, @Param("phprid") String phprid, //
			@Param("sysstatus") Integer sysstatus, @Param("phpbgid") List<Long> phpbgid, //
			@Param("phorderid") String phorder_id, @Param("phcname") String phcname, //
			@Param("prbomid") String prbom_id, @Param("prbitem") String prbitem, //
			@Param("prsitem") String prsitem, @Param("phssdate") Date phssdate, //
			@Param("phsedate") Date phsedate, @Param("sysmdate") Date sys_m_date, Pageable pageable);

	// 取得當筆ID
	@Query(value = "SELECT CURRVAL('production_header_seq')", nativeQuery = true)
	Long getProductionHeaderSeq();

	// 查詢重複製令
	List<ProductionHeader> findAllByProductionRecords(ProductionRecords phprid);

	// 查詢重複製令
	List<ProductionHeader> findAllByProductionRecordsAndPhtype(ProductionRecords phprid, String Phtype);

	// 查詢重複製令+排除狀態
	List<ProductionHeader> findAllByProductionRecordsAndSysstatusNotIn(ProductionRecords phprid,
			List<Integer> sysstatus);

	// 排除狀態
	List<ProductionHeader> findAllBySysstatusNotIn(List<Integer> sysstatus);

	// 查詢重複製令+指定狀態
	List<ProductionHeader> findAllByProductionRecordsAndSysstatusIn(ProductionRecords phprid, List<Integer> sysstatus);

	// 查詢重複製令+ID
	List<ProductionHeader> findAllByProductionRecordsAndPhwpid(ProductionRecords phprid, Long phwpid);

	// 查詢ID
	List<ProductionHeader> findAllByPhid(Long phid);

	// 查詢 Body ID
	List<ProductionHeader> findAllByPhpbgid(Long phpbgid);

	// delete(header)
	Long deleteByPhidAndSysheader(Long id, Boolean sysheader);
}