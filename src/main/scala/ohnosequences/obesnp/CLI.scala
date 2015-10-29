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

      case "vcf" :: "extract" :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)
        val contexts = Extract.fromVCF(database.hg38, vcfFile, OneThousandGenomes)
        println(contexts.size)
        database.shutdown()
      }

      case "vcf" :: "extract" :: "hg19" :: fileName :: Nil => {
        val database = Database.create(delete = false, getWorkingDirectory)
        val vcfFile = new File(getWorkingDirectory, fileName)
        val contexts = Extract.fromVCF(database.hg19, vcfFile, OneThousandGenomes)
        println(contexts.size)
        database.shutdown()
      }

      case "vcf" :: "merge" :: directory :: Nil => {
        println(args)
      }

      case "vcf" :: "filter" :: fileName :: Nil => {
        val reader = new VCFFileReader(new File(getWorkingDirectory, fileName), false)
        val resultFile = new File(getWorkingDirectory, fileName.replace(".vcf", ".filtered.vcf"))
        val res = new PrintWriter(resultFile)
        val writer  = new VCFEncoder(reader.getFileHeader, false, false)

        reader.getFileHeader.getMetaDataInInputOrder.foreach { line =>
          res.println("##" + line.toString)
        }
        res.print("#")
        reader.getFileHeader.getHeaderFields.foreach { field =>
          res.print(field.toString + "\t")
        }
        res.print("FORMAT\t")
        reader.getFileHeader.getGenotypeSamples.foreach { field =>
          res.print(field + "\t")
        }
        res.println()


        val database = Database.create(delete = false, getWorkingDirectory)

        val hg38 = TitanReference.getOrCreateReference(database.graph, "hg38")

        var counter = 0

        reader.iterator().foreach { ctx =>

          counter += 1

          if (counter % 1000 == 0) {
            println(counter + " lines processed")
          }

//          val s = writer.encode(ctx)
//          res.println(s)

          hg38.getChromosome(ctx.getContig).foreach { chr =>
            chr.getSNP(ctx.getStart - 1).foreach { snp =>
              val s = writer.encode(ctx)
              res.println(s)
            }
          }
        }
        res.close()
        reader.close()
        database.shutdown()
      }

      case "help" :: Nil => printUsage()

      case _ => printUsage()


    }

  }
}
