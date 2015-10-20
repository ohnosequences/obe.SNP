package ohnosequences.obesnp

trait Chromosome {
  def name: String

  def getGenes: List[Gene]

  //def getSNPs: List[SNP]
}
