import java.time.LocalDateTime

import models.{GitAuthor, GitCommit, GitCommitLog}
import org.scalatestplus.play.PlaySpec

import scala.concurrent.{ExecutionContext}


class GitCommitSpec extends PlaySpec {
  val commit2 = GitCommit("hash", Some(List("merge", "to")))
  val commitFromString = GitCommit.fromString("commit 1f8bd6df15b6878b7332573f23b92a306e6fb38e (jsofjio)")
  "A GitCommit" must {
    "contain an hash" in {
      commitFromString.hash mustBe "1f8bd6df15b6878b7332573f23b92a306e6fb38e"
    }

    "should contain a list of tags" in {
      commitFromString.tags.get mustBe List("jsofjio")
    }
  }
  val gitAuthor = GitAuthor.fromString("Author: franciscoazevedo <franciscoovazevedo@gmail.com>")

  "A GitAuthor" must {
    "have an author name" in {
      gitAuthor.get.name mustBe "franciscoazevedo"
    }

    "have an author email" in {
      gitAuthor.get.email mustBe "franciscoovazevedo@gmail.com"
    }
  }

  val commitLog = GitCommitLog.fromString(
    "commit f165636b725802fc970ada6857621edfe2425d9c (HEAD -> phase1)" +
    "\nAuthor: franciscoazevedo <franciscoovazevedo@gmail.com>" +
    "\nDate:   Sun Nov 24 16:37:56 2019 +0000" +
    "\n\n    solved FINALLY\n"
  )

  "A gitCommitLog" must {
    "A commitLog should be defined" in {
      commitLog.isDefined mustBe true
    }

    "A commitLog should have a commit with the right hash" in {
      commitLog.get.commit.hash mustBe "f165636b725802fc970ada6857621edfe2425d9c"
    }

    "A commitLog should have a commit with the right tags" in {
      commitLog.get.commit.tags mustBe Some(List("HEAD -> phase1"))
    }

    "A commitLog should have a date" in {
      commitLog.get.date mustBe(GitCommitLog.dateFromCommitString("Date:   Sun Nov 24 16:37:56 2019 +0000"))
    }

    "The commit message should be equal to the one from the commit string" in {
      commitLog.get.description.get mustBe("solved FINALLY")
    }
  }
}

