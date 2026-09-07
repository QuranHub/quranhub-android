package app.quranhub.ui.mushaf.model

import app.quranhub.data.local.entity.TranslationBook

class DisplayableTranslation(translationBook: TranslationBook) {

    var translationBook: TranslationBook

    init {
        this.translationBook = TranslationBook(
            translationBook.id,
            translationBook.name,
            translationBook.author,
            translationBook.language,
            translationBook.fileDownloadPath,
            translationBook.databaseName,
            translationBook.downloadStatus,
            translationBook.downloadLevelPercentage
        )
    }

    var id: String
        get() = translationBook.id
        set(id) {
            translationBook.id = id
        }

    var name: String
        get() = translationBook.name
        set(name) {
            translationBook.name = name
        }

    var author: String
        get() = translationBook.author
        set(author) {
            translationBook.author = author
        }

    var language: String
        get() = translationBook.language
        set(language) {
            translationBook.language = language
        }

    var fileDownloadPath: String
        get() = translationBook.fileDownloadPath
        set(fileDownloadPath) {
            translationBook.fileDownloadPath = fileDownloadPath
        }

    var databaseName: String
        get() = translationBook.databaseName
        set(databaseName) {
            translationBook.databaseName = databaseName
        }

    var downloadStatus: Int
        get() = translationBook.downloadStatus
        set(downloadStatus) {
            translationBook.downloadStatus = downloadStatus
        }

    var downloadLevelPercentage: Int
        get() = translationBook.downloadLevelPercentage
        set(downloadLevelPercentage) {
            translationBook.downloadLevelPercentage = downloadLevelPercentage
        }

    override fun equals(other: Any?): Boolean {
        return if (other is DisplayableTranslation) {
            id == other.id &&
                downloadStatus == other.downloadStatus &&
                downloadLevelPercentage == other.downloadLevelPercentage
        } else false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + downloadStatus
        result = 31 * result + downloadLevelPercentage
        return result
    }

    override fun toString(): String {
        return "DisplayableTranslation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", language='" + language + '\'' +
                ", fileDownloadPath='" + fileDownloadPath + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", downloadLevelPercentage=" + downloadLevelPercentage +
                '}'
    }
}