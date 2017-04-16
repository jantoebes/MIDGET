package xyz.toebes.midget

import java.io.{ File, FilenameFilter }
import java.text.SimpleDateFormat
import java.util.Calendar

import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.commons.io.FileUtils
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeOptions }
import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.WebBrowser
import org.scalatest.time.{ Millis, Seconds, Span }

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap

object Downloader extends WebBrowser with Eventually with ShouldMatchers {
  private val aegonDir = """/Users/Jan/Google Drive/App/midget/data/aegon"""
  private val abnDir = """/Users/Jan/Google Drive/App/midget/data/abn"""

  val filePatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(150, Millis)))

  def downloadAegon = {
    FileUtils.cleanDirectory(new File(aegonDir))

    downloadAegonPdfs(ConfigFactory.load().getConfig("aegon-credentials"))
  }

  def downloadAbn = {
    FileUtils.cleanDirectory(new File(abnDir))

    downloadAbnCsvs(ConfigFactory.load().getConfig("abn-credentials"))
  }

  private def downloadAbnCsvs(config: Config) = {
    implicit val webDriver = chromeDriver(abnDir)
    implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(100, Millis)))

    try {
      implicitlyWait(Span(20, Seconds))

      go to "https://www.abnamro.nl/nl/paymentsreporting/downloadmutations/customer159080983.html"
      waitForReady

      click on cssSelector(".mlf-js-cookie-accept")
      reloadPage
      waitForReady

      click on cssSelector(".mcf-loginSofttoken")
      waitForReady

      eventually { textField(cssSelector(".mcf-accountnumber")).isDisplayed should be(true) }

      textField(cssSelector(".mcf-accountnumber")).value = config.getString("accountnumber")
      textField(cssSelector(".mcf-cardnumber")).value = config.getString("cardnumber")
      textField(cssSelector("input[name=login-pincode-0]")).value = config.getString("pincode")(0).toString
      textField(cssSelector("input[name=login-pincode-1]")).value = config.getString("pincode")(1).toString
      textField(cssSelector("input[name=login-pincode-2]")).value = config.getString("pincode")(2).toString
      textField(cssSelector("input[name=login-pincode-3]")).value = config.getString("pincode")(3).toString
      textField(cssSelector("input[name=login-pincode-4]")).value = config.getString("pincode")(4).toString

      click on cssSelector(".mcf-button-submit")

      eventually { cssSelector("input[name=selectAllTables]").element.isDisplayed should be(true) }

      click on cssSelector("input[name=selectAllTables]")

      click on id("periodType1")

      textField(cssSelector("""input[name='mutationsDownloadSelectionCriteriaDTO\.bookDateFromDay']""")).value = "01"
      textField(cssSelector("""input[name='mutationsDownloadSelectionCriteriaDTO\.bookDateFromMonth']""")).value = "01"
      textField(cssSelector("""input[name='mutationsDownloadSelectionCriteriaDTO\.bookDateFromYear']""")).value = "2016"

      singleSel(cssSelector("""[name='mutationsDownloadSelectionCriteriaDTO\.fileFormat']""")).value = "1"

      click on cssSelector("""[name=btOk]""")

      eventually {
        new File(abnDir).list(new FilenameFilter() {
          def accept(dir: File, name: String) = name.toLowerCase().endsWith(".tab")
        }).length should be(1)
      }(filePatienceConfig)

      Thread.sleep(1000)
    } finally {
      webDriver.close()
    }
  }

  private def downloadAegonPdfs(config: Config) = {
    implicit val webDriver = chromeDriver(aegonDir)

    try {
      implicitlyWait(Span(10, Seconds))

      go to "https://www.aegon.nl/sso-box/wayf.html?int_source=/inloggen"
      click on id("accept-cookie-choice")
      reloadPage()

      textField(id("username")).value = config.getString("username")
      pwdField(id("password")).value = config.getString("password")
      click on cssSelector(".btn,.btn-green-icon-lock")

      downloadAegonPdf("Wl_lQTU7-X3cUUjS6vPfoQ2")
      downloadAegonPdf("BnZrJbEXoM4VH8jkWYqrDQ2")
      downloadAegonPdf("HvIMAlyYgbtQdlgIXPtn7Q2")

      eventually {
        new File(aegonDir).list(new FilenameFilter() {
          def accept(dir: File, name: String) = name.toLowerCase().endsWith(".pdf")
        }).length should be(3)
      }(filePatienceConfig)
    } finally {
      webDriver.close()
    }

    def downloadAegonPdf(link: String) = {
      go to "https://www.aegon.nl/mijnaegon/mijnbank"
      switch to frame("mijnbank")
      cssSelector(s"tr[data-link='$link']").webElement.click()
      waitForReady

      click on id("ddpPeriod_class")
      textField(id("ddpPeriod-from")).value = "01-01-2016"
      textField(id("ddpPeriod-to")).value = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime)
      click on id("btn-filter-ddpPeriod")
      waitForReady
      cssSelector("#download-overview").webElement.click()
      Thread.sleep(5000)
      println(s"SUCCESS DOWNLOADED $link")

    }
  }

  private def waitForReady(implicit webDriver: WebDriver) = {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(100, Millis)))
    eventually {
      executeScript("return document.readyState =='complete' && jQuery.active == 0") should equal(true)
    }
  }

  private def chromeDriver(dir: String) = {
    System.setProperty("webdriver.chrome.args", "--disable-logging");
    System.setProperty("webdriver.chrome.silentOutput", "true");

    val options = new ChromeOptions()

    options.setExperimentalOption("prefs", HashMap[String, Any](
      "profile.default_content_settings.popups" -> 0,
      "download.default_directory" -> dir
    ).asJava)

    options.addArguments("--test-type");
    options.addArguments("-incognito");

    new ChromeDriver(options)
  }
}
