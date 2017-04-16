package xyz.toebes.midget

import java.util.logging.{ Level, Logger }

import com.barbarysoftware.watchservice.BarbaryWatch

object App {
  def main(args: Array[String]): Unit = {
    try {
      Console.println("|========|")
      Console.println("| MIDGET |")
      Console.println("|========|")
      Console.println("")

      BaseApp.download

      BaseApp.run
      val runnable = BarbaryWatch.start(BaseApp)

      Console.println("Press any key to exit")
      scala.io.StdIn.readLine()
      BarbaryWatch.stop(runnable)
    } catch {
      case ex: Exception => {
        throw ex
      }
    }
  }

}
