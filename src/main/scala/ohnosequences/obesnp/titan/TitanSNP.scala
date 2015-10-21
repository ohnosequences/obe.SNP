package ohnosequences.obesnp.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.Vertex


import ohnosequences.obesnp.SNP

/**
 * Created by evdokim on 20/10/2015.
 */
class TitanSNP(graph: TitanGraph, val vertex: Vertex) extends SNP {

  override def name: String = {
    vertex.getProperty(TitanSNP.nameProperty)
  }
}

object TitanSNP {
  val label = "SNP"
  val nameProperty = "SNP_NAME"
  val fakeProperty = "SNP_FAKE"

  def byName(graph: TitanGraph, name: String): Option[TitanSNP] = {
    val it = graph.query()
      .has("label", TitanSNP.label)
      .has(TitanSNP.nameProperty, name)
      .vertices().iterator()

    if (it.hasNext) {
      Some(new TitanSNP(graph, it.next()))
    } else {
      None
    }
  }

  def create(graph: TitanGraph, name: String): TitanSNP = {
    val vertex = graph.addVertexWithLabel(TitanSNP.label)
    vertex.setProperty(TitanSNP.nameProperty, name)
    vertex.setProperty(TitanSNP.fakeProperty, "fake")

    new TitanSNP(graph, vertex)
  }

  def getOrCreate(graph: TitanGraph, name: String): TitanSNP = {
    GlobalBench.bench.bench("querySNPbyName"){
      byName(graph, name)
    }
      .getOrElse(
          GlobalBench.bench.bench("createSNP"){
            create(graph, name)
          }
        )
  }

}
