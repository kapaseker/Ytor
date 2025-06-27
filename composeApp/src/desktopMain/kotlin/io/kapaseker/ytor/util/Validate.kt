package io.kapaseker.ytor.util

import java.net.URI
import java.net.URISyntaxException

fun String.isValidHttpUrl(): Boolean {
    // 基本格式检查
    if (!this.matches("^(https?://).*".toRegex(RegexOption.IGNORE_CASE))) {
        return false
    }

    // 特殊字符检查（不允许空格、中文等）
    if (this.contains(" ")) return false

    try {
        // 使用URI类验证结构
        val uri = URI(this)

        // 验证协议必须为http或https
        if (uri.scheme != "http" && uri.scheme != "https") {
            return false
        }

        // 验证至少包含域名（host部分）
        if (uri.host.isNullOrBlank()) {
            return false
        }

        // 可选：验证端口范围（0-65535）
        uri.port.takeIf { it != -1 }?.let {
            if (it < 0 || it > 65535) return false
        }

        return true
    } catch (e: URISyntaxException) {
        return false
    } catch (e: Exception) {
        return false
    }
}