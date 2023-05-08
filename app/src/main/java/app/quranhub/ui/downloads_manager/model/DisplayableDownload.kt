package app.quranhub.ui.downloads_manager.model

class DisplayableDownload {

    var name: String
    var downloadedAmount: String? = null
    var isDownloadable = true
    var isDeletable = false

    constructor(name: String) {
        this.name = name
    }

    constructor(
        name: String, downloadedAmount: String?,
        downloadable: Boolean, deletable: Boolean
    ) {
        this.name = name
        this.downloadedAmount = downloadedAmount
        isDownloadable = downloadable
        isDeletable = deletable
    }

    override fun toString(): String {
        return "DisplayableDownload{" +
                "name='" + name + '\'' +
                ", downloadedAmount='" + downloadedAmount + '\'' +
                ", downloadable=" + isDownloadable +
                ", deletable=" + isDeletable +
                '}'
    }
}