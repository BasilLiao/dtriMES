package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.ProductionDaily;

public interface ProductiondailyDao extends JpaRepository<ProductionDaily, Long> {

	// 查詢全部
	ArrayList<ProductionDaily> findAll();

	// 查詢一部分
	@Query("SELECT c FROM ProductionDaily c "//
			+ "WHERE (:pdprbomid is null or c.pdprbomid LIKE %:pdprbomid% ) and "// BOM ID
			+ "(:pdprpbsn is null or c.pdprpbsn LIKE %:pdprpbsn% ) and "// 燒錄SN
			+ "((cast(:pdstime as date) is null and cast(:pdetime as date) is null) or c.pdstime BETWEEN :pdstime  AND :pdetime) and "// 開始時間 與 結束時間
			+ "(:pdprpmodel is null or c.pdprpmodel LIKE %:pdprpmodel% ) and "// 產品型號
			+ "(:pdwcclass is null or c.pdwcclass LIKE %:pdwcclass% ) and "// 班別
			+ "(:pdwcline is null or c.pdwcline LIKE %:pdwcline% ) and "// 線別
			+ "(:pdprid is null or c.pdprid LIKE %:pdprid% ) and "// 工單號
			+ "(:pdwaccounts is null or c.pdwaccounts = :pdwaccounts ) and "//
			+ "( c.sysstatus = :sysstatus ) "//
			+ "order by c.pdid desc")
	ArrayList<ProductionDaily> findAllByProductionDaily(//
			String pdprbomid, String pdprpbsn, Date pdstime, Date pdetime, String pdprpmodel, //
			Integer sysstatus, String pdwcclass, String pdwcline, String pdprid, String pdwaccounts, Pageable pageable);

	// 查詢是否重複 群組
	// @Query("SELECT c FROM SystemConfig c " + "WHERE (c.scgname = :scgname) " +
	// "order by c.scgid desc")
	// ArrayList<ProductionDaily> findAllByConfigGroupTop1(@Param("scgname") String
	// scgname, Pageable pageable);

	// delete
	Long deleteByPdidAndSysheader(Long pdid, Boolean sysheader);
}