package ohnosequences.obesnp

import ohnosequences.obesnp.titan._
import org.junit.Test
import org.junit.Assert._
import scala.collection.JavaConversions._


class Tests {

  val database = Database.create(false, CLI.getWorkingDirectory)
  val graph = database.graph

  @Test
  def snpTest(): Unit = {
    if(!database.isModuleImported(HG38dbSNP)) {
      println("error: HG38dbSNP must be imported for this test")
    } else {
      val snpName = "rs9491696"
      val snp = TitanSNP.byName(graph, snpName)
      assertEquals(snpName, snp.map(_.name).getOrElse(""))
      assertEquals("fake", snp.map(_.vertex.getProperty[String](TitanSNP.fakeProperty)).getOrElse(""))
    }
  }

  @Test
  def chrTest(): Unit = {
    if(!database.isModuleImported(HG38dbSNP)) {
      println("error: HG38dbSNP must be imported for this test")
    } else {
      val hg38 = TitanReference.getOrCreateReference(database.graph, "hg38")
      val chr = hg38.getChromosome("chr6")
      assertEquals(chr.map(_.name).getOrElse(""), "chr6")
    }
  }

  @Test
  def chrTest2(): Unit = {
    if(!database.isModuleImported(HG38dbSNP)) {
      println("error: HG38dbSNP must be imported for this test")
    } else {
      val hg38 = TitanReference.getOrCreateReference(database.graph, "hg38")
      val chr = hg38.getChromosome("chr6")
      //println(chr.map(_.getSNPs.map(_.name)))
      val snp = chr.flatMap(_.getSNP(127131493))
      //println(snp)
      assertEquals("rs9491696", snp.map(_.name).getOrElse(""))
    }
  }

  @Test
  def titanPosition(): Unit = {
    if(!database.isModuleImported(HG38dbSNP)) {
      println("error: HG38dbSNP must be imported for this test")
    } else {
      val hg38 = TitanReference.getOrCreateReference(database.graph, "hg38")
      val chr = hg38.getChromosome("chr6")
      //println(chr.map(_.getSNPs.map(_.name)))
      val snp = chr.flatMap(_.getSNP(127131493))

      //println(snp)
      assertEquals(127131493L, snp.map(_.position.startPosition).getOrElse(0))
    }
  }



}
