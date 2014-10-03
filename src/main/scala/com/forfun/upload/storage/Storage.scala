package com.forfun.upload.storage

import java.io.{FileInputStream, FileOutputStream, File}

import com.typesafe.scalalogging.slf4j.{Logging}
import org.apache.commons.io.IOUtils


import scala.collection.mutable


trait Storage{

  def get(id: String): Option[UploadFileInfo]

  def put(id: String, totalWords: Int, counts: Map[String, Int]): UploadFileInfo

}

object StorageImplicits{

  implicit object InMemory extends Storage with Logging{
    import spray.json._
    import DefaultJsonProtocol._
    val storage =  mutable.Map.empty[String, UploadFileInfo]

    /**
     * get the file info in case it was processed before.
     * It loads the partial json information from a file which contains all
     * occurrences of each word. This inputstream is used to send back
     * chunks
     * @param id
     * @return
     */
    override def get(id: String): Option[UploadFileInfo] = {
      storage.get(id).map{ ufi =>
        val jsonFile = new File(s"/tmp/$id.json")
        ufi.copy(content = new FileInputStream(jsonFile))
      }
    }

    override def put(id: String, totalWords: Int, counts: Map[String, Int]): UploadFileInfo = {

      val jsonFile = new File(s"/tmp/$id.json")
      val output = new FileOutputStream(jsonFile)

      //FIXME it is a possible slow point for huge maps
      IOUtils.write(JsObject("counts" -> counts.toJson).compactPrint.substring(1) ,output)

      val info = UploadFileInfo(id, totalWords, new FileInputStream(jsonFile))
      storage.put(id, info)
      info
    }
  }

}