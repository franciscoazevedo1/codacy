package controllers

import controllers.HomeController.SearchForm
import javax.inject._
import models.GitCommitLog1
import play.api.data.Form
import play.api.mvc._
import play.api.data.Forms._
import play.api.i18n.I18nSupport

import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc)  with I18nSupport{
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

  def search = Action { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => BadRequest(s"OLA $errorForm"),
      searchWord =>
        Ok(views.html.searchResults(GitCommitLog1.begin(searchWord.gitHubUrl)))
    )
  }

}

object HomeController {
  case class SearchForm(gitHubUrl: String)
}
