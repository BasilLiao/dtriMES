package dtri.com.tw.db.pgsql.dao;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.MaintenanceOrder;

public interface MaintenanceOrderDao extends JpaRepository<MaintenanceOrder, Long> {

	// 查詢全部
	ArrayList<MaintenanceOrder> findAll();

	// 查詢特定ID
	ArrayList<MaintenanceOrder> findAllByMoid(String moid);

	// 場內維修 特定查詢
	// 查詢 檢核
	@Query("SELECT o.moid FROM MaintenanceOrder o LEFT join o.details d  join d.register r  WHERE "//
			+ "(:moid     is null or o.moid LIKE %:moid% ) and "// 維修單
			+ "(:mrsn     is null or r.mrsn LIKE %:mrsn% ) and "// 產品序號
			+ "(:mdcheck  is null or d.mdcheck  = :mdcheck ) and "// 檢核狀態
			+ "(:mrpbtype is null or r.mrpbtype = :mrpbtype ) and "// 產品類型
			+ "(:mdstatement is null or d.mdstatement LIKE %:mdstatement% ) and "// 問題描述
			+ "(cast(:mosramdate as date) is null or  :mosramdate > o.moramdate ) and "// 申請日(起)
			+ "(cast(:moeramdate as date) is null or  :moeramdate < o.moramdate ) and"// 申請日(終)
			+ "(cast(:mrspbsysmdate as date) is null or  :mrspbsysmdate > r.mrpbsysmdate ) and "// 生產日期(起)
			+ "(cast(:mrepbsysmdate as date) is null or  :mrepbsysmdate < r.mrpbsysmdate ) "// 生產日期(終)
			+ " GROUP By o.moid")
	ArrayList<String> findAllByMaintenanceOrder(//
			String moid, String mrsn, String mdcheck, String mrpbtype, String mdstatement, //
			Date mosramdate, Date moeramdate, Date mrspbsysmdate, Date mrepbsysmdate, Pageable pageable);

	@Query("SELECT o FROM MaintenanceOrder o WHERE " //
			+ "(coalesce(:moid, null) is null or o.moid IN :moid ) order by o.moid desc ") // coalesce 回傳非NULL值
	ArrayList<MaintenanceOrder> findAllByMaintenanceOrder(List<String> moid);
	
	// 移除資料
	Long deleteByMoid(String moid);

}