package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.MaintenanceRegister;

public interface MaintenanceRegisterDao extends JpaRepository<MaintenanceRegister, Long> {

	// 查詢全部
	ArrayList<MaintenanceRegister> findAll();

}