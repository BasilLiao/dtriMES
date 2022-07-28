package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.db.entity.RepairDetail;

public interface RepairDetailDao extends JpaRepository<RepairDetail, String> {

	// 查詢全部
	ArrayList<RepairDetail> findAll();

	// 移除資料
	Long deleteByRdid(String rdid);

	ArrayList<RepairDetail> findAllByRdid(String rdid);

}