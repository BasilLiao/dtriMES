package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.MaintenanceRegister;

public interface MaintenanceRegisterDao extends JpaRepository<MaintenanceRegister, String> {

	// 查詢全部
	ArrayList<MaintenanceRegister> findAll();

	ArrayList<MaintenanceRegister> findAllByMrsn(String mrsn);

	// 移除資料
	Long deleteByDetails(String details);

}