package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.db.entity.ProductionTest;

public interface ProductionTestDao extends JpaRepository<ProductionTest, Long> {

	@Query("SELECT t FROM ProductionTest t " //
			+ "WHERE (:ptpbbsn is null or t.ptpbbsn LIKE %:ptpbbsn% ) and "//
			+ "(:ptprid is null or t.ptprid LIKE %:ptprid% ) and "//
			+ "(:ptprmodel is null or t.ptprmodel LIKE %:ptprmodel% ) and "//
			+ "(:ptprbomid is null or t.ptprbomid LIKE %:ptprbomid% ) and "//
			+ "(:ptprbitem is null or t.ptprbitem LIKE %:ptprbitem% ) and "//
			+ "(:ptprsitem is null or t.ptprsitem LIKE %:ptprsitem% ) "//
	)
	ArrayList<ProductionTest> findAllByTest(String ptpbbsn, String ptprid, //
			String ptprmodel, String ptprbomid, //
			String ptprbitem, String ptprsitem, Pageable pageable);

	/**
	 * 使用 PostgreSQL 原生語法移除 3 年前的測試紀錄
	 * 
	 * @return 影響（刪除）的資料筆數
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM production_test WHERE sys_c_date < NOW() - INTERVAL '2 years'", nativeQuery = true)
	int deleteThreeYearsAgoRecords();

}