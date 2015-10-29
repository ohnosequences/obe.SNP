package ohnosequences.obesnp.titan

import java.io.File

abstract class Module(val name: String) {
  def install(database: Database, workingDirectory: File): Unit = {
    if (database.isModuleImported(Module.this)) {
      println("warning: module " + name + " has been already imported")
    } else {
      install0(workingDirectory)
      database.setAsImported(Module.this)
      database.graph.commit()
      database.shutdown()
    }
  }

  def install0(workingDirectory: File)
}

case object HG19dbSNP extends Module("hg19dbSNP") {
  override def install0(workingDirectory: File): Unit = {
    val database = Database.create(delete = false, workingDirectory)
    val file = new File(workingDirectory, "hg19.dbSNP.txt.gz")
    val snps = new File(workingDirectory, "snps.txt")
    Download.downloadHG19dbSNP(file)
    val reference = database.hg19
    val snpsInfo = Import.obesitySNPs(snps)
    Import.ucscSNPcommon(file, database.graph, reference, snpsInfo)
  }
}

case object HG38dbSNP extends Module("hg38dbSNP") {

  override def install0(workingDirectory: File): Unit = {
    val database = Database.create(delete = false, workingDirectory)
    val file = new File(workingDirectory, "hg38.dbSNP.txt.gz")
    val snps = new File(workingDirectory, "snps.txt")
    Download.downloadHG38dbSNP(file)
    val reference = database.hg38
    val snpsInfo = Import.obesitySNPs(snps)
    Import.ucscSNPcommon(file, database.graph, reference, snpsInfo)
  }

}