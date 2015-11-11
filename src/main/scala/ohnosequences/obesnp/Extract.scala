package ohnosequences.obesnp

import java.io.{FilenameFilter, PrintWriter, File}

import htsjdk.variant.variantcontext.VariantContext

import scala.collection.JavaConversions._

import htsjdk.variant.vcf.{VCFEncoder, VCFFileReader}
import ohnosequences.obesnp.titan.{Database, TitanReference}

/**
 * Created by evdokim on 29/10/2015.
 */
object Extract {

  def fromVCF(reference: TitanReference, file: File, chromosomeNaming: ChromosomeNaming): (VCFFileReader, List[VariantContext]) = {
    val reader = new VCFFileReader(file)

    val res = reference.getChromosomes.flatMap { chromosome =>
      chromosome.getSNPPositions.flatMap { snpPos =>
        //println("querying " + snpPos.snp.name + " " + newName + ":" + snpPos.startPosition)
        reader.query(chromosomeNaming.encode(chromosome.name), snpPos.startPosition.toInt + 1, snpPos.startPosition.toInt + 2).toList
      }
    }
    (reader, res)
  }

  def extractAndWrite(reference: TitanReference, vcfFile: File, chromosomeNaming: ChromosomeNaming): Unit = {
    val (reader, contexts) = Extract.fromVCF(reference, vcfFile, chromosomeNaming)
    val resultFile = new File(vcfFile.getAbsolutePath.replace(".vcf", ".filt.vcf"))
    Extract.writeVCF(reader, contexts, resultFile)
  }

//
//  def fromVCFs(reference: TitanReference, directory: File, chromosomeNaming: ChromosomeNaming): Option[(VCFFileReader, List[List[VariantContext]])] = {
//    val vcfs = directory.listFiles(new FilenameFilter {
//      override def accept(dir: File, name: String): Boolean = name.endsWith(".vcf")
//    }).toList
//
//    vcfs match {
//      case Nil => None
//      case h :: tail => {
//        Some(
//          new VCFFileReader(h) ->
//          vcfs.map { file =>
//            val reader = new VCFFileReader(file)
//            val res = reference.getChromosomes.flatMap { chromosome =>
//              chromosome.getSNPPositions.flatMap { snpPos =>
//                //println("querying " + snpPos.snp.name + " " + newName + ":" + snpPos.startPosition)
//                reader.query(chromosomeNaming.encode(chromosome.name), snpPos.startPosition.toInt + 1, snpPos.startPosition.toInt + 2).toList
//              }
//            }
//            reader.close()
//            res
//          }
//        )
//      }
//    }
//
//  }

  def writeVCF(reader: VCFFileReader, variants: List[VariantContext], resultFile: File) = {
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

    variants.foreach { ctx =>
      val s = writer.encode(ctx)
      res.println(s)
    }
    res.close()
    reader.close()
  }

  def writeVCFs(reader: VCFFileReader, variants: List[VariantContext], resultFile: File) = {
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

    variants.foreach { ctx =>
      val s = writer.encode(ctx)
      res.println(s)
    }
    res.close()
    reader.close()
  }

}
