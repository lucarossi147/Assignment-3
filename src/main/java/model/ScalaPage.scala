package model

case class ScalaPage(page: String) {
  /**
   * remove unwanted words from page and return all the other words as a List of String
   *
   * @param unwantedWords words to delete from string
   * @return string without unwantedWords
   */
  def getRelevantWords(unwantedWords: Set[String]): Seq[String] = {
      page.toLowerCase
        .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
        .split("\\s+")
        .filterNot(w => unwantedWords.contains(w)||w.isEmpty)
  }
}
