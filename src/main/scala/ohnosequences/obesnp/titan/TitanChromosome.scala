package ohnosequences.obesnp.titan

import scala.collection.JavaConversions._
import com.thinkaurelius.titan.core.{TitanGraph}
import com.tinkerpop.blueprints.{Compare, Direction, Vertex}
import ohnosequences.obesnp.{Gene, Chromosome}



object TitanChromosomeEdge {
  val label = "CHR_E"
  val nameProperty = "CHR_E_NAME"
}

object TitanChromosome {
  val label = "CHR"
  val nameProperty = "CHR_NAME"
}

class TitanChromosome(graph: TitanGraph, val vertex: Vertex) extends Chromosome {
  override def name: String = vertex.getProperty(TitanChromosome.nameProperty)


  override def toString: String = {
    "ch" + name
  }

  def getGenes: List[Gene] = {
    vertex.query()
      .labels(TitanGene.label)
      .direction(Direction.OUT).vertices().toList
      .map(new TitanGene(graph, _))
  }

  def findNearestStart(start: Long): Option[TitanGene] = {
    val edge = vertex.query()
      .labels(TitanGenePosition.label)
      .direction(Direction.OUT)
      .has(TitanGenePosition.startProperty, Compare.LESS_THAN_EQUAL, start)
      .limit(1)
      .edges().headOption
    edge.map { e =>
      val pos = new TitanGenePosition(graph, e)
      println("found position: " + pos)
      pos.gene
    }

  }

  def addSNP(start: Long, referenceValue: String, snp: TitanSNP): Unit = {

    val r = GlobalBench.bench.bench("querySNPByPos") {
      vertex.query()
      .labels(TitanSNPPosition.label)
      .direction(Direction.OUT)
      .has(TitanSNPPosition.startProperty, start)
      .edges().headOption
    }
    r match {
      case None => {
        GlobalBench.bench.bench("createSNPPos") {
          val positionEdge = vertex.addEdge(TitanSNPPosition.label, snp.vertex)
          positionEdge.setProperty(TitanSNPPosition.startProperty, start)
          positionEdge.setProperty(TitanSNPPosition.referenceValueProperty, referenceValue)
        }
      }
      case Some(pos) => //already added
    }
  }

//  def addSNP(start: Long, referenceValue: String, snp: TitanSNP): Unit = {
//
//    val r = GlobalBench.bench.bench("querySNPByPos") {
//      vertex.query()
//        .labels(TitanSNPPosition.label)
//        .direction(Direction.OUT)
//        .has(TitanSNPPosition.startProperty, start)
//        .edges().headOption
//    }
//    r match {
//      case None => {
//        GlobalBench.bench.bench("createSNPPos") {
//          val positionEdge = vertex.addEdge(TitanSNPPosition.label, snp.vertex)
//          positionEdge.setProperty(TitanSNPPosition.startProperty, start)
//          positionEdge.setProperty(TitanSNPPosition.referenceValueProperty, referenceValue)
//        }
//      }
//      case Some(pos) => //already added
//    }
//  }

  def addPosition(start: Long, end: Long, gene: Vertex): Unit = {
    vertex.query()
      .labels(TitanGenePosition.label)
      .direction(Direction.OUT)
      .has(TitanGenePosition.startProperty, start)
      .edges().headOption match {
      case None => {
        val positionEdge = vertex.addEdge(TitanGenePosition.label, gene)
        positionEdge.setProperty(TitanGenePosition.startProperty, start)
        positionEdge.setProperty(TitanGenePosition.endProperty, end)
      }
      case Some(pos) => {
        //println("already added")
      }
    }
  }

}
