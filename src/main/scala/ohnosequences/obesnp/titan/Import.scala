package ohnosequences.obesnp.titan

import java.io.{FileInputStream, File}
import java.util.zip.{ZipFile, GZIPInputStream}

import scala.collection.JavaConversions._

import com.thinkaurelius.titan.core.TitanGraph

import scala.collection.mutable

trait AnySNPInfo

case class SNPInfo(riskAllele: String, nearsetGene: String, se: Double) extends AnySNPInfo

object Import {


  def obesitySNPs(file: File): Map[String, SNPInfo] = {
    io.Source.fromFile(file).getLines().map { line =>
      val raw = line.trim.split("\\s+")
      val name = raw(0)
      val nearestGene = raw(2)
      val riskAllele = raw(3)
      val se = raw(5).replace(',', '.').toDouble
      (name, SNPInfo(riskAllele, nearestGene, se))
    }.toMap
  }


  def ucscSNPcommon[SI <: SNPInfo](snp142CommonFile: File, graph: TitanGraph, reference: TitanReference, snps: Map[String, SI]): Unit = {
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(snp142CommonFile)))
    var counter = 0


    source.getLines().foreach { line =>
      val raw = line.trim.split('\t')
      if(raw.length < 10) {
        print("warning: couldn't parse line " + line)
      } else if (snps.contains(raw(4))) {

        val chromosome = GlobalBench.bench.bench("queryChrByName"){
          reference.getOrCreateChromosome(raw(1))
        }

       // chromosome.addSNP(raw(2).toLong, raw(8), raw(4))

        val snp = TitanSNP.getOrCreate(graph, raw(4))
        chromosome.addSNP(raw(2).toLong, raw(8), snp)
        //println("chr: " + chromosome.name + " snp:" + snp.name + " pos:" + raw(2))
      }

      if (counter % 10000 == 0) {
        println(counter + " processed")
      }

      if (counter % 100 == 0) {
        graph.commit()
      }
      counter += 1
    }
    graph.commit()
    GlobalBench.bench.printStats()
  }

  def ncbiPositionsImport(seqGeneFile: File, assembly: String, graph: TitanGraph, reference: TitanReference): Unit = {
    //9606	12	126927027	126957331	+	NT_009755.19	4346404	4376708	+	LOC100128554	GeneID:100128554	GENE	GRCh37.p13-Primary Assembly	-
    val seqGeneRegexp = "9606\\s+([^\\s]+)\\s+(\\d+)\\s+(\\d+).+GeneID:(\\d+)\\s+GENE\\s+$assembly$.+"
      .replace("$assembly$", assembly)
      .r
    //"GRCh37\\.p13"
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(seqGeneFile)))

    val chromosomes = new mutable.HashSet[String]()
    var counter = 0
    source.getLines().foreach {
      case seqGeneRegexp(chr, start, end, geneID) => {
        //  println(chr + ":" + start + "-" + end + " " + geneID)
        val chName = chr.split('|')(0)
        chromosomes += chName

        val chromosome = reference.getOrCreateChromosome(chName)

        graph.query()
          .has("label", TitanGene.label)
          .has(TitanGene.geneIDProperty, geneID).vertices().foreach { geneVertex =>
          chromosome.addPosition(start.toLong, end.toLong, geneVertex)
        }

        if (counter % 500 == 0) {
          println(counter + " processed")
        }
        counter += 1
      }
      case line => {
        if (line.contains("GENE") && line.contains("GRCh3")) {
          println("error unexpected line: " + line)
        }
      }
    }
    graph.commit()
    println(counter)
    println(chromosomes)
  }


  def addGene(graph: TitanGraph, uniprotId: String, geneIDs: List[String], geneSymbol: String, mimId: String, acs: List[String]): Unit = {

    if (uniprotId.isEmpty) {
      // println("error: uniprotId is empty GeneID:" + geneID + " Symbol:" + geneSymbol)
    }
    if (geneSymbol.isEmpty) {
      //println("warning: Gene Symbol is empty Id:" + uniprotId + " GeneID:" + geneID)
    }

    val geneIdsLong: List[Long] = geneIDs.flatMap { id =>
      if (id.matches("\\d+")) {
        Some(id.toLong)
      } else {
        None
      }
    }

    graph.query().has("label", TitanGene.label).has(TitanGene.uniprotIDProperty, uniprotId).vertices().iterator().toList match {

      case Nil => {
        //should create new gene
        val vertex = graph.addVertexWithLabel(TitanGene.label)
        vertex.setProperty(TitanGene.uniprotIDProperty, uniprotId)
        vertex.setProperty(TitanGene.geneSymbolProperty, geneSymbol)

        geneIdsLong.foreach { geneId =>
          vertex.addProperty(TitanGene.geneIDProperty, geneId)
        }

        acs.foreach { ac =>
          vertex.addProperty(TitanGene.uniprotACProperty, ac)
        }

        if (!mimId.isEmpty) {
          vertex.addProperty(TitanGene.mimProperty, mimId)
        }



      }
      case oneVertex :: Nil => {
        println("uniprot id is not unique for " + uniprotId)
      }

      case _ => {
        println("uniprot id is not unique for " + uniprotId)
      }
    }
  }


  def importUniprot(uniprotFile: File, graph: TitanGraph): Unit = {
    println("importing Uniprot from " + uniprotFile.getName)
    val source = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(uniprotFile)))
    var geneSymbol = ""
    var uniprotId = ""
    var mim = ""
    var geneIDs = new mutable.ArrayBuffer[String]()

    var counter = 0

    val idRegexp = """ID\s+(\w+)\s+.+""".r

    //DR   MIM; 608579; phenotype.
    val mimRegexp = """DR\s+MIM;\s+(\d+);\s+phenotype.+""".r

    //DR   GeneID; 7529; -.
    //DR   GeneID; 200316; -.
    val geneIdRegexp = """DR\s+GeneID;\s+(\d+);.+""".r

    //GN   Name=HLA-A; Synonyms=HLAA;
    val geneSymbolRegexp = """GN\s+Name=([^;\s]+).+""".r

    var acs = new mutable.ArrayBuffer[String]()

    //AC   Q92892; Q92893;
    val acRegexp = """AC\s+(.+)""".r


    var first = true
    source.getLines().foreach {
      case idRegexp(rid) => {
        if (counter % 1000 == 0) {
          println(counter + " processed")
        }
        if (first) {
          first = false
        } else {
          addGene(graph, uniprotId, geneIDs.toList, geneSymbol, mimId = mim, acs.toList)
          geneSymbol = ""
          //uniprotName = ""
          mim = ""
          geneIDs.clear()
          acs.clear()
        }
        uniprotId = rid
        counter += 1
      }

      case geneSymbolRegexp(symbol) => {
        geneSymbol = symbol
      }

      case mimRegexp(mimId) => {
        mim = mimId
      }
      case geneIdRegexp(id) => {
        geneIDs += id
      }

      case acRegexp(acsRaw) => {
        acsRaw.split(";\\s*").filterNot(_.isEmpty).foreach { ac =>
          acs += ac
        }
      }
      case _ => ()
    }
    addGene(graph, uniprotId, geneIDs.toList, geneSymbol, mimId = mim, acs.toList)
    graph.commit()
  }




}
