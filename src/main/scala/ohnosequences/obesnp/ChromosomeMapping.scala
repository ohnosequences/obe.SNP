package ohnosequences.obesnp

/**
 * Created by evdokim on 29/10/2015.
 */

trait ChromosomeNaming {
  def decode(name: String): String
  def encode(name: String): String
}

object OneThousandGenomes extends ChromosomeNaming {
  override def decode(name: String): String = {
    "chr" + name
  }

  override def encode(name: String): String = {
    name.replace("chr", "").replace("ch", "")
  }

}

