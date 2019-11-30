package controllers


import controllers.HomeController.SearchForm
import javax.inject._
import models.errors.Errors.{ErrorInForm, ErrorParsingCommit, ErrorParsingSearch}
import models.{GitCommitLog, GitHubAnswer, GitHubApi, GitHubApiRequest}
import play.api.cache.AsyncCacheApi
import play.api.data.Form
import play.api.mvc._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


@Singleton
class HomeController @Inject()(ws: WSClient, cc : ControllerComponents) extends AbstractController(cc) with I18nSupport{
  type ListCommitLogs = List[Option[GitCommitLog]]

  val searchForm: Form[SearchForm] = Form(
    mapping(
      "gitHubUrl" -> text
    )(SearchForm.apply)(SearchForm.unapply)
  )

  object ActionExceptionHandler extends ActionBuilder[Request, AnyContent]  {
    override protected def executionContext: ExecutionContext = ExecutionContext.global

    override def parser: BodyParser[AnyContent] = parse.anyContent

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      block(request).recover {
        case e: ErrorParsingCommit => BadRequest(e.msg)
        case e: ErrorInForm => BadRequest(s"${e.msg} - ${e.errorForm.errors}")
        case e: ErrorParsingSearch => BadRequest(e.msg)
        case e: Throwable =>
          println(s"${e.getStackTrace}")
          InternalServerError("Something went wrong")
      }
    }
  }

  def index = Action { implicit request  =>
    Ok(views.html.index(searchForm))
  }

  def search = ActionExceptionHandler.async { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => Future.failed(throw ErrorInForm(errorForm = errorForm)),
      searchWord => {
        val parsedSearchWord: Future[GitHubApiRequest] = GitHubApi.fromString(searchWord.gitHubUrl) match {
          case Some(r) => Future.successful(r)
          case None => Future.failed(throw new ErrorParsingSearch)
        }
        (for{
            searchRepoName <- parsedSearchWord
            a <- ws.url(searchRepoName.gitHubListCommitsUrls).get()
            list = a.json.as[List[GitHubAnswer]].map { _.toGitCommitLog }
          } yield Ok(views.html.searchResults(list))
         ).recoverWith {
          case _ =>
            for {
              listOfCommits <- GitCommitLog.start(searchWord.gitHubUrl)
            } yield Ok(views.html.searchResults(listOfCommits))

        }

      }
    )
  }
}

object HomeController {
  case class SearchForm(gitHubUrl: String)
}
