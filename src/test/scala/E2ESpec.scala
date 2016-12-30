import java.io.File
import java.nio.charset.StandardCharsets

import com.typesafe.config.ConfigFactory
import org.scalatest._
import xyz.toebes.midget.BaseApp
import xyz.toebes.midget.config.Config

class E2ESpec extends FlatSpec with Matchers with AppendedClues {

  case class Setup(setName: String){
    def baseUrl = "src/test/data/" + setName + "/"
    val expectedOutput = baseUrl + "expected/output/"
    val expectedErrors = baseUrl + "expected/errors/"
    val output = baseUrl + "output/output/"
    val errors = baseUrl + "output/errors/"

    def init = {
      System.setProperty("baseUrl", baseUrl)
      ConfigFactory.invalidateCaches()
      getListOfFiles(output).map(_.delete())
      this

    }
  }

  it should "succes" in {
    val setup = Setup("succes").init

    BaseApp.run

    check(setup)
  }

  it should "failed" in {
    val setup = Setup("failed").init

    BaseApp.run

    check(setup)
  }

  def check(setup: Setup) = {
    doCheck(setup.output, setup.expectedOutput)
    doCheck(setup.errors, setup.expectedErrors)
  }

  def doCheck(actual: String, expected:String): Unit ={
    getListOfFiles(actual).length should equal (getListOfFiles(expected).length)

    for(file <- getListOfFiles(expected).map(_.getName)){
      val expectedContent = scala.io.Source.fromFile(expected + file).mkString.trim
      val actualContent = scala.io.Source.fromFile(actual + file).mkString.trim
      actualContent should equal(expectedContent) withClue("Filename: " + file)
    }
  }

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

}