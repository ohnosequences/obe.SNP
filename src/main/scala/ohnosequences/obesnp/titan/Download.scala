package ohnosequences.obesnp.titan

import java.io.File
import java.net.URL

object Download {

  def download(name: String, url: URL, file: File, downloadTwice: Boolean): Unit = {
    import sys.process._
    println("downloading " + name + " from " + url)
    if (!file.exists()) {
      (url #> file).!!
      println("downloaded")
    } else {
      println(file.getName + " has been already downloaded")
    }
  }

  def downloadUniprot(file: File): Unit = {
    val uniprotURL = new URL("ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_sprot_human.dat.gz")
    download("uniprot", uniprotURL, file, false)
  }

  def downloadHG38dbSNP(file: File): Unit = {
    val url = new URL("http://hgdownload.soe.ucsc.edu/goldenPath/hg38/database/snp142Common.txt.gz")
    download("hg38dbSNP", url, file, false)
  }

  def downloadHG19dbSNP(file: File): Unit = {
    val url = new URL("http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/snp142Common.txt.gz")
    download("hg19dbSNP", url, file, false)
  }


}
