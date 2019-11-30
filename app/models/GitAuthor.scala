package models

case class GitAuthor(name: String, email: String) {
  override def toString: String = s"$name - $email"
}

object GitAuthor {
  private def clearAuthorName(name: String): String = name.replace("Author: ", "").trim

  private def clearAuthorEmail(email: String): String = email.replaceAll("\\>","")

  def fromString(commitLogAuthor: String): GitAuthor = {
    val listAuthorCommit = commitLogAuthor.split("<").toList
    listAuthorCommit match {
      case h :: t :: Nil => GitAuthor(clearAuthorName(h), clearAuthorEmail(t))
      case _ => throw new Exception(s"Failed parting author name $listAuthorCommit")
    }
  }
}

