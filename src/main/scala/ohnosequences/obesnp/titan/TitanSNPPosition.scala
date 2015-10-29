package ohnosequences.obesnp.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.{Direction, Edge}
import ohnosequences.obesnp.SNPPositon

/**
 * Created by evdokim on 20/10/2015.
 */
object TitanSNPPosition {
  val label = "SNP_POS"
  val startProperty = "SNP_POS_START"
  val referenceValueProperty = "SNP_POS_REF"
}

class TitanSNPPosition(graph: TitanGraph, edge: Edge) extends SNPPositon {

  override def startPosition: Long = {
    edge.getProperty(TitanSNPPosition.startProperty)
  }

  override def referenceValue: String = {
    edge.getProperty(TitanSNPPosition.referenceValueProperty)
  }

  def snp: TitanSNP = {
    new TitanSNP(graph, edge.getVertex(Direction.IN))
  }

  def chromosome: TitanChromosome = {
    new TitanChromosome(graph, edge.getVertex(Direction.OUT))
  }
}