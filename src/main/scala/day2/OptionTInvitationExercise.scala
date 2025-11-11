package day2

import cats.data.OptionT
import day2.OptionTInvitationExercise.Invitation

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * 演習のゴール:
  *   1. Future[Option[A]] データ源を OptionT に持ち上げる
  *   2. for-comprehension で複数の OptionT を合成する
  *   3. OptionT#value で Future[Option[A]] に戻して副作用を観察する
  */
object OptionTInvitationExercise extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  final case class User(id: Long, email: String, companyId: Option[Long])
  final case class Company(id: Long, domain: String, industry: String)
  final case class Template(industry: String, body: String)
  final case class Invitation(to: String, body: String)

  private val userTable: Map[Long, User] = Map(
    1L -> User(1L, "lena@example.com", Some(101L)),
    2L -> User(2L, "kai@example.com", Some(102L)),
    3L -> User(3L, "solo@example.com", None)
  )

  private val companyTable: Map[Long, Company] = Map(
    101L -> Company(101L, "lunatech.dev", "fintech"),
    102L -> Company(102L, "stellar.health", "healthcare")
  )

  private val templateTable: Map[String, Template] = Map(
    "fintech" -> Template("fintech", "Fintech launch invite for {{email}}"),
    "healthcare" -> Template("healthcare", "Healthcare webinar invite for {{email}}")
  )

  def fetchUser(id: Long): Future[Option[User]] =
    Future.successful(userTable.get(id))

  def fetchCompany(id: Long): Future[Option[Company]] =
    Future.successful(companyTable.get(id))

  def fetchTemplate(industry: String): Future[Option[Template]] =
    Future.successful(templateTable.get(industry))

  // --- TODO 1 ---------------------------------------------------------------
  // Future[Option[User]] と Future[Option[Company]] を OptionT で合成し、
  // (User, Company) を返す関数を実装してください。
  // エラー処理は OptionT に任せ、値が無ければ None に落としてください。
  def loadUserCompany(userId: Long): OptionT[Future, (User, Company)] =
    for {
      user <- OptionT(fetchUser(userId))
      companyId <- OptionT.fromOption[Future](user.companyId)
      company <- OptionT(fetchCompany(companyId))
    } yield (user, company)

  // --- TODO 2 ---------------------------------------------------------------
  // loadUserCompany の結果を使い、テンプレートを読み込んで Invitation を作成してください。
  // 文字列テンプレート中の {{email}} をユーザのメールに置き換える処理も実装します。
  def draftInvitation(userId: Long): OptionT[Future, Invitation] =
    for {
      (user, company) <- loadUserCompany(userId)
      template <- OptionT(fetchTemplate(company.industry))
    } yield Invitation(
      to = user.email,
      body = template.body.replace("{{email}}", user.email)
    )

  // --- TODO 3 ---------------------------------------------------------------
  // 任意の userId を変えて挙動を観察できるようにしてください。
  // draftInvitation の結果を Await.result で取り出し、標準出力に表示します。
  val previewUserId: Long = 2L
  val preview: OptionT[Future, Invitation] = draftInvitation(previewUserId)
  println(Await.result(preview.value, 1.second))
}
