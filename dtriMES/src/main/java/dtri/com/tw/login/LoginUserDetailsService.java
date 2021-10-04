package dtri.com.tw.login;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.db.pgsql.dao.SystemGroupDao;
import dtri.com.tw.db.pgsql.dao.SystemUserDao;

@Service
public class LoginUserDetailsService implements UserDetailsService {

	@Autowired
	private SystemUserDao userDao;

	@Autowired
	private SystemGroupDao groupDao;

	// ---登入驗證---[Spring 自動呼叫]
	@Override
	public UserDetails loadUserByUsername(String account) {
		// Step1.取得user
		SystemUser user = userDao.findBySuaccount(account);
		if (user == null) {
			throw new UsernameNotFoundException(account);
		}
		// Step2.取得group+Permission
		List<SystemGroup> systemGroups = groupDao.findBySggidOrderBySggid(user.getSusggid());
		if (systemGroups == null) {
			throw new UsernameNotFoundException(account);
		}

		// Step3.檢查-驗證使用者 密碼前面加上"{noop}"使用NoOpPasswordEncoder，也就是不對密碼進行任何格式的編碼。

		// 測試用
		/*
		 * UserBuilder builder_u = User.builder();
		 * builder_u.username(user.getSuaccount()); builder_u.password("{noop}" +
		 * user.getSupassword()); builder_u.roles(user.getSuposition() + "");
		 * UserDetails test = builder_u.build();
		 */
		// return test;

		// Step4.登記-可使用權限
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (SystemGroup systemGroup : systemGroups) {
			System.out.println("-------------" + systemGroup.getSystemPermission().getSpcontrol() + "-------------");
			System.out.println("-------------" + systemGroup.getSgpermission() + "-------------");
			char[] ch = systemGroup.getSgpermission().toCharArray();
			// 共10位數登記(單元+權限)
			for (int i = 9; i >= 0; i--) {
				if (ch[i] == '1') {
					int move_p = 9 - i;
					double now_p = Math.pow(10, move_p);
					String now_s = String.format("%010d", (int) now_p);
					String role = systemGroup.getSystemPermission().getSpcontrol().replace(".", "_") + '_' + now_s;
					System.out.println(role);
					authorities.add(new SimpleGrantedAuthority(role));
				} else {
					continue;
				}
			}
		} // Step5.註冊-使用者
		UserDetails userDetails = new LoginUserDetails(user, systemGroups, authorities);
		return userDetails;
	}

	public ArrayList<SystemUser> loadUserAll() {
		ArrayList<SystemUser> users = userDao.findAll();
		if (users == null) {
			throw new UsernameNotFoundException("Can't get all user!!");
		}
		return users;
	}
}