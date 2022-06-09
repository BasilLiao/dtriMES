package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}