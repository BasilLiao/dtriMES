package dtri.com.tw.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.SocketException;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dtri.com.tw.bean.FtpUtilBean;
import dtri.com.tw.tools.Fm_Time;

@Service
public class FtpService {

	/**
	 * https://www.itread01.com/content/1541594827.html 獲取FTPClient物件
	 *
	 * @param ftpHost     FTP主機伺服器
	 * @param ftpPassword FTP 登入密碼
	 * @param ftpUserName FTP登入使用者名稱
	 * @param ftpPort     FTP埠 預設為21
	 * @return
	 */
	Logger logger = LoggerFactory.getLogger(FTPClient.class);

	private static FTPClient getFTPClient(FTPClient ftpClient, String ftpHost, String ftpUserName, String ftpPassword, int ftpPort) {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ftpHost, ftpPort);// 連線FTP伺服器
			ftpClient.login(ftpUserName, ftpPassword);// 登陸FTP伺服器
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				System.out.println("未連線到FTP，使用者名稱或密碼錯誤。");
				ftpClient.disconnect();
			} else {
				System.out.println("FTP連線成功。");
			}
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("FTP的IP地址可能錯誤，請正確配置。");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("FTP的埠錯誤,請正確配置。");
		}
		return ftpClient;
	}

	/***
	 * 從FTP伺服器下載檔案
	 * 
	 * @param ftpHost     FTP IP地址
	 * @param ftpUserName FTP 使用者名稱
	 * @param ftpPassword FTP使用者名稱密碼
	 * @param ftpPort     FTP埠
	 * @param ftpPath     FTP伺服器中檔案所在路徑 格式： ftptest/aa
	 * @param localPath   下載到本地的位置 格式：H:/download
	 * @param fileName    檔名稱
	 */
	public void downloadFtpFile(FtpUtilBean ftp) {

		FTPClient ftpClient = new FTPClient();

		try {
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			ftpClient.setControlEncoding("UTF-8"); // 中文支援
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftp.getFtpPath());

			File localFile = new File(ftp.getLocalPath() + File.separatorChar + ftp.getFileName());
			OutputStream os = new FileOutputStream(localFile);
			ftpClient.retrieveFile(ftp.getFileName(), os);
			os.close();
			ftpClient.logout();

		} catch (FileNotFoundException e) {
			System.out.println("沒有找到" + ftp.getFtpPath() + "檔案");
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("連線FTP失敗.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("檔案讀取錯誤。");
			e.printStackTrace();
		}

	}

	/**
	 * Description: 向FTP伺服器上傳檔案
	 * 
	 * @param ftpHost     FTP伺服器hostname
	 * @param ftpUserName 賬號
	 * @param ftpPassword 密碼
	 * @param ftpPort     埠
	 * @param ftpPath     FTP伺服器中檔案所在路徑 格式： ftptest/aa
	 * @param fileName    ftp檔名稱
	 * @param input       檔案流
	 * @return 成功返回true，否則返回false
	 */
	public boolean uploadFile(FtpUtilBean ftp) {
		boolean success = false;
		FTPClient ftpClient = new FTPClient();
		try {
			int reply;
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				return success;
			}
			ftpClient.setControlEncoding("UTF-8"); // 中文支援
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftp.getFtpPath());

			ftpClient.storeFile(ftp.getFileName(), ftp.getInput());

			ftp.getInput().close();
			ftpClient.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return success;
	}

	/**
	 * PLT 檢查+回傳資料
	 *
	 ***/
	public JSONObject getLogPLT(FTPClient ftpClient, FtpUtilBean ftp, String work_use, String searchName[], boolean plt_file_classify) {
		System.out.println(new Date());
		JSONObject content = new JSONObject();
		try {
			// 登入 如果採用預設埠，可以使用ftp.connect(url)的方式直接連線FTP伺服器
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			// 設定檔案傳輸型別為二進位制+UTF-8 傳輸
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.setControlEncoding("UTF-8");
			// 獲取ftp登入應答程式碼
			int reply = ftpClient.getReplyCode();
			// 驗證是否登陸成功
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				System.err.println("FTP server refused connection.");
				return null;
			}
			// 轉移到FTP伺服器目錄至指定的目錄下
			ftpClient.changeWorkingDirectory(new String(ftp.getRemotePath().getBytes("UTF-8"), "iso-8859-1"));

			// 獲取檔案列表(查詢)
			JSONArray archives = new JSONArray();
			JSONObject one = new JSONObject();
			FTPFile[] fs = ftpClient.listFiles();

			for (FTPFile ff : fs) {
				String[] f_n = ff.getName().split("_");

				// ========比對_查詢條件/符合條件========
				if ((searchName[0].equals("") || f_n[0].indexOf(searchName[0]) != -1) && (searchName[1].equals("") || f_n[1].indexOf(searchName[1]) != -1)
						&& (searchName[2].equals("") || f_n[2].indexOf(searchName[2]) != -1)) {

					// ======== 建立輩分資料夾========
					makeDirectories(ftpClient, ftp.getRemotePathBackup() + searchName[0]);

					// ========製令單 如果有OQC 排除========
					if (ff.getName().indexOf("OQC") != -1) {
						//Date updateDate = ff.getTimestamp().getTime();
						String file_name_OQC = ff.getName();
						String dirPath = ftp.getRemotePathBackup() + searchName[0];
						String re_path = ftp.getRemotePath() + "/" + file_name_OQC;
						String new_path = dirPath + "/" + file_name_OQC + "_" + new Date().getTime() + "_" + work_use;
						archives.put(new JSONObject().put("re_path", re_path).put("new_path", new_path));
						// ftpClient.rename(re_path, new_path);
						continue;
					}
					ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
					byteArray.reset();
					ftpClient.retrieveFile(ff.getName(), byteArray);
					BufferedReader bufferedReader = new BufferedReader(new StringReader(byteArray.toString("UTF-8")));

					// ========第一行抓取+檢驗 (檔頭BOM)去除 是否為正確========
					String line = new String(), line_all = new String();
					line = bufferedReader.readLine();
					line_all = bufferedReader.readLine();
					if (line != null && line.charAt(0) != '{') {
						line = line.substring(1);
					}

					// ========取得所有內容========
					StringBuilder everything = new StringBuilder();
					while ((line_all = bufferedReader.readLine()) != null) {
						everything.append(line_all);
						everything.append(System.lineSeparator());
					}

					// 字串 轉 JSON 如果異常為空
					try {
						new JSONObject(line);
					} catch (Exception ex) {
						// ========(如果格式不對)轉移檔案========
						if (plt_file_classify) {
							//Date updateDate = ff.getTimestamp().getTime();
							String dirPath = ftp.getRemotePathBackup() + searchName[0];
							String re_path = ftp.getRemotePath() + "/" + ff.getName();
							String new_path = dirPath + "/" + //
									ff.getName().replace(".log", "").replace(".txt", "") + "_" + //
									new Date().getTime() + "_" + work_use + "_bad.log";
							archives.put(new JSONObject().put("re_path", re_path).put("new_path", new_path));
						}
						continue;
					}

					// ========轉移檔案登記========
					Date updateDate = ff.getTimestamp().getTime();
					String new_path = "";
					if (plt_file_classify) {
						String dirPath = ftp.getRemotePathBackup() + searchName[0];
						String re_path = ftp.getRemotePath() + "/" + ff.getName();
						new_path = dirPath + "/" + //
								ff.getName().replace(".log", "").replace(".txt", "") + "_" + //
								new Date().getTime() + "_" + work_use + ".log";
						archives.put(new JSONObject().put("re_path", re_path).put("new_path", new_path));
					}

					// ========補型號/補主機板號/轉16進制========
					long file_size = ff.getSize();
					one = new JSONObject(line);
					one.put("ph_pr_id", one.has("WorkOrder") ? one.getString("WorkOrder") : "");
					if (one.has("UUID")) {
						String mbSn[] = (one.getString("UUID").split("-"));
						one.put("MB(UUID)", mbSn[mbSn.length - 1]);
					} else {
						one.put("MB(UUID)", "");
					}
					one.put("ph_model", "");
					one.put("pb_sn", one.has("SN") ? one.getString("SN") : "");
					one.put("pb_l_size", file_size);
					one.put("pb_l_text", everything.toString());
					one.put("pb_l_path", new_path);
					one.put("check", true);
					one.put("pb_l_dt", Fm_Time.to_yMd_Hms(updateDate));
					content = one;
					// ========關閉串流========
					bufferedReader.close();
					byteArray.close();
					byteArray = null;
				}
			}
			content.put("pb_l_files", archives);
			ftpClient.logout();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		System.out.println(new Date());
		return content;
	}

	// 歸檔
	public boolean logPLT_Archive(FTPClient ftpClient, FtpUtilBean ftp, JSONArray archive_files) {
		System.out.println(new Date());
		try {
			// 登入 如果採用預設埠，可以使用ftp.connect(url)的方式直接連線FTP伺服器
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			// 設定檔案傳輸型別為二進位制+UTF-8 傳輸
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.setControlEncoding("UTF-8");
			// 獲取ftp登入應答程式碼
			int reply = ftpClient.getReplyCode();
			// 驗證是否登陸成功
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				System.err.println("FTP server refused connection.");
				return false;
			}
			// 轉移到FTP伺服器目錄至指定的目錄下
			ftpClient.changeWorkingDirectory(new String(ftp.getRemotePath().getBytes("UTF-8"), "iso-8859-1"));

			for (Object one_file : archive_files) {
				JSONObject json = (JSONObject) one_file;
				String re_path = json.getString("re_path");
				String new_path = json.getString("new_path");

				if (!ftpClient.rename(re_path, new_path)) {
					logger.error("to Backup OK!!");
				}

			}

			System.out.println(new Date());
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	/**
	 * Creates a nested directory structure on a FTP server 創建資料夾
	 * 
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param dirPath   Path of the directory, i.e /projects/java/ftp/demo
	 * @return true if the directory was created successfully, false otherwise
	 * @throws IOException if any error occurred during client-server communication
	 */
	public static boolean makeDirectories(FTPClient ftpClient, String dirPath) throws IOException {
		boolean created = ftpClient.makeDirectory(dirPath);

		return created;
	}
}