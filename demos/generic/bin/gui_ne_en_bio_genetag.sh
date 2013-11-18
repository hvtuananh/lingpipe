CLASS=com.aliasi.demo.demos.NamedEntityDemo
ARGS="com.aliasi.tokenizer.IndoEuropeanTokenizerFactory,com.aliasi.sentences.MedlineSentenceModel,/models/ne-en-bio-genetag.HmmChunker,Biomedical English trained on NCBI's GeneTag Corpus"

CMD=com.aliasi.demo.framework.DemoGUI

CP=../../:../lingpipe-demos.jar:../../../lingpipe-4.1.0.jar:../../lib/nekohtml-1.9.14.jar:../../lib/xercesImpl-2.9.1.jar:../../lib/xml-apis-2.9.1.jar
java -cp $CP $CMD -demoConstructor=$CLASS -demoConstructorArgs=$ARGS $1 $2 $3 $4 $5 $6 $7 $8 $9
