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



}
