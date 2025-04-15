
package dtri.com.tw.service;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class BasicNotificationMailService {

//@Autowired
//BasicNotificationMailDao notificationMailDao;

@Autowired
private JavaMailSender mailSender;

private final Logger logger = LoggerFactory.getLogger(this.getClass());

// 寄信  

public boolean sendEmail(String[] toUser, String[] toCcUser, String subject, String bodyHtml, String bnmattname, byte[] bnmattcontent) {
//	這是 核心的郵件發送方法，它的參數：
//	toUser：主要收件人 (必填)。
//	toCcUser：副本收件人 (可選)。
//	subject：郵件主旨。
//	bodyHtml：郵件的 HTML 內容。
//	bnmattname：附件名稱。
//	bnmattcontent：附件內容（byte 陣列格式）。	
boolean sendOK = true;
	try {
	// 簡單版mail
//      SimpleMailMessage message = new SimpleMailMessage();
//      message.setTo(to);
//      message.setSubject(subject);
//      message.setText(bodyHtml);
//      mailSender.send(message);      
	if (toUser.length > 0) {
		// 創建 MimeMessage 物件      //透過 mailSender.createMimeMessage() 建立郵件物件。
		MimeMessage message = mailSender.createMimeMessage();
		
		// 使用 MimeMessageHelper 來設置消息內容和屬性        
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//		使用 MimeMessageHelper 來幫助 設定郵件內容。
//		true 表示 允許 HTML 格式。
//		"UTF-8" 設定 郵件編碼，確保不會亂碼。
		
		
		// 設置收件人、主題、以及內容        
		helper.setTo(toUser);
		if (toCcUser.length > 0 && !toCcUser[0].equals("")) {
			helper.setCc(toCcUser);
		}
		helper.setSubject(subject);
		// 設置 HTML 格式的內容        
		helper.setText(bodyHtml, true);
		// 附件?        
		if (bnmattcontent != null && !bnmattname.equals("") && bnmattname != null) {
			// 使用 ByteArrayDataSource 將 byte[] 包裝成資料來源          
			ByteArrayDataSource dataSource = new ByteArrayDataSource(bnmattcontent, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			helper.addAttachment(bnmattname, dataSource);
		}
//		如果有附件：
//		ByteArrayDataSource 用來 包裝 byte[] 為附件。
//		helper.addAttachment() 添加附件。
				
		// 發送郵件        
		mailSender.send(message);
		}
	} catch (Exception e) {
		System.out.println(e);
		logger.error(e.toString());
		sendOK = false;
	}	
	return sendOK;
}
	// 檢查信件   這個方法用來 檢查資料庫中是否有尚未發送的郵件，並嘗試發送。
//	public void readySendCheckEmail() {
//		// 尚未寄信件    從資料庫讀取 尚未發送 (false) 的郵件。 
//		ArrayList<BasicNotificationMail> mails = notificationMailDao.findAllByCheck(null,null,null,null,false,null,null);
//		// 遍歷未發送的郵件 使用 forEach 逐封郵件處理。
//		mails.forEach(m -> {
//			//格式化收件人  將收件人與副本收件人 轉換成陣列格式。
//			String[] toUsers = m.getBnmmail().replace("[","").replace("]","").replaceAll(" ","").split(",");
//			String[] toCcUsers = m.getBnmmailcc().replace("[","").replace("]","").replaceAll(" ","").split(",");
//			// 傳送   sendEmail() 發送郵件，並根據發送結果 更新郵件狀態。   
//			boolean ok = this.sendEmail(toUsers, toCcUsers, m.getBnmtitle(), m.getBnmcontent(), m.getBnmattname(),m.getBnmattcontent());
//			m.setBnmsend(ok);    // 成功?/失敗?    
//		});
//		// 更新標記存入    將發送狀態更新回資料庫。
//		notificationMailDao.saveAll(mails);
//	}
}