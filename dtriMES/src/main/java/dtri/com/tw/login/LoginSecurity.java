package dtri.com.tw.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class LoginSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	LoginUserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 這個method可以設定那些路由要經過身分權限的審核，或是login、logout路由特別設定等地方，因此這邊也是設定身分權限的關鍵地方。
		// authorizeRequests()配置路徑攔截，表明路徑訪問所對應的權限，角色，認證信息。
		// formLogin()對應表單認證相關的配置
		// logout()對應了註銷相關的配置
		// httpBasic()可以配置basic登錄
		// (permitAll = 全部允許) (authenticated = 登入後可訪問) (anyRequest = 所有請求)

		// Restful API ( GET -> 訪問/查詢 ) ( POST -> 新增 ) ( PUT -> 更新) ( DELETE -> 刪除 )
		// 下列-權限驗證
		String system_con = "/ajax/system_config.basil";
		String system_per = "/ajax/system_permission.basil";
		String system_gro = "/ajax/system_group.basil";
		String system_use = "/ajax/system_user.basil";
		String system_mail = "/ajax/system_mail.basil" ;
		
		String production_head = "/ajax/production_header.basil";
		String production_body = "/ajax/production_body.basil";
		String production_reco = "/ajax/production_records.basil";
		String production_conf = "/ajax/production_config.basil";
		String production_daily = "/ajax/production_daily.basil";
		String production_test = "/ajax/production_test.basil";

		String workstation = "/ajax/workstation.basil";
		// String workstation_item = "/ajax/workstation_item.basil";
		String workstation_class = "/ajax/workstation_class.basil";
		String workstation_work = "/ajax/workstation_work.basil";
		String workstation_conf = "/ajax/workstation_config.basil";
		String workstation_prog = "/ajax/workstation_program.basil";
		String workstation_repeat = "/ajax/workstation_repeat.basil";
		String workstation_disassemble = "/ajax/workstation_disassemble.basil";

		String own_user = "/ajax/own_user.basil";
		String customer = "/ajax/customer.basil";	

		String repair_unit = "/ajax/repair_unit.basil";
		String repair_code = "/ajax/repair_code.basil";
		String repair_history = "/ajax/repair_history.basil";
		String repair_order_dtr = "/ajax/repair_order_dtr.basil";

		String repair_list = "/ajax/repair_list.basil";
		String repair_list_bat = "/ajax/repair_list_bat.basil";
		String repair_rma_list_bat = "/ajax/repair_rma_list_bat.basil";

		String repair_rma_list = "/ajax/repair_rma_list.basil";
		
		String label_list = "/ajax/label_list.basil";

		String work_hours = "/ajax/work_hours.basil";
		String work_type = "/ajax/work_type.basil";

		http.authorizeRequests()
				// thirdparty && img 資料夾靜態資料可 直接 存取 (預設皆有 訪問權限 資料可[匿名]存取)
				.antMatchers(HttpMethod.GET, "/thirdparty/**", "/img/**", "/login.basil", "/login.html").permitAll()
				.antMatchers(HttpMethod.POST, "/ajax/api.basil").permitAll()
				// ----請求-index-(訪問)----
				.antMatchers(HttpMethod.POST, "/ajax/index.basil").hasAuthority(actionRole("index.basil", ""))

				// ----請求-system_config-(訪問) ----
				.antMatchers(HttpMethod.POST, system_con).hasAuthority(actionRole(system_con, ""))//
				.antMatchers(HttpMethod.POST, system_con + ".AR").hasAuthority(actionRole(system_con, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, system_con + ".AC").hasAuthority(actionRole(system_con, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, system_con + ".AU").hasAuthority(actionRole(system_con, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, system_con + ".AD").hasAuthority(actionRole(system_con, "AD"))// (移除)

				// ----請求-system_permission-(訪問) ----
				.antMatchers(HttpMethod.POST, system_per).hasAuthority(actionRole(system_per, ""))//
				.antMatchers(HttpMethod.POST, system_per + ".AR").hasAuthority(actionRole(system_per, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, system_per + ".AC").hasAuthority(actionRole(system_per, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, system_per + ".AU").hasAuthority(actionRole(system_per, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, system_per + ".AD").hasAuthority(actionRole(system_per, "AD"))// (移除)

				// ----請求-sys_group-(訪問) ----
				.antMatchers(HttpMethod.POST, system_gro).hasAuthority(actionRole(system_gro, ""))//
				.antMatchers(HttpMethod.POST, system_gro + ".AR").hasAuthority(actionRole(system_gro, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, system_gro + ".AC").hasAuthority(actionRole(system_gro, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, system_gro + ".AU").hasAuthority(actionRole(system_gro, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, system_gro + ".AD").hasAuthority(actionRole(system_gro, "AD"))// (移除)

				// ----請求-sys_user-(訪問) ----
				.antMatchers(HttpMethod.POST, system_use).hasAuthority(actionRole(system_use, ""))//
				.antMatchers(HttpMethod.POST, system_use + ".AR").hasAuthority(actionRole(system_use, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, system_use + ".AC").hasAuthority(actionRole(system_use, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, system_use + ".AU").hasAuthority(actionRole(system_use, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, system_use + ".AD").hasAuthority(actionRole(system_use, "AD"))// (移除)

				// ----請求-production_header-(訪問) ----
				.antMatchers(HttpMethod.POST, production_head).hasAuthority(actionRole(production_head, ""))//
				.antMatchers(HttpMethod.POST, production_head + ".AR").hasAuthority(actionRole(production_head, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, production_head + ".AC").hasAuthority(actionRole(production_head, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, production_head + ".AU").hasAuthority(actionRole(production_head, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, production_head + ".AD").hasAuthority(actionRole(production_head, "AD"))// (移除)

				// ----請求-production_body-(訪問) ----
				.antMatchers(HttpMethod.POST, production_body).hasAuthority(actionRole(production_body, ""))//
				.antMatchers(HttpMethod.POST, production_body + ".AR").hasAuthority(actionRole(production_body, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, production_body + ".AC").hasAuthority(actionRole(production_body, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, production_body + ".AU").hasAuthority(actionRole(production_body, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, production_body + ".AD").hasAuthority(actionRole(production_body, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, production_body + ".RT").hasAuthority(actionRole(production_body, "AR"))// (報告)

				// ----請求-production_records-(訪問) ----
				.antMatchers(HttpMethod.POST, production_reco).hasAuthority(actionRole(production_reco, ""))//
				.antMatchers(HttpMethod.POST, production_reco + ".AR").hasAuthority(actionRole(production_reco, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, production_reco + ".AC").hasAuthority(actionRole(production_reco, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, production_reco + ".AU").hasAuthority(actionRole(production_reco, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, production_reco + ".AD").hasAuthority(actionRole(production_reco, "AD"))// (移除)

				// ----請求-production_test-(訪問) ----
				.antMatchers(HttpMethod.POST, production_test).hasAuthority(actionRole(production_test, ""))//
				.antMatchers(HttpMethod.POST, production_test + ".AR").hasAuthority(actionRole(production_test, "AR"))// (查詢)

				// ----請求-workstation_item-(訪問) ----
				/*
				 * .antMatchers(HttpMethod.POST,
				 * workstation_item).hasAuthority(actionRole(workstation_item, ""))//
				 * .antMatchers(HttpMethod.POST, workstation_item +
				 * ".AR").hasAuthority(actionRole(workstation_item, "AR"))// (查詢)
				 * .antMatchers(HttpMethod.POST, workstation_item +
				 * ".AC").hasAuthority(actionRole(workstation_item, "AC"))// (新增)
				 * .antMatchers(HttpMethod.PUT, workstation_item +
				 * ".AU").hasAuthority(actionRole(workstation_item, "AU"))// (修改)
				 * .antMatchers(HttpMethod.DELETE, workstation_item +
				 * ".AD").hasAuthority(actionRole(workstation_item, "AD"))// (移除)
				 */

				// ----請求-workstation_work-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_work).hasAuthority(actionRole(workstation_work, ""))//
				.antMatchers(HttpMethod.POST, workstation_work + ".AR").hasAuthority(actionRole(workstation_work, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation_work + ".AC").hasAuthority(actionRole(workstation_work, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation_work + ".AU").hasAuthority(actionRole(workstation_work, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation_work + ".AD").hasAuthority(actionRole(workstation_work, "AD"))// (移除)

				// ----請求-workstation_class-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_class).hasAuthority(actionRole(workstation_class, ""))//
				.antMatchers(HttpMethod.POST, workstation_class + ".AR").hasAuthority(actionRole(workstation_class, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation_class + ".AC").hasAuthority(actionRole(workstation_class, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation_class + ".AU").hasAuthority(actionRole(workstation_class, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation_class + ".AD").hasAuthority(actionRole(workstation_class, "AD"))// (移除)

				// ----請求-workstation_work-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation).hasAuthority(actionRole(workstation, ""))//
				.antMatchers(HttpMethod.POST, workstation + ".AR").hasAuthority(actionRole(workstation, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation + ".AC").hasAuthority(actionRole(workstation, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation + ".AU").hasAuthority(actionRole(workstation, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation + ".AD").hasAuthority(actionRole(workstation, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, workstation + ".PT").hasAuthority(actionRole(workstation, "PT"))// (列印)

				// ----請求-workstation_program-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_prog).hasAuthority(actionRole(workstation_prog, ""))//
				.antMatchers(HttpMethod.POST, workstation_prog + ".AR").hasAuthority(actionRole(workstation_prog, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation_prog + ".AC").hasAuthority(actionRole(workstation_prog, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation_prog + ".AU").hasAuthority(actionRole(workstation_prog, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation_prog + ".AD").hasAuthority(actionRole(workstation_prog, "AD"))// (移除)

				// ----請求-production_config-(訪問) ----
				.antMatchers(HttpMethod.POST, production_conf).hasAuthority(actionRole(production_conf, ""))//
				.antMatchers(HttpMethod.POST, production_conf + ".AR").hasAuthority(actionRole(production_conf, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, production_conf + ".AC").hasAuthority(actionRole(production_conf, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, production_conf + ".AU").hasAuthority(actionRole(production_conf, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, production_conf + ".AD").hasAuthority(actionRole(production_conf, "AD"))// (移除)

				// ----請求-production_daily-(訪問) ----
				.antMatchers(HttpMethod.POST, production_daily).hasAuthority(actionRole(production_daily, ""))//
				.antMatchers(HttpMethod.POST, production_daily + ".AR").hasAuthority(actionRole(production_daily, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, production_daily + ".AC").hasAuthority(actionRole(production_daily, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, production_daily + ".AU").hasAuthority(actionRole(production_daily, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, production_daily + ".AD").hasAuthority(actionRole(production_daily, "AD"))// (移除)

				// ----請求-workstation_config-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_conf).hasAuthority(actionRole(workstation_conf, ""))//
				.antMatchers(HttpMethod.POST, workstation_conf + ".AR").hasAuthority(actionRole(workstation_conf, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation_conf + ".AC").hasAuthority(actionRole(workstation_conf, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation_conf + ".AU").hasAuthority(actionRole(workstation_conf, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation_conf + ".AD").hasAuthority(actionRole(workstation_conf, "AD"))// (移除)

				// ----請求-workstation_repeat-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_repeat).hasAuthority(actionRole(workstation_repeat, ""))//
				.antMatchers(HttpMethod.POST, workstation_repeat + ".AR").hasAuthority(actionRole(workstation_repeat, "AR"))// (查詢[檢查])
				.antMatchers(HttpMethod.POST, workstation_repeat + ".AC").hasAuthority(actionRole(workstation_repeat, "AC"))// (新增[登記重工])
				.antMatchers(HttpMethod.PUT, workstation_repeat + ".AU").hasAuthority(actionRole(workstation_repeat, "AU"))// (修改[交換])
				.antMatchers(HttpMethod.DELETE, workstation_repeat + ".AD").hasAuthority(actionRole(workstation_repeat, "AD"))// (移除)

				// ----請求-workstation_disassemble-(訪問) ----
				.antMatchers(HttpMethod.POST, workstation_disassemble).hasAuthority(actionRole(workstation_disassemble, ""))//
				.antMatchers(HttpMethod.POST, workstation_disassemble + ".AR").hasAuthority(actionRole(workstation_disassemble, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, workstation_disassemble + ".AC").hasAuthority(actionRole(workstation_disassemble, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, workstation_disassemble + ".AU").hasAuthority(actionRole(workstation_disassemble, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, workstation_disassemble + ".AD").hasAuthority(actionRole(workstation_disassemble, "AD"))// (移除)

				// ----請求-repair_code-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_code).hasAuthority(actionRole(repair_code, ""))//
				.antMatchers(HttpMethod.POST, repair_code + ".AR").hasAuthority(actionRole(repair_code, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, repair_code + ".AC").hasAuthority(actionRole(repair_code, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_code + ".AU").hasAuthority(actionRole(repair_code, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_code + ".AD").hasAuthority(actionRole(repair_code, "AD"))// (移除)

				// ----請求-repair_unit-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_unit).hasAuthority(actionRole(repair_unit, ""))//
				.antMatchers(HttpMethod.POST, repair_unit + ".AR").hasAuthority(actionRole(repair_unit, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, repair_unit + ".AC").hasAuthority(actionRole(repair_unit, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_unit + ".AU").hasAuthority(actionRole(repair_unit, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_unit + ".AD").hasAuthority(actionRole(repair_unit, "AD"))// (移除)

				// ----請求-repair_order_dtr-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_order_dtr).hasAuthority(actionRole(repair_order_dtr, ""))//
				.antMatchers(HttpMethod.POST, repair_order_dtr + ".AR").hasAuthority(actionRole(repair_order_dtr, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, repair_order_dtr + ".AC").hasAuthority(actionRole(repair_order_dtr, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_order_dtr + ".AU").hasAuthority(actionRole(repair_order_dtr, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_order_dtr + ".AD").hasAuthority(actionRole(repair_order_dtr, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, repair_order_dtr + ".S1").hasAuthority(actionRole(repair_order_dtr, "S1"))// (客製化-查詢)
				.antMatchers(HttpMethod.PUT, repair_order_dtr + ".S2").hasAuthority(actionRole(repair_order_dtr, "S2"))// (客製化-修改)

				// ----請求-repair_order_rma-(訪問) ----
//				.antMatchers(HttpMethod.POST, repair_order_rma).hasAuthority(actionRole(repair_order_rma, ""))//
//				.antMatchers(HttpMethod.POST, repair_order_rma + ".AR").hasAuthority(actionRole(repair_order_rma, "AR"))// (查詢)
//				.antMatchers(HttpMethod.POST, repair_order_rma + ".AC").hasAuthority(actionRole(repair_order_rma, "AC"))// (新增)
//				.antMatchers(HttpMethod.PUT, repair_order_rma + ".AU").hasAuthority(actionRole(repair_order_rma, "AU"))// (修改)
//				.antMatchers(HttpMethod.DELETE, repair_order_rma + ".AD").hasAuthority(actionRole(repair_order_rma, "AD"))// (移除)
//				.antMatchers(HttpMethod.POST, repair_order_rma + ".S1").hasAuthority(actionRole(repair_order_rma, "S1"))// (客製化-查詢)
//				.antMatchers(HttpMethod.PUT, repair_order_rma + ".S2").hasAuthority(actionRole(repair_order_rma, "S2"))// (客製化-修改)

				// ----請求-repair_history-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_history).hasAuthority(actionRole(repair_history, ""))//
				.antMatchers(HttpMethod.POST, repair_history + ".AR").hasAuthority(actionRole(repair_history, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, repair_history + ".AC").hasAuthority(actionRole(repair_history, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_history + ".AU").hasAuthority(actionRole(repair_history, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_history + ".AD").hasAuthority(actionRole(repair_history, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, repair_history + ".RT").hasAuthority(actionRole(repair_history, "AR"))// (報告)
				
				// ----請求-repair_list-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_list).hasAuthority(actionRole(repair_list, ""))//
				.antMatchers(HttpMethod.POST, repair_list + ".AR").hasAuthority(actionRole(repair_list, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, repair_list + ".AC").hasAuthority(actionRole(repair_list, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_list + ".AU").hasAuthority(actionRole(repair_list, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_list + ".AD").hasAuthority(actionRole(repair_list, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, repair_list + ".S1").hasAuthority(actionRole(repair_list, "S1"))// (客製化-查詢)
				.antMatchers(HttpMethod.PUT, repair_list + ".S2").hasAuthority(actionRole(repair_list, "S2"))// (客製化-修改)

				// ----請求-repair_rma_list_bat-(訪問) johnny---通用-(RMA售後)維修單
				.antMatchers(HttpMethod.POST, repair_rma_list_bat).hasAuthority(actionRole(repair_rma_list_bat, ""))//
				.antMatchers(HttpMethod.POST, repair_rma_list_bat + ".AR").hasAuthority(actionRole(repair_rma_list_bat, "AR"))// (查詢)
				.antMatchers(HttpMethod.PUT, repair_rma_list_bat + ".AU").hasAuthority(actionRole(repair_rma_list_bat, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_rma_list_bat + ".AD").hasAuthority(actionRole(repair_rma_list_bat, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, repair_rma_list_bat + ".S1").hasAuthority(actionRole(repair_rma_list_bat, "S1"))// (客製化-查詢)
				.antMatchers(HttpMethod.PUT, repair_rma_list_bat + ".S2").hasAuthority(actionRole(repair_rma_list_bat, "S2"))// (客製化-修改)				
				
				// ----請求-repair_list_bat-(訪問) ----
				.antMatchers(HttpMethod.POST, repair_list_bat).hasAuthority(actionRole(repair_list_bat, ""))//
				.antMatchers(HttpMethod.POST, repair_list_bat + ".AR").hasAuthority(actionRole(repair_list_bat, "AR"))// (查詢)
				.antMatchers(HttpMethod.PUT, repair_list_bat + ".AU").hasAuthority(actionRole(repair_list_bat, "AU"))// (修改)

				// ----請求-	repair_rma_list-(訪問) johnny----通用-(RMA售後)維修處理
				.antMatchers(HttpMethod.POST, repair_rma_list).hasAuthority(actionRole(repair_rma_list, ""))//					
				.antMatchers(HttpMethod.POST, repair_rma_list + ".AR").hasAuthority(actionRole(repair_rma_list, "AR"))// (查詢)
				//.antMatchers(HttpMethod.POST, repair_rma_list + ".AC").hasAuthority(actionRole(repair_rma_list, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, repair_rma_list + ".AU").hasAuthority(actionRole(repair_rma_list, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, repair_rma_list + ".AD").hasAuthority(actionRole(repair_rma_list, "AD"))// (移除)				
				.antMatchers(HttpMethod.POST, repair_rma_list + ".RT").hasAuthority(actionRole(repair_rma_list, "AR"))// (報告)				
				.antMatchers(HttpMethod.POST, repair_rma_list + ".S1").hasAuthority(actionRole(repair_rma_list, "S1"))// (客製化-查詢)
				.antMatchers(HttpMethod.PUT, repair_rma_list + ".S2").hasAuthority(actionRole(repair_rma_list, "S2"))// (客製化-新增/修改)
			
				// ----請求-label_list-(訪問) ----
				.antMatchers(HttpMethod.POST, label_list).hasAuthority(actionRole(label_list, ""))//
				.antMatchers(HttpMethod.POST, label_list + ".AR").hasAuthority(actionRole(label_list, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, label_list + ".AC").hasAuthority(actionRole(label_list, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, label_list + ".AU").hasAuthority(actionRole(label_list, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, label_list + ".AD").hasAuthority(actionRole(label_list, "AD"))// (移除)
				.antMatchers(HttpMethod.POST, label_list + ".S1").hasAuthority(actionRole(label_list, "S1"))// (客製化-查詢)
				.antMatchers(HttpMethod.PUT, label_list + ".S2").hasAuthority(actionRole(label_list, "S2"))// (客製化-修改/添加)
				.antMatchers(HttpMethod.PUT, label_list + ".S3").hasAuthority(actionRole(label_list, "S3"))// (客製化-列印)

				// ----請求-work_hours-(訪問) ----
				.antMatchers(HttpMethod.POST, work_hours).hasAuthority(actionRole(work_hours, ""))//
				.antMatchers(HttpMethod.POST, work_hours + ".AR").hasAuthority(actionRole(work_hours, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, work_hours + ".AC").hasAuthority(actionRole(work_hours, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, work_hours + ".AU").hasAuthority(actionRole(work_hours, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, work_hours + ".AD").hasAuthority(actionRole(work_hours, "AD"))// (移除)

				// ----請求-work_type-(訪問) ----
				.antMatchers(HttpMethod.POST, work_type).hasAuthority(actionRole(work_type, ""))//
				.antMatchers(HttpMethod.POST, work_type + ".AR").hasAuthority(actionRole(work_type, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, work_type + ".AC").hasAuthority(actionRole(work_type, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, work_type + ".AU").hasAuthority(actionRole(work_type, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, work_type + ".AD").hasAuthority(actionRole(work_type, "AD"))// (移除)

				// ----請求-work_type-(訪問) ----
				.antMatchers(HttpMethod.POST, own_user).hasAuthority(actionRole(own_user, ""))//
				.antMatchers(HttpMethod.POST, own_user + ".AR").hasAuthority(actionRole(own_user, "AR"))// (查詢)
				// .antMatchers(HttpMethod.POST, own_user +
				// ".AC").hasAuthority(actionRole(own_user, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, own_user + ".AU").hasAuthority(actionRole(own_user, "AU"))// (修改)
				// .antMatchers(HttpMethod.DELETE, own_user +
				// ".AD").hasAuthority(actionRole(own_user, "AD"))// (移除)
				
				// ----請求-	system_mail-(訪問) johnny---- 
				.antMatchers(HttpMethod.POST, system_mail).hasAuthority(actionRole(system_mail, ""))//
				.antMatchers(HttpMethod.POST, system_mail + ".AR").hasAuthority(actionRole(system_mail, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, system_mail + ".AC").hasAuthority(actionRole(system_mail, "AC"))// (新增)  			
				.antMatchers(HttpMethod.PUT, system_mail + ".AU").hasAuthority(actionRole(system_mail, "AU"))// (修改)  
				.antMatchers(HttpMethod.DELETE, system_mail + ".AD").hasAuthority(actionRole(system_mail, "AD"))// (移除) 
				
				// ----請求-work_type-(訪問) ----
				.antMatchers(HttpMethod.POST, customer).hasAuthority(actionRole(customer, ""))//
				.antMatchers(HttpMethod.POST, customer + ".AR").hasAuthority(actionRole(customer, "AR"))// (查詢)
				.antMatchers(HttpMethod.POST, customer + ".AC").hasAuthority(actionRole(customer, "AC"))// (新增)
				.antMatchers(HttpMethod.PUT, customer + ".AU").hasAuthority(actionRole(customer, "AU"))// (修改)
				.antMatchers(HttpMethod.DELETE, customer + ".AD").hasAuthority(actionRole(customer, "AD"))// (移除)

				// 請求需要檢驗-全部請求
				.anyRequest().authenticated();
		// 下列-登入位置
		http.formLogin()
				// 登入-預設登入頁面 預設帳密參數為(.usernameParameter(username).passwordParameter(password))
				.loginPage("/login.basil?status")
				// 登入-程序對象
				.loginProcessingUrl("/index.basil")
				// 登入-成功
				.successForwardUrl("/index.basil")
				// 登入-失敗
				.failureUrl("/login.basil?status=account or password incorrect!");
		// 下列-登出位置
		http.logout()
				// 登出-預設登入頁面
				.logoutUrl("/logout.basil")
				// 登出-程序對象
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
				// 登出-後轉跳
				.logoutSuccessUrl("/login.basil?status=You are exit!")
				// 登出-移除Session
				.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
				// 登出-移除Cookies
				.deleteCookies();
		// 關閉CSRF跨域
		// 在 Thymeleaf 或 JSP 中，Token 名稱與值可分別使用 ${_csrf.parameterName} 與 ${_csrf.token}
		// 來取得，發送請求時，必須得包含這個 Token，否則就會被拒絕請求。
		http.csrf().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		/***
		 * 測試兩組帳號-驗證資訊存放於記憶體-內存幫我們存放使用者的帳號及密碼 <br>
		 * PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		 * auth.inMemoryAuthentication().passwordEncoder(pwdEncoder).withUser("admin").password(pwdEncoder.encode("admin")).roles("ADMIN",
		 * "MEMBER");
		 * auth.inMemoryAuthentication().passwordEncoder(pwdEncoder).withUser("caterpillar")
		 * .password(pwdEncoder.encode("caterpillar")).roles("MEMBER");
		 */

		auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}

	/** 權限-規則-群組歸類 **/
	private String actionRole(String cell_who, String action_do) {
		// (sg_permission[特殊3(512),特殊2(256),特殊1(128),訪問(64),下載(32),上傳(16),新增(8),修改(4),刪除(2),查詢(1)])
		// 訪問
		String cell_role = cell_who.replace(".", "_").replace("/ajax/", "");
		String hasAuthority = cell_role + "_0001000000";
		// CRUD
		switch (action_do) {
		case "S3":
			hasAuthority = cell_role + "_1000000000"; // 特殊3
			break;
		case "S2":
			hasAuthority = cell_role + "_0100000000"; // 特殊2
			break;
		case "S1":
			hasAuthority = cell_role + "_0010000000"; // 特殊1
			break;
		case "FD":
			hasAuthority = cell_role + "_0000100000"; // 下載
			break;
		case "FU":
			hasAuthority = cell_role + "_0000010000"; // 上載
			break;
		case "AC":
			hasAuthority = cell_role + "_0000001000"; // 新增
			break;
		case "AU":
			hasAuthority = cell_role + "_0000000100"; // 修改
			break;
		case "AD":
			hasAuthority = cell_role + "_0000000010"; // 移除
			break;
		case "AR":
			hasAuthority = cell_role + "_0000000001"; // 查詢
			break;
		default:
			break;
		}
		System.out.println(cell_who + " " + hasAuthority);
		return hasAuthority;
	}
}
