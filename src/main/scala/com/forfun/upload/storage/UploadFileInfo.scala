package com.forfun.upload.storage

import java.io.InputStream

case class UploadFileInfo(fingerprint: String, totalWords: Int, content: InputStream)
