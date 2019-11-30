package controllers


import com.typesafe.play.cachecontrol.ResponseServeActions.Validate
import controllers.HomeController.SearchForm
import javax.inject._
import models.errors.ErrorParsingCommit
import models.{GitCommitLog, GitHubAnswer, GitHubApi}
import play.api.cache.AsyncCacheApi
import play.api.data.Form
import play.api.mvc._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


@Singleton
class HomeController @Inject()(ws: WSClient, cc : ControllerComponents, cache: AsyncCacheApi) extends AbstractController(cc) with I18nSupport{
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
      errorForm => Future.successful(BadRequest(s"Error in the form: $errorForm")),
      searchWord => {
        (for{

            a <- ws.url(GitHubApi.fromString(searchWord.gitHubUrl).get.gitHubListCommitsUrls).get()
            list = a.json.as[List[GitHubAnswer]].map { _.toGitCommitLog }
            _ = cache.set("searchResults", list)
          } yield Ok(views.html.searchResults(list))
         ).recoverWith {
          case _=>
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
