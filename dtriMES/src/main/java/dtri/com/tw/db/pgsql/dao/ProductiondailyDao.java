package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.ProductionDaily;

public interface ProductiondailyDao extends JpaRepository<ProductionDaily, Long> {

	// 查詢全部
	ArrayList<ProductionDaily> findAll();

	// 查詢一部分
	@Query("SELECT c FROM ProductionDaily c "//
			+ "WHERE (:pdprbomid is null or c.pdprbomid LIKE %:pdprbomid% ) and "//
			+ "((:pdstime is null and :pdetime is null) or c.pdstime BETWEEN :pdstime  AND :pdetime) and "//
			+ "(:pdprpmodel is null or c.pdprpmodel LIKE %:pdprpmodel% ) and "//
			+ "(:pdwcclass is null or c.pdwcclass LIKE %:pdwcclass% ) and "//
			+ "(:pdwcline is null or c.pdwcline LIKE %:pdwcline% ) and "//
			+ "(:pdprid is null or c.pdprid LIKE %:pdprid% ) and "//
			+ "( c.sysstatus = :sysstatus )  "//
			+ "order by c.pdid desc")
	ArrayList<ProductionDaily> findAllByProductionDaily(String pdprbomid, Date pdstime, Date pdetime, String pdprpmodel, //
			Integer sysstatus,String pdwcclass,String pdwcline,String pdprid, Pageable pageable);

	// 查詢是否重複 群組
	//@Query("SELECT c FROM SystemConfig c " + "WHERE  (c.scgname = :scgname) " + "order by c.scgid desc")
	//ArrayList<ProductionDaily> findAllByConfigGroupTop1(@Param("scgname") String scgname, Pageable pageable);


	// delete
	Long deleteByPdidAndSysheader(Long pdid, Boolean sysheader);
}