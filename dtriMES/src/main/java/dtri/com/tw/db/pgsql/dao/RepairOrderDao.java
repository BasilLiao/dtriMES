package dtri.com.tw.db.pgsql.dao;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.RepairOrder;

public interface RepairOrderDao extends JpaRepository<RepairOrder, Long> {

	// 查詢全部
	ArrayList<RepairOrder> findAll();

	// 查詢特定ID
	ArrayList<RepairOrder> findAllByRoid(String roid);

	// 場內維修 特定查詢(只有 維修單據)
	// 查詢 檢核
	@Query("SELECT o.roid FROM RepairOrder o LEFT join o.details d  join d.register r  WHERE "//
			+ "(:roid     is null or o.roid LIKE %:roid% ) and "// 維修單
			+ "(:rrsn     is null or r.rrsn LIKE %:rrsn% ) and "// 產品序號
			+ "(:rdcheck  is null or d.rdcheck  = :rdcheck ) and "// 檢核狀態
			+ "(:rrpbtype is null or r.rrpbtype = :rrpbtype ) and "// 產品類型
			+ "(:rdstatement is null or d.rdstatement LIKE %:rdstatement% ) and "// 問題描述
			+ "(:rdufinally is null or d.rdufinally LIKE %:rdufinally% ) and "// 修復 人員
			+ "(cast(:rosramdate as date) is null or  :rosramdate > o.roramdate ) and "// 申請日(起)
			+ "(cast(:roeramdate as date) is null or  :roeramdate < o.roramdate ) and"// 申請日(終)
			+ "(cast(:rrspbsysmdate as date) is null or  :rrspbsysmdate > r.rrpbsysmdate ) and "// 生產日期(起)
			+ "(cast(:rrepbsysmdate as date) is null or  :rrepbsysmdate < r.rrpbsysmdate ) and "// 生產日期(終)
			+ "(:rofrom  is null or o.rofrom  = :rofrom ) "// 來源
			+ " GROUP By o.roid")
	ArrayList<String> findAllByRepairOrder(//
			String roid, String rrsn, String rdcheck, String rrpbtype, String rdstatement, String rdufinally, //
			Date rosramdate, Date roeramdate, Date rrspbsysmdate, Date rrepbsysmdate, String rofrom, Pageable pageable);


	@Query("SELECT o FROM RepairOrder o WHERE " //
			+ "(coalesce(:roid, null) is null or o.roid IN :roid ) order by o.roid desc ") // coalesce 回傳非NULL值
	ArrayList<RepairOrder> findAllByRepairOrder(List<String> roid);

	// 移除資料
	Long deleteByRoid(String roid);

}