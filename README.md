# obe.SNP
obe.SNP is a tool for extraction genomic information relevant to obesity.

## Databases
To start using obe.SNP the databases with known variants (dbSNP) should be downloaded.
obe.SNP have special commands that download the databases from UCSC Genome Browser:

```
obe import dbSNP hg38
```

and

```
obe import dbSNP hg19
```

The second database has positions for the old version of genome reference.
Although this reference is outdated there are still a lot of data that are using it
(e.g. 1000 Genomes).

## Associations file
Another thing that is needed to start using obe.SNP is a file that contains a list
of variants associated with obesity. obe.SNP is shipped with the list included in
the suplementary materials of [a recent obesity study](http://www.ncbi.nlm.nih.gov/pubmed/25994509).

## Input
obe.SNP uses VCF format as an input and output.

## List of commands

### Extract
`obe extract [hg19|hg38] <inputFile>` -- extracts variants related to obesity from
the input file.

### Import

`obe import dbSNP hg39` -- download and import dbSNP for hg19 positions.

`obe import dbSNP hg38` -- download and import dbSNP for hg38 positions.

### Database

`obe database reset` -- reset the database.







Extraction obesity related variants from VCF files.
