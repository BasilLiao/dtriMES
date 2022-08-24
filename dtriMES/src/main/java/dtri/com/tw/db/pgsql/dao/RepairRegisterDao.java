package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.RepairRegister;

public interface RepairRegisterDao extends JpaRepository<RepairRegister, String> {

	// 查詢全部
	ArrayList<RepairRegister> findAll();

	ArrayList<RepairRegister> findAllByRrsn(String rrsn);

	ArrayList<RepairRegister> findAllByRrprid(String rrprid);

	// 移除資料
	Long deleteByDetails(String details);

}