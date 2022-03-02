package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.Customer;

public interface CustomerDao extends JpaRepository<Customer, Long> {

	// 查詢全部
	ArrayList<Customer> findAll();

	// 查詢一部分
	@Query("SELECT c FROM Customer c " //
			+ "WHERE (:ccname is null or c.ccname LIKE %:ccname% ) and "//
			+ "(:cname is null or c.cname LIKE %:cname% ) and "//
			+ "(:sysstatus = 0 or c.sysstatus = :sysstatus )  "//
			+ "order by c.sysmdate desc")
	ArrayList<Customer> findAllByCustomer(String ccname, String cname, Integer sysstatus, Pageable pageable);

	ArrayList<Customer> findAllByCid(Long cid);

	// delete
	Long deleteBycid(Long id);
}