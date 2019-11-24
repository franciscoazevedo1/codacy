package models

import java.time.LocalDateTime

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, JsPath, JsValue, Json, Reads}
import play.api.libs.ws.WSRequest
import scalaj.http.Http

import scala.concurrent.Future

case class GitHubAnswer(name: String, email: String, date: String, sha: String, message: String) {
  def toGitCommitLog: Option[GitCommitLog] = {
    val gitAuthor = GitAuthor(name, email)
    val gitCommit = GitCommit(sha, None)
    val dateR: LocalDateTime = LocalDateTime.parse(date)
    Some(GitCommitLog(None, gitCommit, gitAuthor, dateR, Some(message)))
  }

  def fromString(string: String) =
    Json.parse(string).validate[List[JsValue]].asOpt.get
}

object GitHubAnswer {
  implicit val reads: Reads[GitHubAnswer] = (
    (JsPath \"commit" \ "author" \ "name").read[String] and
      (JsPath \ "commit" \ "author" \ "email").read[String] and
      (JsPath \ "commit" \ "author" \ "date").read[String] and
      (JsPath \"sha").read[String] and
      (JsPath \ "commit" \ "message").read[String]
    )(GitHubAnswer.apply _)
}

case class GitHubAnswerList(list: List[GitHubAnswer]) {
  override def toString: String = list.mkString("--")
}


object GitHubAnswerList {
  implicit val reads: Reads[GitHubAnswerList] =
    (JsPath \ "").read[List[GitHubAnswer]].map(GitHubAnswerList.apply(_))
}
case class GitHubApiRequest(owner: String, repo: String) {
  val gitHubListCommitsUrls = s"https://api.github.com/repos/$owner/$repo/commits"

}

object GitHubApi {
  def fromString(gitHubUrl: String): Option[GitHubApiRequest] = {
    val gitHubUrlToList: List[String] = gitHubUrl.split(":").last.split("/").toList
    gitHubUrlToList match {
      case owner :: repo :: Nil => Some(GitHubApiRequest(owner, repo.replace(".git", "")))
      case _ => None
    }

  }
}

