package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.LabelList;

public interface LabelListDao extends JpaRepository<LabelList, String> {

	// 查詢全部
	@Query("SELECT o FROM LabelList o  WHERE "//
			+ "(:llname is null or o.llname =:llname ) And"// 標籤名稱
			+ "(:llgname is null or o.llgname =:llgname ) "// 標籤群組名稱
			+ "ORDER By o.llgname asc,o.llname asc")
	ArrayList<LabelList> findAllByLlgnameAndLlname(String llname, String llgname, Pageable pageable);

	ArrayList<LabelList> findAllByLlid(Long id);

	ArrayList<LabelList> findAllByOrderByLlgnameAscLlnameAsc();

	// 移除資料
	Long deleteByLlid(Long id);

	// 取得不同群組
	@Query(value = "SELECT DISTINCT w.llgname FROM LabelList w")
	ArrayList<String> getLabelGroupDistinct();

}