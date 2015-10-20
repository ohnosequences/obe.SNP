package ohnosequences.obesnp

/**
 * Created by evdokim on 20/10/2015.
 */
trait SNP {
  def name: String
}

trait SNPPositon {
  def startPosition: Long

  //def endPosition: Long

  def referenceValue: String
}
