package dtri.com.tw.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemPermission;
import dtri.com.tw.login.LoginUserDetails;

public abstract class AbstractController {

	private String SYS_C;
	private String SYS_F;

	public AbstractController(String SYS_F) {
		this.SYS_C = this.getClass().getSimpleName();
		this.SYS_F = SYS_F;
	}

	abstract String access(@RequestBody String json_object);

	abstract String search(@RequestBody String json_object);

	abstract String modify(@RequestBody String json_object);

	abstract String delete(@RequestBody String json_object);

	/** 取得<該功能> 功能 名稱 */
	public String getSYS_F() {
		System.out.println("---controller -function:[" + SYS_F + "] Check");
		return SYS_F;
	}

	/** 取得<該功能> Class 名稱 */
	public String getSYS_C() {
		System.out.println("---controller -class: [" + SYS_C + "] Check");
		return SYS_C;
	}

	/** 取得<該功能> Class & Function 名稱 */
	public void showSYS_CM(String SYS_M) {
		System.out.println("---controller : [" + SYS_C + "][" + SYS_M + "] Check");
	}

	/** 取得<該功能> 傳送參數 */
	public void show(String msg) {
		System.out.println(msg);
	}

	/** 取得<User權限> */
	public LoginUserDetails loginUser() {
		// 取得-當前用戶資料
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			LoginUserDetails userDetails = (LoginUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			// Step1.查詢資料權限
			return userDetails;
		} else {
			return null;
		}
	}

	/** 取得<UI權限> */
	public SystemPermission permissionUI() {
		// Step0.取得-當前用戶資料
		LoginUserDetails lud = loginUser();
		if (lud != null) {
			List<SystemGroup> systemGroup = lud.getSystemGroup();
			// Step1.查詢資料權限
			SystemPermission permission = new SystemPermission();
			String sYS_F = this.SYS_F;
			systemGroup.forEach(p -> {
				if (p.getSystemPermission().getSpcontrol().equals(sYS_F)) {
					permission.setSppermission(p.getSgpermission());
				}
			});
			return permission;
		} else {
			return null;
		}
	}
}
