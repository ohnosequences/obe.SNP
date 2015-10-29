package ohnosequences.obesnp.titan

import java.io.File
import com.thinkaurelius.titan.core._
import com.thinkaurelius.titan.graphdb.query.TitanPredicate
import com.tinkerpop.blueprints.{Direction, Vertex}
import ohnosequences.obesnp._


object Database {



  def create(delete: Boolean, workingDirectory: File): Database = {
    val dbLocation = new File(workingDirectory, "database")

    if (delete) {
      org.apache.commons.io.FileUtils.deleteDirectory(dbLocation)
    }

    val g = TitanFactory.build()
      .set("storage.backend", "berkeleyje")
      .set("storage.directory", dbLocation.getAbsolutePath)
      // .set("storage.backend","cassandra")
      // .set("storage.hostname","127.0.0.1")
      .set("attributes.allow-all", false)
      .set("query.force-index", true)
      .set("query.ignore-unknown-index-key", true)
      .open()

    val mgmt = g.getManagementSystem()

    if (delete) {
      val importedLabel = mgmt.makeVertexLabel("imported").make()
      val moduleName = mgmt.makePropertyKey("module").dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("importedByModuleName", classOf[Vertex]).addKey(moduleName).indexOnly(importedLabel).buildCompositeIndex()
      val fakeName = mgmt.makePropertyKey("fake").dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("importedByFake", classOf[Vertex]).addKey(fakeName).indexOnly(importedLabel).buildCompositeIndex()

      val referenceLabel = mgmt.makeVertexLabel(TitanReference.label).make()
      val referenceName = mgmt.makePropertyKey(TitanReference.nameProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("referenceBy" + TitanReference.nameProperty, classOf[Vertex]).addKey(referenceName).indexOnly(referenceLabel).buildCompositeIndex()

      val chromosomeLabel = mgmt.makeVertexLabel(TitanChromosome.label).make()
      mgmt.makePropertyKey(TitanChromosome.nameProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()

      val chromosomeEdgeLabel = mgmt.makeEdgeLabel(TitanChromosomeEdge.label).multiplicity(Multiplicity.ONE2MANY).make()
      val chromosomeEdgeName = mgmt.makePropertyKey(TitanChromosomeEdge.nameProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildEdgeIndex(chromosomeEdgeLabel, "chromosomeByName", Direction.OUT, Order.ASC, chromosomeEdgeName)


      val positionLabel = mgmt.makeEdgeLabel(TitanGenePosition.label).multiplicity(Multiplicity.MULTI).make()
      val startPosition = mgmt.makePropertyKey(TitanGenePosition.startProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()
      mgmt.makePropertyKey(TitanGenePosition.endProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()


      mgmt.buildEdgeIndex(positionLabel, "geneByStart", Direction.OUT, Order.DESC, startPosition)

      val snpPositionLabel = mgmt.makeEdgeLabel(TitanSNPPosition.label).multiplicity(Multiplicity.MULTI).make()
      val snpStartPosition = mgmt.makePropertyKey(TitanSNPPosition.startProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildEdgeIndex(snpPositionLabel, "snpByStart", Direction.OUT, Order.DESC, snpStartPosition)


      val geneLabel = mgmt.makeVertexLabel(TitanGene.label).make()
      val geneIdProp = mgmt.makePropertyKey(TitanGene.geneIDProperty).dataType(classOf[java.lang.Long]).cardinality(Cardinality.LIST).make()
      mgmt.buildIndex("geneBy" + TitanGene.geneIDProperty, classOf[Vertex]).addKey(geneIdProp).indexOnly(geneLabel).buildCompositeIndex()

      val geneUniprotACProp = mgmt.makePropertyKey(TitanGene.uniprotACProperty).dataType(classOf[String]).cardinality(Cardinality.LIST).make()
      mgmt.buildIndex("geneBy" + TitanGene.uniprotACProperty, classOf[Vertex]).addKey(geneUniprotACProp).indexOnly(geneLabel).buildCompositeIndex()

      val geneUniprotIdProp = mgmt.makePropertyKey(TitanGene.uniprotIDProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.uniprotIDProperty, classOf[Vertex]).addKey(geneUniprotIdProp).indexOnly(geneLabel).buildCompositeIndex()

      val geneMIMProp = mgmt.makePropertyKey(TitanGene.mimProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.mimProperty, classOf[Vertex]).addKey(geneMIMProp).indexOnly(geneLabel).buildCompositeIndex()

      val geneSymbolPropery = mgmt.makePropertyKey(TitanGene.geneSymbolProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("geneBy" + TitanGene.geneSymbolProperty, classOf[Vertex]).addKey(geneSymbolPropery).indexOnly(geneLabel).buildCompositeIndex()


      val snpLabel = mgmt.makeVertexLabel(TitanSNP.label).make()
      val snpName = mgmt.makePropertyKey(TitanSNP.nameProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      val snpFake = mgmt.makePropertyKey(TitanSNP.fakeProperty).dataType(classOf[String]).cardinality(Cardinality.SINGLE).make()
      mgmt.buildIndex("snpBy" + TitanSNP.nameProperty, classOf[Vertex]).addKey(snpName).indexOnly(snpLabel).buildCompositeIndex()
      mgmt.buildIndex("snpByFake", classOf[Vertex]).addKey(snpFake).indexOnly(snpLabel).buildCompositeIndex()

      mgmt.commit()
    }
    new Database(g)
  }
}

class Database(val graph: TitanGraph) {

  def hg38 = TitanReference.getOrCreateReference(graph, "hg38")

  def hg19 = TitanReference.getOrCreateReference(graph, "hg19")

  def shutdown(): Unit = {
    graph.commit()
    try {
      graph.shutdown()
    } catch {
      case e: IllegalStateException => println("warning: " + e.toString)
    }
    println("databased shutdown")
  }


  def isModuleImported(module: Module): Boolean = {
    val it = graph
      .query()
      .has("label", "imported")
      .has("module", module.name)
      .vertices()
      .iterator()
    it.hasNext
  }

  def setAsImported(module: Module): Unit = {
    println("set imported: " + module.name)
    val importVertex = graph.addVertexWithLabel("imported")
    importVertex.setProperty("module", module.name)
    importVertex.setProperty("fake", "fake")
    graph.commit()
  }

  def listImportedModules(): List[String] = {
    import scala.collection.JavaConversions._
    graph
      .query()
      .has("label", "imported")
      .has("fake", "fake")
      .vertices()
      .iterator()
      .toList
      .map { v => v.getProperty("module").toString }
  }


}
