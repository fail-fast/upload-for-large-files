package com.forfun.upload.fingerprint

import org.rabinfingerprint.fingerprint.RabinFingerprintLong
import org.rabinfingerprint.polynomial.Polynomial


/**
 * Using a rolling hash to generate a fingerprint of the upload file.
 * The fingerprint is the file identifier used to reload the information
 * in case the file was processed before
 * see https://github.com/themadcreator/rabinfingerprint
 */
class Fingerprint {

  private [this] val polynomial = Polynomial.createFromLong(14037737891124947L)
  private [this] val rabin = new RabinFingerprintLong(polynomial)

  def pushBytes(bytes: Array[Byte]) = rabin.pushBytes(bytes)

  def fingerprint: String = java.lang.Long.toString(rabin.getFingerprintLong, 16)

}


object Fingerprint{
  def apply() = new Fingerprint()
}


