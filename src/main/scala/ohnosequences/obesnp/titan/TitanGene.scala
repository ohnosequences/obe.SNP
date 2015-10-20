package ohnosequences.obesnp.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Vertex
import ohnosequences.obesnp.Gene

import scala.collection.JavaConversions._

object TitanGene {
  val label = "GENE"
  val uniprotIDProperty = "GENE_UNIPROT_ID"
  val geneSymbolProperty = "GENE_SYMBOL"
  val geneIDProperty = "GENE_ID"
  val uniprotACProperty = "GENE_UNIPROT_AC"
  val mimProperty = "GENE_MIM"


  def byGeneID(graph: TitanGraph, geneID: Long): Option[TitanGene] = {
    val it = graph.query()
      .has("label", TitanGene.label)
      .has(geneIDProperty, geneID)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }

  def byGeneSymbol(graph: TitanGraph, geneSymbol: String): Option[TitanGene] = {
    val it = graph.query()
      .has("label", TitanGene.label)
      .has(TitanGene.geneSymbolProperty, geneSymbol)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }

  def byUniprotAC(graph: TitanGraph, ac: String): Option[TitanGene] = {
    val it = graph.query()
      .has("label", TitanGene.label)
      .has(TitanGene.uniprotACProperty, ac)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanGene(graph, it.next()))
    } else {
      None
    }

  }
}

class TitanGene(graph: TitanGraph, val vertex: Vertex) extends Gene {

  override def symbol: String = {
    vertex.getProperty(TitanGene.geneSymbolProperty)
  }

  def mim: String = {
    vertex.getProperty(TitanGene.mimProperty)
  }

  def geneIDs: List[java.lang.Long] = {
    val raw: java.util.ArrayList[java.lang.Long] = vertex.getProperty(TitanGene.geneIDProperty)
    raw.toList
  }

  def uniprotACs: List[String] = {
    val raw: java.util.ArrayList[String] = vertex.getProperty(TitanGene.uniprotACProperty)
    raw.toList
  }

  def uniprotId: String = {
    vertex.getProperty(TitanGene.uniprotIDProperty)
  }

  override def toString: String = {
    "Gene(" + symbol + ", GeneID:" + geneIDs + ", " + uniprotId + ", " + mim + ")"
  }
}
