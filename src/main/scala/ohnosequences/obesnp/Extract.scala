package ohnosequences.obesnp

import java.io.{FileOutputStream, BufferedOutputStream, File}
import java.util.zip.GZIPOutputStream

import htsjdk.tribble.{TribbleIndexedFeatureReader, Tribble}
import htsjdk.tribble.index.{Index, IndexFactory}
import htsjdk.tribble.util.LittleEndianOutputStream
import htsjdk.variant.bcf2.BCF2Codec
import htsjdk.variant.variantcontext.VariantContext

import scala.collection.JavaConversions._

import htsjdk.variant.vcf.{VCFCodec, VCFFileReader}
import ohnosequences.obesnp.titan.{TitanReference, Database}

/**
 * Created by evdokim on 29/10/2015.
 */
object Extract {

  def snps(database: Database, reference: TitanReference) = {
    //reference.getChromosomes.flatMap()
  }

  def writeIndex(idx: Index, idxFile: File) {
    var stream: LittleEndianOutputStream = null
    try {
      if (idxFile.getName.endsWith(".gz") || idxFile.getName.endsWith("idx.gz")) {
        stream = new LittleEndianOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(idxFile))))
      } else {
        stream = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(idxFile)))
      }
      idx.write(stream);
    } finally {
      if (stream != null) {
        stream.close()
      }
    }
  }

  def fromVCF(reference: TitanReference, file: File, chromosomeNaming: ChromosomeNaming): List[VariantContext] = {
    //val indexed =
    val codec = new VCFCodec()
    println("indexing file " + file.getAbsolutePath)
   // val index = IndexFactory.createIntervalIndex(file, codec)
    //val indexFile = Tribble.indexFile(file)

    //val indexFile = new File(file.getAbsolutePath.replace(".vcf", ".vcf.idx"))
    // println("writing index to " + indexFile.getAbsolutePath)
    // writeIndex(index, indexFile)
    //index.
    //println(indexed.getAbsolutePath)
    val reader = new VCFFileReader(file)

    println("loading " + file.getAbsolutePath)
   // val reader = new TribbleIndexedFeatureReader(file.getAbsolutePath, codec, index)
    var counter = 0L

    //println(reader.getFileHeader)

    //    reader.iterator().foreach { ctx =>
    //      counter += 1
    //
    //      if (counter % 1000 == 0) {
    //        println(counter + " lines processed")
    //      }
    //
    //
    //      reference.getChromosome(ctx.getContig) match {
    //        case Some(chr) =>
    //          chr.getSNP(ctx.getStart - 1).foreach { snp =>
    //  //          val s = writer.encode(ctx)
    //  //          res.println(s)
    //            println(snp.name)
    //          }
    //        case None => println("couldn't find " + ctx.getContig)
    //      }
    //    }


    val res = reference.getChromosomes.flatMap { chromosome =>
      chromosome.getSNPPositions.flatMap { snpPos =>
        //println("querying " + snpPos.snp.name + " " + newName + ":" + snpPos.startPosition)
        reader.query(chromosomeNaming.encode(chromosome.name), snpPos.startPosition.toInt + 1, snpPos.startPosition.toInt + 2).toList
      }
    }

//    println("small test")
//    println(reader.query("10", 60753, 60754).hasNext())
    reader.close()
    res
  }

}
