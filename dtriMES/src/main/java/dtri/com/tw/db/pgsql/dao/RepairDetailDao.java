package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

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
	@Query("SELECT d FROM RepairDetail d join d.register r join d.order o WHERE "//
			+ "(:rdid is null or d.rdid LIKE %:rdid%) and "//
			+ "(:rrsn is null or r.rrsn LIKE %:rrsn%) and "//
			+ "(:rdcheck = 0 or d.rdcheck  = :rdcheck) and"//
			+ "(o.rocheck  = :rocheck) and"//
			+ "(:roid is null or o.roid LIKE %:roid%) and "//
			+ "(:rdruid = 0L or (d.rdruid  = :rdruid or d.rdruid=0L)) "//
			+ "order by d.rdid asc")
	ArrayList<RepairDetail> findAllByRdidAndRdruid(String roid, String rdid, String rrsn, int rdcheck, int rocheck, Long rdruid, Pageable pageable);

	// 場內維修 特定查詢
	// 查詢 檢核
	@Query("SELECT d FROM RepairDetail d join d.register r join d.order o  WHERE "//
			+ "(:rdstatement is null or d.rdstatement LIKE %:rdstatement% ) and "// 問題描述
			+ "(:rdufinally is null or d.rdufinally LIKE %:rdufinally% ) and "// 修復 人員
			+ "(:roid     is null or o.roid LIKE %:roid% ) and "// 維修單
			+ "(:rrsn     is null or r.rrsn LIKE %:rrsn% ) and "// 產品序號
			+ "(:rdcheck  is null or d.rdcheck  = :rdcheck ) and "// 檢核狀態
			+ "(:rrpbtype is null or r.rrpbtype = :rrpbtype ) and "// 產品類型
			+ "(cast(:rosramdate as date) is null or  :rosramdate > o.roramdate ) and "// 申請日(起)
			+ "(cast(:roeramdate as date) is null or  :roeramdate < o.roramdate ) and"// 申請日(終)
			+ "(cast(:rrspbsysmdate as date) is null or  :rrspbsysmdate > r.rrpbsysmdate ) and "// 生產日期(起)
			+ "(cast(:rrepbsysmdate as date) is null or  :rrepbsysmdate < r.rrpbsysmdate ) and "// 生產日期(終)
			+ "(:rofrom  is null or o.rofrom  = :rofrom ) ") // 來源
	ArrayList<RepairDetail> findAllByRepairDetail(//
			String roid, String rrsn, String rdcheck, String rrpbtype, String rdstatement, String rdufinally, //
			Date rosramdate, Date roeramdate, Date rrspbsysmdate, Date rrepbsysmdate, String rofrom, Pageable pageable);
}