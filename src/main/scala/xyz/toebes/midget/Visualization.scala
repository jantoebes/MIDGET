package xyz.toebes.midget

import kantan.codecs.Result
import spray.json.DefaultJsonProtocol._
import spray.json._
import xyz.toebes.midget.importer.{ AbnLine, AegonLine }
import xyz.toebes.midget.output.Outputter

import scala.reflect.ClassTag

object Date {

}

object Visualization {
  implicit val ord = Ordering.by(Date.unapply)

  case class Date(year: Int, month: Int, day: Int)

  case class Transaction(date: Date, account: String, balance: BigDecimal)

  case class Identifier(date: String, account: String)

  case class Serie(name: String, data: Seq[Transaction])

  implicit val dateFormat = jsonFormat3(Date)
  implicit val transactionFormat = jsonFormat3(Transaction)

  val total: String = "total"

  def handleErrors[T: ClassTag](items: Seq[Result[_, T]]): Seq[T] = {
    val errors: Seq[_] = items.collect { case kantan.codecs.Result.Failure(a) ⇒ a }
    val success: Seq[T] = items.collect { case kantan.codecs.Result.Success(a) ⇒ a }

    if (errors.nonEmpty) {
      Seq.empty
    } else {
      success.toSeq
    }
  }

  def reportAsCSV() = {
    val series: Seq[Serie] = getTransactions

    Outputter.printText("xls", "data", series.find(_.name.equalsIgnoreCase(total)).get.data.map(item => {
      s"${item.account},${item.date.year}-${item.date.month}-${item.date.day},${item.balance}"
    }).mkString(System.lineSeparator()), "csv")
  }

  def getTransactions: Seq[Serie] = {
    val abnTransactions = AbnLine.readTransactions.getOrElse(throw new IllegalStateException("Error in visualisation"))
    val aegonTransactions = AegonLine.readTransactions.getOrElse(throw new IllegalStateException("Error in visualisation"))

    val transactions: Seq[Transaction] = (abnTransactions ++ aegonTransactions).sortBy(_.date)

    val accounts: Seq[String] = transactions.map(_.account).distinct.sorted

    val transactionDates: Seq[Date] = transactions.map(_.date).distinct.sorted

    val accountTransactionsMultiPerDate: Map[String, Seq[Transaction]] =
      transactions
        .sortBy(_.date)
        .groupBy(_.account)

    val accountTransactions: Map[String, Seq[Transaction]] =
      accountTransactionsMultiPerDate
        .mapValues(
          _.groupBy(_.date)
            .map(_._2.last)
            .toList
            .sortBy(_.date)
        )

    val transactionsSummed = total -> getAccountsTotal(accountTransactions, transactionDates, accounts)

    val allTransactions = accountTransactions + transactionsSummed

    val summarized = allTransactions.mapValues(valuesForAccount => {
      val firstAccountValuesForMonth: Seq[Transaction] = valuesForAccount.groupBy(_.date.month).mapValues(_.head).values.toSeq
      val lastTransaction = valuesForAccount.sortBy(_.date).last

      (firstAccountValuesForMonth :+ lastTransaction).sortBy(_.date)
    })

    val y = summarized
      .map(item => Serie(item._1, item._2))
      .toList

    y
  }

  def getAccountsTotal(accountTransactions: Map[String, Seq[Transaction]], transactionDates: Seq[Date], accounts: Seq[String]) = {
    case class FoldData(latestAccountBalance: Map[String, BigDecimal] = Map.empty[String, BigDecimal], result: Seq[Transaction] = Seq.empty)

    def getFirstValue(items: Seq[Transaction]) = items.sortBy(_.date).head.balance
    transactionDates
      .foldLeft(FoldData(latestAccountBalance = accountTransactions.mapValues(getFirstValue)))((foldData: FoldData, date: Date) => {
        val accountsBalance = accounts
          .map((account: String) => {
            val transactions = accountTransactions(account)

            val balanceForDate = transactions.find(item => item.date == date).map(_.balance)

            (account, balanceForDate.getOrElse(foldData.latestAccountBalance(account)))
          }).toMap

        val transactions = foldData.result :+ Transaction(date, total, accountsBalance.values.sum)

        FoldData(accountsBalance, transactions)
      }).result.sortBy(_.date)
  }
}
