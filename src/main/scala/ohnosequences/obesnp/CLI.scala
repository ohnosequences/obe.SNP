package ohnosequences.obesnp

import java.io.{PrintWriter, File}
import com.thinkaurelius.titan.core.TitanVertex
import htsjdk.variant.vcf.{VCFEncoder, VCFFileReader}
import ohnosequences.obesnp.titan._
import scala.collection.JavaConversions._
import scala.collection.mutable


object CLI {

  def getWorkingDirectory: File = {
    val jar = new File(this.getClass.getProtectionDomain()
      .getCodeSource().getLocation.toURI)

    if (jar.getName.endsWith(".jar")) {
      jar.getParentFile
    } else {
      jar.getParentFile.getParentFile.getParentFile //for sbt
    }
  }

  def printUsage(): Unit = {
    val usageStream = getClass.getResourceAsStream("/ohnosequences/obesnp/usage")
    println(io.Source.fromInputStream(usageStream).getLines().mkString(System.lineSeparator()))
  }



  def main(args: Array[String]): Unit = {
    //println(args.toList)
    args.toList match {

      case "import" :: "dbSNP" :: "hg38" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        HG38dbSNP.install(database, getWorkingDirectory)
      }

      case "import" :: "dbSNP" :: "hg19" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        HG19dbSNP.install(database, getWorkingDirectory)
      }

      case "modules" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        println("imported modules:")
        database.listImportedModules().foreach { moduleName =>
          println(moduleName)
        }
        database.graph.commit()
        database.shutdown()
      }

      case "database" :: "reset" :: Nil => {
        val database = Database.create(delete = true, getWorkingDirectory)
        database.shutdown()
      }

      case "snps" :: "count" :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val snps = database
          .graph
          .query()
          .has("label", TitanSNP.label)
          .has(TitanSNP.fakeProperty, "fake")
          .vertices().iterator().toList

        println(snps.size)
        database.shutdown()
      }

      case "extract" :: ref :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)
        val reference = if (ref.equals("hg38")) {
          database.hg38
        } else {
          database.hg19
        }
        val (reader, contexts) = Extract.fromVCF(reference, vcfFile, OneThousandGenomes)
        reader.close()
        println(contexts.size)
        database.shutdown()
      }

      case "extract" :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)

        val (reader, contexts) = Extract.fromVCF(database.hg19, vcfFile, OneThousandGenomes)
        reader.close()
        println(contexts.size)
        database.shutdown()
      }


      case "filter" :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)
        val (reader, contexts) = Extract.fromVCF(database.hg19, vcfFile, OneThousandGenomes)
        val resultFile = new File(vcfFile.getAbsolutePath.replace(".vcf", ".filt.vcf"))
        Extract.writeVCF(reader, contexts, resultFile)
        database.shutdown()
      }

      case "filter" :: ref :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)

        val reference = if (ref.equals("hg38")) {
          database.hg38
        } else {
          database.hg19
        }

        val (reader, contexts) = Extract.fromVCF(reference, vcfFile, OneThousandGenomes)
        val resultFile = new File(vcfFile.getAbsolutePath.replace(".vcf", ".filt.vcf"))
        Extract.writeVCF(reader, contexts, resultFile)
        database.shutdown()
      }

      case "help" :: Nil => printUsage()

      case _ => printUsage()


    }

  }
}
