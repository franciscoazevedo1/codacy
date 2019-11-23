package models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import sys.process._

import models.errors.ErrorParsingCommit

case class GitCommitLog(merge: Option[String], commit: GitCommit,  author: GitAuthor, date: LocalDateTime, description: Option[String])

object GitCommitLog1 {
  private def dateFromCommitString(dateString: String): LocalDateTime = {
    val DATE_FORMAT = "E MMM dd HH:mm:ss yyyy Z"
    val LOCAL_DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern(DATE_FORMAT)
    val cleanDateString = dateString.replaceAll("Date:   ", "")
    LocalDateTime.parse(cleanDateString, LOCAL_DATE_TIME_FORMATTER)
  }

  private def descriptionFromCommitString(description: String): String =
    description.trim

  private def mergeFromCommitString(mergeString: String): Option[String] =
    Some(mergeString.replace("Merge: ", ""))

  def fromOriginalString(originalCommitsString: String): List[GitCommitLog] = {
    val listOfCommits = originalCommitsString.trim.split("commit").filterNot(_ == "").toList
    listOfCommits.map { string => fromString(string) }
  }

  private def removeDir(gitUrl: String) = {
    val folderName = gitUrl.split("\\/").last.replace(".git", "")
    s"cd ../".#&&(s"rm -rf $folderName") !
  }

  def fromGitUrl(gitUrl: String) = {
    val folderName = gitUrl.split("\\/").last.replace(".git", "")
    s"cd ../".#&&(s"git clone $gitUrl").#&&(s"git log $folderName") !!
  }

  def begin(gitUrl: String) = {
    val listOfCommits = fromOriginalString(fromGitUrl(gitUrl))
    listOfCommits
  }

  private def fromString(commitString: String): GitCommitLog = {
    val commitInList = commitString.split("\n").filterNot(_ == "").toList

    commitInList match {
      case merge :: hash :: author :: date :: description :: Nil =>
        GitCommitLog(
          mergeFromCommitString(merge),
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(description))
        )

      case hash :: author :: date :: description :: Nil =>
        GitCommitLog(
          None,
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(description))
        )

      case hash :: author :: date :: Nil =>
        GitCommitLog(
          None,
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          None
        )

      case _ => throw new ErrorParsingCommit
    }
  }
}