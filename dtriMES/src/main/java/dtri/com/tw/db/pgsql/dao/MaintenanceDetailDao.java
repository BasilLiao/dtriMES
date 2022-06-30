package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.MaintenanceDetail;

public interface MaintenanceDetailDao extends JpaRepository<MaintenanceDetail, Long> {

	// 查詢全部
	ArrayList<MaintenanceDetail> findAll();

}