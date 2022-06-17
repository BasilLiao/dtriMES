package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.ProductionDaily;

public interface MaintainOrderDtrDao extends JpaRepository<ProductionDaily, Long> {

	// 查詢全部
	ArrayList<ProductionDaily> findAll();

	// 查詢 檢核
	@Query("SELECT c FROM ProductionDaily c WHERE "//
			+ "(:pdprid    is null or c.pdprid LIKE %:pdprid% ) and "// 工單號?
			+ "(:pdprbomid is null or c.pdprbomid LIKE %:pdprbomid% ) and "// BOM ID?
			+ "(:pdwcline  is null or c.pdwcline LIKE %:pdwcline% ) and "// 線別?
			+ "(:pdwcclass is null or c.pdwcclass LIKE %:pdwcclass% ) and "// 班別?
			+ "(:pdwcname is null or c.pdwcname LIKE %:pdwcname% ) and "// 工作站(代號)?
			+ "(:pdwaccounts is null or c.pdwaccounts = :pdwaccounts ) and "// 同一批人?
			+ "(:pdstime is null or to_char(c.pdstime,'YYYY-MM-DD') LIKE %:pdstime% ) and "// 同一天?
			+ "( c.sysstatus = :sysstatus ) "// 0 = 正常 /1 = 結單
			+ "order by c.pdid desc")
	ArrayList<ProductionDaily> findAllByProductionDailyCheck(//
			String pdprid, String pdprbomid, String pdwcline, String pdwcclass, String pdwcname, String pdwaccounts, String pdstime, Integer sysstatus);

	// ProductionDaily 每日報表查詢
	@Query("SELECT c FROM ProductionDaily c WHERE"//
			+ "(:pdwcline is null or  c.pdwcline LIKE %:pdwcline% ) and "// 線別
			+ "(:pdwcclass is null or c.pdwcclass LIKE %:pdwcclass% ) and "// 班別
			+ "(:pdprid is null or    c.pdprid LIKE %:pdprid% ) and "// 工單號
			+ "(:pdprpmodel is null or c.pdprpmodel LIKE %:pdprpmodel% ) and "// 產品型號
			+ "(:pdprbomid is null or c.pdprbomid LIKE %:pdprbomid% ) and "// BOM ID
			+ "(cast(:pdstime as date) is null or c.sysmdate >= :pdstime) and "// 時間區間
			+ "(cast(:pdetime as date) is null or c.sysmdate <= :pdetime) "// 時間區間
			+ "order by to_char(c.pdstime,'YYYY-MM-DD') desc, c.pdwcline asc, c.pdwcclass asc, c.pdprid asc,c.pdwcname asc")
	ArrayList<ProductionDaily> findAllByProductionDaily(//
			String pdwcline, String pdwcclass, String pdprid, String pdprpmodel, String pdprbomid, Date pdstime, Date pdetime);

	// 查詢是否重複 SN
	ArrayList<ProductionDaily> findAllByPdpridAndPdpbbsnLikeAndPdwcname(String pdprid, String pdprpbsn, String pdwcname);

	// @Query("SELECT c FROM SystemConfig c " + "WHERE (c.scgname = :scgname) " +
	// "order by c.scgid desc")
	// ArrayList<ProductionDaily> findAllByConfigGroupTop1(@Param("scgname") String
	// scgname, Pageable pageable);

	// delete
	Long deleteByPdidAndSysheader(Long pdid, Boolean sysheader);
}