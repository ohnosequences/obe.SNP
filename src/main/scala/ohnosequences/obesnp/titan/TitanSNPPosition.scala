package ohnosequences.obesnp.titan

import com.tinkerpop.blueprints.Edge
import ohnosequences.obesnp.SNPPositon

/**
 * Created by evdokim on 20/10/2015.
 */
object TitanSNPPosition {
  val label = "SNP_POS"
  val startProperty = "SNP_POS_START"
  val referenceValueProperty = "SNP_POS_REF"
}

class TitanSNPPosition(edge: Edge) extends SNPPositon {

  override def startPosition: Long = {
    edge.getProperty(TitanSNPPosition.startProperty)
  }

  override def referenceValue: String = {
    edge.getProperty(TitanSNPPosition.referenceValueProperty)

  }
}