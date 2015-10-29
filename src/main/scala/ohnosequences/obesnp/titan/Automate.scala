package ohnosequences.obesnp.titan

import java.io.File

import htsjdk.variant.vcf.VCFFileReader

/**
 * Created by evdokim on 29/10/2015.
 */
object Automate {
  def readVCFs(files: List[File]) = {
    val reader = new VCFFileReader(files.head, false)
    //reader.
  }

}
