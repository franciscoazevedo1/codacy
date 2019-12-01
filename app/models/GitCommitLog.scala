package models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import sys.process._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.Try
case class GitCommitLog(merge: Option[String], commit: GitCommit,  author: GitAuthor, date: Option[LocalDateTime], description: Option[String])

object GitCommitLog {
  def dateFromCommitString(dateString: String): Option[LocalDateTime] = {
    val DATE_FORMAT = "E MMM d HH:mm:ss yyyy Z"
    val LOCAL_DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern(DATE_FORMAT)
    val cleanDateString = dateString.replaceAll("Date:   ", "")
    Try {
      LocalDateTime.parse(cleanDateString, LOCAL_DATE_TIME_FORMATTER)
    }.toOption
  }

  private def descriptionFromCommitString(description: List[String]): String =
    description.mkString(" ")

  private def mergeFromCommitString(mergeString: String): Option[String] =
    Some(mergeString.replace("Merge: ", ""))

  def fromOriginalString(originalCommitsString: String): List[Option[GitCommitLog]] = {
    val listOfCommits = originalCommitsString.trim.split("commit").filterNot(_ == "").toList.take(100) // to keep the same amount of logs that come from GitHubApi
    listOfCommits.map { string =>
      fromString(string) }
  }

  private def removeDir(gitUrl: String) = {
    val folderName = gitUrl.split("\\/").last.replace(".git", "")
    s"cd ../".#&&(s"rm -rf $folderName") !
  }

  def fromGitUrl(gitUrl: String) = {
      val folderName = gitUrl.split("\\/").last.replace(".git", "")
      Future.successful(s"git clone $gitUrl".#&&(s"git --git-dir $folderName/.git log") !!)

  }

  def start(gitUrl: String) = {
    (for {
      commits <- fromGitUrl(gitUrl)
      listOfCommits = fromOriginalString(commits)
      _ = removeDir(gitUrl)
    } yield listOfCommits).recover{
      case e: Throwable =>
        removeDir(gitUrl)
        println(e.getStackTrace + e.getMessage + e.printStackTrace())
        throw e
    }

  }

  def fromString(commitString: String): Option[GitCommitLog] = {
    val commitInList = commitString.split("\n").filterNot(_ == "").toList.map{_.trim}.filterNot(_ == "")

    commitInList match {
      case hash :: merge :: author :: date :: tail if !commitInList.filter(_.contains("Merge")).isEmpty =>
        Some(GitCommitLog(
          mergeFromCommitString(merge),
          GitCommit.fromString(hash),
          GitAuthor.fromString(author).getOrElse(GitAuthor("None", "none")),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(tail))
        ))

      case hash :: author :: date :: tail  =>
        Some(GitCommitLog(
          None,
          GitCommit.fromString(hash),
          GitAuthor.fromString(author).getOrElse(GitAuthor("None", "none")),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(tail))
        ))

      case _ =>
        println(s"ERROR Parsing $commitString")
        None
    }

  }

}