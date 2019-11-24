package controllers

import java.time.LocalDateTime

import controllers.HomeController.SearchForm
import javax.inject._
import models.{GitAuthor, GitCommit, GitCommitLog, GitHubAnswer, GitHubAnswerList, GitHubApi}
import play.api.data.Form
import play.api.mvc._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(ws: WSClient, cc : ControllerComponents) extends AbstractController(cc)  with I18nSupport{
  val searchForm: Form[SearchForm] = Form(
    mapping(
      "gitHubUrl" -> text
    )(SearchForm.apply)(SearchForm.unapply)
  )
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request  =>
    Ok(views.html.index(searchForm))
  }

  def search = Action.async { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => Future.successful(BadRequest(s"Error in the form: $errorForm")),
      searchWord => {
        for{
          a <- ws.url(GitHubApi.fromString(searchWord.gitHubUrl).get.gitHubListCommitsUrls).get()
          as = a.json.validateOpt[JsValue]
          list  = a.json.as[List[JsValue]].map { js =>
            Some(GitCommitLog(
              None,
              GitCommit((js \ "sha").as[String], None),
              GitAuthor((js \ "commit" \ "author" \ "name").as[String], (js \ "commit" \ "author" \ "email").as[String]),
              (js \ "commit" \ "author" \ "date").as[LocalDateTime],
              Some((js \ "commit" \ "message").as[String])
            ))
          }
          _ = list.foreach(println)
        } yield Ok(views.html.searchResults(list))

      }
    )
  }

}

object HomeController {
  case class SearchForm(gitHubUrl: String)
}
