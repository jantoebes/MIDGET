package xyz.toebes.midget

import java.util.Calendar

import com.barbarysoftware.watchservice.WatchRun

object BaseApp extends WatchRun {
  def download = {
    Console.print("Import ABN? (Y)")
    val importAbn = scala.io.StdIn.readChar().toUpper == 'Y'
    Console.print("Import AEGON? (Y)")
    val importAegon = scala.io.StdIn.readChar().toUpper == 'Y'

    if (importAbn) {
      Console.println("Importing ABN...")
      Downloader.downloadAbn
    }

    if (importAegon) {
      Console.println("Importing AEGON...")
      Downloader.downloadAegon
    }
  }

  def run(): Unit = {
    Console.println(s"Starting new run ${Calendar.getInstance().getTime}...")

    Console.println("Classifying ...")
    Classification.classify

    Console.println("Report for excel ...")
    Visualization.reportAsCSV()

    Console.println(s"Done")
  }
}
