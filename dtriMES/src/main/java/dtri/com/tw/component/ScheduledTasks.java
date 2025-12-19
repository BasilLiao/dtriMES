package dtri.com.tw.component;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dtri.com.tw.service.OqcReviewFormService;
import dtri.com.tw.service.ProductionDailyService;
import dtri.com.tw.service.ProductionDailyYieldService;

//https://polinwei.com/spring-boot-scheduling-tasks/
@Component
public class ScheduledTasks {
	/// 標準範例
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	@Autowired
	ProductionDailyService pdyService;
	@Autowired
	ProductionDailyYieldService pdyYieldService;
	@Autowired
	OqcReviewFormService oqcRviewFormService;

	@Scheduled(fixedDelay = 600000)
	public void autoDaily() {
		logger.info("===fixedDelay:pdyService.updateData(): 時間:{}", dateFormat.format(new Date()));
		pdyService.updateData();
	}

	@Scheduled(cron = "0 30 19 * * ?")
	public void runEveryDayAt730PM() {
		pdyYieldService.getData(); //每日不良率
		System.out.println("每天晚上 19:30 執行的任務：" + new java.util.Date());
	}

	//OQC 每周五 18:00 寄送 OQC已結單的資料
	@Scheduled(cron = "0 00 18 ? * FRI")
	public void runWEveryFridayAt1800PM() {
		oqcRviewFormService.getDataOqc(); //每周已審核清單
		System.out.println("每周五晚上 18:00 執行的任務：" + new java.util.Date());
	}
		
	// fixedDelay = 60000 表示當前方法執行完畢 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
	// @Scheduled(fixedDelay = 60000)
	public void testFixDelay() {
		logger.info("===fixedDelay: 時間:{}", dateFormat.format(new Date()));
	}

	// fixedRate = 60000 表示當前方法開始執行 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
	// @Scheduled(fixedRate = 60000)
	public void testFixedRate() {
		logger.info("===fixedRate: 時間:{}", dateFormat.format(new Date()));
	}

	// initialDelay = 180000 表示延遲 180000 (3秒) 執行第一次任務, 然後每 5000ms(5 秒) 再次呼叫該方法
	// @Scheduled(initialDelay = 180000, fixedRate = 5000)
	public void testInitialDelay() {
		logger.info("===initialDelay: 時間:{}", dateFormat.format(new Date()));
	}

	// cron接受cron表示式，根據cron表示式確定定時規則
	// @Scheduled(cron = "0 0/1 * * * ?")
	public void testCron() {
		logger.info("===cron: 時間:{}", dateFormat.format(new Date()));

	}

}