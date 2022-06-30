package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.MaintenanceOrder;

public interface MaintenanceOrderDao extends JpaRepository<MaintenanceOrder, Long> {

	// 查詢全部
	ArrayList<MaintenanceOrder> findAll();

}