package ohnosequences.obesnp.titan

import scala.collection.JavaConversions._

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.{Direction, Vertex}
import ohnosequences.obesnp.{Chromosome, Reference}

object TitanReference {
  val label = "REF"
  val nameProperty = "REF_NAME"

  def createReference(graph: TitanGraph, id: String): TitanReference = {
    val reference = graph.addVertexWithLabel(TitanReference.label)
    reference.addProperty(nameProperty, id)
    new TitanReference(graph, id, reference)
  }

  def getOrCreateReference(graph: TitanGraph, id: String): TitanReference = {
    apply(graph, id) match {
      case Some(ref) => ref
      case None => createReference(graph, id)
    }
  }

  def apply(titan: TitanGraph, id: String): Option[TitanReference] = {
    val vertex = titan.query().has("label", TitanReference.label).has(TitanReference.nameProperty, id).limit(1).vertices().headOption
    vertex.map(new TitanReference(titan, id, _))
  }
}

class TitanReference(graph: TitanGraph, id: String, val vertex: Vertex) extends Reference {

  def getChromosome(name: String): Option[TitanChromosome] = {
    val iterator = vertex
      .query()
      .labels(TitanChromosomeEdge.label)
      .direction(Direction.OUT)
      .has(TitanChromosomeEdge.nameProperty, name)
      .vertices()
      .iterator()
    if (iterator.hasNext) {
      Some(new TitanChromosome(graph, iterator.next()))
    } else {
      None
    }
  }

  def getOrCreateChromosome(name: String): TitanChromosome = {
    getChromosome(name).getOrElse(createChromosome(name))
  }

  override def getChromosomes: List[Chromosome] = {
    vertex.query()
      .labels(TitanChromosome.label)
      .direction(Direction.OUT).vertices().toList
      .map(new TitanChromosome(graph, _))
  }

  def createChromosome(name: String): TitanChromosome = {
    val chromosomeVertex = graph.addVertex(TitanChromosome.label)
    chromosomeVertex.setProperty(TitanChromosome.nameProperty, name)
    val chromosomeEdge = vertex.addEdge(TitanChromosomeEdge.label, chromosomeVertex)
    chromosomeEdge.setProperty(TitanChromosomeEdge.nameProperty, name)
    graph.commit()
    new TitanChromosome(graph, chromosomeVertex)
  }
}
