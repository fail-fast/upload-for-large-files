package com.forfun.upload


import akka.actor._
import com.forfun.upload.fingerprint.Fingerprint
import com.forfun.upload.storage.{UploadFileInfo, Storage}
import org.apache.commons.io.IOUtils
import spray.can.Http
import scala.concurrent.duration._
import java.io._
import spray.http._
import spray.io.CommandWrapper
import scala.collection._
import com.forfun.upload.storage.StorageImplicits._


object UploadHandler{
  case class SendChunk(is: InputStream, off: Int)
  def props(client: ActorRef, start: ChunkedRequestStart): Props = Props(new UploadHandler(client, start))
}

/**
 * This upload handler is dealing specific with txt file. No validations so far
 * @param client
 * @param start
 */
class UploadHandler (client: ActorRef, start: ChunkedRequestStart) extends Actor with ActorLogging {
  import UploadHandler._

  /** cancel any timeout to keep the connection alive while chunks are coming*/
  client ! CommandWrapper(SetRequestTimeout(Duration.Inf))

  /** The upload file will come as chunks and saved in a temporary folder. Once the file is processed it is deleted */
  val uploadFile = File.createTempFile("upload-file-receiver", ".txt", new File("/tmp"))
  uploadFile.deleteOnExit()
  val output = new FileOutputStream(uploadFile)

  /** used to produce a hash/fingerprint from the uploaded file. It is used as identification to store the file */
  val fPrint = Fingerprint()


  def receive = {

    /** start receiving message chunks*/
    case c: MessageChunk =>
      /** read each chunk and save it in a file... does not keep in memory */
      val bytes = c.data.toByteArray
      IOUtils.write(bytes, output)
      // read the bytes to process a fingerprint using a rolling hash
      fPrint.pushBytes(bytes)

    /** Finish reading all chunks */
    case e: ChunkedMessageEnd =>
      output.close()

      val storage = implicitly[Storage]
      val fingerprint = fPrint.fingerprint

      /** If the same file is uploaded again, the service should NOT re-read the file and recount, but rather load the information from where its stored. */
      val uploadFileInfo = storage.get(fingerprint).getOrElse(processInfo(fingerprint, uploadFile))

      /** the response can be huge (The occurrences of each word) then it has to be sent like chunks to the client
        * starts with a partial json and then process the hasmap */
      client ! ChunkedResponseStart(
        HttpResponse(
          status = 200,
          entity = jsonResponseEntity(uploadFileInfo)
        )
      ).withAck(SendChunk(uploadFileInfo.content, 0))


    /** when finishing reading the json and sent all chunks */
    case SendChunk(is, -1) =>
      client ! ChunkedMessageEnd // tells to the client the chunks are done
      client ! CommandWrapper(SetRequestTimeout(2.seconds)) // reset timeout to original value
      uploadFile.delete()
      context.stop(self)

    /** if still content to be sent to the client */
    case SendChunk(is, off) =>
      val buffer = new Array[Byte](1024 * 8) // reads up to 8K from the persisted json(words)
      val off = is.read(buffer)

      client ! MessageChunk(buffer).withAck(SendChunk(is, off))

    /** in case connection is closed or lost */
    case x: Http.ConnectionClosed =>
      uploadFile.delete()
      client ! CommandWrapper(SetRequestTimeout(2.seconds)) // reset timeout to original value
      context.stop(self)

  }


  /**
   * Read the uploaded file and process the content adding in a hashmap to figure out
   * The occurrences of each word. This hashmap can take a big chunk on the memory,
   * specially dealing with several concurrent request for large files ~10MB.
   * This implementation is 'naive' right now, but let's talk about it.
   * Rely on the filesystem to help this map and aggregation? Using files to
   * help this and relief memory?
   * @param uploadFileFingerPrint
   * @param uploadFileRef
   * @param storage
   * @return
   */
  def processInfo(uploadFileFingerPrint: String, uploadFileRef: File)(implicit storage: Storage): UploadFileInfo = {

    var totalWords = 0
    val countWords =  mutable.Map.empty[String, Int]

    scala.io.Source.fromFile(uploadFileRef).getLines().foreach{ line =>
      line.split("[ !,.?;]+").foreach{ rawWord =>

        if(rawWord.trim.length > 0) {
          totalWords += 1
          val word = rawWord.toLowerCase
          val currentCount = if (countWords.contains(word)) countWords(word) else 0

          countWords += (word -> (currentCount + 1))
        }
      }

    }

    storage.put(uploadFileFingerPrint, totalWords, countWords.toMap)

  }

  /**
   * Indicate to the response it is a JSON
   * sends a partial json and the rest goes in chunks
   * @param info
   * @return
   */
  def jsonResponseEntity(info: UploadFileInfo) =
    HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = s""" {"fingerprint":"${info.fingerprint}","totalWords":${info.totalWords}, """ // partial json
    )


}
