package ohnosequences.obesnp

import java.io.{PrintWriter, File}

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

}
