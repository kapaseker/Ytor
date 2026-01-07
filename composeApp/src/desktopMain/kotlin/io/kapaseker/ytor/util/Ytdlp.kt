package io.kapaseker.ytor.util

object YtdlpCommand {

}

private class Command {

    class Builder {
        private val commands = mutableMapOf<String, String>()
        fun path(dir: String): Builder = this.apply {
            commands.put("-P", dir)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}

object Ytdlp {

    fun download(
        url: String,
        dir: String,
    ) {
//        Runtime.getRuntime().exec(
//            arrayOf(
//                "yt-dlp",
//                "-P",
//                saveOption,
//                "-f",
//                "best[height<=1080][ext=mp4]/best[ext=mp4]/best",
//                input
//            )
//        )
//
//        good.inputStream.bufferedReader().forEachLine {
//            println(it)
//        }
    }
}